package org.innovateuk.ifs.competition.transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.category.domain.Category;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.domain.CompetitionType;
import org.innovateuk.ifs.competition.mapper.CompetitionMapper;
import org.innovateuk.ifs.competition.repository.CompetitionRepository;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.project.repository.ProjectRepository;
import org.innovateuk.ifs.publiccontent.transactional.PublicContentService;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.innovateuk.ifs.user.domain.OrganisationType;
import org.innovateuk.ifs.user.mapper.OrganisationTypeMapper;
import org.innovateuk.ifs.user.resource.OrganisationTypeResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.COMPETITION_CANNOT_RELEASE_FEEDBACK;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.security.SecurityRuleUtil.isSupport;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

/**
 * Service for operations around the usage and processing of Competitions
 */
@Service
public class CompetitionServiceImpl extends BaseTransactionalService implements CompetitionService {

    private static final Log LOG = LogFactory.getLog(CompetitionServiceImpl.class);

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private OrganisationTypeMapper organisationTypeMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PublicContentService publicContentService;

    @Autowired
    private CompetitionKeyStatisticsService competitionKeyStatisticsService;

    @Override
    public ServiceResult<CompetitionResource> getCompetitionById(Long id) {
        return find(competitionRepository.findById(id), notFoundError(Competition.class, id)).andOnSuccess(comp -> serviceSuccess(competitionMapper.mapToResource(comp)));
    }

    @Override
    public ServiceResult<List<OrganisationTypeResource>> getCompetitionOrganisationTypes(long id) {
        return find(competitionRepository.findById(id), notFoundError(OrganisationType.class, id)).andOnSuccess(comp -> serviceSuccess((List) organisationTypeMapper.mapToResource(comp.getLeadApplicantTypes())));
    }

    @Override
    public ServiceResult<List<CompetitionResource>> findAll() {
        return serviceSuccess((List) competitionMapper.mapToResource(
                competitionRepository.findAll().stream().filter(comp -> !comp.isTemplate()).collect(Collectors.toList())
        ));
    }

    @Override
    public ServiceResult<List<CompetitionSearchResultItem>> findLiveCompetitions() {
        List<Competition> competitions = competitionRepository.findLive();
        return serviceSuccess(simpleMap(competitions, this::searchResultFromCompetition));
    }

    @Override
    public ServiceResult<List<CompetitionSearchResultItem>> findProjectSetupCompetitions() {
        List<Competition> competitions = competitionRepository.findProjectSetup();
        return serviceSuccess(simpleMap(competitions, this::searchResultFromCompetition));
    }

    @Override
    public ServiceResult<List<CompetitionSearchResultItem>> findUpcomingCompetitions() {
        List<Competition> competitions = competitionRepository.findUpcoming();
        return serviceSuccess(simpleMap(competitions, this::searchResultFromCompetition));
    }

    @Override
    public ServiceResult<List<CompetitionSearchResultItem>> findNonIfsCompetitions() {
        List<Competition> competitions = competitionRepository.findNonIfs();
        return serviceSuccess(simpleMap(competitions, this::searchResultFromCompetition));
    }

    @Override
    public ServiceResult<CompetitionSearchResult> searchCompetitions(String searchQuery, int page, int size) {
        String searchQueryLike = String.format("%%%s%%", searchQuery);
        PageRequest pageRequest = new PageRequest(page, size);
        Page<Competition> pageResult = competitionRepository.search(searchQueryLike, pageRequest);

        CompetitionSearchResult result = new CompetitionSearchResult();
        List<Competition> competitions = pageResult.getContent();
        result.setContent(simpleMap(competitions, this::searchResultFromCompetition));
        result.setNumber(pageRequest.getPageNumber());
        result.setSize(size);
        result.setTotalElements(pageResult.getTotalElements());
        result.setTotalPages(pageResult.getTotalPages());

        return serviceSuccess(result);
    }

    private CompetitionSearchResultItem searchResultFromCompetition(Competition c) {
        return getCurrentlyLoggedInUser().andOnSuccess(currentUser -> serviceSuccess(new CompetitionSearchResultItem(c.getId(),
                c.getName(),
                ofNullable(c.getInnovationAreas()).orElseGet(Collections::emptySet)
                        .stream()
                        .map(Category::getName)
                        .collect(Collectors.toCollection(TreeSet::new)),
                c.getApplications().size(),
                c.startDateDisplay(),
                c.getCompetitionStatus(),
                ofNullable(c.getCompetitionType()).map(CompetitionType::getName).orElse(null),
                projectRepository.findByApplicationCompetitionId(c.getId()).size(),
                publicContentService.findByCompetitionId(c.getId()).getSuccessObjectOrThrowException().getPublishDate(),
                isSupport(currentUser) ? "/competition/" + c.getId() + "/applications/all" : "/competition/" + c.getId()
        ))).getSuccessObjectOrThrowException();
    }

    @Override
    public ServiceResult<CompetitionCountResource> countCompetitions() {
        //TODO INFUND-3833 populate complete count
        return serviceSuccess(new CompetitionCountResource(competitionRepository.countLive(), competitionRepository.countProjectSetup(),
                competitionRepository.countUpcoming(), 0L, competitionRepository.countNonIfs()));
    }

    @Override
    @Transactional
    public ServiceResult<Void> closeAssessment(long competitionId) {
        Competition competition = competitionRepository.findById(competitionId);
        competition.closeAssessment(ZonedDateTime.now());
        return serviceSuccess();
    }

    @Override
    @Transactional
    public ServiceResult<Void> notifyAssessors(long competitionId) {
        Competition competition = competitionRepository.findById(competitionId);
        competition.notifyAssessors(ZonedDateTime.now());
        return serviceSuccess();
    }

    @Override
    @Transactional
    public ServiceResult<Void> releaseFeedback(long competitionId) {
        CompetitionFundedKeyStatisticsResource keyStatisticsResource =
                competitionKeyStatisticsService.getFundedKeyStatisticsByCompetition(competitionId)
                        .getSuccessObjectOrThrowException();
        if (keyStatisticsResource.isCanReleaseFeedback()) {
            Competition competition = competitionRepository.findById(competitionId);
            competition.releaseFeedback(ZonedDateTime.now());
            return serviceSuccess();
        } else {
            return serviceFailure(new Error(COMPETITION_CANNOT_RELEASE_FEEDBACK));
        }
    }

    @Override
    @Transactional
    public ServiceResult<Void> manageInformState(long competitionId) {
        CompetitionFundedKeyStatisticsResource keyStatisticsResource =
                competitionKeyStatisticsService.getFundedKeyStatisticsByCompetition(competitionId)
                .getSuccessObjectOrThrowException();
        if (keyStatisticsResource.isCanReleaseFeedback()) {
            Competition competition = competitionRepository.findById(competitionId);
            competition.setFundersPanelEndDate(ZonedDateTime.now());
        }
        return serviceSuccess();
    }
}
