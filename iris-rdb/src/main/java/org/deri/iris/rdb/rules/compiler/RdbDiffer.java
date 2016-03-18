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
import java.util.Set;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.storage.IRdbRelation;
import org.deri.iris.rdb.storage.RdbDisjoinedRelation;
import org.deri.iris.rdb.storage.RdbEmptyTupleRelation;
import org.deri.iris.rdb.storage.RdbView;
import org.deri.iris.rdb.utils.RdbUtils;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdbDiffer extends RdbRuleElement {

	private static Logger logger = LoggerFactory.getLogger(RdbDiffer.class);

	private Connection connection;

	private ITuple viewCriteria;

	private ITuple inputTuple;

	private List<List<Integer>> indices;

	private IPredicate predicate;

	private RdbView rightRelation;

	public RdbDiffer(Connection connection, IPredicate predicate,
			IRdbRelation relation, ITuple viewCriteria, ITuple inputTuple)
			throws SQLException {
		this.connection = connection;
		this.predicate = predicate;
		this.viewCriteria = viewCriteria;
		this.inputTuple = inputTuple;

		Set<IVariable> inputVariables = inputTuple.getVariables();
		List<IVariable> viewVariables = RdbUtils.uniqueVariables(viewCriteria);

		// Find the indices of the variables used in the natural join.
		List<Integer> join1 = new ArrayList<Integer>();
		List<Integer> join2 = new ArrayList<Integer>();

		for (IVariable leftVariable : inputVariables) {
			int leftIndex = inputTuple.indexOf(leftVariable);

			for (IVariable rightVariable : viewVariables) {
				int rightIndex = viewCriteria.indexOf(rightVariable);

				if (leftVariable.equals(rightVariable)) {
					join1.add(leftIndex);
					join2.add(rightIndex);

					// NB Variables in views occur only once.
					break;
				}
			}
		}

		indices = new ArrayList<List<Integer>>();

		for (int i = 0; i < join1.size(); i++) {
			List<Integer> joinIndices = new ArrayList<Integer>();

			joinIndices.add(join1.get(i));
			joinIndices.add(join2.get(i) + inputTuple.size());

			indices.add(joinIndices);
		}

		rightRelation = new RdbView(connection, relation, viewCriteria,
				String.valueOf(hashCode()), false);
	}

	@Override
	public IRdbRelation process(IRdbRelation leftRelation)
			throws EvaluationException {
		try {
			if (leftRelation == null) {
				return new RdbEmptyTupleRelation(connection, 1);
			}

			// TODO Check if this is correct.
			if (leftRelation.getArity() == 0) {
				return leftRelation;
			}

			return new RdbDisjoinedRelation(connection, leftRelation,
					rightRelation, indices, String.valueOf(hashCode()));
		} catch (SQLException e) {
			throw new EvaluationException(
					"Failed to create disjoined relation: " + e.getMessage());
		}
	}

	@Override
	public ITuple getOutputTuple() {
		return inputTuple;
	}

	@Override
	public RdbRuleElement getDeltaSubstitution(IFacts deltas) {
		IRelation relation = deltas.get(predicate);

		if (relation instanceof IRdbRelation) {
			try {
				return new RdbDiffer(connection, predicate,
						(IRdbRelation) relation, viewCriteria, inputTuple);
			} catch (SQLException e) {
				logger.error("Failed to create delta substitution for " + this);
			}
		}

		return null;
	}

	@Override
	public void dispose() {
		if (rightRelation != null) {
			rightRelation.close();
		}
	}

}
