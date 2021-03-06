package eu.icred.plugin.csv.input;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import eu.icred.deprecated.ITransformContext;
import eu.icred.deprecated.impl.BasicTransformer;
import eu.icred.model.datatype.Amount;
import eu.icred.model.datatype.Area;
import eu.icred.model.datatype.enumeration.AreaMeasurement;
import eu.icred.model.datatype.enumeration.AreaType;
import eu.icred.model.datatype.enumeration.Subset;
import eu.icred.model.node.AbstractNode;
import eu.icred.model.node.ExtensionMap;
import eu.icred.model.node.group.AbstractGroupNode;

/**
 * transforms a <code>CSVLine<A></code> to an <code>AbstractNode</code> of type
 * <NodeType>
 * 
 * @author phoudek
 * 
 * @param <NodeType>
 *            Type of the <code>AbstractNode</code> for transformer target
 */
public class CSV2NodeTransformer<NodeType extends AbstractNode> extends BasicTransformer<CSVLine<NodeType>, NodeType> {
    private static Logger logger = Logger.getLogger(CSV2NodeTransformer.class);

    protected Class<NodeType> type;

    public CSV2NodeTransformer(Class<NodeType> type) {
        super();
        this.type = type;
    }

    /**
     * creates a map with all setters of a specific <code>AbstractNode</code>
     * 
     * @param type
     *            type of the AbstractNode
     * @return map of setters
     */
    private Map<String, Method> getMethodMap(Class<?> type) {
        Map<String, Method> methodMap = new HashMap<String, Method>();
        Method[] methods = type.getMethods();

        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("set")) {
                methodMap.put(methodName, method);
            }
        }
        return methodMap;
    }

    private void setExtensionMapValue(NodeType object, String key, String value) {
        ExtensionMap em = object.getExtensionMap();
 
        String keyName = key.substring(key.lastIndexOf(".") + 1);

        if (key.startsWith("EXTENSION.ITEM.")) {
            em.setValue(keyName, value);
        } else if (key.startsWith("EXTENSION.LIST.")) {
            em.setSubList(keyName, Arrays.asList(value.split(",")));
        } else {
//            throw new Error("unknown key: " + key);
        }
    }

    /**
     * calls the setter for an object
     * 
     * @param object
     *            object to set the value
     * @param key
     *            name of the datafield / setter
     * @param value
     *            value to set
     */
    protected void setValue(NodeType object, String key, String value) {
        if (key.startsWith("EXTENSION.")) {
            setExtensionMapValue(object, key, value);
            return;
        }

        Map<String, Method> methodMap = getMethodMap(object.getClass());

        Method method = null;
        NodeType methodInvokeObject = object;

        Integer separatorPos = key.indexOf(".");
        if (separatorPos >= 0) {
            // nested object:
            String objectName = key.substring(0, separatorPos);
            String attributeName = key.substring(separatorPos + 1);

            Method getNestedObjectMethod = null;
            try {
                getNestedObjectMethod = getMethodDeep(object.getClass(), "get" + objectName);
                if (getNestedObjectMethod != null) {
                    methodInvokeObject = (NodeType) getNestedObjectMethod.invoke(object);
                }
                if (methodInvokeObject == null) {
                    Class<?> invokeObjectClass = Class.forName(AbstractGroupNode.class.getPackage().getName() + "." + objectName);
                    methodInvokeObject = (NodeType) invokeObjectClass.newInstance();
                    Method setNestedObjectMethod = getMethodDeep(object.getClass(), "set" + objectName, invokeObjectClass);
                    setNestedObjectMethod.invoke(object, methodInvokeObject);
                }
                String methodName = "set" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
                Method[] methods = methodInvokeObject.getClass().getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    Method invokeObjectMethod = methods[i];
                    if (invokeObjectMethod.getName().equals(methodName)) {
                        method = invokeObjectMethod;
                        break;
                    }
                }
            } catch (Exception e) {
                Exception x = e;
            }
        } else {
            String methodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
            method = methodMap.get(methodName);
        }

        if (method != null) {
            Object setValue = null;
            Class<?> targetType = method.getParameterTypes()[0];

            try {
                if (value.equals("")) {
                    setValue = null;
                } else if (String.class.isAssignableFrom(targetType)) {
                    setValue = value;
                } else if (Double.class.isAssignableFrom(targetType)) {
                    setValue = Double.parseDouble(value);
                } else if (Integer.class.isAssignableFrom(targetType)) {
                    setValue = Integer.valueOf(value);
                } else if (BigDecimal.class.isAssignableFrom(targetType)) {
                    setValue = BigDecimal.valueOf(Double.parseDouble(value));
                } else if (Currency.class.isAssignableFrom(targetType)) {
                    setValue = Currency.getInstance(value);
                } else if (LocalDate.class.isAssignableFrom(targetType)) {
                    // TODO: überprüfen
                    setValue = LocalDate.parse(value);
                } else if (LocalDateTime.class.isAssignableFrom(targetType)) {
                    // TODO: überprüfen
                    setValue = LocalDateTime.parse(value);
                } else if (Period.class.isAssignableFrom(targetType)) {
                    // TODO: überprüfen
                    setValue = Period.parse(value);
                } else if (Locale.class.isAssignableFrom(targetType)) {
                    // TODO: korrigieren
                    setValue = Locale.GERMANY;
                } else if (Boolean.class.isAssignableFrom(targetType)) {
                    setValue = Boolean.parseBoolean(value);
                } else if (XMLGregorianCalendar.class.isAssignableFrom(targetType)) {
                    Date date = LocalDateTime.parse(value).toDate();
                    GregorianCalendar c = new GregorianCalendar();
                    c.setTime(date);
                    setValue = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
                } else if (javax.xml.datatype.Duration.class.isAssignableFrom(targetType)) {
                    setValue = DatatypeFactory.newInstance().newDuration(value);
                } else if (Amount.class.isAssignableFrom(targetType)) {
                    String[] parts = value.split(" ");
                    if (parts.length == 1) {
                        Amount amount = new Amount();
                        amount.setValue(Double.parseDouble(parts[0]));
                        amount.setCurrency(Currency.getInstance("EUR"));
                        setValue = amount;
                    } else if (parts.length == 2) {
                        Amount amount = new Amount();
                        amount.setValue(Double.parseDouble(parts[0]));
                        amount.setCurrency(Currency.getInstance(parts[1]));
                        setValue = amount;
                    } else {
                        logger.warn("invalid value '" + value + "' for type '" + targetType + "' - value will be set to empty");
                    }
                } else if (Area.class.isAssignableFrom(targetType)) {
                    String[] parts = value.split(" ");
                    if (parts.length == 1) {
                        Area area = new Area();
                        area.setValue(Double.parseDouble(parts[0]));
                        area.setAreaMessurement(AreaMeasurement.NOT_SPECIFIED);
                        area.setAreaType(AreaType.NOT_SPECIFIED);
                        setValue = area;
                    } else if (parts.length == 2) {
                        Area area = new Area();
                        area.setValue(Double.parseDouble(parts[0]));
                        area.setAreaMessurement(AreaMeasurement.valueOf(parts[1]));
                        area.setAreaType(AreaType.NOT_SPECIFIED);
                        setValue = area;
                    } else if (parts.length == 3) {
                        Area area = new Area();
                        area.setValue(Double.parseDouble(parts[0]));
                        area.setAreaMessurement(AreaMeasurement.valueOf(parts[1]));
                        area.setAreaType(AreaType.valueOf(parts[2]));
                        setValue = area;
                    } else {
                        logger.warn("invalid value '" + value + "' for type '" + targetType + "' - value will be set to empty");
                    }
                    // } else if (Country.class.isAssignableFrom(targetType)) {
                    // setValue = new Country(value);
                } else if (targetType.getPackage() == Subset.class.getPackage()) {
                    // type is enumeration:
                    try {
                        Method fromString = targetType.getDeclaredMethod("fromString", String.class);
                        setValue = fromString.invoke(null, value);
                    } catch (Throwable t) {
                    }

                    if (setValue == null) {
                        try {
                            Method valueOf = targetType.getDeclaredMethod("valueOf", String.class);
                            setValue = valueOf.invoke(null, value);
                        } catch (Throwable t) {
                            Object x = Arrays.asList(targetType.getEnumConstants());
                            logger.warn("cannot set value '" + value + "' as " + targetType.getSimpleName() + " - allowed values: " + x);
                        }
                    }
                } else {
                    // unknown type
                    setValue = null;
                    logger.warn("cannot set value '" + value + "' - unknown data type: " + targetType + " - value will be set to empty");
                }

                method.invoke(methodInvokeObject, setValue);
            } catch (Throwable e) {
                throw new RuntimeException("could not set '" + key + "' (value=" + value + ") to object " + object.toString()
                        + " (plugin bug - please report bug of plugin)", e);
            }
        } else {
            if (!key.endsWith(".identifier") && !key.endsWith(".objectIdSender")) {
                throw new RuntimeException("could not set '" + key + "' to object " + object.toString()
                        + ": no setter found for key (please report bug of plugin)");
            }
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see eu.icred.deprecated.impl.BasicTransformer#doTransform
     */
    @Override
    protected NodeType doTransform(CSVLine<NodeType> csvLine, ITransformContext context) {
        NodeType object = null;

        try {
            object = type.newInstance();
            for (Entry<String, String> dataField : csvLine.entrySet()) {
                String keyName = ((String) dataField.getKey()).startsWith("EXTENSION.REVC_") ? "EXTENSION.ITEM." + ((String) dataField.getKey()).substring(10)
                        : (String) dataField.getKey();
                setValue(object, keyName, dataField.getValue());
            }

            ExtensionMap em = object.getExtensionMap();
            for (Map.Entry<String, String> dataField : csvLine.getOriginalFields().entrySet()) {
                String keyName = (String) dataField.getKey();
                if ((!keyName.endsWith(".OBJECT_ID_SENDER")) && (!keyName.endsWith("PERIOD.IDENTIFIER"))) {
                    em.setValue("ORGCSV_" + keyName, (String) dataField.getValue());
                }
            }
        } catch (IllegalAccessException e) {
            logger.error("could not create object of type '" + type.toString() + "' (plugin bug - please report bug of plugin)", e);
        } catch (InstantiationException e) {
            logger.error("could not create object of type '" + type.toString() + "' (plugin bug - please report bug of plugin)", e);
        }

        return (NodeType) object;
    }

    private Method getMethodDeep(Class clazz, String methodName, Class... parameterTypes) {
        Method m = null;

        do {
            try {

                m = clazz.getMethod(methodName, parameterTypes);
            } catch (Exception e) {
            }
            if (m == null) {
                clazz = clazz.getSuperclass();
            }
        } while (m == null && !clazz.equals(Object.class));

        return m;
    }
}
