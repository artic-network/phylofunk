package network.artic.phylofunk.seqfunks;

import jebl.evolution.sequences.Sequence;

/**
 * @author Andrew Rambaut
 * @version $
 */
public interface SequenceProcessor {
    Sequence processSequence(Sequence sequence);
}
