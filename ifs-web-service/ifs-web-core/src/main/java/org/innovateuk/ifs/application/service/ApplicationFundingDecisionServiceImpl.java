package org.innovateuk.ifs.application.service;

import org.apache.catalina.util.ParameterMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.resource.FundingNotificationResource;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;

@Service
public class ApplicationFundingDecisionServiceImpl implements ApplicationFundingDecisionService {

	private static final Log LOG = LogFactory.getLog(ApplicationFundingDecisionServiceImpl.class);

	@Autowired
	private ApplicationFundingDecisionRestService applicationFundingDecisionRestService;

	@Autowired
	private ApplicationSummaryRestService applicationSummaryRestService;

	@Override
	public ServiceResult<Void> sendFundingNotifications(FundingNotificationResource fundingNotificationResource) {
		return applicationFundingDecisionRestService.sendApplicationFundingDecisions(fundingNotificationResource).toServiceResult();
	}

	@Override
	public ServiceResult<Void> saveApplicationFundingDecisionData(Long competitionId, FundingDecision fundingDecision, List<Long> applicationIds) {

		if(isAllowedFundingDecision(fundingDecision)) {
			Map<Long, FundingDecision> applicationIdToFundingDecision = createSubmittedApplicationFundingDecisionMap(applicationIds, competitionId, fundingDecision);
			applicationFundingDecisionRestService.saveApplicationFundingDecisionData(competitionId, applicationIdToFundingDecision).getSuccessObjectOrThrowException();
		}
		else {
			return serviceFailure(new Error("Disallowed funding decision submitted", HttpStatus.BAD_REQUEST));
		}

		return serviceSuccess();
	}

	public boolean isAllowedFundingDecision(FundingDecision fundingDecision) {
		if(fundingDecision.equals(FundingDecision.UNDECIDED)) {
			return false;
		}
		else {
			return true;
		}
	}

	public Optional<FundingDecision> getFundingDecisionForString(String val) {
		Optional<FundingDecision> fundingDecision = Optional.empty();

		try {
			fundingDecision = Optional.of(FundingDecision.valueOf(val));
		}
		catch(IllegalArgumentException e) {
			LOG.info("Funding decision string disallowed");
		}

		return fundingDecision;
	}

	private List<Long> submittedApplicationIdsForCompetition(Long competitionId) {
		return applicationSummaryRestService.getSubmittedApplications(competitionId, null, 0, Integer.MAX_VALUE, Optional.empty(), Optional.empty()).getSuccessObjectOrThrowException().getContent()
				.stream().map(summaryResource -> summaryResource.getId()).collect(Collectors.toList());
	}

	private Map<Long, FundingDecision> createSubmittedApplicationFundingDecisionMap(List<Long> applicationIds, Long competitionId, FundingDecision fundingDecision) {

		return filteredListOfFundingDecisions(applicationIds, competitionId, fundingDecision);
	}

	private Map<Long, FundingDecision> filteredListOfFundingDecisions(List<Long> applicationIds, Long competitionId, FundingDecision fundingDecision) {
		Map<Long, FundingDecision> applicationIdToFundingDecision = new ParameterMap<>();

		List<Long> ids = submittedApplicationIdsForCompetition(competitionId);
		applicationIds.stream()
				.filter(id -> ids.contains(id))
				.forEach(id -> applicationIdToFundingDecision.put(id, fundingDecision));

		return applicationIdToFundingDecision;
	}
}
