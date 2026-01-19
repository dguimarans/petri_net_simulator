package com.petrinet.cli;

import static com.petrinet.cli.CommandLineConstants.*;
import static com.petrinet.io.OutputFormat.OUTPUT_FILE_EXTENSION;
import static com.petrinet.io.OutputFormat.OUTPUT_FILE_SEPARATOR;

import java.nio.file.Paths;

import com.petrinet.config.OutputMode;
import com.petrinet.config.SimulationConfig;
import com.petrinet.engine.SimulationEngine;
import com.petrinet.io.PetriNetException;
import com.petrinet.io.PetriNetFileParser;
import com.petrinet.io.PetriNetFileParser.ParsedPetriNet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Simulate {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            throw new PetriNetException(USAGE_MESSAGE);
        }

        SimulationConfig config = parseArguments(args);
        validateConfig(config);

        ParsedPetriNet parsedPetriNet = PetriNetFileParser.parse(config.getInputFile());
        parsedPetriNet.simulationControl().setVerbose(config.isVerbose());

        // Main simulation loop
        for (int i = 1; i <= config.getSimulationRuns(); i++) {
            parsedPetriNet.petriNet().resetMarking();

            String baseOutputFile = buildOutputFileName(config.getOutputFile(), i);

            SimulationEngine simulation = new SimulationEngine(
                parsedPetriNet.petriNet(),
                baseOutputFile,
                parsedPetriNet.simulationControl(),
                config
            );
            simulation.run();

            if (config.isVerbose()) {
                log.info("---");
            }
            log.info("Simulation {} ended at time: {}", i, simulation.getSimulationTime());
            if (config.isVerbose()) {
                log.info("============");
            }
        }

        logOutputSummary(config);
    }

    /**
     * Parses CLI arguments with support for both legacy and new formats.
     * Priority: CLI args > config file > defaults
     */
    private static SimulationConfig parseArguments(String[] args) throws Exception {

        // Detect legacy format: first arg doesn't start with "-"
        if (args.length >= 2 && !args[0].startsWith("-")) {
            return parseLegacyArguments(args);
        }

        return parseNewArguments(args);
    }

    /**
     * Legacy format: inputFile outputFile [runs] [-verbose]
     */
    private static SimulationConfig parseLegacyArguments(String[] args) throws PetriNetException {
        if (args.length < 2) {
            throw new PetriNetException(USAGE_MESSAGE);
        }

        SimulationConfig config = new SimulationConfig();
        config.setInputFile(args[0]);
        config.setOutputFile(args[1]);
        config.setOutputMode(OutputMode.FULL_STATE); // Legacy default

        if (args.length > 2 && args[2].matches(NUMERIC_PATTERN)) {
            config.setSimulationRuns(Integer.parseInt(args[2]));
        }

        for (int i = 2; i < args.length; i++) {
            if (args[i].equals(VERBOSE_FLAG) || args[i].equals(VERBOSE_FLAG_LONG)) {
                config.setVerbose(true);
                break;
            }
        }

        log.debug("Using legacy argument format");
        return config;
    }

    /**
     * New format with flags: -c config.properties -i input.pn -o output.csv ...
     */
    private static SimulationConfig parseNewArguments(String[] args) throws Exception {
        SimulationConfig config = new SimulationConfig();
        String configFile = null;

        // First pass: find config file
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(CONFIG_FLAG) || args[i].equals(CONFIG_FLAG_LONG)) {
                configFile = args[i + 1];
                break;
            }
        }

        // Load config file if specified
        if (configFile != null) {
            config = SimulationConfig.fromFile(Paths.get(configFile));
        }

        // Second pass: CLI overrides
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {
                case INPUT_FLAG, INPUT_FLAG_LONG -> config.setInputFile(args[++i]);
                case OUTPUT_FLAG, OUTPUT_FLAG_LONG -> config.setOutputFile(args[++i]);
                case RUNS_FLAG, RUNS_FLAG_LONG -> config.setSimulationRuns(Integer.parseInt(args[++i]));
                case OUTPUT_MODE_FLAG, OUTPUT_MODE_FLAG_LONG -> config.setOutputMode(OutputMode.fromString(args[++i]));
                case VERBOSE_FLAG, VERBOSE_FLAG_LONG -> config.setVerbose(true);
                case CONFIG_FLAG, CONFIG_FLAG_LONG -> i++; // Already processed
                default -> {
                    if (arg.startsWith("-")) {
                        throw new PetriNetException("Unknown option: " + arg + "\n" + USAGE_MESSAGE);
                    }
                }
            }
        }

        return config;
    }

    private static void validateConfig(SimulationConfig config) throws PetriNetException {
        if (config.getInputFile() == null || config.getInputFile().isBlank()) {
            throw new PetriNetException("Input file is required.\n" + USAGE_MESSAGE);
        }
        if (config.getOutputFile() == null || config.getOutputFile().isBlank()) {
            throw new PetriNetException("Output file is required.\n" + USAGE_MESSAGE);
        }
        if (config.getSimulationRuns() < 1) {
            throw new PetriNetException("Simulation runs must be at least 1.");
        }
    }

    private static String buildOutputFileName(String baseOutput, int runNumber) {
        String baseName = getFileBaseName(baseOutput);
        return baseName + OUTPUT_FILE_SEPARATOR + runNumber + OUTPUT_FILE_EXTENSION;
    }

    private static void logOutputSummary(SimulationConfig config) {
        String baseName = getFileBaseName(config.getOutputFile());
        String basePattern = baseName + OUTPUT_FILE_SEPARATOR + "[n]" + OUTPUT_FILE_EXTENSION;

        if (config.shouldWriteCompact()) {
            log.info("Compact outputs: {}", basePattern);
        }
        if (config.shouldWriteFullState()) {
            // Only add _full suffix when both modes are active
            String fullStatePattern = config.shouldWriteCompact()
                ? config.getFullStateOutputFile(basePattern)
                : basePattern;
            log.info("Full-state outputs: {}", fullStatePattern);
        }
    }

    /**
     * Extracts the base name from a file path by removing the extension.
     * Handles paths with dots like "../results/output.csv" correctly.
     */
    private static String getFileBaseName(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex > filePath.lastIndexOf('/')) {
            return filePath.substring(0, dotIndex);
        }
        return filePath;
    }
}
