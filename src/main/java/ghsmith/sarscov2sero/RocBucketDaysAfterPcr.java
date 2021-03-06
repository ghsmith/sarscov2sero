package ghsmith.sarscov2sero;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffrey H. Smith, MD
 */
public class RocBucketDaysAfterPcr {
 
    static public void main(String args[]) throws IOException {
        
        SeroCaseFinder scf = new SeroCaseFinder(new File(args[0]), 99);
        List<SeroCase> seroCases = scf.getAll();

        List<Float> cutOffs = seroCases.stream().sorted(Comparator.comparingDouble(seroCase -> seroCase.ourResult)).map(seroCase -> seroCase.ourResult).collect(Collectors.toList());
        cutOffs.add(2.0f);
        cutOffs = cutOffs.stream().sorted().distinct().collect(Collectors.toList());

        int[] bucketBeginList = {0, 4, 7};
        int[] bucketEndList = {3, 6, 999};
        
        System.out.print(String.format("%s,",
            "cutOff"
        ));
        for(int b = 0; b < bucketBeginList.length; b++) {
            System.out.print(String.format("fpr-%d-%d,tpr-%d-%d,",
                bucketBeginList[b],
                bucketEndList[b],
                bucketBeginList[b],
                bucketEndList[b]
            ));
        }
        System.out.println();

        for(float cutOff : cutOffs) {
            final float finalCutOff = cutOff;
            System.out.print(String.format("%4.3f,",
                finalCutOff
            ));
            for(int b = 0; b < bucketBeginList.length; b++) {
                final int bucketBegin = bucketBeginList[b];
                final int bucketEnd = bucketEndList[b];
                long countStandardNonPos = seroCases.stream().filter(seroCase -> !seroCase.standardResult.equals("positive")).count();
                long countStandardNonPosGteCutoff = seroCases.stream().filter(seroCase -> !seroCase.standardResult.equals("positive") && seroCase.ourResult >= finalCutOff).count();
                long countStandardPos = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= bucketBegin && seroCase.daysAfterPcr <= bucketEnd && seroCase.standardResult.equals("positive")).count();
                long countStandardPosGteCutoff = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= bucketBegin && seroCase.daysAfterPcr <= bucketEnd && seroCase.standardResult.equals("positive") && seroCase.ourResult >= finalCutOff).count();
                System.out.print(String.format("%4.3f,%4.3f,",
                    (float)countStandardNonPosGteCutoff / countStandardNonPos,
                    (float)countStandardPosGteCutoff / countStandardPos
                ));
            }
            System.out.println();
        }

        System.out.print(String.format("%s,",
            "count"
        ));
        for(int b = 0; b < bucketBeginList.length; b++) {
            final int bucketBegin = bucketBeginList[b];
            final int bucketEnd = bucketEndList[b];
            long countStandardNonPos = seroCases.stream().filter(seroCase -> !seroCase.standardResult.equals("positive")).count();
            long countStandardPos = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= bucketBegin && seroCase.daysAfterPcr <= bucketEnd && seroCase.standardResult.equals("positive")).count();
            System.out.print(String.format("%d,%d,",
                countStandardNonPos,
                countStandardPos
            ));
        }
        System.out.println();

        System.out.print(String.format("%s,",
            "auc"
        ));
        for(int b = 0; b < bucketBeginList.length; b++) {
            final int bucketBegin = bucketBeginList[b];
            final int bucketEnd = bucketEndList[b];
            float auc = 0;
            float lastX = -1;
            float lastY = -1;
            for(float cutOff : cutOffs) {
                final float finalCutOff = cutOff;
                long countStandardNonPos = seroCases.stream().filter(seroCase -> !seroCase.standardResult.equals("positive")).count();
                long countStandardNonPosGteCutoff = seroCases.stream().filter(seroCase -> !seroCase.standardResult.equals("positive") && seroCase.ourResult >= finalCutOff).count();
                long countStandardPos = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= bucketBegin && seroCase.daysAfterPcr <= bucketEnd && seroCase.standardResult.equals("positive")).count();
                long countStandardPosGteCutoff = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= bucketBegin && seroCase.daysAfterPcr <= bucketEnd && seroCase.standardResult.equals("positive") && seroCase.ourResult >= finalCutOff).count();
                float x = (float)countStandardNonPosGteCutoff / countStandardNonPos;
                float y = (float)countStandardPosGteCutoff / countStandardPos;
                if(lastX != -1 && lastY != -1) {
                    auc += (lastX - x) * (y + ((lastY - y) / 2));
                }
                lastX = x;
                lastY = y;
            }
            System.out.print(String.format(",%4.3f,",
               auc
            ));
        }
        System.out.println();
        
    }
    
}
