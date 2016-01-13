/**
 * 
 */
package eu.icred.plugin.csv.input;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.icred.model.node.AbstractNode;
import eu.icred.model.node.Container;
import eu.icred.model.node.Data;
import eu.icred.model.node.Period;
import eu.icred.model.node.entity.AbstractEntityNode;
import eu.icred.model.node.entity.Account;
import eu.icred.model.node.entity.BookEntry;
import eu.icred.model.node.entity.Building;
import eu.icred.model.node.entity.Company;
import eu.icred.model.node.entity.Land;
import eu.icred.model.node.entity.Lease;
import eu.icred.model.node.entity.LeasedUnit;
import eu.icred.model.node.entity.Project;
import eu.icred.model.node.entity.Property;
import eu.icred.model.node.entity.Record;
import eu.icred.model.node.entity.ServiceContract;
import eu.icred.model.node.entity.Term;
import eu.icred.model.node.entity.Unit;
import eu.icred.model.node.entity.Valuation;

/**
 * @author Pascal Houdek
 * 
 */
public class BasicEntityNodeConverter<EntityNode extends AbstractEntityNode> extends NodeConverter<EntityNode> {
    /**
     * @author Pascal Houdek
     * @param type
     */
    public BasicEntityNodeConverter(Class<EntityNode> type) {
        super(type);
    }

    final static List<Class<? extends AbstractEntityNode>> x = new ArrayList<Class<? extends AbstractEntityNode>>() {
                                                                 {
                                                                     this.add(Company.class);
                                                                     this.add(Property.class);
                                                                     this.add(Project.class);
                                                                     this.add(ServiceContract.class);
                                                                     this.add(Valuation.class);
                                                                     this.add(Building.class);
                                                                     this.add(Lease.class);
                                                                     this.add(Land.class);
                                                                     this.add(Unit.class);
                                                                     this.add(LeasedUnit.class);
                                                                     this.add(Term.class);
                                                                     this.add(Account.class);
                                                                     this.add(BookEntry.class);
                                                                     this.add(Record.class);
                                                                 }
                                                             };

    @SuppressWarnings("unchecked")
    private Map<String, EntityNode> getNodeList(AbstractNode parent, Class<?> entityClass) throws IntrospectionException, IllegalAccessException,
        IllegalArgumentException, InvocationTargetException {
        Map<String, EntityNode> listOfNodes = null;

        Class<?> parentClass = parent.getClass();
        String entityClassName = entityClass.getSimpleName();

        for (Field field : parentClass.getDeclaredFields()) {
            String fieldName = field.getName();

            if (fieldName.endsWith("s") && fieldName.substring(0, 4).toLowerCase().equals(entityClassName.substring(0, 4).toLowerCase())) {

                PropertyDescriptor pd = new PropertyDescriptor(fieldName, parent.getClass());
                Method getListMethod = pd.getReadMethod();
                listOfNodes = (Map<String, EntityNode>) getListMethod.invoke(parent);
                if (listOfNodes == null) {
                    listOfNodes = new HashMap<String, EntityNode>();
                    Method setListMethod = pd.getWriteMethod();
                    setListMethod.invoke(parent, listOfNodes);

                }
            }

        }

        return listOfNodes;
    }

    /**
     * method connects the zgif object from <code>NodeConverterDescriptor</code>
     * with the converted node object of the csvLine
     * 
     * @author phoudek
     * @param entity
     *            the converted zgif node object
     * @param csvLine
     *            the original csv data line
     */
    public void connectObjectWithContainer(Container zgif, EntityNode entity, CSVLine<EntityNode> csvLine) {
        try {
            Map<String, String> originalCSV = csvLine.getOriginalFields();

            String periodIdentifier = originalCSV.get("PERIOD.IDENTIFIER");
            Data root = null;
            if (periodIdentifier != null && !periodIdentifier.isEmpty()) {
                Period period = zgif.getPeriods().get(periodIdentifier);
                root = period.getData();
            } else {
                root = zgif.getMaindata();
            }

            Map<Class<?>, String> foundParents = new HashMap<Class<?>, String>();
            for (Map.Entry<String, String> entry : originalCSV.entrySet()) {
                String key = entry.getKey();
                if (key.endsWith(".OBJECT_ID_SENDER")) {
                    Class<?> clazz = null;
                    for (Class<?> testClass : x) {
                        if (testClass.getSimpleName().toUpperCase().equals(key.substring(0, key.indexOf(".")))) {
                            clazz = testClass;
                            break;
                        }
                    }

                    foundParents.put(clazz, entry.getValue());
                }
            }
                
            AbstractNode curNode = root;
            for (Class<?> clazz : x) {
                if (foundParents.containsKey(clazz) && !(entity.getClass().equals(Term.class) && clazz.equals(Building.class))) {
                    String identifier = foundParents.get(clazz);

                    if (curNode instanceof Lease && clazz.equals(Unit.class)) {
                        clazz = LeasedUnit.class;
                        // TODO - hash:
                        identifier = "c" + foundParents.get(Company.class).replaceFirst("^0+(?!$)", "") + "-p" + foundParents.get(Property.class).replaceFirst("^0+(?!$)", "") + "-l" + foundParents.get(Lease.class).replaceFirst("^0+(?!$)", "")
                            + "-u" + foundParents.get(Unit.class).replaceFirst("^0+(?!$)", "");
                    }
                    Map<String, AbstractEntityNode> list = (Map<String, AbstractEntityNode>) getNodeList(curNode, clazz);
                    if (list.get(identifier) != null) {
                        curNode = list.get(identifier);
                    }
                    if (entity instanceof Unit) {
                        if (clazz.equals(Building.class) || clazz.equals(Land.class)) {
                            Map<String, EntityNode> entityList = getNodeList(curNode, this.type);
                            entityList.put(entity.getObjectIdSender(), entity);
                        }
                    }
                }
            }

            Map<String, EntityNode> entityList = getNodeList(curNode, this.type);
            String identifier = entity.getObjectIdSender();
            if(entity instanceof Term) {
                identifier = ((Term)entity).getConditionType().name();
            }
            entityList.put(identifier, entity);
            // System.out.println(curNode.getClass());
            //
            // Information.getHierarchicalParentClassesOfNode(root.getClass(),
            // entity.getClass());
            //
            // // Iterator<Class<?>> parentIterator =
            // //
            // Arrays.asList(Information.getParentClassesOfNode(object.getClass())).iterator();
            //
            // System.out.println("=== " + entity + " ===");
            //
            // AbstractNode curParent = root;
            // while (curParent != null) {
            // System.out.println(root);
            // Class<?> nextParentClass = null;// parentIterator.next();
            // List<Field> nodeListFields = new
            // NodeInformation(curParent.getClass()).getNodeLists();
            //
            // curParent = null;
            // for (Field nodeListField : nodeListFields) {
            // ParameterizedType pt = (ParameterizedType)
            // nodeListField.getGenericType();
            // String typeName = pt.getActualTypeArguments()[1].toString();
            //
            // if
            // (typeName.endsWith(nextParentClass.getSimpleName().replaceFirst("Abstract",
            // ""))) {
            // String typeNameCSV = typeName.substring(typeName.lastIndexOf(".")
            // + 1).toUpperCase();
            // String nextParentObjectId = originalCSV.get(typeNameCSV +
            // ".OBJECT_ID_SENDER");
            //
            // PropertyDescriptor pd = new
            // PropertyDescriptor(nodeListField.getName(),
            // curParent.getClass());
            // Method getListMethod = pd.getReadMethod();
            // Map<String, AbstractNode> listOfNodes = (Map<String,
            // AbstractNode>) getListMethod.invoke(curParent);
            // AbstractNode nextParent = listOfNodes.get(nextParentObjectId);
            // curParent = nextParent;
            // }
            // }
            // }
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
