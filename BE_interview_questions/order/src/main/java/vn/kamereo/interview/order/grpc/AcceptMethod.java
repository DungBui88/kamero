package vn.kamereo.interview.order.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.kamereo.interview.order.AcceptRequest;
import vn.kamereo.interview.order.AcceptResponse;
import vn.kamereo.interview.order.dto.Order;
import vn.kamereo.interview.order.dto.OrderStatusTransition;
import vn.kamereo.interview.order.publisher.OrderEvent;
import vn.kamereo.interview.order.publisher.OrderEventPublisher;
import vn.kamereo.interview.order.repository.OrderNotFoundException;
import vn.kamereo.interview.order.repository.OrderRepository;
import vn.kamereo.interview.order.state_machine.OrderStatusStateMachine;
import vn.kamereo.state_machine.InvalidTransitionException;

@Component
public class AcceptMethod implements ServiceMethod<AcceptRequest, AcceptResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptMethod.class);

    private final OrderRepository orderRepository;

    private final OrderEventPublisher orderEventPublisher;

    private final OrderStatusStateMachine statusStateMachine;

    @Autowired
    public AcceptMethod(
            final OrderRepository orderRepository,
            final OrderEventPublisher orderEventPublisher,
            final OrderStatusStateMachine statusStateMachine
    ) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.statusStateMachine = statusStateMachine;
    }

    @Override
    public void call(final AcceptRequest request, final StreamObserver<AcceptResponse> responseObserver) {
        try {
            final Order order = orderRepository.findById(request.getOrderId());
            applyOrderState(order, OrderStatusTransition.ACCEPT);
            orderEventPublisher.emit(OrderEvent.ORDER_ACCEPTED);

            responseObserver.onNext(AcceptResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (final OrderNotFoundException e) {
            LOGGER.info("Given order not found:", e);
            responseObserver.onError(Status.INVALID_ARGUMENT.withCause(e).withDescription("2").asException());
        } catch (final InvalidTransitionException e) {
            LOGGER.error("Invalid transition", e);
            responseObserver.onError(Status.FAILED_PRECONDITION.withCause(e).withDescription("11").asException());
        } catch (final Exception e) {
            LOGGER.info("Internal server error:", e);
            responseObserver.onError(Status.INTERNAL.withCause(e).withDescription("19").asException());
        }
    }

    protected void applyOrderState(Order order, OrderStatusTransition status) {
        final Order acceptedOrder = statusStateMachine.apply(order, status);
        orderRepository.save(acceptedOrder);
    }
}
