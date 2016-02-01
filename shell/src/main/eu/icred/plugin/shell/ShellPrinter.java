package eu.icred.plugin.shell;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.icred.model.datatype.enumeration.Subset;
import eu.icred.model.node.AbstractNode;
import eu.icred.model.node.Container;
import eu.icred.model.node.Meta;
import eu.icred.model.node.Period;
import eu.icred.model.node.entity.AbstractEntityNode;
import eu.icred.model.node.entity.LeasedUnit;
import eu.icred.model.node.entity.Unit;
import eu.icred.plugin.PluginComponent;
import eu.icred.plugin.worker.WorkerConfiguration;
import eu.icred.plugin.worker.output.ExportWorkerConfiguration;
import eu.icred.plugin.worker.output.IExportWorker;

/**
 * simple export plugin that prints the hierarchy of a container object to
 * console
 * 
 * @author phoudek
 * 
 */
public class ShellPrinter implements IExportWorker {
    private static Logger        logger            = Logger.getLogger(ShellPrinter.class);
    public static final Subset[] SUPPORTED_SUBSETS = Subset.values();

    protected Container          container         = null;

    private String               prefix            = "";

    /**
     * returns the list of supported subsets of this import plugin
     * 
     * @author phoudek
     * @return list of supported subsets
     */
    @Override
    public List<Subset> getSupportedSubsets() {
        return Arrays.asList(SUPPORTED_SUBSETS);
    }

    private void doExport() {
        try {
            System.out.println(prefix + "Content of Container:");
            exportNode(container.getMeta());
            exportNode(container.getMaindata());
            exportNodes(container.getPeriods());
        } catch (Throwable e) {
            logger.error("error exporting", e);
        }
    }

    private void exportPeriods(Collection<Period> periods) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (periods != null && periods.size() > 0) {
            prefix += "  ";
            // System.out.println(prefix + periods.size() + "x Periods:");
            for (Period period : periods) {
                System.out.println(prefix + "- Period " + period.toString() + " (" + period.getIdentifier() + "; " + period.getFrom() + "->" + period.getTo()
                    + ")");
                // TODO
                // exportNode(period.getData());
            }
            prefix = prefix.substring(2);
        }
    }

    private void exportNode(AbstractNode node) throws Throwable {
        if (node != null) {
            System.out.print(prefix + "- " + node.getClass().getSimpleName());
            prefix += "  ";

            if (node instanceof Meta) {
                exportMeta((Meta) node);
            } else if (node instanceof AbstractEntityNode) {
                exportEntityNode((AbstractEntityNode) node);
            } else if (node instanceof Period) {
                System.out.println(" ("+((Period) node).getIdentifier()+")");
                exportNode(((Period) node).getData());
            } else {
                System.out.println();
                Method[] methods = node.getClass().getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getReturnType().equals(Map.class) && method.getParameterTypes().length == 0) {
                        Map<String, AbstractNode> nodes = (Map<String, AbstractNode>) method.invoke(node);
                        if (nodes != null) {
                            exportNodes(nodes);
                        }
                    }
                }
            }
            // System.out.print(prefix + " - " +
            // node.getClass().getSimpleName());
            // if (node instanceof AbstractEntityNode) {
            // AbstractEntityNode eNode = (AbstractEntityNode) node;
            // System.out.print(" (id=" + eNode.getObjectIdSender() + "; label="
            // + eNode.getLabel() + ")");
            // }
            // System.out.println();
            //
            // Method[] methods = node.getClass().getDeclaredMethods();
            // for (Method method : methods) {
            // String methodName = method.getName();
            // if (methodName.startsWith("getListOf")) {
            // @SuppressWarnings("unchecked")
            // Map<String, AbstractNode> nodes = (Map<String, AbstractNode>)
            // method.invoke(node);
            // if (nodes != null) {
            // exportNodes(nodes.values());
            // }
            // }
            // }
            prefix = prefix.substring(2);
        }
    }

    private void exportEntityNode(AbstractEntityNode node) throws Throwable {
        System.out.print(" (");
        if (node instanceof LeasedUnit) {
            System.out.print("hash=" + ((LeasedUnit) node).getHash() + ", ");
        } else if (node instanceof Unit) {
            System.out.print("hash=" + ((Unit) node).getHash() + ", ");
        }
        System.out.print("id=" + node.getObjectIdSender() + ", label=" + node.getLabel() + ")");

        System.out.println();
        Method[] methods = node.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getReturnType().equals(Map.class)) {
                Map<String, AbstractNode> nodes = (Map<String, AbstractNode>) method.invoke(node);
                if (nodes != null) {
                    exportNodes(nodes);
                }
            }
        }
    }

    private void exportNodes(Map<String, ? extends AbstractNode> nodes) throws Throwable {
        if (nodes != null && !nodes.isEmpty()) {
            prefix += "  ";

            // AbstractNode firstNode = nodes.iterator().next();
            // System.out.println(prefix + nodes.size() + "x " +
            // firstNode.getClass().getSimpleName() + ":");
            for (AbstractNode node : nodes.values()) {
                exportNode(node);
            }

            prefix = prefix.substring(2);
        }
    }

    protected void exportMeta(Meta meta) {
        if (meta != null) {
            System.out.println();
            System.out.println(prefix + "  format    = " + meta.getFormat());
            System.out.println(prefix + "  version   = " + meta.getVersion());
            System.out.println(prefix + "  creator   = " + meta.getCreator());
            System.out.println(prefix + "  publisher = " + meta.getPublisher());
            System.out.println(prefix + "  created   = " + meta.getCreated());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.icred.plugin.output.ExportPlugin#load(eu.icred
     * .plugin.PluginConfiguration, eu.icred.model.node.AbstractContainer)
     */
    @Override
    public void load(ExportWorkerConfiguration config, Container container) {
        this.container = container;
        doExport();
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.icred.plugin.output.ExportPlugin#unload()
     */
    @Override
    public void unload() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.icred.plugin.output.ExportPlugin#getConfigGui()
     */
    @Override
    public PluginComponent<ExportWorkerConfiguration> getConfigGui() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.icred.plugin.IPlugin#load(eu.icred.plugin.
     * PluginConfiguration)
     */
    @Override
    public void load(WorkerConfiguration config) {
        throw new RuntimeException("not allowed");
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.icred.plugin.output.IExportPlugin#
     * getRequiredConfigurationArguments()
     */
    @Override
    public ExportWorkerConfiguration getRequiredConfigurationArguments() {
        return new ExportWorkerConfiguration();
    }
}
