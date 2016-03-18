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
import org.deri.iris.factory.Factory;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.storage.IRdbRelation;
import org.deri.iris.rdb.storage.RdbEmptyTupleRelation;

public class RdbTrue extends RdbRuleElement {

	private static final String TABLE_NAME = "___true___";

	private IRdbRelation view;

	public RdbTrue(Connection connection) throws SQLException {
		view = new RdbEmptyTupleRelation(connection, TABLE_NAME, 1);
	}

	@Override
	public IRdbRelation process(IRdbRelation input) throws EvaluationException {
		return view;
	}

	@Override
	public ITuple getOutputTuple() {
		return Factory.BASIC.createTuple();
	}

	@Override
	public RdbRuleElement getDeltaSubstitution(IFacts deltas) {
		return this;
	}

	@Override
	public void dispose() {
		if (view != null) {
			view.close();
		}
	}
}
