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
 * A relation that joins two relations on the specified attributes, where the
 * attributes of the left relation have the same values as the attributes
 * of the right relation. This relation may return duplicate tuples.
 */
public class RdbJoinedRelation extends AbstractRdbRelation {

	private static final String VIEW_SUFFIX = "_joined";

	private static final String leftTableAlias = "leftTable";

	private static final String rightTableAlias = "rightTable";

	private IRdbRelation leftRelation;

	private IRdbRelation rightRelation;

	private IRdbRelation viewRelation;

	private List<List<Integer>> indices;

	private String suffix;

	public RdbJoinedRelation(Connection connection, IRdbRelation leftRelation,
			IRdbRelation rightRelation) throws SQLException {
		this(connection, leftRelation, rightRelation, (String) null);
	}

	public RdbJoinedRelation(Connection connection, IRdbRelation leftRelation,
			IRdbRelation rightRelation, String suffix) throws SQLException {
		this(connection, leftRelation, rightRelation,
				new ArrayList<List<Integer>>(), suffix);
	}

	public RdbJoinedRelation(Connection connection, IRdbRelation leftRelation,
			IRdbRelation rightRelation, List<List<Integer>> indices)
			throws SQLException {
		this(connection, leftRelation, rightRelation, indices, null);
	}

	public RdbJoinedRelation(Connection connection, IRdbRelation leftRelation,
			IRdbRelation rightRelation, List<List<Integer>> indices,
			String suffix) throws SQLException {
		super(connection);

		this.leftRelation = leftRelation;
		this.rightRelation = rightRelation;
		this.indices = indices;
		this.suffix = suffix;

		createView();

		viewRelation = new SimpleRdbRelation(connection, getTableName(),
				getArity());
	}

	@Override
	public int getArity() {
		return leftRelation.getArity() + rightRelation.getArity();
	}

	@Override
	public String getTableName() {
		String unquotedLeftTableName = RdbUtils.unquoteIdentifier(leftRelation
				.getTableName());
		String unquotedRightTableName = RdbUtils
				.unquoteIdentifier(rightRelation.getTableName());

		String viewName = "(" + unquotedLeftTableName + ")_("
				+ unquotedRightTableName + ")";

		if (suffix != null) {
			viewName += "_" + suffix;
		}

		viewName += VIEW_SUFFIX;

		return RdbUtils.quoteIdentifier(viewName);
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
	public void drop() {
		// Close the relation.
		close();
		
		// Drop the view relation.
		viewRelation.drop();
		
		// Drop the view (and all depending views) if it exists.
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

	// Create the view if it does not exist yet.
	private void createView() throws SQLException {
		// Performance of evaluation is much better, if there is no SELECT
		// DISTINCT in this step.
		String createViewFormat = "CREATE OR REPLACE VIEW %s(%s) AS "
				+ "SELECT %s FROM %s %s";

		String newAttributes = createNewAttributes();
		String oldAttributes = createAttributes();
		String tables = createTables();
		String whereClause = createWhereClause();

		String createViewSql = String.format(createViewFormat, getTableName(),
				newAttributes, oldAttributes, tables, whereClause);

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

	private String createTables() {
		String leftTableName = leftRelation.getTableName() + " AS "
				+ leftTableAlias;
		String rightTableName = rightRelation.getTableName() + " AS "
				+ rightTableAlias;

		return leftTableName + ", " + rightTableName;
	}

	private String createNewAttributes() {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < getArity(); i++) {
			if (i > 0) {
				builder.append(", ");
			}

			builder.append(IRdbRelation.ATTRIBUTE_PREFIX + (i + 1));
		}

		return builder.toString();
	}

	private List<String> createAttributeList() {
		String leftTableName = leftTableAlias;
		String rightTableName = rightTableAlias;

		List<String> attributes = new ArrayList<String>();

		for (int i = 0; i < leftRelation.getArity(); i++) {
			attributes.add(leftTableName + ".attr" + (i + 1));
		}

		for (int i = 0; i < rightRelation.getArity(); i++) {
			attributes.add(rightTableName + ".attr" + (i + 1));
		}

		return attributes;
	}

	private String createAttributes() {
		List<String> attributes = createAttributeList();

		StringBuilder builder = new StringBuilder();

		int i = 1;
		for (String attribute : attributes) {
			if (i > 1) {
				builder.append(", ");
			}

			builder.append(attribute);
			builder.append(" AS ");
			builder.append(IRdbRelation.ATTRIBUTE_PREFIX + i + " ");

			i++;
		}

		return builder.toString();
	}

	private String createWhereClause() {
		List<String> whereParts = new ArrayList<String>();

		List<String> attributeList = createAttributeList();

		String equalityOperand = " = ";

		for (List<Integer> joinIndices : indices) {
			if (joinIndices.size() > 1) {
				String firstAttribute = attributeList.get(joinIndices.get(0));

				for (int i = 1; i < joinIndices.size(); i++) {
					String otherAttribute = attributeList.get(joinIndices
							.get(i));
					String wherePart = firstAttribute + equalityOperand
							+ otherAttribute;
					whereParts.add(wherePart);
				}
			}
		}

		StringBuilder whereBuilder = new StringBuilder();
		whereBuilder.append(RdbUtils.join(whereParts, " AND "));

		if (whereBuilder.length() > 0) {
			whereBuilder.insert(0, "WHERE ");
		}

		return whereBuilder.toString();
	}

	@Override
	public String toString() {
		return viewRelation.toString();
	}

	@Override
	public void close() {
		viewRelation.close();
	}

	@Override
	public CloseableIterator<ITuple> iterator() {
		return viewRelation.iterator();
	}

}
