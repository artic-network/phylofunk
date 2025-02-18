package network.artic.phylofunk.funks;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * A factory interface for creating commands.
 */
public interface FunkFactory {
    String getName();

    String getDescription();

    void setOptions(Options options);

    void create(CommandLine commandLine, boolean isVerbose);
}
