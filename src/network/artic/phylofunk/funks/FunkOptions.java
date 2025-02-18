package network.artic.phylofunk.funks;

import org.apache.commons.cli.Option;

/**
 * @author Andrew Rambaut
 * @version $
 */
public class FunkOptions {

    public static final String DEFAULT_DELIMITER = "|";

    public final static Option INPUT = Option.builder("i")
            .longOpt("input")
            .argName("file")
            .hasArg()
            .required(true)
            .desc("input file")
            .type(String.class).build();

    public final static Option INPUT_PATH = Option.builder("i")
            .longOpt("input")
            .argName("path")
            .hasArg()
            .required(true)
            .desc("input path")
            .type(String.class).build();

    public final static Option METADATA = Option.builder("m")
            .longOpt("metadata")
            .argName("file")
            .hasArg()
            .required(true)
            .desc("input metadata file")
            .type(String.class).build();

    public final static Option INDEX_COLUMN = Option.builder("c")
            .longOpt("id-column")
            .argName("column name")
            .hasArg()
            .required(false)
            .desc("metadata column to use to match labels (default first column)")
            .type(String.class).build();

    public final static Option INDEX_FIELD = Option.builder("n")
            .longOpt("id-field")
            .argName("field number")
            .hasArg()
            .required(false)
            .desc("label field to use to match metadata (default = whole label)")
            .type(Integer.class).build();

    public final static Option FIELD_DELIMITER = Option.builder()
            .longOpt("field-delimiter")
            .argName("delimiter")
            .hasArg()
            .required(false)
            .desc("the delimiter used to specify fields in the labels (default = '" + DEFAULT_DELIMITER + "')")
            .type(String.class).build();

    public final static Option OUTPUT_FILE = Option.builder("o")
            .longOpt("output")
            .argName("file")
            .hasArg()
            .required(true)
            .desc("output file")
            .type(String.class).build();

    public final static Option OUTPUT_PATH = Option.builder("o")
            .longOpt("output")
            .argName("path")
            .hasArg()
            .required(false)
            .desc("output path")
            .type(String.class).build();

    public final static Option OUTPUT_PREFIX = Option.builder("p")
            .longOpt("prefix")
            .argName("file_prefix")
            .hasArg()
            .required(true)
            .desc("output file prefix")
            .type(String.class).build();

    public final static Option OUTPUT_METADATA = Option.builder("d")
            .longOpt("output-metadata")
            .argName("file")
            .hasArg()
            .required(false)
            .desc("output a metadata file to match the output file")
            .type(String.class).build();

    public final static Option LABEL_FIELDS = Option.builder("l")
            .longOpt("label-fields")
            .argName("columns")
            .hasArgs()
            .required(false)
            .desc("a list of metadata columns to add as label fields")
            .type(String.class).build();

    public final static Option ADD_COLUMNS = Option.builder("a")
            .longOpt("add-columns")
            .argName("columns")
            .hasArgs()
            .required(false)
            .desc("a list of metadata columns to add")
            .type(String.class).build();


    public final static Option REPLACE = Option.builder("r")
            .longOpt("replace")
            .required(false)
            .desc("replace the label headers rather than appending (default false)")
            .type(String.class).build();

    public final static Option OVERWRITE = Option.builder()
            .longOpt("overwrite")
            .required(false)
            .desc("overwrite existing values (default false)")
            .type(String.class).build();

    public final static Option EXTRACT = Option.builder()
            .longOpt("extract")
            .required(false)
            .desc("extract only the matching rows (default false)")
            .type(String.class).build();

    public final static Option IGNORE_MISSING = Option.builder()
            .longOpt("ignore-missing")
            .required(false)
            .desc("ignore any missing matches in annotations table (default false)")
            .type(String.class).build();

    public final static Option UNIQUE_ONLY = Option.builder()
            .longOpt("unique-only")
            .required(false)
            .desc("only place tips that have an unique position (default false)")
            .type(String.class).build();


    public final static Option TAXA = Option.builder( "t" )
            .longOpt("taxa")
            .argName("file")
            .hasArg()
            .required(true)
            .desc( "file of taxa (table or tree)" )
            .type(String.class).build();

    public final static Option DEFAULT_VALUE = Option.builder(  )
            .longOpt("default-value")
            .argName("value")
            .hasArg()
            .required(false)
            .desc( "the default value to use if the index is not found in the metadata" )
            .type(String.class).build();

    public final static Option HEADER_FIELDS =  Option.builder(  )
            .longOpt("header-fields")
            .argName("columns")
            .hasArgs()
            .required(false)
            .desc( "a list of metadata columns to add as sequence header fields" )
            .type(String.class).build();

    public final static Option MATCH_VALUES =  Option.builder(  )
            .longOpt("match")
            .argName("columns")
            .hasArgs()
            .required(false)
            .desc( "a list of metadata columns and regex values to match to. Example: country=\"Scotland|England\"" )
            .type(String.class).build();

    public final static Option ANNOTATE_DESCRIPTION =  Option.builder( )
            .longOpt("annotate-description")
            .required(false)
            .desc( "put the annotations in the sequence description rather than appending to the name (default false)" )
            .type(String.class).build();

}

