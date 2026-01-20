package com.petrinet.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SimulationConfig class.
 *
 * These tests verify:
 * - Default values are correct
 * - Properties file loading works
 * - Output mode logic is correct
 */
class SimulationConfigTest {

    // ========== DEFAULT VALUES ==========

    @Test
    @DisplayName("New config should have correct default values")
    void shouldHaveCorrectDefaults() {
        // Arrange & Act
        SimulationConfig config = new SimulationConfig();

        // Assert
        assertEquals(1, config.getSimulationRuns(), "Default runs should be 1");
        assertFalse(config.isVerbose(), "Default verbose should be false");
        assertEquals(OutputMode.COMPACT, config.getOutputMode(), "Default mode should be COMPACT");
        assertEquals("_full", config.getFullStateSuffix(), "Default suffix should be '_full'");
        assertNull(config.getInputFile(), "Input file should be null by default");
        assertNull(config.getOutputFile(), "Output file should be null by default");
    }

    // ========== PROPERTIES FILE LOADING ==========

    @Test
    @DisplayName("Should load configuration from properties file")
    void shouldLoadFromPropertiesFile(@TempDir Path tempDir) throws IOException {
        // Arrange - create a temporary properties file
        Path configFile = tempDir.resolve("test.properties");
        String propertiesContent = """
            input.file=model.pn
            output.file=results.csv
            output.mode=full-state
            simulation.runs=10
            simulation.verbose=true
            """;
        Files.writeString(configFile, propertiesContent);

        // Act - load the config
        SimulationConfig config = SimulationConfig.fromFile(configFile);

        // Assert - verify all values loaded correctly
        assertEquals("model.pn", config.getInputFile());
        assertEquals("results.csv", config.getOutputFile());
        assertEquals(OutputMode.FULL_STATE, config.getOutputMode());
        assertEquals(10, config.getSimulationRuns());
        assertTrue(config.isVerbose());
    }

    // ========== OUTPUT MODE LOGIC ==========

    @Test
    @DisplayName("COMPACT mode should write compact only")
    void compactModeShouldWriteCompactOnly() {
        // Arrange
        SimulationConfig config = new SimulationConfig();
        config.setOutputMode(OutputMode.COMPACT);

        // Assert
        assertTrue(config.shouldWriteCompact(), "Should write compact");
        assertFalse(config.shouldWriteFullState(), "Should NOT write full-state");
    }

    @Test
    @DisplayName("FULL_STATE mode should write full-state only")
    void fullStateModeShouldWriteFullStateOnly() {
        // Arrange
        SimulationConfig config = new SimulationConfig();
        config.setOutputMode(OutputMode.FULL_STATE);

        // Assert
        assertFalse(config.shouldWriteCompact(), "Should NOT write compact");
        assertTrue(config.shouldWriteFullState(), "Should write full-state");
    }

    @Test
    @DisplayName("BOTH mode should write both formats")
    void bothModeShouldWriteBoth() {
        // Arrange
        SimulationConfig config = new SimulationConfig();
        config.setOutputMode(OutputMode.BOTH);

        // Assert
        assertTrue(config.shouldWriteCompact(), "Should write compact");
        assertTrue(config.shouldWriteFullState(), "Should write full-state");
    }

    // ========== FILE NAME GENERATION ==========

    @Test
    @DisplayName("Should generate correct full-state filename with suffix")
    void shouldGenerateFullStateFilename() {
        // Arrange
        SimulationConfig config = new SimulationConfig();

        // Act & Assert
        assertEquals("output_full.csv", config.getFullStateOutputFile("output.csv"));
        assertEquals("results/data_full.csv", config.getFullStateOutputFile("results/data.csv"));
        assertEquals("../out_full.csv", config.getFullStateOutputFile("../out.csv"));
    }
}
