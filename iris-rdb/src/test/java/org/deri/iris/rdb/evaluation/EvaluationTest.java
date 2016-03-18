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

package org.deri.iris.rdb.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.deri.iris.Configuration;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.RdbKnowledgeBase;
import org.deri.iris.storage.IRelation;
import org.junit.After;
import org.junit.Before;

/**
 * An abstract class for evaluation tests, where only the evaluations of queries
 * against a specific program have to be tested.
 * 
 * @author Adrian Marte
 */
public abstract class EvaluationTest {

	protected List<IRule> rules;

	protected List<IQuery> queries;

	protected IFacts facts;

	protected Configuration configuration;

	private long duration;

	protected RdbKnowledgeBase kb;

	@Before
	public void setUp() throws Exception {
		// Create the default configuration.
		configuration = createConfiguration();

		// Create the facts.
		facts = createFacts();

		// Create the rules.
		rules = createRules();

		// Create the queries.
		queries = createQueries();

		// Create the knowledge base.
		kb = new RdbKnowledgeBase(facts, rules, configuration);
	}

	@After
	public void shutDown() {
		if (kb != null) {
			kb.dispose();
		}
	}

	protected abstract IFacts createFacts();

	protected abstract List<IRule> createRules();

	protected abstract List<IQuery> createQueries();

	protected Configuration createConfiguration() {
		return new Configuration();
	}

	protected IRelation evaluate(IQuery query) throws Exception {
		// Use default configuration.
		return evaluate(query, new ArrayList<IVariable>(), configuration);
	}

	protected IRelation evaluate(IQuery query, Configuration configuration)
			throws Exception {
		return evaluate(query, new ArrayList<IVariable>(), configuration);
	}

	protected IRelation evaluate(IQuery query, List<IVariable> outputVariables)
			throws Exception {
		// Use default configuration.
		return evaluate(query, outputVariables, configuration);
	}

	protected IRelation evaluate(IQuery query, List<IVariable> outputVariables,
			Configuration configuration) throws Exception {
		long begin = System.currentTimeMillis();
		IRelation relation = kb.execute(query, outputVariables);

		duration = System.currentTimeMillis() - begin;

		return relation;
	}

	/**
	 * Returns the time in milliseconds it took to evaluate the previous query.
	 * 
	 * @return The time in milliseconds it took to evaluate the previous query.
	 */
	protected long getDuration() {
		return duration;
	}

}
