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
            TAXA.setRequired(false);
            options.addOption(TAXA);
            METADATA.setRequired(false);
            options.addOption(METADATA);
            options.addOption(INDEX_COLUMN);
            options.addOption(INDEX_FIELD);
            options.addOption(FIELD_DELIMITER);
            options.addOption(MATCH_COLUMN);
            options.addOption(MATCH_FIELD);
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
                    commandLine.getOptionValue("id-column", ""),
                    Integer.parseInt(commandLine.getOptionValue("id-field", "0")),
                    commandLine.getOptionValue("field-delimiter", "|"),
                    Boolean.parseBoolean(commandLine.getOptionValue("remove", "False")),
                    commandLine.getOptionValues("match-column"),
                    commandLine.getOptionValues("match-field"),
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
                   boolean remove,
                   String[] columnMatches,
                   String[] fieldMatches,
                   boolean ignoreMissing,
                   boolean isVerbose) {

        super(metadataFileName, taxaFileName, indexColumn, indexField, headerDelimiter, isVerbose);

        if (taxa != null) {
            if (columnMatches != null || fieldMatches != null) {
                errorStream.println("Provide a taxon file or metadata matches but not both.");
                System.exit(1);

            }
        } else {
            if (columnMatches == null && fieldMatches == null) {
                errorStream.println("Provide either a taxon file or one or more metadata column matches.");
                System.exit(1);

            }
        }

        Map<String, List<Pattern>> columnPatterns = new HashMap<>();
        if (columnMatches != null) {
            for (String match : columnMatches) {
                String[] parts = match.split("=");
                if (!columnNames.contains(parts[0])) {
                    errorStream.println("Column, " + parts[0] + ", not found in metadata for matching");
                    System.exit(1);
                }
                List<Pattern> values = columnPatterns.getOrDefault(parts[0], new ArrayList<>());
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
                columnPatterns.put(parts[0], values);
            }
        }

        Map<Integer, List<Pattern>> fieldPatterns = new HashMap<>();
        if (fieldMatches != null) {
            for (String match : fieldMatches) {
                String[] parts = match.split("=");
                int field = 0;
                try {
                    field = Integer.parseInt(parts[0]);
                } catch (NumberFormatException nfe) {
                }
                if (field <= 0) {
                    errorStream.println("Field number, " + parts[0] + ", should be an integer > 1.");
                    System.exit(1);
                }

                List<Pattern> values = fieldPatterns.getOrDefault(field, new ArrayList<>());
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
                fieldPatterns.put(field, values);
            }
        }

        Set<String> matchedSequenceKeySet = new HashSet<>();

        processSequences(fastaFileName, outputFileName, sequence -> {
            String index = getSequenceID(sequence);

            if (taxa == null || this.taxa.contains(index)) {
                boolean match = true;
                if (!columnPatterns.isEmpty()) {
                    CSVRecord record = metadata.get(index);
                    for (String column : columnPatterns.keySet()) {
                        String value = record.get(column);
                        List<Pattern> matchList = columnPatterns.get(column);
                        for (Pattern pattern : matchList) {
                            if (!pattern.matcher(value).find()) {
                                match = false;
                            }

                        }
                    }
                }

                if (!fieldPatterns.isEmpty()) {
                    for (int field : fieldPatterns.keySet()) {
                        String value = getLabelField(sequence.getTaxon().getName(), field, fieldDelimiter);
                        List<Pattern> matchList = fieldPatterns.get(field);
                        for (Pattern pattern : matchList) {
                            if (!pattern.matcher(value).find()) {
                                match = false;
                            }

                        }
                    }
                }

                if (match) {
                    matchedSequenceKeySet.add(index);
                }

                return (match == remove ? null : sequence);
            }
            return null;
        });

        if (isVerbose) {
            outStream.println("Matched taxa: " + matchedSequenceKeySet.size());
            outStream.println();
        }

        if (outputMetadataFileName != null) {
            List<CSVRecord> metadataRows = new ArrayList<>();
            for (String key : matchedSequenceKeySet) {
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

