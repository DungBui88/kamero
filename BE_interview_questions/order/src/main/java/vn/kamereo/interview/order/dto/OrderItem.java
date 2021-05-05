package vn.kamereo.interview.order.dto;

import lombok.Getter;

@Getter
public class OrderItem {

    private final String productId;
    private final Long price;
    private final Integer quantity;

    public OrderItem(final String productId, final Long price, final Integer quantity) {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }
}
