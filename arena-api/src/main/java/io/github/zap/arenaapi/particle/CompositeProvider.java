package io.github.zap.arenaapi.particle;

import org.bukkit.util.Vector;

/**
 * Vector provider that can represent any number of other VectorProviders.
 */
public class CompositeProvider implements VectorProvider {
    protected final VectorProvider[] providers;
    private final int[] lengths;

    private int length = -1;
    private int providerIndex = 0;
    private int fragmentIndex = 0;

    public CompositeProvider(int length) {
        this.providers = new VectorProvider[length];
        this.lengths = new int[length];
    }

    public CompositeProvider(VectorProvider... providers) {
        this.providers = providers;
        this.lengths = new int[providers.length];
    }

    @Override
    public int init() {
        if(length == -1) {
            length = 0;

            for (int i = 0; i < providers.length; i++) {
                VectorProvider provider = providers[i];
                int providerLength = provider.init();

                lengths[i] = providerLength;
                this.length += providerLength;
            }
        }

        return length;
    }

    @Override
    public Vector next() {
        VectorProvider currentProvider = providers[providerIndex];

        if(++fragmentIndex == lengths[providerIndex]) {
            providerIndex++;
            fragmentIndex = 0;
        }

        return currentProvider.next();
    }

    @Override
    public void reset() {
        for (VectorProvider provider : providers) {
            provider.reset();
        }

        providerIndex = 0;
        fragmentIndex = 0;

        length = -1;
    }
}
