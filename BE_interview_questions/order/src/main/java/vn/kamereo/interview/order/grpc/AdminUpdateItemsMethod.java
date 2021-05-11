package vn.kamereo.interview.order.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.kamereo.interview.order.AdminUpdateRequest;
import vn.kamereo.interview.order.AdminUpdateResponse;
import vn.kamereo.interview.order.dto.Order;
import vn.kamereo.interview.order.dto.OrderItem;
import vn.kamereo.interview.order.dto.OrderStatus;
import vn.kamereo.interview.order.repository.OrderNotFoundException;
import vn.kamereo.interview.order.repository.OrderRepository;
import vn.kamereo.state_machine.InvalidTransitionException;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AdminUpdateItemsMethod implements ServiceMethod<AdminUpdateRequest, AdminUpdateResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUpdateItemsMethod.class);

    private final OrderRepository orderRepository;

    @Autowired
    public AdminUpdateItemsMethod(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void call(AdminUpdateRequest request, StreamObserver<AdminUpdateResponse> responseObserver) {
        try {
            final Order order = orderRepository.findById(request.getOrderId());
            if (order.getStatus() != OrderStatus.ACCEPTED
                    && order.getStatus() != OrderStatus.CANCELED) {
                throw InvalidOrderStateException.from(order, order.getStatus());
            }

            final ArrayList<OrderItem> currentItems = new ArrayList<>(order.getOrderItems());
            final ArrayList<OrderItem> addingItems = new ArrayList<>(request.getItemsList().stream()
                    .map(item -> new OrderItem(item.getProductId(), item.getPrice(), item.getQuantity()))
                    .collect(Collectors.toList()));

            if (currentItems.isEmpty()) {
                currentItems.addAll(addingItems);
            } else {
                currentItems.removeIf(item -> addingItems.stream()
                        .anyMatch(addingItem -> addingItem.getProductId().equals(item.getProductId())
                                && addingItem.getPrice().equals(item.getPrice())
                                && addingItem.getQuantity() == 0));
                currentItems.addAll(addingItems);
                cleanUpEmptyProduct(currentItems);
                combineProductItems(currentItems);
            }

            order.getOrderItems().clear();
            order.getOrderItems().addAll(currentItems);
            orderRepository.save(order);

            responseObserver.onNext(AdminUpdateResponse.newBuilder()
                    .addAllItems(currentItems.stream().map(item -> AdminUpdateResponse.OrderItem.newBuilder()
                            .setProductId(item.getProductId())
                            .setPrice(item.getPrice())
                            .setQuantity(item.getQuantity())
                            .build()).collect(Collectors.toList())).build());
            responseObserver.onCompleted();
        } catch (final OrderNotFoundException e) {
            LOGGER.info("Given order not found:", e);
            responseObserver.onError(Status.INVALID_ARGUMENT.withCause(e).withDescription("2").asException());
        } catch (final InvalidTransitionException e) {
            LOGGER.error("Invalid transition", e);
            responseObserver.onError(Status.FAILED_PRECONDITION.withCause(e).withDescription("131").asException());
        } catch (final Exception e) {
            LOGGER.info("Internal server error:", e);
            responseObserver.onError(Status.INTERNAL.withCause(e).withDescription("139").asException());
        }
    }

    private void combineProductItems(final List<OrderItem> items) {
        Set<OrderItem> mergedItems = new HashSet<>();
        if (items.size() > 1) {
            for (int i = 0; i <= items.size() - 1; i++) {
                OrderItem currentItem = items.get(i);
                int quantity = currentItem.getQuantity() == null ? 0 : currentItem.getQuantity();
                if (mergedItems.stream().noneMatch(item -> item.getProductId().equals(currentItem.getProductId())
                        && item.getPrice().equals(currentItem.getPrice()))) {
                    for (int j = i + 1; j <= items.size() - 1; j++) {
                        OrderItem nextItem = items.get(j);
                        if (currentItem.getProductId().equals(nextItem.getProductId())
                                && currentItem.getPrice().equals(nextItem.getPrice())) {
                            quantity += nextItem.getQuantity();
                        }
                    }
                    mergedItems.add(new OrderItem(currentItem.getProductId(), currentItem.getPrice(), quantity));
                }
            }
        }

        items.clear();
        items.addAll(mergedItems);
    }

    private void cleanUpEmptyProduct(ArrayList<OrderItem> items) {
        items.removeIf(item -> item.getQuantity() == null || item.getQuantity() == 0);
    }
}
