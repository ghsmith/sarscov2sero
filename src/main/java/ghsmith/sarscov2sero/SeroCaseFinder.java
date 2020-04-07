package ghsmith.sarscov2sero;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author Geoffrey H. Smith, MD
 */
public class SeroCaseFinder {

    File file;
    int daysAfterPcrToUseIfNull;
    List<SeroCase> seroCases;

    public SeroCaseFinder(File file, int daysAfterPcValueToUseIfNull) {
        this.file = file;
        this.daysAfterPcrToUseIfNull = daysAfterPcValueToUseIfNull;
    }
    
    public List<SeroCase> getAll() throws IOException {
        if(seroCases == null) {
            seroCases = new ArrayList<>();
            CSVParser csvParser = CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.DEFAULT.withFirstRecordAsHeader());
            for(CSVRecord csvRecord : csvParser) {
                SeroCase seroCase = new SeroCase();
                seroCases.add(seroCase);
                seroCase.caseId = csvRecord.get("caseId");
                seroCase.daysAfterPcr = csvRecord.get("daysAfterPcr") != null && csvRecord.get("daysAfterPcr").length() > 0 ? Integer.valueOf(csvRecord.get("daysAfterPcr")) : daysAfterPcrToUseIfNull;
                seroCase.standardResult = csvRecord.get("standardResult");
                seroCase.ourResult = Float.valueOf(csvRecord.get("ourResult"));
            }
        }
        return seroCases;
    }
    
}
