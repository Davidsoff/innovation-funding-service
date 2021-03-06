package org.innovateuk.ifs.competitionsetup.service.sectionupdaters.application;

import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection;
import org.innovateuk.ifs.competition.resource.GuidanceRowResource;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.form.application.AbstractApplicationQuestionForm;
import org.innovateuk.ifs.competitionsetup.form.application.ApplicationQuestionForm;
import org.innovateuk.ifs.competitionsetup.service.sectionupdaters.CompetitionSetupSubsectionSaver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;

/**
 * Competition setup section saver for the application form section.
 */
@Service
public class ApplicationQuestionSectionSaver extends AbstractApplicationSectionSaver implements CompetitionSetupSubsectionSaver {

	@Override
	public CompetitionSetupSubsection subsectionToSave() {
		return CompetitionSetupSubsection.QUESTIONS;
	}

    @Override
	public boolean supportsForm(Class<? extends CompetitionSetupForm> clazz) {
		return ApplicationQuestionForm.class.equals(clazz);
	}

    @Override
    protected void mapGuidanceRows(AbstractApplicationQuestionForm abstractApplicationQuestionForm) {
        ApplicationQuestionForm form = (ApplicationQuestionForm) abstractApplicationQuestionForm;
        form.getQuestion().setGuidanceRows(new ArrayList<>());
        form.getGuidanceRows().forEach(guidanceRow -> {
            GuidanceRowResource guidanceRowResource = new GuidanceRowResource();
            guidanceRowResource.setJustification(guidanceRow.getJustification());
            guidanceRowResource.setSubject(guidanceRow.getScoreFrom() + "," + guidanceRow.getScoreTo());
            form.getQuestion().getGuidanceRows().add(guidanceRowResource);
        });
    }

    @Override
    protected ServiceResult<Void> autoSaveGuidanceRowSubject(GuidanceRowResource guidanceRow, String fieldName, String value) {
        if (fieldName.endsWith("scoreFrom")) {
            guidanceRow.setSubject(modifySubject(guidanceRow.getSubject(), value, null));
            return serviceSuccess();
        } else if (fieldName.endsWith("scoreTo")) {
            guidanceRow.setSubject(modifySubject(guidanceRow.getSubject(), null, value));
            return serviceSuccess();
        } else {
            return serviceFailure(new Error("Field not found", HttpStatus.BAD_REQUEST));
        }
    }

    private String modifySubject(String subject, String value1, String value2) {
        // Initialise subject for newly created guidance rows as will be null
        if (subject == null) {
            subject = "";
        }
        String[] splitSubject = subject.split(",");
        if (value2 == null) {
            return value1 + "," + (splitSubject.length > 1 ? splitSubject[1] : 0);
        } else {
            return (splitSubject.length > 1 ? splitSubject[0] : 0) + "," + value2;
        }
    }

}
