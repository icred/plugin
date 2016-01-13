package eu.icred.plugin.csv.input;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import eu.icred.deprecated.IConverterDescriptor;
import eu.icred.deprecated.ITransformer;
import eu.icred.deprecated.impl.BasicConverter;
import eu.icred.model.annotation.DataField;
import eu.icred.model.annotation.ObjectIdentifier;
import eu.icred.model.node.AbstractNode;
import eu.icred.model.node.Container;

/**
 * basic generic converter for every sub object/node of a zgif object, e.g.
 * company, property, unit, lease, ...
 * 
 * @author phoudek
 * @param <Node>
 *            class of the node/sub object type
 */
abstract public class NodeConverter<Node extends AbstractNode> extends BasicConverter {
    private static Logger             logger = Logger.getLogger(NodeConverter.class);

    protected Class<Node>             type;
    protected NodeConverterDescriptor descriptor;

    /**
     * @author phoudek
     * @param type
     *            class of the node/sub object type
     */
    public NodeConverter(Class<Node> type) {
        this.type = type;
    }

    /**
     * @author phoudek
     * @param descriptor
     *            bean that contains Reader for csv data stream and the zgif
     *            object
     * @see BasicConverter#doConvertData(IConverterDescriptor)
     */
    @Override
    public void doConvertData(IConverterDescriptor descriptor) {

        this.descriptor = (NodeConverterDescriptor) descriptor;

        convert(this.descriptor.getCsvStream());
    }

    private void convert(InputStream stream) {
        CSVReader<Node> csvReader;
        try {
            Container container = descriptor.getContainer();
            csvReader = new CSVReader<Node>(type, stream);

            CSVLine<Node> line = null;
            CSVLine<Node> lastLine = null;
            do {
                try {
                    line = csvReader.readLine();
                    if (line != null) {
                        CSV2NodeTransformer<Node> transformer = (CSV2NodeTransformer<Node>) getTransformer();
                        transformer.setValidator(new CSVLineValidator<Node>());
                        transformer.setValidate(true);
                        Node obj = null;
                        try {
                            obj = transformer.transform(line, null);
                        } catch (Exception e) {
                            throw new Exception("unable to transform line to " + type.getSimpleName() + " object", e);
                        }
                        if (obj != null) {
                            try {
                                connectObjectWithContainer(container, obj, line);
                            } catch (Exception e) {
                                throw new Exception("unable to connect object " + obj.toString() + " to zgif. Are the parent-IDs correct?", e);
                            }
                        }
                    }
                } catch (Exception e) {
                    Long lineNumber = (line != null) ? line.getLineNumber() : ((lastLine != null) ? lastLine.getLineNumber() + 1 : null);
                    String lineNumberString = (lineNumber != null) ? lineNumber.toString() : "?";
                    logger.error("unable to read csv data of line " + lineNumberString + " and transform to " + type.getSimpleName() + " object", e);
                }
            } while (line != null);
        } catch (Exception e) {
            logger.error("unable to read csv stream for " + type.getSimpleName() + " objects", e);
        }
    }

    abstract public void connectObjectWithContainer(Container zgif, Node object, CSVLine<Node> csvLine);

    /**
     * @return returns the Transformer for transforming a csv line to a zgif
     *         node object
     * @author phoudek
     */
    @Override
    public ITransformer<CSVLine<Node>, Node> getTransformer() {
        return new CSV2NodeTransformer<Node>(type);
    }

    protected static Method getObjectIdentifierGetter(Class<?> type) {
        Method objectIdentifierGetter = null;

        List<Field> fields = getAllFieldsOfClass(type);
        for (Field field : fields) {
            if (field.getAnnotation(ObjectIdentifier.class) != null) {
                String fieldName = field.getName();
                try {
                    objectIdentifierGetter = type.getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
                } catch (SecurityException e) {
                    logger.info("no identifier getter found for class: " + type);
                } catch (NoSuchMethodException e) {
                    logger.info("no identifier getter found for class: " + type);
                }
            }
        }

        return objectIdentifierGetter;
    }

    private static List<Field> getAllFieldsOfClass(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();
        Class<?> current = clazz;
        while (current.getSuperclass() != null) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));

            current = current.getSuperclass();
        }
        return fields;
    }
}
