package network.artic.phylofunk.treefunks;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.ReRootedTree;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.RootedTreeUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import network.artic.phylofunk.funks.FunkFactory;
import static network.artic.phylofunk.treefunks.TreeOptions.*;


/**
 * Reroot the tree with an outgroup or a midpoint root
 */
public class Reroot extends TreeFunk {
    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "reroot";
        }

        @Override
        public String getDescription() {
            return "Re-root the tree using an outgroup.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            options.addOption(OUTPUT_FILE);
            options.addOption(OUTPUT_FORMAT);
            options.addOption(INDEX_FIELD);
            options.addOption(FIELD_DELIMITER);
            OptionGroup orderGroup2= new OptionGroup();
            orderGroup2.addOption(OUTGROUPS);
            orderGroup2.addOption(MIDPOINT);
            options.addOption(ROOT_LOCATION);
            options.addOptionGroup(orderGroup2);
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

            RootType rootType = commandLine.hasOption("midpoint") ? RootType.MIDPOINT : RootType.OUTGROUP;
            double rootLocation = Double.parseDouble(commandLine.getOptionValue("root-location", "0.5"));
            new Reroot(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("output"),
                    format,
                    Integer.parseInt(commandLine.getOptionValue("index-field", "0")),
                    commandLine.getOptionValue("field-delimeter", "\\|"),
                    rootType,
                    rootLocation,
                    commandLine.getOptionValues("outgroups"),
                    isVerbose);
        }
    };


    public Reroot(String treeFileName,
                  String outputPath,
                  FormatType outputFormat,
                  int indexHeader,
                  String headerDelimiter,
                  RootType rootType,
                  double rootLocation,
                  String[] outgroups,
                  boolean isVerbose) {

        super(null, null, null, indexHeader, headerDelimiter, isVerbose);

        if (rootLocation < 0.0 || rootLocation > 1.0) {
            errorStream.println("root-location option should be between 0.0 and 1.0 ");
            System.exit(1);
        }

        RootedTree tree = readTree(treeFileName);

        Map<Taxon, String> taxonMap = getTaxonMap(tree);

        RootedTree outTree;

        switch (rootType) {
            case OUTGROUP:
                if (isVerbose) {
                    outStream.println("Outgroup rooting");
                    outStream.println("Outgroup: " + String.join(", ", outgroups));
                    outStream.println();
                }
//                throw new UnsupportedOperationException("Outgroup rooting not implemented yet");

                Set<Node> outgroupTips = new HashSet<>();
                for (String outgroup : outgroups) {
                    Node tip = tree.getNode(Taxon.getTaxon(outgroup));
                    if (tip == null) {
                        errorStream.println("Outgroup, " + outgroup + ", not found in the tree");
                        System.exit(1);
                    }
                    outgroupTips.add(tip);
                }

                Node outgroupNode = RootedTreeUtils.getCommonAncestorNode(tree, outgroupTips);
                Node ingroupNode = tree.getParent(outgroupNode);
                double ingroupBranchLength = tree.getLength(outgroupNode) * rootLocation;
                try {
                    outTree = new ReRootedTree(tree, ingroupNode, outgroupNode, ingroupBranchLength);
                } catch (Graph.NoEdgeException nee) {
                    throw new RuntimeException(nee);
                }
                break;
            case MIDPOINT:
                if (isVerbose) {
                    outStream.println("Midpoint rooting");
                    outStream.println();
                }
//                throw new UnsupportedOperationException("Midpoint rooting not implemented yet");
                outTree =  ReRootedTree.rootTreeAtCenter(tree);
                break;
            default:
                throw new IllegalArgumentException("Unknown reroot type");
        }

        writeTreeFile(outTree, outputPath, outputFormat);
    }

}

