package vn.kamereo.interview.order.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.kamereo.interview.order.CancelRequest;
import vn.kamereo.interview.order.CancelResponse;
import vn.kamereo.interview.order.dto.Order;
import vn.kamereo.interview.order.dto.OrderStatusTransition;
import vn.kamereo.interview.order.publisher.OrderEvent;
import vn.kamereo.interview.order.publisher.OrderEventPublisher;
import vn.kamereo.interview.order.repository.OrderNotFoundException;
import vn.kamereo.interview.order.repository.OrderRepository;
import vn.kamereo.interview.order.state_machine.OrderStatusStateMachine;
import vn.kamereo.state_machine.InvalidTransitionException;

@Component
public class CancelMethod implements ServiceMethod<CancelRequest, CancelResponse>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(CancelMethod.class);

    private final OrderRepository orderRepository;

    private final OrderEventPublisher orderEventPublisher;

    private final OrderStatusStateMachine statusStateMachine;

    @Autowired
    public CancelMethod(
            final OrderRepository orderRepository,
            final OrderEventPublisher orderEventPublisher,
            final OrderStatusStateMachine statusStateMachine
    ) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.statusStateMachine = statusStateMachine;
    }

    @Override
    public void call(final CancelRequest request, final StreamObserver<CancelResponse> responseObserver) {
        try {
            final Order order = orderRepository.findById(request.getOrderId());
            final Order canceledOrder = statusStateMachine.apply(order, OrderStatusTransition.CANCEL);
            orderRepository.save(canceledOrder);
            orderEventPublisher.emit(OrderEvent.ORDER_CANCELED);
            responseObserver.onNext(CancelResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (final OrderNotFoundException e) {
            LOGGER.info("Given order not found:", e);
            responseObserver.onError(Status.INVALID_ARGUMENT.withCause(e).withDescription("2").asException());
        } catch (final InvalidTransitionException e) {
            LOGGER.error("Invalid transition", e);
            responseObserver.onError(Status.FAILED_PRECONDITION.withCause(e).withDescription("21").asException());
        } catch (final Exception e) {
            LOGGER.info("Internal server error:", e);
            responseObserver.onError(Status.INTERNAL.withCause(e).withDescription("29").asException());
        }
    }
}
