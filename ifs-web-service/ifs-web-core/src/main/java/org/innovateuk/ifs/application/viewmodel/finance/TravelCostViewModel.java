package org.innovateuk.ifs.application.viewmodel.finance;

import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.innovateuk.ifs.form.resource.FormInputType;

/**
 * View model for travel costs form input.
 */
public class TravelCostViewModel extends AbstractCostViewModel {

    @Override
    protected FormInputType formInputType() {
        return FormInputType.TRAVEL;
    }

    @Override
    public FinanceRowType getFinanceRowType() {
        return FinanceRowType.TRAVEL;
    }
}
