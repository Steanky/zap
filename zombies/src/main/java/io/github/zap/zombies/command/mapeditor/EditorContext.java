package io.github.zap.zombies.command.mapeditor;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.particle.*;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class EditorContext implements Disposable {
    private static final Vector UNIT = new Vector(1, 1, 1);
    public enum Renderables {
        SELECTION(0),
        MAP(1),
        ROOMS(2),
        WINDOWS(3);

        @Getter
        private final int index;

        Renderables(int index) {
            this.index = index;
        }
    }

    private static final VectorProvider[] EMPTY_VECTOR_PROVIDER_ARRAY = new VectorProvider[0];

    private static final Shader SELECTION_SHADER = new SolidShader(Particle.REDSTONE, 1,
            new Particle.DustOptions(Color.GREEN, 1));

    private static final Shader MAP_BOUNDS_SHADER = new SolidShader(Particle.REDSTONE, 1,
            new Particle.DustOptions(Color.RED, 2));

    private static final Shader ROOM_SHADER = new SolidShader(Particle.REDSTONE, 1,
            new Particle.DustOptions(Color.WHITE, 1));

    private class SelectionRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return SELECTION_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            return firstClicked == null ? VectorProvider.EMPTY : new Cube(getSelection(), 1);
        }
    }

    private class MapRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return MAP_BOUNDS_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            return map == null ? VectorProvider.EMPTY : new Cube(map.getMapBounds(), 0.25);
        }
    }

    private class RoomRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return ROOM_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            if(map != null && map.getRooms().size() > 0) {
                List<VectorProvider> vectorProviders = new ArrayList<>();

                for(RoomData room : map.getRooms()) {
                    for(BoundingBox boundingBox : room.getBounds().getBounds()) {
                        vectorProviders.add(new Cube(boundingBox, 0.5));
                    }
                }

                return new CompositeProvider(vectorProviders.toArray(EMPTY_VECTOR_PROVIDER_ARRAY));
            }
            else {
                return VectorProvider.EMPTY;
            }
        }
    }

    private final List<Renderable> renderables = new ArrayList<>();

    @Getter
    private final Player player;

    @Getter
    private MapData map;

    private Vector firstClicked = null;
    private Vector secondClicked = null;

    private final Renderer renderer;

    public EditorContext(Player player) {
        this.player = player;

        renderer = new SimpleRenderer(player.getWorld(), 0, 5);
        addRenderable(new SelectionRenderable());
        addRenderable(new MapRenderable());
        addRenderable(new RoomRenderable());
        renderer.start();
    }

    private void addRenderable(Renderable renderable) {
        renderer.add(renderable);
        renderables.add(renderable);
    }

    public Renderable getRenderable(Renderables renderable) {
        return renderables.get(renderable.index);
    }

    public void setMap(MapData map) {
        this.map = map;

        for(Renderable renderable : renderables) {
            renderable.update();
        }
    }

    public void handleClicked(Block at) {
        Vector clickedVector = at.getLocation().toVector();

        if(firstClicked == null && secondClicked == null) {
            firstClicked = clickedVector;
        }
        else if(firstClicked != null && secondClicked == null) {
            secondClicked = clickedVector;
        }
        else if(firstClicked != null) {
            firstClicked = secondClicked;
            secondClicked = clickedVector;
        }

        renderables.get(Renderables.SELECTION.index).update();
    }

    public BoundingBox getSelection() {
        if(firstClicked != null && secondClicked != null) {
            return BoundingBox.of(firstClicked, secondClicked).expandDirectional(UNIT);
        }
        else if(firstClicked != null) {
            return BoundingBox.of(firstClicked, firstClicked).expandDirectional(UNIT);
        }

        return null;
    }

    @Override
    public void dispose() {
        renderer.stop();
    }
}