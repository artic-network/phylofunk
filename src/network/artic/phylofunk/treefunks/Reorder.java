package network.artic.phylofunk.treefunks;

import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SortedRootedTree;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import network.artic.phylofunk.funks.FunkFactory;
import static network.artic.phylofunk.treefunks.TreeOptions.*;


/**
 * Reorder the branches at each node to increasing or decreasing tip counts
 */
public class Reorder extends TreeFunk {
    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "reorder";
        }

        @Override
        public String getDescription() {
            return "Re-order nodes in ascending or descending clade size.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            METADATA.setRequired(false);
            options.addOption(METADATA);
            options.addOption(INDEX_COLUMN);
            options.addOption(INDEX_FIELD);
            options.addOption(FIELD_DELIMITER);
            options.addOption(OUTPUT_FILE);
            options.addOption(OUTPUT_FORMAT);
            OptionGroup orderGroup = new OptionGroup();
            orderGroup.addOption(INCREASING);
            orderGroup.addOption(DECREASING);
            orderGroup.addOption(SORT_COLUMNS);
            options.addOptionGroup(orderGroup);
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

            OrderType orderType = (commandLine.hasOption("increasing") ?
                    OrderType.INCREASING :
                    (commandLine.hasOption("decreasing") ? OrderType.DECREASING : OrderType.UNCHANGED));

            new Reorder(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("metadata"),
                    commandLine.getOptionValue("output"),
                    format,
                    commandLine.getOptionValue("id-column", ""),
                    Integer.parseInt(commandLine.getOptionValue("id-field", "0")),
                    commandLine.getOptionValue("field-delimeter", DEFAULT_DELIMITER),
                    orderType,
                    commandLine.getOptionValues("sort-by"),
                    isVerbose);
        }
    };


    public Reorder(String treeFileName,
                   String metadataFileName,
                   String outputPath,
                   FormatType outputFormat,
                   String indexColumn,
                   int indexHeader,
                   String headerDelimiter,
                   OrderType orderType,
                   String[] sortColumns,
                   boolean isVerbose) {

        super(metadataFileName, null, indexColumn, indexHeader, headerDelimiter, isVerbose);

        RootedTree tree = readTree(treeFileName);

        RootedTree outTree = tree;

        if (sortColumns != null) {
            throw new UnsupportedOperationException("sort by metadata columns not impemented yet");
        }
        
        if (orderType != OrderType.UNCHANGED) {
            if (isVerbose) {
                outStream.println("Reordering branches by " + orderType.name().toLowerCase() + " node density");
                outStream.println();
            }
            outTree = new SortedRootedTree(tree,
                    orderType.equals(OrderType.DECREASING) ?
                            SortedRootedTree.BranchOrdering.DECREASING_NODE_DENSITY :
                            SortedRootedTree.BranchOrdering.INCREASING_NODE_DENSITY);
        }

        if (isVerbose) {
            outStream.println("Writing tree file, " + outputPath + ", in " + outputFormat.name().toLowerCase() + " format");
            outStream.println();
        }

        writeTreeFile(outTree, outputPath, outputFormat);
    }

}

