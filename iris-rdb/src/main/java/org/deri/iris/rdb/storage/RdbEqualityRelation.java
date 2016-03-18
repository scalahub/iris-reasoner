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
import org.deri.iris.rdb.utils.RdbUtils;
import org.deri.iris.storage.IRelation;

/**
 * A relation that stores equivalent terms. This relation does not return
 * duplicate tuples.
 */
public class RdbEqualityRelation extends AbstractRdbRelation {

	private static final String TABLE_NAME = "__equals__";

	private static Map<Connection, RdbEqualityRelation> relations;

	private RdbRelation relation;

	static {
		relations = new HashMap<Connection, RdbEqualityRelation>();
	}

	private RdbEqualityRelation(Connection connection) throws SQLException {
		super(connection);

		this.relation = new RdbRelation(connection, TABLE_NAME, 2);
	}

	/**
	 * Returns a singleton instance for the specified connection. The instances
	 * are kept in a weak hash map, where the connection is the key and the
	 * relation is the value. When this method is called for two different
	 * connections, two different instances of this relation are returned.
	 * 
	 * @param connection
	 *            The connection to return the singleton instance for.
	 * @return A singleton instance of this relation for the specified
	 *         connection.
	 * @throws SQLException
	 *             If the relation can not be created.
	 */
	public static RdbEqualityRelation getInstance(Connection connection)
			throws SQLException {
		RdbEqualityRelation relation = relations.get(connection);

		if (relation == null) {
			relation = new RdbEqualityRelation(connection);
			relations.put(connection, relation);
		}

		return relation;
	}

	@Override
	public int getArity() {
		return relation.getArity();
	}

	@Override
	public String getTableName() {
		return RdbUtils.quoteIdentifier(TABLE_NAME);
	}

	@Override
	public void drop() {
		// Do nothing.
	}

	@Override
	public boolean add(ITuple tuple) {
		return relation.add(tuple);
	}

	@Override
	public boolean addAll(IRelation relation) {
		return this.relation.addAll(relation);
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
