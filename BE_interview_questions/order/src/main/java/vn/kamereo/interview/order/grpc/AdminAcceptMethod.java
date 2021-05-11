package vn.kamereo.interview.order.grpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.kamereo.interview.order.AcceptRequest;
import vn.kamereo.interview.order.AcceptResponse;
import vn.kamereo.interview.order.publisher.OrderEventPublisher;
import vn.kamereo.interview.order.repository.OrderRepository;
import vn.kamereo.interview.order.state_machine.OrderStatusStateMachine;

@Component
public class AdminAcceptMethod extends AcceptMethod implements ServiceMethod<AcceptRequest, AcceptResponse> {

    @Autowired
    public AdminAcceptMethod(final OrderRepository orderRepository,
                             final OrderEventPublisher orderEventPublisher,
                             final OrderStatusStateMachine adminStatusStateMachine) {
        super(orderRepository, orderEventPublisher, adminStatusStateMachine);
    }
}
