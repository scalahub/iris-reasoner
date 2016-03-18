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

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.rdb.utils.RdbUtils;
import org.deri.iris.storage.IRelation;

/**
 * This relation does not return duplicate tuples.
 */
public class RdbEmptyTupleRelation extends AbstractRdbRelation {

	private static final ITuple EMPTY_TUPLE = Factory.BASIC.createTuple();
	
	private static String PREFIX = "empty_";

	private int size;

	private String tableName;

	private RdbRelation relation;

	public RdbEmptyTupleRelation(Connection connection, int size)
			throws SQLException {
		this(connection, null, size);
	}

	public RdbEmptyTupleRelation(Connection connection, String tableName,
			int size) throws SQLException {
		super(connection);

		this.size = size;

		if (tableName == null) {
			// UUID uuid = UUID.randomUUID();
			// tableName = uuid.toString();
			tableName = PREFIX + hashCode();
		}

		this.tableName = tableName;
		this.relation = new RdbRelation(connection, tableName, 0);

		addTuples();
	}

	private void addTuples() {
		for (int i = 0; i < size; i++) {
			relation.add(EMPTY_TUPLE);
		}
	}

	@Override
	public int getArity() {
		return 0;
	}

	@Override
	public String getTableName() {
		return RdbUtils.quoteIdentifier(tableName);
	}

	@Override
	public void drop() {
		close();
		relation.drop();
	}

	@Override
	public boolean add(ITuple tuple) {
		return relation.add(tuple);
	}

	@Override
	public boolean addAll(IRelation relation) {
		return relation.addAll(relation);
	}

	@Override
	public int size() {
		return relation.size();
	}

	@Override
	public ITuple get(int index) {
		return relation.get(index);
	}

	@Override
	public boolean contains(ITuple tuple) {
		return relation.contains(tuple);
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
