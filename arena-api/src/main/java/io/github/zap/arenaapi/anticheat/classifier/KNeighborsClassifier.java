package io.github.zap.arenaapi.anticheat.classifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KNeighborsClassifier implements Classifier {

    private final int neighborCount;
    private final int classCount;
    private final long[][] X;
    private final int[] y;

    public KNeighborsClassifier(int neighborCount, int classCount, long[][] X, int[] y) {
        this.neighborCount = neighborCount;
        this.classCount = classCount;
        this.X = X;
        this.y = y;
    }

    @Override
    public int predict(long[] features) {
        int classIndex = 0;

        if (neighborCount == 1) {
            long minDistance = Long.MAX_VALUE;
            long distance;
            for (int i = 0; i < y.length; i++) {
                distance = distanceSquared(X[i], features);
                if (distance <= minDistance) {
                    minDistance = distance;
                    classIndex = y[i];
                }
            }
        } else {
            int[] classes = new int[classCount];

            List<Neighbor> distances = new ArrayList<>();
            for (int i = 0; i < y.length; i++) {
                distances.add(new Neighbor(y[i], distanceSquared(X[i], features)));
            }
            distances.sort(Comparator.comparing(neighbor -> neighbor.distance));

            for (Neighbor neighbor : distances.subList(0, neighborCount)) {
                classes[neighbor.clazz]++;
            }

            for (int i = 0; i < classCount; i++) {
                classIndex = classes[i] > classes[classIndex] ? i : classIndex;
            }
        }

        return classIndex;
    }

    private static long distanceSquared(long[] a, long[] b) {
        long distance = 0L;
        for (int i = 0, l = a.length; i < l; i++) {
            long dr = Math.abs(a[i] - b[i]);
            distance += dr * dr;
        }

        return distance;
    }


    private static class Neighbor {

        private final int clazz;

        private final long distance;

        public Neighbor(int clazz, long distance) {
            this.clazz = clazz;
            this.distance = distance;
        }

    }

}
