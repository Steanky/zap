package io.github.zap.arenaapi.anticheat.classifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KNeighborsClassifier implements Classifier {

    private final int neighborCount;
    private final int classCount;
    private final double[][] X;
    private final int[] y;

    public KNeighborsClassifier(int neighborCount, int classCount, double[][] X, int[] y) {
        this.neighborCount = neighborCount;
        this.classCount = classCount;
        this.X = X;
        this.y = y;
    }

    @Override
    public int predict(double[] features) {
        int classIdx = 0;

        if (neighborCount == 1) {
            double minDist = Double.POSITIVE_INFINITY;
            double curDist;
            for (int i = 0; i < y.length; i++) {
                curDist = compute(X[i], features);
                if (curDist <= minDist) {
                    minDist = curDist;
                    classIdx = y[i];
                }
            }
        } else {
            int[] classes = new int[classCount];

            List<Neighbor> dists = new ArrayList<>();
            for (int i = 0; i < y.length; i++) {
                dists.add(new Neighbor(y[i], compute(X[i], features)));
            }
            dists.sort(Comparator.comparing(neighbor -> neighbor.dist));

            for (Neighbor neighbor : dists.subList(0, neighborCount)) {
                classes[neighbor.clazz]++;
            }

            for (int i = 0; i < classCount; i++) {
                classIdx = classes[i] > classes[classIdx] ? i : classIdx;
            }
        }

        return classIdx;
    }

    private static double compute(double[] temp, double[] cand) {
        double dist = 0D;
        for (int i = 0, l = temp.length; i < l; i++) {
            double diff = Math.abs(temp[i] - cand[i]);
            dist += diff * diff;
        }

        return Math.sqrt(dist);
    }


    private static class Neighbor {

        private final int clazz;

        private final double dist;

        public Neighbor(int clazz, double dist) {
            this.clazz = clazz;
            this.dist = dist;
        }

    }

}
