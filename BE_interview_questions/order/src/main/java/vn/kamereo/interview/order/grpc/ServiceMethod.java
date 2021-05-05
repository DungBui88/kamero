package vn.kamereo.interview.order.grpc;

import io.grpc.stub.StreamObserver;

public interface ServiceMethod<Req, Res>  {

    void call(final Req request, final StreamObserver<Res> responseObserver);
}
