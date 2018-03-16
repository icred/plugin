package eu.icred.plugin.csv.input;

import eu.icred.model.node.Container;
import eu.icred.model.node.entity.Term;

public class TermConverter extends BasicEntityNodeConverter<Term> {

    private static Integer planPeriodCounter = 1;

    public TermConverter() {
        super(Term.class);
    }

    @Override
    public void connectObjectWithContainer(Container zgif, Term term, CSVLine<Term> csvLine) {

        if ((term.getObjectIdSender() == null) || (term.getObjectIdSender() == "")) {
            term.setObjectIdSender(term.getConditionType().name() + "_" + (planPeriodCounter++));
        }
        
        super.connectObjectWithContainer(zgif, term, csvLine);
    }
}
