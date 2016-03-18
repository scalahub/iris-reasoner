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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.deri.iris.Configuration;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.compiler.Parser;
import org.deri.iris.facts.Facts;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.RdbKnowledgeBase;
import org.deri.iris.storage.IRelation;

/**
 * Evaluates a Datalog program using the {@link RdbKnowledgeBase}.
 */
public class ProgramExecutor implements Callable<String> {

	/** The new line separator to use when formatting output. */
	public static final String NEW_LINE = System.getProperty("line.separator");

	/** Output helper. */
	public static final String BAR = "----------------------------------";

	/** Flag for how to format the output. */
	public static final boolean SHOW_VARIABLE_BINDINGS = true;

	/** Flag for how to format the output. */
	public static final boolean SHOW_QUERY_TIME = true;

	/** Flag for how to format the output. */
	public static final boolean SHOW_ROW_COUNT = true;

	/** Flag for showing the relation. */
	public static final boolean SHOW_RELATION = false;

	private final String program;

	private final Configuration configuration;

	private final boolean useInMemory;

	/**
	 * Constructor. This is where the program is actually evaluated.
	 * 
	 * @param program
	 *            The Datalog program to evaluate.
	 * @param configuration
	 *            The configuration object.
	 */
	public ProgramExecutor(String program, Configuration configuration,
			boolean useInMemory) {
		this.program = program;
		this.configuration = configuration;
		this.useInMemory = useInMemory;
	}

	/**
	 * Format the actual query results (tuples) and append them to the specified
	 * {@link StringBuilder}.
	 * 
	 * @param builder
	 *            The {@link StringBuilder} representing the output.
	 * @param relation
	 *            The relation, whose tuples should be appended to the
	 *            {@link StringBuilder}.
	 */
	private void formatResults(StringBuilder builder, IRelation relation) {
		int size = relation.size();

		for (int t = 0; t < size; ++t) {
			ITuple tuple = relation.get(t);
			builder.append(tuple.toString()).append(NEW_LINE);
		}
	}

	@Override
	public String call() throws Exception {
		RdbKnowledgeBase knowledgeBase = null;

		try {
			Parser parser = new Parser();
			parser.parse(program);

			Map<IPredicate, IRelation> relations = parser.getFacts();
			List<IRule> rules = parser.getRules();

			IFacts facts = new Facts(relations, configuration.relationFactory);

			StringBuilder output = new StringBuilder();

			long duration = -System.currentTimeMillis();

			// Create the knowledge base.
			knowledgeBase = new RdbKnowledgeBase(facts, rules, configuration,
					useInMemory);

			duration += System.currentTimeMillis();

			if (SHOW_QUERY_TIME) {
				output.append("Init time: ").append(duration).append("ms")
						.append(NEW_LINE);
			}

			List<IVariable> variableBindings = new ArrayList<IVariable>();

			for (IQuery query : parser.getQueries()) {
				duration = -System.currentTimeMillis();

				// Execute the query.
				IRelation results = knowledgeBase.execute(query,
						variableBindings);

				duration += System.currentTimeMillis();

				output.append(BAR).append(NEW_LINE);
				output.append("Query:      ").append(query);

				if (SHOW_ROW_COUNT) {
					output.append(" ==>> ").append(results.size());
					if (results.size() == 1)
						output.append(" row");
					else
						output.append(" rows");
				}

				if (SHOW_QUERY_TIME) {
					output.append(" in ").append(duration).append("ms");
				}

				output.append(NEW_LINE);

				if (SHOW_VARIABLE_BINDINGS) {
					output.append("Variables:  ");
					boolean first = true;
					for (IVariable variable : variableBindings) {
						if (first)
							first = false;
						else
							output.append(", ");
						output.append(variable);
					}
					output.append(NEW_LINE);
				}

				if (SHOW_RELATION) {
					formatResults(output, results);
				}
			}

			return output.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (knowledgeBase != null) {
				knowledgeBase.dispose();
			}
		}

		return null;
	}

}
