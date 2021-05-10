package vn.kamereo.interview.order.grpc;

import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import vn.kamereo.interview.order.*;

@Service
public class OrderService extends OrderServiceGrpc.OrderServiceImplBase {

    private final CreateOrderMethod createOrderMethod;

    private final AcceptMethod acceptMethod;

    private final AdminAcceptMethod adminAcceptMethod;

    private final CancelMethod cancelMethod;

    private final AdminCancelMethod adminCancelMethod;

    private final ReceiveMethod receiveMethod;

    private final AdminUpdateItemsMethod adminUpdateItemsMethod;

    public OrderService(
            final CreateOrderMethod createOrderMethod,
            final AcceptMethod acceptMethod,
            final AdminAcceptMethod adminAcceptMethod,
            final CancelMethod cancelMethod,
            final AdminCancelMethod adminCancelMethod,
            final ReceiveMethod receiveMethod,
            final AdminUpdateItemsMethod adminUpdateItemsMethod
    ) {
        this.createOrderMethod = createOrderMethod;
        this.acceptMethod = acceptMethod;
        this.adminAcceptMethod = adminAcceptMethod;
        this.cancelMethod = cancelMethod;
        this.adminCancelMethod = adminCancelMethod;
        this.receiveMethod = receiveMethod;
        this.adminUpdateItemsMethod = adminUpdateItemsMethod;
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
    public void adminAccept(final AcceptRequest request, final StreamObserver<AcceptResponse> responseObserver) {
        adminAcceptMethod.call(request, responseObserver);
    }

    @Override
    public void cancel(final CancelRequest request, final StreamObserver<CancelResponse> responseObserver) {
        cancelMethod.call(request, responseObserver);
    }

    @Override
    public void adminCancel(final CancelRequest request, final StreamObserver<CancelResponse> responseObserver) {
        adminCancelMethod.call(request, responseObserver);
    }

    @Override
    public void receive(final ReceiveRequest request, final StreamObserver<ReceiveResponse> responseObserver) {
        receiveMethod.call(request, responseObserver);
    }

    @Override
    public void adminUpdateItems(final AdminUpdateRequest request,final StreamObserver<AdminUpdateResponse> responseObserver) {
        adminUpdateItemsMethod.call(request, responseObserver);
    }
}
