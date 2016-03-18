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

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.storage.IRdbRelation;
import org.deri.iris.rdb.storage.RdbView;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdbFirstSubgoal extends RdbRuleElement {

	private static Logger logger = LoggerFactory
			.getLogger(RdbFirstSubgoal.class);

	private final Connection connection;

	private final IPredicate predicate;

	private final ITuple viewCriteria;

	private IRdbRelation view;

	public RdbFirstSubgoal(Connection connection, IPredicate predicate,
			IRdbRelation relation, ITuple viewCriteria) throws SQLException {
		this.connection = connection;
		this.predicate = predicate;
		this.viewCriteria = viewCriteria;

		if (relation.getArity() == 0) {
			view = relation;
		} else {
			view = new RdbView(connection, relation, viewCriteria,
					String.valueOf(hashCode()));
		}
	}

	@Override
	public IRdbRelation process(IRdbRelation input) throws EvaluationException {
		return view;
	}

	@Override
	public ITuple getOutputTuple() {
		return viewCriteria;
	}

	@Override
	public RdbRuleElement getDeltaSubstitution(IFacts deltas) {
		IRelation relation = deltas.get(predicate);

		if (relation instanceof IRdbRelation) {
			try {
				return new RdbFirstSubgoal(connection, predicate,
						(IRdbRelation) relation, viewCriteria);
			} catch (SQLException e) {
				logger.error("Failed to create delta substitution for " + this);
			}
		}

		return null;
	}

	@Override
	public void dispose() {
		// Only close the view created by the constructor.
		if (view != null && view.getArity() != 0) {
			view.close();
		}
	}
}
