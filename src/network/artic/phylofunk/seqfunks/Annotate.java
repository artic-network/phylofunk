package network.artic.phylofunk.seqfunks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.taxa.Taxon;

import network.artic.phylofunk.funks.FunkOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVRecord;

import network.artic.phylofunk.funks.FunkFactory;

import static network.artic.phylofunk.funks.FunkOptions.*;

/**
 *
 */
public class Annotate extends SequenceFunk {
    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "annotate";
        }

        @Override
        public String getDescription() {
            return "Annotate sequence headers from a metadata table.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            options.addOption(OUTPUT_FILE);
            options.addOption(METADATA);
            options.addOption(INDEX_COLUMN);
            options.addOption(INDEX_FIELD);
            options.addOption(FIELD_DELIMITER);
            options.addOption(DEFAULT_VALUE);
            options.addOption(FunkOptions.LABEL_FIELDS);
            options.addOption(REPLACE);
//            options.addOption(ANNOTATE_DESCRIPTION);
            options.addOption(IGNORE_MISSING);
            options.addOption(SKIP_MISSING);
        }

        @Override
        public void create(CommandLine commandLine, boolean isVerbose) {
            new Annotate(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("metadata"),
                    commandLine.getOptionValue("output"),
                    commandLine.getOptionValue("id-column", ""),
                    Integer.parseInt(commandLine.getOptionValue("id-field", "0")),
                    commandLine.getOptionValue("field-delimiter", "|"),
                    commandLine.getOptionValue("default-value"),
                    commandLine.getOptionValues("label-fields"),
                    commandLine.hasOption("replace"),
                    commandLine.hasOption("annotate-description"),
                    commandLine.hasOption("ignore-missing"),
                    commandLine.hasOption("skip-missing"),
                    isVerbose);
        }

    };

    public Annotate(String fastaFileName,
                    String metadataFileName,
                    String outputPath,
                    String indexColumn,
                    int indexHeader,
                    String fieldDelimiter,
                    String defaultValue,
                    String[] labelColumns,
                    boolean replace,
                    boolean annotateDescription,
                    boolean ignoreMissing,
                    boolean skipMissing,
                    boolean isVerbose) {

        super(metadataFileName, null, indexColumn, indexHeader, fieldDelimiter, isVerbose);

        if (annotateDescription) {
            throw new UnsupportedOperationException("annotate-description option not supported yet");
        }

        if (metadata == null) {
            errorStream.println("To use annotate sequence labels, a metadata file must be provided.");
            System.exit(1);
        }
        
        for (String column : labelColumns) {
            if (!columnNames.contains(column)) {
                errorStream.println("Metadata doesn't contain the label column, " + column + ".");
                System.exit(1);
            }
        }

        List<Sequence> sequences = readFasta(fastaFileName);

        Map<String, Sequence> sequenceMap = getSequenceMap(sequences);

        List<Sequence> outSequences = new ArrayList<>();

        if (labelColumns != null && labelColumns.length > 0) {
            if (isVerbose) {
                outStream.println((replace ? "Replacing" : "Appending") + " label fields with columns: " + String.join(", ", labelColumns));
                outStream.println();
            }
            outSequences = relabelSequences(sequenceMap, metadata, labelColumns, fieldDelimiter, defaultValue, replace, annotateDescription, ignoreMissing, skipMissing);
        }

        if (isVerbose) {
            outStream.println("Writing fasta file, " + outputPath + ", with " + outSequences.size() + " sequences");
            outStream.println();
        }

        writeFastaFile(outSequences, outputPath);

    }

    /**
     * Annotates the tips of a tree with a set of columns from the metadata table
     * @param sequenceMap
     * @param metadata
     * @param columnNames
     * @param replace
     * @param ignoreMissing
     */
    private List<Sequence> relabelSequences(Map<String, Sequence> sequenceMap,
                                            Map<String, CSVRecord> metadata,
                                            String[] columnNames,
                                            String headerDelimiter,
                                            String defaultValue,
                                            boolean replace,
                                            boolean annotateDescription,
                                            boolean ignoreMissing,
                                            boolean skipMissing ) {

        List<Sequence> relabelledSequences = new ArrayList<>();

        int missingCount = 0;

        for (String key : sequenceMap.keySet()) {
            Sequence sequence = sequenceMap.get(key);
            CSVRecord record = metadata.get(key);

            if (record == null) {
                if (!ignoreMissing && !skipMissing && defaultValue == null) {
                    errorStream.println("Sequence index, " + key + ", not found in metadata table and no default value supplied");
                    System.exit(1);
                } else {
                    missingCount += 1;
                }
            }

            if (record != null || defaultValue != null) {
                StringBuilder sequenceHeader = new StringBuilder();
                boolean first = true;
                if (!replace || annotateDescription) {
                    sequenceHeader.append(sequence.getTaxon().getName());
                    if (annotateDescription) {
                        sequenceHeader.append(" ");
                    } else {
                        first = false;
                    }
                }

                for (String name : columnNames) {
                    if (!first) {
                        sequenceHeader.append(headerDelimiter);
                    }
                    sequenceHeader.append(record != null ? record.get(name) : defaultValue);
                    first = false;
                }
                relabelledSequences.add(
                        new BasicSequence(sequence.getSequenceType(), Taxon.getTaxon(sequenceHeader.toString()), sequence.getStates())
                );

            } else if (ignoreMissing) {
                relabelledSequences.add(sequence);
            }
        }

        if (isVerbose && missingCount > 0) {
            if (ignoreMissing) {
                outStream.println("Unmatched sequences: " + missingCount + " left unannotated");
            } else if (skipMissing) {
                outStream.println("Unmatched sequences: " + missingCount + " skipped and not written to output");
            } else if (defaultValue != null) {
                outStream.println("Unmatched sequences: " + missingCount + " given default values");
            }
            outStream.println();
        }

        return relabelledSequences;
    }

}

