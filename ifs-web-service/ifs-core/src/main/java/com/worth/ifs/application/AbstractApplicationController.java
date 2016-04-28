package com.worth.ifs.application;

import static com.worth.ifs.application.service.Futures.call;
import static com.worth.ifs.util.CollectionFunctions.simpleFilter;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;

import com.worth.ifs.BaseController;
import com.worth.ifs.application.domain.SectionType;
import com.worth.ifs.application.finance.view.FinanceHandler;
import com.worth.ifs.application.finance.view.FinanceOverviewModelManager;
import com.worth.ifs.application.form.ApplicationForm;
import com.worth.ifs.application.form.Form;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.application.resource.QuestionStatusResource;
import com.worth.ifs.application.resource.ResponseResource;
import com.worth.ifs.application.resource.SectionResource;
import com.worth.ifs.application.service.ApplicationService;
import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.application.service.OrganisationService;
import com.worth.ifs.application.service.QuestionService;
import com.worth.ifs.application.service.ResponseService;
import com.worth.ifs.application.service.SectionService;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.security.UserAuthenticationService;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.filter.CookieFlashMessageFilter;
import com.worth.ifs.form.resource.FormInputResource;
import com.worth.ifs.form.resource.FormInputResponseResource;
import com.worth.ifs.form.service.FormInputResponseService;
import com.worth.ifs.form.service.FormInputService;
import com.worth.ifs.invite.constant.InviteStatusConstants;
import com.worth.ifs.invite.resource.InviteOrganisationResource;
import com.worth.ifs.invite.resource.InviteResource;
import com.worth.ifs.invite.service.InviteRestService;
import com.worth.ifs.user.domain.OrganisationTypeEnum;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.resource.ProcessRoleResource;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.service.OrganisationRestService;
import com.worth.ifs.user.service.ProcessRoleService;
import com.worth.ifs.user.service.UserService;

/**
 * This object contains shared methods for all the Controllers related to the {@link ApplicationResource} data.
 */
public abstract class AbstractApplicationController extends BaseController {
    public static final String MARK_AS_COMPLETE = "mark_as_complete";
    public static final String MARK_SECTION_AS_COMPLETE = "mark_section_as_complete";
    public static final String MARK_SECTION_AS_INCOMPLETE = "mark_section_as_incomplete";
    public static final String MARK_AS_INCOMPLETE = "mark_as_incomplete";
    public static final String UPLOAD_FILE = "upload_file";
    public static final String REMOVE_UPLOADED_FILE = "remove_uploaded_file";
    public static final String ADD_COST = "add_cost";
    public static final String REMOVE_COST = "remove_cost";
    public static final String EDIT_QUESTION = "edit_question";
    public static final String APPLICATION_FORM = "application-form";
    public static final String MODEL_ATTRIBUTE_FORM = "form";
    public static final String QUESTION_ID = "questionId";
    public static final String APPLICATION_ID = "applicationId";
    public static final String APPLICATION_BASE_URL = "/application/";

    public static final String ASSIGN_QUESTION_PARAM = "assign_question";
    public static final String FORM_MODEL_ATTRIBUTE = "form";
    public static final String APPLICATION_START_DATE = "application.startDate";
    public static final String QUESTION_URL = "/question/";
    public static final String SECTION_URL = "/section/";
    private static final Log LOG = LogFactory.getLog(AbstractApplicationController.class);

    @Autowired
    protected MessageSource messageSource;

    @Autowired
    protected ResponseService responseService;

    @Autowired
    protected FormInputResponseService formInputResponseService;

    @Autowired
    protected QuestionService questionService;

    @Autowired
    protected ApplicationService applicationService;

    @Autowired
    protected SectionService sectionService;

    @Autowired
    protected UserService userService;

    @Autowired
    protected ProcessRoleService processRoleService;

    @Autowired
    protected UserAuthenticationService userAuthenticationService;

    @Autowired
    protected OrganisationService organisationService;

    @Autowired
    protected OrganisationRestService organisationRestService;

    @Autowired
    protected CookieFlashMessageFilter cookieFlashMessageFilter;

    @Autowired
    protected FormInputService formInputService;

    @Autowired
    protected CompetitionService competitionService;

    @Autowired
    protected InviteRestService inviteRestService;
    
    @Autowired
    protected FinanceOverviewModelManager financeOverviewModelManager;

    @Autowired
    protected FinanceHandler financeHandler;

    protected Long extractAssigneeProcessRoleIdFromAssignSubmit(HttpServletRequest request) {
        Long assigneeId = null;
        Map<String, String[]> params = request.getParameterMap();
        if(params.containsKey(ASSIGN_QUESTION_PARAM)){
            String assign = request.getParameter(ASSIGN_QUESTION_PARAM);
            assigneeId = Long.valueOf(assign.split("_")[1]);
        }

        return assigneeId;
    }

    protected Long extractQuestionProcessRoleIdFromAssignSubmit(HttpServletRequest request) {
        Long questionId = null;
        Map<String, String[]> params = request.getParameterMap();
        if(params.containsKey(ASSIGN_QUESTION_PARAM)){
            String assign = request.getParameter(ASSIGN_QUESTION_PARAM);
            questionId = Long.valueOf(assign.split("_")[0]);
        }

        return questionId;
    }

    protected void assignQuestion(HttpServletRequest request, Long applicationId) {
        UserResource user = userAuthenticationService.getAuthenticatedUser(request);
        ProcessRoleResource assignedBy = processRoleService.findProcessRole(user.getId(), applicationId);

        Map<String, String[]> params = request.getParameterMap();
        if(params.containsKey(ASSIGN_QUESTION_PARAM)){
            Long questionId = extractQuestionProcessRoleIdFromAssignSubmit(request);
            Long assigneeId = extractAssigneeProcessRoleIdFromAssignSubmit(request);

            questionService.assign(questionId, applicationId, assigneeId, assignedBy.getId());
        }
    }

    /**
     * Get the details of the current application, add this to the model so we can use it in the templates.
     */
    protected ApplicationResource addApplicationDetails(ApplicationResource application,
                                                        CompetitionResource competition,
                                                        Long userId,
                                                        Optional<SectionResource> section,
                                                        Optional<Long> currentQuestionId,
                                                        Model model,
                                                        ApplicationForm form,
                                                        List<ProcessRoleResource> userApplicationRoles) {
        model.addAttribute("currentApplication", application);
        model.addAttribute("currentCompetition", competition);

        Optional<OrganisationResource> userOrganisation = getUserOrganisation(userId, userApplicationRoles);

        if(form == null){
            form = new ApplicationForm();
        }
        form.setApplication(application);

        addOrganisationDetails(model, application, userOrganisation, userApplicationRoles);
        addQuestionsDetails(model, application, form);
        addUserDetails(model, application, userId);
        addApplicationFormDetailInputs(application, form);
        addMappedSectionsDetails(model, application, competition, section, userOrganisation);

        addAssignableDetails(model, application, userOrganisation.orElse(null), userId, section, currentQuestionId);
        addCompletedDetails(model, application, userOrganisation, userApplicationRoles);

        model.addAttribute(FORM_MODEL_ATTRIBUTE, form);
        return application;
    }

    protected  void addApplicationFormDetailInputs(ApplicationResource application, Form form) {
        Map<String, String> formInputs = form.getFormInput();
        formInputs.put("application_details-title", application.getName());
        formInputs.put("application_details-duration", String.valueOf(application.getDurationInMonths()));
        if(application.getStartDate() == null){
            formInputs.put("application_details-startdate_day", "");
            formInputs.put("application_details-startdate_month", "");
            formInputs.put("application_details-startdate_year", "");
        }else{
            formInputs.put("application_details-startdate_day", String.valueOf(application.getStartDate().getDayOfMonth()));
            formInputs.put("application_details-startdate_month", String.valueOf(application.getStartDate().getMonthValue()));
            formInputs.put("application_details-startdate_year", String.valueOf(application.getStartDate().getYear()));
        }
        form.setFormInput(formInputs);
    }

    protected  void addApplicationInputs(ApplicationResource application, Model model) {
        model.addAttribute("application_title", application.getName());
        model.addAttribute("application_duration", String.valueOf(application.getDurationInMonths()));
        if(application.getStartDate() == null){
            model.addAttribute("application_startdate_day", "");
            model.addAttribute("application_startdate_month", "");
            model.addAttribute("application_startdate_year", "");
        }
        else{
            model.addAttribute("application_startdate_day", String.valueOf(application.getStartDate().getDayOfMonth()));
            model.addAttribute("application_startdate_month", String.valueOf(application.getStartDate().getMonthValue()));
            model.addAttribute("application_startdate_year", String.valueOf(application.getStartDate().getYear()));
        }
    }

    protected void addOrganisationDetails(Model model, ApplicationResource application, Optional<OrganisationResource> userOrganisation,
                                          List<ProcessRoleResource> userApplicationRoles) {

        model.addAttribute("userOrganisation", userOrganisation.orElse(null));
        SortedSet<OrganisationResource> organisations = getApplicationOrganisations(userApplicationRoles);
        model.addAttribute("applicationOrganisations", organisations);
        model.addAttribute("academicOrganisations", getAcademicOrganisations(organisations));
        
        model.addAttribute("applicationOrganisations", organisations);
        
        List<String> activeApplicationOrganisationNames = organisations.stream().map(OrganisationResource::getName).collect(Collectors.toList());
        
        List<String> pendingOrganisationNames = pendingInvitations(application).stream()
        		.map(InviteResource::getInviteOrganisationName)
        		.distinct()
        		.filter(orgName -> StringUtils.hasText(orgName)
                        && activeApplicationOrganisationNames.stream().noneMatch(organisationName -> organisationName.equals(orgName))).collect(Collectors.toList());

        model.addAttribute("pendingOrganisationNames", pendingOrganisationNames);
        
        Optional<OrganisationResource> leadOrganisation = getApplicationLeadOrganisation(userApplicationRoles);
        leadOrganisation.ifPresent(org ->
                        model.addAttribute("leadOrganisation", org)
        );
    }

    protected void addQuestionsDetails(Model model, ApplicationResource application, Form form) {
        LOG.info("*********************");
        LOG.info(application.getId());
        List<FormInputResponseResource> responses = getFormInputResponses(application);
        Map<Long, FormInputResponseResource> mappedResponses = formInputResponseService.mapFormInputResponsesToFormInput(responses);
        model.addAttribute("responses",mappedResponses);

        if(form == null){
            form = new Form();
        }
        Map<String, String> values = form.getFormInput();
        mappedResponses.forEach((k, v) ->
                        values.put(k.toString(), v.getValue())
        );
        form.setFormInput(values);
        model.addAttribute(FORM_MODEL_ATTRIBUTE, form);
    }

    protected List<ResponseResource> getResponses(ApplicationResource application) {
        return responseService.getByApplication(application.getId());
    }

    protected List<FormInputResponseResource> getFormInputResponses(ApplicationResource application) {
        return formInputResponseService.getByApplication(application.getId());
    }

    protected void addUserDetails(Model model, ApplicationResource application, Long userId) {
        Boolean userIsLeadApplicant = userService.isLeadApplicant(userId, application);
        model.addAttribute("userIsLeadApplicant", userIsLeadApplicant);
        model.addAttribute("leadApplicant", userService.getLeadApplicantProcessRoleOrNull(application));
    }

    protected Future<Set<Long>> getMarkedAsCompleteDetails(ApplicationResource application, Optional<OrganisationResource> userOrganisation) {
        Long organisationId=0L;
        if(userOrganisation.isPresent()) {
            organisationId = userOrganisation.get().getId();
        }
        return questionService.getMarkedAsComplete(application.getId(), organisationId);
    }

    protected void addAssignableDetails(Model model, ApplicationResource application, OrganisationResource userOrganisation,
                                         Long userId, Optional<SectionResource> currentSection, Optional<Long> currentQuestionId) {

        if (isApplicationInViewMode(model, application, userOrganisation))
            return;

        Map<Long, QuestionStatusResource> questionAssignees;
        if(currentQuestionId.isPresent()){
            QuestionStatusResource questionStatusResource = questionService.getByQuestionIdAndApplicationIdAndOrganisationId(currentQuestionId.get(), application.getId(), userOrganisation.getId());
            questionAssignees = new HashMap<>();
            if(questionStatusResource != null) {
                questionAssignees.put(currentQuestionId.get(), questionStatusResource);
            }
        }else if(currentSection.isPresent()){
            SectionResource section = currentSection.get();
            questionAssignees = questionService.getQuestionStatusesByQuestionIdsAndApplicationIdAndOrganisationId(section.getQuestions(), application.getId(), userOrganisation.getId());
        }else{
            questionAssignees = questionService.getQuestionStatusesForApplicationAndOrganisation(application.getId(), userOrganisation.getId());
        }

        if(currentQuestionId.isPresent()) {
            QuestionStatusResource questionAssignee = questionAssignees.get(currentQuestionId.get());
            model.addAttribute("questionAssignee", questionAssignee);
        }

        List<QuestionStatusResource> notifications = questionService.getNotificationsForUser(questionAssignees.values(), userId);
        questionService.removeNotifications(notifications);

        List<InviteResource> pendingAssignableUsers = pendingInvitations(application);
        
        model.addAttribute("assignableUsers", processRoleService.findAssignableProcessRoles(application.getId()));
        model.addAttribute("pendingAssignableUsers", pendingAssignableUsers);
        model.addAttribute("questionAssignees", questionAssignees);
        model.addAttribute("notifications", notifications);
    }

    private boolean isApplicationInViewMode(Model model, ApplicationResource application, OrganisationResource userOrganisation) {
        if(!application.isOpen() || userOrganisation == null){
            //Application Not open, so add empty lists
            model.addAttribute("assignableUsers", new ArrayList<ProcessRole>());
            model.addAttribute("pendingAssignableUsers", new ArrayList<InviteResource>());
            model.addAttribute("questionAssignees", new HashMap<Long, QuestionStatusResource>());
            model.addAttribute("notifications", new ArrayList<QuestionStatusResource>());
            return true;
        }
        return false;
    }

    private List<InviteResource> pendingInvitations(ApplicationResource application) {
        RestResult<List<InviteOrganisationResource>> pendingAssignableUsersResult = inviteRestService.getInvitesByApplication(application.getId());

        return pendingAssignableUsersResult.handleSuccessOrFailure(
        		failure -> new ArrayList<>(0),
        		success -> success.stream().flatMap(item -> item.getInviteResources().stream())
                    .filter(item -> !InviteStatusConstants.ACCEPTED.equals(item.getStatus()))
                    .collect(Collectors.toList()));
	}

    protected void addMappedSectionsDetails(Model model, ApplicationResource application, CompetitionResource competition,
                                            Optional<SectionResource> currentSection,
                                            Optional<OrganisationResource> userOrganisation) {
        List<SectionResource> allSections = sectionService.getAllByCompetitionId(competition.getId());
        List<SectionResource> parentSections = sectionService.filterParentSections(allSections);

        Map<Long, SectionResource> sections =
                parentSections.stream().collect(Collectors.toMap(SectionResource::getId,
                        Function.identity()));

        userOrganisation.ifPresent(org -> model.addAttribute("completedSections", sectionService.getCompleted(application.getId(), org.getId())));

        List<QuestionResource> questions = questionService.findByCompetition(competition.getId());

        List<FormInputResource> formInputResources = formInputService.findByCompetitionId(competition.getId());

        model.addAttribute("sections", sections);
        Map<Long, List<QuestionResource>> sectionQuestions = parentSections.stream()
                .collect(Collectors.toMap(
                        SectionResource::getId,
                        s -> getQuestionsBySection(s.getQuestions(), questions)
                ));
        Map<Long, List<FormInputResource>> questionFormInputs = sectionQuestions.values().stream()
            .flatMap(a -> a.stream())
            .collect(Collectors.toMap(q -> q.getId(), k -> findFormInputByQuestion(k.getId(), formInputResources)));
        model.addAttribute("questionFormInputs", questionFormInputs);
        model.addAttribute("sectionQuestions", sectionQuestions);

        Map<Long, List<QuestionResource>> subsectionQuestions = new HashMap<>();
        if(currentSection.isPresent()){
            Map<Long, List<SectionResource>>  subSections = new HashMap<>();
            subSections.put(currentSection.get().getId(), getSectionsFromListByIdList(currentSection.get().getChildSections(), allSections));

            model.addAttribute("subSections", subSections);
            subsectionQuestions = subSections.get(currentSection.get().getId()).stream()
                    .collect(Collectors.toMap(SectionResource::getId,
                            ss -> getQuestionsBySection(ss.getQuestions(), questions)
                    ));
            model.addAttribute("subsectionQuestions", subsectionQuestions);
        }else{
            Map<Long, List<SectionResource>>   subSections = parentSections.stream()
                    .collect(Collectors.toMap(
                            SectionResource::getId, s -> getSectionsFromListByIdList(s.getChildSections(), allSections)
                    ));
            model.addAttribute("subSections", subSections);
            subsectionQuestions = parentSections.stream()
                    .collect(Collectors.toMap(SectionResource::getId,
                            ss -> getQuestionsBySection(ss.getQuestions(), questions)
                    ));
            model.addAttribute("subsectionQuestions", subsectionQuestions);
        }

        Map<Long, List<FormInputResource>> subSectionQuestionFormInputs = subsectionQuestions.values().stream().flatMap(a -> a.stream()).collect(Collectors.toMap(q -> q.getId(), k -> findFormInputByQuestion(k.getId(), formInputResources)));
        model.addAttribute("subSectionQuestionFormInputs", subSectionQuestionFormInputs);
    }

    private List<SectionResource> getSectionsFromListByIdList(final List<Long> childSections, final List<SectionResource> allSections) {
        return simpleFilter(allSections, section -> childSections.contains(section.getId()));
    }

    private List<FormInputResource> findFormInputByQuestion(final Long id, final List<FormInputResource> list) {
        return simpleFilter(list, input -> input.getId().equals(id));
    }

    private List<QuestionResource> getQuestionsBySection(final List<Long> questionIds, final List<QuestionResource> questions) {
        return simpleFilter(questions, q -> questionIds.contains(q.getId()));
    }

    private void addCompletedDetails(Model model, ApplicationResource application, Optional<OrganisationResource> userOrganisation, List<ProcessRoleResource> userApplicationRoles) {
        Future<Set<Long>> markedAsComplete = getMarkedAsCompleteDetails(application, userOrganisation); // List of question ids
        model.addAttribute("markedAsComplete", markedAsComplete);

        SortedSet<OrganisationResource> organisations = getApplicationOrganisations(userApplicationRoles);
        Set<Long> questionsCompletedByAllOrganisation = new TreeSet<>(call(getMarkedAsCompleteDetails(application, Optional.ofNullable(organisations.first()))));
        // only keep the questionIDs of questions that are complete by all organisations
        organisations.forEach(o -> questionsCompletedByAllOrganisation.retainAll(call(getMarkedAsCompleteDetails(application, Optional.ofNullable(o)))));
        model.addAttribute("questionsCompletedByAllOrganisation", questionsCompletedByAllOrganisation);

        Map<Long, Set<Long>> completedSectionsByOrganisation = sectionService.getCompletedSectionsByOrganisation(application.getId());
        Set<Long> sectionsMarkedAsComplete = new TreeSet<>(completedSectionsByOrganisation.get(completedSectionsByOrganisation.keySet().stream().findFirst().get()));
        completedSectionsByOrganisation.forEach((key, values) -> sectionsMarkedAsComplete.retainAll(values));

        model.addAttribute("completedSectionsByOrganisation", completedSectionsByOrganisation);
        model.addAttribute("sectionsMarkedAsComplete", sectionsMarkedAsComplete);
        model.addAttribute("allQuestionsCompleted", sectionService.allSectionsMarkedAsComplete(application.getId()));
        
        List<SectionResource> financeSections = sectionService.getSectionsForCompetitionByType(application.getCompetition(), SectionType.FINANCE);
        boolean hasFinanceSection;
        Long financeSectionId;
        if(financeSections.isEmpty()) {
        	hasFinanceSection = false;
        	financeSectionId = null;
        } else {
        	hasFinanceSection = true;
        	financeSectionId = financeSections.get(0).getId();
        }
        
        model.addAttribute("hasFinanceSection", hasFinanceSection);
        model.addAttribute("financeSectionId", financeSectionId);
        
        List<SectionResource> eachOrganisationFinanceSections = sectionService.getSectionsForCompetitionByType(application.getCompetition(), SectionType.ORGANISATION_FINANCES);
        Long eachCollaboratorFinanceSectionId;
        if(eachOrganisationFinanceSections.isEmpty()) {
        	eachCollaboratorFinanceSectionId = null;
        } else {
        	eachCollaboratorFinanceSectionId = eachOrganisationFinanceSections.get(0).getId();
        }        
        model.addAttribute("eachCollaboratorFinanceSectionId", eachCollaboratorFinanceSectionId);
    }

    protected void addSectionDetails(Model model, Optional<SectionResource> currentSection) {
        model.addAttribute("currentSectionId", currentSection.map(SectionResource::getId).orElse(null));
        model.addAttribute("currentSection", currentSection.orElse(null));
        if(currentSection.isPresent()) {
            List<QuestionResource> questions = getQuestionsBySection(currentSection.get().getQuestions(), questionService.findByCompetition(currentSection.get().getCompetition()));
            Map<Long, List<QuestionResource>> sectionQuestions = new HashMap<>();
            sectionQuestions.put(currentSection.get().getId(), questions);
            Map<Long, List<FormInputResource>> questionFormInputs = sectionQuestions.values().stream().flatMap(a -> a.stream()).collect(Collectors.toMap(q -> q.getId(), k -> formInputService.findByQuestion(k.getId())));

            model.addAttribute("questionFormInputs", questionFormInputs);
            model.addAttribute("sectionQuestions", sectionQuestions);
            model.addAttribute("title", currentSection.get().getName());
        }
    }

    protected Optional<SectionResource> getSectionByIds(Long competitionId, List<Long> sections, Optional<Long> sectionId, boolean selectFirstSectionIfNoneCurrentlySelected) {
        List<SectionResource> allSections = sectionService.getAllByCompetitionId(competitionId);
        return getSection(allSections, sectionId, selectFirstSectionIfNoneCurrentlySelected);
    }

    protected Optional<SectionResource> getSection(List<SectionResource> sections, Optional<Long> sectionId, boolean selectFirstSectionIfNoneCurrentlySelected) {

        if (sectionId.isPresent()) {
            Long id = sectionId.get();

            // get the section that we want to show, so we can use this on to show the correct questions.
            return sections.stream().filter(x -> x.getId().equals(id)).findFirst();

        } else if (selectFirstSectionIfNoneCurrentlySelected) {
            return sections.isEmpty() ? Optional.empty() : Optional.ofNullable(sections.get(0));
        }

        return Optional.empty();
    }

    protected ApplicationResource addApplicationAndSections(ApplicationResource application,
                                                                             CompetitionResource competition,
                                                                             Long userId,
                                                                             Optional<SectionResource> section,
                                                                             Optional<Long> currentQuestionId,
                                                                             Model model,
                                                                             ApplicationForm form) {

        List<ProcessRoleResource> userApplicationRoles = processRoleService.findProcessRolesByApplicationId(application.getId());

        application = addApplicationDetails(application, competition, userId, section, currentQuestionId, model, form, userApplicationRoles);
        
        model.addAttribute("completedQuestionsPercentage", applicationService.getCompleteQuestionsPercentage(application.getId()));
        addSectionDetails(model, section);

        return application;
    }

    protected void addOrganisationAndUserFinanceDetails(Long competitionId, Long applicationId, UserResource user,
                                                        Model model, ApplicationForm form) {
        model.addAttribute("currentUser", user);
        
        List<SectionResource> financeSections = sectionService.getSectionsForCompetitionByType(competitionId, SectionType.FINANCE);
        boolean hasFinanceSection = !financeSections.isEmpty();
        
        if(hasFinanceSection) {
	        financeOverviewModelManager.addFinanceDetails(model, competitionId, applicationId);
	        if(!form.isAdminMode()){
	            String organisationType = organisationService.getOrganisationType(user.getId(), applicationId);
	            financeHandler.getFinanceModelManager(organisationType).addOrganisationFinanceDetails(model, applicationId, user.getId(), form);
	        }
        }
    }

    public SortedSet<OrganisationResource> getApplicationOrganisations(List<ProcessRoleResource> userApplicationRoles) {
        Comparator<OrganisationResource> compareById =
                Comparator.comparingLong(OrganisationResource::getId);
        Supplier<SortedSet<OrganisationResource>> supplier = () -> new TreeSet<>(compareById);

        return userApplicationRoles.stream()
                .filter(uar -> uar.getRoleName().equals(UserApplicationRole.LEAD_APPLICANT.getRoleName())
                            || uar.getRoleName().equals(UserApplicationRole.COLLABORATOR.getRoleName()))
                .map(uar -> organisationRestService.getOrganisationById(uar.getOrganisation()).getSuccessObjectOrThrowException())
                .collect(Collectors.toCollection(supplier));
    }

    public SortedSet<OrganisationResource> getAcademicOrganisations(SortedSet<OrganisationResource> organisations) {
        Comparator<OrganisationResource> compareById =
                Comparator.comparingLong(OrganisationResource::getId);
        Supplier<TreeSet<OrganisationResource>> supplier = () -> new TreeSet<>(compareById);
        ArrayList<OrganisationResource> organisationList = new ArrayList<>(organisations);

        return organisationList.stream()
                .filter(o -> OrganisationTypeEnum.ACADEMIC.getOrganisationTypeId().equals(o.getOrganisationType()))
                .collect(Collectors.toCollection(supplier));
    }

    public Optional<OrganisationResource> getApplicationLeadOrganisation(List<ProcessRoleResource> userApplicationRoles) {

        return userApplicationRoles.stream()
                .filter(uar -> uar.getRoleName().equals(UserApplicationRole.LEAD_APPLICANT.getRoleName()))
                .map(uar -> organisationRestService.getOrganisationById(uar.getOrganisation()).getSuccessObjectOrThrowException())
                .findFirst();
    }

    public Optional<OrganisationResource> getUserOrganisation(Long userId, List<ProcessRoleResource> userApplicationRoles) {

        return userApplicationRoles.stream()
                .filter(uar -> uar.getUser().getId().equals(userId))
                .map(uar -> organisationRestService.getOrganisationById(uar.getOrganisation()).getSuccessObjectOrThrowException())
                .findFirst();
    }

    public UserAuthenticationService getUserAuthenticationService() {
        return userAuthenticationService;
    }
}
