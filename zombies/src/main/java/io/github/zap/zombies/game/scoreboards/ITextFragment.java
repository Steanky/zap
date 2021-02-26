package io.github.zap.zombies.game.scoreboards;

public interface ITextFragment{
    String getComputedText();
    Iterable<ITextWriter> getWriters();
    void addWriter(ITextWriter writer);
    void removeWriter(ITextWriter writer);

}