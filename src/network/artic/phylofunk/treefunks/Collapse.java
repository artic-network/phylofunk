package network.artic.phylofunk.treefunks;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.MutableRootedTree;
import jebl.evolution.trees.RootedTree;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import network.artic.phylofunk.funks.FunkFactory;
import static network.artic.phylofunk.treefunks.TreeOptions.*;

/**
 * Collapses all branches less than a threshold to form a polytomy
 */
public class Collapse extends TreeFunk {

    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "collapse";
        }

        @Override
        public String getDescription() {
            return "Collapse branch lengths below a threshold into polytomies.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            options.addOption(OUTPUT_FILE);
            options.addOption(OUTPUT_FORMAT);
            options.addOption(BRANCH_THRESHOLD);
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

            new Collapse(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("output"),
                    format,
                    Double.parseDouble(commandLine.getOptionValue("threshold", "0.0")),
                    isVerbose);
        }

    };

    public Collapse(String treeFileName,
                    String outputPath,
                    FormatType outputFormat,
                    double branchThreshold,
                    boolean isVerbose) {

        super(isVerbose);

        if (branchThreshold <= 0.0) {
            errorStream.println("Branch length threshold value should be > 0.0");
            System.exit(1);
        }

        RootedTree tree = readTree(treeFileName);

        MutableRootedTree outTree = new MutableRootedTree(tree);

        int count = 0;
        for (Node node : outTree.getInternalNodes()) {
            if (outTree.getLength(node) < branchThreshold) {
                Node parent = outTree.getParent(node);
                if (parent != null) {
                    outTree.removeChild(node, parent);
                    for (Node child : outTree.getChildren(node)) {
                        outTree.addChild(child, parent);
                    }
                    count += 1;
                }

            }
        }

        if (isVerbose) {
            outStream.println("Branches collapsed: " + count);
            outStream.println();
            outStream.println("Writing tree file, " + outputPath + ", in " + outputFormat.name().toLowerCase() + " format");
            outStream.println();
        }
        writeTreeFile(outTree, outputPath, outputFormat);
    }

}

