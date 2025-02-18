package network.artic.phylofunk.seqfunks;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.SequenceType;
import jebl.util.BasicProgressListener;
import network.artic.phylofunk.funks.FunkFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import static network.artic.phylofunk.funks.FunkOptions.INPUT;
import static network.artic.phylofunk.funks.FunkOptions.OUTPUT_FILE;

/**
 * Command to find calculate unique hashes for sequences
 */
public class Hashes extends SequenceFunk {

    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "hashes";
        }

        @Override
        public String getDescription() {
            return "Generate unique hash codes for sequences.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            options.addOption(OUTPUT_FILE);
        }

        @Override
        public void create(CommandLine commandLine, boolean isVerbose) {
            new Hashes(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("output"),
                    8,
                    isVerbose);
        }

    };


    public Hashes(String alignmentFileName,
                  String outputFileName,
                  int length1,
                  boolean isVerbose) {

        super(isVerbose);

        int length = 6;

        long startTime = System.currentTimeMillis();

        if (isVerbose) {
            outStream.println("Writing hash codes for sequences");
        }

        final MessageDigest sha;

        try {
            sha = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException ex) {
            throw new UnsupportedOperationException("Unable to create hash algorithm " + ex.getMessage());
        }

        AtomicInteger count = new AtomicInteger();
        AtomicInteger collisionCount = new AtomicInteger();

        try {
            PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(outputFileName)));

            writer.print("sequence_name,full_hash");
            if (length > 0) {
                writer.print(",short_hash");
            }
            writer.println();

            Set<String> hashSet = new HashSet<>();
            Map<String, String> shortenedHashMap = new HashMap<>();
            Set<String> sequenceSet = new HashSet<>();

            FastaImporter importer = new FastaImporter(new FileReader(alignmentFileName), SequenceType.NUCLEOTIDE);
            importer.importSequences(
                    sequence -> {
                        String seq = sequence.getString();
                        byte[] result =  sha.digest(seq.getBytes());

                        String hash = hexEncode(result);

                        writer.print(sequence.getTaxon().getName() + "," + hash);

                        boolean seqMatch = sequenceSet.contains(seq);
                        boolean hashMatch = hashSet.contains(hash);

                        sequenceSet.add(seq);
                        hashSet.add(hash);

                        if (!seqMatch && hashMatch) {
                            errorStream.println("Long hash collision: " + hash);
                        }
                        if (seqMatch && !hashMatch){
                            errorStream.println("Long hash not matching: " + hash);
                        }

                        if (length > 0) {
                            String shortenedHash = hash;
                            if (hash.length() > length) {
                                shortenedHash = hash.substring(hash.length() - length);
                            }

                            if (!hashMatch && shortenedHashMap.containsKey(shortenedHash)) {
                                errorStream.println("Short hash collision: " + shortenedHash + " -> " + hash + " | " + shortenedHashMap.get(shortenedHash));
                                collisionCount.addAndGet(1);
                            }

                            shortenedHashMap.put(shortenedHash, hash);

                            writer.print("," + shortenedHash);
                        }

                        writer.println();

                        count.addAndGet(1);
                    },
                    new BasicProgressListener());
            writer.close();
        } catch (IOException ioe) {
            errorStream.println("Error reading fasta file, " + alignmentFileName + ": " + ioe.getMessage());
            System.exit(1);
        } catch (ImportException ie) {
            errorStream.println("Error parsing fasta file, " + alignmentFileName + ": " + ie.getMessage());
            System.exit(1);
        }

        if (isVerbose) {
            outStream.println("Sequences processed: " + count);
            outStream.println("Short hash collisions: " + collisionCount);
            long endTime = System.currentTimeMillis();
            outStream.println("Time taken: " + ((double)(endTime - startTime) / 1000) + " secs");
        }

    }

    static private String hexEncode(byte[] input){
        StringBuilder result = new StringBuilder();
        char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f'};
        for (int idx = 0; idx < input.length; ++idx) {
            byte b = input[idx];
            result.append(digits[ (b&0xf0) >> 4 ]);
            result.append(digits[ b&0x0f]);
        }
        return result.toString();
    }

}

