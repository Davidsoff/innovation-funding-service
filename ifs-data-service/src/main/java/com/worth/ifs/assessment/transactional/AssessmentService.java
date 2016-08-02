package com.worth.ifs.assessment.transactional;

import com.worth.ifs.assessment.resource.AssessmentResource;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.workflow.domain.ProcessOutcome;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Transactional and secured service providing operations around {@link com.worth.ifs.assessment.domain.Assessment} data.
 */
public interface AssessmentService {

    @PreAuthorize("hasPermission(#id, 'com.worth.ifs.assessment.domain.Assessment', 'READ')")
    ServiceResult<AssessmentResource> findById(@P("id") final Long id);

    @PreAuthorize("hasPermission(#assessmentId, 'com.worth.ifs.assessment.domain.Assessment', 'UPDATE')")
    ServiceResult<Void> updateStatus(@P("assessmentId") final Long assessmentId, final ProcessOutcome processOutcome);
}
