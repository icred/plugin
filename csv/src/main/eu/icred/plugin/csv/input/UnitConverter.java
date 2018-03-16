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
        Map<Class<?>, String> foundParents = new HashMap<Class<?>, String>();
        String periodIdentifier = null;
        try {
            Map<String, String> originalCSV = csvLine.getOriginalFields();

            periodIdentifier = originalCSV.get("PERIOD.IDENTIFIER");
            Data root = null;
            if (periodIdentifier != null && !periodIdentifier.isEmpty()) {
                Period period = zgif.getPeriods().get(periodIdentifier);
                root = period.getData();
            } else {
                root = zgif.getMaindata();
            }

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

            String companyId = foundParents.get(Company.class);
            Company company = root.getCompanies().get(companyId);

            String propertyId = foundParents.get(Property.class);
            Property property = company.getProperties().get(propertyId);

            String buildingId = foundParents.get(Building.class);
            String leaseId = foundParents.get(Lease.class);
            if (buildingId != null) {
                Building building = property.getBuildings().get(buildingId);
                if (building != null) {
                    Map<String, Unit> units = building.getUnits();
                    if (units == null) {
                        units = new HashMap<String, Unit>();
                        building.setUnits(units);
                    }

                    String unitId = unit.getObjectIdSender();
                    String hash = "c" + companyId + "-p" + propertyId + "-u" + unitId;
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
        } catch (Exception ex) {
            String parentsString = "[";
            for (Map.Entry<Class<?>, String> parent : foundParents.entrySet()) {
                if(parentsString == "[") {
                    parentsString += parent.getKey().getSimpleName()+":"+parent.getValue();
                } else {
                    parentsString += ", "+parent.getKey().getSimpleName()+":"+parent.getValue();
                }
            }
            parentsString += "]";
            throw new RuntimeException("error connecting unit (id="+unit.getObjectIdSender()+", period-id="+periodIdentifier+", parents="+parentsString+") into zgif object tree", ex);
        }
    }
}
