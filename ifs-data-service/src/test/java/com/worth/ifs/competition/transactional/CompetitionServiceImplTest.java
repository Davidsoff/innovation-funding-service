package com.worth.ifs.competition.transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.worth.ifs.BaseServiceUnitTest;
import com.worth.ifs.category.domain.Category;
import com.worth.ifs.category.repository.CategoryRepository;
import com.worth.ifs.category.resource.CategoryType;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.competition.mapper.CompetitionMapper;
import com.worth.ifs.competition.repository.CompetitionRepository;
import com.worth.ifs.competition.resource.CompetitionCountResource;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSearchResult;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.worth.ifs.competition.transactional.CompetitionServiceImpl.COMPETITION_CLASS_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the CompetitionServiceImpl with mocked repositories/mappers.
 */
public class CompetitionServiceImplTest extends BaseServiceUnitTest<CompetitionServiceImpl> {
    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CompetitionMapper competitionMapper;


    @Override
    protected CompetitionServiceImpl supplyServiceUnderTest() {
        return new CompetitionServiceImpl();
    }

    @Test
    public void test_getCompetitionById() throws Exception {
        Long competitionId = 1L;
        Competition competition = new Competition();
        CompetitionResource resource = new CompetitionResource();
        when(competitionRepository.findById(competitionId)).thenReturn(competition);
        when(competitionMapper.mapToResource(competition)).thenReturn(resource);

        CompetitionResource response = service.getCompetitionById(competitionId).getSuccessObjectOrThrowException();

        assertEquals(resource, response);
    }

    @Test
    public void test_addCategories() throws Exception {
        Long competitionId = 1L;
        Competition competition = new Competition();
        competition.setId(competitionId);
        Category innovationSector = new Category();
        Set<Category> researchCategories = Sets.newHashSet(new Category());
        Category innovationArea = new Category();
        when(categoryRepository.findByTypeAndCategoryLinks_ClassNameAndCategoryLinks_ClassPk(CategoryType.INNOVATION_SECTOR, COMPETITION_CLASS_NAME, competition.getId())).thenReturn(innovationSector);
        when(categoryRepository.findByTypeAndCategoryLinks_ClassNameAndCategoryLinks_ClassPk(CategoryType.INNOVATION_AREA, COMPETITION_CLASS_NAME, competition.getId())).thenReturn(innovationArea);
        when(categoryRepository.findAllByTypeAndCategoryLinks_ClassNameAndCategoryLinks_ClassPk(CategoryType.RESEARCH_CATEGORY, COMPETITION_CLASS_NAME, competition.getId())).thenReturn(researchCategories);

        Competition compResp = service.addCategories(competition);

        assertEquals(competition, compResp);
        assertEquals(competition.getInnovationArea(), innovationArea);
        assertEquals(competition.getInnovationSector(), innovationSector);
        assertEquals(competition.getResearchCategories(), researchCategories);
    }

    @Test
    public void test_findAll() throws Exception {
        List<Competition> competitions = Lists.newArrayList(new Competition());
        List<CompetitionResource> resources = Lists.newArrayList(new CompetitionResource());
        when(competitionRepository.findAll()).thenReturn(competitions);
        when(competitionMapper.mapToResource(competitions)).thenReturn(resources);

        List<CompetitionResource> response = service.findAll().getSuccessObjectOrThrowException();

        assertEquals(resources, response);
    }

    @Test
    public void test_findLiveCompetitions() throws Exception {
        List<Competition> competitions = Lists.newArrayList(new Competition());
        List<CompetitionResource> resources = Lists.newArrayList(new CompetitionResource());
        when(competitionRepository.findLive()).thenReturn(competitions);
        when(competitionMapper.mapToResource(competitions)).thenReturn(resources);

        List<CompetitionResource> response = service.findLiveCompetitions().getSuccessObjectOrThrowException();

        assertEquals(resources, response);
    }
    @Test
    public void test_findProjectSetupCompetitions() throws Exception {
        List<Competition> competitions = Lists.newArrayList(new Competition());
        List<CompetitionResource> resources = Lists.newArrayList(new CompetitionResource());
        when(competitionRepository.findProjectSetup()).thenReturn(competitions);
        when(competitionMapper.mapToResource(competitions)).thenReturn(resources);

        List<CompetitionResource> response = service.findProjectSetupCompetitions().getSuccessObjectOrThrowException();

        assertEquals(resources, response);
    }

    @Test
    public void test_findUpcomingCompetitions() throws Exception {
        List<Competition> competitions = Lists.newArrayList(new Competition());
        List<CompetitionResource> resources = Lists.newArrayList(new CompetitionResource());
        when(competitionRepository.findUpcoming()).thenReturn(competitions);
        when(competitionMapper.mapToResource(competitions)).thenReturn(resources);

        List<CompetitionResource> response = service.findUpcomingCompetitions().getSuccessObjectOrThrowException();

        assertEquals(resources, response);
    }

    @Test
    public void test_countCompetitions() throws Exception {
        Long countLive = 1L;
        Long countProjectSetup = 2L;
        Long countUpcoming = 3L;
        when(competitionRepository.countLive()).thenReturn(countLive);
        when(competitionRepository.countProjectSetup()).thenReturn(countProjectSetup);
        when(competitionRepository.countUpcoming()).thenReturn(countUpcoming);

        CompetitionCountResource response = service.countCompetitions().getSuccessObjectOrThrowException();

        assertEquals(countLive, response.getLiveCount());
        assertEquals(countProjectSetup, response.getProjectSetupCount());
        assertEquals(countUpcoming, response.getUpcomingCount());
    }

    @Test
    public void test_searchCompetitions() throws Exception {
        String searchQuery = "SearchQuery";
        String searchLike = "%" + searchQuery + "%";
        int page = 1;
        int size = 20;
        PageRequest pageRequest = new PageRequest(page, size);
        Page<Competition> queryResponse = mock(Page.class);
        long totalElements = 2L;
        int totalPages = 1;
        List<Competition> competitions = Lists.newArrayList(new Competition());
        List<CompetitionResource> resources = Lists.newArrayList(new CompetitionResource());
        when(queryResponse.getTotalElements()).thenReturn(totalElements);
        when(queryResponse.getTotalPages()).thenReturn(totalPages);
        when(queryResponse.getNumber()).thenReturn(page);
        when(queryResponse.getNumberOfElements()).thenReturn(size);
        when(queryResponse.getContent()).thenReturn(competitions);
        when(competitionRepository.search(searchLike, pageRequest)).thenReturn(queryResponse);
        when(competitionMapper.mapToResource(competitions)).thenReturn(resources);

        CompetitionSearchResult response = service.searchCompetitions(searchQuery, page, size).getSuccessObjectOrThrowException();

        assertEquals(totalElements, response.getTotalElements());
        assertEquals(totalPages, response.getTotalPages());
        assertEquals(page, response.getNumber());
        assertEquals(size, response.getSize());
        assertEquals(resources, response.getContent());
    }
}