package com.petrinet.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.AfterEach;

import com.petrinet.model.PetriNet;
import com.petrinet.model.Place;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Output class.
 *
 * These tests verify:
 * - Compact output format is correct
 * - Full-state output format is correct
 * - Initial state writing works
 */
class OutputTest {

    private Output output;

    @AfterEach
    void cleanup() throws PetriNetException {
        if (output != null) {
            output.close();
        }
    }

    // ========== COMPACT FORMAT ==========

    @Test
    @DisplayName("Should write correct compact headers")
    void shouldWriteCompactHeaders(@TempDir Path tempDir) throws PetriNetException, IOException {
        // Arrange
        Path outputFile = tempDir.resolve("output.csv");
        output = new Output(outputFile.toString());

        // Act
        output.writeCompactHeaders();
        output.close();
        output = null; // Prevent double-close in cleanup

        // Assert
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(1, lines.size(), "Should have 1 header line");
        assertEquals("Transition,Time,Place,PlaceName,Tokens", lines.get(0));
    }

    @Test
    @DisplayName("Should write correct compact line format")
    void shouldWriteCompactLine(@TempDir Path tempDir) throws PetriNetException, IOException {
        // Arrange
        Path outputFile = tempDir.resolve("output.csv");
        output = new Output(outputFile.toString());

        // Act
        output.writeCompactHeaders();
        output.writeCompactLine(1, 5.5, 3, "Buffer", 10);
        output.writeCompactLine(2, 7.25, 5, "Source", 0);
        output.close();
        output = null;

        // Assert
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(3, lines.size(), "Should have header + 2 data lines");
        assertEquals("T1,5.5,P3,Buffer,10", lines.get(1));
        assertEquals("T2,7.25,P5,Source,0", lines.get(2));
    }

    @Test
    @DisplayName("Should write compact initial state for all places")
    void shouldWriteCompactInitialState(@TempDir Path tempDir) throws PetriNetException, IOException {
        // Arrange
        Path outputFile = tempDir.resolve("output.csv");
        output = new Output(outputFile.toString());

        // Create a simple PetriNet with 3 places
        PetriNet pn = new PetriNet(3, 0);
        pn.addPlace(new Place(1, 10, "Source"));
        pn.addPlace(new Place(2, 0, "Buffer"));
        pn.addPlace(new Place(3, 5, "Sink"));

        // Act
        output.writeCompactHeaders();
        output.writeInitialCompactState(0.0, pn);
        output.close();
        output = null;

        // Assert
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(4, lines.size(), "Should have header + 3 initial state lines");
        assertEquals("T0,0.0,P1,Source,10", lines.get(1));
        assertEquals("T0,0.0,P2,Buffer,0", lines.get(2));
        assertEquals("T0,0.0,P3,Sink,5", lines.get(3));
    }

    // ========== FULL-STATE FORMAT ==========

    @Test
    @DisplayName("Should write correct full-state headers")
    void shouldWriteFullStateHeaders(@TempDir Path tempDir) throws PetriNetException, IOException {
        // Arrange
        Path outputFile = tempDir.resolve("output.csv");
        output = new Output(outputFile.toString());

        PetriNet pn = new PetriNet(3, 0);
        pn.addPlace(new Place(1, 10, "Source"));
        pn.addPlace(new Place(2, 0, "Buffer"));
        pn.addPlace(new Place(3, 5, "Sink"));

        // Act
        output.writeHeaders(pn);
        output.close();
        output = null;

        // Assert
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(1, lines.size());
        assertEquals("Transition,Time,Source,Buffer,Sink", lines.get(0));
    }

    @Test
    @DisplayName("Should write correct full-state line format")
    void shouldWriteFullStateLine(@TempDir Path tempDir) throws PetriNetException, IOException {
        // Arrange
        Path outputFile = tempDir.resolve("output.csv");
        output = new Output(outputFile.toString());

        // Act
        output.writeFullStateLine(1, 2.5, "9,1,5");
        output.close();
        output = null;

        // Assert
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(1, lines.size());
        assertEquals("T1,2.5,9,1,5", lines.get(0));
    }

    @Test
    @DisplayName("Should write full-state initial state")
    void shouldWriteFullStateInitialState(@TempDir Path tempDir) throws PetriNetException, IOException {
        // Arrange
        Path outputFile = tempDir.resolve("output.csv");
        output = new Output(outputFile.toString());

        // Create a simple PetriNet with 3 places
        PetriNet pn = new PetriNet(3, 0);
        pn.addPlace(new Place(1, 10, "Source"));
        pn.addPlace(new Place(2, 0, "Buffer"));
        pn.addPlace(new Place(3, 5, "Sink"));

        // Act
        output.writeHeaders(pn);
        output.writeInitialState(0.0, pn);
        output.close();
        output = null;

        // Assert
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(2, lines.size(), "Should have header + 1 initial state line");
        assertEquals("T0,0.0,10,0,5", lines.get(1));
    }

    // ========== FILE HANDLING ==========

    @Test
    @DisplayName("Should create parent directories if they don't exist")
    void shouldCreateParentDirectories(@TempDir Path tempDir) throws PetriNetException {
        // Arrange
        Path nestedPath = tempDir.resolve("subdir/nested/output.csv");

        // Act
        output = new Output(nestedPath.toString());
        output.close();
        output = null;

        // Assert
        assertTrue(Files.exists(nestedPath.getParent()), "Parent directories should be created");
        assertTrue(Files.exists(nestedPath), "Output file should exist");
    }
}
