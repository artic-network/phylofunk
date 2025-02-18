package network.artic.phylofunk.treefunks;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.MutableRootedTree;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.RootedTreeUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import network.artic.phylofunk.funks.FunkFactory;
import static network.artic.phylofunk.treefunks.TreeOptions.*;


/**
 * Scales branch lengths of a tree by a factor or to a specified root height.
 */
public class Scale extends TreeFunk {
    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "scale";
        }

        @Override
        public String getDescription() {
            return "Scale all the branch lengths in a tree by a factor.";
        }

        @Override
        public void setOptions(Options options) {
            BRANCH_THRESHOLD.setRequired(false);
            options.addOption(INPUT);
            options.addOption(OUTPUT_FILE);
            options.addOption(OUTPUT_FORMAT);
            OptionGroup scaleGroup = new OptionGroup();
            scaleGroup.addOption(SCALE_FACTOR);
            scaleGroup.addOption(ROOT_HEIGHT);
            options.addOptionGroup(scaleGroup);
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

            if (commandLine.hasOption("height") && commandLine.hasOption("factor")) {
                errorStream.println("Use only one of the 'factor' or 'height' options\n");
                System.exit(1);
                return;
            }

            new Scale(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("output"),
                    format,
                    Double.parseDouble(commandLine.getOptionValue("factor", "1.0")),
                    Double.parseDouble(commandLine.getOptionValue("threshold", "-1.0")),
                    commandLine.hasOption("height"),
                    Double.parseDouble(commandLine.getOptionValue("height", "1.0")),
                    isVerbose);
        }
    };

    public Scale(String treeFileName,
                 String outputPath,
                 FormatType outputFormat,
                 double scaleFactor,
                 double branchThreshold,
                 boolean scaleRootHeight,
                 double rootHeight,
                 boolean isVerbose) {

        super(isVerbose);

        RootedTree tree = readTree(treeFileName);

        if (scaleRootHeight) {
            if (rootHeight <= 0.0) {
                errorStream.println("Root height should be > 0.0");
                System.exit(1);
            }
            double tipDistance = RootedTreeUtils.getAverageTipDistance(tree, tree.getRootNode());
            scaleFactor = rootHeight / tipDistance;
            if (isVerbose) {
                outStream.println("Scaling root height to: " + rootHeight);
            }
        }
        if (scaleFactor <= 0.0) {
            errorStream.println("Scale factor should be > 0.0");
            System.exit(1);
        }
        if (isVerbose) {
            outStream.println("Scaling all branch lengths by: " + scaleFactor);
        }

        MutableRootedTree outTree = new MutableRootedTree(tree);

        for (Node node : outTree.getNodes()) {
            outTree.setLength(node, outTree.getLength(node) * scaleFactor);
        }
        int scaleCount = outTree.getNodes().size();

        int collapseCount = 0;
        if (branchThreshold >= 0.0) {
            for (Node node : outTree.getInternalNodes()) {
                if (outTree.getLength(node) <= branchThreshold) {
                    Node parent = outTree.getParent(node);
                    if (parent != null) {
                        outTree.removeChild(node, parent);
                        for (Node child : outTree.getChildren(node)) {
                            outTree.addChild(child, parent);
                        }
                        collapseCount += 1;
                    }

                }
            }
        }

        if (isVerbose) {
            outStream.println("Branches scaled: " + scaleCount);
            if (branchThreshold >= 0.0) {
                outStream.println("Branches collapsed: " + collapseCount);
            }
            outStream.println();
            outStream.println("Writing tree file, " + outputPath + ", in " + outputFormat.name().toLowerCase() + " format");
            outStream.println();
        }
        writeTreeFile(outTree, outputPath, outputFormat);
    }

}

