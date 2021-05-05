package vn.kamereo.state_machine;

public class InvalidTransitionException extends RuntimeException {

    private static final long serialVersionUID = 823740834732361733L;

    private InvalidTransitionException(final String message) {
        super(message);
    }

    public static InvalidTransitionException from(
            final Object transition,
            final Object entity,
            final Object currentState,
            final Object expectedStates
    ) {
        final String message = String.format(
                "Invalid transition: %s. Entity: %s. Current state: %s. Expected states: %s",
                transition,
                entity,
                currentState,
                expectedStates
        );
        return new InvalidTransitionException(message);
    }

}
