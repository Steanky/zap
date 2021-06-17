package io.github.zap.arenaapi.anticheat;

import io.github.zap.arenaapi.anticheat.classifier.Classifier;
import io.github.zap.arenaapi.anticheat.classifier.KNeighborsClassifier;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class AntiCheat {

    private final Classifier classifier;

    public AntiCheat(Reader classifierData) throws IOException {
        List<CSVRecord> records = CSVFormat.DEFAULT.parse(classifierData).getRecords();

        if (!records.isEmpty()) {
            double[][] X = new double[records.size()][];
            int[] y = new int[records.size()];

            for (int i = 0; i < records.size(); i++) {
                CSVRecord record = records.get(i);
                double[] intervals = X[i] = new double[record.size() - 1];
                y[i] = record.get(0).equals("ac") ? 0 : 1;
                for (int j = 1; j < record.size(); j++) {
                    intervals[j - 1] = Double.parseDouble(record.get(j));
                }
            }

            classifier = new KNeighborsClassifier(3, 2, X, y);
        } else {
            throw new IllegalArgumentException("No classifier data was provided in the given file!");
        }
    }

    public boolean isUsingAutoClicker(long[] X) {
        double[] array = new double[X.length];
        for (int i = 0; i < X.length; i++) {
            array[i] = X[i];
        }

        return classifier.predict(array) == 0;
    }

}
