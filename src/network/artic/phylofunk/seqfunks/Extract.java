package network.artic.phylofunk.seqfunks;

import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVRecord;

import network.artic.phylofunk.funks.FunkFactory;

import static network.artic.phylofunk.funks.FunkOptions.*;

/**
 *
 */
public class Extract extends SequenceFunk {
    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "extract";
        }

        @Override
        public String getDescription() {
            return "Extract sequences from a list or based on metadata.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            options.addOption(OUTPUT_FILE);
            options.addOption(OUTPUT_METADATA);
            options.addOption(TAXA);
            METADATA.setRequired(false);
            options.addOption(METADATA);
            options.addOption(INDEX_COLUMN);
            options.addOption(INDEX_FIELD);
            options.addOption(FIELD_DELIMITER);
            options.addOption(MATCH_VALUES);
            options.addOption(IGNORE_MISSING);
        }

        @Override
        public void create(CommandLine commandLine, boolean isVerbose) {
            new Extract(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("taxa"),
                    commandLine.getOptionValue("metadata"),
                    commandLine.getOptionValue("output"),
                    commandLine.getOptionValue("output-metadata"),
                    commandLine.getOptionValue("index-column", ""),
                    Integer.parseInt(commandLine.getOptionValue("index-field", "0")),
                    commandLine.getOptionValue("field-delimiter", "|"),
                    Integer.parseInt(commandLine.getOptionValue("from-coordinate", "0")),
                    Integer.parseInt(commandLine.getOptionValue("to-coordinate", "0")),
                    Boolean.parseBoolean(commandLine.getOptionValue("remove", "False")),
                    commandLine.getOptionValues("match"),
                    commandLine.getOptionValues("match"),
                    commandLine.hasOption("ignore-missing"),
                    isVerbose);
        }

    };


    public Extract(String fastaFileName,
                   String taxaFileName,
                   String metadataFileName,
                   String outputFileName,
                   String outputMetadataFileName,
                   String indexColumn,
                   int indexField,
                   String headerDelimiter,
                   int fromCoordinate,
                   int toCoordinate,
                   boolean remove,
                   String[] columns,
                   String[] matches,
                   boolean ignoreMissing,
                   boolean isVerbose) {

        super(metadataFileName, taxaFileName, indexColumn, indexField, headerDelimiter, isVerbose);

        Map<String, List<Pattern>> columnMatches = new HashMap<>();
        if (matches != null) {
            for (String match : matches) {
                String[] parts = match.split("=");
                List<Pattern> values = columnMatches.getOrDefault(parts[0], new ArrayList<Pattern>());
                if (parts.length == 2) {
                    Pattern pattern = Pattern.compile(parts[1]);
                    values.add(pattern);
                } else if (parts.length == 1) {
                    // only the column heading given so match any non-empty value
                    Pattern pattern = Pattern.compile(".*");
                    values.add(pattern);
                } else {
                    errorStream.println("Unrecognised field match argument: " + match);
                    System.exit(1);
                }
                columnMatches.put(parts[0], values);
            }
        }

        Set<String> matchedTaxonSet = new HashSet<>();

        if (fastaFileName != null) {
            processSequences(fastaFileName, outputFileName, sequence -> {
                String index = getSequenceID(sequence);

                if (this.taxa.contains(index)) {
                    boolean match = true;
                    if (columnMatches.size() > 0) {
                        CSVRecord record = metadata.get(index);
                        for (String column : columnMatches.keySet()) {
                            String value = record.get(column);
                            if (value == null) {
                                errorStream.println("Column, " + column + ", not found in metadata for matching");
                                System.exit(1);
                            }
                            List<Pattern> matchList = columnMatches.get(column);
                            for (Pattern pattern : matchList) {
                                if (!pattern.matcher(value).find()) {
                                    match = false;
                                }

                            }
                        }
                    }

                    if (match) {
                        matchedTaxonSet.add(index);
                    }

                    return (match == remove ? null : sequence);
                }
                return null;
            });

            int missingCount = 0;
            for (String key : matchedTaxonSet) {
                if (!taxa.contains(key)) {
                    missingCount += 1;
                    if (!ignoreMissing) {
                        errorStream.println("Taxon index, " + key + ", not found in sequences");
                        System.exit(1);
                    }
                    if (isVerbose) {
                        outStream.println("Unmatched taxon: " + key);
                    }
                }
            }
            if (isVerbose) {
                if (missingCount > 0) {
                    outStream.println("Unmatched taxa: " + missingCount);
                }
                outStream.println();
            }
        }

        if (outputMetadataFileName != null) {
            List<CSVRecord> metadataRows = new ArrayList<>();
            for (String key : taxa) {
                if (metadata.containsKey(key)) {
                    metadataRows.add(metadata.get(key));
                }
            }
            if (metadataRows.isEmpty()) {
                outStream.println("No matches in metadata - not creating output metadata file.");
            } else {
                if (isVerbose) {
                    outStream.println("Writing metadata file, " + outputMetadataFileName);
                    outStream.println();
                }
                writeMetadataFile(metadataRows, outputMetadataFileName);
            }
        }

    }


}

