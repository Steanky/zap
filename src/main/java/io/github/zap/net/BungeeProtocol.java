package io.github.zap.net;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;

//TODO: implement bungeecord protocol
public final class BungeeProtocol implements MessageProtocol {
    @Getter
    private static final BungeeProtocol instance = new BungeeProtocol();

    private BungeeProtocol() { }

    @Override
    public ImmutablePair<Boolean, ImmutableMap<String, Object>> readFrom(ByteArrayDataInput input) {
        return null;
    }

    @Override
    public void writeTo(ByteArrayDataOutput output, ImmutableMap<String, Object> metadata, byte[] message) {

    }
}
