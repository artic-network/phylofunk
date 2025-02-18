package network.artic.phylofunk.treefunks;

import java.util.*;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedSubtree;
import jebl.evolution.trees.RootedTree;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVRecord;

import network.artic.phylofunk.funks.FunkFactory;
import static network.artic.phylofunk.treefunks.TreeOptions.*;

/**
 * Prune out a set of taxa from the tree
 */
public class Prune extends TreeFunk {
    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "prune";
        }

        @Override
        public String getDescription() {
            return "Prune out taxa from a list or based on metadata.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            options.addOption(TAXON_FILE);
            options.addOption(TAXA);
            METADATA.setRequired(false);
            options.addOption(METADATA);
            options.addOption(OUTPUT_FILE);
            options.addOption(OUTPUT_FORMAT);
            options.addOption(OUTPUT_METADATA);
            options.addOption(INDEX_COLUMN);
            options.addOption(INDEX_FIELD);
            options.addOption(FIELD_DELIMITER);
            options.addOption(KEEP_TAXA);
            options.addOption(IGNORE_MISSING);
        }

        @Override
        public void create(CommandLine commandLine, boolean isVerbose) {
            FormatType format = FormatType.NEXUS;

            if (commandLine.hasOption("f")) {
                try {
                    format = FormatType.valueOf(commandLine.getOptionValue("f").toUpperCase());
                } catch (IllegalArgumentException iae) {
                    errorStream.println("Unrecognised output format: " + commandLine.getOptionValue("f") + "\n");
                    System.exit(1);
                    return;
                }
            }

            new Prune(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("taxon-file"),
                    commandLine.getOptionValues("taxa"),
                    commandLine.getOptionValue("metadata"),
                    commandLine.getOptionValue("output"),
                    format,
                    commandLine.getOptionValue("output-metadata"),
                    commandLine.getOptionValue("id-column", ""),
                    Integer.parseInt(commandLine.getOptionValue("id-field", "0")),
                    commandLine.getOptionValue("field-delimeter", DEFAULT_DELIMITER),
                    commandLine.hasOption("keep-taxa"),
                    commandLine.hasOption("ignore-missing"),
                    isVerbose);
        }
    };


    public Prune(String treeFileName,
                 String taxaFileName,
                 String[] targetTaxa,
                 String metadataFileName,
                 String outputFileName,
                 FormatType outputFormat,
                 String outputMetadataFileName,
                 String indexColumn,
                 int indexHeader,
                 String headerDelimiter,
                 boolean keepTaxa,
                 boolean ignoreMissing,
                 boolean isVerbose) {

        super(metadataFileName, taxaFileName, indexColumn, indexHeader, headerDelimiter, isVerbose);

        List<String> targetTaxaList = new ArrayList<>(targetTaxa != null ? Arrays.asList(targetTaxa) : Collections.emptyList());
        if (taxa != null) {
            targetTaxaList.addAll(taxa);
        }

        if (targetTaxaList.size() == 0) {
            throw new IllegalArgumentException("prune command requires a taxon list and/or additional target taxa");
        }

        RootedTree tree = readTree(treeFileName);

        Map<Taxon, String> taxonMap = getTaxonMap(tree);

        if (!ignoreMissing) {
            for (String key : targetTaxaList) {
                if (!taxonMap.containsValue(key)) {
                    errorStream.println("Taxon, " + key + ", not found in tree");
                    System.exit(1);
                }
            }
        }

        // subtree option in JEBL requires the taxa that are to be included
        Set<Taxon> includedTaxa = new HashSet<>();

        for (Node tip : tree.getExternalNodes()) {
            Taxon taxon = tree.getTaxon(tip);
            String index = taxonMap.get(taxon);
            if (targetTaxaList.contains(index) == keepTaxa) {
                includedTaxa.add(taxon);
            }
        }

        if (isVerbose) {
            outStream.println("   Number of taxa pruned: " + (tree.getExternalNodes().size() - includedTaxa.size()) );
            outStream.println("Number of taxa remaining: " + includedTaxa.size());
            outStream.println();
        }

        if (includedTaxa.size() < 2) {
            errorStream.println("At least 2 taxa must remain in the tree");
            System.exit(1);
        }

        RootedTree outTree = new RootedSubtree(tree, includedTaxa);

        if (isVerbose) {
            outStream.println("Writing tree file, " + outputFileName + ", in " + outputFormat.name().toLowerCase() + " format");
            outStream.println();
        }

        writeTreeFile(outTree, outputFileName, outputFormat);

        if (outputMetadataFileName != null) {
            List<CSVRecord> metadataRows = new ArrayList<>();
            for (Taxon taxon : includedTaxa) {
                metadataRows.add(metadata.get(taxonMap.get(taxon)));
            }
            if (isVerbose) {
                outStream.println("Writing metadata file, " + outputMetadataFileName);
                outStream.println();
            }
            writeMetadataFile(metadataRows, outputMetadataFileName);
        }
    }
}

