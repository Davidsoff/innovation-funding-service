package com.worth.ifs.sil.experian.controller;

import com.worth.ifs.BaseControllerMockMVCTest;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import static com.worth.ifs.sil.experian.controller.ExperianEndpointController.validationErrors;
import static com.worth.ifs.util.JsonMappingUtil.toJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests around the SIL email stub
 */
public class ExperianEndpointControllerMockMvcTest extends BaseControllerMockMVCTest<ExperianEndpointController> {

    @Override
    protected ExperianEndpointController supplyControllerUnderTest() {
        return new ExperianEndpointController();
    }

    @Test
    public void testExperianValidate() throws Exception {
        validationErrors.keySet().forEach(bankDetails -> {
            String requestBody = toJson(bankDetails);
            try {
                MvcResult result = mockMvc.
                        perform(
                                post("/silstub/experianValidate").
                                        header("Content-Type", "application/json").
                                        header("IFS_AUTH_TOKEN", "123abc").
                                        content(requestBody)
                        ).
                        andExpect(status().isOk()).
                        andReturn();
                result.getResponse();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}