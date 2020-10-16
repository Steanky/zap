package io.github.zap.net;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.apache.commons.lang3.tuple.ImmutablePair;


/**
 * Interface that defines how binary objects may be converted to and from byte arrays.
 */
public interface MessageProtocol {
    /**
     * Attempt to read the metadata of the object being transmitted, given the ByteArrayDataInput. This method should not
     * throw exceptions when provided with invalid input, rather, it should simply set the first parameter of the
     * returned pair to false.
     * @param input The input bytes
     * @return An ImmutablePair whose first parameter represents the success of the conversion and whose second
     * parameter contains metadata
     */
    ImmutablePair<Boolean, ImmutableMap<String, Object>> readFrom(ByteArrayDataInput input);

    /**
     * Writes the metadata and body data to the output.
     * @param output The output data stream
     * @param metadata The metadata that will be written to the output
     * @param body The bytes that will be written to the output
     */
    void writeTo(ByteArrayDataOutput output, ImmutableMap<String, Object> metadata, byte[] body);
}
