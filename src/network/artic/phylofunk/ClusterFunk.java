package network.artic.phylofunk;

import network.artic.phylofunk.funks.Funk;
import network.artic.phylofunk.funks.FunkFactory;
import network.artic.phylofunk.treefunks.Annotate;
import network.artic.phylofunk.treefunks.Collapse;
import org.apache.commons.cli.*;

import java.util.Arrays;

/**
 * Entrypoint class with main().
 */
class ClusterFunk extends FunkApp {

    private final static String NAME = "jclusterfunk";
    private static final String VERSION = "v1.0.0";
    private static final String HEADER = NAME + " " + VERSION + "\nBunch of functions for trees\n\n";
    private static final String FOOTER = "";

    private static final FunkFactory[] FACTORIES = new FunkFactory[]{
            Annotate.FACTORY,
            Collapse.FACTORY
    };

    ClusterFunk(String[] args) {
        super(args, FACTORIES, NAME, VERSION, HEADER, FOOTER);
    }

    public static void main(String[] args) {
        new ClusterFunk(args);
    }
}

