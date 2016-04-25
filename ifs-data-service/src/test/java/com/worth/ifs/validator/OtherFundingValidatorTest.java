package com.worth.ifs.validator;

import com.worth.ifs.application.builder.QuestionBuilder;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.finance.builder.ApplicationFinanceBuilder;
import com.worth.ifs.finance.builder.CostBuilder;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.resource.cost.CostType;
import com.worth.ifs.finance.resource.cost.OtherFunding;
import org.junit.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.worth.ifs.finance.handler.item.OtherFundingHandler.COST_KEY;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class OtherFundingValidatorTest extends AbstractValidatorTest {

    @Override
    public Validator getValidator() {
        return new OtherFundingValidator(costRepositoryMock, questionServiceMock);
    }

    @Test
    @Override
    public void testInvalid() throws Exception {
        mockWithRadio("Yes");

        OtherFunding otherFunding = new OtherFunding(1L, "Yes", "", "", new BigDecimal(0));
        expectErrors(3, Arrays.asList("Invalid secured date.  Please use MM-YYYY format.", "Funding source cannot be blank", "Funding amount should be greater than 0"), otherFunding);

        otherFunding = new OtherFunding(2L, "Yes", "Source1", "", new BigDecimal(0));
        expectErrors(2, Arrays.asList("Invalid secured date.  Please use MM-YYYY format.", "Funding amount should be greater than 0"), otherFunding);

        otherFunding = new OtherFunding(3L, "Yes", "Source1", "2342", new BigDecimal(100));
        expectErrors(1, Collections.singletonList("Invalid secured date.  Please use MM-YYYY format."), otherFunding);

        otherFunding = new OtherFunding(4L, "Yes", "Source1", "15-2014", new BigDecimal(100));
        expectErrors(1, Collections.singletonList("Invalid secured date.  Please use MM-YYYY format."), otherFunding);

        otherFunding = new OtherFunding(5L, "Yes", "Source1", "2014", new BigDecimal(100));
        expectErrors(1, Collections.singletonList("Invalid secured date.  Please use MM-YYYY format."), otherFunding);

        otherFunding = new OtherFunding(5L, "Yes", "Source1", "12-2014hvhvh", new BigDecimal(100));
        expectErrors(1, Collections.singletonList("Invalid secured date.  Please use MM-YYYY format."), otherFunding);
    }

    @Test
    @Override
    public void testValid() throws Exception {
        mockWithRadio("Yes");
        OtherFunding otherFunding = new OtherFunding(1L, "Yes", "Source1", "10-2014", new BigDecimal(100));
        expectNoErrors(otherFunding);

        mockWithRadio("No");
        otherFunding = new OtherFunding(2L, "No", "", "ertt", new BigDecimal(0));
        expectNoErrors(otherFunding);

        otherFunding = new OtherFunding(6L, "Yes", "Source1", "2014", new BigDecimal(100));
        expectNoErrors(otherFunding);
    }

    private void expectErrors(int count, List<String> messages, OtherFunding otherFunding){
        BindingResult bindingResult = getBindingResult(otherFunding);
        getValidator().validate(otherFunding, bindingResult);
        assertTrue(bindingResult.hasErrors());
        assertEquals(count, bindingResult.getErrorCount());

        int i = 0;
        for(String message : messages){
            assertEquals(message, bindingResult.getAllErrors().get(i).getDefaultMessage());
            i++;
        }
    }

    private void expectNoErrors(OtherFunding otherFunding){
        BindingResult bindingResult = getBindingResult(otherFunding);
        getValidator().validate(otherFunding, bindingResult);
        assertFalse(bindingResult.hasErrors());
        assertEquals(0, bindingResult.getErrorCount());
    }

    private void mockWithRadio(String value){
        ApplicationFinance applicationFinance = ApplicationFinanceBuilder.newApplicationFinance().build();
        Cost cost = CostBuilder.newCost().withApplicationFinance(applicationFinance).withItem(value).build();
        Question question = QuestionBuilder.newQuestion().build();
        when(costRepositoryMock.findOne(any(Long.class))).thenReturn(cost);
        when(questionServiceMock.getQuestionByFormInputType(CostType.OTHER_FUNDING.getType())).thenReturn(ServiceResult.serviceSuccess(question));
        List<Cost> listOfCostWithYes = new ArrayList<>();
        listOfCostWithYes.add(cost);
        when(costRepositoryMock.findByApplicationFinanceIdAndNameAndQuestionId(anyLong(), eq(COST_KEY), anyLong())).thenReturn(listOfCostWithYes);
    }
}
