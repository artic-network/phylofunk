# jfastafunk

###

The general command line for running jfastafunk is:

`jfastafunk <command> [options]`

### commands

|command|description|
|:---|:---|
| `annotate` | Take data fields from a metadata file and apply to the sequence headers. |
| `compress` | Compress to functionally identical sequences. |
| `extract` | Extract sequences from a list or based on metadata. |
| `remove` | Remove sequences from a list or based on metadata. |

### general options

`-h` / `--help` List the available options and stop. Combine with a command to get help for that command.

`--version` Print the version number and stop.

`-v` / `--verbose` Print extended information about analysis performed.

`-i` / `--input <filename>` Specify the input tree file.

`-m` / `--metadata <filename>` Specify a metadata table in CSV format where required.

`-t` / `--taxa <filename>` Specify a list of taxa in CSV format or as a tree where required.

`-o` / `--output <output_path>` Output filename or path to a directory if multiple output files will be produced.

`-f` / `--format <nexus|newick>` Output tree file format (nexus or newick)

`-p` / `--prefix <file_prefix>` Output file prefix when multiple output files are produced.

### taxa matching options

`-c` / `--index-column <column name>` Metadata column to use to match tip labels (default first column)

`--index-field <field number>` The tip label field to use to match metadata rows indexed from 1 (default = whole label)

`--field-delimiter <delimiter>` The delimiter used to specify fields in the tip labels (default `|`)
                                     
### command specific options

#### `annotate`                                

`--label-fields <columns>` A list of metadata columns to add as tip label header fields.

`--tip-attributes <columns>` A list of metadata columns to add as tip attributes.

`--ignore-missing` Ignore any tips that don't have a match in the annotations table (default: stop and report missing metadata).

`--replace` Replace the existing annotations or tip labels rather than appending (default: append).

#### `compress`

#### `extract`

`--ignore-missing` Ignore any tips that don't have a match in the annotations table (default: stop and report missing metadata).

#### `remove`

`--ignore-missing` Ignore any tips that don't have a match in the annotations table (default: stop and report missing metadata).
