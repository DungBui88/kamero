package vn.kamereo.interview.order.repository;

import com.google.common.base.Strings;
import org.springframework.stereotype.Repository;
import vn.kamereo.interview.order.dto.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class DummyOrderRepository implements OrderRepository {

    private final Map<String, Order> stores = new HashMap<>();

    @Override
    public Order save(final Order order) {
        final Order savedOrder = Strings.isNullOrEmpty(order.getOrderId())
                ? order.withOrderId(UUID.randomUUID().toString())
                : order;

        stores.put(savedOrder.getOrderId(), savedOrder);
        return savedOrder;
    }

    @Override
    public Order findById(final String orderId) throws OrderNotFoundException {
        final Order order = stores.get(orderId);
        if (order == null) {
            throw new OrderNotFoundException();
        }
        return order;
    }

}
