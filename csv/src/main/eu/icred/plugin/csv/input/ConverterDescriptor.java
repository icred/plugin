package eu.icred.plugin.csv.input;

import java.io.InputStream;

import eu.icred.deprecated.IConverterDescriptor;

/**
 * bean with csv data streams
 * 
 * @author phoudek
 * 
 */
public class ConverterDescriptor implements IConverterDescriptor {

    private InputStream csvStreamMeta;
    private InputStream csvStreamPeriods;
    private InputStream csvStreamCompany;
    private InputStream csvStreamProperty;
    private InputStream csvStreamLand;
    private InputStream csvStreamPartner;
    private InputStream csvStreamBuild;
    private InputStream csvStreamLease;
    private InputStream csvStreamUnit;
    private InputStream csvStreamTerm;
    private InputStream csvStreamAccount;

    /**
     * simple constructor
     * 
     * @author phoudek
     */
    public ConverterDescriptor() {
    }

    /**
     * @author phoudek
     * @return the csvStreamMeta
     */
    public InputStream getCsvStreamMeta() {
        return csvStreamMeta;
    }

    /**
     * @author phoudek
     * @param csvStreamMeta
     *            the csvStreamMeta to set
     */
    public void setCsvStreamMeta(InputStream csvStreamMeta) {
        this.csvStreamMeta = csvStreamMeta;
    }

    /**
     * @author phoudek
     * @return the csvStreamPeriods
     */
    public InputStream getCsvStreamPeriods() {
        return csvStreamPeriods;
    }

    /**
     * @author phoudek
     * @param csvStreamPeriods
     *            the csvStreamPeriods to set
     */
    public void setCsvStreamPeriods(InputStream csvStreamPeriods) {
        this.csvStreamPeriods = csvStreamPeriods;
    }

    /**
     * @author phoudek
     * @return the csvStreamCompany
     */
    public InputStream getCsvStreamCompany() {
        return csvStreamCompany;
    }

    /**
     * @author phoudek
     * @param csvStreamCompany
     *            the csvStreamCompany to set
     */
    public void setCsvStreamCompany(InputStream csvStreamCompany) {
        this.csvStreamCompany = csvStreamCompany;
    }

    /**
     * @author phoudek
     * @return the csvStreamProperty
     */
    public InputStream getCsvStreamProperty() {
        return csvStreamProperty;
    }

    /**
     * @author phoudek
     * @param csvStreamProperty
     *            the csvStreamProperty to set
     */
    public void setCsvStreamProperty(InputStream csvStreamProperty) {
        this.csvStreamProperty = csvStreamProperty;
    }

    /**
     * @author phoudek
     * @return the csvStreamLand
     */
    public InputStream getCsvStreamLand() {
        return csvStreamLand;
    }

    /**
     * @author phoudek
     * @param csvStreamLand
     *            the csvStreamLand to set
     */
    public void setCsvStreamLand(InputStream csvStreamLand) {
        this.csvStreamLand = csvStreamLand;
    }

    /**
     * @author phoudek
     * @return the csvStreamBuild
     */
    public InputStream getCsvStreamBuild() {
        return csvStreamBuild;
    }

    /**
     * @author phoudek
     * @param csvStreamBuild
     *            the csvStreamBuild to set
     */
    public void setCsvStreamBuild(InputStream csvStreamBuild) {
        this.csvStreamBuild = csvStreamBuild;
    }

    /**
     * @author phoudek
     * @return the csvStreamLease
     */
    public InputStream getCsvStreamLease() {
        return csvStreamLease;
    }

    /**
     * @author phoudek
     * @param csvStreamLease
     *            the csvStreamLease to set
     */
    public void setCsvStreamLease(InputStream csvStreamLease) {
        this.csvStreamLease = csvStreamLease;
    }

    /**
     * @author phoudek
     * @return the csvStreamUnit
     */
    public InputStream getCsvStreamUnit() {
        return csvStreamUnit;
    }

    /**
     * @author phoudek
     * @param csvStreamUnit
     *            the csvStreamUnit to set
     */
    public void setCsvStreamUnit(InputStream csvStreamUnit) {
        this.csvStreamUnit = csvStreamUnit;
    }

    /**
     * @author phoudek
     * @return the csvStreamTerm
     */
    public InputStream getCsvStreamTerm() {
        return csvStreamTerm;
    }

    /**
     * @author phoudek
     * @param csvStreamTerm
     *            the csvStreamTerm to set
     */
    public void setCsvStreamTerm(InputStream csvStreamTerm) {
        this.csvStreamTerm = csvStreamTerm;
    }

    /**
     * @author phoudek
     * @return the csvStreamAccount
     */
    public InputStream getCsvStreamAccount() {
        return csvStreamAccount;
    }

    /**
     * @author phoudek
     * @param csvStreamAccount
     *            the csvStreamAccount to set
     */
    public void setCsvStreamAccount(InputStream csvStreamAccount) {
        this.csvStreamAccount = csvStreamAccount;
    }

    public InputStream getCsvStreamPartner() {
        return csvStreamPartner;
    }

    public void setCsvStreamPartner(InputStream csvStreamPartner) {
        this.csvStreamPartner = csvStreamPartner;
    }
    
    
}
