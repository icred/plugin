package eu.icred.plugin.csv;

import eu.icred.plugin.IPlugin;
import eu.icred.plugin.csv.input.Converter;
import eu.icred.plugin.worker.input.IImportWorker;
import eu.icred.plugin.worker.output.IExportWorker;

public class Plugin implements IPlugin {

    /*
     * (non-Javadoc)
     * 
     * @see eu.icred.plugin.IPlugin#getPluginId()
     */
    @Override
    public String getPluginId() {
        return "icred.csv";
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.icred.plugin.IPlugin#getPluginVersion()
     */
    @Override
    public String getPluginVersion() {
        return "0.6.2";
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.icred.plugin.IPlugin#getPluginName()
     */
    @Override
    public String getPluginName() {
        return "ICRED-CSV-Plugin";
    }

    @Override
    public IImportWorker getImportPlugin() {
        return new Converter();
    }

    @Override
    public IExportWorker getExportPlugin() {
        return null;
    }

    @Override
    public boolean isModelVersionSupported(String version) {
        return version.startsWith("1-0.5.") || version.startsWith("1-0.6.");
    }
}
