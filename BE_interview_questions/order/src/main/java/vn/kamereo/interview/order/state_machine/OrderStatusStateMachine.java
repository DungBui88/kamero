package vn.kamereo.interview.order.state_machine;

import org.springframework.stereotype.Component;
import vn.kamereo.interview.order.dto.Order;
import vn.kamereo.interview.order.dto.OrderStatus;
import vn.kamereo.interview.order.dto.OrderStatusTransition;
import vn.kamereo.state_machine.StateMachineBase;

import javax.annotation.Nonnull;

import static vn.kamereo.interview.order.dto.OrderStatus.ACCEPTED;
import static vn.kamereo.interview.order.dto.OrderStatus.CANCELED;
import static vn.kamereo.interview.order.dto.OrderStatus.DELIVERED;
import static vn.kamereo.interview.order.dto.OrderStatus.PENDING;

@Component(value = "statusStateMachine")
public class OrderStatusStateMachine extends StateMachineBase<Order, OrderStatus, OrderStatusTransition> {

    @Override
    protected void configure() {
        transition(OrderStatusTransition.ACCEPT)
                .from(OrderStatus.values())
                .to(ACCEPTED);

        transition(OrderStatusTransition.CANCEL)
                .from(PENDING)
                .to(CANCELED);

        transition(OrderStatusTransition.RECEIVE)
                .from(ACCEPTED)
                .to(DELIVERED);
    }

    @Nonnull
    @Override
    protected OrderStatus getCurrentState(final Order entity) {
        return entity.getStatus();
    }

    @Nonnull
    @Override
    protected Order setState(final Order entity, final OrderStatus newState) {
        return entity.withOrderStatus(newState);
    }
}
