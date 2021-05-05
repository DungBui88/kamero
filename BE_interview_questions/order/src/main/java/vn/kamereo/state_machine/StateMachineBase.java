package vn.kamereo.state_machine;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class StateMachineBase<E, S, T> implements StateMachine<E, T> {

    private final TransitionConfig<S> defaultTransitionConfig = new TransitionConfig<>(Collections.emptySet(), null);

    @Nonnull
    private final Map<T, TransitionConfig<S>> transitionMap = Maps.newHashMap();

    @Nonnull
    protected abstract S getCurrentState(final E entity);

    @Nonnull
    protected abstract E setState(final E entity, final S newState);

    protected abstract void configure();

    protected StateMachineBase() {
        configure();
    }

    protected TransitionBuildStep transition(final T transition) {
        return new TransitionBuildStep(transition);
    }

    @Override
    public E apply(final E entity, final T transition) {
        final TransitionConfig<S> config = transitionMap.getOrDefault(transition, defaultTransitionConfig);

        final S currentState = getCurrentState(entity);

        if (!config.getFroms().contains(currentState)) {
            throw InvalidTransitionException.from(
                    transition,
                    entity,
                    currentState,
                    config.getFroms()
            );
        }

        return setState(entity, config.getTo());
    }

    public class TransitionBuildStep {

        private final T transition;

        private final Set<S> froms = Sets.newHashSet();

        TransitionBuildStep(final T transition) {
            this.transition = transition;
        }

        @SafeVarargs
        public final TransitionBuildStep from(final S... states) {
            Collections.addAll(froms, states);
            return this;
        }

        public StateMachineBase to(final S to) {
            if (null != transitionMap.get(transition)) {
                throw new RuntimeException(String.format(
                        "Transition [%s] is configured twice.",
                        transition
                ));
            }

            transitionMap.put(transition, new TransitionConfig(froms, to));
            return StateMachineBase.this;
        }

    }

    private static class TransitionConfig<S> {
        private final Set<S> froms;

        private final S to;

        private TransitionConfig(final Set<S> froms, final S to) {
            this.froms = froms;
            this.to = to;
        }

        public Set<S> getFroms() {
            return froms;
        }

        public S getTo() {
            return to;
        }
    }

}
