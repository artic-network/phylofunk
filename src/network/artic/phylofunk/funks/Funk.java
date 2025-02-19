package network.artic.phylofunk.funks;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for all funk commands. Provides some static utility functions.
 */
public abstract class Funk {
    public final static PrintStream errorStream = System.err;
    public final static PrintStream outStream = System.out;

    public final boolean isVerbose;
    public final String indexColumn;
    public final int indexField;
    public final String fieldDelimiter;

    protected Map<String, CSVRecord> metadata = null;
    protected CSVRecord headerRecord = null;

    Set<String> keys = null;


    List<String> metadataHeaders = null;


    /**
     * Constructor
     * @param isVerbose
     */
    public Funk(boolean isVerbose) {
        this.isVerbose = isVerbose;
        this.indexColumn = null;
        this.indexField = -1;
        this.fieldDelimiter = null;
    }

    /**
     * Constructor
     * @param metadataFileName
     * @param indexColumn
     * @param indexField
     * @param fieldDelimiter
     * @param isVerbose
     */
    public Funk(String metadataFileName, String indexColumn, int indexField, String fieldDelimiter, boolean isVerbose) {
        this.isVerbose = isVerbose;

        this.indexColumn = indexColumn;
        this.indexField = indexField;
        if ("|".equals(fieldDelimiter)) {
            this.fieldDelimiter = "\\|";
        } else {
            this.fieldDelimiter = fieldDelimiter;
        }

        if (metadataFileName != null) {
            readMetadataTable(metadataFileName, indexColumn);
        }
    }

    public static FunkFactory getCommandFactory(String name, FunkFactory[] commandFactories) {
        for (FunkFactory factory : commandFactories) {
            if (name.equalsIgnoreCase(factory.getName())) {
                return factory;
            }
        }

        throw new IllegalArgumentException("Command not found");
    }



    protected final void readMetadataTable(String metadataFileName, String indexColumn) {
        metadata = readCSV(metadataFileName, indexColumn);
        keys = metadata.keySet();

        if (isVerbose) {
            outStream.println("Read metadata table: " + metadataFileName);
            outStream.println("               Rows: " + metadata.size());
            outStream.println("       Index column: " + (indexColumn == null ? headerRecord.getParser().getHeaderNames().get(0) : indexColumn));
            outStream.println();
        }
    }

    protected final Map<String, CSVRecord> readCSV(String fileName, String indexColumn) {
        Map<String, CSVRecord> csv = new HashMap<>();
        try {
            Reader in = new FileReader(fileName);
            CSVParser parser = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            if (indexColumn != null) {
                // a particular column is used to index - check it is there for the first record
                // and use it to key the records

                boolean first = true;
                for (CSVRecord record : parser) {
                    if (first) {
                        headerRecord = record;
                        if (record.get(indexColumn) == null) {
                            errorStream.println("Index column, " + indexColumn + " not found in metadata table");
                            System.exit(1);
                        }
                        first = false;
                    }
                    String key = record.get(indexColumn);
                    if (!key.isEmpty()) {
                        if (csv.containsKey(key)) {
                            errorStream.println("Duplicate index value, " + key + " in metadata table");
//                        System.exit(1);
                        }
                        csv.put(key, record);
                    }
                }
            } else {
                // key the records against the first column
                boolean first = true;
                for (CSVRecord record : parser) {
                    if (first) {
                        headerRecord = record;
                        first = false;
                    }
                    String key = record.get(0);
                    if (!key.isEmpty()) {
                        if (csv.containsKey(key)) {
                            errorStream.println("Duplicate index value, " + key + " in metadata table");
//                        System.exit(1);
                        }
                        csv.put(key, record);
                    }
                }
            }
        } catch (IOException e) {
            errorStream.println("Error reading metadata file: " + e.getMessage());
            System.exit(1);
        }
        return csv;
    }

    /**
     * Writes a csv file
     * @param records
     * @param fileName
     */
    protected final void writeMetadataFile(List<CSVRecord> records, String fileName) {
        writeCSVFile(records, fileName);
    }

    /**
     * Writes a csv file
     * @param records
     * @param fileName
     */
    private static void writeCSVFile(List<CSVRecord> records, String fileName) {
        try {
            PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(fileName)));

            List<String> headerNames = records.get(0).getParser().getHeaderNames();
            writer.println(String.join(",", headerNames));

            for (CSVRecord record : records) {
                boolean first = true;
                for (String value : record) {
                    if (first) {
                        writer.print(value);
                        first = false;
                    } else {
                        writer.print(",");
                        writer.print(value);
                    }
                }
                writer.println();
            }

            writer.close();
        } catch (IOException e) {
            errorStream.println("Error writing metadata file: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Writes a text file
     * @param lines
     * @param fileName
     */
    static void writeTextFile(List<String> lines, String fileName) {
        try {
            PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(fileName)));

            for (String line : lines) {
                writer.println(line);
            }

            writer.close();
        } catch (IOException e) {
            errorStream.println("Error writing text file: " + e.getMessage());
            System.exit(1);
        }

    }}
