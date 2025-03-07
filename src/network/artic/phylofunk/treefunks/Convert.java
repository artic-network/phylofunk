package network.artic.phylofunk.treefunks;

import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SortedRootedTree;
import network.artic.phylofunk.funks.FunkFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import static network.artic.phylofunk.treefunks.TreeOptions.*;


/**
 * Reorder the branches at each node to increasing or decreasing tip counts
 */
public class Convert extends TreeFunk {
    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "convert";
        }

        @Override
        public String getDescription() {
            return "Converts the tree to a different file format.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            options.addOption(OUTPUT_FILE);
            options.addOption(OUTPUT_FORMAT);
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

            new Convert(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("output"),
                    format,
                    isVerbose);
        }
    };


    public Convert(String treeFileName,
                   String outputPath,
                   FormatType outputFormat,
                   boolean isVerbose) {

        super(isVerbose);

        RootedTree tree = readTree(treeFileName);

        RootedTree outTree = tree;

        if (isVerbose) {
            outStream.println("Writing tree file, " + outputPath + ", in " + outputFormat.name().toLowerCase() + " format");
            outStream.println();
        }

        writeTreeFile(outTree, outputPath, outputFormat);
    }

}

