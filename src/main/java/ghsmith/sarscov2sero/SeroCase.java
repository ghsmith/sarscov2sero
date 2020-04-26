package ghsmith.sarscov2sero;

import java.sql.Date;

/**
 *
 * @author Geoffrey H. Smith, MD
 */
public class SeroCase {

    public String longAccNo;
    public String labelAccNo;
    public Date collectionDt;
    public Date pcrCollectionDt;
    public String pcrResult;

    public String caseId;
    public int daysAfterPcr;
    public String standardResult;
    public Float ourResult;
    public Float ourResultSdAboveNegPoolMean;
    
}
