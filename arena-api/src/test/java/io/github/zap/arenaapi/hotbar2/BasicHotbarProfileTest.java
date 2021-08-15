package io.github.zap.arenaapi.hotbar2;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
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
        public boolean isSelected() {
            return false;
        }

        @Override
        public void setSelected(@NotNull PlayerItemHeldEvent event) {

        }

        @Override
        public void setDeselected(@NotNull PlayerItemHeldEvent event) {

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
    void testOutOfBoundsPut() {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> profile.putObject(new MockHotbarObject(), 9));
    }

    @Test
    void testFullCapacity() {
        for(int i = 0; i < 9; i++) {
            HotbarObject object = new MockHotbarObject();
            profile.putObject(object, i);
            Assertions.assertEquals(profile.getObject(i).getSlot(), i);
            Assertions.assertSame(object, profile.getObject(i).getHotbarObject());
        }
    }

    @Test
    void deleteObjectInSlot() {
        HotbarObject object = new MockHotbarObject();
        profile.putObject(object, 0);
        profile.deleteObjectInSlot(0);
        Assertions.assertNull(profile.getObject(0).getHotbarObject());
    }

    @Test
    void swapObjects() {
        HotbarObject first = new MockHotbarObject();
        HotbarObject second = new MockHotbarObject();

        profile.putObject(first, 0);
        profile.putObject(second, 1);

        profile.swapObjects(0, 1);
        Assertions.assertSame(first, profile.getObject(1).getHotbarObject());
        Assertions.assertSame(second, profile.getObject(0).getHotbarObject());
    }

    @Test
    void getObjects() {
        HotbarObject first = new MockHotbarObject();
        HotbarObject second = new MockHotbarObject();

        profile.putObject(first, 0);
        profile.putObject(second, 8);

        HotbarObject.Slotted[] slotted = profile.getObjects();
        int i = 0;
        for(HotbarObject.Slotted slottedObject : slotted) {
            if(i > 0 && i < 8) {
                Assertions.assertNull(slottedObject.getHotbarObject());
            }
            else {
                Assertions.assertNotNull(slottedObject.getHotbarObject());
            }

            Assertions.assertEquals(i++, slottedObject.getSlot());
        }
    }

    @Test
    void indexOfExisting() {
        HotbarObject object = new MockHotbarObject();
        profile.putObject(object, 5);

        Assertions.assertEquals(5, profile.indexOf(object));
    }

    @Test
    void indexOfNotExisting() {
        HotbarObject object = new MockHotbarObject();
        profile.putObject(object, 5);

        Assertions.assertEquals(-1, profile.indexOf(new MockHotbarObject()));
    }

    @Test
    void getObject() {
        HotbarObject object = new MockHotbarObject();
        profile.putObject(object, 5);

        for(int i = 0; i < 9; i++) {
            if(i != 5) {
                Assertions.assertNull(profile.getObject(i).getHotbarObject());
            }
            else {
                Assertions.assertEquals(object, profile.getObject(i).getHotbarObject());
            }
        }
    }

    @Test
    void asGroupView() {
        HotbarGroupView groupView = profile.asGroupView();
        Assertions.assertNotNull(groupView);
        Assertions.assertSame(groupView, profile.asGroupView());
    }

    @Test
    void iterator() {
        HotbarObject first = new MockHotbarObject();
        HotbarObject second = new MockHotbarObject();
        HotbarObject third = new MockHotbarObject();

        profile.putObject(first, 0);
        profile.putObject(second, 3);
        profile.putObject(third, 8);

        int i = 0;
        for(HotbarObject.Slotted slotted : profile) {
            Assertions.assertNotNull(slotted.getHotbarObject());

            if(i == 0) {
                Assertions.assertEquals(0, slotted.getSlot());
                Assertions.assertSame(first, slotted.getHotbarObject());
            }
            else if(i == 3) {
                Assertions.assertEquals( 3, slotted.getSlot());
                Assertions.assertSame(second, slotted.getHotbarObject());
            }
            else if(i == 8) {
                Assertions.assertEquals(8, slotted.getSlot());
                Assertions.assertSame(third, slotted.getHotbarObject());
            }

            i++;
        }

        Assertions.assertEquals(3, i);
    }

    @Test
    void emptyIterator() {
        for(HotbarObject.Slotted slotted : profile) {
            Assertions.fail();
        }
    }
}
