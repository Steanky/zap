package io.github.zap.arenaapi.hotbar2;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BasicHotbarProfileTest {
    private static class MockHotbarObject implements HotbarObject {
        @Override
        public @Nullable ItemStack getStack() {
            return null;
        }

        @Override
        public void onPlayerInteract(@NotNull PlayerInteractEvent event) {

        }

        @Override
        public void cleanup() {

        }

        @Override
        public void onSelected() {

        }

        @Override
        public void onDeselected() {

        }
    }

    private BasicHotbarProfile profile;

    @BeforeEach
    void setUp() {
        profile = new BasicHotbarProfile();
    }

    @Test
    void putObject() {
        HotbarObject object = new MockHotbarObject();
        profile.putObject(object, 0);
        HotbarObject.Slotted slotted = profile.getObject(0);
        Assertions.assertEquals(0, slotted.getSlot());
        Assertions.assertSame(object, slotted.getHotbarObject());
    }

    @Test
    void testCapacity() {
        for(int i = 0; i < 9; i++) {
            profile.putObject(new MockHotbarObject(), i);
        }
    }

    @Test
    void deleteObjectInSlot() {
    }

    @Test
    void swapObjects() {
    }

    @Test
    void getObjects() {
    }

    @Test
    void indexOf() {
    }

    @Test
    void getObject() {
    }

    @Test
    void asGroupView() {
    }

    @Test
    void iterator() {
    }
}