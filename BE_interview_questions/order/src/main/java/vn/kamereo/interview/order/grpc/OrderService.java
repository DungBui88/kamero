package vn.kamereo.interview.order.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import vn.kamereo.interview.order.AcceptRequest;
import vn.kamereo.interview.order.AcceptResponse;
import vn.kamereo.interview.order.CancelRequest;
import vn.kamereo.interview.order.CancelResponse;
import vn.kamereo.interview.order.CreateOrderRequest;
import vn.kamereo.interview.order.CreateOrderResponse;
import vn.kamereo.interview.order.OrderServiceGrpc;
import vn.kamereo.interview.order.ReceiveRequest;
import vn.kamereo.interview.order.ReceiveResponse;

@Service
public class OrderService extends OrderServiceGrpc.OrderServiceImplBase {

    private final CreateOrderMethod createOrderMethod;

    private final AcceptMethod acceptMethod;

    private final CancelMethod cancelMethod;

    private final ReceiveMethod receiveMethod;

    public OrderService(
            final CreateOrderMethod createOrderMethod,
            final AcceptMethod acceptMethod,
            final CancelMethod cancelMethod,
            final ReceiveMethod receiveMethod
    ) {
        this.createOrderMethod = createOrderMethod;
        this.acceptMethod = acceptMethod;
        this.cancelMethod = cancelMethod;
        this.receiveMethod = receiveMethod;
    }

    @Override
    public void createOrder(
            final CreateOrderRequest request,
            final StreamObserver<CreateOrderResponse> responseObserver
    ) {
        createOrderMethod.call(request, responseObserver);
    }

    @Override
    public void accept(final AcceptRequest request, final StreamObserver<AcceptResponse> responseObserver) {
        acceptMethod.call(request, responseObserver);
    }

    @Override
    public void cancel(final CancelRequest request, final StreamObserver<CancelResponse> responseObserver) {
        cancelMethod.call(request, responseObserver);
    }

    @Override
    public void receive(final ReceiveRequest request, final StreamObserver<ReceiveResponse> responseObserver) {
        receiveMethod.call(request, responseObserver);
    }
}
