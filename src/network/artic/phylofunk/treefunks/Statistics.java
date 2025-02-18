package network.artic.phylofunk.treefunks;

import java.io.PrintStream;

import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.RootedTreeUtils;
import jebl.evolution.trees.Utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import network.artic.phylofunk.funks.FunkFactory;
import static network.artic.phylofunk.treefunks.TreeOptions.*;


/**
 *
 */
public class Statistics extends TreeFunk {

    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "statistics";
        }

        @Override
        public String getDescription() {
            return "Extract statistics and information from trees.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            options.addOption(OUTPUT_FILE);
            options.addOption(STATISTICS);
        }

        @Override
        public void create(CommandLine commandLine, boolean isVerbose) {
            new Statistics(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("output"),
                    commandLine.getOptionValues("stats"),
                    isVerbose);
        }
    };

    enum StatisticType {
        INTERNAL_BRANCH_LENGTHS
    }

    public Statistics(final String treeFileName,
                      final String outputFileName,
                      final String[] statistics,
                      final boolean isVerbose) {

        super(isVerbose);

        RootedTree tree = readTree(treeFileName);

        writeStatistics(tree, outStream);

//        try {
        //writer = new PrintWriter(Files.newBufferedWriter(Paths.get(outputFileName)));
//            writer.print("tip_count");
//            writer.print("\t");
//            writer.print("length");
//            writer.print("\t");
//            writer.print("changes");
//            writer.println();
//
//            for (Node node : tree.getInternalNodes()) {
//                if (!tree.isRoot(node)) {
//                    writer.print(tree.getExternalNodeCount(node));
//                    writer.print("\t");
//                    writer.print(tree.getLength(node));
//                    writer.print("\t");
//                    writer.print((int)(tree.getLength(node) * GENOME_LENGTH));
//                    writer.println();
//                }
//            }
//            writer.print("tree");
//            writer.print("\t");
//            writer.print("tip");
//            writer.print("\t");
//            writer.print("cluster");
//            writer.print("\t");
//            writer.print("tmrca");
//            writer.println();
//        } catch (IOException ioe) {
//            errorStream.println("Error opening output file: " + ioe.getMessage());
//            System.exit(1);
//        }

//        final PrintWriter finalWriter = writer;
//
//        processTrees(treeFileName, (tree) -> {
//            return tree;
//        });
//
//        finalWriter.close();
    }

    private void writeStatistics(RootedTree tree, PrintStream out) {
        out.println("tip count\t" + (RootedTreeUtils.getTipCount(tree, tree.getRootNode())));
        out.println("binary?\t" + (RootedTreeUtils.isBinary(tree)));
        out.println("ultrametric?\t" + (RootedTreeUtils.isUltrametric(tree, 0.0001)));
        out.println("root height\t" + (tree.getHeight(tree.getRootNode())));
        out.println("max tip height\t" + (RootedTreeUtils.getMaxTipHeight(tree, tree.getRootNode())));
        out.println("min tip height\t" + (RootedTreeUtils.getMinTipHeight(tree, tree.getRootNode())));
        out.println("root to tip distance\t" + (RootedTreeUtils.getAverageTipDistance(tree, tree.getRootNode())));
        out.println("max node count\t" + Utils.maxLevels(tree));
        out.println("total length\t" + Utils.getLength(tree));
    }


}

