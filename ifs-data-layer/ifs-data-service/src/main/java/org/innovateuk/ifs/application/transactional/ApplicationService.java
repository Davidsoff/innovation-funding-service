package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.domain.IneligibleOutcome;
import org.innovateuk.ifs.application.resource.*;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.form.domain.FormInputResponse;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Transactional and secure service for Application processing work
 */
public interface ApplicationService {

    @PreAuthorize("hasPermission(#competitionId, 'org.innovateuk.ifs.competition.resource.CompetitionResource', 'CREATE')")
    ServiceResult<ApplicationResource> createApplicationByApplicationNameForUserIdAndCompetitionId(final String applicationName, @P("competitionId") final Long competitionId, final Long userId);

    @PreAuthorize("hasPermission(#fileEntry, 'UPDATE')")
    ServiceResult<FormInputResponseFileEntryResource> createFormInputResponseFileUpload(@P("fileEntry") FormInputResponseFileEntryResource fileEntry, Supplier<InputStream> inputStreamSupplier);

    @PreAuthorize("hasPermission(#fileEntry, 'UPDATE')")
    ServiceResult<Void> updateFormInputResponseFileUpload(@P("fileEntry") FormInputResponseFileEntryResource fileEntry, Supplier<InputStream> inputStreamSupplier);

    @PreAuthorize("hasPermission(#fileEntry, 'org.innovateuk.ifs.application.resource.FormInputResponseFileEntryResource', 'UPDATE')")
    ServiceResult<FormInputResponse> deleteFormInputResponseFileUpload(@P("fileEntry") FormInputResponseFileEntryId fileEntry);

    @PreAuthorize("hasPermission(#fileEntry, 'org.innovateuk.ifs.application.resource.FormInputResponseFileEntryResource', 'READ')")
    ServiceResult<FormInputResponseFileAndContents> getFormInputResponseFileUpload(@P("fileEntry") FormInputResponseFileEntryId fileEntry);

    @PreAuthorize("hasPermission(#applicationId, 'org.innovateuk.ifs.application.resource.ApplicationResource', 'READ')")
    ServiceResult<ApplicationResource> getApplicationById(@P("applicationId") final Long applicationId);

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<ApplicationResource>> findAll();

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<ApplicationResource>> findByUserId(final Long userId);

    /**
     * This method saves only a few application attributes that
     * the user is able to modify on the application form.
     */
    @PreAuthorize("hasPermission(#applicationId, 'org.innovateuk.ifs.application.resource.ApplicationResource', 'UPDATE')")
    ServiceResult<ApplicationResource> saveApplicationDetails(@P("applicationId") final Long id, ApplicationResource application);

    @PreAuthorize("hasPermission(#applicationId, 'org.innovateuk.ifs.application.resource.ApplicationResource', 'UPDATE')")
    ServiceResult<ApplicationResource> saveApplicationSubmitDateTime(@P("applicationId") final Long id, ZonedDateTime date);

    @PreAuthorize("hasAnyAuthority('comp_admin' , 'project_finance')")
    @SecuredBySpring(value = "SET_FUNDING_DECISION_EMAIL_DATE", securedType = ApplicationResource.class, description = "Comp Admins should be able to set the funding decision email date")
    ServiceResult<ApplicationResource> setApplicationFundingEmailDateTime(@P("applicationId") final Long applicationId, ZonedDateTime fundingEmailDate);

    @PreAuthorize("hasPermission(#applicationId, 'org.innovateuk.ifs.application.resource.ApplicationResource', 'READ')")
    ServiceResult<CompletedPercentageResource> getProgressPercentageByApplicationId(@P("applicationId") final Long applicationId);

    @PreAuthorize("hasPermission(#applicationId, 'org.innovateuk.ifs.application.resource.ApplicationResource', 'UPDATE_APPLICATION_STATE')")
    ServiceResult<ApplicationResource> updateApplicationState(@P("applicationId") final Long id, final ApplicationState state);

    @PreAuthorize("hasPermission(#applicationId, 'org.innovateuk.ifs.application.resource.ApplicationResource', 'APPLICATION_SUBMITTED_NOTIFICATION')")
    ServiceResult<Void> sendNotificationApplicationSubmitted(@P("applicationId") Long application);

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<ApplicationResource>> getApplicationsByCompetitionIdAndUserId(final Long competitionId,
                                                                                     final Long userId,
                                                                                     final UserRoleType role);

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<ApplicationResource> findByProcessRole(Long id);

    @PreAuthorize("hasPermission(#id, 'org.innovateuk.ifs.application.resource.ApplicationResource', 'READ')")
    ServiceResult<Boolean> applicationReadyForSubmit(final Long id);

    @SecuredBySpring(value = "READ", description = "Only those with either comp admin or project finance roles can read the applications")
    @PreAuthorize("hasAnyAuthority('comp_admin' , 'project_finance')")
	ServiceResult<List<Application>> getApplicationsByCompetitionIdAndState(Long competitionId, Collection<ApplicationState> applicationStates);

    @PreAuthorize("hasPermission(#applicationId, 'org.innovateuk.ifs.application.resource.ApplicationResource', 'READ')")
    ServiceResult<BigDecimal> getProgressPercentageBigDecimalByApplicationId(Long applicationId);

    @SecuredBySpring(value = "NOTIFY_APPLICANTS_OF_FEEDBACK",
            description = "Comp admins and project finance users can notify applicants that their feedback is released")
    @PreAuthorize("hasAnyAuthority('comp_admin' , 'project_finance')")
    ServiceResult<Void> notifyApplicantsByCompetition(Long competitionId);

    @SecuredBySpring(value = "MARK_AS_INELIGIBLE",
            description = "Comp admins and project finance users can mark applications as ineligible")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    ServiceResult<Void> markAsIneligible(long applicationId, IneligibleOutcome reason);

    @SecuredBySpring(value = "INFORM_APPLICATION_IS_INELIGIBLE",
        description = "Comp admins and project finance users can inform applicants that their application is ineligible")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    ServiceResult<Void> informIneligible(long applicationId, ApplicationIneligibleSendResource applicationIneligibleSendResource);

    @PreAuthorize("hasPermission(#applicationId, 'org.innovateuk.ifs.application.resource.ApplicationResource', 'READ')")
    ServiceResult<Boolean> showApplicationTeam(final Long applicationId, final Long userId);

}
