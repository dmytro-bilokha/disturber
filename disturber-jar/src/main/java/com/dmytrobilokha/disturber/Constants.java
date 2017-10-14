package com.dmytrobilokha.disturber;

/**
 * The class contains application-wide constants
 */
public final class Constants {
    public static final String APPLICATION_NAME = "disturber";
    public static final String LOGFILE_PROPERTY_KEY = APPLICATION_NAME + ".logfile";
    public static final String CONFIG_DIR_PROPERTY_KEY = APPLICATION_NAME + ".configdir";
    public static final String PROPERTIES_FILE_NAME = APPLICATION_NAME + ".properties";
    public static final String APPLICATION_TITLE = "Disturber";
    public static final String ICONS_PATH = "/icons/";
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String NEW_LINE = System.lineSeparator();

    private Constants() {
        //We are not going to instantiate this class
    }
}
