package org.innovateuk.ifs.project.grantofferletter.transactional;

import org.apache.commons.lang3.tuple.Pair;
import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.address.domain.Address;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.commons.error.CommonErrors;
import org.innovateuk.ifs.commons.error.CommonFailureKeys;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.file.domain.FileEntry;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.notifications.resource.ExternalUserNotificationTarget;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.project.builder.PartnerOrganisationBuilder;
import org.innovateuk.ifs.project.domain.PartnerOrganisation;
import org.innovateuk.ifs.project.domain.Project;
import org.innovateuk.ifs.project.domain.ProjectUser;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterState;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.project.resource.ProjectState;
import org.innovateuk.ifs.project.transactional.EmailService;
import org.innovateuk.ifs.user.builder.UserBuilder;

import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.Role;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.innovateuk.ifs.address.builder.AddressBuilder.newAddress;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.NOTIFICATIONS_UNABLE_TO_SEND_MULTIPLE;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.PROJECT_SETUP_ALREADY_COMPLETE;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.file.builder.FileEntryBuilder.newFileEntry;
import static org.innovateuk.ifs.file.builder.FileEntryResourceBuilder.newFileEntryResource;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.innovateuk.ifs.invite.builder.ProjectInviteBuilder.newProjectInvite;
import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.*;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.project.builder.PartnerOrganisationBuilder.newPartnerOrganisation;
import static org.innovateuk.ifs.project.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.builder.ProjectUserBuilder.newProjectUser;
import static org.innovateuk.ifs.project.grantofferletter.transactional.GrantOfferLetterServiceImpl.GRANT_OFFER_LETTER_DATE_FORMAT;
import static org.innovateuk.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static org.innovateuk.ifs.user.builder.RoleBuilder.newRole;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.OrganisationTypeEnum.BUSINESS;
import static org.innovateuk.ifs.user.resource.OrganisationTypeEnum.RESEARCH;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class GrantOfferLetterServiceImplTest extends BaseServiceUnitTest<GrantOfferLetterService> {

    private Long projectId = 123L;
    private Long applicationId = 456L;
    private Long userId = 7L;
    private Application application;
    private List<Organisation> organisations;
    private Organisation nonAcademicUnfunded;
    private Role leadApplicantRole;
    private User user;
    private ProcessRole leadApplicantProcessRole;
    private ProjectUser leadPartnerProjectUser;
    private Project project;
    private List<OrganisationResource> organisationResources;

    @Mock
    private EmailService projectEmailService;

    private Address address;

    private FileEntryResource fileEntryResource;

    private FileEntry createdFile;

    private String htmlFile;

    private Pair<File, FileEntry> fileEntryPair;

    @Captor
    ArgumentCaptor<Map<String, Object>> templateArgsCaptor;

    @Captor
    ArgumentCaptor<String> templateCaptor;

    @Captor
    ArgumentCaptor<FileEntryResource> fileEntryResCaptor;

    @Captor
    ArgumentCaptor<Supplier<InputStream>> supplierCaptor;

    @Before
    public void setUp() {
        organisations = newOrganisation().withOrganisationType(RESEARCH).withName("Org1&", "Org2\"", "Org3<").build(3);
        nonAcademicUnfunded = newOrganisation().withOrganisationType(BUSINESS).withName("Org4").build();
        organisationResources = newOrganisationResource().build(4);

        Competition competition = newCompetition().build();

        address = newAddress().withAddressLine1("test1")
                .withAddressLine2("test2")
                .withPostcode("PST")
                .withTown("town").build();

        leadApplicantRole = newRole(UserRoleType.LEADAPPLICANT).build();

        user = newUser().
                withId(userId).
                build();

        leadApplicantProcessRole = newProcessRole().
                withOrganisationId(organisations.get(0).getId()).
                withRole(leadApplicantRole).
                withUser(user).
                build();

        leadPartnerProjectUser = newProjectUser().
                withOrganisation(organisations.get(0)).
                withRole(PROJECT_PARTNER).
                withUser(user).
                build();

        application = newApplication().
                withId(applicationId).
                withCompetition(competition).
                withProcessRoles(leadApplicantProcessRole).
                withName("My Application").
                withDurationInMonths(5L).
                withStartDate(LocalDate.of(2017, 3, 2)).
                build();

        PartnerOrganisation partnerOrganisation = newPartnerOrganisation().withOrganisation(organisations.get(0)).build();
        PartnerOrganisation partnerOrganisation2 = newPartnerOrganisation().withOrganisation(organisations.get(1)).build();
        PartnerOrganisation partnerOrganisation3 = newPartnerOrganisation().withOrganisation(organisations.get(2)).build();

        List<PartnerOrganisation> partnerOrganisations = new ArrayList<>();
        partnerOrganisations.add(partnerOrganisation);
        partnerOrganisations.add(partnerOrganisation2);
        partnerOrganisations.add(partnerOrganisation3);

        project = newProject().
                withId(projectId).
                withPartnerOrganisations(partnerOrganisations).
                withAddress(address).
                withApplication(application).
                withProjectUsers(singletonList(leadPartnerProjectUser)).
                build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(organisationRepositoryMock.findOne(organisations.get(0).getId())).thenReturn(organisations.get(0));
        when(organisationRepositoryMock.findOne(organisations.get(1).getId())).thenReturn(organisations.get(1));
        when(organisationRepositoryMock.findOne(organisations.get(2).getId())).thenReturn(organisations.get(2));
        when(organisationMapperMock.mapToResource(organisations.get(0))).thenReturn(organisationResources.get(0));
        when(organisationMapperMock.mapToResource(organisations.get(1))).thenReturn(organisationResources.get(1));
        when(organisationMapperMock.mapToResource(organisations.get(2))).thenReturn(organisationResources.get(2));
    }

    @Test
    public void testCreateSignedGrantOfferLetterFileEntry() {
        assertCreateFile(
                project::getSignedGrantOfferLetter,
                (fileToCreate, inputStreamSupplier) ->
                        service.createSignedGrantOfferLetterFileEntry(123L, fileToCreate, inputStreamSupplier));
    }

    @Test
    public void testCreateGrantOfferLetterFileEntry() {
        assertCreateFile(
                project::getGrantOfferLetter,
                (fileToCreate, inputStreamSupplier) ->
                        service.createGrantOfferLetterFileEntry(123L, fileToCreate, inputStreamSupplier));
    }

    @Test
    public void testCreateAdditionalContractFileEntry() {
        assertCreateFile(
                project::getAdditionalContractFile,
                (fileToCreate, inputStreamSupplier) ->
                        service.createAdditionalContractFileEntry(123L, fileToCreate, inputStreamSupplier));
    }

    @Test
    public void testGetAdditionalContractFileEntryDetails() {
        assertGetFileDetails(
                project::setAdditionalContractFile,
                () -> service.getAdditionalContractFileEntryDetails(123L));
    }

    @Test
    public void testGetGrantOfferLetterFileEntryDetails() {
        assertGetFileDetails(
                project::setGrantOfferLetter,
                () -> service.getGrantOfferLetterFileEntryDetails(123L));
    }

    @Test
    public void testGetSignedGrantOfferLetterFileEntryDetails() {
        assertGetFileDetails(
                project::setSignedGrantOfferLetter,
                () -> service.getSignedGrantOfferLetterFileEntryDetails(123L));
    }

    @Test
    public void testGetAdditionalContractFileContents() {
        assertGetFileContents(
                project::setAdditionalContractFile,
                () -> service.getAdditionalContractFileAndContents(123L));
    }

    @Test
    public void testGetGrantOfferLetterFileContents() {
        assertGetFileContents(
                project::setGrantOfferLetter,
                () -> service.getGrantOfferLetterFileAndContents(123L));
    }

    @Test
    public void testGetSignedGrantOfferLetterFileContents() {
        assertGetFileContents(
                project::setSignedGrantOfferLetter,
                () -> service.getSignedGrantOfferLetterFileAndContents(123L));
    }

    @Test
    public void testUpdateSignedGrantOfferLetterFileEntry() {
        when(golWorkflowHandlerMock.isSent(any())).thenReturn(Boolean.TRUE);
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.SETUP);
        assertUpdateFile(
                project::getSignedGrantOfferLetter,
                (fileToUpdate, inputStreamSupplier) ->
                        service.updateSignedGrantOfferLetterFile(123L, fileToUpdate, inputStreamSupplier));
    }

    @Test
    public void testUpdateSignedGrantOfferLetterFileEntryProjectLive() {

        FileEntryResource fileToUpdate = newFileEntryResource().build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        when(projectWorkflowHandlerMock.getState(any())).thenReturn(ProjectState.LIVE);
        when(golWorkflowHandlerMock.isSent(any())).thenReturn(Boolean.FALSE);

        ServiceResult<Void> result = service.updateSignedGrantOfferLetterFile(123L, fileToUpdate, inputStreamSupplier);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(PROJECT_SETUP_ALREADY_COMPLETE));
    }

    @Test
    public void testUpdateSignedGrantOfferLetterFileEntryGolNotSent() {

        FileEntryResource fileToUpdate = newFileEntryResource().build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        when(projectWorkflowHandlerMock.getState(any())).thenReturn(ProjectState.SETUP);
        when(golWorkflowHandlerMock.isSent(any())).thenReturn(Boolean.FALSE);

        ServiceResult<Void> result = service.updateSignedGrantOfferLetterFile(123L, fileToUpdate, inputStreamSupplier);
        assertTrue(result.isFailure());
        assertEquals(result.getErrors().get(0).getErrorKey(), CommonFailureKeys.GRANT_OFFER_LETTER_MUST_BE_SENT_BEFORE_UPLOADING_SIGNED_COPY.toString());
    }

    @Test
    public void testSubmitGrantOfferLetterFailureNoSignedGolFile() {

        ServiceResult<Void> result = service.submitGrantOfferLetter(projectId);

        assertTrue(result.getFailure().is(CommonFailureKeys.SIGNED_GRANT_OFFER_LETTER_MUST_BE_UPLOADED_BEFORE_SUBMIT));
        Assert.assertThat(project.getOfferSubmittedDate(), nullValue());
    }

    @Test
    public void testSubmitGrantOfferLetterFailureCannotReachSignedState() {
        project.setSignedGrantOfferLetter(mock(FileEntry.class));

        when(golWorkflowHandlerMock.sign(any())).thenReturn(Boolean.FALSE);

        ServiceResult<Void> result = service.submitGrantOfferLetter(projectId);

        assertTrue(result.getFailure().is(CommonFailureKeys.GRANT_OFFER_LETTER_CANNOT_SET_SIGNED_STATE));
        Assert.assertThat(project.getOfferSubmittedDate(), nullValue());
    }

    @Test
    public void testSubmitGrantOfferLetterSuccess() {
        project.setSignedGrantOfferLetter(mock(FileEntry.class));

        when(golWorkflowHandlerMock.sign(any())).thenReturn(Boolean.TRUE);

        ServiceResult<Void> result = service.submitGrantOfferLetter(projectId);

        assertTrue(result.isSuccess());
        Assert.assertThat(project.getOfferSubmittedDate(), notNullValue());
    }

    @Test
    public void testGenerateGrantOfferLetter() {
        assertGenerateFile(
                fileEntryResource ->
                        service.generateGrantOfferLetter(123L, fileEntryResource));
    }

    @Test
    public void testRemoveGrantOfferLetterFileEntry() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingGOLFile = newFileEntry().build();
        project.setGrantOfferLetter(existingGOLFile);

        when(userRepositoryMock.findOne(internalUserResource.getId())).thenReturn(internalUser);
        when(golWorkflowHandlerMock.removeGrantOfferLetter(project, internalUser)).thenReturn(true);
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.SETUP);
        when(fileServiceMock.deleteFileIgnoreNotFound(existingGOLFile.getId())).thenReturn(serviceSuccess(existingGOLFile));

        ServiceResult<Void> result = service.removeGrantOfferLetterFileEntry(123L);

        assertTrue(result.isSuccess());
        assertNull(project.getGrantOfferLetter());

        verify(golWorkflowHandlerMock).removeGrantOfferLetter(project, internalUser);
        verify(fileServiceMock).deleteFileIgnoreNotFound(existingGOLFile.getId());
    }

    @Test
    public void testRemoveGrantOfferLetterFileEntryProjectLive() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingGOLFile = newFileEntry().build();
        project.setGrantOfferLetter(existingGOLFile);

        when(userRepositoryMock.findOne(internalUserResource.getId())).thenReturn(internalUser);
        when(golWorkflowHandlerMock.removeGrantOfferLetter(project, internalUser)).thenReturn(true);
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.LIVE);
        when(fileServiceMock.deleteFileIgnoreNotFound(existingGOLFile.getId())).thenReturn(serviceSuccess(existingGOLFile));

        ServiceResult<Void> result = service.removeGrantOfferLetterFileEntry(123L);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(PROJECT_SETUP_ALREADY_COMPLETE));
    }

    @Test
    public void testRemoveGrantOfferLetterFileEntryButWorkflowRejected() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingGOLFile = newFileEntry().build();
        project.setGrantOfferLetter(existingGOLFile);

        when(userRepositoryMock.findOne(internalUserResource.getId())).thenReturn(internalUser);
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.SETUP);
        when(golWorkflowHandlerMock.removeGrantOfferLetter(project, internalUser)).thenReturn(false);

        ServiceResult<Void> result = service.removeGrantOfferLetterFileEntry(123L);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GRANT_OFFER_LETTER_CANNOT_BE_REMOVED));
        assertEquals(existingGOLFile, project.getGrantOfferLetter());

        verify(golWorkflowHandlerMock).removeGrantOfferLetter(project, internalUser);
        verify(fileServiceMock, never()).deleteFile(existingGOLFile.getId());
    }

    @Test
    public void testRemoveSignedGrantOfferLetterFileEntry() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingSignedGOLFile = newFileEntry().build();
        project.setSignedGrantOfferLetter(existingSignedGOLFile);

        when(userRepositoryMock.findOne(internalUserResource.getId())).thenReturn(internalUser);
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.SETUP);
        when(fileServiceMock.deleteFileIgnoreNotFound(existingSignedGOLFile.getId())).thenReturn(serviceSuccess(existingSignedGOLFile));

        ServiceResult<Void> result = service.removeSignedGrantOfferLetterFileEntry(123L);

        assertTrue(result.isSuccess());
        assertNull(project.getSignedGrantOfferLetter());

        verify(fileServiceMock).deleteFileIgnoreNotFound(existingSignedGOLFile.getId());
    }

    @Test
    public void testRemoveSignedGrantOfferLetterFileEntryProjectLive() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingSignedGOLFile = newFileEntry().build();
        project.setSignedGrantOfferLetter(existingSignedGOLFile);

        when(userRepositoryMock.findOne(internalUserResource.getId())).thenReturn(internalUser);
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.LIVE);
        when(fileServiceMock.deleteFileIgnoreNotFound(existingSignedGOLFile.getId())).thenReturn(serviceSuccess(existingSignedGOLFile));

        ServiceResult<Void> result = service.removeSignedGrantOfferLetterFileEntry(123L);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(PROJECT_SETUP_ALREADY_COMPLETE));
    }

    private final Organisation organisation(OrganisationTypeEnum type, String name) {
        return newOrganisation()
                .withOrganisationType(type)
                .withName(name)
                .build();
    }

    @Test
    public void testGenerateGrantOfferLetterIfReadySuccess() {

        setupGolTemplate();

        Organisation o1 = organisation(BUSINESS, "OrgLeader&");
        Organisation o2 = organisation(BUSINESS, "Org2\"");
        Organisation o3 = organisation(BUSINESS, "Org3<");

        ApplicationFinanceResource applicationFinanceResource = newApplicationFinanceResource()
                .withGrantClaimPercentage(30)
                .withApplication(456L)
                .withOrganisation(3L)
                .build();

        setupOrganisationsForGrantOfferLetter(o1, o2, o3, applicationFinanceResource, applicationFinanceResource, applicationFinanceResource);

        Map<String, Object> templateArgs = setupTemplateArguments();

        when(spendProfileServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.APPROVED));

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);

        verify(rendererMock).renderTemplate(templateCaptor.capture(), templateArgsCaptor.capture());
        verify(fileServiceMock).createFile(fileEntryResCaptor.capture(), supplierCaptor.capture());

        assertTrue(checkGolTemplate());
        assertTrue(result.isSuccess());
        assertTrue(compareTemplate(templateArgs, templateArgsCaptor.getAllValues().get(0)));
    }

    @Test
    public void testGenerateGrantOfferLetterFailureSpendProfilesNotApproved() {
        when(projectRepositoryMock.findOne(123L)).thenReturn(project);
        when(spendProfileServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.REJECTED));

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testGenerateGrantOfferLetterOtherDocsNotApproved() {

        Competition comp = newCompetition().withName("Test Comp").build();
        Organisation o1 = newOrganisation().withName("OrgLeader").build();
        Role leadAppRole = newRole(UserRoleType.LEADAPPLICANT).build();
        User u = newUser().withFirstName("ab").withLastName("cd").build();
        ProcessRole leadAppProcessRole = newProcessRole().withOrganisationId(o1.getId()).withUser(u).withRole(leadAppRole).build();
        Application app = newApplication().withCompetition(comp).withProcessRoles(leadAppProcessRole).withId(3L).build();
        ProjectUser pm = newProjectUser().withRole(PROJECT_MANAGER).withOrganisation(o1).build();
        PartnerOrganisation po = PartnerOrganisationBuilder.newPartnerOrganisation().withOrganisation(o1).withLeadOrganisation(true).build();
        Project project = newProject().withOtherDocumentsApproved(ApprovalType.REJECTED).withApplication(app).withPartnerOrganisations(asList(po)).withProjectUsers(asList(pm)).withDuration(10L).build();

        when(spendProfileServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.APPROVED));
        when(projectRepositoryMock.findOne(123L)).thenReturn(project);

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testGenerateGrantOfferLetterNoProject() {

        when(spendProfileServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.APPROVED));
        when(projectRepositoryMock.findOne(123L)).thenReturn(null);

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);
        assertTrue(result.isSuccess());
    }

    private Map<String, Object> setupTemplateArguments() {
        Map<String, Object> templateArgs = new HashMap();
        templateArgs.put("ProjectLength", 10L);
        templateArgs.put("ProjectTitle", "project 1");
        templateArgs.put("LeadContact", "ab cd");
        templateArgs.put("ApplicationNumber", 3L);
        templateArgs.put("LeadOrgName", "OrgLeader&");
        templateArgs.put("CompetitionName", "Test Comp<");
        templateArgs.put("Address1", "InnovateUK>");
        templateArgs.put("Address2", "Northstar House\"");
        templateArgs.put("Address3", "");
        templateArgs.put("TownCity", "Swindon&");
        templateArgs.put("PostCode", "SN1 1AA'");
        templateArgs.put("ProjectStartDate", ZonedDateTime.now().format(DateTimeFormatter.ofPattern(GRANT_OFFER_LETTER_DATE_FORMAT)));
        templateArgs.put("Date", ZonedDateTime.now().toString());
        return templateArgs;
    }

    private boolean compareTemplate(Map<String, Object> expectedTemplateArgs, Map<String, Object> templateArgs) {
        boolean result = true;
        result &= expectedTemplateArgs.get("ProjectLength").equals(templateArgs.get("ProjectLength"));
        result &= expectedTemplateArgs.get("ProjectTitle").equals(templateArgs.get("ProjectTitle"));
        result &= expectedTemplateArgs.get("ProjectStartDate").equals(templateArgs.get("ProjectStartDate"));
        result &= expectedTemplateArgs.get("LeadContact").equals(templateArgs.get("LeadContact"));
        result &= expectedTemplateArgs.get("ApplicationNumber").equals(templateArgs.get("ApplicationNumber"));
        result &= expectedTemplateArgs.get("LeadOrgName").equals(templateArgs.get("LeadOrgName"));
        result &= expectedTemplateArgs.get("CompetitionName").equals(templateArgs.get("CompetitionName"));
        result &= expectedTemplateArgs.get("Address1").equals(templateArgs.get("Address1"));
        result &= expectedTemplateArgs.get("Address2").equals(templateArgs.get("Address2"));
        result &= expectedTemplateArgs.get("Address3").equals(templateArgs.get("Address3"));
        result &= expectedTemplateArgs.get("TownCity").equals(templateArgs.get("TownCity"));
        result &= expectedTemplateArgs.get("PostCode").equals(templateArgs.get("PostCode"));
        result &= ZonedDateTime.parse((String) expectedTemplateArgs.get("Date")).isBefore(ZonedDateTime.parse((String) templateArgs.get("Date"))) || ZonedDateTime.parse((String) expectedTemplateArgs.get("Date")).isEqual(ZonedDateTime.parse((String) templateArgs.get("Date")));
        return result;
    }

    private void setupGolTemplate() {
        fileEntryResource = newFileEntryResource().
                withFilesizeBytes(1024).
                withMediaType("application/pdf").
                withName("grant_offer_letter").
                build();

        createdFile = newFileEntry().build();
        fileEntryPair = Pair.of(new File("blah"), createdFile);

        StringBuilder stringBuilder = new StringBuilder();
        htmlFile = stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<html dir=\"ltr\" lang=\"en\">\n")
                .append("<head>\n")
                .append("<meta charset=\"UTF-8\"></meta>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("<p>\n")
                .append("${LeadContact}<br/>\n")
                .append("</p>\n")
                .append("</body>\n")
                .append("</html>\n").toString();

        when(rendererMock.renderTemplate(eq("common/grantoffer/grant_offer_letter.html"), any(Map.class))).thenReturn(ServiceResult.serviceSuccess(htmlFile));
        when(fileServiceMock.createFile(any(FileEntryResource.class), any(Supplier.class))).thenReturn(ServiceResult.serviceSuccess(fileEntryPair));
        when(fileEntryMapperMock.mapToResource(createdFile)).thenReturn(fileEntryResource);
    }

    private boolean checkGolTemplate() {
        boolean result = true;
        result &= fileEntryResource.getMediaType().equals(fileEntryResCaptor.getAllValues().get(0).getMediaType());
        result &= (fileEntryResource.getName() + ".pdf").equals(fileEntryResCaptor.getAllValues().get(0).getName());

        String startOfGeneratedFileString = null;
        try {
            int n = supplierCaptor.getAllValues().get(0).get().available();
            byte[] startOfGeneratedFile = new byte[n];
            supplierCaptor.getAllValues().get(0).get().read(startOfGeneratedFile, 0, n < 9 ? n : 9);
            startOfGeneratedFileString = new String(startOfGeneratedFile, StandardCharsets.UTF_8);
        } catch (IOException e) {

        }
        String pdfHeader = "%PDF-1.4\n";
        result &= pdfHeader.equals(startOfGeneratedFileString.substring(0, pdfHeader.length()));
        return result;
    }

    private void setupOrganisationsForGrantOfferLetter(Organisation o1, Organisation o2, Organisation o3, ApplicationFinanceResource af1, ApplicationFinanceResource af2, ApplicationFinanceResource af3) {
        Competition comp = newCompetition()
                .withName("Test Comp<")
                .build();

        Role leadAppRole = newRole(UserRoleType.LEADAPPLICANT)
                .build();
        User u = newUser()
                .withFirstName("ab")
                .withLastName("cd")
                .build();
        ProcessRole leadAppProcessRole = newProcessRole()
                .withOrganisationId(o1.getId())
                .withUser(u)
                .withRole(leadAppRole)
                .build();
        Application app = newApplication()
                .withCompetition(comp)
                .withProcessRoles(leadAppProcessRole)
                .withId(3L)
                .build();
        ProjectUser pm = newProjectUser()
                .withRole(PROJECT_MANAGER)
                .withOrganisation(o1)
                .build();

        PartnerOrganisation po = newPartnerOrganisation()
                .withOrganisation(o1)
                .withLeadOrganisation(true)
                .build();

        PartnerOrganisation po2 = newPartnerOrganisation()
                .withOrganisation(o2)
                .withLeadOrganisation(false)
                .build();

        PartnerOrganisation po3 = newPartnerOrganisation()
                .withOrganisation(o3)
                .withLeadOrganisation(false)
                .build();

        Address address = newAddress()
                .withAddressLine1("InnovateUK>")
                .withAddressLine2("Northstar House\"")
                .withTown("Swindon&")
                .withPostcode("SN1 1AA'")
                .build();
        Project project = newProject()
                .withOtherDocumentsApproved(ApprovalType.APPROVED)
                .withName("project 1")
                .withApplication(app)
                .withPartnerOrganisations(asList(po3, po, po2))
                .withProjectUsers(asList(pm))
                .withDuration(10L)
                .withAddress(address)
                .withTargetStartDate(LocalDate.now())
                .build();

        when(projectRepositoryMock.findOne(123L)).thenReturn(project);

        when(organisationRepositoryMock.findOne(o1.getId())).thenReturn(o1);
        when(organisationRepositoryMock.findOne(o2.getId())).thenReturn(o2);
        when(organisationRepositoryMock.findOne(o3.getId())).thenReturn(o3);

    }

    @Test
    public void testSendGrantOfferLetterNoGol() {

        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(user).withOrganisation(nonAcademicUnfunded).withInvite(newProjectInvite().build()).build(1);
        Project p = newProject().withProjectUsers(pu).withPartnerOrganisations(newPartnerOrganisation().withOrganisation(nonAcademicUnfunded).build(1)).withGrantOfferLetter(null).build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(p);
        when(notificationServiceMock.sendNotification(any(), eq(EMAIL))).thenReturn(serviceSuccess());

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isFailure());
    }

    @Test
    public void testSendGrantOfferLetterSendFails() {

        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(user).withOrganisation(nonAcademicUnfunded).withInvite(newProjectInvite().build()).build(1);
        FileEntry golFile = newFileEntry().withMediaType("application/pdf").withFilesizeBytes(10).build();
        Project p = newProject().withProjectUsers(pu).withPartnerOrganisations(newPartnerOrganisation().withOrganisation(nonAcademicUnfunded).build(1)).withGrantOfferLetter(golFile).build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(p);

        NotificationTarget to = new ExternalUserNotificationTarget("A B", "a@b.com");

        Map<String, Object> expectedNotificationArguments = asMap(
                "dashboardUrl", "https://ifs-local-dev/dashboard"
        );

        when(projectEmailService.sendEmail(singletonList(to), expectedNotificationArguments, GrantOfferLetterServiceImpl.NotificationsGol.GRANT_OFFER_LETTER_PROJECT_MANAGER)).thenReturn(serviceFailure(NOTIFICATIONS_UNABLE_TO_SEND_MULTIPLE));

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isFailure());
    }

    @Test
    public void testSendGrantOfferLetterNoProject() {

        when(projectRepositoryMock.findOne(projectId)).thenReturn(null);

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isFailure());
    }

    @Test
    public void testSendGrantOfferLetterSuccess() {

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(user).withOrganisation(nonAcademicUnfunded).withInvite(newProjectInvite().build()).build(1);
        Project p = newProject().withProjectUsers(pu).withPartnerOrganisations(newPartnerOrganisation().withOrganisation(nonAcademicUnfunded).build(1)).withGrantOfferLetter(golFile).build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(p);

        NotificationTarget to = new ExternalUserNotificationTarget("A B", "a@b.com");

        Map<String, Object> expectedNotificationArguments = asMap(
                "dashboardUrl", "https://ifs-local-dev/dashboard"
        );

        when(projectEmailService.sendEmail(singletonList(to), expectedNotificationArguments, GrantOfferLetterServiceImpl.NotificationsGol.GRANT_OFFER_LETTER_PROJECT_MANAGER)).thenReturn(serviceSuccess());

        User user = UserBuilder.newUser().build();
        setLoggedInUser(newUserResource().withId(user.getId()).build());
        when(userRepositoryMock.findOne(user.getId())).thenReturn(user);

        when(golWorkflowHandlerMock.grantOfferLetterSent(p, user)).thenReturn(Boolean.TRUE);

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testSendGrantOfferLetterFailure() {

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(user).withOrganisation(nonAcademicUnfunded).withInvite(newProjectInvite().build()).build(1);
        Project p = newProject().withProjectUsers(pu).withPartnerOrganisations(newPartnerOrganisation().withOrganisation(nonAcademicUnfunded).build(1)).withGrantOfferLetter(golFile).build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(p);

        NotificationTarget to = new ExternalUserNotificationTarget("A B", "a@b.com");

        Map<String, Object> expectedNotificationArguments = asMap(
                "dashboardUrl", "https://ifs-local-dev/dashboard"
        );

        when(projectEmailService.sendEmail(singletonList(to), expectedNotificationArguments, GrantOfferLetterServiceImpl.NotificationsGol.GRANT_OFFER_LETTER_PROJECT_MANAGER)).thenReturn(serviceSuccess());

        User user = UserBuilder.newUser().build();
        setLoggedInUser(newUserResource().withId(user.getId()).build());
        when(userRepositoryMock.findOne(user.getId())).thenReturn(user);

        when(golWorkflowHandlerMock.grantOfferLetterSent(p, user)).thenReturn(Boolean.FALSE);

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isFailure());
    }

    @Test
    public void testIsSendGrantOfferLetterAllowed() {

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isSendAllowed(project)).thenReturn(Boolean.TRUE);

        ServiceResult<Boolean> result = service.isSendGrantOfferLetterAllowed(projectId);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testIsSendGrantOfferLetterAllowedFails() {

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isSendAllowed(project)).thenReturn(Boolean.FALSE);

        ServiceResult<Boolean> result = service.isSendGrantOfferLetterAllowed(projectId);

        assertTrue(result.isSuccess() && Boolean.FALSE == result.getSuccessObject());
    }

    @Test
    public void testIsSendGrantOfferLetterAllowedNoProject() {

        when(projectRepositoryMock.findOne(projectId)).thenReturn(null);

        ServiceResult<Boolean> result = service.isSendGrantOfferLetterAllowed(projectId);

        assertTrue(result.isFailure());
    }

    @Test
    public void testIsGrantOfferLetterAlreadySent() {

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isAlreadySent(project)).thenReturn(Boolean.TRUE);

        ServiceResult<Boolean> result = service.isGrantOfferLetterAlreadySent(projectId);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testIsGrantOfferLetterAlreadySentFails() {

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isAlreadySent(project)).thenReturn(Boolean.FALSE);

        ServiceResult<Boolean> result = service.isGrantOfferLetterAlreadySent(projectId);

        assertTrue(result.isSuccess() && Boolean.FALSE == result.getSuccessObject());
    }

    @Test
    public void testIsGrantOfferLetterAlreadySentNoProject() {

        when(projectRepositoryMock.findOne(projectId)).thenReturn(null);
        ServiceResult<Boolean> result = service.isGrantOfferLetterAlreadySent(projectId);
        assertTrue(result.isFailure());
    }

    @Test
    public void testApproveSignedGrantOfferLetterSuccess() {

        User u = newUser().withFirstName("A").withLastName("B").withEmailAddress("a@b.com").build();
        setLoggedInUser(newUserResource().withId(u.getId()).build());
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(u).withOrganisation(nonAcademicUnfunded).withInvite(newProjectInvite().build()).build(1);
        Project p = newProject().withProjectUsers(pu).withPartnerOrganisations(newPartnerOrganisation().withOrganisation(nonAcademicUnfunded).build(1)).build();

        NotificationTarget to = new ExternalUserNotificationTarget("A B", "a@b.com");

        when(projectRepositoryMock.findOne(projectId)).thenReturn(p);
        when(golWorkflowHandlerMock.isReadyToApprove(p)).thenReturn(Boolean.TRUE);
        when(userRepositoryMock.findOne(u.getId())).thenReturn(u);
        when(golWorkflowHandlerMock.grantOfferLetterApproved(p, u)).thenReturn(Boolean.TRUE);
        when(projectWorkflowHandlerMock.grantOfferLetterApproved(p, p.getProjectUsersWithRole(PROJECT_MANAGER).get(0))).thenReturn(Boolean.TRUE);
        when(projectEmailService.sendEmail(singletonList(to), emptyMap(), GrantOfferLetterServiceImpl.NotificationsGol.PROJECT_LIVE)).thenReturn(serviceSuccess());

        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, ApprovalType.APPROVED);

        verify(projectRepositoryMock, atLeast(2)).findOne(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(p);
        verify(golWorkflowHandlerMock).grantOfferLetterApproved(p, u);
        verify(projectWorkflowHandlerMock).grantOfferLetterApproved(p, p.getProjectUsersWithRole(PROJECT_MANAGER).get(0));
        verify(projectEmailService).sendEmail(singletonList(to), emptyMap(), GrantOfferLetterServiceImpl.NotificationsGol.PROJECT_LIVE);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testDuplicateEmailsAreNotSent() {

        User u = newUser().withFirstName("A").withLastName("B").withEmailAddress("a@b.com").build();
        setLoggedInUser(newUserResource().withId(u.getId()).build());
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(u).withOrganisation(nonAcademicUnfunded).withInvite(newProjectInvite().build()).build(1);
        List<ProjectUser> fc = newProjectUser().withRole(PROJECT_FINANCE_CONTACT).withUser(u).withOrganisation(nonAcademicUnfunded).withInvite(newProjectInvite().build()).build(1);
        pu.addAll(fc);
        Project p = newProject().withProjectUsers(pu).withPartnerOrganisations(newPartnerOrganisation().withOrganisation(nonAcademicUnfunded).build(1)).build();

        NotificationTarget to = new ExternalUserNotificationTarget("A B", "a@b.com");

        when(projectRepositoryMock.findOne(projectId)).thenReturn(p);
        when(golWorkflowHandlerMock.isReadyToApprove(p)).thenReturn(Boolean.TRUE);
        when(userRepositoryMock.findOne(u.getId())).thenReturn(u);
        when(golWorkflowHandlerMock.grantOfferLetterApproved(p, u)).thenReturn(Boolean.TRUE);
        when(projectWorkflowHandlerMock.grantOfferLetterApproved(p, p.getProjectUsersWithRole(PROJECT_MANAGER).get(0))).thenReturn(Boolean.TRUE);
        when(projectEmailService.sendEmail(singletonList(to), emptyMap(), GrantOfferLetterServiceImpl.NotificationsGol.PROJECT_LIVE)).thenReturn(serviceSuccess());

        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, ApprovalType.APPROVED);

        verify(projectRepositoryMock, atLeast(2)).findOne(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(p);
        verify(golWorkflowHandlerMock).grantOfferLetterApproved(p, u);
        verify(projectWorkflowHandlerMock).grantOfferLetterApproved(p, p.getProjectUsersWithRole(PROJECT_MANAGER).get(0));
        when(projectEmailService.sendEmail(singletonList(to), emptyMap(), GrantOfferLetterServiceImpl.NotificationsGol.PROJECT_LIVE)).thenReturn(serviceSuccess());

        assertTrue(result.isSuccess());
    }

    @Test
    public void testApproveSignedGrantOfferLetterFailure() {
        User u = newUser().withFirstName("A").withLastName("B").withEmailAddress("a@b.com").build();
        setLoggedInUser(newUserResource().withId(u.getId()).build());

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        project.setGrantOfferLetter(golFile);

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(Boolean.TRUE);
        when(userRepositoryMock.findOne(u.getId())).thenReturn(u);
        when(golWorkflowHandlerMock.grantOfferLetterApproved(project, u)).thenReturn(Boolean.FALSE);

        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, ApprovalType.APPROVED);

        verify(projectRepositoryMock).findOne(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);
        verify(golWorkflowHandlerMock).grantOfferLetterApproved(project, u);
        verify(projectWorkflowHandlerMock, never()).grantOfferLetterApproved(any(), any());

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_UNEXPECTED_ERROR));
    }

    @Test
    public void testApproveSignedGrantOfferLetterFailureNotReadyToApprove() {

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        project.setGrantOfferLetter(golFile);

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(Boolean.FALSE);

        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, ApprovalType.APPROVED);

        verify(projectRepositoryMock).findOne(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GRANT_OFFER_LETTER_NOT_READY_TO_APPROVE));
    }

    @Test
    public void testGetSignedGrantOfferLetterApprovalStatusSuccess() {

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isApproved(project)).thenReturn(Boolean.TRUE);

        ServiceResult<Boolean> result = service.isSignedGrantOfferLetterApproved(projectId);

        verify(projectRepositoryMock).findOne(projectId);
        verify(golWorkflowHandlerMock).isApproved(project);

        assertTrue(result.isSuccess() && Boolean.TRUE == result.getSuccessObject());
    }

    @Test
    public void testGetSignedGrantOfferLetterApprovalStatusFailure() {

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isApproved(project)).thenReturn(Boolean.FALSE);

        ServiceResult<Boolean> result = service.isSignedGrantOfferLetterApproved(projectId);

        verify(projectRepositoryMock).findOne(projectId);
        verify(golWorkflowHandlerMock).isApproved(project);

        assertTrue(result.isSuccess() && Boolean.FALSE == result.getSuccessObject());
    }

    @Test
    public void testGetGrantOfferLetterWorkflowStateWhenProjectDoesNotExist() {

        when(projectRepositoryMock.findOne(projectId)).thenReturn(null);

        ServiceResult<GrantOfferLetterState> result = service.getGrantOfferLetterWorkflowState(projectId);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonErrors.notFoundError(Project.class, projectId)));
    }

    @Test
    public void testGetGrantOfferLetterWorkflowState() {

        Project projectInDB = newProject().build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(projectInDB);
        when(golWorkflowHandlerMock.getState(projectInDB)).thenReturn(GrantOfferLetterState.APPROVED);

        ServiceResult<GrantOfferLetterState> result = service.getGrantOfferLetterWorkflowState(projectId);

        assertTrue(result.isSuccess());
        assertEquals(GrantOfferLetterState.APPROVED, result.getSuccessObject());

    }

    private static final String webBaseUrl = "https://ifs-local-dev/dashboard";

    @Override
    protected GrantOfferLetterService supplyServiceUnderTest() {

        GrantOfferLetterServiceImpl projectGrantOfferService = new GrantOfferLetterServiceImpl();
        ReflectionTestUtils.setField(projectGrantOfferService, "webBaseUrl", webBaseUrl);
        return projectGrantOfferService;
    }

}
