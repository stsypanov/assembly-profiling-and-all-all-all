package com.tsypanov.benchmark;

import org.openjdk.jmh.runner.Defaults;
import org.openjdk.jmh.runner.NoBenchmarksException;
import org.openjdk.jmh.runner.ProfilersFailedException;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.CommandLineOptionException;
import org.openjdk.jmh.runner.options.CommandLineOptions;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.io.IOException;

public class Main {

    public static void main(String[] argv) throws IOException {
        try {
            CommandLineOptions cmdOptions = new CommandLineOptions(argv);

            Runner runner = new Runner(cmdOptions);

            if (cmdOptions.shouldHelp()) {
                cmdOptions.showHelp();
                return;
            }

            if (cmdOptions.shouldList()) {
                runner.list();
                return;
            }

            if (cmdOptions.shouldListWithParams()) {
                runner.listWithParams(cmdOptions);
                return;
            }

            if (cmdOptions.shouldListProfilers()) {
                cmdOptions.listProfilers();
                return;
            }

            if (cmdOptions.shouldListResultFormats()) {
                cmdOptions.listResultFormats();
                return;
            }

            try {
                runner.run();
            } catch (NoBenchmarksException e) {
                System.err.println("No matching benchmarks. Miss-spelled regexp?");

                if (cmdOptions.verbosity().orElse(Defaults.VERBOSITY) != VerboseMode.EXTRA) {
                    System.err.println("Use " + VerboseMode.EXTRA + " verbose mode to debug the pattern matching.");
                } else {
                    runner.list();
                }
                System.exit(1);
            } catch (ProfilersFailedException e) {
                // This is not exactly an error, print all messages and set the non-zero exit code.
                Throwable ex = e;
                while (ex != null) {
                    System.err.println(ex.getMessage());
                    for (Throwable supp : ex.getSuppressed()) {
                        System.err.println(supp.getMessage());
                    }
                    ex = ex.getCause();
                }
                System.exit(1);
            } catch (RunnerException e) {
                System.err.print("ERROR: ");
                e.printStackTrace(System.err);
                System.exit(1);
            }

        } catch (CommandLineOptionException e) {
            System.err.println("Error parsing command line:");
            System.err.println(" " + e.getMessage());
            System.exit(1);
        }
    }

}