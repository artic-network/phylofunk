package network.artic.phylofunk.funks;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVRecord;

import static network.artic.phylofunk.funks.FunkOptions.*;

/**
 *
 */
public class Merge2 extends Funk {
    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "merge";
        }

        @Override
        public String getDescription() {
            return "Merge two metadata files.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            options.addOption(OUTPUT_FILE);
            options.addOption(METADATA);
            options.addOption(INDEX_COLUMN);
            options.addOption(HEADER_FIELDS);
            options.addOption(REPLACE);
            options.addOption(IGNORE_MISSING);
        }

        @Override
        public void create(CommandLine commandLine, boolean isVerbose) {
            new Merge2(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("metadata"),
                    commandLine.getOptionValue("output"),
                    commandLine.getOptionValue("index-column", ""),
                    commandLine.getOptionValues("header-fields"),
                    commandLine.hasOption("ignore-missing"),
                    isVerbose);
        }

    };
    public Merge2(String metadataFileName1,
                 String metadataFileName2,
                 String outputFileName,
                 String indexColumn,
                 String[] headerColumns,
                 boolean ignoreMissing,
                 boolean isVerbose) {

        super(isVerbose);

        Map<String, CSVRecord> metadata1 = readCSV(metadataFileName1, indexColumn);
        List<String> metadataHeaders1 = new LinkedList<>(metadata1.values().iterator().next().getParser().getHeaderNames());
        if (indexColumn != null && !metadataHeaders1.contains(indexColumn)) {
            errorStream.println("Index column, " + indexColumn + ", not found in metadata file " + metadataFileName1);
            System.exit(1);
        }
        keys = metadata1.keySet();

        Map<String, CSVRecord> metadata2 = readCSV(metadataFileName2, indexColumn);
        List<String> metadataHeaders2 = new LinkedList<>(metadata2.values().iterator().next().getParser().getHeaderNames());
        if (indexColumn != null && !metadataHeaders2.contains(indexColumn)) {
            errorStream.println("Index column, " + indexColumn + ", not found in metadata file " + metadataFileName2);
            System.exit(1);
        }

        String indexColumn1 = (indexColumn == null ? metadataHeaders1.get(0) : indexColumn);
        String indexColumn2 = (indexColumn == null ? metadataHeaders2.get(0) : indexColumn);

        if (isVerbose) {
            outStream.println("Read metadata table: " + metadataFileName1);
            outStream.println("               Rows: " + metadata1.size());
            outStream.println("       Index column: " + indexColumn1);
            outStream.println();
            outStream.println("Read metadata table: " + metadataFileName2);
            outStream.println("               Rows: " + metadata2.size());
            outStream.println("       Index column: " + indexColumn2);
            outStream.println();
        }

        if (!ignoreMissing) {
            for (String key : metadata1.keySet()) {
                CSVRecord record2 = metadata2.get(key);
                if (record2 == null) {
                    errorStream.println("Row index, " + key + ", not found in second metadata table");
                    System.exit(1);
                }
            }
        }

        try {
            if (isVerbose) {
                outStream.println("Writing output metadata file: " + outputFileName);
                outStream.println();
            }

            PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(outputFileName)));

            metadataHeaders2.remove(indexColumn2);
            for (String header : metadataHeaders1) {
                metadataHeaders2.remove(header);
            }

            writer.print(String.join(",", metadataHeaders1));
            writer.print("," + String.join(",", metadataHeaders2));
            writer.println();

            for (String key : metadata1.keySet()) {
                CSVRecord record1 = metadata1.get(key);
                CSVRecord record2 = metadata2.get(key);

                boolean first = true;
                for (String header : metadataHeaders1) {
                    if (!first) {
                        writer.print(',');
                    } else {
                        first = false;
                    }
                    if (record2 != null && record2.isSet(header)) {
                        printValue(writer, record2.get(header));
                    } else {
                        printValue(writer, record1.get(header));
                    }
                }

                for (String header : metadataHeaders2) {
                    writer.print(',');
                    if (record2 != null && record2.isSet(header)) {
                        printValue(writer, record2.get(header));
                    }
                }

                writer.println();
            }
            writer.close();
        } catch (IOException ioe) {
            errorStream.println("Error writing metadata file, " + outputFileName + ": " + ioe.getMessage());
            System.exit(1);
        }


    }

    private void printValue(PrintWriter writer, String value) {
        if (value.contains(",")) {
            writer.print("\"" + value + "\"");
        } else {
            writer.print(value);
        }
    }

}

