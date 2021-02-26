package io.github.zap.zombies.game.scoreboards;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Represent a segment of string to render in SidebarTextWriter
 */
public class TextFragment implements ITextFragment {
    @Getter
    public String value;

    private Set<ITextWriter> writers = new HashSet<>();

    /**
     * Create a new instance of TextFragment
     * @param value the initial value
     */
    public TextFragment(String value) {
        this.value = value;
    }

    /**
     * Create a new instance of TextFragment
     */
    public TextFragment() {
        this("");
    }

    /**
     * Set a new value of this TextFragment
     * @param newValue a string represent the new value to set
     */
    public void setValue(String newValue) {
        if(!getValue().equals(newValue)) {
            value = newValue;
             getWriters().forEach(x -> x.onTextFragmentChanged(this));
        }
    }

    /**
     * Get the computed text
     */
    @Override
    public String getComputedText() {
        return getValue();
    }

    @Override
    public Iterable<ITextWriter> getWriters() {
        return writers;
    }

    @Override
    public void addWriter(ITextWriter writer) {
        writers.add(writer);
    }

    @Override
    public void removeWriter(ITextWriter writer) {
        writers.remove(writer);
    }
}
