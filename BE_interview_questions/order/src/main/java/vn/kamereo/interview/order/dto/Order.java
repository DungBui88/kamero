package vn.kamereo.interview.order.dto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
@ParametersAreNonnullByDefault
@Nonnull
public class Order {

    @Nullable
    private String orderId;

    private final String userId;

    private final List<OrderItem> orderItems;

    private final OrderStatus status;

    public Order(final String userId, final List<OrderItem> orderItems) {
        this.userId = userId;
        this.orderItems = orderItems;
        status = OrderStatus.PENDING;
    }

    public Order(final String orderId, final String userId, final List<OrderItem> orderItems, final OrderStatus status) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderItems = orderItems;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Order withOrderStatus(final OrderStatus newState) {
        return new Order(
                orderId,
                userId,
                orderItems,
                newState
        );
    }

    public Order withOrderId(final String newId) {
        return new Order(
                newId,
                userId,
                orderItems,
                status
        );
    }
}
