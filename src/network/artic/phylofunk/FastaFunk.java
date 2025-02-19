package network.artic.phylofunk;

import network.artic.phylofunk.funks.FunkFactory;
import network.artic.phylofunk.seqfunks.Annotate;
import network.artic.phylofunk.seqfunks.Dedupe;
import network.artic.phylofunk.seqfunks.Extract;
import network.artic.phylofunk.seqfunks.Hashes;

/**
 *
 */
class FastaFunk extends FunkApp {
    private final static String NAME = "jfastafunk";
    private static final String VERSION = "v1.0.0";
    private static final String HEADER = "\nFastaFunk " + VERSION + "\nBunch of functions for FASTA files\n\n";
    private static final String FOOTER = "";

    private static final FunkFactory[] FACTORIES = new FunkFactory[] {
            Annotate.FACTORY,
            Dedupe.FACTORY,
            Extract.FACTORY,
            // Hashes.FACTORY
    };

    FastaFunk(String[] args) {
        super(args, FACTORIES, NAME, VERSION, HEADER, FOOTER);
    }


    public static void main(String[] args) {
        new FastaFunk(args);
    }

}

