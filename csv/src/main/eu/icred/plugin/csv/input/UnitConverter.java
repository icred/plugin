/**
 * 
 */
package eu.icred.plugin.csv.input;

import java.util.HashMap;
import java.util.Map;

import eu.icred.model.node.Container;
import eu.icred.model.node.Data;
import eu.icred.model.node.Period;
import eu.icred.model.node.entity.Building;
import eu.icred.model.node.entity.Company;
import eu.icred.model.node.entity.Lease;
import eu.icred.model.node.entity.LeasedUnit;
import eu.icred.model.node.entity.Property;
import eu.icred.model.node.entity.Unit;

/**
 * @author Pascal Houdek
 * 
 */
public class UnitConverter extends BasicEntityNodeConverter<Unit> {

    /**
     * @author Pascal Houdek
     * @param type
     */
    public UnitConverter() {
        super(Unit.class);
    }

    /**
     * method connects the zgif object from <code>NodeConverterDescriptor</code>
     * with the converted node object of the csvLine
     * 
     * @author phoudek
     * @param unit
     *            the converted zgif node object
     * @param csvLine
     *            the original csv data line
     */
    public void connectObjectWithContainer(Container zgif, Unit unit, CSVLine<Unit> csvLine) {
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

        String companyId = foundParents.get(Company.class).replaceFirst("^0+(?!$)", "");
        Company company = root.getCompanies().get(companyId);

        String propertyId = foundParents.get(Property.class).replaceFirst("^0+(?!$)", "");
        Property property = company.getProperties().get(propertyId);

        String buildingId = foundParents.get(Building.class).replaceFirst("^0+(?!$)", "");
        String leaseId = foundParents.get(Lease.class).replaceFirst("^0+(?!$)", "");
        if (buildingId != null) {
            Building building = property.getBuildings().get(buildingId);
            if (building != null) {
                Map<String, Unit> units = building.getUnits();
                if (units == null) {
                    units = new HashMap<String, Unit>();
                    building.setUnits(units);
                }

                String unitId = unit.getObjectIdSender().replaceFirst("^0+(?!$)", "");
                String hash = "c" + companyId + "-p" + propertyId + "-l" + leaseId + "-u" + unitId;
                unit.setHash(hash);

                units.put(unit.getObjectIdSender(), unit);
            }
        }

        if (leaseId != null) {
            Map<String, Lease> leases = property.getLeases();
            if (leases != null) {
                Lease lease = leases.get(leaseId);
                if (lease != null) {
                    Map<String, LeasedUnit> units = lease.getLeasedUnits();
                    if (units == null) {
                        units = new HashMap<String, LeasedUnit>();
                        lease.setLeasedUnits(units);
                    }

                    LeasedUnit lUnit = new LeasedUnit();
                    String unitHash = unit.getHash();
                    lUnit.setHash(unitHash);

                    units.put(unitHash, lUnit);
                }
            }
        }

        //
        // AbstractNode curNode = root;
        // for (Class<?> clazz : x) {
        // if(foundParents.containsKey(clazz)) {
        // String parentId = foundParents.get(clazz);
        // Map<String, AbstractEntityNode> list = (Map<String,
        // AbstractEntityNode>) getNodeList(curNode, clazz);
        // if(list.get(parentId) != null) {
        // curNode = list.get(parentId);
        // }
        // if(entity instanceof Unit) {
        // if(clazz.equals(Building.class) || clazz.equals(Land.class)) {
        // Map<String, EntityNode> entityList = getNodeList(curNode, this.type);
        // entityList.put(entity.getObjectIdSender(), entity);
        // }
        // }
        // }
        // }
        //
        // Map<String, EntityNode> entityList = getNodeList(curNode, this.type);
        // entityList.put(entity.getObjectIdSender(), entity);
        //
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
        // String typeNameCSV = typeName.substring(typeName.lastIndexOf(".") +
        // 1).toUpperCase();
        // String nextParentObjectId = originalCSV.get(typeNameCSV +
        // ".OBJECT_ID_SENDER");
        //
        // PropertyDescriptor pd = new
        // PropertyDescriptor(nodeListField.getName(), curParent.getClass());
        // Method getListMethod = pd.getReadMethod();
        // Map<String, AbstractNode> listOfNodes = (Map<String, AbstractNode>)
        // getListMethod.invoke(curParent);
        // AbstractNode nextParent = listOfNodes.get(nextParentObjectId);
        // curParent = nextParent;
        // }
        // }
        // }
    }
}
