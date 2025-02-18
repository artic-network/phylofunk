package network.artic.phylofunk.treefunks;

import jebl.evolution.trees.RootedTree;

/**
 * @author Andrew Rambaut
 * @version $
 */
public interface TreeFunction {
    RootedTree processTree(RootedTree tree);
}
