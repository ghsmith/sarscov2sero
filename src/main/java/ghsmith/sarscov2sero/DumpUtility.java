package ghsmith.sarscov2sero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Geoffrey H. Smith, MD
 */
public class DumpUtility {

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {

        Properties priv = new Properties();
        try(InputStream inputStream = DumpUtility.class.getClassLoader().getResourceAsStream("private.properties")) {
            priv.load(inputStream);
        }
        
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection connCdw = DriverManager.getConnection(priv.getProperty("connCdw.url"), priv.getProperty("connCdw.u"), priv.getProperty("connCdw.p"));
        connCdw.setAutoCommit(false);
        connCdw.createStatement().execute("set role hnam_sel_all");
        
        SeroCaseFinder scf = new SeroCaseFinder(connCdw);
        
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = stdIn.readLine()) != null && line.length() != 0) {
            SeroCase seroCase = scf.getThinByLabelAccNo(line.substring(0, 12));
            if(seroCase != null) {
                System.out.println(String.format("%s,%s,%s,%tD %tR", line, seroCase.labelAccNo, seroCase.longAccNo, seroCase.collectionDt, seroCase.collectionDt));
            }
        }
        
    }
    
}
