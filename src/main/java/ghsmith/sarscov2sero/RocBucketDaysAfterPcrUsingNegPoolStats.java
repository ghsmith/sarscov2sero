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
public class RocBucketDaysAfterPcrUsingNegPoolStats {
 
    static public void main(String args[]) throws IOException {
        
        SeroCaseFinder scf = new SeroCaseFinder(new File(args[0]), 99);
        List<SeroCase> seroCases = scf.getAll();
        
        List<Float> cutOffs = seroCases.stream().sorted(Comparator.comparingDouble(seroCase -> seroCase.ourResultSdAboveNegPoolMean)).map(seroCase -> seroCase.ourResultSdAboveNegPoolMean).collect(Collectors.toList());
        cutOffs.add(1f);
        cutOffs.add(2f);
        cutOffs.add(3f);
        cutOffs.add(4f);
        cutOffs.add(5f);
        cutOffs.add(6f);
        cutOffs.add(7f);
        cutOffs.add(8f);
        cutOffs.add(9f);
        cutOffs.add(10f);
        cutOffs.add(1000f);
        cutOffs = cutOffs.stream().sorted().distinct().collect(Collectors.toList());
        
        //System.out.println(seroCases.stream().map(seroCase -> seroCase.ourResultSdAboveNegPoolMean).collect(Collectors.toList()).stream().min(Comparator.comparing(Float::valueOf)));
        //System.out.println(seroCases.stream().map(seroCase -> seroCase.ourResultSdAboveNegPoolMean).collect(Collectors.toList()).stream().max(Comparator.comparing(Float::valueOf)));
        //seroCases.stream().sorted(Comparator.comparingDouble(seroCase -> seroCase.ourResultSdAboveNegPoolMean)).map(seroCase -> seroCase.ourResultSdAboveNegPoolMean).collect(Collectors.toList());
        
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
            System.out.print(String.format("%3.2f,",
                finalCutOff
            ));
            for(int b = 0; b < bucketBeginList.length; b++) {
                final int bucketBegin = bucketBeginList[b];
                final int bucketEnd = bucketEndList[b];
                long countStandardNonPos = seroCases.stream().filter(seroCase -> !seroCase.standardResult.equals("positive")).count();
                long countStandardNonPosGteCutoff = seroCases.stream().filter(seroCase -> !seroCase.standardResult.equals("positive") && seroCase.ourResultSdAboveNegPoolMean >= finalCutOff).count();
                long countStandardPos = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= bucketBegin && seroCase.daysAfterPcr <= bucketEnd && seroCase.standardResult.equals("positive")).count();
                long countStandardPosGteCutoff = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= bucketBegin && seroCase.daysAfterPcr <= bucketEnd && seroCase.standardResult.equals("positive") && seroCase.ourResultSdAboveNegPoolMean >= finalCutOff).count();
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
            float lastX = -100;
            float lastY = -100;
            for(float cutOff : cutOffs) {
                final float finalCutOff = cutOff;
                long countStandardNonPos = seroCases.stream().filter(seroCase -> !seroCase.standardResult.equals("positive")).count();
                long countStandardNonPosGteCutoff = seroCases.stream().filter(seroCase -> !seroCase.standardResult.equals("positive") && seroCase.ourResultSdAboveNegPoolMean >= finalCutOff).count();
                long countStandardPos = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= bucketBegin && seroCase.daysAfterPcr <= bucketEnd && seroCase.standardResult.equals("positive")).count();
                long countStandardPosGteCutoff = seroCases.stream().filter(seroCase -> seroCase.daysAfterPcr >= bucketBegin && seroCase.daysAfterPcr <= bucketEnd && seroCase.standardResult.equals("positive") && seroCase.ourResultSdAboveNegPoolMean >= finalCutOff).count();
                float x = (float)countStandardNonPosGteCutoff / countStandardNonPos;
                float y = (float)countStandardPosGteCutoff / countStandardPos;
                if(lastX != -100 && lastY != -100) {
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
