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
            long[][] X = new long[records.size()][];
            int[] y = new int[records.size()];

            for (int i = 0; i < records.size(); i++) {
                CSVRecord record = records.get(i);
                long[] intervals = X[i] = new long[record.size() - 1];
                y[i] = record.get(0).equals("ac") ? 0 : 1;
                for (int j = 1; j < record.size(); j++) {
                    intervals[j - 1] = Long.parseLong(record.get(j));
                }
            }

            classifier = new KNeighborsClassifier(3, 2, X, y);
        } else {
            throw new IllegalArgumentException("No classifier data was provided in the given file!");
        }
    }

    public boolean isUsingAutoClicker(long[] X) {
        return classifier.predict(X) == 0;
    }

}
