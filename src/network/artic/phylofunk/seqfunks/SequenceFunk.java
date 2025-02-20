package network.artic.phylofunk.seqfunks;

import jebl.evolution.graphs.Node;
import jebl.evolution.io.*;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.util.ProgressListener;
import network.artic.phylofunk.funks.Funk;
import network.artic.phylofunk.treefunks.FormatType;
import network.artic.phylofunk.treefunks.TreeProcessor;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * @author Andrew Rambaut
 * @version $
 */
public class SequenceFunk extends Funk {

    Set<String> taxa = null;

    /**
     * Constructor
     * @param isVerbose
     */
    SequenceFunk(boolean isVerbose) {
        super(isVerbose);
    }

    /**
     * Constructor
     * @param metadataFileName
     * @param taxaFileName
     * @param indexColumn
     * @param indexField
     * @param fieldDelimiter
     * @param isVerbose
     */
    SequenceFunk(String metadataFileName, String taxaFileName, String indexColumn, int indexField, String fieldDelimiter, boolean isVerbose) {
        super(metadataFileName, indexColumn, indexField, fieldDelimiter, isVerbose);

        if (taxaFileName != null) {
            readTaxa(taxaFileName, indexColumn);
        }
    }

    /**
     * Read a fasta file
     * @param fastaFileName
     * @return
     */
    List<Sequence> readFasta(String fastaFileName) {

        List<Sequence> sequences = null;
        try {
            FastaImporter importer = new FastaImporter(new FileReader(fastaFileName), SequenceType.NUCLEOTIDE);
            sequences = importer.importSequences();

        } catch (IOException ioe) {
            errorStream.println("Error reading fasta file, " + fastaFileName + ": " + ioe.getMessage());
            System.exit(1);
        } catch (ImportException ie) {
            errorStream.println("Error parsing fasta file, " + fastaFileName + ": " + ie.getMessage());
            System.exit(1);
        }


        if (isVerbose) {
            outStream.println("Read fasta file: " + fastaFileName);
            outStream.println("        Sequences: " + sequences.size());
            outStream.println();
        }

        return sequences;
    }

    final void processSequences(String sequenceFileName, SequenceProcessor process) {
        processSequences(sequenceFileName, null, process);
    }

    final void processSequences(String sequenceFileName, String outputFileName, SequenceProcessor process) {

        if (isVerbose) {
            outStream.println("  Reading sequence file: " + sequenceFileName);
        }

        FastaImporter importer = null;
        FastaExporter exporter1 = null;

        try {
            importer = new FastaImporter(new FileReader(sequenceFileName), SequenceType.NUCLEOTIDE);
        } catch (IOException ioe) {
            errorStream.println("Error reading sequence file: " + ioe.getMessage());
            System.exit(1);
        }

        FileWriter writer = null;

        if (outputFileName != null) {
            try {
                if (isVerbose) {
                    outStream.println("  Writing sequence file: " + outputFileName);
                }
                writer = new FileWriter(outputFileName);
                exporter1 = new FastaExporter(writer);
            } catch (IOException ioe) {
                errorStream.println("Error writing sequence file: " + ioe.getMessage());
                System.exit(1);
            }
        }

        final FastaExporter exporter = exporter1;

        try {
            final int[] count = {0};

            ImmediateSequenceImporter.Callback callback = seq -> {
                Sequence outSeq = process.processSequence(seq);

                try {
                    if (outSeq != null) {
                        exporter.exportSequence(outSeq);
                    }
                } catch (IOException ioe) {
                    errorStream.println("Error processing sequence file: " + ioe.getMessage());
                    System.exit(1);
                }

                count[0]++;
                if (isVerbose && count[0] % 10000 == 0) {
                    outStream.println("Number of sequences processed: " + count[0]);
                }
            };
            importer.importSequences(callback, ProgressListener.EMPTY);

            if (isVerbose) {
                outStream.println("Total sequences processed: " + count[0]);
                outStream.println();
            }

        } catch (ImportException ie) {
            errorStream.println("Error parsing sequence file, " + sequenceFileName + ": " + ie.getMessage());
            System.exit(1);
        } catch (IOException ioe) {
            errorStream.println("Error processing sequence file: " + ioe.getMessage());
            System.exit(1);
        }

    }

    protected void readTaxa(String taxaFileName, String indexColumn) {
        if (isVerbose) {
            outStream.println("Reading taxa list: " + taxaFileName);
        }

        try {
            TreeImporter importer = null;

            String format = getTreeFileType(new FileReader(taxaFileName));

            if (format.equals("nexus")) {
                importer = new NexusImporter(new FileReader(taxaFileName));
            } else if (format.equals("newick")) {
                importer = new NewickImporter(new FileReader(taxaFileName), false);
            } else {
                // not a tree file - do nothing...
            }

            if (importer != null) {
                RootedTree tree = (RootedTree) importer.importNextTree();
                taxa = new HashSet<>(getTaxonMap(tree).values());
            } else {
                taxa = readCSV(taxaFileName, indexColumn).keySet();
            }

        } catch (IOException ioe) {
            errorStream.println("Error reading taxon file, " + taxaFileName + ": " + ioe.getMessage());
            System.exit(1);
        } catch (ImportException ie) {
            errorStream.println("Error parsing taxon file, " + taxaFileName + ": " + ie.getMessage());
            System.exit(1);
        }


        if (isVerbose) {
            outStream.println("        Number: " + taxa.size());
            outStream.println();
        }
    }

    final Map<String, Sequence> getSequenceMap(List<Sequence> sequences) {
        return getSequenceMap(sequences, false);
    }

    final Map<String, Sequence> getSequenceMap(List<Sequence> sequences, boolean dedupe) {
        Map<String, Sequence> sequenceMap = new HashMap<>();

        for (Sequence sequence : sequences) {
            String index = getSequenceID(sequence);
            if (!dedupe && isVerbose && sequenceMap.containsKey(index)) {
                outStream.println("FASTA file contains duplicate id: " + index);
            }
            sequenceMap.put(index, sequence);
        }

        return sequenceMap;
    }

    final String getSequenceID(Sequence sequence) {
        String index = sequence.getTaxon().getName();
        if (indexField > 0) { // index header fields indexed from 1
            index = getLabelField(index, indexField, fieldDelimiter);
        }
        return index;
    }

    final String getLabelField(String label, int labelField, String fieldDelimiter) {
        if (labelField > 0) { // label fields indexed from 1
            String[] headers = label.split(fieldDelimiter);
            if (labelField > headers.length) {
                errorStream.println("Sequence label, " + label + ", doesn't have enough fields (index-header = " + labelField + ")");
                System.exit(1);
            }
            return headers[labelField - 1];
        }
        return null;
    }


    final Map<Taxon, String> getTaxonMap(RootedTree tree) {
        Map<Taxon, String> taxonMap = new HashMap<>();

        for (Node tip : tree.getExternalNodes()) {
            Taxon taxon = tree.getTaxon(tip);
            String index = taxon.getName();
            if (indexField > 0) { // index header indexed from 1
                // if an index header field has been specified then split it out (otherwise use the entire name)
                String[] headers = taxon.getName().split(fieldDelimiter);
                if (indexField > headers.length) {
                    errorStream.println("Tip name, " + taxon.getName() + ", doesn't have enough fields (index-field = " + indexField + ")");
                    System.exit(1);
                }
                index = headers[indexField - 1];
            }
            taxonMap.put(taxon, index);
        }

        return taxonMap;
    }

    private String getTreeFileType(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();
        while (line != null && line.length() == 0) {
            line = bufferedReader.readLine();
        }

        if (line != null && line.trim().toUpperCase().startsWith("#NEXUS")) {
            return "nexus";
        }
        if (line != null && line.trim().toUpperCase().startsWith("(")) {
            return "newick";
        }

        return "none";
    }


    /**
     * Writes a fasta file with a list of sequences
     * @param sequences
     * @param fileName
     */
    static void writeFastaFile(List<Sequence> sequences, String fileName) {
        try {
            FileWriter writer = new FileWriter(fileName);

            SequenceExporter exporter = new FastaExporter(writer);

            exporter.exportSequences(sequences);
            writer.close();
        } catch (IOException e) {
            errorStream.println("Error writing fasta file: " + e.getMessage());
            System.exit(1);
        }
    }



}
