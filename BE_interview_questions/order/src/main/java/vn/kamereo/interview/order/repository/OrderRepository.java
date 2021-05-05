package vn.kamereo.interview.order.repository;

import vn.kamereo.interview.order.dto.Order;

public interface OrderRepository {
    Order save(final Order order);

    Order findById(final String orderId) throws OrderNotFoundException;
}
