package eu.icred.plugin.csv.input;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;

import org.apache.log4j.Logger;

import eu.icred.deprecated.IConverterDescriptor;
import eu.icred.deprecated.ITransformer;
import eu.icred.deprecated.impl.BasicConverter;
import eu.icred.model.datatype.enumeration.Subset;
import eu.icred.model.node.Container;
import eu.icred.model.node.entity.Building;
import eu.icred.model.node.entity.Company;
import eu.icred.model.node.entity.Lease;
import eu.icred.model.node.entity.Property;
import eu.icred.plugin.PluginComponent;
import eu.icred.plugin.worker.WorkerConfiguration;
import eu.icred.plugin.worker.input.IImportWorker;
import eu.icred.plugin.worker.input.ImportWorkerConfiguration;
import eu.icred.ui.gui.DefaultPluginGui;

/**
 * Converter converts serveral csv data streams to the internal zgif object
 * 
 * @author phoudek
 * 
 */
@SuppressWarnings("deprecation")
public class Converter extends BasicConverter implements IImportWorker {
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(Converter.class);

    public static final Subset[] SUPPORTED_SUBSETS = Subset.values();

    private Container container = null;

    /**
     * @author phoudek
     * @param descriptor
     *            bean that contains Reader for csv data streams
     * @see BasicConverter#doConvertData(IConverterDescriptor)
     */
    @Override
    public void doConvertData(IConverterDescriptor descriptor) {
        ConverterDescriptor myDesc = (ConverterDescriptor) descriptor;

        NodeConverterDescriptor nodeConverterDescr = new NodeConverterDescriptor();
        container = new Container();
        nodeConverterDescr.setContainer(container);


        nodeConverterDescr.setCsvStream(myDesc.getCsvStreamMeta());
        new MetaConverter().convertData(nodeConverterDescr);
        
        nodeConverterDescr.setCsvStream(myDesc.getCsvStreamPeriods());
        new PeriodConverter().convertData(nodeConverterDescr);

        nodeConverterDescr.setCsvStream(myDesc.getCsvStreamCompany());
        new BasicEntityNodeConverter<Company>(Company.class).convertData(nodeConverterDescr);

        nodeConverterDescr.setCsvStream(myDesc.getCsvStreamProperty());
        new BasicEntityNodeConverter<Property>(Property.class).convertData(nodeConverterDescr);

        nodeConverterDescr.setCsvStream(myDesc.getCsvStreamBuild());
        new BasicEntityNodeConverter<Building>(Building.class).convertData(nodeConverterDescr);

        nodeConverterDescr.setCsvStream(myDesc.getCsvStreamLease());
        new BasicEntityNodeConverter<Lease>(Lease.class).convertData(nodeConverterDescr);
        

        nodeConverterDescr.setCsvStream(myDesc.getCsvStreamUnit());
//      new BasicEntityNodeConverter<Unit>(Unit.class).convertData(nodeConverterDescr);
        new UnitConverter().convertData(nodeConverterDescr);
        
        nodeConverterDescr.setCsvStream(myDesc.getCsvStreamTerm());
//      new BasicEntityNodeConverter<Term>(Term.class).convertData(nodeConverterDescr);
        new TermConverter().convertData(nodeConverterDescr);
        
        nodeConverterDescr.setCsvStream(myDesc.getCsvStreamAccount());
        new AccountConverter().convertData(nodeConverterDescr);
    }
    
    /**
     * @author phoudek
     * @return there is no transformer class for a zgif object, return value is
     *         always <code>null</code>
     */
    @Override
    public ITransformer<?, ?> getTransformer() {
        return null;
    }

    /**
     * getter for zgif object. instance type depends on the subset of meta data
     * 
     * @author phoudek
     * @return zgif object
     */
    public Container getContainer() {
        return this.container;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.icred.plugin.Plugin#unload()
     */
    public void unload() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.icred.plugin.input.ImportPlugin#load(eu.icred.
     * plugin.input.ImportPluginConfiguration)
     */
    public void load(ImportWorkerConfiguration config) {
        logger.debug("load(config)");
        ConverterDescriptor desc = new ConverterDescriptor();

        SortedMap<String, InputStream> streams = config.getStreams();
        
        desc.setCsvStreamMeta(streams.get("meta"));
        desc.setCsvStreamPeriods(streams.get("periods"));
        desc.setCsvStreamCompany(streams.get("company"));
        desc.setCsvStreamProperty(streams.get("property"));
        desc.setCsvStreamBuild(streams.get("building"));
        desc.setCsvStreamUnit(streams.get("unit"));
        desc.setCsvStreamLease(streams.get("lease"));
        desc.setCsvStreamTerm(streams.get("term"));
        desc.setCsvStreamPartner(streams.get("partner"));
        desc.setCsvStreamAccount(streams.get("account"));

        doConvertData(desc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.icred.plugin.Plugin#load(eu.icred.plugin. PluginConfiguration)
     */
    public void load(WorkerConfiguration config) {
        load((ImportWorkerConfiguration) config);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.icred.plugin.input.ImportPlugin#
     * getRequiredConfigurationArguments()
     */
    public ImportWorkerConfiguration getRequiredConfigurationArguments() {

        return new ImportWorkerConfiguration() {
            {
                SortedMap<String, InputStream> streams = getStreams();
                streams.put("meta", null);
                streams.put("periods", null);
                streams.put("company", null);
                streams.put("property", null);
                streams.put("building", null);
                streams.put("unit", null);
                streams.put("lease", null);
                streams.put("term", null);
                streams.put("partner", null);
                streams.put("account", null);
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.icred.plugin.input.ImportPlugin#getConfigGui()
     */
    public PluginComponent<ImportWorkerConfiguration> getConfigGui() {
        return new DefaultPluginGui<ImportWorkerConfiguration>(getRequiredConfigurationArguments());
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.icred.plugin.Plugin#getSupportedSubsets()
     */
    public List<Subset> getSupportedSubsets() {
        return Arrays.asList(SUPPORTED_SUBSETS);
    }
}
