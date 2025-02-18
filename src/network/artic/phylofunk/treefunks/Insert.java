package network.artic.phylofunk.treefunks;

import java.util.*;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.MutableRootedTree;
import jebl.evolution.trees.RootedTree;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import network.artic.phylofunk.funks.FunkFactory;
import static network.artic.phylofunk.treefunks.TreeOptions.*;

/**
 * Replaces a tip in a tree with a polytomy of specified taxa
 */
public class Insert extends TreeFunk {
    enum InsertMode {
        UNIQUE_ONLY,
        DUPLICATES
    }

    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "insert";
        }

        @Override
        public String getDescription() {
            return "Insert tips into the tree.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            options.addOption(METADATA);
            options.addOption(OUTPUT_FILE);
            options.addOption(OUTPUT_FORMAT);
            options.addOption(INDEX_COLUMN);
            options.addOption(INDEX_FIELD);
            options.addOption(FIELD_DELIMITER);
            options.addOption(DESTINATION_COLUMN);
            options.addOption(UNIQUE_ONLY);
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

            new Insert(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("metadata"),
                    commandLine.getOptionValue("output"),
                    format,
                    commandLine.getOptionValue("destination-column", ""),
                    commandLine.getOptionValue("id-column", ""),
                    Integer.parseInt(commandLine.getOptionValue("id-field", "0")),
                    commandLine.getOptionValue("field-delimeter", DEFAULT_DELIMITER),
                    commandLine.hasOption("unique-only"),
                    commandLine.hasOption("ignore-missing"),
                    isVerbose);
        }
    };

    public Insert(String treeFileName,
                  String metadataFileName,
                  String outputFileName,
                  FormatType outputFormat,
                  String destinationColumn,
                  String indexColumn,
                  int indexHeader,
                  String headerDelimiter,
                  boolean uniqueOnly,
                  boolean ignoreMissing,
                  boolean isVerbose) {

        super(metadataFileName, null, indexColumn, indexHeader, headerDelimiter, isVerbose);

        RootedTree tree = readTree(treeFileName);

        Map<String, Node> tipMap = getTipMap(tree);


        // map with tip id and list of taxa to insert
        Map<Taxon, List<String>> insertionMap = new HashMap<>();

        int insertionCount = 0;

        for (String key : metadata.keySet()) {

            String destination = metadata.get(key).get(destinationColumn);
            if (!destination.isEmpty()) {
                Node tip = tipMap.get(destination);

                if (tip != null) {
                    Taxon locationTaxon = tree.getTaxon(tip);

                    // add the list to a map keyed by taxon of insertion location
                    List<String> insertionList = insertionMap.getOrDefault(locationTaxon, new ArrayList<>());
                    insertionList.add(key);
                    insertionMap.put(locationTaxon, insertionList);

                    insertionCount += 1;
                } else {
                    if (!ignoreMissing) {
                        errorStream.println("Destination taxon, " + destination + ", not found in tree");
                        System.exit(1);
                    } else if (isVerbose) {
                        outStream.println("Destination taxon, " + destination + ", not found in tree");
                    }
                }
            }
        }

        MutableRootedTree outTree = new MutableRootedTree(tree);

        insertTips(outTree, outTree.getRootNode(), 0, insertionMap);

        if (isVerbose) {

                outStream.println("   Number of taxa added: " + insertionCount);
                outStream.println(" Number of destinations: " + insertionMap.keySet().size());
                outStream.println("   Final number of tips: " + outTree.getExternalNodes().size());
                outStream.println();
        }


        if (isVerbose) {
            outStream.println("Writing tree file, " + outputFileName + ", in " + outputFormat.name().toLowerCase() + " format");
            outStream.println();
        }

        writeTreeFile(outTree, outputFileName, outputFormat);
    }

    private void insertTips(MutableRootedTree tree, Node node, int depth,
                            Map<Taxon, List<String>> insertionMap) {
        if (!tree.isExternal(node)) {
            for (Node child: tree.getChildren(node)) {
                insertTips(tree, child, depth + 1, insertionMap);
            }
        } else {
            Taxon locationTaxon = tree.getTaxon(node);
            List<String> insertions = insertionMap.get(locationTaxon);

            if (insertions != null) {
                List<Taxon> taxaToInsert = new ArrayList<>();

                for (String insertion : insertions) {
                    taxaToInsert.add(Taxon.getTaxon(insertion));
                }

                if (!taxaToInsert.isEmpty()) {
                    Node parent = tree.getParent(node);

                    if (tree.getLength(node) > 0.0) {
                        Node root = tree.getRootNode();

                        tree.removeChild(node, parent);

                        // create a new internal node and add the tip as a child
                        Node newParent = tree.createInternalNode(Collections.singletonList(node));
                        tree.setLength(newParent, tree.getLength(node));
                        tree.setLength(node, 0.0);

                        tree.addChild(newParent, parent);

                        parent = newParent;

                        // createInternalNode() will have changed the root so set it back
                        tree.setRoot(root);
                    }

                    for (Taxon insertion : taxaToInsert) {
                        Node child = tree.createExternalNode(insertion);
                        tree.addChild(child, parent);
                        tree.setLength(child, 0.0);
                    }
                }
            }
        }
    }
}


