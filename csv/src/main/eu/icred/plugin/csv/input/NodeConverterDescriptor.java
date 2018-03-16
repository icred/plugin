package eu.icred.plugin.csv.input;

import java.io.InputStream;

import eu.icred.deprecated.IConverterDescriptor;
import eu.icred.model.node.Container;

public class NodeConverterDescriptor implements IConverterDescriptor {
	private InputStream csvStream;
	private Container container;

	public InputStream getCsvStream() {
		return csvStream;
	}

	public void setCsvStream(InputStream csvStream) {
		this.csvStream = csvStream;
	}

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }
}
