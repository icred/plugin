/**
 * 
 */
package eu.icred.plugin.csv.input;

import java.util.HashMap;
import java.util.Map;

import eu.icred.model.node.Container;
import eu.icred.model.node.Data;
import eu.icred.model.node.Period;

/**
 * @author Pascal Houdek
 *
 */
public class PeriodConverter extends NodeConverter<Period> {

    /**
     * @author Pascal Houdek
     * @param type
     */
    public PeriodConverter() {
        super(Period.class);
    }

    /* (non-Javadoc)
     * @see eu.icred.plugin.input.csv.NodeConverter#connectObjectWithContainer(eu.icred.model.node.Container, eu.icred.model.node.AbstractNode, eu.icred.plugin.input.csv.CSVLine)
     */
    @Override
    public void connectObjectWithContainer(Container zgif, Period period, CSVLine<Period> csvLine) {
        Map<String, Period> periods = zgif.getPeriods();
        if(periods == null) {
            periods = new HashMap<String, Period>();
            zgif.setPeriods(periods);
        }
        
        period.setData(new Data());
        
        periods.put(period.getIdentifier(), period);
    }

}
