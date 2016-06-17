package com.worth.ifs.competition.transactional;

import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.competition.transactional.CompetitionServiceImpl.COMPETITION_CLASS_NAME;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.worth.ifs.category.repository.CategoryLinkRepository;
import com.worth.ifs.category.repository.CategoryRepository;
import com.worth.ifs.category.resource.CategoryType;
import com.worth.ifs.category.transactional.CategoryLinkService;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.competition.mapper.CompetitionMapper;
import com.worth.ifs.competition.mapper.CompetitionTypeMapper;
import com.worth.ifs.competition.repository.CompetitionRepository;
import com.worth.ifs.competition.repository.CompetitionTypeRepository;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionResource.Status;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import com.worth.ifs.competition.resource.CompetitionTypeResource;
import com.worth.ifs.transactional.BaseTransactionalService;

/**
 * Service for operations around the usage and processing of Competitions
 */
@Service
public class CompetitionSetupServiceImpl extends BaseTransactionalService implements CompetitionSetupService {
    private static final Log LOG = LogFactory.getLog(CompetitionSetupServiceImpl.class);
    @Autowired
    private CategoryLinkRepository categoryLinkRepository;
    @Autowired
    private CompetitionRepository competitionRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryLinkService categoryLinkService;
    @Autowired
    private CompetitionService competitionService;
    @Autowired
    private CompetitionMapper competitionMapper;
    @Autowired
    private CompetitionTypeMapper competitionTypeMapper;
    @Autowired
    private CompetitionTypeRepository competitionTypeRepository;


    @Override
    public ServiceResult<String> generateCompetitionCode(Long id, LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYMM");
        Competition competition = competitionRepository.findById(id);
        String datePart = formatter.format(dateTime);
        List<Competition> openingSameMonth = competitionRepository.findByCodeLike("%"+datePart+"%");
        if(StringUtils.hasText(competition.getCode())){
            return serviceSuccess(competition.getCode());
        }else if(openingSameMonth.isEmpty()){
            String unusedCode = datePart + "-1";
            competition.setCode(unusedCode);
            competitionRepository.save(competition);
            return serviceSuccess(unusedCode);
        }else{
            List<String> codes = openingSameMonth.stream().map(c -> c.getCode()).sorted().peek(c -> LOG.info("Codes : "+ c)).collect(Collectors.toList());
            String unusedCode = "";
            for (int i = 1; i < 10000; i++) {
                unusedCode = datePart+"-"+i;
                if(!codes.contains(unusedCode)){
                    break;
                }
            }
            competition.setCode(unusedCode);
            competitionRepository.save(competition);
            return serviceSuccess(unusedCode);
        }
    }

    @Override
    public ServiceResult<CompetitionResource> update(Long id, CompetitionResource competitionResource) {
        Competition competition = competitionMapper.mapToDomain(competitionResource);
        saveCategories(competitionResource);
        competition = competitionRepository.save(competition);
        competitionService.addCategories(competition);
        return serviceSuccess(competitionMapper.mapToResource(competition));
    }

    private void saveCategories(CompetitionResource competitionResource) {
        saveInnovationArea(competitionResource);
        saveInnovationSector(competitionResource);
    }

    private void saveInnovationSector(CompetitionResource competitionResource) {
        Long sectorId = competitionResource.getInnovationSector();
        saveCategoryLink(competitionResource, sectorId, CategoryType.INNOVATION_SECTOR);
    }

    private void saveInnovationArea(CompetitionResource competitionResource) {
        Long areaId = competitionResource.getInnovationArea();
        saveCategoryLink(competitionResource, areaId, CategoryType.INNOVATION_AREA);
    }

    private void saveCategoryLink(CompetitionResource competitionResource, Long categoryId, CategoryType categoryType) {
        categoryLinkService.updateCategoryLink(categoryId, categoryType, COMPETITION_CLASS_NAME, competitionResource.getId());
    }

    @Override
    public ServiceResult<CompetitionResource> create() {
        Competition competition = new Competition();
        competition.setStatus(Status.COMPETITION_SETUP);
        return serviceSuccess(competitionMapper.mapToResource(competitionRepository.save(competition)));
    }

    @Override
    public ServiceResult<Void> markSectionComplete(Long competitionId, CompetitionSetupSection section) {
    	
    	// TODO
    	// get competition, populate map accordingly
        return serviceSuccess();
    }

    @Override
    public ServiceResult<Void> markSectionInComplete(Long competitionId, CompetitionSetupSection section) {
    	// TODO
    	// get competition, populate map accordingly
        return serviceSuccess();
    }


    @Override
    public ServiceResult<List<CompetitionTypeResource>> findAllTypes() {
        return serviceSuccess((List) competitionTypeMapper.mapToResource(competitionTypeRepository.findAll()));
    }
}
