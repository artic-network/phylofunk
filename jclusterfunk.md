# jclusterfunk

The general command line for running jclusterfunk is:

`jclusterfunk <command> [options]`

To get the list of available options for a given command use:

`jclusterfunk annotate --help`


### commands

| command       | description                                                                                                                                                                                                                          |
|:--------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `annotate`    | Take data fields from a metadata file and apply either to the tip labels of the tree or as annotations as used by [FigTree](http://tree.bio.ed.ac.uk/software/figtree).                                                              |
| `collapse`    | Collapses branches less than a threshold into a polytomy.                                                                                                                                                                            |
| `convert`     | Convert the tree from one format to another without changing it.                                                                                                                                                                     |
| `extract`     | Extracts metadata fields from the tips of a tree.                                                                                                                                                                                    |
| `merge`       | Merges two metadata tables based on an index column (usually taxon names).                                                                                                                                                           |
| `prune`       | Prune out sets of tips from a tree.                                                                                                                                                                                                  |
| `reconstruct` | Reconstructs annotation values at internal nodes using parsimony.                                                                                                                                                                    |
| `reorder`     | Reorder branches at each node to have increasing or decreasing numbers of child nodes.                                                                                                                                               |
| `reroot`      | Reroot the tree using an outgroup or at the midpoint.                                                                                                                                                                                |
| `sample`      | Sample taxa down using metadata attributes.                                                                                                                                                                                          |
| `scale`       | Scales branch lengths of a tree by a factor or to a specified root height.                                                                                                                                                           |
| `statistics`  | Writes out a list of statistics and information about a tree.                                                                                                                                                                        |
| `tmrca`       | Finds the time of most recent common ancestor of a set of taxa.                                                                                                                                                                      |
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

`-c` / `--id-column <column name>` Metadata column to use to match tip labels (default first column)

`--id-field <field number>` The tip label field to use to match metadata rows indexed from 1 (default = whole label)

`--field-delimiter <delimiter>` The delimiter used to specify fields in the tip labels (default `|`)

These options are used by functions where a metadata file (in CSV or TSV format) is provided and the rows in the table are matched to the sequences using unique IDs or strings. These link a column in the metadata table (using the `--id-column` option specifying the column name) to the label of the tips (taxa) in the tree. Optionally this link can be made to a specific 'field' in the label using the `--id-field` option and the number of the field. Fields in a taxon label are strings (i.e., accessions, locations, dates) separated by a delimiter character (by default it uses the bar `|` character). They are numbered from 1.

### command specific options

#### `annotate`

This function links the metadata file to tree taxa and then 'annotates' the taxa labels with additional pieces of data from the metadata table. The data can be added directly as 'fields' into the taxon/tip label or can be added as an 'attribute' using the [extended NEXUS metacomment format](http://beast.community/) using `<key> = <value>` pairs. This can be used by software that supports this format (including [FigTree](http://github.com/rambaut/figtree) and [BEAST](http://beast.community/).

`--label-fields <columns>` A list of metadata columns to add as tip label fields.

`--tip-attributes <columns>` A list of metadata columns to add as tip attributes.

`--ignore-missing` Ignore any tips that don't have a match in the annotations table (default: stop and report missing metadata).

`--replace` Replace the existing annotations or tip labels rather than appending (default: append).

_Example:_
`jclusterfunk annotate -i phylip.tree -m metadata.csv --index-column sample --index-field 1 --label-fields country date -o annotated.tree`

This would match the value in the metadata column called `sample` with the first field in the tree tip/taxa labels and then append the values for `country` and `date` to the label.

This would result in a label like this:
```
taxon1234|Scotland|1972-02-19
```

#### `prune`

Specify a set of taxa using the `-t` option - these can be in the first column of a CSV or TSV file or in a tree in Newick or NEXUS format. The tips are pruned out along with any internal nodes that have fewer than 2 descendants as a result.

`-k` `--keep-taxa` Keep the taxa specifed and prune the rest (default: prune specified taxa)

#### `reorder`

Reorder the branches at each internal node so that they are in either increasing or decreasing order of the number of tips below them.

`--decreasing` Order nodes by decreasing clade size.
`--increasing` Order nodes by increasing clade size.

#### `reroot`

Change the root position of the tree. If an outgroup is specified then the most recent common ancestor (MRCA) of the specified taxa will be found (this is based on the current rooting of the tree and the MRCA of the outgroup can't be the root itself).

`--outgroup <outgroup taxa>` Root tree using specified outgroup
`--root-location` The position of the root on the branch leading to the outgroup - a number between 0 and 1 (default: 0.5).
`--midpoint` Root tree at the branch-length midpoint.

