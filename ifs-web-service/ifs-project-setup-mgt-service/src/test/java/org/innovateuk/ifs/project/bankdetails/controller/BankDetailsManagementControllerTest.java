package org.innovateuk.ifs.project.bankdetails.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.address.resource.AddressTypeResource;
import org.innovateuk.ifs.organisation.resource.OrganisationAddressResource;
import org.innovateuk.ifs.project.bankdetails.populator.BankDetailsReviewModelPopulator;
import org.innovateuk.ifs.project.bankdetails.resource.BankDetailsResource;
import org.innovateuk.ifs.project.bankdetails.resource.ProjectBankDetailsStatusSummary;
import org.innovateuk.ifs.project.bankdetails.viewmodel.BankDetailsReviewViewModel;
import org.innovateuk.ifs.project.bankdetails.viewmodel.ChangeBankDetailsViewModel;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.innovateuk.ifs.address.builder.AddressResourceBuilder.newAddressResource;
import static org.innovateuk.ifs.address.resource.OrganisationAddressType.BANK_DETAILS;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.organisation.builder.OrganisationAddressResourceBuilder.newOrganisationAddressResource;
import static org.innovateuk.ifs.project.bankdetails.builder.BankDetailsResourceBuilder.newBankDetailsResource;
import static org.innovateuk.ifs.project.bankdetails.builder.ProjectBankDetailsStatusSummaryBuilder.newProjectBankDetailsStatusSummary;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static org.innovateuk.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.user.resource.UserRoleType.FINANCE_CONTACT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class BankDetailsManagementControllerTest extends BaseControllerMockMVCTest<BankDetailsManagementController> {
    private ProjectResource project;
    private OrganisationResource organisationResource;
    private OrganisationResource updatedOrganisationResource;

    private BankDetailsResource bankDetailsResource;
    private BankDetailsResource notUpdatedBankDetailsResource;
    private BankDetailsResource updatedBankDetailsResource;
    private BankDetailsResource updatedAddressBankDetailsResource;

    private List<ProjectUserResource> projectUsers;
    private BankDetailsReviewViewModel bankDetailsReviewViewModel;
    private ChangeBankDetailsViewModel notUpdatedChangeBankDetailsViewModel;
    @Mock
    private BankDetailsReviewModelPopulator bankDetailsReviewModelPopulator;

    @Before
    public void setUp(){
        super.setUp();
        organisationResource = newOrganisationResource().withName("Vitruvius Stonework Limited").withCompanyHouseNumber("60674010").build();
        updatedOrganisationResource = newOrganisationResource().withId(organisationResource.getId()).withName("Vitruvius Stonework").withCompanyHouseNumber("60674010").build();
        OrganisationAddressResource organisationAddressResource = newOrganisationAddressResource().withOrganisation(organisationResource.getId()).withAddress(newAddressResource().withAddressLine1("Montrose House 1").withAddressLine2("Clayhill Park").withAddressLine3("Cheshire West and Chester").withTown("Neston").withCounty("Cheshire").withPostcode("CH64 3RU").build()).build();
        project = newProjectResource().build();

        bankDetailsResource = newBankDetailsResource().withProject(project.getId()).withOrganisation(organisationResource.getId()).withOrganiationAddress(organisationAddressResource).withAccountNumber("51406795").withSortCode("404745").withCompanyName(organisationResource.getName()).withRegistrationNumber(organisationResource.getCompanyHouseNumber()).build();

        AddressTypeResource addressTypeResource = new AddressTypeResource(BANK_DETAILS.getOrdinal(), BANK_DETAILS.name());

        OrganisationAddressResource unmodifiedOrganisationAddressResource = newOrganisationAddressResource().withOrganisation(organisationResource.getId()).withAddressType(addressTypeResource).withAddress(newAddressResource().withAddressLine1("Montrose House 1").withAddressLine2("Clayhill Park").withAddressLine3("Cheshire West and Chester").withTown("Neston").withCounty("Cheshire").withPostcode("CH64 3RU").build()).build();
        unmodifiedOrganisationAddressResource.setId(null);
        unmodifiedOrganisationAddressResource.getAddress().setId(null);

        updatedBankDetailsResource = newBankDetailsResource().withId(bankDetailsResource.getId()).withProject(project.getId()).withOrganisation(organisationResource.getId()).withOrganiationAddress(unmodifiedOrganisationAddressResource).withAccountNumber(bankDetailsResource.getAccountNumber()).withSortCode("404746").withCompanyName(organisationResource.getName()).withRegistrationNumber(bankDetailsResource.getRegistrationNumber()).build();

        notUpdatedBankDetailsResource = newBankDetailsResource().withId(bankDetailsResource.getId()).withProject(project.getId()).withOrganisation(organisationResource.getId()).withOrganiationAddress(unmodifiedOrganisationAddressResource).withAccountNumber(bankDetailsResource.getAccountNumber()).withSortCode(bankDetailsResource.getSortCode()).withCompanyName(organisationResource.getName()).withRegistrationNumber(bankDetailsResource.getRegistrationNumber()).build();

        OrganisationAddressResource updatedLine1OrganisationAddressResource = newOrganisationAddressResource().withOrganisation(organisationResource.getId()).withAddressType(addressTypeResource).withAddress(newAddressResource().withAddressLine1("Montrose House 2").withAddressLine2("Clayhill Park").withAddressLine3("Cheshire West and Chester").withTown("Neston").withCounty("Cheshire").withPostcode("CH64 3RU").build()).build();
        updatedLine1OrganisationAddressResource.setId(null);
        updatedLine1OrganisationAddressResource.getAddress().setId(null);
        updatedAddressBankDetailsResource = newBankDetailsResource().withId(bankDetailsResource.getId()).withProject(project.getId()).withOrganisation(organisationResource.getId()).withOrganiationAddress(updatedLine1OrganisationAddressResource).withAccountNumber(bankDetailsResource.getAccountNumber()).withSortCode(bankDetailsResource.getSortCode()).withCompanyName(organisationResource.getName()).withRegistrationNumber(bankDetailsResource.getRegistrationNumber()).build();

        projectUsers = newProjectUserResource().build(3);
        projectUsers.get(0).setRoleName(FINANCE_CONTACT.getName());
        projectUsers.get(0).setOrganisation(organisationResource.getId());

        bankDetailsReviewViewModel = buildModelView(project, projectUsers.get(0), organisationResource, bankDetailsResource);

        notUpdatedChangeBankDetailsViewModel = new ChangeBankDetailsViewModel(bankDetailsReviewViewModel.getProjectId(), bankDetailsReviewViewModel.getApplicationId(), bankDetailsReviewViewModel.getProjectName(), bankDetailsReviewViewModel.getFinanceContactName(), bankDetailsReviewViewModel.getFinanceContactEmail(), bankDetailsReviewViewModel.getFinanceContactPhoneNumber(), bankDetailsReviewViewModel.getOrganisationId(), bankDetailsReviewViewModel.getOrganisationName(), bankDetailsReviewViewModel.getRegistrationNumber(), bankDetailsReviewViewModel.getBankAccountNumber(), bankDetailsReviewViewModel.getSortCode(), bankDetailsReviewViewModel.getOrganisationAddress(), bankDetailsReviewViewModel.getVerified(), bankDetailsReviewViewModel.getCompanyNameScore(), bankDetailsReviewViewModel.getRegistrationNumberMatched(), bankDetailsReviewViewModel.getAddressScore(), bankDetailsReviewViewModel.getApproved(), bankDetailsReviewViewModel.getApprovedManually(), false);
    }

    private BankDetailsReviewViewModel buildModelView(ProjectResource project, ProjectUserResource financeContact, OrganisationResource organisation, BankDetailsResource bankDetails){
        return new BankDetailsReviewViewModel(
                project.getId(),
                project.getApplication(),
                project.getName(),
                financeContact.getUserName(),
                financeContact.getEmail(),
                financeContact.getPhoneNumber(),
                organisation.getId(),
                organisation.getName(),
                organisation.getCompanyHouseNumber(),
                bankDetails.getAccountNumber(),
                bankDetails.getSortCode(),
                bankDetails.getOrganisationAddress().getAddress().getAsSingleLine(),
                bankDetails.isVerified(),
                bankDetails.getCompanyNameScore(),
                bankDetails.getRegistrationNumberMatched(),
                bankDetails.getAddressScore(),
                bankDetails.isApproved(),
                bankDetails.isManualApproval());
    }

    @Override
    protected BankDetailsManagementController supplyControllerUnderTest() {
        return new BankDetailsManagementController();
    }

    @Test
    public void canViewBankDetailsWhenBankDetailsSubmitted() throws Exception {
        when(organisationService.getOrganisationById(organisationResource.getId())).thenReturn(organisationResource);
        when(bankDetailsRestService.getBankDetailsByProjectAndOrganisation(project.getId(), organisationResource.getId())).thenReturn(restSuccess(bankDetailsResource));
        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(bankDetailsReviewModelPopulator.populateBankDetailsReviewViewModel(organisationResource, project, bankDetailsResource)).thenReturn(bankDetailsReviewViewModel);
        MvcResult result = mockMvc.perform(get("/project/" + project.getId() + "/organisation/" + organisationResource.getId() + "/review-bank-details")).
                andExpect(view().name("project/review-bank-details")).
                andExpect(status().isOk()).
                andReturn();

        Map<String, Object> modelMap = result.getModelAndView().getModel();
        BankDetailsReviewViewModel model = (BankDetailsReviewViewModel) modelMap.get("model");
        assertEquals(model, bankDetailsReviewViewModel);
    }

    @Test
    public void canViewBankDetailsWhenBankDetailsReSubmitted() throws Exception {
        when(organisationService.getOrganisationById(organisationResource.getId())).thenReturn(organisationResource);
        when(bankDetailsRestService.getBankDetailsByProjectAndOrganisation(project.getId(), organisationResource.getId())).thenReturn(restSuccess(bankDetailsResource));
        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(bankDetailsReviewModelPopulator.populateBankDetailsReviewViewModel(organisationResource, project, bankDetailsResource)).thenReturn(bankDetailsReviewViewModel);
        bankDetailsReviewViewModel.setApprovedManually(true);

        //The manualApproval flag will be 'true' in case of re-submission
        bankDetailsResource.setManualApproval(true);
        MvcResult result = mockMvc.perform(get("/project/" + project.getId() + "/organisation/" + organisationResource.getId() + "/review-bank-details")).
                andExpect(view().name("project/review-bank-details")).
                andExpect(status().isOk()).
                andReturn();

        Map<String, Object> modelMap = result.getModelAndView().getModel();
        BankDetailsReviewViewModel model = (BankDetailsReviewViewModel) modelMap.get("model");
        assertEquals(bankDetailsReviewViewModel.getBankAccountNumber(), model.getBankAccountNumber());
            assertEquals(true, model.getApprovedManually());
    }

    @Test
    public void canViewBankDetailsChangeForm() throws Exception {
        when(organisationService.getOrganisationById(organisationResource.getId())).thenReturn(organisationResource);
        when(projectService.getById(project.getId())).thenReturn(project);
        when(bankDetailsRestService.getBankDetailsByProjectAndOrganisation(project.getId(), organisationResource.getId())).thenReturn(restSuccess(bankDetailsResource));
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(bankDetailsReviewModelPopulator.populateBankDetailsReviewViewModel(organisationResource, project, bankDetailsResource)).thenReturn(bankDetailsReviewViewModel);

        MvcResult result = mockMvc.perform(get("/project/" + project.getId() + "/organisation/" + organisationResource.getId() + "/review-bank-details/change")).
                andExpect(view().name("project/change-bank-details")).
                andExpect(status().isOk()).
                andReturn();

        Map<String, Object> modelMap = result.getModelAndView().getModel();
        BankDetailsReviewViewModel model = (BankDetailsReviewViewModel) modelMap.get("model");
        assertEquals(model, notUpdatedChangeBankDetailsViewModel);
    }

    @Test
    public void canUpdateAccountDetails() throws Exception {
        when(organisationService.getOrganisationById(organisationResource.getId())).thenReturn(organisationResource);
        when(projectService.getById(project.getId())).thenReturn(project);
        when(bankDetailsRestService.getBankDetailsByProjectAndOrganisation(project.getId(), organisationResource.getId())).thenReturn(restSuccess(bankDetailsResource));
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(bankDetailsRestService.updateBankDetails(project.getId(), updatedBankDetailsResource)).thenReturn(restSuccess());
        when(organisationService.updateNameAndRegistration(organisationResource)).thenReturn(organisationResource);

        mockMvc.perform(post("/project/" + project.getId() + "/organisation/" + organisationResource.getId() + "/review-bank-details/change").
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param("organisationName", "Vitruvius Stonework Limited").
                param("registrationNumber", "60674010").
                param("accountNumber", "51406795").
                param("sortCode", "404746").
                param("addressForm.selectedPostcode.addressLine1", "Montrose House 1").
                param("addressForm.selectedPostcode.addressLine2", "Clayhill Park").
                param("addressForm.selectedPostcode.addressLine3", "Cheshire West and Chester").
                param("addressForm.selectedPostcode.town", "Neston").
                param("addressForm.selectedPostcode.county", "Cheshire").
                param("addressForm.selectedPostcode.postcode", "CH64 3RU")).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/project/" + project.getId() +"/organisation/" + organisationResource.getId() + "/review-bank-details"));

        verify(bankDetailsRestService).updateBankDetails(project.getId(), updatedBankDetailsResource);
        verify(organisationService).updateNameAndRegistration(organisationResource);
    }

    @Test
    public void canUpdateBankAddress() throws Exception {
        when(organisationService.getOrganisationById(organisationResource.getId())).thenReturn(organisationResource);
        when(projectService.getById(project.getId())).thenReturn(project);
        when(bankDetailsRestService.getBankDetailsByProjectAndOrganisation(project.getId(), organisationResource.getId())).thenReturn(restSuccess(bankDetailsResource));
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(bankDetailsRestService.updateBankDetails(project.getId(), updatedAddressBankDetailsResource)).thenReturn(restSuccess());
        when(organisationService.updateNameAndRegistration(organisationResource)).thenReturn(organisationResource);

        mockMvc.perform(post("/project/" + project.getId() + "/organisation/" + organisationResource.getId() + "/review-bank-details/change").
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param("organisationName", "Vitruvius Stonework Limited").
                param("registrationNumber", "60674010").
                param("accountNumber", "51406795").
                param("sortCode", "404745").
                param("addressForm.selectedPostcode.addressLine1", "Montrose House 2").
                param("addressForm.selectedPostcode.addressLine2", "Clayhill Park").
                param("addressForm.selectedPostcode.addressLine3", "Cheshire West and Chester").
                param("addressForm.selectedPostcode.town", "Neston").
                param("addressForm.selectedPostcode.county", "Cheshire").
                param("addressForm.selectedPostcode.postcode", "CH64 3RU")).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/project/" + project.getId() +"/organisation/" + organisationResource.getId() + "/review-bank-details"));

        verify(bankDetailsRestService).updateBankDetails(project.getId(), updatedAddressBankDetailsResource);
        verify(organisationService).updateNameAndRegistration(organisationResource);
    }

    @Test
    public void canUpdateOrganisationDetails() throws Exception {
        when(organisationService.getOrganisationById(organisationResource.getId())).thenReturn(organisationResource);
        when(projectService.getById(project.getId())).thenReturn(project);
        when(bankDetailsRestService.getBankDetailsByProjectAndOrganisation(project.getId(), organisationResource.getId())).thenReturn(restSuccess(bankDetailsResource));
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(bankDetailsRestService.updateBankDetails(project.getId(), notUpdatedBankDetailsResource)).thenReturn(restSuccess());
        when(organisationService.updateNameAndRegistration(updatedOrganisationResource)).thenReturn(updatedOrganisationResource);

        mockMvc.perform(post("/project/" + project.getId() + "/organisation/" + organisationResource.getId() + "/review-bank-details/change").
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param("organisationName", "Vitruvius Stonework").
                param("registrationNumber", "60674010").
                param("accountNumber", "51406795").
                param("sortCode", "404745").
                param("addressForm.selectedPostcode.addressLine1", "Montrose House 1").
                param("addressForm.selectedPostcode.addressLine2", "Clayhill Park").
                param("addressForm.selectedPostcode.addressLine3", "Cheshire West and Chester").
                param("addressForm.selectedPostcode.town", "Neston").
                param("addressForm.selectedPostcode.county", "Cheshire").
                param("addressForm.selectedPostcode.postcode", "CH64 3RU")).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/project/" + project.getId() +"/organisation/" + organisationResource.getId() + "/review-bank-details"));

        verify(bankDetailsRestService).updateBankDetails(any(Long.class), any(BankDetailsResource.class));
        verify(organisationService).updateNameAndRegistration(updatedOrganisationResource);
    }

    @Test
    public void testViewPartnerBankDetails() throws  Exception {
        Long projectId = 123L;
        final ProjectBankDetailsStatusSummary bankDetailsStatusSummary = newProjectBankDetailsStatusSummary().build();
        when(bankDetailsRestService.getBankDetailsStatusSummaryByProject(projectId)).thenReturn(restSuccess(bankDetailsStatusSummary));
        MvcResult result = mockMvc.perform(get("/project/" + projectId + "/review-all-bank-details")).andExpect(status().isOk()).andExpect(view().name("project/bank-details-status")).andReturn();
        assertEquals(bankDetailsStatusSummary, result.getModelAndView().getModel().get("model"));
    }
}
