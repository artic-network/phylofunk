package network.artic.phylofunk.treefunks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import network.artic.phylofunk.funks.FunkFactory;
import static network.artic.phylofunk.treefunks.TreeOptions.*;


/**
 * Split the tree into subtrees defined by annotations of the tips or the nodes.
 */
public class Split extends TreeFunk {

    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "split";
        }

        @Override
        public String getDescription() {
            return "Split out subtrees based on tip annotations.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            METADATA.setRequired(false);
            options.addOption(METADATA);
            options.addOption(OUTPUT_PATH);
            options.addOption(OUTPUT_PREFIX);
            options.addOption(OUTPUT_FORMAT);
            options.addOption(OUTPUT_METADATA);
            options.addOption(ATTRIBUTE);
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

            new Split(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("metadata"),
                    commandLine.getOptionValue("output"),
                    commandLine.getOptionValue("prefix"),
                    format,
                    commandLine.getOptionValue("output-metadata"),
                    commandLine.getOptionValue("id-column", ""),
                    Integer.parseInt(commandLine.getOptionValue("id-field", "0")),
                    commandLine.getOptionValue("field-delimeter", DEFAULT_DELIMITER),
                    commandLine.getOptionValue("attribute"),
                    isVerbose);
        }
    };

    public Split(String treeFileName,
                 String metadataFileName,
                 String outputPath,
                 String outputFileStem,
                 FormatType outputFormat,
                 String outputMetadataFileName,
                 String indexColumn,
                 int indexHeader,
                 String headerDelimiter,
                 String attributeName,
                 boolean isVerbose) {

        super(metadataFileName, null, indexColumn, indexHeader, headerDelimiter, isVerbose);

        String path = checkOutputPath(outputPath);

        RootedTree tree = readTree(treeFileName);

        Map<Taxon, String> taxonMap = getTaxonMap(tree);

        Map<Object, Set<Node>> attributeValues = collectTipAttributeValues(tree, attributeName);

        List<Object> keys = new ArrayList<>(attributeValues.keySet());
        keys.sort((o1, o2) -> {
            return (o1.toString().length() == o2.toString().length() ?
                    o1.toString().compareTo(o2.toString()) :
                    o1.toString().length() - o2.toString().length());
        });

        if (isVerbose) {
            outStream.println("Attribute: " + attributeName);
            outStream.println("Values: " + String.join(", ", toString(keys)));
            outStream.println();
        }

        clearInternalAttributes(tree);

        for (Object value: keys) {
            annotateMonophyleticNodes(tree, attributeName, value, true, attributeName);
        }

//        for (Object value: keys) {
//            collapseSubtrees(tree, attributeName, value);
//        }

        for (Object value: keys) {
            splitSubtrees(tree, attributeName, value, true, path, outputFileStem, true, outputFormat);
        }

    }
    
    /**
     * Finds the MRCA for a set of tip nodes and then recursively annotates the subtree
     * @param tree
     * @param attributeName
     * @param attributeValue
     */
    private void annotateMonophyleticNodes(RootedTree tree, String attributeName, Object attributeValue, boolean isHierarchical, String newAttributeName) {
        annotateMonophyleticNodes(tree, tree.getRootNode(), attributeName, attributeValue, isHierarchical, newAttributeName);
    }

    /**
     * recursive version
     * @param tree
     * @param node
     * @param attributeName
     * @param attributeValue
     */
    private boolean annotateMonophyleticNodes(RootedTree tree, Node node, String attributeName, Object attributeValue,
                                              boolean isHierarchical, String newAttributeName) {
        boolean isMonophyletic = true;

        if (tree.isExternal(node)) {
            Object value = node.getAttribute(attributeName);
            if (value == null || !(attributeValue.equals(value) || (isHierarchical && isSublineage(attributeValue.toString(), value.toString())))) {
                return false;
            }
        } else {

            for (Node child : tree.getChildren(node)) {
                isMonophyletic = annotateMonophyleticNodes(tree, child, attributeName, attributeValue, isHierarchical, newAttributeName) && isMonophyletic;
            }
        }

        if (isMonophyletic) {
            node.setAttribute(newAttributeName, attributeValue);
        }

        return isMonophyletic;
    }

    /**
     * Is lineage 1 a sub-lineage of lineage 2 in the dot notation
     * @param lineage1
     * @param lineage2
     * @return
     */
    private boolean isSublineage(String lineage1, String lineage2) {
        String[] split1 = lineage1.split("\\.");
        String[] split2 = lineage2.split("\\.");

        if (split1.length < split2.length) {
            // lineage1 is actually shorter than lineage2
            return false;
        }
        ;
        for (int i = 0; i < split2.length; i++) {
            if (!split1[i].equals(split2[i])) {
                return false;
            }
        }
        return true;
    }

    private void collapseSubtrees(RootedTree tree, String attributeName, Object attributeValue) {
        collapseSubtrees(tree, tree.getRootNode(), attributeName, attributeValue, null);
    }

    /**
     * recursive version
     * @param tree
     * @param node
     * @param attributeName
     * @param parentValue
     */
    private void collapseSubtrees(RootedTree tree, Node node, String attributeName, Object attributeValue, Object parentValue) {
        if (!tree.isExternal(node)) {
            Object value = node.getAttribute(attributeName);
            if (attributeValue.equals(value) && !value.equals(parentValue)) {
                node.setAttribute("!collapse", "{\"collapsed\",1.7E-4}");
            }

            for (Node child : tree.getChildren(node)) {
                collapseSubtrees(tree, child, attributeName, attributeValue, value);
            }

        }
    }



}

