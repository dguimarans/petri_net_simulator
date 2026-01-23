# Petri Net Simulator - Refactoring TODO List

**Last Updated:** 2026-01-21
**Current Status:** A+ (99/100) - Merged to main with tests, config system, output modes, and performance improvements

---

## 🔥 **PRIORITY 1: Quick Fixes (5-10 minutes)**

### ✅ **COMPLETED**
- [x] Extract magic strings to constants classes
- [x] Replace `Hashtable` with `HashMap`
- [x] Implement proper exception handling (remove `System.exit`)
- [x] Add Log4j2 for logging
- [x] Use try-with-resources for file I/O
- [x] Remove commented code
- [x] Improve method returns (remove unnecessary conditionals)
- [x] Move source code to Maven structure
- [x] Replace hardcoded paths with `Path` API (Output.java now uses `Paths.get()`)
- [x] Delete unused `ExecutionControl` class (replaced by `SimulationConfig`)

### 🎯 **REMAINING**
None! Priority 1 is complete.

---

## 🚀 **PRIORITY 2: Code Quality & Consistency**

### Testing

#### ✅ Completed Tests (14 tests)

**SimulationConfigTest (6 tests):**
- [x] `shouldHaveCorrectDefaults` - Verify default values
- [x] `shouldLoadFromPropertiesFile` - Properties file loading
- [x] `compactModeShouldWriteCompactOnly` - COMPACT mode logic
- [x] `fullStateModeShouldWriteFullStateOnly` - FULL_STATE mode logic
- [x] `bothModeShouldWriteBoth` - BOTH mode logic
- [x] `shouldGenerateFullStateFilename` - File suffix generation

**OutputTest (7 tests):**
- [x] `shouldWriteCompactHeaders` - Compact CSV headers
- [x] `shouldWriteCompactLine` - Compact line format
- [x] `shouldWriteCompactInitialState` - Initial state in compact format
- [x] `shouldWriteFullStateHeaders` - Full-state CSV headers
- [x] `shouldWriteFullStateLine` - Full-state line format
- [x] `shouldWriteFullStateInitialState` - Initial state in full-state format
- [x] `shouldCreateParentDirectories` - Directory creation

#### ❌ Missing Tests - Configuration & I/O

**PetriNetFileParserTest:**
- [ ] `shouldParsePlacesSection` - Parse basic places
- [ ] `shouldParsePlacesWithCustomNames` - Parse places with names
- [ ] `shouldParseUntimedTransition` - Parse transition without distribution
- [ ] `shouldParseTimedTransition` - Parse transition with distribution
- [ ] `shouldParseAllDistributionTypes` - CON, UNI, EXP, NOR, TRI, LOG, BEX, GAM
- [ ] `shouldParseTerminationTime` - Parse @TerminationTime section
- [ ] `shouldParseTerminationMarking` - Parse @TerminationMarking section
- [ ] `shouldParseBothTerminationCriteria` - Both time and marking
- [ ] `shouldHandleSectionsInAnyOrder` - Flexible section ordering
- [ ] `shouldIgnoreComments` - Skip # comment lines
- [ ] `shouldIgnoreBlankLines` - Skip empty lines
- [ ] `shouldThrowOnMissingPlacesSection` - Error handling
- [ ] `shouldThrowOnMissingTransitionsSection` - Error handling
- [ ] `shouldThrowOnInvalidPlaceFormat` - Malformed place line
- [ ] `shouldThrowOnInvalidTransitionFormat` - Malformed transition line
- [ ] `shouldThrowOnMismatchedArrayLengths` - placesIn.length != weightsIn.length

**SimulateCliTest (requires refactoring to test):**
- [ ] `shouldParseLegacyArgs` - inputFile outputFile
- [ ] `shouldParseLegacyArgsWithRuns` - inputFile outputFile runs
- [ ] `shouldParseLegacyArgsWithVerbose` - inputFile outputFile runs -verbose
- [ ] `shouldParseNewFormatFlags` - -i, -o, -r, -m flags
- [ ] `shouldOverrideConfigWithCli` - CLI takes precedence
- [ ] `shouldDefaultToFullStateForLegacy` - Legacy compatibility
- [ ] `shouldDefaultToCompactForNewFormat` - New default behavior
- [ ] `shouldRejectUnknownFlags` - Error on invalid flags

*Note:* CLI tests require extracting parsing logic from `Simulate.java` to a testable class (currently `private static` methods).

#### ❌ Missing Tests - Model Classes

**PlaceTest:**
- [ ] `shouldStoreInitialTokens` - Constructor sets initial tokens
- [ ] `shouldUpdateTokens` - setTokens() works correctly
- [ ] `shouldResetToInitialTokens` - resetTokens() restores initial state
- [ ] `shouldGenerateDefaultName` - Default name is "P" + id
- [ ] `shouldUseCustomName` - Custom name from constructor

**TransitionTest:**
- [ ] `shouldStoreInputPlaces` - Constructor sets input places
- [ ] `shouldStoreOutputPlaces` - Constructor sets output places
- [ ] `shouldStoreWeights` - Input/output weights stored correctly
- [ ] `shouldBeTimedWithDistribution` - isTimed() returns true
- [ ] `shouldBeUntimedWithoutDistribution` - isTimed() returns false
- [ ] `shouldCountFirings` - countFirings() increments counter
- [ ] `shouldCallDistribution` - call() returns sampled value

**EventTest:**
- [ ] `shouldStoreTransitionAndTime` - Constructor values
- [ ] `shouldCompareByTime` - Comparable implementation for sorting

#### ❌ Missing Tests - Simulation Logic (Critical)

**PetriNetTest - Transition Enablement:**
- [ ] `shouldEnableTransitionWhenAllInputPlacesHaveSufficientTokens`
- [ ] `shouldDisableTransitionWhenInputPlaceLacksTokens`
- [ ] `shouldHandleMultipleInputPlaces` - All inputs must be satisfied
- [ ] `shouldHandleWeightedArcs` - Requires weight tokens, not just 1
- [ ] `shouldHandleInhibitorArc` - Weight 0 means fire only when place empty
- [ ] `shouldEnableInhibitorWhenPlaceEmpty` - Inhibitor allows firing
- [ ] `shouldDisableInhibitorWhenPlaceHasTokens` - Inhibitor blocks firing

**PetriNetTest - Transition Firing:**
- [ ] `shouldConsumeTokensFromInputPlaces` - Tokens removed correctly
- [ ] `shouldProduceTokensToOutputPlaces` - Tokens added correctly
- [ ] `shouldHandleWeightedConsumption` - Remove weight tokens
- [ ] `shouldHandleWeightedProduction` - Add weight tokens
- [ ] `shouldNotAffectUnrelatedPlaces` - Only connected places change
- [ ] `shouldIncrementFiringCounter` - Transition tracks firings

**SimulationEngineTest - Untimed Transitions:**
- [ ] `shouldFireUntimedTransitionImmediately` - No delay
- [ ] `shouldFireChainOfUntimedTransitions` - Cascading fires
- [ ] `shouldDetectDeadlockWithNoEnabledTransitions`

**SimulationEngineTest - Timed Transitions:**
- [ ] `shouldScheduleTimedTransitionWithDelay` - Event added to queue
- [ ] `shouldFireTimedTransitionAtScheduledTime` - Correct time
- [ ] `shouldMaintainEventOrderByTime` - Earlier events fire first
- [ ] `shouldSampleDistributionForDelay` - Distribution called

**SimulationEngineTest - Mixed Scenarios:**
- [ ] `shouldFireUntimedThenScheduleTimed` - Producer-consumer pattern
  ```
  P1(token) --[untimed]--> P2 --[timed(EXP)]--> P3
  ```
- [ ] `shouldHandleSharedResourceConflict` - Two enabled, one resource
  ```
  P1(1 token) enables T1 and T2, but only one can fire
  T1 fires first → T2 becomes disabled
  ```
- [ ] `shouldRespectFiringOrder` - Deterministic when times equal
- [ ] `shouldTerminateAtTimeLimit` - @TerminationTime works
- [ ] `shouldTerminateAtMarking` - @TerminationMarking works
- [ ] `shouldTerminateAtEitherCondition` - First condition wins

**SimulationEngineTest - Multiple Runs:**
- [ ] `shouldResetMarkingBetweenRuns` - Places reset to initial state
- [ ] `shouldProduceDifferentResultsWithRandomDistributions` - Stochastic behavior
- [ ] `shouldProduceSameResultsWithConstantDistributions` - Deterministic behavior

#### ❌ Missing Tests - Distribution Refactoring (Future)

**DistributionTypeTest (when enum is created):**
- [ ] `shouldReturnCorrectCodeForConstant` - CON
- [ ] `shouldReturnCorrectCodeForUniform` - UNI
- [ ] `shouldReturnCorrectCodeForExponential` - EXP
- [ ] `shouldReturnCorrectCodeForNormal` - NOR
- [ ] `shouldReturnCorrectCodeForTriangular` - TRI
- [ ] `shouldReturnCorrectCodeForLogNormal` - LOG
- [ ] `shouldReturnCorrectCodeForBoundedExp` - BEX
- [ ] `shouldReturnCorrectCodeForGamma` - GAM
- [ ] `shouldParseCodeToEnum` - fromCode() method
- [ ] `shouldThrowOnUnknownCode` - Invalid code handling

**DistributionsTest:**
- [ ] `shouldSampleConstantDistribution` - Always returns same value
- [ ] `shouldSampleUniformInRange` - Values within [lower, upper]
- [ ] `shouldSampleExponentialPositive` - Values > 0
- [ ] `shouldSampleNormalAroundMean` - Statistical properties
- [ ] `shouldSampleTriangularInRange` - Values within [min, max]
- [ ] `shouldSampleLogNormalPositive` - Values > 0
- [ ] `shouldSampleBoundedExpAboveLower` - Values >= lower bound
- [ ] `shouldSampleGammaPositive` - Values > threshold

### Lombok Integration
- [ ] **Add Lombok to POJOs (`Place`, `Transition`, `Event`)**
  *Why:* Reduce boilerplate (getters, setters, constructors)
  *Where:* `Place.java`, `Event.java`
  *Note:* Already configured in pom.xml

### Thread Safety
- [x] **Replace static ID counters with explicit IDs**
  *Status:* ✅ Complete

---

## 📐 **PRIORITY 3: Architecture Refactoring**

### Input File Parsing
- [x] **Create `PetriNetFileParser` class** ✅ Complete
- [x] **Implement `Place.reset()` method** ✅ Complete
- [x] **Add defensive programming for termination criteria** ✅ Complete
- [x] **Improve parser validation and error messages** ✅ Complete

- [ ] **Add line numbers to parse error messages**
  *Why:* Better debugging - know exactly where in file the error occurred
  *Where:* `PetriNetFileParser.java`

- [ ] **Extract CLI parsing to testable class**
  *Why:* Enable unit testing of argument parsing logic
  *Where:* New `ArgumentParser.java` or make methods package-private
  *Current issue:* `Simulate.java` has `private static` parsing methods

### Builder Pattern *(Optional - Low Priority)*
- [ ] Add Builder pattern to `PetriNet`
- [ ] Create `PetriNet.buildFromFile()` static factory

### Validation
- [ ] **Add input validation with Bean Validation (JSR 380)**

---

## 🔬 **PRIORITY 4: Distribution Refactoring (3-5 hours)**

### Apache Commons Math Integration
- [ ] **Replace custom `Distributions` class with Apache Commons Math3**
  *Already in pom.xml:* `commons-math3:3.6.1`

### Distribution Type Enum
- [ ] **Create `DistributionType` enum**
  *Why:* Replace magic strings "CON", "UNI", "EXP", etc.
  ```java
  public enum DistributionType {
      CONSTANT("CON"), UNIFORM("UNI"), EXPONENTIAL("EXP"),
      NORMAL("NOR"), TRIANGULAR("TRI"), LOGNORMAL("LOG"),
      BOUNDED_EXP("BEX"), GAMMA("GAM");

      public static DistributionType fromCode(String code) { ... }
  }
  ```

### Additional Distributions
- [ ] **Add binomial distribution** - Model probability of failure

---

## 🐛 **PRIORITY 5: Bug Investigation (1-2 hours)**

### Event List Issue
- [ ] **Review initialization with unconditional transitions**
  *Issue:* Only first token pulled, rest wait until transition fires
  *Where:* `SimulationEngine.java:66-83`
  *Test Case:* Use `gamma_test.pn` input file

---

## 📚 **PRIORITY 6: Documentation & Quality**

### Configuration Management
- [x] **Create configuration file support** ✅ Complete

### Code Quality Tools
- [ ] **Add Maven plugins:**
  - SpotBugs for bug detection
  - Checkstyle for code style
  - JaCoCo for code coverage

### Documentation
- [x] **Create README.md** ✅ Complete
- [ ] **Add comprehensive Javadoc**

---

## ⚡ **Performance Improvements**

### Pending Investigations
- [ ] **Investigate event list efficiency: ordered insertion vs sorting**
  *Current:* O(n log n) sort on each iteration
  *Options:*
  - `PriorityQueue<Event>` for O(log n) insertion and O(1) peek/poll
  - Binary search insertion
  *Note:* Profile first to determine if bottleneck

### Completed
- [x] **StringBuilder in `stateToString()`** - 9x speedup
- [x] **Compact output format** - 14x speedup
- [x] **Output mode configuration** - compact, full-state, both
- [x] **Initial state in compact output** ✅ Complete

---

## 📦 **Package Structure (Reference)**

```
com.petrinet/
├── config/
│   ├── OutputMode.java            ← Output format enum
│   └── SimulationConfig.java      ← Unified config with properties support
├── engine/
│   ├── SimulationControl.java     ← Simulation config (termination)
│   ├── SimulationEngine.java      ← Simulation execution
│   └── Event.java                 ← Event model
├── model/
│   ├── Place.java                 ← Place model (with resetTokens)
│   ├── Transition.java            ← Transition model
│   ├── PetriNet.java              ← Petri Net model
│   └── ModelDefaultStrings.java   ← Model constants
├── distributions/
│   └── Distributions.java         ← To be refactored
├── io/
│   ├── PetriNetFileFormat.java    ← File format constants
│   ├── OutputFormat.java          ← Output format constants
│   ├── Output.java                ← CSV writer
│   ├── PetriNetFileParser.java    ← File parsing logic
│   └── PetriNetException.java     ← Custom exception
└── cli/
    ├── CommandLineConstants.java  ← CLI constants
    └── Simulate.java              ← Main entry point

src/test/java/com/petrinet/
├── config/
│   └── SimulationConfigTest.java  ← ✅ 6 tests
├── io/
│   └── OutputTest.java            ← ✅ 7 tests
└── AppTest.java                   ← 1 test
```

---

## 🎯 **Quick Reference: What's Done vs What's Left**

| Area | Status | Priority |
|------|--------|----------|
| Magic strings extraction | ✅ Complete | - |
| Exception handling | ✅ Complete | - |
| Logging (Log4j2) | ✅ Complete | - |
| Try-with-resources | ✅ Complete | - |
| Static ID counter removal | ✅ Complete | - |
| File parser extraction | ✅ Complete | - |
| Place reset functionality | ✅ Complete | - |
| Configuration file support | ✅ Complete | - |
| Output mode selection | ✅ Complete | - |
| Performance optimization | ✅ Complete (9-14x) | - |
| README documentation | ✅ Complete | - |
| **Initial state in compact output** | ✅ Complete | - |
| **Basic test coverage** | ✅ 14 tests | - |
| **ExecutionControl cleanup** | ✅ Deleted | - |
| Comprehensive test coverage | 🔶 ~15% | P2 |
| CLI parsing testability | ❌ Blocked | P3 |
| Line numbers in parse errors | ❌ 0% | P3 |
| Lombok integration | ❌ 0% | P2 |
| Distribution refactoring | ❌ 0% | P4 |
| Javadoc documentation | ❌ 0% | P6 |
| Event list efficiency | 🔍 To investigate | Perf |

---

## 💡 **Notes**

- **Java 21 configured** - Records, pattern matching, text blocks available
- **Maven Surefire 3.1.2** - JUnit 5 support added
- **14 tests passing** - Basic coverage for config and output
- **9-14x performance improvement** - StringBuilder + compact output
- **Dual CLI support** - Legacy and new flag-based syntax
- **Configuration precedence** - CLI args > config file > defaults
- **Merged to main** - 2026-01-21

---

## 🚦 **Recommended Work Order**

1. **P2: Simulation Logic Tests** - Critical for correctness validation
   - Start with `PetriNetTest` (enablement, firing)
   - Then `SimulationEngineTest` (timed/untimed scenarios)
2. **P2: Parser Tests** - Validate file parsing edge cases
3. **P3: Extract CLI Parsing** - Enable testability
4. **P4: Distribution Enum** - Clean up magic strings
5. **P4: Distribution Tests** - Validate sampling behavior
6. **Perf: Event List** - Profile and optimize if needed
7. **P2: Lombok** - Reduce boilerplate incrementally
8. **P6: Javadoc** - Document public APIs

---

## 💭 **Additional Suggestions**

### Test Infrastructure
- [ ] **Add test fixtures/utilities** - Reusable PetriNet builders for tests
  ```java
  class TestPetriNetBuilder {
      static PetriNet simpleProducerConsumer() { ... }
      static PetriNet sharedResourceConflict() { ... }
  }
  ```

- [ ] **Add integration tests** - End-to-end simulation with known results
  - Use small, hand-verifiable Petri Nets
  - Compare output files against expected results

- [ ] **Add parameterized tests** - Test multiple distributions with same logic
  ```java
  @ParameterizedTest
  @EnumSource(DistributionType.class)
  void shouldParseAllDistributionTypes(DistributionType type) { ... }
  ```

### Application Improvements
- [ ] **Add simulation statistics output** - Total firings per transition, average tokens
- [ ] **Add seed parameter for reproducibility** - Deterministic random for debugging
- [ ] **Add progress indicator for long simulations** - Percentage or events processed
- [ ] **Add CSV output validation** - Verify output format in tests
