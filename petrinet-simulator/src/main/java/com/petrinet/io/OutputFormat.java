package com.petrinet.io;

public final class OutputFormat {

    /** File format version */
    public static final String VERSION = "1.0";
    
    // File extension
    public static final String OUTPUT_FILE_EXTENSION = ".csv";

    // File name parsing
    public static final String FILE_EXTENSION_PATTERN = "\\.";
    public static final String OUTPUT_FILE_SEPARATOR = "_";

    // CSV format constants
    public static final String CSV_DELIMITER = ",";
    public static final String CSV_NEWLINE = "\n";
    public static final String CSV_HEADER_TRANSITION = "Transition";
    public static final String CSV_HEADER_TIME = "Time";

    private OutputFormat() {
        throw new AssertionError("Cannot instantiate output format constants class");
    }
}