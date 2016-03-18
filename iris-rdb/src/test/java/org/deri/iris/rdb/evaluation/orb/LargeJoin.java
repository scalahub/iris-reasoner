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
package org.deri.iris.rdb.evaluation.orb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.deri.iris.facts.IFacts;

/**
 * Executes the LargeJoin tests of the OpenRuleBench.
 */
public abstract class LargeJoin {

	public void join1(Join1Query query, Join1Data data) throws Exception {
		File program = null;

		ClassLoader loader = getClass().getClassLoader();

		URL join1DataUrl = loader.getResource(data.getFileName());
		URL join1QueryUrl = loader.getResource(query.getFileName());

		try {
			program = File.createTempFile("join1program", ".iris");
			program.createNewFile();

			FileUtils.copyURLToFile(join1DataUrl, program);
			File queryFile = new File(join1QueryUrl.toURI());

			List<String> lines = FileUtils.readLines(queryFile);

			FileWriter writer = new FileWriter(program, true);
			for (String line : lines) {
				writer.write(line);
				writer.write(System.getProperty("line.separator"));
			}

			writer.flush();
			writer.close();

			execute(program);
		} finally {
			if (program != null) {
				program.delete();
			}

			dispose();
		}
	}

	public void join2() throws Exception {
		try {
			ClassLoader loader = getClass().getClassLoader();

			URL join1Url = loader.getResource("openrulebench/join2/join2.iris");

			File program = new File(join1Url.toURI());
			execute(program);
		} finally {
			dispose();
		}
	}

	public void dblp() throws Exception {
		File program = null;

		ClassLoader loader = getClass().getClassLoader();

		URL dataUrl = loader
				.getResource("openrulebench/dblp_test/dblp_data.iris");
		URL programUrl = loader
				.getResource("openrulebench/dblp_test/dblp_program.iris");

		try {
			program = File.createTempFile("join1program", ".iris");
			program.createNewFile();

			FileUtils.copyURLToFile(dataUrl, program);
			File queryFile = new File(programUrl.toURI());

			List<String> lines = FileUtils.readLines(queryFile);

			FileWriter writer = new FileWriter(program, true);
			for (String line : lines) {
				writer.write(line);
				writer.write(System.getProperty("line.separator"));
			}

			writer.flush();
			writer.close();

			execute(program);
		} finally {
			if (program != null) {
				program.delete();
			}

			dispose();
		}
	}

	private void execute(File program) throws Exception {
		IFacts facts = createFacts();
		List<IRule> rules = new ArrayList<IRule>();
		List<IQuery> queries = new ArrayList<IQuery>();

		long start = System.currentTimeMillis();
		parse(program, facts, rules, queries);
		long end = System.currentTimeMillis();
		double duration = (end - start) / 1000.0;

		System.out.println("Parsing program took " + duration + "s");

		IKnowledgeBase kb = createKnowledgeBase(facts, rules);

		for (IQuery query : queries) {
			start = System.currentTimeMillis();

			// Execute the query.
			kb.execute(query);

			end = System.currentTimeMillis();
			duration = (end - start) / 1000.0;

			System.out.println(query + " took " + duration + "s");
		}
	}

	private void parse(File program, IFacts facts, List<IRule> rules,
			List<IQuery> queries) throws ParserException, IOException {
		Parser parser = new Parser(facts);
		BufferedReader reader = new BufferedReader(new FileReader(program),
				1024 * 1024);

		String line;
		long i = 0;
		while ((line = reader.readLine()) != null) {
			System.err.println("" + (i++));

			parser.parse(line);
			rules.addAll(parser.getRules());
			queries.addAll(parser.getQueries());
		}

		reader.close();
	}

	protected abstract IFacts createFacts();

	protected abstract IKnowledgeBase createKnowledgeBase(IFacts facts,
			List<IRule> rules) throws EvaluationException;

	protected abstract void dispose();

	public static enum Join1Data {

		DATA0("openrulebench/join1/join1_50000.iris"),

		DATA1("openrulebench/join1/join1_250000.iris"),

		DATA2("openrulebench/join1/join1_1250000.iris");

		private String fileName;

		private Join1Data(String fileName) {
			this.fileName = fileName;
		}

		public String getFileName() {
			return fileName;
		}

	}

	public static enum Join1Query {

		A("openrulebench/join1/join_a.iris"),

		B1("openrulebench/join1/join_b1.iris"),

		B2("openrulebench/join1/join_b2.iris"),

		BF_A("openrulebench/join1/join_bf_a.iris"),

		BF_B1("openrulebench/join1/join_bf_b1.iris"),

		BF_B2("openrulebench/join1/join_bf_b2.iris"),

		DUPLICATE_A("openrulebench/join1/join_duplicate_a.iris"),

		DUPLICATE_B1("openrulebench/join1/join_duplicate_b1.iris"),

		DUPLICATE_B2("openrulebench/join1/join_duplicate_b2.iris"),

		FB_A("openrulebench/join1/join_fb_a.iris"),

		FB_B1("openrulebench/join1/join_fb_b1.iris"),

		FB_B2("openrulebench/join1/join_fb_b2.iris");

		private String fileName;

		private Join1Query(String fileName) {
			this.fileName = fileName;
		}

		public String getFileName() {
			return fileName;
		}

	}

}
