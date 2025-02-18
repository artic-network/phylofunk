package network.artic.phylofunk.treefunks;

import network.artic.phylofunk.funks.FunkOptions;
import org.apache.commons.cli.Option;

/**
 * @author Andrew Rambaut
 * @version $
 */
class TreeOptions extends FunkOptions {

    final static Option TAXON_FILE = Option.builder()
            .longOpt("taxon-file")
            .argName("file")
            .hasArg()
            .required(false)
            .desc("file of taxa (in a CSV table or tree)")
            .type(String.class).build();

    final static Option TAXA = Option.builder("t")
            .longOpt("taxa")
            .argName("taxon-ids")
            .hasArgs()
            .required(false)
            .desc("a list of taxon ids")
            .type(String.class).build();

    final static Option OUTPUT_FORMAT = Option.builder("f")
            .longOpt("format")
            .argName("nexus|newick")
            .hasArg()
            .required(false)
            .desc("output file format (nexus or newick)")
            .type(String.class).build();

    final static Option OUTPUT_TAXA = Option.builder()
            .longOpt("output-taxa")
            .required(false)
            .desc("output a text file of taxon names to match each output tree")
            .type(String.class).build();

    final static Option ATTRIBUTE = Option.builder("a")
            .longOpt("attribute")
            .argName("attribute_name")
            .hasArg()
            .required(true)
            .desc("the attribute name")
            .type(String.class).build();

    final static Option VALUE = Option.builder()
            .longOpt("value")
            .argName("attribute_value")
            .hasArg()
            .required(true)
            .desc("the attribute value")
            .type(String.class).build();

    final static Option ROOT_VALUE = Option.builder()
            .longOpt("root-value")
            .argName("attribute_value")
            .hasArg()
            .required(true)
            .desc("the attribute value at the root")
            .type(String.class).build();

    final static Option ALGORITHM = Option.builder()
            .longOpt("algorithm")
            .argName("deltran/acctran")
            .required(false)
            .numberOfArgs(1)
            .desc("the algorithm for reconstruction of node values")
            .type(String.class).build();

    final static Option OUT_ATTRIBUTE = Option.builder()
            .longOpt("out-attribute")
            .argName("name")
            .hasArg()
            .required(false)
            .desc("the new attribute name in output")
            .type(String.class).build();

    final static Option LABEL_FIELDS = Option.builder("l")
            .longOpt("label-fields")
            .argName("columns")
            .hasArgs()
            .required(false)
            .desc("a list of metadata columns to add as tip label fields")
            .type(String.class).build();

    final static Option TIP_ATTRIBUTES = Option.builder()
            .longOpt("tip-attributes")
            .argName("columns")
            .hasArgs()
            .required(false)
            .desc("a list of metadata columns to add as tip attributes")
            .type(String.class).build();

    final static Option MRCA = Option.builder()
            .longOpt("mrca")
            .required(false)
            .desc("include the entire clade from the MRCA of the target taxa")
            .type(String.class).build();

    final static Option COLLAPSE_BY = Option.builder()
            .longOpt("collapse-by")
            .argName("attribute_name")
            .hasArg()
            .required(false)
            .desc("an attribute to collapse homogenous subtrees by")
            .type(String.class).build();

    final static Option CLUMP_BY = Option.builder()
            .longOpt("clump-by")
            .argName("attribute_name")
            .hasArg()
            .required(false)
            .desc("an attribute to clump homogenous children by")
            .type(String.class).build();

    final static Option MIN_COLLAPSED_SIZE = Option.builder()
            .longOpt("min-collapsed")
            .argName("size")
            .hasArg()
            .required(false)
            .desc("minimum number of tips in a collapsed subtree")
            .type(Integer.class).build();

    final static Option MIN_CLUMPED_SIZE = Option.builder()
            .longOpt("min-clumped")
            .argName("size")
            .hasArg()
            .required(false)
            .desc("minimum number of tips in a clump")
            .type(Integer.class).build();

    final static Option MAX_SOFT = Option.builder()
            .longOpt("max-soft")
            .argName("size")
            .hasArg()
            .required(false)
            .desc("maximum number of tips in a soft collapsed node")
            .type(Integer.class).build();

    final static Option MIDPOINT = Option.builder()
            .longOpt("midpoint")
            .required(false)
            .desc("midpoint root the tree")
            .type(String.class).build();

    final static Option OUTGROUPS = Option.builder()
            .longOpt("outgroups")
            .argName("tips")
            .hasArgs()
            .required(false)
            .desc("a list of tips to use as an outgroup for re-rooting")
            .type(String.class).build();

    final static Option ROOT_LOCATION = Option.builder()
            .longOpt("root-location")
            .argName("fraction")
            .required(false)
            .numberOfArgs(1)
            .desc("location on the root branch for the root as a fraction of the branch length from the ingroup")
            .type(Double.class).build();

    final static Option BRANCH_THRESHOLD = Option.builder("t")
            .longOpt("threshold")
            .argName("length")
            .required(true)
            .numberOfArgs(1)
            .desc("the threshold for branch lengths to be collapsed into polytomies")
            .type(Double.class).build();

    final static Option SCALE_FACTOR = Option.builder("s")
            .longOpt("factor")
            .argName("value")
            .required(true)
            .numberOfArgs(1)
            .desc("the factor to scale all branches by")
            .type(Double.class).build();

    final static Option ROOT_HEIGHT = Option.builder()
            .longOpt("height")
            .argName("value")
            .required(true)
            .numberOfArgs(1)
            .desc("the height of the root to scale to")
            .type(Double.class).build();

    final static Option INCREASING = Option.builder()
            .longOpt("increasing")
            .desc("order nodes by increasing clade size")
            .type(String.class).build();

    final static Option DECREASING = Option.builder()
            .longOpt("decreasing")
            .desc("order nodes by decreasing clade size")
            .type(String.class).build();

    final static Option SORT_COLUMNS = Option.builder()
            .longOpt("sort-by")
            .argName("columns")
            .hasArgs()
            .required(false)
            .desc("a list of metadata columns to sort by (prefix by ^ to reverse order)")
            .type(String.class).build();

    final static Option DESTINATION_COLUMN = Option.builder()
            .longOpt("destination-column")
            .argName("column name")
            .hasArg()
            .required(false)
            .desc("metadata column for destination to insert tips")
            .type(String.class).build();

    final static Option REPLACE = Option.builder("r")
            .longOpt("replace")
            .required(false)
            .desc("replace the annotations or tip label headers rather than appending (default false)")
            .type(String.class).build();

    final static Option OVERWRITE = Option.builder()
            .longOpt("overwrite")
            .required(false)
            .desc("overwrite existing values (default false)")
            .type(String.class).build();

    final static Option EXTRACT = Option.builder()
            .longOpt("extract")
            .required(false)
            .desc("extract only the matching rows (default false)")
            .type(String.class).build();

    final static Option STATISTICS = Option.builder()
            .longOpt("stats")
            .required(true)
            .desc("a list of statistics to include in the output (see docs for details)")
            .type(String.class).build();

    final static Option IGNORE_MISSING = Option.builder()
            .longOpt("ignore-missing")
            .required(false)
            .desc("ignore any missing matches in annotations table (default false)")
            .type(String.class).build();

    final static Option UNIQUE_ONLY = Option.builder()
            .longOpt("unique-only")
            .required(false)
            .desc("only place tips that have an unique position (default false)")
            .type(String.class).build();

    final static Option KEEP_TAXA = Option.builder("k")
            .longOpt("keep-taxa")
            .required(false)
            .desc("keep only the taxa specifed (default false)")
            .type(String.class).build();

    final static Option STEM = Option.builder()
            .longOpt("stem")
            .required(false)
            .desc("find the time of the stem above the MRCA (default false)")
            .type(String.class).build();

}

