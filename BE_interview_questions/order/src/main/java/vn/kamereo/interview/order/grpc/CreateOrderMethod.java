package vn.kamereo.interview.order.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.kamereo.interview.order.CreateOrderRequest;
import vn.kamereo.interview.order.CreateOrderResponse;
import vn.kamereo.interview.order.dto.Order;
import vn.kamereo.interview.order.dto.OrderItem;
import vn.kamereo.interview.order.publisher.OrderEventPublisher;
import vn.kamereo.interview.order.publisher.OrderEvent;
import vn.kamereo.interview.order.repository.OrderRepository;

import java.util.stream.Collectors;

@Component
public class CreateOrderMethod implements ServiceMethod<CreateOrderRequest, CreateOrderResponse>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateOrderMethod.class);

    private final OrderRepository orderRepository;

    private final OrderEventPublisher publisher;

    @Autowired
    public CreateOrderMethod(final OrderRepository orderRepository, final OrderEventPublisher publisher) {
        this.orderRepository = orderRepository;
        this.publisher = publisher;
    }

    @Override
    public void call(final CreateOrderRequest request, final StreamObserver<CreateOrderResponse> responseObserver) {
        final Order order = new Order(
                request.getUserId(),
                request.getItemsList()
                        .stream()
                        .map(item -> new OrderItem(item.getProductId(), item.getPrice(), item.getQuantity()))
                        .collect(Collectors.toList())
        );

        try {
            final Order savedOrder = orderRepository.save(order);
            publisher.emit(OrderEvent.ORDER_CREATE);

            responseObserver.onNext(CreateOrderResponse.newBuilder()
                    .setOrderId(savedOrder.getOrderId())
                    .build()
            );
            responseObserver.onCompleted();
        } catch (final Exception e) {
            LOGGER.info("Internal server error:", e);
            responseObserver.onError(Status.INTERNAL.withCause(e).withDescription("9").asException());
        }
    }
}
