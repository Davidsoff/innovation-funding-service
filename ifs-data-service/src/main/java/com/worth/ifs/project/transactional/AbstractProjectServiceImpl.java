package com.worth.ifs.project.transactional;

import com.worth.ifs.bankdetails.domain.BankDetails;
import com.worth.ifs.bankdetails.repository.BankDetailsRepository;
import com.worth.ifs.finance.transactional.FinanceRowService;
import com.worth.ifs.project.constant.ProjectActivityStates;
import com.worth.ifs.project.domain.MonitoringOfficer;
import com.worth.ifs.project.domain.Project;
import com.worth.ifs.project.domain.ProjectUser;
import com.worth.ifs.project.finance.domain.SpendProfile;
import com.worth.ifs.project.finance.repository.SpendProfileRepository;
import com.worth.ifs.project.mapper.ProjectMapper;
import com.worth.ifs.project.mapper.ProjectUserMapper;
import com.worth.ifs.project.repository.MonitoringOfficerRepository;
import com.worth.ifs.project.repository.ProjectRepository;
import com.worth.ifs.project.repository.ProjectUserRepository;
import com.worth.ifs.project.workflow.projectdetails.configuration.ProjectDetailsWorkflowHandler;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.user.domain.Organisation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static com.worth.ifs.project.constant.ProjectActivityStates.*;
import static com.worth.ifs.user.resource.OrganisationTypeEnum.isResearch;

class AbstractProjectServiceImpl extends BaseTransactionalService {
    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    ProjectUserRepository projectUserRepository;

    @Autowired
    ProjectUserMapper projectUserMapper;

    @Autowired
    MonitoringOfficerRepository monitoringOfficerRepository;

    @Autowired
    BankDetailsRepository bankDetailsRepository;

    @Autowired
    SpendProfileRepository spendProfileRepository;

    @Autowired
    FinanceRowService financeRowService;

    @Autowired
    private ProjectDetailsWorkflowHandler projectDetailsWorkflowHandler;

    List<ProjectUser> getProjectUsersByProjectId(Long projectId) {
        return projectUserRepository.findByProjectId(projectId);
    }

    ProjectActivityStates createOtherDocumentStatus(final Project project) {
        if (project.getCollaborationAgreement() != null && project.getExploitationPlan() != null) {
            return COMPLETE;
        } else {
            return ACTION_REQUIRED;
        }
    }

    ProjectActivityStates createGrantOfferLetterStatus() {
        //TODO update logic when GrantOfferLetter is implemented
        return NOT_STARTED;
    }

    ProjectActivityStates createProjectDetailsStatus(Project project) {
        return projectDetailsWorkflowHandler.isSubmitted(project) ? COMPLETE : ACTION_REQUIRED;
    }

    ProjectActivityStates createMonitoringOfficerStatus(final Optional<MonitoringOfficer> monitoringOfficer, final ProjectActivityStates leadProjectDetailsSubmitted) {
        if (leadProjectDetailsSubmitted.equals(COMPLETE)) {
            return monitoringOfficer.isPresent() ? COMPLETE : PENDING;
        } else {
            return NOT_STARTED;
        }

    }

    ProjectActivityStates createBankDetailStatus(final Project project, final Optional<BankDetails> bankDetails, final Organisation partnerOrganisation) {
        if (bankDetails.isPresent()) {
            return bankDetails.get().isApproved() ? COMPLETE : PENDING;
        } else {
            Boolean isSeekingFunding = financeRowService.organisationSeeksFunding(project.getId(), project.getApplication().getId(), partnerOrganisation.getId()).getSuccessObject();
            if (isResearch(partnerOrganisation.getOrganisationType().getId()) || !isSeekingFunding) {
                return NOT_REQUIRED;
            } else {
                return ACTION_REQUIRED;
            }
        }
    }

    ProjectActivityStates createFinanceCheckStatus(final ProjectActivityStates bankDetailsStatus) {
        if(bankDetailsStatus.equals(COMPLETE) || bankDetailsStatus.equals(PENDING) || bankDetailsStatus.equals(NOT_REQUIRED)){
            return ACTION_REQUIRED;
        } else {
            //TODO update logic when Finance checks are implemented
            return NOT_STARTED;
        }
    }

    ProjectActivityStates createSpendProfileStatus(final ProjectActivityStates financeCheckStatus, final Optional<SpendProfile> spendProfile) {
        //TODO - Implement REJECT status when internal spend profile action story is completed
        if (spendProfile.isPresent()) {
            if (spendProfile.get().isMarkedAsComplete()) {
                return COMPLETE;
            } else {
                return ACTION_REQUIRED;
            }
        } else {
            if(financeCheckStatus.equals(COMPLETE)){
                return PENDING;
            } else {
                return NOT_STARTED;
            }
        }
    }
}