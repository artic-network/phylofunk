package network.artic.phylofunk;

import network.artic.phylofunk.funks.Funk;
import network.artic.phylofunk.funks.FunkFactory;
import org.apache.commons.cli.*;

import java.util.Arrays;

/**
 * @author Andrew Rambaut
 * @version $
 */
public abstract class FunkApp {
    private final FunkFactory[] factories;
    private final String name;
    private final String version;
    private final String header;
    private final String footer;


    public FunkApp(String[] args, FunkFactory[] factories, String name, String version, String header, String footer) {

        this.factories = factories;
        this.name = name;
        this.version = version;
        this.header = header;
        this.footer = footer;

        // create Options object
        Options options = new Options();
        options.addOption("h", "help", false, "display help");
        options.addOption(null, "version", false, "display version and stop");

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;

        FunkFactory factory;

        if (args.length > 0 && !args[0].startsWith("-")) {
            factory = Funk.getCommandFactory(args[0], factories);

            for (String arg : Arrays.copyOfRange(args, 1, args.length)) {

                if (arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("--help")) {
                    // add the options here for the help message
                    factory.setOptions(options);
                    options.addOption("v", "verbose", false, "write analysis details to console");
                    printHelp(factory, options);
                    return;
                }
                if (arg.equalsIgnoreCase("--version") ) {
                    System.out.println(version);
                    return;
                }
            }

            factory.setOptions(options);

            options.addOption("v", "verbose", false, "write analysis details to console");

            try {
                commandLine = parser.parse(options, Arrays.copyOfRange(args, 1, args.length));
            } catch (IllegalArgumentException iae) {
                System.out.println("Unrecognised command: " + args[0] + "\n");
                printHelp(null, options);
                return;
            } catch (ParseException pe) {
                System.out.println(pe.getMessage() + "\n");
                printHelp(factory, options);
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
                System.out.println(version);
                return;
            }

            printHelp(null, options);
            return;

        }

        boolean isVerbose = commandLine.hasOption("verbose");

        if (isVerbose) {
            System.out.println("Command: " + factory.getName());
        }

        long startTime = System.currentTimeMillis();

        factory.create(commandLine, isVerbose);

        long timeTaken = (System.currentTimeMillis() - startTime) / 1000;

        if (isVerbose) {
            System.err.println("Time taken: " + timeTaken + " secs");
        }
    }

    private void printHelp(FunkFactory commandFactory, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        StringBuilder sb = new StringBuilder();
        sb.append(header);

        if (commandFactory == null) {
            sb.append("Available commands:\n");
//            for (FunkFactory factory : factories) {
//                sb.append(" ").append(factory.getName());
//            }
            int maxLen = 0;
            for (FunkFactory factory : factories) {
                if (factory.getName().length() > maxLen) {
                    maxLen = factory.getName().length();
                }
            }
            for (FunkFactory factory : factories) {
                sb.append("  ").append(factory.getName());
                for (int i = 0; i < maxLen - factory.getName().length(); i++) {
                    sb.append(" ");
                }
                sb.append(" - ").append(factory.getDescription()).append("\n");
            }

            sb.append("\nuse: " + name + " <command> -h,--help to display individual options\n\n");

            formatter.printHelp(name + " <command> <options> [-h]", sb.toString(), options, footer, false);
        } else {
            sb.append("Command: ")
                    .append(commandFactory.getName())
                    .append("\n\n")
                    .append(commandFactory.getDescription())
                    .append("\n\n");
            formatter.printHelp(name + " " + commandFactory.getName(), sb.toString(), options, footer, true);
        }
    }

}
