package network.artic.phylofunk.treefunks;

import java.util.Map;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVRecord;

import network.artic.phylofunk.funks.FunkFactory;
import static network.artic.phylofunk.treefunks.TreeOptions.*;

/**
 * Adds tip annotations from a metadata file. These can either be added to the tip labels or as NEXUS
 * metadata comments for the tips.
 */
public class Annotate extends TreeFunk {

    public static final FunkFactory FACTORY = new FunkFactory() {
        @Override
        public String getName() {
            return "annotate";
        }

        @Override
        public String getDescription() {
            return "Annotate tips and nodes from a metadata table.";
        }

        @Override
        public void setOptions(Options options) {
            options.addOption(INPUT);
            options.addOption(OUTPUT_FILE);
            options.addOption(OUTPUT_FORMAT);
            options.addOption(METADATA);
            options.addOption(INDEX_COLUMN);
            options.addOption(INDEX_FIELD);
            options.addOption(FIELD_DELIMITER);
            options.addOption(LABEL_FIELDS);
            options.addOption(TIP_ATTRIBUTES);
            options.addOption(REPLACE);
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

            new Annotate(
                    commandLine.getOptionValue("input"),
                    commandLine.getOptionValue("metadata"),
                    commandLine.getOptionValue("output"),
                    format,
                    commandLine.getOptionValue("id-column", ""),
                    Integer.parseInt(commandLine.getOptionValue("id-field", "0")),
                    commandLine.getOptionValue("field-delimeter", DEFAULT_DELIMITER),
                    commandLine.getOptionValues("label-fields"),
                    commandLine.getOptionValues("tip-attributes"),
                    commandLine.hasOption("replace"),
                    commandLine.hasOption("ignore-missing"),
                    isVerbose);
        }

    };

    public Annotate(String treeFileName,
                    String metadataFileName,
                    String outputFileName,
                    FormatType outputFormat,
                    String indexColumn,
                    int indexHeader,
                    String headerDelimiter,
                    String[] labelColumns,
                    String[] annotationColumns,
                    boolean replace,
                    boolean ignoreMissing,
                    boolean isVerbose) {

        super(metadataFileName, null, indexColumn, indexHeader, headerDelimiter, isVerbose);

        RootedTree tree = readTree(treeFileName);

        Map<Taxon, String> taxonMap = getTaxonMap(tree);

        if (annotationColumns != null && annotationColumns.length > 0) {
            if (outputFormat != FormatType.NEXUS) {
                errorStream.println("Tip annotations are only compatible with NEXUS output format");
                System.exit(1);
            }

            if (isVerbose) {
                outStream.println((replace ? "Replacing" : "Appending") + " tip annotations with columns: " + String.join(", ", annotationColumns));
                outStream.println();
            }
            annotateTips(tree, taxonMap, metadata, annotationColumns, replace, ignoreMissing);
        }

        if (labelColumns != null && labelColumns.length > 0) {
            if (isVerbose) {
                outStream.println((replace ? "Replacing" : "Appending") + " tip labels with columns: " + String.join(", ", labelColumns));
                outStream.println();
            }
            relabelTips(tree, taxonMap, metadata, labelColumns, headerDelimiter, replace, ignoreMissing);
        }

        if (isVerbose) {
            outStream.println("Writing tree file, " + outputFileName + ", in " + outputFormat.name().toLowerCase() + " format");
            outStream.println();
        }

        writeTreeFile(tree, outputFileName, outputFormat);

    }

    /**
     * Annotates the tips of a tree with a set of columns from the metadata table
     * @param tree
     * @param taxonMap
     * @param metadata
     * @param columnNames
     * @param replace
     */
    private void annotateTips(RootedTree tree,
                              Map<Taxon, String> taxonMap,
                              Map<String, CSVRecord> metadata,
                              String[] columnNames,
                              boolean replace,
                              boolean ignoreMissing) {
        if (replace) {
            clearExternalAttributes(tree);
        }

        for (Node tip : tree.getExternalNodes()) {
            String key = taxonMap.get(tree.getTaxon(tip));
            CSVRecord record = metadata.get(key);
            if (record == null) {
                if (!ignoreMissing) {
                    errorStream.println("Tip index, " + key + ", not found in metadata table");
                    System.exit(1);
                }
            } else {
                for (String name : columnNames) {
                    if (!record.get(name).isEmpty()) {
                        tip.setAttribute(name, record.get(name));
                    }
                }
            }
        }
    }

    /**
     * Annotates the tips of a tree with a set of columns from the metadata table
     * @param tree
     * @param taxonMap
     * @param metadata
     * @param columnNames
     * @param replace
     */
    private void relabelTips(RootedTree tree,
                             Map<Taxon, String> taxonMap,
                             Map<String, CSVRecord> metadata,
                             String[] columnNames,
                             String headerDelimiter,
                             boolean replace,
                             boolean ignoreMissing) {
        for (Node tip : tree.getExternalNodes()) {
            String key = taxonMap.get(tree.getTaxon(tip));
            CSVRecord record = metadata.get(key);
            if (record == null) {
                if (!ignoreMissing) {
                    errorStream.println("Tip index, " + key + ", not found in metadata table");
                    System.exit(1);
                }
            } else {
                StringBuilder tipLabel = new StringBuilder();
                boolean first = true;
                if (!replace) {
                    tipLabel.append(tree.getTaxon(tip).getName());
                    first = false;
                }

                for (String name : columnNames) {
                    if (!first) {
                        tipLabel.append(headerDelimiter);
                        first = false;
                    }
                    tipLabel.append(record.get(name));
                }
                tree.renameTaxa(tree.getTaxon(tip), Taxon.getTaxon(tipLabel.toString()));
            }
        }
    }

}

