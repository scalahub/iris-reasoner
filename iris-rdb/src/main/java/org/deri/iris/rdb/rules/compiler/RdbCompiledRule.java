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
package org.deri.iris.rdb.rules.compiler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.storage.IRdbRelation;
import org.deri.iris.rdb.storage.RdbEmptyTupleRelation;
import org.deri.iris.rdb.storage.RdbUnionRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compiles a rule and creates a relation for the body of the rule according to
 * the algorithm defined in SOA4All D3.2.9.
 */
public class RdbCompiledRule implements IRdbCompiledRule {

	private static final Logger logger = LoggerFactory
			.getLogger(RdbCompiledRule.class);

	/** The connection to the database. */
	private final Connection connection;

	/** The rule elements in order. */
	private List<RdbRuleElement> elements;

	/** The head predicate. */
	private final IPredicate headPredicate;

	public RdbCompiledRule(Connection connection,
			List<RdbRuleElement> elements, IPredicate headPredicate) {
		this.connection = connection;
		this.headPredicate = headPredicate;
		this.elements = elements;
	}

	@Override
	public IRdbRelation evaluate() throws EvaluationException {
		// The first literal receives no relation.
		IRdbRelation output = null;

		for (RdbRuleElement element : elements) {
			output = element.process(output);

			if (output == null) {
				logger.warn("Output relation of a compiled rule element is null");
			}

			// Must always get some output relation, even if it is empty.
			assert output != null;

			// All literals are conjunctive, so if any literal produces no
			// results, then the whole rule produces no results. However,
			// computing the size for large relations may be more expensive than
			// just progressing with evaluation, as in the end the evaluation
			// of the SQL statements "stops" anyway if any of the involved
			// relations is empty.
		}

		return output;
	}

	@Override
	public IRdbRelation evaluateIteratively(IFacts deltas)
			throws EvaluationException {
		List<IRdbRelation> unionRelations = new ArrayList<IRdbRelation>();

		for (int r = 0; r < elements.size(); ++r) {
			RdbRuleElement original = elements.get(r);
			RdbRuleElement substitution = original.getDeltaSubstitution(deltas);

			if (substitution != null) {
				elements.set(r, substitution);

				// Now evaluate the modified rule.
				IRdbRelation output = evaluate();

				if (output == null) {
					logger.warn("Output relation of a compiled rule element is null");
				}

				// Must always get some output relation, even if it is empty.
				assert output != null;

				unionRelations.add(output);

				elements.set(r, original);

				// Dispose the substitution, as we do not need it anymore.
				substitution.dispose();
			}
		}

		try {
			if (unionRelations.size() > 0) {
				return new RdbUnionRelation(connection, unionRelations);
			} else {
				return new RdbEmptyTupleRelation(connection, 0);
			}
		} catch (SQLException e) {
			return null;
		}
	}

	public IPredicate headPredicate() {
		return headPredicate;
	}

	public ITuple getOutputTuple() {
		if (elements.size() > 0) {
			return elements.get(elements.size() - 1).getOutputTuple();
		} else {
			return Factory.BASIC.createTuple();
		}
	}

	@Override
	public List<IVariable> getVariablesBindings() {
		throw new UnsupportedOperationException(
				"Does not support variable bindings");
	}

}
