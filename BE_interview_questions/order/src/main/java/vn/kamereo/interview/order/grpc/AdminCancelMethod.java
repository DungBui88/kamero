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
import vn.kamereo.interview.order.dto.OrderStatus;
import vn.kamereo.interview.order.publisher.OrderEventPublisher;
import vn.kamereo.interview.order.repository.OrderRepository;
import vn.kamereo.interview.order.state_machine.OrderStatusStateMachine;

@Component
public class AdminCancelMethod extends CancelMethod implements ServiceMethod<CancelRequest, CancelResponse>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminCancelMethod.class);

    @Autowired
    public AdminCancelMethod(final OrderRepository orderRepository,
                             final OrderEventPublisher orderEventPublisher,
                             final OrderStatusStateMachine adminStatusStateMachine) {
        super(orderRepository, orderEventPublisher, adminStatusStateMachine);
    }

    @Override
    public void call(final CancelRequest request, final StreamObserver<CancelResponse> responseObserver) {
        try {
            final Order order = super.getOrderRepository().findById(request.getOrderId());
            if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELED) {
                responseObserver.onError(Status.FAILED_PRECONDITION.withDescription("121").asException());
            } else {
                super.call(request, responseObserver);
            }
        } catch (final Exception e) {
            LOGGER.info("Internal server error:", e);
            responseObserver.onError(Status.INTERNAL.withCause(e).withDescription("29").asException());
        }
    }
}
