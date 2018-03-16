/**
 * 
 */
package eu.icred.plugin.csv.input;

import eu.icred.model.node.Container;
import eu.icred.model.node.Meta;

/**
 * @author Pascal Houdek
 * 
 */
public class MetaConverter extends NodeConverter<Meta> {

    /**
     * @author Pascal Houdek
     * @param type
     */
    public MetaConverter() {
        super(Meta.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.icred.plugin.input.csv.NodeConverter#connectObjectWithContainer
     * (eu.icred.model.node.AbstractNode,
     * eu.icred.plugin.input.csv.CSVLine)
     */
    @Override
    public void connectObjectWithContainer(Container zgif, Meta meta, CSVLine<Meta> csvLine) {
        zgif.setMeta(meta);
    }

}
