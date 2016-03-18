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
 * attributes of the left relation do not have the same values as the attributes
 * of the right relation. This relation may return duplicate tuples.
 */
public class RdbDisjoinedRelation extends AbstractRdbRelation {

	private static final String VIEW_SUFFIX = "_disjoined";

	private IRdbRelation leftRelation;

	private IRdbRelation rightRelation;

	private IRdbRelation viewRelation;

	private List<List<Integer>> indices;

	private String suffix;

	public RdbDisjoinedRelation(Connection connection,
			IRdbRelation leftRelation, IRdbRelation rightRelation,
			List<List<Integer>> indices) throws SQLException {
		this(connection, leftRelation, rightRelation, indices, null);
	}

	public RdbDisjoinedRelation(Connection connection,
			IRdbRelation leftRelation, IRdbRelation rightRelation,
			List<List<Integer>> indices, String suffix) throws SQLException {
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
		return leftRelation.getArity();
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
		// Close this relation.
		close();
		
		// Drop the view relation.
		viewRelation.drop();
		
		// Drop the view and all depending views if it exists.
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
		String createViewFormat = "CREATE OR REPLACE VIEW %s(%s) AS "
				+ "SELECT %s FROM %s %s";

		String newAttributes = createNewAttributes();
		String selectAttributes = createSelectAttributes();
		String joinClause = createJoinClause();

		String createViewSql = String.format(createViewFormat, getTableName(),
				newAttributes, selectAttributes, leftRelation.getTableName(),
				joinClause);

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
		String leftTableName = leftRelation.getTableName();
		String rightTableName = rightRelation.getTableName();

		List<String> attributes = new ArrayList<String>();

		for (int i = 0; i < leftRelation.getArity(); i++) {
			attributes.add(leftTableName + ".attr" + (i + 1));
		}

		for (int i = 0; i < rightRelation.getArity(); i++) {
			attributes.add(rightTableName + ".attr" + (i + 1));
		}

		return attributes;
	}

	private String createSelectAttributes() {
		List<String> attributes = createAttributeList();
		List<String> selectAttributes = new ArrayList<String>();

		for (int i = 0; i < getArity(); i++) {
			selectAttributes.add(attributes.get(i) + " AS "
					+ IRdbRelation.ATTRIBUTE_PREFIX + (i + 1));
		}

		return RdbUtils.join(selectAttributes, ", ");
	}

	private String createJoinClause() {
		List<String> joinParts = new ArrayList<String>();
		List<String> isNullParts = new ArrayList<String>();

		List<String> attributes = createAttributeList();

		for (List<Integer> joinIndices : indices) {
			if (joinIndices.size() > 1) {
				String firstAttribute = attributes.get(joinIndices.get(0));

				for (int i = 1; i < joinIndices.size(); i++) {
					String otherAttribute = attributes.get(joinIndices.get(i));
					String wherePart = firstAttribute + " = " + otherAttribute;
					joinParts.add(wherePart);

					String isNullPart = otherAttribute + " IS NULL";
					isNullParts.add(isNullPart);
				}
			}
		}

		StringBuilder joinBuilder = new StringBuilder();

		// Define on which table will be joined.
		joinBuilder.append("LEFT JOIN " + rightRelation.getTableName());

		if (joinParts.size() > 0) {
			String joinConditions = RdbUtils.join(joinParts, " AND ");
			String whereConditions = RdbUtils.join(isNullParts, " AND ");

			String joinFormat = " ON %s WHERE %s";
			String join = String.format(joinFormat, joinConditions,
					whereConditions);

			joinBuilder.append(join);
		}

		return joinBuilder.toString();
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
