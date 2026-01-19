package com.petrinet.cli;

public final class CommandLineConstants {

    // --- Existing flags (legacy support) ---
    public static final String VERBOSE_FLAG = "-verbose";
    public static final String NUMERIC_PATTERN = "\\d+";

    // --- New flags ---
    public static final String CONFIG_FLAG = "-c";
    public static final String CONFIG_FLAG_LONG = "--config";
    public static final String INPUT_FLAG = "-i";
    public static final String INPUT_FLAG_LONG = "--input";
    public static final String OUTPUT_FLAG = "-o";
    public static final String OUTPUT_FLAG_LONG = "--output";
    public static final String RUNS_FLAG = "-r";
    public static final String RUNS_FLAG_LONG = "--runs";
    public static final String OUTPUT_MODE_FLAG = "-m";
    public static final String OUTPUT_MODE_FLAG_LONG = "--output-mode";
    public static final String VERBOSE_FLAG_LONG = "--verbose";

    // --- Usage messages ---
    public static final String USAGE_MESSAGE = """
        Usage: java -jar petrinet-simulator.jar [options]

        Options:
          -c, --config <file>       Configuration file (optional)
          -i, --input <file>        Input Petri Net file (required if not in config)
          -o, --output <file>       Output file base name (required if not in config)
          -r, --runs <number>       Number of simulation runs (default: 1)
          -m, --output-mode <mode>  Output mode: compact, full-state, both (default: compact)
          -verbose, --verbose       Enable verbose logging

        Legacy syntax (still supported):
          java -jar simulator.jar <inputFile> <outputFile> [runs] [-verbose]

        Examples:
          java -jar petrinet-simulator.jar -c config.properties
          java -jar petrinet-simulator.jar -i model.pn -o results.csv -r 10 --output-mode both
          java -jar petrinet-simulator.jar model.pn results.csv 10 -verbose
        """;

    private CommandLineConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }
}
