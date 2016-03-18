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

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deri.iris.Configuration;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.compiler.Parser;
import org.deri.iris.facts.Facts;
import org.deri.iris.facts.IFacts;
import org.deri.iris.optimisations.magicsets.MagicSets;
import org.deri.iris.optimisations.rulefilter.RuleFilter;
import org.deri.iris.rdb.RdbKnowledgeBase;
import org.deri.iris.storage.IRelation;

/**
 * An example program created for SOA4All D3.2.8.
 */
public class Example {

	public static void main(String[] args) throws Exception {
		// Create a Reader on the Datalog program file.
		File program = new File("datalog_program.iris");
		Reader reader = new FileReader(program);

		// Parse the Datalog program.
		Parser parser = new Parser();
		parser.parse(reader);

		// Retrieve the facts, rules and queries from the parsed program.
		Map<IPredicate, IRelation> factMap = parser.getFacts();
		List<IRule> rules = parser.getRules();
		List<IQuery> queries = parser.getQueries();

		// Create a default configuration.
		Configuration configuration = new Configuration();

		// Enable Magic Sets together with rule filtering.
		configuration.programOptmimisers.add(new RuleFilter());
		configuration.programOptmimisers.add(new MagicSets());

		// Convert the map from predicate to relation to a IFacts object.
		IFacts facts = new Facts(factMap, configuration.relationFactory);

		// Create the knowledge base.
		IKnowledgeBase knowledgeBase = new RdbKnowledgeBase(facts, rules,
				configuration);

		// Evaluate all queries over the knowledge base.
		for (IQuery query : queries) {
			List<IVariable> variableBindings = new ArrayList<IVariable>();
			IRelation relation = knowledgeBase.execute(query, variableBindings);

			// Output the variables.
			System.out.println(variableBindings);

			// For performance reasons compute the relation size only once.
			int relationSize = relation.size();
			
			// Output each tuple in the relation, where the term at position i
			// corresponds to the variable at position i in the variable
			// bindings list.
			for (int i = 0; i < relationSize; i++) {
				System.out.println(relation.get(i));
			}
		}
	}
	
}
