package network.artic.phylofunk.seqfunks;

import java.util.*;

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
public class Dedupe extends SequenceFunk {
    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "dedupe";
        }

        @Override
        public String getDescription() {
            return "Remove genomes with duplicate id or metadata.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            options.addOption(OUTPUT_FILE);
            options.addOption(OUTPUT_METADATA);
            METADATA.setRequired(false);
            options.addOption(METADATA);
            options.addOption(INDEX_COLUMN);
            options.addOption(INDEX_FIELD);
            options.addOption(TARGET_COLUMN);
            options.addOption(TARGET_FIELD);
            options.addOption(FIELD_DELIMITER);
        }

        @Override
        public void create(CommandLine commandLine, boolean isVerbose) {
            new Dedupe(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("metadata"),
                    commandLine.getOptionValue("output"),
                    commandLine.getOptionValue("output-metadata"),
                    commandLine.getOptionValue("id-column", ""),
                    Integer.parseInt(commandLine.getOptionValue("id-field", "0")),
                    commandLine.getOptionValue("target-column", ""),
                    Integer.parseInt(commandLine.getOptionValue("target-field", "0")),
                    commandLine.getOptionValue("field-delimeter", "|"),
                    isVerbose);
        }

    };

    public Dedupe(String fastaFileName,
                  String metadataFileName,
                  String outputFileName,
                  String outputMetadataFileName,
                  String indexColumn,
                  int indexField,
                  String targetColumn,
                  int targetField,
                  String headerDelimiter,
                  boolean isVerbose) {

        super(metadataFileName, null, indexColumn, indexField, headerDelimiter, isVerbose);

        if (!targetColumn.isEmpty()) {
            if (metadata == null) {
                errorStream.println("To use a target column to dedupe, a metadata file must be provided.");
                System.exit(1);
            }
            if (!columnNames.contains(targetColumn)) {
                errorStream.println("Metadata doesn't contain target column, " + targetColumn + ".");
                System.exit(1);
            }
            if (isVerbose) {
                outStream.println("De-duplicating sequences using metadata column, " + targetColumn + ".");
                outStream.println();
            }
        } else {
            if (targetField > 1) {
                outStream.println("De-duplicating sequences using label field " + targetField + ".");
                outStream.println();
            } else  {
                outStream.println("De-duplicating sequences using the entire sequence labels.");
                outStream.println();
            }
        }

        List<Sequence> sequences = readFasta(fastaFileName);

        Map<String, Sequence> sequenceMap = getSequenceMap(sequences, true);

        Map<String, Sequence> outSequences;

        if (!targetColumn.isEmpty() || targetField > 0) {
            outSequences = dedupeSequences(sequenceMap, metadata, targetColumn, targetField, fieldDelimiter);
        } else {
            // dedupe on the entire sequence label which will already have been done in the sequenceMap
            outSequences = sequenceMap;
        }

        if (isVerbose) {
            outStream.println("Writing fasta file, " + outputFileName + ", with " + outSequences.size() + " de-duplicated sequences.");
            outStream.println();
        }

        writeFastaFile(new ArrayList<>(outSequences.values()), outputFileName);

        if (outputMetadataFileName != null && metadata != null) {
            List<CSVRecord> metadataRows = new ArrayList<>();
            for (String key : outSequences.keySet()) {
                if (metadata.containsKey(key)) {
                    metadataRows.add(metadata.get(key));
                }
            }
            if (isVerbose) {
                outStream.println("Writing metadata file, " + outputMetadataFileName);
                outStream.println();
            }
            writeMetadataFile(metadataRows, outputMetadataFileName);
        }

    }

    private Map<String, Sequence> dedupeSequences(Map<String, Sequence> sequenceMap, Map<String, CSVRecord> metadata, String targetColumn, int targetField, String fieldDelimiter) {
        Map<String, String> uniqueMap = new HashMap<>();

        for (String key : sequenceMap.keySet()) {
            String targetValue = null;
            if (metadata != null) {
                CSVRecord record = metadata.get(key);

                if (record == null) {
                    errorStream.println("Sequence index, " + key + ", not found in metadata table.");
                    System.exit(1);
                }

                targetValue = record.get(targetColumn);
            } else if (targetField > 0) {
                targetValue = getLabelField(key, targetField, fieldDelimiter);
            }

            uniqueMap.put(targetValue, key);
        }

//        if (isVerbose && missingCount > 0) {
//            if (ignoreMissing) {
//                outStream.println("Unmatched sequences: " + missingCount + " left unannotated");
//            } else if (skipMissing) {
//                outStream.println("Unmatched sequences: " + missingCount + " skipped and not written to output");
//            } else if (defaultValue != null) {
//                outStream.println("Unmatched sequences: " + missingCount + " given default values");
//            }
//            outStream.println();
//        }

        Map<String, Sequence> uniqueSequenceMap = new HashMap<>();
        for (String key : uniqueMap.values()) {
            uniqueSequenceMap.put(key, sequenceMap.get(key));
        }

        return uniqueSequenceMap;
    }
}

