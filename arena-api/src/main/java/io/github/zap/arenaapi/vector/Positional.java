package io.github.zap.arenaapi.vector;

import org.jetbrains.annotations.NotNull;

public interface Positional extends VectorAccess {
    @NotNull VectorAccess position();

    @Override
    default @NotNull VectorAccess copyVector() {
        return position().copyVector();
    }

    @Override
    default double x() {
        return position().x();
    }

    @Override
    default double y() {
        return position().y();
    }

    @Override
    default double z() {
        return position().z();
    }

    @Override
    default int chunkX() {
        return VectorAccess.super.chunkX();
    }

    @Override
    default @NotNull VectorAccess add(double x, double y, double z) {
        return position().add(x, y, z);
    }

    @Override
    default @NotNull VectorAccess subtract(double x, double y, double z) {
        return position().subtract(x, y, z);
    }

    @Override
    default @NotNull VectorAccess multiply(double x, double y, double z) {
        return position().multiply(x, y, z);
    }

    @Override
    default @NotNull VectorAccess divide(double x, double y, double z) {
        return position().divide(x, y, z);
    }
}
