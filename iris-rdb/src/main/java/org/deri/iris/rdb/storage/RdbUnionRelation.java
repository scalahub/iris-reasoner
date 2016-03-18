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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.rdb.utils.RdbUtils;
import org.deri.iris.storage.IRelation;

/**
 * This relation does not return duplicate tuples.
 */
public class RdbUnionRelation extends AbstractRdbRelation {

	private static final String VIEW_SUFFIX = "_union";

	private List<IRdbRelation> relations;

	private SimpleRdbRelation viewRelation;

	private final String suffix;

	public RdbUnionRelation(Connection connection, List<IRdbRelation> relations)
			throws SQLException {
		this(connection, relations, null);
	}

	public RdbUnionRelation(Connection connection,
			List<IRdbRelation> relations, String suffix) throws SQLException {
		super(connection);
		this.suffix = suffix;

		if (relations.size() == 0) {
			throw new IllegalArgumentException(
					"Can not create union of 0 relations");
		}

		int arity = -1;
		for (IRdbRelation relation : relations) {
			if (arity != -1) {
				if (arity != relation.getArity()) {
					throw new IllegalArgumentException(
							"Relations have different arity");
				}
			}

			arity = relation.getArity();
		}

		this.relations = relations;

		createView();

		viewRelation = new SimpleRdbRelation(connection, getTableName(),
				getArity());
	}

	// Create the view if it does not exist yet.
	private void createView() throws SQLException {
		String createViewFormat = "CREATE OR REPLACE VIEW %s(%s) AS %s";

		String newAttributes = createNewAttributes();
		String select = createSelect();

		String createViewSql = String.format(createViewFormat, getTableName(),
				newAttributes, select);

		Connection connection = getConnection();
		CallableStatement call = null;

		try {
			call = connection.prepareCall(createViewSql);

			logger.debug("Executing " + call);
			call.execute();
		} finally {
			RdbUtils.closeStatement(call);
		}
	}

	private List<String> createAttributeList() {
		List<String> attributeList = new ArrayList<String>();

		for (int i = 0; i < getArity(); i++) {
			attributeList.add(IRdbRelation.ATTRIBUTE_PREFIX + (i + 1));
		}

		return attributeList;
	}

	private String createNewAttributes() {
		List<String> attributeList = createAttributeList();
		return RdbUtils.join(attributeList, ", ");
	}

	private String createSelect() {
		String attributes = createNewAttributes();

		StringBuilder selects = new StringBuilder();

		int i = 0;
		String sqlFormat = "SELECT %s FROM %s";

		for (IRdbRelation relation : relations) {
			if (i++ > 0) {
				selects.append(" UNION ");
			}

			String sql = String.format(sqlFormat, attributes,
					relation.getTableName());
			selects.append(sql);
		}

		return selects.toString();
	}

	@Override
	public int getArity() {
		return relations.get(0).getArity();
	}

	@Override
	public String getTableName() {
		StringBuilder tableName = new StringBuilder();

		int i = 0;
		for (IRdbRelation relation : relations) {
			if (i++ > 0) {
				tableName.append("_");
			}

			tableName.append("(");
			String relationName = RdbUtils.unquoteIdentifier(relation
					.getTableName());
			tableName.append(relationName);
			tableName.append(")");
		}

		if (suffix != null) {
			tableName.append("_" + suffix);
		}
		
		tableName.append(VIEW_SUFFIX);

		return RdbUtils.quoteIdentifier(tableName.toString());
	}

	@Override
	public void drop() {
		// Close the relation.
		close();
		
		// Drop the view relation.
		viewRelation.drop();
		
		// Drop the view if it exists.
		String sqlFormat = "DROP VIEW IF EXISTS %s CASCADE";
		String sql = String.format(sqlFormat, getTableName());

		Connection connection = getConnection();
		CallableStatement call = null;

		try {
			call = connection.prepareCall(sql);

			logger.debug("Executing " + call);
			call.execute();
		} catch (SQLException e) {
			logger.error("Failed to drop view " + getTableName(), e);
		} finally {
			RdbUtils.closeStatement(call);
		}
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
		return viewRelation.size();
	}

	@Override
	public ITuple get(int index) {
		return viewRelation.get(index);
	}

	@Override
	public boolean contains(ITuple tuple) {
		return viewRelation.contains(tuple);
	}

	@Override
	public void close() {
		viewRelation.close();
	}

	@Override
	public String toString() {
		return viewRelation.toString();
	}

	@Override
	public CloseableIterator<ITuple> iterator() {
		return viewRelation.iterator();
	}

}
