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
import org.deri.iris.api.terms.IConcreteTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.rdb.utils.RdbUtils;
import org.deri.iris.storage.IRelation;

/**
 * This relation may return duplicate tuples.
 */
public class RdbProjectedRelation extends AbstractRdbRelation {

	private static final String VIEW_SUFFIX = "_projected";

	private IRdbRelation relation;

	private IRdbRelation viewRelation;

	private ITuple viewCriteria;

	private RdbUniverseRelation universe;

	private ITuple inputTuple;

	private String suffix;

	public RdbProjectedRelation(Connection connection, IRdbRelation relation,
			ITuple viewCriteria, ITuple inputTuple) throws SQLException {
		this(connection, relation, viewCriteria, inputTuple, null);
	}

	public RdbProjectedRelation(Connection connection, IRdbRelation relation,
			ITuple viewCriteria, ITuple inputTuple, String suffix)
			throws SQLException {
		super(connection);

		this.relation = relation;
		this.viewCriteria = viewCriteria;
		this.inputTuple = inputTuple;
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
		// A projected relation should only return distinct values, as it is
		// meant to be the "final" relation of a query or rule.
		String createViewFormat = "CREATE OR REPLACE VIEW %s(%s) AS "
				+ "SELECT DISTINCT %s FROM %s";

		List<String> attributeList = new ArrayList<String>();
		for (int i = 1; i <= viewCriteria.size(); i++) {
			attributeList.add(IRdbRelation.ATTRIBUTE_PREFIX + i);
		}
		String attributes = RdbUtils.join(attributeList, ", ");

		String selectAttributes = createSelectAttributes();

		String createViewSql = String.format(createViewFormat, getTableName(),
				attributes, selectAttributes, relation.getTableName());

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

	private String createSelectAttributes() throws SQLException {
		List<String> attributes = new ArrayList<String>();

		int i = 1;
		for (ITerm term : viewCriteria) {
			String attributeName = IRdbRelation.ATTRIBUTE_PREFIX + i;

			if (term instanceof IConcreteTerm) {
				universe.add(term);
				int termId = universe.getId(term);

				if (termId == -1) {
					throw new SQLException("Could not add term " + term
							+ " to universe");
				}

				attributes.add(termId + " AS " + attributeName);
			} else {
				int index = inputTuple.indexOf(term);

				if (index > -1) {
					String thatAttribute = IRdbRelation.ATTRIBUTE_PREFIX
							+ (index + 1);
					attributes.add(thatAttribute + " AS " + attributeName);
				}
			}

			i++;
		}

		String selectAttributes = RdbUtils.join(attributes, ", ");
		return selectAttributes;
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
		return viewCriteria.size();
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
