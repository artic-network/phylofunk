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
class ClusterFunk {

    private final static String NAME = "jclusterfunk";
    private static final String VERSION = "v1.0";
    private static final String HEADER = NAME + " " + VERSION + "\nBunch of functions for trees\n\n";
    private static final String FOOTER = "";

    private static final FunkFactory[] FACTORIES = new FunkFactory[] {
            Annotate.FACTORY,
            Collapse.FACTORY
    };

    private static void printHelp(FunkFactory commandFactory, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        StringBuilder sb = new StringBuilder();
        sb.append(ClusterFunk.HEADER);

        if (commandFactory == null) {
            sb.append("Available commands:\n ");
            for (FunkFactory factory : FACTORIES) {
                sb.append(" ").append(factory.getName());
            }
            sb.append("\n\nuse: <command> -h,--help to display individual options\n");

            formatter.printHelp(NAME + " <command> <options> [-h]", sb.toString(), options, ClusterFunk.FOOTER, false);
        } else {
            sb.append("Funk: ")
                    .append(commandFactory.getName())
                    .append("\n\n")
                    .append(commandFactory.getDescription())
                    .append("\n\n");
            formatter.printHelp(NAME + " " + commandFactory, sb.toString(), options, ClusterFunk.FOOTER, true);
        }

    }

    public static void main(String[] args) {

        // create Options object
        Options options = new Options();
        options.addOption("h", "help", false, "display help");
        options.addOption(null, "version", false, "display version");

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;

        FunkFactory factory = null;

        if (args.length > 0 && !args[0].startsWith("-")) {
            try {
                factory = Funk.getCommandFactory(args[0], FACTORIES);

                options.addOption("v","verbose", false, "write analysis details to console");

                factory.setOptions(options);

                commandLine = parser.parse( options, Arrays.copyOfRange(args, 1, args.length));

                if (commandLine.hasOption("help")) {
                    printHelp(factory, options);
                    return;
                }
                if (commandLine.hasOption("version")) {
                    System.out.println(VERSION);
                    return;
                }

            } catch (IllegalArgumentException iae) {
                System.out.println("Unrecognised command: " + args[0] + "\n");
                printHelp(null, options);
                return;
            } catch (ParseException pe) {
                System.out.println(pe.getMessage() + "\n");
                printHelp(null, options);
                return;
            }
        } else {
            try {
                commandLine = parser.parse(options, args);
            } catch (ParseException pe) {
                System.out.println(pe.getMessage() + "\n");
                printHelp(null, options);
                return;
            }

            if (commandLine.hasOption("version")) {
                System.out.println(VERSION);
                return;
            }

            printHelp(null, options);
            return;

        }

        boolean isVerbose = commandLine.hasOption("verbose");

        if (isVerbose) {
            System.out.println("Funk: " + factory.getName());
        }

        long startTime = System.currentTimeMillis();

        factory.create(commandLine, isVerbose);

        long timeTaken = (System.currentTimeMillis() - startTime) / 1000;

        if (isVerbose) {
            System.err.println("Time taken: " + timeTaken + " secs");
        }

    }

}

