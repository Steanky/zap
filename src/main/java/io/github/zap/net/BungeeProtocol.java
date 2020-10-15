package io.github.zap.net;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


public final class BungeeProtocol implements MessageProtocol {
    @Getter
    private static final BungeeProtocol instance = new BungeeProtocol();

    private BungeeProtocol() { }

    @Override
    public ImmutablePair<Boolean, MessageMetadata> readFrom(ByteArrayDataInput input) {
        //TODO: implement bungeecord protocol
        throw new NotImplementedException();
    }

    @Override
    public void writeTo(ByteArrayDataOutput output, MessageMetadata metadata, byte[] message) {
        //TODO: implement bungeecord protocol
        throw new NotImplementedException();
    }
}
