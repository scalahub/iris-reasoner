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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.rdb.utils.RdbUtils;
import org.deri.iris.storage.IRelation;
import org.deri.iris.utils.UniqueList;

/**
 * This relation may return duplicate tuples.
 */
public class RdbView extends AbstractRdbRelation {

	private static final String VIEW_SUFFIX = "_view";

	private IRdbRelation relation;

	private ITuple viewCriteria;

	private boolean isPositive;

	private IRdbRelation viewRelation;

	private RdbUniverseRelation universe;

	private String suffix;

	public RdbView(Connection connection, IRdbRelation relation,
			ITuple viewCriteria) throws SQLException {
		this(connection, relation, viewCriteria, null, true);
	}

	public RdbView(Connection connection, IRdbRelation relation,
			ITuple viewCriteria, String suffix) throws SQLException {
		this(connection, relation, viewCriteria, suffix, true);
	}

	public RdbView(Connection connection, IRdbRelation relation,
			ITuple viewCriteria, String suffix, boolean isPositive)
			throws SQLException {
		super(connection);

		this.relation = relation;
		this.viewCriteria = viewCriteria;
		this.isPositive = isPositive;
		this.suffix = suffix;

		this.universe = RdbUniverseRelation.getInstance(connection);

		createView();

		viewRelation = new SimpleRdbRelation(connection, getTableName(),
				getArity());
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
	public void drop() {
		// Close the relation.
		close();

		// Drop the view relation.
		viewRelation.drop();

		// Create the view if it does not exist yet.
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

	private void createView() throws SQLException {
		// Create the view if it does not exist yet.
		String createViewFormat = "CREATE OR REPLACE VIEW %s AS "
				+ "SELECT * FROM %s %s";

		String whereClause = createWhereClause();

		String createViewSql = String.format(createViewFormat, getTableName(),
				relation.getTableName(), whereClause);

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

	private String createWhereClause() {
		// Use the view criteria to create the WHERE clause of the SQL
		// queries.

		List<List<String>> attributesToJoin = new ArrayList<List<String>>();

		Set<IVariable> variables = viewCriteria.getVariables();

		for (IVariable variable : variables) {
			List<String> attributes = new ArrayList<String>();

			int i = 0;
			for (ITerm term : viewCriteria) {
				if (variable.equals(term)) {
					attributes.add("attr" + (i + 1));
				}

				i++;
			}

			attributesToJoin.add(attributes);
		}

		Map<String, Integer> valuesToFilter = new HashMap<String, Integer>();

		for (int i = 0; i < viewCriteria.size(); i++) {
			ITerm term = viewCriteria.get(i);

			universe.add(term);
			int termId = universe.getId(term);

			// TODO Check if normalized representation should be used.
			if (termId > -1) {
				valuesToFilter.put(IRdbRelation.ATTRIBUTE_PREFIX + (i + 1),
						termId);
			}
		}

		List<String> whereParts = new ArrayList<String>();

		String equalitySign = isPositive ? " = " : " <> ";

		for (List<String> attributeJoin : attributesToJoin) {
			if (attributeJoin.size() > 1) {
				String firstAttribute = attributeJoin.get(0);

				for (int i = 1; i < attributeJoin.size(); i++) {
					String wherePart = firstAttribute + equalitySign
							+ attributeJoin.get(i);
					whereParts.add(wherePart);
				}
			}
		}

		for (String attribute : valuesToFilter.keySet()) {
			String termId = valuesToFilter.get(attribute).toString();
			String wherePart = attribute + " = '" + termId + "'";
			whereParts.add(wherePart);
		}

		StringBuilder whereBuilder = new StringBuilder();

		int i = 0;
		for (String wherePart : whereParts) {
			if (i++ > 0) {
				whereBuilder.append(" AND ");
			}

			whereBuilder.append(wherePart);
		}

		if (whereBuilder.length() > 0) {
			whereBuilder.insert(0, "WHERE ");
		}

		return whereBuilder.toString();
	}

	@Override
	public String getTableName() {
		String parentTableName = RdbUtils.unquoteIdentifier(relation
				.getTableName());
		String viewName = "(" + parentTableName + ")";

		if (suffix != null) {
			viewName += "_" + suffix;
		}

		viewName += VIEW_SUFFIX;

		return RdbUtils.quoteIdentifier(viewName);
	}

	@Override
	public int getArity() {
		return relation.getArity();
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
	public String toString() {
		return viewRelation.toString();
	}

	public ITuple getViewCriteria() {
		return viewCriteria;
	}

	public List<IVariable> getVariables() {
		List<IVariable> variables = viewCriteria.getAllVariables();

		UniqueList<IVariable> uniqueVariables = new UniqueList<IVariable>();
		uniqueVariables.addAll(variables);

		return uniqueVariables;
	}

	@Override
	public void close() {
		viewRelation.close();

		// Do not close the universe, as it may be used somewhere else.
	}

	@Override
	public CloseableIterator<ITuple> iterator() {
		return viewRelation.iterator();
	}

}
