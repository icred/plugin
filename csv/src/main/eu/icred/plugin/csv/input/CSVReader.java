package eu.icred.plugin.csv.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.icred.model.node.AbstractNode;

/**
 * reader for a csv file
 * 
 * @author phoudek
 * 
 * @param <NodeType>
 *            type of AbstractNode
 */
//TODO: performanceoptimierbar, da für jede Dateizeile mehrmals eine Schleife läuft
class CSVReader<NodeType extends AbstractNode> {
    private static Logger logger = Logger.getLogger(CSVReader.class);

    private List<String> headerData = null;
    private BufferedReader reader;
    private Class<NodeType> type;

    private long lineCounter = 0;

    private final String seperateChar = ";(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))";

    public CSVReader(Class<NodeType> type, InputStream stream) throws IOException, Exception {
        this.type = type;
        this.reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));

        initialize();
    }

    private void initialize() throws IOException, Exception {
        headerData = readLineAsList();

        if (headerData == null || headerData.isEmpty()) {
            throw new Error("empty input file");
        }
    }

    /**
     * reads a csv line
     * 
     * @return csv line object of type <NodeType>
     * @throws Exception
     */
    public CSVLine<NodeType> readLine() throws Exception {
        // read csv as List:
        List<String> csvData = readLineAsList();
        if (csvData == null || csvData.isEmpty()) {
            return null;
        }

        // convert to CSVLine-Object
        CSVLine<NodeType> result = new CSVLine<NodeType>(type, lineCounter++);
        for (int i = 0; i < headerData.size(); i++) {
            String csvHead = headerData.get(i);
            String csvValue = (i < csvData.size() ? csvData.get(i) : "");

            result.put(csvHead, csvValue);
        }

        return result;
    }

    /**
     * reads a csv line as {@link List} of {@link String}
     * 
     * @return {@link List} of {@link String}
     * @throws IOException
     */
    protected List<String> readLineAsList() throws IOException {
        String fullMultiLine = null;
        String curLine;
        do {
            curLine = this.reader.readLine();
            if (curLine != null) {
                if (fullMultiLine == null) {
                    fullMultiLine = curLine;
                } else {
                    fullMultiLine = fullMultiLine + "\r\n" + curLine;
                }
            }
        } while (!isLineCompleted(fullMultiLine));
        if ((fullMultiLine == null) || (fullMultiLine.isEmpty())) {
            return null;
        }

        List<String> result = new ArrayList<String>();

        String[] curData = fullMultiLine.split(this.seperateChar);
        for (int i = 0; i < curData.length; i++) {
            String value = curData[i];

            // remove enclosing chars
            if ((value.startsWith("\"")) && (value.endsWith("\""))) {
                value = value.substring(1, value.length() - 1);
            }
            // remove double quotes
            value = value.replaceAll("\"\"", "\"");

            result.add(value);
        }

        return result;
    }

    /**
     * prüft ob ein Datensatz abgeschlossen oder mehrzeilig ist
     * @param line aktueller Datensatz
     * @return Ergebnis
     */
    protected boolean isLineCompleted(String line) {
        if (line == null)
            return true;
        
        boolean completed = true;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '"') {
                completed = !completed;
            }
        }
        return completed;
    }
}
