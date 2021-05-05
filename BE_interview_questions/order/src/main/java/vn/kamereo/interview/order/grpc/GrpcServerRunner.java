package vn.kamereo.interview.order.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Profile("app")
@Component
public class GrpcServerRunner implements CommandLineRunner {

    private final Server server;

    public GrpcServerRunner(final OrderService orderService) {
        server = ServerBuilder.forPort(8888)
                .addService(orderService)
                .addService(ProtoReflectionService.newInstance())
                .build();
    }

    @Override
    public void run(final String... args) throws Exception {
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                stop();
            } catch (final InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
        blockUntilShutdown();
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
