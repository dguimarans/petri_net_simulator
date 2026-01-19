# Petri Net Simulator

A discrete event simulation engine based on extended Petri Nets with support for inhibitor arcs and multiple probability distributions.

## Requirements

- Java 21 or higher
- Maven 3.6+

## Building the Project

```bash
# Clone the repository
git clone <repository-url> [target-directory]
# Move to target directory

# Build the executable JAR (includes all dependencies)
mvn clean package

# The JAR file will be created at:
# target/petrinet-simulator-1.0-SNAPSHOT.jar
```

## Running the Simulator

### Using Configuration File (Recommended)

```bash
java -jar target/petrinet-simulator-1.0-SNAPSHOT.jar -c config.properties
```

Example configuration file (`simulation.properties`):

```properties
# Input/Output files
input.file=models/example.pn
output.file=results/output.csv

# Output mode: compact | full-state | both
output.mode=compact

# Suffix for full-state file when mode=both (e.g., output.csv -> output_full.csv)
output.full-state.suffix=_full

# Number of simulation runs
simulation.runs=1

# Verbose logging (true/false)
simulation.verbose=false
```

### Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `input.file` | Path to input Petri Net file (.pn) | Required |
| `output.file` | Base path for output CSV file(s) | Required |
| `output.mode` | Output format: `compact`, `full-state`, or `both` | `compact` |
| `output.full-state.suffix` | Suffix added to full-state file when mode=both | `_full` |
| `simulation.runs` | Number of simulation runs to execute | `1` |
| `simulation.verbose` | Enable verbose logging | `false` |

### Command Line Options

CLI arguments override configuration file values:

```bash
java -jar petrinet-simulator.jar [options]

Options:
  -c, --config <file>       Configuration file (optional)
  -i, --input <file>        Input Petri Net file
  -o, --output <file>       Output file base name
  -r, --runs <number>       Number of simulation runs
  -m, --output-mode <mode>  Output mode: compact, full-state, both
  -verbose, --verbose       Enable verbose logging
```

Examples:

```bash
# Using config file with CLI overrides
java -jar petrinet-simulator.jar -c config.properties -r 10 --output-mode both

# Direct CLI usage
java -jar petrinet-simulator.jar -i model.pn -o results.csv -r 5 -m compact
```

### Legacy Command Line (Backwards Compatible)

```bash
java -jar petrinet-simulator.jar <inputFile> <outputFile> [runs] [-verbose]
```

Examples:

```bash
# Single run
java -jar petrinet-simulator.jar model.pn output.csv

# Multiple runs with verbose output
java -jar petrinet-simulator.jar model.pn output.csv 10 -verbose
```

Note: Legacy mode defaults to `full-state` output for backwards compatibility.

## Input File Format

Input files use the `.pn` extension with the following structure:

```
@Places
<place definitions>

@Transitions
<transition definitions>

@TerminationTime
<time value>

@TerminationMarking
<marking conditions>
```

Files can contain one or both termination criteria.

### Places Section

Format: `tokens[;name]`

```
@Places
# Comments start with '#'
3;Source          # Place with 3 tokens named "Source"
0;Buffer          # Place with 0 tokens named "Buffer"
1                 # Place with 1 token (auto-named P3)
```

### Transitions Section

Format: `placesIn;placesOut;weightsIn;weightsOut[;distribution]`

- **placesIn**: Comma-separated list of input place IDs (1-indexed)
- **placesOut**: Comma-separated list of output place IDs
- **weightsIn**: Comma-separated arc weights from input places (0 = inhibitor arc)
- **weightsOut**: Comma-separated arc weights to output places
- **distribution**: Optional timing distribution (omit for purely conditional transitions)

```
@Transitions
# Conditional transition: P1 -> P2
1;2;1;1

# Timed transition with normal distribution
1,2;3;1,1;1;NOR(5.0,1.0)

# Inhibitor arc (weight 0): fires only when P3 is empty
1,3;2;1,0;1;EXP(2.0)
```

### Supported Distributions

| Code | Distribution | Parameters | Example |
|------|--------------|------------|---------|
| `CON` | Constant | value | `CON(5.0)` |
| `UNI` | Uniform | lower, upper | `UNI(1.0,10.0)` or `UNI()` for [0,1] |
| `EXP` | Exponential | rate (lambda) | `EXP(0.5)` |
| `NOR` | Normal | mean, std_dev | `NOR(10.0,2.0)` or `NOR()` for N(0,1) |
| `TRI` | Triangular | min, max, mode | `TRI(1.0,5.0,3.0)` |
| `LOG` | Log-Normal | mean, std_dev | `LOG(2.0,0.5)` |
| `BEX` | Bounded Exponential | lower, rate | `BEX(0.0,1.0)` |
| `GAM` | Gamma | shape, scale, threshold | `GAM(2.0,1.0,0.0)` |

### Termination Conditions

Simulations terminate when either condition is met:

```
@TerminationTime
1440              # Stop at time 1440 (e.g., minutes in 24 hours)

@TerminationMarking
5;10              # Stop when Place 5 has >= 10 tokens
6;5               # AND Place 6 has >= 5 tokens
```

When both termination criteria are specified, simulations will finish based on which criterion is met first.

### Complete Example

```
# Simple producer-consumer model
@Places
10;Source         # P1: 10 items to process
0;Buffer          # P2: processing buffer
0;Processed       # P3: completed items

@Transitions
# T1: Source -> Buffer (exponential arrivals)
1;2;1;1;EXP(0.5)
# T2: Buffer -> Processed (normal processing time)
2;3;1;1;NOR(2.0,0.5)

@TerminationMarking
3;10              # Stop when 10 items processed
```

## Output File Formats

### Compact Format (Default)

One line per place change. Efficient for large models.

```csv
Transition,Time,Place,PlaceName,Tokens
T1,0.0,1,Source,9
T1,0.0,2,Buffer,1
T2,1.23,2,Buffer,0
T2,1.23,3,Processed,1
```

| Column | Description |
|--------|-------------|
| Transition | Transition that fired |
| Time | Simulation time of the event |
| Place | Place ID that changed |
| PlaceName | Name of the place |
| Tokens | New token count after the change |

### Full-State Format (Legacy)

One line per transition firing with complete system state.

```csv
Transition,Time,Source,Buffer,Processed
T0,0.0,10,0,0
T1,0.5,9,1,0
T2,1.2,9,0,1
T1,1.8,8,1,1
```

| Column | Description |
|--------|-------------|
| Transition | Transition that fired (INIT = initial state) |
| Time | Simulation time |
| Place columns | Token count for each place after firing |

### Output File Naming

For multiple runs, output files are numbered:
- `output_1.csv`, `output_2.csv`, ... (compact or full-state only)
- `output_1.csv`, `output_1_full.csv`, ... (when mode=both)

## Performance Notes

- **Compact mode** is recommended for large models (1000+ places) - up to 14x faster than full-state
- **Full-state mode** provides complete snapshots but generates larger files
- **Both mode** writes two files, useful for debugging while maintaining efficient compact logs

## License

MIT License
