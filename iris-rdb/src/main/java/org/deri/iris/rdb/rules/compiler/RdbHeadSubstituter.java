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
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.storage.IRdbRelation;
import org.deri.iris.rdb.storage.RdbProjectedRelation;
import org.deri.iris.rdb.storage.RdbTempRelation;

public class RdbHeadSubstituter extends RdbRuleElement {

	private final Connection connection;

	private final ITuple outputTuple;

	private ITuple headTuple;

	public RdbHeadSubstituter(Connection connection, ITuple outputTuple,
			ITuple headTuple) {
		this.connection = connection;
		this.outputTuple = outputTuple;
		this.headTuple = headTuple;
	}

	@Override
	public IRdbRelation process(IRdbRelation input) throws EvaluationException {
		if (input.getArity() == 0 && input.size() > 0) {
			if (!headTuple.isGround()) {
				throw new EvaluationException("Found non-ground tuple in the "
						+ "head of a rule with empty body");
			}

			try {
				IRdbRelation temp = new RdbTempRelation(connection,
						headTuple.size());
				temp.add(headTuple);

				return temp;
			} catch (SQLException e) {
				throw new EvaluationException(e.getMessage());
			}
		}

		try {
			return new RdbProjectedRelation(connection, input, headTuple,
					outputTuple);
		} catch (SQLException e) {
			throw new EvaluationException(e.getMessage());
		}
	}

	@Override
	public ITuple getOutputTuple() {
		return headTuple;
	}

	@Override
	public RdbRuleElement getDeltaSubstitution(IFacts deltas) {
		return null;
	}

	@Override
	public void dispose() {
		// Nothing to do.
	}

}
