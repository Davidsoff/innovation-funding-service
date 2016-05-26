package com.worth.ifs.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Mock;

import com.worth.ifs.application.service.AssessorFeedbackService;
import com.worth.ifs.filter.CookieFlashMessageFilter;

public class AssessorFeedbackControllerTest extends BaseControllerMockMVCTest<AssessorFeedbackController> {

	public static final Long COMPETITION_ID = Long.valueOf(123L);
    
	@Override
	protected AssessorFeedbackController supplyControllerUnderTest() {
		return new AssessorFeedbackController();
	}
	
    @Mock
    private AssessorFeedbackService assessorFeedbackService;
    
    @Mock
    private CookieFlashMessageFilter cookieFlashMessageFilter;
    
	@Test
    public void submitAssessorFeedbackFailVerification() throws Exception {
    	
		when(assessorFeedbackService.feedbackUploaded(COMPETITION_ID)).thenReturn(false);
		
    	mockMvc.perform(
    				post("/competition/" + COMPETITION_ID + "/assessorfeedbacksubmit")
    			)
                .andExpect(redirectedUrl("/competition/" + COMPETITION_ID));
    	
    	verify(assessorFeedbackService, never()).submitAssessorFeedback(any(Long.class));
    	verify(cookieFlashMessageFilter).setFlashMessage(isA(HttpServletResponse.class), eq("feedbackNotUploadedForAllApplications"));
    }
    
	@Test
    public void submitAssessorFeedback() throws Exception {
    	
    	when(assessorFeedbackService.feedbackUploaded(COMPETITION_ID)).thenReturn(true);
    	
		mockMvc.perform(
    				post("/competition/" + COMPETITION_ID + "/assessorfeedbacksubmit")
    			)
                .andExpect(redirectedUrl("/competition/" + COMPETITION_ID));
    	
    	verifyNoMoreInteractions(cookieFlashMessageFilter);
    	
    	verify(assessorFeedbackService).submitAssessorFeedback(COMPETITION_ID);
    }
    
	@Test
    public void assessorFeedbackFailVerification() throws Exception {
    	
		when(assessorFeedbackService.feedbackUploaded(COMPETITION_ID)).thenReturn(false);
		
    	mockMvc.perform(
    				post("/competition/" + COMPETITION_ID + "/assessorfeedback")
    			)
                .andExpect(redirectedUrl("/competition/" + COMPETITION_ID));
    	
    	verify(assessorFeedbackService, never()).submitAssessorFeedback(any(Long.class));
    	verify(cookieFlashMessageFilter).setFlashMessage(isA(HttpServletResponse.class), eq("feedbackNotUploadedForAllApplications"));
    }
    
	@Test
    public void assessorFeedback() throws Exception {
    	
    	when(assessorFeedbackService.feedbackUploaded(COMPETITION_ID)).thenReturn(true);
		
    	mockMvc.perform(
    				post("/competition/" + COMPETITION_ID + "/assessorfeedback")
    			)
    			.andExpect(view().name("assessor-feedback-confirmation"))
    			.andExpect(model().attribute("competitionId", 123L));
    	verify(assessorFeedbackService, never()).submitAssessorFeedback(any(Long.class));
    	verifyNoMoreInteractions(cookieFlashMessageFilter);
    }
    
}