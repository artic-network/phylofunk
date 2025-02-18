package network.artic.phylofunk.seqfunks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.taxa.Taxon;

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
            options.addOption(HEADER_FIELDS);
            options.addOption(REPLACE);
            options.addOption(ANNOTATE_DESCRIPTION);
            options.addOption(IGNORE_MISSING);
        }

        @Override
        public void create(CommandLine commandLine, boolean isVerbose) {
            new Annotate(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("metadata"),
                    commandLine.getOptionValue("output"),
                    commandLine.getOptionValue("index-column", ""),
                    Integer.parseInt(commandLine.getOptionValue("index-field", "0")),
                    commandLine.getOptionValue("field-delimiter", "|"),
                    commandLine.getOptionValue("default-value"),
                    commandLine.getOptionValues("header-fields"),
                    commandLine.hasOption("replace"),
                    commandLine.hasOption("annotate-description"),
                    commandLine.hasOption("ignore-missing"),
                    commandLine.hasOption("remove-missing"),
                    isVerbose);
        }

    };

    public Annotate(String fastaFileName,
                    String metadataFileName,
                    String outputPath,
                    String indexColumn,
                    int indexHeader,
                    String headerDelimiter,
                    String defaultValue,
                    String[] headerColumns,
                    boolean replace,
                    boolean annotateDescription,
                    boolean ignoreMissing,
                    boolean removeMissing,
                    boolean isVerbose) {

        super(metadataFileName, null, indexColumn, indexHeader, headerDelimiter, isVerbose);

        List<Sequence> sequences = readFasta(fastaFileName);

        Map<String, Sequence> sequenceMap = getSequenceMap(sequences);

        List<Sequence> outSequences = new ArrayList<>();

        if (headerColumns != null && headerColumns.length > 0) {
            if (isVerbose) {
                outStream.println((replace ? "Replacing" : "Appending") + " header fields with columns: " + String.join(", ", headerColumns));
                outStream.println();
            }
            outSequences = relabelSequences(sequenceMap, metadata, headerColumns, headerDelimiter, defaultValue, replace, annotateDescription, ignoreMissing, removeMissing);
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
                                            boolean removeMissing ) {

        List<Sequence> relabelledSequences = new ArrayList<>();

        for (String key : sequenceMap.keySet()) {
            Sequence sequence = sequenceMap.get(key);
            CSVRecord record = metadata.get(key);

            if (record == null && !ignoreMissing && !removeMissing && defaultValue == null) {
                errorStream.println("Sequence index, " + key + ", not found in metadata table and no default value supplied");
                System.exit(1);
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

        return relabelledSequences;
    }


}

