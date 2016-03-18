/*
 * Integrated Rule Inference System (IRIS):
 * An extensible rule inference system for datalog with extensions.
 * 
 * Copyright (C) 2011 Semantic Technology Institute (STI) Innsbruck, 
 * University of Innsbruck, Technikerstrasse 21a, 6020 Innsbruck, Austria.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.deri.iris.rdb.cli;

import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deri.iris.Configuration;
import org.deri.iris.optimisations.magicsets.MagicSets;
import org.deri.iris.optimisations.rulefilter.RuleFilter;
import org.deri.iris.rules.safety.StandardRuleSafetyProcessor;

public class Console {

	public static void main(String[] args) {
		// Create the Options.
		Options options = new Options();

		options.addOption("db", "database", true,
				"the path for the database files");
		options.addOption("p", "program", true, "the Datalog program");
		options.addOption("pf", "program-file", true,
				"the Datalog program file");
		options.addOption("ms", "magic-sets", false,
				"uses magic-sets optimization");
		options.addOption("m", "in-memory", false,
				"uses in-memory database (default is false)");
		options.addOption("t", "timeout", true,
				"timeout in miliseconds (default is to run forever)");
		options.addOption("s", "safe-rules", false, "to allow only safe rules");

		// Create the command line parser.
		CommandLineParser parser = new PosixParser();
		CommandLine line = null;

		try {
			// Parse the command line arguments.
			line = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			
			// Automatically generate the help statement.
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Console", options);
			System.exit(1);
		}

		String program = "";
		Configuration configuration = new Configuration();

		if (line.hasOption("program")) {
			program = line.getOptionValue("program");
		}

		if (line.hasOption("program-file")) {
			String filename = line.getOptionValue("program-file");

			try {
				program = loadFile(filename);
			} catch (Exception e) {
				System.err.println("Unable to load input file '" + filename
						+ "': " + e.getMessage());
				System.exit(2);
			}
		}

		if (line.hasOption("magic-sets")) {
			configuration.programOptmimisers.add(new RuleFilter());
			configuration.programOptmimisers.add(new MagicSets());
		}

		if (line.hasOption("timeout")) {
			int timeout = Integer.parseInt(line.getOptionValue("timeout"));
			configuration.evaluationTimeoutMilliseconds = timeout;
		}

		if (line.hasOption("safe-rules")) {
			configuration.ruleSafetyProcessor = new StandardRuleSafetyProcessor();
		}

		boolean useInMemory = false;
		if (line.hasOption("in-memory")) {
			useInMemory = true;
		}

		if (program.isEmpty()) {
			// Automatically generate the help statement.
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Console", options);
		} else {
			execute(program, configuration, useInMemory);
		}
	}

	private static final String loadFile(String fileName) throws IOException {
		FileReader reader = new FileReader(fileName);

		StringBuilder builder = new StringBuilder();

		int ch = -1;
		while ((ch = reader.read()) >= 0) {
			builder.append((char) ch);
		}

		return builder.toString();
	}

	public static void execute(String program, Configuration configuration,
			boolean useInMemory) {
		ExecutorService service = Executors.newCachedThreadPool();

		ProgramExecutor executor = new ProgramExecutor(program, configuration,
				useInMemory);
		Future<String> future = service.submit(executor);

		long timeout = configuration.evaluationTimeoutMilliseconds;

		try {
			String output = null;

			if (timeout > 0) {
				output = future.get(timeout, TimeUnit.MILLISECONDS);
			} else {
				output = future.get();
			}

			if (output != null) {
				System.out.println(output);
			} else {
				System.err.println("Output is null");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			System.out.println("Evaluation timed out (timeout = " + timeout
					+ "ms)");
			e.printStackTrace();
		} finally {
			service.shutdown();
		}
	}
}
