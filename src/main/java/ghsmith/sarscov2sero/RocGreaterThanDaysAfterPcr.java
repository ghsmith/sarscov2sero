package ghsmith.sarscov2sero;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Geoffrey H. Smith, MD
 */
public class RocGreaterThanDaysAfterPcr {
 
    static public void main(String args[]) throws IOException {
        
        SeroCaseFinder scf = new SeroCaseFinder(new File(args[0]));
        List<SeroCase> seroCases = scf.getAll();
        
        System.out.print(String.format("%s,",
            "cutOff"
        ));
        for(int daysAfterPcrCutOff = 0; daysAfterPcrCutOff < 10; daysAfterPcrCutOff++) {
            System.out.print(String.format("fpr-%d,tpr-%d,",
                daysAfterPcrCutOff,
                daysAfterPcrCutOff
            ));
        }
        System.out.println();

        for(float cutOff = 0; cutOff < 1.4; cutOff+=0.01) {
            final float finalCutOff = cutOff;
            System.out.print(String.format("%3.2f,",
                finalCutOff
            ));
            for(int daysAfterPcrCutOff = 0; daysAfterPcrCutOff < 10; daysAfterPcrCutOff++) {
                final int finalDaysAfterPcrCutOff = daysAfterPcrCutOff;
                long countStandardNonPos = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= finalDaysAfterPcrCutOff && !seroCase.standardResult.equals("positive")).count();
                long countStandardNonPosGteCutoff = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= finalDaysAfterPcrCutOff && !seroCase.standardResult.equals("positive") && seroCase.ourResult >= finalCutOff).count();
                long countStandardPos = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= finalDaysAfterPcrCutOff && seroCase.standardResult.equals("positive")).count();
                long countStandardPosGteCutoff = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= finalDaysAfterPcrCutOff && seroCase.standardResult.equals("positive") && seroCase.ourResult >= finalCutOff).count();
                System.out.print(String.format("%3.2f,%3.2f,",
                    (float)countStandardNonPosGteCutoff / countStandardNonPos,
                    (float)countStandardPosGteCutoff / countStandardPos
                ));
            }
            System.out.println();
        }

        System.out.print(String.format("%s,",
            "count"
        ));
        for(int daysAfterPcrCutOff = 0; daysAfterPcrCutOff < 10; daysAfterPcrCutOff++) {
            final int finalDaysAfterPcrCutOff = daysAfterPcrCutOff;
            long countStandardNonPos = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= finalDaysAfterPcrCutOff && !seroCase.standardResult.equals("positive")).count();
            long countStandardPos = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= finalDaysAfterPcrCutOff && seroCase.standardResult.equals("positive")).count();
            System.out.print(String.format("%d,%d,",
                countStandardNonPos,
                countStandardPos
            ));
        }
        System.out.println();

        System.out.print(String.format("%s,",
            "auc"
        ));
        for(int daysAfterPcrCutOff = 0; daysAfterPcrCutOff < 10; daysAfterPcrCutOff++) {
            final int finalDaysAfterPcrCutOff = daysAfterPcrCutOff;
            float auc = 0;
            float lastX = -1;
            float lastY = -1;
            for(float cutOff = 0; cutOff < 1.4; cutOff+=0.01) {
                final float finalCutOff = cutOff;
                long countStandardNonPos = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= finalDaysAfterPcrCutOff && !seroCase.standardResult.equals("positive")).count();
                long countStandardNonPosGteCutoff = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= finalDaysAfterPcrCutOff && !seroCase.standardResult.equals("positive") && seroCase.ourResult >= finalCutOff).count();
                long countStandardPos = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= finalDaysAfterPcrCutOff && seroCase.standardResult.equals("positive")).count();
                long countStandardPosGteCutoff = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= finalDaysAfterPcrCutOff && seroCase.standardResult.equals("positive") && seroCase.ourResult >= finalCutOff).count();
                float x = (float)countStandardNonPosGteCutoff / countStandardNonPos;
                float y = (float)countStandardPosGteCutoff / countStandardPos;
                if(lastX != -1 && lastY != -1) {
                    auc += (lastX - x) * (y + ((lastY - y) / 2));
                }
                lastX = x;
                lastY = y;
            }
            System.out.print(String.format(",%3.2f,",
               auc
            ));
        }
        System.out.println();
        
    }
    
}
