package io.github.zap.arenaapi.pathfind.agent;

record PathAgentImpl(double x, double y, double z, double width, double height, double jumpHeight,
                     double fallTolerance) implements PathAgent {
    @Override
    public String toString() {
        return "PathAgentImpl{x=" + x + ", y=" + y + ", z=" + z + ", width=" + width + ", height=" + height +
                ", jumpHeight=" + jumpHeight + ", fallTolerance=" + fallTolerance + "}";
    }
}
