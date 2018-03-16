package eu.icred.plugin.csv.input;

import eu.icred.model.node.Container;
import eu.icred.model.node.entity.Account;

public class AccountConverter extends BasicEntityNodeConverter<Account> {
    public AccountConverter() {
        super(Account.class);
    }

    @Override
    public void connectObjectWithContainer(Container zgif, Account account, CSVLine<Account> csvLine) {
        account.setObjectIdSender(account.getObjectIdSender() + "_/_" + account.getAccountingStandard());
        
        super.connectObjectWithContainer(zgif, account, csvLine);
    }
}
