package vn.kamereo.interview.order.grpc;

import com.asarkar.grpc.test.GrpcCleanupExtension;
import com.asarkar.grpc.test.Resources;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import vn.kamereo.interview.order.*;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ExtendWith({
        SpringExtension.class,
        GrpcCleanupExtension.class
})
class AdminUpdateItemsMethodTest {

    @Autowired
    private OrderService orderService;

    private Server server;

    private ManagedChannel channel;

    private OrderServiceGrpc.OrderServiceBlockingStub client;

    @BeforeEach
    void setUp() throws IOException {
        final String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name)
                .addService(orderService)
                .directExecutor()
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(name).directExecutor().build();
        client = OrderServiceGrpc.newBlockingStub(channel);
    }

    @Test
    void testSuccessfulUpdateCurrentItemQuantity(final Resources resources) {
        resources.register(channel, Duration.ofSeconds(1));
        resources.register(server, Duration.ofSeconds(1));

        String productId = UUID.randomUUID().toString();
        final CreateOrderResponse createResponse = client.createOrder(CreateOrderRequest.newBuilder()
                .setUserId(UUID.randomUUID().toString())
                .addItems(CreateOrderRequest.OrderItem.newBuilder()
                        .setProductId(productId)
                        .setPrice(100_000)
                        .setQuantity(10)
                        .build())
                .build()
        );

        client.adminAccept(AcceptRequest.newBuilder()
                .setOrderId(createResponse.getOrderId())
                .build()
        );

        final AdminUpdateResponse response = client.adminUpdateItems(AdminUpdateRequest.newBuilder()
                .setOrderId(createResponse.getOrderId())
                .addItems(AdminUpdateRequest.OrderItem.newBuilder()
                        .setProductId(productId)
                        .setPrice(100_000)
                        .setQuantity(10)
                        .build())
                .build());

        final List<AdminUpdateResponse.OrderItem> results = response.getItemsList();
        assertEquals(1, results.size());
        assertEquals(productId, results.get(0).getProductId());
        assertEquals(100_000, results.get(0).getPrice());
        assertEquals(20, results.get(0).getQuantity());
    }

    @Test
    void testSuccessUpdateAddNewItem(final Resources resources) {
        resources.register(channel, Duration.ofSeconds(1));
        resources.register(server, Duration.ofSeconds(1));

        final CreateOrderResponse createResponse = client.createOrder(CreateOrderRequest.newBuilder()
                .setUserId(UUID.randomUUID().toString())
                .addItems(CreateOrderRequest.OrderItem.newBuilder()
                        .setProductId(UUID.randomUUID().toString())
                        .setPrice(100_000)
                        .setQuantity(10)
                        .build())
                .build()
        );

        client.adminAccept(AcceptRequest.newBuilder()
                .setOrderId(createResponse.getOrderId())
                .build()
        );

        final AdminUpdateResponse response = client.adminUpdateItems(AdminUpdateRequest.newBuilder()
                .setOrderId(createResponse.getOrderId())
                .addItems(AdminUpdateRequest.OrderItem.newBuilder()
                        .setProductId(UUID.randomUUID().toString())
                        .setPrice(100_000)
                        .setQuantity(10)
                        .build())
                .build());

        final List<AdminUpdateResponse.OrderItem> results = response.getItemsList();
        assertEquals(2, results.size());
    }

    @Test
    void testSuccessUpdateRemoveItem(final Resources resources) {
        resources.register(channel, Duration.ofSeconds(1));
        resources.register(server, Duration.ofSeconds(1));

        final CreateOrderResponse createResponse = client.createOrder(CreateOrderRequest.newBuilder()
                .setUserId(UUID.randomUUID().toString())
                .addItems(CreateOrderRequest.OrderItem.newBuilder()
                        .setProductId(UUID.randomUUID().toString())
                        .setPrice(100_000)
                        .setQuantity(10)
                        .build())
                .build()
        );

        client.adminAccept(AcceptRequest.newBuilder()
                .setOrderId(createResponse.getOrderId())
                .build()
        );

        final AdminUpdateResponse response = client.adminUpdateItems(AdminUpdateRequest.newBuilder()
                .setOrderId(createResponse.getOrderId())
                .addItems(AdminUpdateRequest.OrderItem.newBuilder()
                        .setProductId(createResponse.getOrderId())
                        .setPrice(100_000)
                        .setQuantity(0)
                        .build())
                .build());

        final List<AdminUpdateResponse.OrderItem> results = response.getItemsList();
        assertEquals(0, results.size());
    }

    @Test
    void testSuccessUpdateAddNewItemUpdateOldItemAndRemoveOneOldItem(final Resources resources) {
        resources.register(channel, Duration.ofSeconds(1));
        resources.register(server, Duration.ofSeconds(1));

        String productIdToUpdate = UUID.randomUUID().toString();
        String productIdToAdd = UUID.randomUUID().toString();
        String productIdToDelete = UUID.randomUUID().toString();
        String productIdNoUpdate = UUID.randomUUID().toString();
        final CreateOrderResponse createResponse = client.createOrder(CreateOrderRequest.newBuilder()
                .setUserId(UUID.randomUUID().toString())
                .addItems(CreateOrderRequest.OrderItem.newBuilder()
                        .setProductId(productIdNoUpdate)
                        .setPrice(100_000)
                        .setQuantity(1)
                        .build())
                .addItems(CreateOrderRequest.OrderItem.newBuilder()
                        .setProductId(productIdToUpdate)
                        .setPrice(100_000)
                        .setQuantity(2)
                        .build())
                .addItems(CreateOrderRequest.OrderItem.newBuilder()
                        .setProductId(productIdToDelete)
                        .setPrice(100_000)
                        .setQuantity(3)
                        .build())
                .build()
        );

        client.adminAccept(AcceptRequest.newBuilder()
                .setOrderId(createResponse.getOrderId())
                .build()
        );

        final AdminUpdateResponse response = client.adminUpdateItems(AdminUpdateRequest.newBuilder()
                .setOrderId(createResponse.getOrderId())
                .addItems(AdminUpdateRequest.OrderItem.newBuilder()
                        .setProductId(productIdToUpdate)
                        .setPrice(100_000)
                        .setQuantity(10)
                        .build())
                .addItems(AdminUpdateRequest.OrderItem.newBuilder()
                        .setProductId(productIdToDelete)
                        .setPrice(100_000)
                        .setQuantity(0)
                        .build())
                .addItems(AdminUpdateRequest.OrderItem.newBuilder()
                        .setProductId(productIdToAdd)
                        .setPrice(100_000)
                        .setQuantity(10)
                        .build())
                .build());

        final List<AdminUpdateResponse.OrderItem> results = response.getItemsList();
        assertEquals(3, results.size());
        AdminUpdateResponse.OrderItem productUpdated = results.stream()
                .filter(oi -> productIdToUpdate.equals(oi.getProductId())).findFirst().get();
        assertEquals(12, productUpdated.getQuantity());
        AdminUpdateResponse.OrderItem productAdded = results.stream()
                .filter(oi -> productIdToAdd.equals(oi.getProductId())).findFirst().get();
        assertEquals(10, productAdded.getQuantity());
        AdminUpdateResponse.OrderItem productNoUpdated = results.stream()
                .filter(oi -> productIdNoUpdate.equals(oi.getProductId())).findFirst().get();
        assertEquals(1, productNoUpdated.getQuantity());
    }
}
