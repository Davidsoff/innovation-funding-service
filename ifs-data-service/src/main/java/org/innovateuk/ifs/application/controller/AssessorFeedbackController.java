package org.innovateuk.ifs.application.controller;

import org.innovateuk.ifs.application.transactional.AssessorFeedbackService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.file.transactional.FileHttpHeadersValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.innovateuk.ifs.file.controller.FileControllerUtils.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/assessorfeedback")
public class AssessorFeedbackController {

    @Autowired
    private AssessorFeedbackService assessorFeedbackService;

    @Autowired
    @Qualifier("assessorFeedbackFileValidator")
    private FileHttpHeadersValidator fileValidator;

    @RequestMapping(value = "/assessorFeedbackDocument", method = POST, produces = "application/json")
    public RestResult<FileEntryResource> addAssessorFeedbackDocument(
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Content-Length", required = false) String contentLength,
            @RequestParam(value = "applicationId") long applicationId,
            @RequestParam(value = "filename", required = false) String originalFilename,
            HttpServletRequest request) {

        return handleFileUpload(contentType, contentLength, originalFilename, fileValidator, request, (fileAttributes, inputStreamSupplier) ->
                assessorFeedbackService.createAssessorFeedbackFileEntry(applicationId, fileAttributes.toFileEntryResource(), inputStreamSupplier));
    }

    @RequestMapping(value = "/assessorFeedbackDocument", method = GET)
    public @ResponseBody
    ResponseEntity<Object> getFileContents(
            @RequestParam("applicationId") long applicationId) throws IOException {

        return handleFileDownload(() -> assessorFeedbackService.getAssessorFeedbackFileEntryContents(applicationId));
    }

    @RequestMapping(value = "/assessorFeedbackDocument/fileentry", method = GET, produces = "application/json")
    public RestResult<FileEntryResource> getFileEntryDetails(
            @RequestParam("applicationId") long applicationId) throws IOException {

        return assessorFeedbackService.getAssessorFeedbackFileEntryDetails(applicationId).toGetResponse();
    }


    @RequestMapping(value = "/assessorFeedbackDocument", method = PUT, produces = "application/json")
    public RestResult<Void> updateAssessorFeedbackDocument(
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Content-Length", required = false) String contentLength,
            @RequestParam(value = "applicationId") long applicationId,
            @RequestParam(value = "filename", required = false) String originalFilename,
            HttpServletRequest request) {

        return handleFileUpdate(contentType, contentLength, originalFilename, fileValidator, request, (fileAttributes, inputStreamSupplier) ->
                assessorFeedbackService.updateAssessorFeedbackFileEntry(applicationId, fileAttributes.toFileEntryResource(), inputStreamSupplier));
    }

    @RequestMapping(value = "/assessorFeedbackDocument", method = DELETE, produces = "application/json")
    public RestResult<Void> deleteAssessorFeedbackDocument(
            @RequestParam("applicationId") long applicationId) throws IOException {

        return assessorFeedbackService.deleteAssessorFeedbackFileEntry(applicationId).toDeleteResponse();
    }
    
    @RequestMapping(value = "/assessorFeedbackUploaded", method = GET, produces = "application/json")
    public RestResult<Boolean> assessorFeedbackUploaded(
            @RequestParam("competitionId") long competitionId) {

        ServiceResult<Boolean> uploadedResult = assessorFeedbackService.assessorFeedbackUploaded(competitionId);
        return uploadedResult.toGetResponse();
    }
    
    @RequestMapping(value = "/submitAssessorFeedback/{competitionId}", method = POST, produces = "application/json")
    public RestResult<Void> submitAssessorFeedback(
            @PathVariable("competitionId") long competitionId) {

        return assessorFeedbackService.submitAssessorFeedback(competitionId).andOnSuccess(() ->
               assessorFeedbackService.notifyLeadApplicantsOfAssessorFeedback(competitionId)).
               toPostResponse();
    }
}