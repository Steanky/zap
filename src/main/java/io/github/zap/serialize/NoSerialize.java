package io.github.zap.serialize;

/**
 * Specifies that a field should be excluded from serialization. All fields, excepting static ones, are serialized
 * by default.
 */
public @interface NoSerialize { }