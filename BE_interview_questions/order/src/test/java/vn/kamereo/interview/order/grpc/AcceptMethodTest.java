package vn.kamereo.interview.order.grpc;

import com.asarkar.grpc.test.GrpcCleanupExtension;
import com.asarkar.grpc.test.Resources;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import vn.kamereo.interview.order.AcceptRequest;
import vn.kamereo.interview.order.CreateOrderRequest;
import vn.kamereo.interview.order.CreateOrderResponse;
import vn.kamereo.interview.order.OrderServiceGrpc;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@SpringBootTest
@ExtendWith({
        SpringExtension.class,
        GrpcCleanupExtension.class
})
class AcceptMethodTest {

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
    void testSuccessfulAccept(final Resources resources) {
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

        client.accept(AcceptRequest.newBuilder()
                .setOrderId(createResponse.getOrderId())
                .build()
        );
    }

    @Test
    void testWhenOrderNotFound(final Resources resources) {
        resources.register(channel, Duration.ofSeconds(1));
        resources.register(server, Duration.ofSeconds(1));

        try {
            client.accept(AcceptRequest.newBuilder()
                    .setOrderId(UUID.randomUUID().toString())
                    .build()
            );
        } catch (final StatusRuntimeException e) {
            Assertions.assertEquals("2", e.getStatus().getDescription());
        }
    }
}
