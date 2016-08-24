package com.worth.ifs.assessment.builder;

import com.worth.ifs.assessment.resource.AssessmentResource;
import com.worth.ifs.assessment.resource.AssessmentStates;
import com.worth.ifs.workflow.resource.ProcessEvent;
import com.worth.ifs.workflow.resource.ProcessStates;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static com.google.common.primitives.Longs.asList;
import static com.worth.ifs.assessment.builder.AssessmentResourceBuilder.newAssessmentResource;
import static org.junit.Assert.assertEquals;

public class AssessmentResourceBuilderTest {

    @Test
    public void buildOne() throws Exception {
        final Long expectedId = 1L;
        final ProcessEvent expectedEvent = ProcessEvent.ASSESSMENT;
        final ProcessStates expectedStatus = AssessmentStates.OPEN;
        final Calendar expectedLastModifiedDate = GregorianCalendar.getInstance();
        final LocalDate expectedStartDate = LocalDate.now().minusDays(2);
        final LocalDate expectedEndDate = LocalDate.now().minusDays(1);
        final List<Long> expectedProcessOutcomes = asList(1L);
        final Long expectedProcessRole = 2L;
        final Boolean expectedSubmitted = Boolean.FALSE;
        final Boolean expectedStarted = Boolean.TRUE;
        final Long expectedApplication = 3L;
        final Long expectedCompetition = 4L;

        final AssessmentResource assessment = newAssessmentResource()
                .withId(expectedId)
                .withProcessEvent(expectedEvent)
                .withProcessStatus(expectedStatus)
                .withLastModifiedDate(expectedLastModifiedDate)
                .withStartDate(expectedStartDate)
                .withEndDate(expectedEndDate)
                .withProcessOutcome(expectedProcessOutcomes)
                .withProcessRole(expectedProcessRole)
                .withSubmitted(expectedSubmitted)
                .withStarted(expectedStarted)
                .withApplication(expectedApplication)
                .withCompetition(expectedCompetition)
                .build();

        assertEquals(expectedId, assessment.getId());
        assertEquals(expectedEvent.name(), assessment.getEvent());
        assertEquals(expectedStatus.getState(), assessment.getStatus());
        assertEquals(expectedLastModifiedDate, assessment.getLastModified());
        assertEquals(expectedStartDate, assessment.getStartDate());
        assertEquals(expectedEndDate, assessment.getEndDate());
        assertEquals(expectedProcessOutcomes, assessment.getProcessOutcomes());
        assertEquals(expectedProcessRole, assessment.getProcessRole());
        assertEquals(expectedSubmitted, assessment.getSubmitted());
        assertEquals(expectedStarted, assessment.getStarted());
        assertEquals(expectedApplication, assessment.getApplication());
        assertEquals(expectedCompetition, assessment.getCompetition());
    }


    @Test
    public void buildMany() {
        final Long[] expectedIds = { 1L, 2L };
        final ProcessEvent[] expectedEvents = { ProcessEvent.ASSESSMENT, ProcessEvent.ANOTHER_ONE };
        final ProcessStates[] expectedStatuss = { AssessmentStates.OPEN, AssessmentStates.ASSESSED };
        final Calendar[] expectedLastModifiedDates = { GregorianCalendar.getInstance(), GregorianCalendar.getInstance() };
        final LocalDate[] expectedStartDates = { LocalDate.now().minusDays(2), LocalDate.now().minusDays(3) };
        final LocalDate[] expectedEndDates = { LocalDate.now().minusDays(1), LocalDate.now().minusDays(2) };
        final List<Long> expectedProcessOutcomes1 = asList(1L);
        final List<Long> expectedProcessOutcomes2 = asList(2L);
        final Long[] expectedProcessRoles = { 1L, 2L };
        final Boolean[] expectedSubmittedValues = { Boolean.FALSE, Boolean.TRUE };
        final Boolean[] expectedStartedValues = { Boolean.FALSE, Boolean.TRUE };
        final Long[] expectedApplications = { 3L, 4L };
        final Long[] expectedCompetitions = { 5L, 6L };

        final List<AssessmentResource> assessments = newAssessmentResource()
                .withId(expectedIds)
                .withProcessEvent(expectedEvents)
                .withProcessStatus(expectedStatuss)
                .withLastModifiedDate(expectedLastModifiedDates)
                .withStartDate(expectedStartDates)
                .withEndDate(expectedEndDates)
                .withProcessOutcome(expectedProcessOutcomes1, expectedProcessOutcomes2)
                .withProcessRole(expectedProcessRoles)
                .withSubmitted(expectedSubmittedValues)
                .withStarted(expectedStartedValues)
                .withApplication(expectedApplications)
                .withCompetition(expectedCompetitions)
                .build(2);

        final AssessmentResource first = assessments.get(0);
        assertEquals(expectedIds[0], first.getId());
        assertEquals(expectedEvents[0].name(), first.getEvent());
        assertEquals(expectedStatuss[0].getState(), first.getStatus());
        assertEquals(expectedLastModifiedDates[0], first.getLastModified());
        assertEquals(expectedStartDates[0], first.getStartDate());
        assertEquals(expectedEndDates[0], first.getEndDate());
        assertEquals(expectedProcessOutcomes1, first.getProcessOutcomes());
        assertEquals(expectedProcessRoles[0], first.getProcessRole());
        assertEquals(expectedSubmittedValues[0], first.getSubmitted());
        assertEquals(expectedStartedValues[0], first.getStarted());
        assertEquals(expectedApplications[0], first.getApplication());
        assertEquals(expectedCompetitions[0], first.getCompetition());

        final AssessmentResource second = assessments.get(1);
        assertEquals(expectedIds[1], second.getId());
        assertEquals(expectedEvents[1].name(), second.getEvent());
        assertEquals(expectedStatuss[1].getState(), second.getStatus());
        assertEquals(expectedLastModifiedDates[1], second.getLastModified());
        assertEquals(expectedStartDates[1], second.getStartDate());
        assertEquals(expectedEndDates[1], second.getEndDate());
        assertEquals(expectedProcessOutcomes2, second.getProcessOutcomes());
        assertEquals(expectedProcessRoles[1], second.getProcessRole());
        assertEquals(expectedSubmittedValues[1], second.getSubmitted());
        assertEquals(expectedStartedValues[1], second.getStarted());
        assertEquals(expectedApplications[1], second.getApplication());
        assertEquals(expectedCompetitions[1], second.getCompetition());
    }
}