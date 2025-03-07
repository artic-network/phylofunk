package network.artic.phylofunk;

import network.artic.phylofunk.funks.FunkFactory;
import network.artic.phylofunk.funks.Merge;
import network.artic.phylofunk.treefunks.*;

/**
 * Entrypoint class with main().
 */
class ClusterFunk extends FunkApp {

    private final static String NAME = "jclusterfunk";
    private static final String HEADER = NAME + " " + VERSION + "\nBunch of functions for trees\n\n";
    private static final String FOOTER = "";

    private static final FunkFactory[] FACTORIES = new FunkFactory[]{
            Annotate.FACTORY,
            Collapse.FACTORY,
            Convert.FACTORY,
            Extract.FACTORY,
            Insert.FACTORY,
            Merge.FACTORY,
            Prune.FACTORY,
            Reconstruct.FACTORY,
            Reorder.FACTORY,
            Reroot.FACTORY,
            Sample.FACTORY,
            Scale.FACTORY,
            Split.FACTORY,
            Statistics.FACTORY,
            TMRCA.FACTORY
    };

    ClusterFunk(String[] args) {
        super(args, FACTORIES, NAME, HEADER, FOOTER);
    }

    public static void main(String[] args) {
        new ClusterFunk(args);
    }
}

