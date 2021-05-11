package vn.kamereo.interview.order.grpc;

public class InvalidOrderStateException extends RuntimeException {

    private static final long serialVersionUID = 823740834732361733L;

    private InvalidOrderStateException(final String message) {
        super(message);
    }

    public static InvalidOrderStateException from(
            final Object entity,
            final Object currentState
    ) {
        final String message = String.format(
                "Invalid order state : %s. Entity: %s.",
                currentState,
                entity
        );
        return new InvalidOrderStateException(message);
    }
}
