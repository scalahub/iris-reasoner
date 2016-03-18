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
package org.deri.iris.rdb.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.rdb.utils.RdbUtils;
import org.deri.iris.storage.IRelation;

/**
 * This relation does not return duplicate tuples.
 */
public class RdbTrueRelation extends AbstractRdbRelation {

	public static final String TRUE_NAME = "__true__";

	private static Map<Connection, RdbTrueRelation> relations;

	private RdbEmptyTupleRelation relation;

	static {
		relations = new HashMap<Connection, RdbTrueRelation>();
	}

	private RdbTrueRelation(Connection connection) throws SQLException {
		super(connection);

		this.relation = new RdbEmptyTupleRelation(connection, TRUE_NAME, 1);
	}

	public static RdbTrueRelation getInstance(Connection connection)
			throws SQLException {
		RdbTrueRelation relation = relations.get(connection);

		if (relation == null) {
			relation = new RdbTrueRelation(connection);
			relations.put(connection, relation);
		}

		return relation;
	}

	@Override
	public int getArity() {
		return 0;
	}

	@Override
	public String getTableName() {
		return RdbUtils.quoteIdentifier(TRUE_NAME);
	}

	@Override
	public void drop() {
		// Do nothing.
	}

	@Override
	public boolean add(ITuple tuple) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(IRelation relation) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public ITuple get(int index) {
		if (index < size()) {
			return Factory.BASIC.createTuple();
		}

		return null;
	}

	@Override
	public boolean contains(ITuple tuple) {
		if (tuple.size() == 0) {
			return true;
		}

		return false;
	}

	@Override
	public void close() {
		relation.close();
	}

	@Override
	public String toString() {
		return relation.toString();
	}

	@Override
	public CloseableIterator<ITuple> iterator() {
		return relation.iterator();
	}

}
