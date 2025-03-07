package network.artic.phylofunk.treefunks;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.MissingTaxonException;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.RootedTreeUtils;

import network.artic.phylofunk.funks.FunkOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import network.artic.phylofunk.funks.FunkFactory;

import static network.artic.phylofunk.funks.FunkOptions.*;
import static network.artic.phylofunk.funks.FunkOptions.INDEX_COLUMN;
import static network.artic.phylofunk.funks.FunkOptions.INDEX_FIELD;
import static network.artic.phylofunk.treefunks.TreeOptions.*;

/**
 * Finds the time of most recent common ancestor of a set of taxa
 */
public class TMRCA extends TreeFunk {
    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "tmrca";
        }

        @Override
        public String getDescription() {
            return "Extract a TMRCA for a set of taxa from a list of trees.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            options.addOption(TAXON_FILE);
            options.addOption(OUTPUT_FILE);
            options.addOption(INDEX_COLUMN);
            options.addOption(INDEX_FIELD);
            options.addOption(FunkOptions.FIELD_DELIMITER);
            options.addOption(STEM);
        }

        @Override
        public void create(CommandLine commandLine, boolean isVerbose) {
            new TMRCA(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("taxon-file"),
                    commandLine.getOptionValues("taxa"),
                    commandLine.getOptionValue("output"),
                    commandLine.getOptionValue("id-column", ""),
                    Integer.parseInt(commandLine.getOptionValue("id-field", "0")),
                    commandLine.getOptionValue("field-delimeter", DEFAULT_DELIMITER),
                    commandLine.hasOption("stem"),
                    commandLine.hasOption("ignore-missing"),
                    isVerbose);
        }
    };

    public TMRCA(String treeFileName,
                 String taxaFileName,
                 String[] targetTaxa,
                 String outputFileName,
                 String indexColumn,
                 int indexHeader,
                 String headerDelimiter,
                 boolean isStem,
                 boolean ignoreMissing,
                 boolean isVerbose) {

        super(null, taxaFileName, indexColumn, indexHeader, headerDelimiter, isVerbose);

        if (isVerbose) {
            outStream.println("Finding TMRCAs in trees for taxon set" );
            outStream.println();
        }

        final Set<Taxon> taxonSet = new HashSet<>();
        for (String taxonName : taxa) {
            taxonSet.add(Taxon.getTaxon(taxonName));
        }

        if (targetTaxa != null) {
            for (String taxonName : targetTaxa) {
                taxonSet.add(Taxon.getTaxon(taxonName));
            }
        }

        PrintWriter tmpWriter = null;
        if (outputFileName != null) {
            try {
                tmpWriter = new PrintWriter(Files.newBufferedWriter(Paths.get(outputFileName)));
            } catch (IOException ioe) {
                errorStream.println("Error opening output file: " + ioe.getMessage());
                System.exit(1);
            }
        }

        final PrintWriter outputMetadataWriter = tmpWriter;

        if (outputMetadataWriter != null) {
            outputMetadataWriter.print("tree");
            outputMetadataWriter.print("\t");
            outputMetadataWriter.print("tmrca");
            outputMetadataWriter.println();

        }

        processTrees(treeFileName, tree -> {
            double tmrca = 0;

            try {
                Set<Node> tips = RootedTreeUtils.getTipsForTaxa(tree, taxonSet);
                tmrca = findTMRCA(tree, tips, isStem);
            } catch (MissingTaxonException mte) {
                errorStream.println("Tip missing: " + mte.getMessage());
                System.exit(1);
            }

            if (outputMetadataWriter != null) {
                        outputMetadataWriter.print(tree.getAttribute("name"));
                        outputMetadataWriter.print("\t");
                        outputMetadataWriter.print(tmrca);
                        outputMetadataWriter.println();
            }

            return null;
        });

        if (outputMetadataWriter != null) {
            outputMetadataWriter.close();
        }

    }

    /**
     * When ever a change in the value of a given attribute occurs at a node, creates a new cluster number and annotates
     * descendents with that cluster number.
     * @param tree
     */
    double findTMRCA(RootedTree tree, Set<Node> tips, boolean isStem) {
        Node mrca = RootedTreeUtils.getCommonAncestorNode(tree, tips);
        if (isStem && tree.getParent(mrca) != null) {
            return tree.getHeight(tree.getParent(mrca));
        }
        return tree.getHeight(mrca);
    }

}

