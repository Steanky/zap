package io.github.zap.arenaapi.particle;

import org.bukkit.util.Vector;

/**
 * Vector provider that can represent any number of other VectorProviders.
 */
public class CompositeProvider implements VectorProvider {
    protected final VectorProvider[] providers;
    private final int[] lengths;

    private int length = -1;
    private int i = 0;
    private int j = 0;

    public CompositeProvider(int length) {
        this.providers = new VectorProvider[length];
        this.lengths = new int[length];
    }

    @Override
    public int init() {
        if(length == -1) {
            length = 0;

            for (int i = 0; i < providers.length; i++) {
                VectorProvider provider = providers[i];
                int length = provider.init();

                lengths[i] = length;
                this.length += length;
            }
        }

        return length;
    }

    @Override
    public Vector next() {
        VectorProvider current = providers[i];
        Vector vector = current.next();

        if(++j == lengths[i]) {
            i++;
            j = 0;
        }

        return vector;
    }

    @Override
    public void reset() {
        for (VectorProvider provider : providers) {
            provider.reset();
        }

        i = 0;
        j = 0;

        length = -1;
    }
}
