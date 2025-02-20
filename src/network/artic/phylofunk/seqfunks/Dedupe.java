package network.artic.phylofunk.seqfunks;

import java.util.*;

import jebl.evolution.sequences.Sequence;

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
                    commandLine.getOptionValue("index-column", ""),
                    Integer.parseInt(commandLine.getOptionValue("index-field", "0")),
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

        List<Sequence> sequences = readFasta(fastaFileName);

        Map<String, Sequence> sequenceMap = getSequenceMap(sequences);

        throw new UnsupportedOperationException("filtering not implemented yet");

        if (isVerbose) {
            outStream.println("Writing fasta file, " + outputFileName + ", with " + sequenceMap.size() + " sequences");
            outStream.println();
        }

        writeFastaFile(new ArrayList<>(sequenceMap.values()), outputFileName);

        if (outputMetadataFileName != null) {
            List<CSVRecord> metadataRows = new ArrayList<>();
            for (String key : sequenceMap.keySet()) {
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


}

