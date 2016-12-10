package org.innovateuk.ifs.user.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.user.domain.Contract;

import java.util.List;
import java.util.function.BiConsumer;

import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.createDefault;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

/**
 * Builder for {@link Contract}s.
 */
public class ContractBuilder extends BaseBuilder<Contract, ContractBuilder> {

    private ContractBuilder(List<BiConsumer<Integer, Contract>> multiActions) {
        super(multiActions);
    }

    public static ContractBuilder newContract() {
        return new ContractBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected ContractBuilder createNewBuilderWithActions(List<BiConsumer<Integer, Contract>> actions) {
        return new ContractBuilder(actions);
    }

    public ContractBuilder withId(Long... ids) {
        return withArray((id, contract) -> setField("id", id, contract), ids);
    }

    public ContractBuilder withCurrent(Boolean... currents) {
        return withArray((current, contract) -> setField("current", current, contract), currents);
    }

    public ContractBuilder withText(String... texts) {
        return withArray((text, contract) -> setField("text", text, contract), texts);
    }

    public ContractBuilder withAnnexA(String... annexAs) {
        return withArray((annexA, contract) -> setField("annexA", annexA, contract), annexAs);
    }

    public ContractBuilder withAnnexB(String... annexBs) {
        return withArray((annexB, contract) -> setField("annexB", annexB, contract), annexBs);
    }

    public ContractBuilder withAnnexC(String... annexCs) {
        return withArray((annexC, contract) -> setField("annexC", annexC, contract), annexCs);
    }

    @Override
    protected Contract createInitial() {
        return createDefault(Contract.class);
    }
}