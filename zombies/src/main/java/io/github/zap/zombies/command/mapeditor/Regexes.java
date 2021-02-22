package io.github.zap.zombies.command.mapeditor;

public final class Regexes {
    public static String OBJECT_NAME = "^([a-zA-Z0-9_ ]+)$";
    public static String BOOLEAN = "^((true)|(false))$";
    public static String NON_NEGATIVE_INTEGER = "^(\\d+)$";
    public static String INTEGER = "^(-?\\d+)$";
    public static String DOUBLE = "^(-?\\d+(\\.\\d+)?)$";
    public static String STRING_LIST = "^([a-zA-Z0-9_ ]+,?)+([a-zA-Z0-9_ ]+)$";
}
