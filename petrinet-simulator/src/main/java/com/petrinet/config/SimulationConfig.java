package com.petrinet.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Unified configuration with support for:
 * 1. Built-in defaults
 * 2. Properties file overrides
 * 3. CLI argument overrides (highest priority)
 */
@Slf4j
@Getter
@Setter
public class SimulationConfig {

    // --- Defaults ---
    private static final int DEFAULT_SIMULATION_RUNS = 1;
    private static final boolean DEFAULT_VERBOSE = false;
    private static final OutputMode DEFAULT_OUTPUT_MODE = OutputMode.COMPACT;
    private static final String DEFAULT_FULL_STATE_SUFFIX = "_full";

    // --- Property keys ---
    public static final String PROP_SIMULATION_RUNS = "simulation.runs";
    public static final String PROP_VERBOSE = "simulation.verbose";
    public static final String PROP_OUTPUT_MODE = "output.mode";
    public static final String PROP_OUTPUT_FILE = "output.file";
    public static final String PROP_OUTPUT_FULL_STATE_SUFFIX = "output.full-state.suffix";
    public static final String PROP_INPUT_FILE = "input.file";

    // --- Configuration values ---
    private int simulationRuns = DEFAULT_SIMULATION_RUNS;
    private boolean verbose = DEFAULT_VERBOSE;
    private OutputMode outputMode = DEFAULT_OUTPUT_MODE;
    private String inputFile;
    private String outputFile;
    private String fullStateSuffix = DEFAULT_FULL_STATE_SUFFIX;

    /**
     * Creates configuration with built-in defaults only.
     */
    public SimulationConfig() {
    }

    /**
     * Loads configuration from a properties file path.
     * Values not specified in file retain their defaults.
     */
    public static SimulationConfig fromFile(Path configPath) throws IOException {
        SimulationConfig config = new SimulationConfig();

        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(configPath)) {
            props.load(is);
        }

        config.applyProperties(props);
        log.debug("Loaded configuration from: {}", configPath);
        return config;
    }

    /**
     * Applies properties, leaving unspecified values unchanged.
     */
    private void applyProperties(Properties props) {
        if (props.containsKey(PROP_SIMULATION_RUNS)) {
            this.simulationRuns = Integer.parseInt(props.getProperty(PROP_SIMULATION_RUNS));
        }
        if (props.containsKey(PROP_VERBOSE)) {
            this.verbose = Boolean.parseBoolean(props.getProperty(PROP_VERBOSE));
        }
        if (props.containsKey(PROP_OUTPUT_MODE)) {
            this.outputMode = OutputMode.fromString(props.getProperty(PROP_OUTPUT_MODE));
        }
        if (props.containsKey(PROP_INPUT_FILE)) {
            this.inputFile = props.getProperty(PROP_INPUT_FILE);
        }
        if (props.containsKey(PROP_OUTPUT_FILE)) {
            this.outputFile = props.getProperty(PROP_OUTPUT_FILE);
        }
        if (props.containsKey(PROP_OUTPUT_FULL_STATE_SUFFIX)) {
            this.fullStateSuffix = props.getProperty(PROP_OUTPUT_FULL_STATE_SUFFIX);
        }
    }

    /**
     * Generates the full-state output filename based on compact filename.
     * Example: "output.csv" -> "output_full.csv"
     */
    public String getFullStateOutputFile(String baseOutputFile) {
        int dotIndex = baseOutputFile.lastIndexOf('.');
        if (dotIndex > 0) {
            return baseOutputFile.substring(0, dotIndex) + fullStateSuffix + baseOutputFile.substring(dotIndex);
        }
        return baseOutputFile + fullStateSuffix;
    }

    public boolean shouldWriteCompact() {
        return outputMode == OutputMode.COMPACT || outputMode == OutputMode.BOTH;
    }

    public boolean shouldWriteFullState() {
        return outputMode == OutputMode.FULL_STATE || outputMode == OutputMode.BOTH;
    }
}
