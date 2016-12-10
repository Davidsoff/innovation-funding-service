package org.innovateuk.ifs.management.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.category.resource.CategoryResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.invite.resource.AvailableAssessorResource;
import org.innovateuk.ifs.invite.resource.ExistingUserStagedInviteResource;
import org.innovateuk.ifs.management.model.InviteAssessorsFindModelPopulator;
import org.innovateuk.ifs.management.model.InviteAssessorsInviteModelPopulator;
import org.innovateuk.ifs.management.model.InviteAssessorsOverviewModelPopulator;
import org.innovateuk.ifs.management.viewmodel.AvailableAssessorViewModel;
import org.innovateuk.ifs.management.viewmodel.InviteAssessorsFindViewModel;
import org.innovateuk.ifs.management.viewmodel.InviteAssessorsViewModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.innovateuk.ifs.assessment.builder.CompetitionInviteResourceBuilder.newCompetitionInviteResource;
import static org.innovateuk.ifs.category.builder.CategoryResourceBuilder.newCategoryResource;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.IN_ASSESSMENT;
import static org.innovateuk.ifs.invite.builder.AvailableAssessorResourceBuilder.newAvailableAssessorResource;
import static org.innovateuk.ifs.user.resource.BusinessType.ACADEMIC;
import static org.innovateuk.ifs.user.resource.BusinessType.BUSINESS;
import static org.innovateuk.ifs.util.CollectionFunctions.forEachWithIndex;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class CompetitionManagementInviteAssessorsControllerTest extends BaseControllerMockMVCTest<CompetitionManagementInviteAssessorsController> {

    @Spy
    @InjectMocks
    private InviteAssessorsFindModelPopulator inviteAssessorsFindModelPopulator;

    @Spy
    @InjectMocks
    private InviteAssessorsInviteModelPopulator inviteAssessorsInviteModelPopulator;

    @Spy
    @InjectMocks
    private InviteAssessorsOverviewModelPopulator inviteAssessorsOverviewModelPopulator;

    private CompetitionResource competition;

    @Override
    protected CompetitionManagementInviteAssessorsController supplyControllerUnderTest() {
        return new CompetitionManagementInviteAssessorsController();
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();

        competition = newCompetitionResource()
                .withCompetitionStatus(IN_ASSESSMENT)
                .withName("Technology inspired")
                .build();

        when(competitionService.getById(competition.getId())).thenReturn(competition);
    }

    @Test
    public void assessors() throws Exception {
        Long competitionId = 1L;
        mockMvc.perform(get("/competition/{competitionId}/assessors", competitionId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(format("/competition/%s/assessors/find", competitionId)));
    }

    @Test
    public void find() throws Exception {
        List<AvailableAssessorResource> availableAssessorResources = setUpAvailableAssessorResources();

        when(competitionInviteRestService.getAvailableAssessors(competition.getId())).thenReturn(restSuccess(availableAssessorResources));

        MvcResult result = mockMvc.perform(get("/competition/{competitionId}/assessors/find", competition.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessors/find"))
                .andReturn();

        assertCompetitionDetails(competition, result);
        assertAvailableAssessors(availableAssessorResources, result);

        InOrder inOrder = inOrder(competitionService, competitionInviteRestService);
        inOrder.verify(competitionService).getById(competition.getId());
        inOrder.verify(competitionInviteRestService).getAvailableAssessors(competition.getId());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void invite() throws Exception {
        MvcResult result = mockMvc.perform(get("/competition/{competitionId}/assessors/invite", competition.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessors/invite"))
                .andReturn();

        assertCompetitionDetails(competition, result);

        verify(competitionService, only()).getById(competition.getId());
    }

    @Test
    public void overview() throws Exception {
        MvcResult result = mockMvc.perform(get("/competition/{competitionId}/assessors/overview", competition.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessors/overview"))
                .andReturn();

        assertCompetitionDetails(competition, result);

        verify(competitionService, only()).getById(competition.getId());
    }

    @Test
    public void inviteUser() throws Exception {
        String email = "firstname.lastname@example.com";

        List<AvailableAssessorResource> availableAssessorResources = setUpAvailableAssessorResources();

        ExistingUserStagedInviteResource expectedExistingUserStagedInviteResource = new ExistingUserStagedInviteResource(email, competition.getId());
        when(competitionInviteRestService.inviteUser(expectedExistingUserStagedInviteResource)).thenReturn(restSuccess(newCompetitionInviteResource().build()));
        when(competitionInviteRestService.getAvailableAssessors(competition.getId())).thenReturn(restSuccess(availableAssessorResources));

        MvcResult result = mockMvc.perform(post("/competition/{competitionId}/assessors/inviteUser", competition.getId())
                .param("email", email))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessors/find"))
                .andReturn();

        assertCompetitionDetails(competition, result);
        assertAvailableAssessors(availableAssessorResources, result);

        InOrder inOrder = inOrder(competitionService, competitionInviteRestService);
        inOrder.verify(competitionInviteRestService).inviteUser(expectedExistingUserStagedInviteResource);
        inOrder.verify(competitionService).getById(competition.getId());
        inOrder.verify(competitionInviteRestService).getAvailableAssessors(competition.getId());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void deleteInvite() throws Exception {
        String email = "firstname.lastname@example.com";

        List<AvailableAssessorResource> availableAssessorResources = setUpAvailableAssessorResources();

        when(competitionInviteRestService.deleteInvite(email, competition.getId())).thenReturn(restSuccess());
        when(competitionInviteRestService.getAvailableAssessors(competition.getId())).thenReturn(restSuccess(availableAssessorResources));

        MvcResult result = mockMvc.perform(post("/competition/{competitionId}/assessors/deleteInvite", competition.getId())
                .param("email", email))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessors/find"))
                .andReturn();

        assertCompetitionDetails(competition, result);
        assertAvailableAssessors(availableAssessorResources, result);

        InOrder inOrder = inOrder(competitionService, competitionInviteRestService);
        inOrder.verify(competitionInviteRestService).deleteInvite(email, competition.getId());
        inOrder.verify(competitionService).getById(competition.getId());
        inOrder.verify(competitionInviteRestService).getAvailableAssessors(competition.getId());
        inOrder.verifyNoMoreInteractions();
    }

    private List<AvailableAssessorResource> setUpAvailableAssessorResources() {
        return newAvailableAssessorResource()
                .withUserId(1L, 2L)
                .withFirstName("Dave", "John")
                .withLastName("Smith", "Barnes")
                .withEmail("dave@email.com", "john@email.com")
                .withBusinessType(BUSINESS, ACADEMIC)
                .withInnovationArea(newCategoryResource()
                        .withName("Earth Observation", "Healthcare, Analytical science")
                        .buildArray(2, CategoryResource.class))
                .withCompliant(TRUE, FALSE)
                .withAdded(TRUE, FALSE)
                .build(2);
    }

    private void assertCompetitionDetails(CompetitionResource expectedCompetition, MvcResult result) {
        InviteAssessorsViewModel model = (InviteAssessorsViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(expectedCompetition.getId(), model.getCompetitionId());
        assertEquals(expectedCompetition.getName(), model.getCompetitionName());
        assertInnovationSectorAndArea(model);
        assertStatistics(model);
    }

    private void assertInnovationSectorAndArea(InviteAssessorsViewModel model) {
        assertEquals("Health and life sciences", model.getInnovationSector());
        assertEquals("Agriculture and food", model.getInnovationArea());
    }

    private void assertStatistics(InviteAssessorsViewModel model) {
        assertEquals(60, model.getAssessorsInvited());
        assertEquals(23, model.getAssessorsAccepted());
        assertEquals(3, model.getAssessorsDeclined());
        assertEquals(6, model.getAssessorsStaged());
    }

    private void assertAvailableAssessors(List<AvailableAssessorResource> expectedAvailableAssessors, MvcResult result) {
        assertTrue(result.getModelAndView().getModel().get("model") instanceof InviteAssessorsFindViewModel);
        InviteAssessorsFindViewModel model = (InviteAssessorsFindViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(expectedAvailableAssessors.size(), model.getAssessors().size());

        forEachWithIndex(expectedAvailableAssessors, (i, assessorAvailableResource) -> {
            AvailableAssessorViewModel availableAssessorViewModel = model.getAssessors().get(i);
            String expectedName = assessorAvailableResource.getFirstName() + " " + assessorAvailableResource.getLastName();
            assertEquals(expectedName, availableAssessorViewModel.getName());
            assertEquals(assessorAvailableResource.getEmail(), availableAssessorViewModel.getEmail());
            assertEquals(assessorAvailableResource.getBusinessType(), availableAssessorViewModel.getBusinessType());
            assertEquals(assessorAvailableResource.getInnovationArea().getName(), availableAssessorViewModel.getInnovationArea());
            assertEquals(assessorAvailableResource.isCompliant(), availableAssessorViewModel.isCompliant());
            assertEquals(assessorAvailableResource.isAdded(), availableAssessorViewModel.isAdded());
        });
    }
}