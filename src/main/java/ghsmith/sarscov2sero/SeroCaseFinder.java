package ghsmith.sarscov2sero;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    
    Connection conn;
    PreparedStatement pstmt0;

    public SeroCaseFinder(File file, int daysAfterPcValueToUseIfNull) {
        this.file = file;
        this.daysAfterPcrToUseIfNull = daysAfterPcValueToUseIfNull;
    }
    
    public SeroCaseFinder(Connection conn) throws SQLException {
        
        this.conn = conn;
        
        pstmt0 = conn.prepareStatement(
              "select "
            + "  long_acc_no, "
            + "  label_acc_no, "
            + "  collect_dt, "
            + "  ( "
            + "    select "
            + "      max(specimen_collect_dt) keep(dense_rank first order by result_lab_tval desc, specimen_collect_dt asc) "
            + "    from "
            + "      ehcvw.fact_result_lab "
            + "    where "
            + "      structured_result_type_key in (75988, 76063) "
            + "      and patient_key in (select patient_key from ehcvw.lkp_patient where empi_nbr = (select empi_nbr from ehcvw.lkp_patient where patient_key = frl.patient_key)) "
            + "  ) pcr_collect_dt, "
            + "  ( "
            + "    select "
            + "      max(result_lab_tval) keep(dense_rank first order by result_lab_tval desc, specimen_collect_dt asc) "
            + "    from "
            + "      ehcvw.fact_result_lab "
            + "    where "
            + "      structured_result_type_key in (75988, 76063) "
            + "      and patient_key in (select patient_key from ehcvw.lkp_patient where empi_nbr = (select empi_nbr from ehcvw.lkp_patient where patient_key = frl.patient_key)) "
            + "  ) pcr_result_lab_tval "
            + "from "
            + "  ( "
            + "    select "
            + "      accession_nbr long_acc_no, "
            + "      substr(accession_nbr, 3, 3) || substr(accession_nbr, 9, 1) || substr(accession_nbr, 10, 3) || substr(accession_nbr, 14, 5) label_acc_no, "
            + "      min(specimen_collect_dt) collect_dt, "
            + "      min(patient_key) patient_key "
            + "    from "
            + "      ehcvw.fact_result_lab "
            + "    where "
            + "      accession_nbr = ? "
            + "    group by "
            + "      accession_nbr "
            + "  ) frl "
        );

    }
    
    public List<SeroCase> getAll() throws IOException {
        if(seroCases == null) {
            seroCases = new ArrayList<>();
            float negPoolMean = 0;
            float negPoolSd = 0;
            CSVParser csvParser = CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.DEFAULT.withFirstRecordAsHeader());
            for(CSVRecord csvRecord : csvParser) {
                if(csvRecord.get("run") != null && csvRecord.get("run").length() > 0) {
                    negPoolMean = Float.valueOf(csvRecord.get("negpool_mean"));
                    negPoolSd = Float.valueOf(csvRecord.get("negpool_sd"));
                    continue;
                }
                if(csvRecord.get("censored (reason)") != null && csvRecord.get("censored (reason)").length() > 0) { continue; }
                SeroCase seroCase = new SeroCase();
                seroCases.add(seroCase);
                seroCase.caseId = csvRecord.get("caseId");
                seroCase.daysAfterPcr = csvRecord.get("daysAfterPcr") != null && csvRecord.get("daysAfterPcr").length() > 0 ? Integer.valueOf(csvRecord.get("daysAfterPcr")) : daysAfterPcrToUseIfNull;
                seroCase.standardResult = csvRecord.get("standardResult");
                seroCase.ourResult = Float.valueOf(csvRecord.get("ourResult"));
                seroCase.ourResultSdAboveNegPoolMean = (seroCase.ourResult - negPoolMean) / negPoolSd;
            }
        }
        return seroCases;
    }
    
    public SeroCase getThinByLabelAccNo(String labelAccNo) throws SQLException {
        SeroCase seroCase = null;
        String longAccNo;
        if(Integer.valueOf(labelAccNo.substring(3, 4)) > 5) {
            longAccNo = "00" + labelAccNo.substring(0, 3) + "201" + labelAccNo.substring(3, 7) + "0" + labelAccNo.substring(7);
        }
        else {
            longAccNo = "00" + labelAccNo.substring(0, 3) + "202" + labelAccNo.substring(3, 7) + "0" + labelAccNo.substring(7);
        }
        pstmt0.setString(1, longAccNo);
        ResultSet rs = pstmt0.executeQuery();
        if(rs.next()) {
            seroCase = new SeroCase();
            seroCase.longAccNo = rs.getString("long_acc_no");
            seroCase.labelAccNo = rs.getString("label_acc_no");
            seroCase.collectionDt = rs.getDate("collect_dt");
            seroCase.pcrCollectionDt = rs.getDate("pcr_collect_dt");
            seroCase.pcrResult = rs.getString("pcr_result_lab_tval");
        }
        rs.close();
        return seroCase;
    }
    
}
