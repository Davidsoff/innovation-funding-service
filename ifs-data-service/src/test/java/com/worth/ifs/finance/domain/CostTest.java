package com.worth.ifs.finance.domain;

import com.worth.ifs.application.domain.Question;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

public class CostTest {
    Cost costItem;
    Long id;
    String item;
    String description;
    Integer quantity;
    BigDecimal cost;
    ApplicationFinance applicationFinance;
    Question question;
    FinanceType financeType;

    @Before
    public void setUp() throws Exception {
        id = 0L;
        item = "cost item";
        description = "description of cost item";
        quantity = 10;
        cost = new BigDecimal(1000);
        applicationFinance = new ApplicationFinance();
        question = new Question();
        financeType = new FinanceType();
        costItem = new Cost(id, item, description, quantity, cost, applicationFinance, question);
    }

    @Test
    public void costShouldReturnCorrectAttributeValues() throws Exception {
        Assert.assertEquals(costItem.getId(), id);
        Assert.assertEquals(costItem.getItem(), item);
        Assert.assertEquals(costItem.getDescription(), description);
        Assert.assertEquals(costItem.getQuantity(), quantity);
        Assert.assertEquals(costItem.getCost(), cost);
        //TODO: mock getCostValues()
    }

    @Test
    public void constructorsShouldCreateInstancesOnValidInput() throws Exception {
        BigDecimal value = new BigDecimal(2.2);
        new Cost();
        new Cost("item","description",1,value,applicationFinance, question);
        new Cost(19274617892346L, "item2","description2",1,value,applicationFinance, question);
    }

    @Test
    public void costShouldReturnCorrectAttributeValuesAfterSetters() throws Exception {
        String item2 = "cost item";
        String description2 = "description of cost item";
        Integer quantity2 = 10;
        BigDecimal cost2 = new BigDecimal(2000);
        Question question2 = new Question();
        ApplicationFinance applicationFinance2 = new ApplicationFinance();

        costItem.setItem(item2);
        costItem.setDescription(description2);
        costItem.setQuantity(quantity2);
        costItem.setCost(cost2);
        costItem.setQuestion(question2);
        costItem.setApplicationFinance(applicationFinance2);

        Assert.assertEquals(costItem.getItem(), item2);
        Assert.assertEquals(costItem.getDescription(), description2);
        Assert.assertEquals(costItem.getQuantity(), quantity2);
        Assert.assertEquals(costItem.getCost(), cost2);
        Assert.assertEquals(costItem.getQuestion(),question2);
    }
}