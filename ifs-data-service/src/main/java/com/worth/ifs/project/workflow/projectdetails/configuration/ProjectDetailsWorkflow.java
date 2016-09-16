package com.worth.ifs.project.workflow.projectdetails.configuration;

import com.worth.ifs.assessment.resource.AssessmentOutcomes;
import com.worth.ifs.project.resource.ProjectDetailsState;
import com.worth.ifs.project.workflow.projectdetails.actions.ReadyToSubmitProjectDetailsAction;
import com.worth.ifs.project.workflow.projectdetails.actions.SubmitProjectDetailsAction;
import com.worth.ifs.project.workflow.projectdetails.guards.ProjectDetailsSuppliedGuard;
import com.worth.ifs.workflow.WorkflowStateMachineListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.LinkedHashSet;

import static java.util.Arrays.asList;

/**
 * Describes the workflow for the Project Details section for Project Setup.
 */
@Configuration
@EnableStateMachine(name = "projectDetailsStateMachine")
public class ProjectDetailsWorkflow extends StateMachineConfigurerAdapter<ProjectDetailsState, String> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<ProjectDetailsState, String> config) throws Exception {
        config.withConfiguration().listener(new WorkflowStateMachineListener());

    }

    @Override
    public void configure(StateMachineStateConfigurer<ProjectDetailsState, String> states) throws Exception {
        states.withStates()
                .initial(ProjectDetailsState.PENDING)
                .states(new LinkedHashSet<>(asList(ProjectDetailsState.values())));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ProjectDetailsState, String> transitions) throws Exception {
        transitions
                .withExternal()
                    .source(ProjectDetailsState.PENDING).target(ProjectDetailsState.READY_TO_SUBMIT)
                    .action(readyToSubmitProjectDetailsAction())
                    .guard(projectDetailsSuppliedGuard())
                    .and()
                .withExternal()
                    .source(ProjectDetailsState.READY_TO_SUBMIT).target(ProjectDetailsState.SUBMITTED)
                    .event(AssessmentOutcomes.SUBMIT.getType())
                    .action(submitProjectDetailsAction())
                    .guard(projectDetailsSuppliedGuard());
    }

    @Bean
    public ReadyToSubmitProjectDetailsAction readyToSubmitProjectDetailsAction() {
        return new ReadyToSubmitProjectDetailsAction();
    }

    @Bean
    public SubmitProjectDetailsAction submitProjectDetailsAction() {
        return new SubmitProjectDetailsAction();
    }

    @Bean
    public ProjectDetailsSuppliedGuard projectDetailsSuppliedGuard() {
        return new ProjectDetailsSuppliedGuard();
    }
}
