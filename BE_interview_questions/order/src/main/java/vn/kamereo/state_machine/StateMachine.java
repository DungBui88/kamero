package vn.kamereo.state_machine;

public interface StateMachine<E, T> {

    /**
     * Apply a state transition
     *
     * @param entity     entity
     * @param transition transition to be applied
     * @return same entity at new state
     */
    E apply(final E entity, final T transition);

}
