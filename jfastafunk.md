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
                      
### file options

These options specify the input and output files and are used by most of the commands.

`-i` / `--input <filename>` Specify the input file.

`-m` / `--metadata <filename>` Specify a metadata table in CSV or TSV format where required.

`-t` / `--taxa <filename>` Specify a list of taxa in CSV format or as a tree where required.

`-o` / `--output <output_path>` Output filename or path to a directory if multiple output files will be produced.
\
`-d` / `--output-metadata` `<file>` Output a metadata file to match the output file.

`-p` / `--prefix <file_prefix>` Output file prefix when multiple output files are produced.

### label matching options

These options are used by functions where a metadata file (in CSV or TSV format) is provided and the rows in the table are matched to the sequences using unique IDs or strings. These link a column in the metadata table (using the `--id-column` option specifying the column name) to the label/header of the sequences in the FASTA file. Optionally this link can be made to a specific 'field' in the label using the `--id-field` option and the number of the field. Fields in a sequence label are strings (i.e., accessions, locations, dates) separated by a delimiter character (by default it uses the bar `|` character).

`-c` / `--id-column <column name>` Metadata column to use to match labels with the row in the metadata table (default first column)

`-n` / `--id-field <field number>` The label field to use to match metadata rows indexed from 1 (default = whole label)

`--field-delimiter <delimiter>` The delimiter used to specify fields in the labels (default `|`)

`--ignore-missing` Ignore any sequences that don't have a match in the annotations table (default: stop and report missing metadata).
 
### command specific options

#### annotate

This command adds information from the metadata file into the sequence labels as fields separated by a character (by default the bar character, `|` ). A match between the metadata row and the sequence label is made using an index column (by default the first column) and a field in the sequence label (by default the entire label). 

`--annotate-description` Put the annotations in the sequence description rather than appending to the name. A sequence description is after the sequence label separated by a space (default false).

`--label-fields <columns>` A list of metadata columns to add as label fields.

`--default-value <value>`  The default value to use for each field if the index is not found in the metadata.

`-r` / `--replace` Replace the entire existing label rather than appending (default: append).
             
_Example:_ 
`jfastafunk annotate -i sequences.fasta -m metadata.csv --index-column sample --index-field 1 --label-fields country date -o annotated.fasta`

This would match the value in the metadata column called `sample` with the first field in the FASTA sequence label and then append the values for `country` and `date` to the label. 

This would result in a label like this:
```
>seq1234|Scotland|1972-02-19
ATGATGCCGAGCGAGA...
```

#### dedupe
                   
This command de-duplicates sequences that have matching labels or that have the same value in one or more columns of the metadata table. 

#### extract
                      
This command extracts sequences where the label fields or metadata columns match specified values. Can also be used to extract all the sequences that match a set of taxa (either a list in a file or in a tree).

`--match` `<columns>` A list of metadata columns and regex values to match to. Example:
`country="Scotland|England"`

_Example:_
`jfastafunk extract -i sequences.fasta -m metadata.csv --match date="2024" --output-metadata extracted.csv -o extracted.fasta`

This would match the sequences with the metadata rows using the first columns and the complete FASTA sequence label and then write only those sequences that had `2024` in the `date` column. It would also create a new version of the metadata table that only had rows for the extracted sequences.

