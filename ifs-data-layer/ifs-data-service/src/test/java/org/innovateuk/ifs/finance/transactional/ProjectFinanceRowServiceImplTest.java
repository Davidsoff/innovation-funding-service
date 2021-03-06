package org.innovateuk.ifs.finance.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.domain.Question;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.finance.domain.ProjectFinance;
import org.innovateuk.ifs.finance.domain.ProjectFinanceRow;
import org.innovateuk.ifs.finance.handler.OrganisationFinanceDefaultHandler;
import org.innovateuk.ifs.finance.handler.ProjectFinanceHandler;
import org.innovateuk.ifs.finance.handler.item.FinanceRowHandler;
import org.innovateuk.ifs.finance.handler.item.MaterialsHandler;
import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.finance.resource.ProjectFinanceResourceId;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.innovateuk.ifs.finance.resource.cost.Materials;
import org.innovateuk.ifs.form.domain.FormInput;
import org.innovateuk.ifs.project.domain.Project;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.resource.OrganisationTypeEnum;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.HashMap;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.LambdaMatcher.lambdaMatches;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.application.builder.QuestionBuilder.newQuestion;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.finance.builder.ProjectFinanceResourceBuilder.newProjectFinanceResource;
import static org.innovateuk.ifs.finance.builder.ProjectFinanceRowBuilder.newProjectFinanceRow;
import static org.innovateuk.ifs.finance.domain.builder.OrganisationSizeBuilder.newOrganisationSize;
import static org.innovateuk.ifs.form.builder.FormInputBuilder.newFormInput;
import static org.innovateuk.ifs.project.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.user.builder.OrganisationTypeBuilder.newOrganisationType;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class ProjectFinanceRowServiceImplTest extends BaseServiceUnitTest<ProjectFinanceRowServiceImpl> {
    @Mock
    private ProjectFinanceHandler projectFinanceHandlerMock;

    @Mock
    private OrganisationFinanceDefaultHandler organisationFinanceDefaultHandlerMock;

    private HashMap<FinanceRowType, Question> costTypeQuestion;

    private Organisation organisation;

    private Project project;

    private ProjectFinanceRow materialCost;

    private ProjectFinance newFinance;

    private Materials material;

    private Question question;

    @Before
    public void setUp() throws Exception {
        Long organisationId = 456L;

        Long projectId = 123L;

        Long costItemId = 222L;

        Long projectFinanceId = 111L;

        Long questionId = 789L;

        Competition competition = newCompetition().withCompetitionStatus(CompetitionStatus.CLOSED).build();

        organisation = newOrganisation().withId(organisationId).withOrganisationType(newOrganisationType().withOrganisationType(OrganisationTypeEnum.BUSINESS).build()).build();

        Application application = newApplication().withCompetition(competition).build();

        project = newProject().withId(projectId).withApplication(application).build();

        newFinance = new ProjectFinance(organisation, newOrganisationSize().build(), project);
        newFinance.setId(projectFinanceId);

        question = newQuestion().withId(questionId).withCompetition(competition).build();

        material = new Materials();
        material.setCost(BigDecimal.valueOf(100));
        material.setItem("Screws");
        material.setQuantity(5);

        costTypeQuestion = new HashMap<>();
        for (FinanceRowType costType : FinanceRowType.values()) {
            if (FinanceRowType.ACADEMIC != costType) {
                setUpCostTypeQuestions(competition, costType);
            }
        }

        materialCost = newProjectFinanceRow().withId(costItemId).withQuestion(costTypeQuestion.get(FinanceRowType.MATERIALS)).withTarget(newFinance).build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);

        when(organisationRepositoryMock.findOne(organisation.getId())).thenReturn(organisation);

        when(organisationFinanceDelegateMock.getOrganisationFinanceHandler(OrganisationTypeEnum.BUSINESS.getId())).thenReturn(organisationFinanceDefaultHandlerMock);

        when(projectFinanceRowRepositoryMock.findOne(costItemId)).thenReturn(materialCost);

        when(organisationFinanceDefaultHandlerMock.costItemToProjectCost(material)).thenReturn(materialCost);

        when(questionRepositoryMock.findOne(questionId)).thenReturn(question);

        when(projectFinanceRepositoryMock.findOne(projectFinanceId)).thenReturn(newFinance);

        when(projectFinanceRepositoryMock.findByProjectIdAndOrganisationId(projectId, organisationId)).thenReturn(newFinance);
    }

    @Test
    public void testGetCostItem(){
        ServiceResult<FinanceRowItem> result = service.getCostItem(materialCost.getId());
        assertTrue(result.isSuccess());
    }

    @Test
    public void testAddCost() {

        ProjectFinance newFinanceExpectations = argThat(lambdaMatches(finance -> {
            assertEquals(project, finance.getProject());
            assertEquals(organisation, finance.getOrganisation());
            return true;
        }));


        ProjectFinanceResource expectedFinance = newProjectFinanceResource().
                with(id(newFinance.getId())).
                withOrganisation(organisation.getId()).
                withProject(project.getId()).
                build();

        when(projectFinanceRepositoryMock.save(newFinanceExpectations)).thenReturn(newFinance);
        when(projectFinanceMapperMock.mapToResource(newFinance)).thenReturn(expectedFinance);

        when(projectFinanceRowRepositoryMock.save(materialCost)).thenReturn(materialCost);

        ServiceResult<FinanceRowItem> result = service.addCost(newFinance.getId(), question.getId(), material);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testUpdateCost() {
        when(projectFinanceRowRepositoryMock.save(any(ProjectFinanceRow.class))).thenReturn(materialCost);
        ServiceResult<FinanceRowItem> result = service.updateCost(materialCost.getId(), material);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testAddCostWithoutPersisting(){
        ServiceResult<FinanceRowItem> result = service.addCostWithoutPersisting(newFinance.getId(), question.getId());
        assertTrue(result.isSuccess());
    }

    @Test
    public void testAddCostWithoutPersistingWhenQFromDiffComp(){
        Competition anotherCompetition = newCompetition().build();
        Question questionFromAnotherCompetition = newQuestion().withCompetition(anotherCompetition).build();
        ServiceResult<FinanceRowItem> result = service.addCostWithoutPersisting(newFinance.getId(), questionFromAnotherCompetition.getId());
        assertTrue(result.isFailure());
        assertTrue(result.getErrors().contains(notFoundError(Question.class)));
    }

    @Test
    public void testDeleteCost(){
        ServiceResult<Void> result = service.deleteCost(project.getId(), organisation.getId(), materialCost.getId());
        assertTrue(result.isSuccess());
    }

    @Test
    public void testDeleteCostFailsWhenNotMatchingOrg(){
        ProjectFinance otherProjectFinance = new ProjectFinance(organisation, newOrganisationSize().build(), project);
        ProjectFinanceRow costFromOtherProjectFinance = newProjectFinanceRow().withQuestion(costTypeQuestion.get(FinanceRowType.MATERIALS)).withTarget(otherProjectFinance).build();
        ServiceResult<Void> result = service.deleteCost(project.getId(), organisation.getId(), costFromOtherProjectFinance.getId());
        assertTrue(result.isFailure());
        assertTrue(result.getErrors().contains(notFoundError(ProjectFinanceRow.class)));
    }

    @Test
    public void testGetCostHandler(){
        when(projectFinanceRowMapperMock.mapIdToDomain(1L)).thenReturn(materialCost);
        when(organisationFinanceDefaultHandlerMock.costToCostItem(materialCost)).thenReturn(material);
        when(organisationFinanceDefaultHandlerMock.getCostHandler(FinanceRowType.MATERIALS)).thenReturn(new MaterialsHandler());

        FinanceRowHandler financeRowHandler = service.getCostHandler(material);
        assertNotNull(financeRowHandler);
        assertTrue(financeRowHandler instanceof MaterialsHandler);
    }

    @Test
    public void testFinanceCheckDetails() {
        ProjectFinanceResourceId projectFinanceResourceId = new ProjectFinanceResourceId(project.getId(), organisation.getId());
        ProjectFinanceResource expected = newProjectFinanceResource().build();

        //successful test
        when(projectFinanceHandlerMock.getProjectOrganisationFinances(projectFinanceResourceId)).thenReturn(serviceSuccess(expected));
        ServiceResult<ProjectFinanceResource> result = service.financeChecksDetails(project.getId(), organisation.getId());
        assertTrue(result.isSuccess());
        assertEquals(result.getSuccessObject(), expected);

        //unsuccessful test
        when(projectFinanceHandlerMock.getProjectOrganisationFinances(projectFinanceResourceId)).thenReturn(serviceFailure(notFoundError(ProjectFinanceResource.class)));
        result = service.financeChecksDetails(project.getId(), organisation.getId());
        assertTrue(result.isFailure());
        assertTrue(result.getErrors().contains(notFoundError(ProjectFinanceResource.class)));
    }

    @Override
    protected ProjectFinanceRowServiceImpl supplyServiceUnderTest() {
        return new ProjectFinanceRowServiceImpl();
    }

    private void setUpCostTypeQuestions(Competition competition, FinanceRowType costType) {
        FormInput formInput = newFormInput()
                .withType(costType.getFormInputType())
                .build();
        Question question = newQuestion().withFormInputs(singletonList(formInput)).build();

        costTypeQuestion.put(costType, question);
        when(questionServiceMock.getQuestionByCompetitionIdAndFormInputType(eq(competition.getId()), eq(costType.getFormInputType()))).thenReturn(serviceSuccess(question));
    }
}
