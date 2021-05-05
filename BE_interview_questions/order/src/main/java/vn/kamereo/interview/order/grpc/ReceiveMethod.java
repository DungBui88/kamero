package vn.kamereo.interview.order.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.kamereo.interview.order.ReceiveRequest;
import vn.kamereo.interview.order.ReceiveResponse;
import vn.kamereo.interview.order.dto.Order;
import vn.kamereo.interview.order.dto.OrderStatusTransition;
import vn.kamereo.interview.order.publisher.OrderEvent;
import vn.kamereo.interview.order.publisher.OrderEventPublisher;
import vn.kamereo.interview.order.repository.OrderNotFoundException;
import vn.kamereo.interview.order.repository.OrderRepository;
import vn.kamereo.interview.order.state_machine.OrderStatusStateMachine;
import vn.kamereo.state_machine.InvalidTransitionException;

@Component
public class ReceiveMethod implements ServiceMethod<ReceiveRequest, ReceiveResponse>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveMethod.class);

    private final OrderRepository orderRepository;

    private final OrderEventPublisher orderEventPublisher;

    private final OrderStatusStateMachine statusStateMachine;

    @Autowired
    public ReceiveMethod(
            final OrderRepository orderRepository,
            final OrderEventPublisher orderEventPublisher,
            final OrderStatusStateMachine statusStateMachine
    ) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.statusStateMachine = statusStateMachine;
    }

    @Override
    public void call(final ReceiveRequest request, final StreamObserver<ReceiveResponse> responseObserver) {
        try {
            final Order order = orderRepository.findById(request.getOrderId());
            final Order receivedOrder = statusStateMachine.apply(order, OrderStatusTransition.RECEIVE);
            orderRepository.save(receivedOrder);
            orderEventPublisher.emit(OrderEvent.ORDER_RECEIVED);

            responseObserver.onNext(ReceiveResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (final OrderNotFoundException e) {
            LOGGER.info("Given order not found:", e);
            responseObserver.onError(Status.INVALID_ARGUMENT.withCause(e).withDescription("2").asException());
        } catch (final InvalidTransitionException e) {
            LOGGER.error("Invalid transition", e);
            responseObserver.onError(Status.FAILED_PRECONDITION.withCause(e).withDescription("31").asException());
        } catch (final Exception e) {
            LOGGER.info("Internal server error:", e);
            responseObserver.onError(Status.INTERNAL.withCause(e).withDescription("39").asException());
        }
    }
}
