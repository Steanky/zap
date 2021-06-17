package io.github.zap.arenaapi.anticheat.classifier;

public interface Classifier {

    int predict(double[] features);

}
