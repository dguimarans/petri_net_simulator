package com.petrinet.config;

/**
 * Defines the output format for simulation results.
 */
public enum OutputMode {
    COMPACT("compact"),           // Per-change output (new default)
    FULL_STATE("full-state"),     // Full system state per transition (legacy)
    BOTH("both");                 // Write both formats to separate files

    private final String configValue;

    OutputMode(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigValue() {
        return configValue;
    }

    public static OutputMode fromString(String value) {
        for (OutputMode mode : values()) {
            if (mode.configValue.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown output mode: " + value +
            ". Valid values: compact, full-state, both");
    }
}
