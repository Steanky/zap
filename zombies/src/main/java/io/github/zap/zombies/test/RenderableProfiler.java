package io.github.zap.zombies.test;

import io.github.zap.arenaapi.particle.*;
import io.github.zap.arenaapi.util.VectorUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class RenderableProfiler {
    private static final int ITERATIONS = 100;
    private static final int STEP = 1;
    private static final int RENDERABLES = 10;
    private static int length = 1;
    private static int density = 2;

    private static class TestRenderable extends CachingRenderable {
        @Override
        public FragmentData[] draw(FragmentData[] fragmentData) {
            return generateFrags(length, density);
        }
    }

    public static void start() {
        Renderer renderer = new SimpleRenderer(Bukkit.getWorld("world"), 0, 10);
        StopWatch timer = new StopWatch();

        for(int i = 0; i < ITERATIONS; i++) {
            System.out.printf("Creating %s renderables with length = %s and density = %s...%n", RENDERABLES, length, density);

            timer.start();
            for(int j = 0; j < RENDERABLES; j++) {
                renderer.add(new TestRenderable());
            }
            timer.stop();

            System.out.printf("Created renderables. Time elapsed: %sns (%s ms)%n", timer.getNanoTime(), timer.getTime());
            timer.reset();

            System.out.println("Starting resize testing. Increasing size of every renderable.");

            length += STEP;

            timer.start();
            for(int j = 0; j < renderer.size(); j++) {
                renderer.get(j).update();
            }
            timer.stop();

            System.out.printf("All (%s) renderable lengths increased by %s. Time elapsed: %sns (%s ms)%n",
                    renderer.size(), STEP, timer.getNanoTime(), timer.getTime());

            timer.reset();
        }

    }

    public static FragmentData[] generateFrags(int length, int density) {
        return FragmentData.of(VectorUtils.interpolateBounds(BoundingBox.of(new Vector(0, 0, 0),
                new Vector(length, length, length)), density), Particle.CRIT, 1, null);
    }
}
