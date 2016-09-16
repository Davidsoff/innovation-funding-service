package com.worth.ifs.workflow;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

/**
 * A standard workflow Action with the important distinction that subclassing this Action type allows us to test
 * whether or not transitions are possible without actually executing the final successful Action.
 */
public abstract class TestableTransitionWorkflowAction<S> implements Action<S, String> {

    public static final String TESTING_GUARD_KEY = "com.worth.ifs.workflow__TESTING_GUARD_KEY__";

    @Override
    public final void execute(StateContext<S, String> context) {
        if (context.getMessageHeader(TESTING_GUARD_KEY) == null) {
            doExecute(context);
        }
    }

    /**
     * Subclasses implement this method, which is executed if we are actually triggering the transition rather than
     * simply testing if it is possible to make the transition.
     */
    protected abstract void doExecute(StateContext<S, String> context);
}
