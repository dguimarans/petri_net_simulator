package com.petrinet.io;

  /**
   * Defines the Petri Net file format specification version 1.0.
   * 
   * <p>File format structure:
   * <pre>
   * @Places
   * tokens;name
   * 
   * @Transitions
   * placesIn;placesOut;weightsIn;weightsOut;distribution
   * 
   * @TerminationTime
   * time_value
   * 
   * @TerminationMarking
   * placeId;tokenCount
   * </pre>
   */
 
public final class PetriNetFileFormat {

      /** File format version */
      public static final String VERSION = "1.0";

      // Section markers
      public static final String SECTION_PLACES = "@Places";
      public static final String SECTION_TRANSITIONS = "@Transitions";
      public static final String SECTION_TERMINATION_TIME = "@TerminationTime";
      public static final String SECTION_TERMINATION_MARKING = "@TerminationMarking";

      // Field delimiters
      public static final String FIELD_DELIMITER = ";";
      public static final String ARRAY_DELIMITER = ",";

      // Special characters
      public static final String COMMENT_PREFIX = "#";

      // File extension
      public static final String INPUT_FILE_EXTENSION = ".pn";

      // Default values
      public static final int DEFAULT_PLACE_TOKENS = 0;

      /**
       * Private constructor to prevent instantiation.
       */
      private PetriNetFileFormat() {
          throw new AssertionError("Cannot instantiate file format constants class");
      }
  }
