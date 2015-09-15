package com.worth.ifs.application.finance.model;

import com.worth.ifs.application.finance.*;
import com.worth.ifs.application.finance.cost.CostItem;
import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.user.domain.Organisation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class OrganisationFinance {
    Long applicationFinanceId = 0L;
    EnumMap<CostType, CostCategory> costCategories = new EnumMap<>(CostType.class);
    Organisation organisation;
    List<Cost> costs = new ArrayList<>();

    CostItemFactory costItemFactory = new CostItemFactory();

    public OrganisationFinance(Long applicationFinanceId, Organisation organisation, List<Cost> costs) {
        this.applicationFinanceId = applicationFinanceId;
        this.organisation = organisation;
        this.costs = costs;
        initializeOrganisationFinances();
    }

    public void initializeOrganisationFinances() {
        createCostCategories();
        addCostsToCategories(applicationFinanceId);
    }

    private void createCostCategories() {
        for(CostType costType : CostType.values()) {
            CostCategory costCategory = createCostCategoryByType(costType);
            costCategories.put(costType, costCategory);
        }
    }

    private CostCategory createCostCategoryByType(CostType costType) {
        switch(costType) {
            case LABOUR:
                return new LabourCostCategory();
            default:
                return new DefaultCostCategory();
        }
    }

    private void addCostsToCategories(Long applicationFinanceId) {
        costs.stream().forEach(c -> addCostToCategory(c));
    }

    /**
     * The costs are converted to a representation based on its type that can be used in the view and
     * are added to a specific category (e.g. labour).
     * @param cost Cost to be added
     */
    private void addCostToCategory(Cost cost) {
        CostType costType = CostType.fromString(cost.getQuestion().getQuestionType().getTitle());
        CostItem costItem = costItemFactory.createCostItem(costType, cost);
        CostCategory costCategory = costCategories.get(costType);
        costCategory.addCost(costItem);
    }

    public Double getTotal() {
        return costCategories.entrySet().stream().mapToDouble(cat -> cat.getValue().getTotal()).sum();
    }

    public EnumMap<CostType, CostCategory> getCostCategories() {
        return costCategories;
    }

    public CostCategory getCostCategory(CostType costType) {
        return costCategories.get(costType);
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public Long getApplicationFinanceId() {
        return applicationFinanceId;
    }
}
