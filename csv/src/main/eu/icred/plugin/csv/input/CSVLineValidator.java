/**
 * 
 */
package eu.icred.plugin.csv.input;

import java.util.ArrayList;
import java.util.List;

import eu.icred.deprecated.ValidationError;
import eu.icred.deprecated.impl.Validator;
import eu.icred.model.node.AbstractNode;

/**
 * @author phoudek
 *
 */
public class CSVLineValidator<Node extends AbstractNode> extends Validator<CSVLine<Node>> {

    /**
     * @see eu.icred.deprecated.impl.Validator#validate(java.lang.Object)
     */
    @Override
    public List<ValidationError> validate(CSVLine<Node> csvLine) {
        // TODO Auto-generated method stub
        return new ArrayList<ValidationError>();
    }

}
