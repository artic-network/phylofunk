# PhyloFunk (jclusterfunk & jfastafunk)

PhyloFunk is a package of functions for manipulating phylogenetic trees and FASTA format sequence files. 
Although it is a single package, it has two executables - `jclusterfunk` and `jfastafunk` - as shell scripts that access the
relevant main classes in the JAR file. Written in Java it generally efficient and will run on any machine that has Java 8 or 
later installed. 

### JClusterFunk

[`jclusterfunk`](https://artic-network.github.io/phylofunk/jclusterfunk.html) takes phylogenetic trees in Newick or NEXUS format and provides a range of functions
including annotating with metadata, rerooting, pruning, rescaling and much more.

[See here for detailed instructions for running `jclusterfunk`](https://artic-network.github.io/phylofunk/jclusterfunk.html)

### JFastaFunk

[`jfastafunk`](https://artic-network.github.io/phylofunk/jfastafunk.html) takes sequences in FASTA format and provides various functions to organise these. 
It focuses on the sequence labels (headers), matching these to metadata tables inorder to add annotations, extract
sequences into new files, de-duplicate and more. It doesn't change the actual sequences.

[See here for detailed instructions for running `jfastafunk`](https://artic-network.github.io/phylofunk/jfastafunk.html)

## Installation

The easiest way to install is using `conda`:

`conda install artic-network::phylofunk`

Alternatively, download the latest binary release:
https://github.com/artic-network/phylofunk/releases/latest

This contains three files:
`jclusterfunk` an executable shell file to run `jclusterfunk`
`jfastafunk` an executable shell file to run `jfastafunk`
`phylofunk.jar` the Java JAR file used by both these. This should be in the same directory as the shell scripts.

These files can be copied to a `bin` directory on the path such as `/usr/local/bin` or `~/bin`

Type:

```jclusterfunk --version```

to check it is running.

