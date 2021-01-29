package io.github.zap.zombies.command.mapeditor;

import io.github.zap.arenaapi.particle.RenderableProvider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an object that can be created/viewed/edited via the mapeditor.
 */
public abstract class EditorFocus implements RenderableProvider {
    private List<EditableField> cache = null;

    public List<EditableField> getEditableFields() {
        if(cache == null) {
            cache = new ArrayList<>();

            for(Field field : this.getClass().getFields()) {
                EditorSettable annotation = field.getAnnotation(EditorSettable.class);

                if(annotation != null) {
                    cache.add(new EditableField(field, annotation));
                }
            }
        }

        return cache;
    }
}
