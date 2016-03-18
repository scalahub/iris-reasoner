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
import java.util.Map;
import java.util.WeakHashMap;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.rdb.utils.RdbUtils;
import org.deri.iris.storage.IRelation;

/**
 * This relation does not return duplicate tuples.
 */
public class RdbUniverseView extends AbstractRdbRelation {

	private static final String VIEW_SUFFIX = "_view";

	private static Map<Connection, RdbUniverseView> universeViews;

	private RdbUniverseRelation universe;

	private IRdbRelation viewRelation;

	static {
		universeViews = new WeakHashMap<Connection, RdbUniverseView>();
	}

	private RdbUniverseView(Connection connection) throws SQLException {
		super(connection);

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
		viewRelation.drop();
	}

	private void createView() throws SQLException {
		// Create the view if it does not exist yet.
		String sqlFormat = "CREATE VIEW IF NOT EXISTS %s AS "
				+ "SELECT %s AS %s FROM %s";

		String createViewSql = String.format(sqlFormat, getTableName(),
				RdbUniverseRelation.ID_NAME, IRdbRelation.ATTRIBUTE_PREFIX + 1,
				RdbUniverseRelation.UNIVERSE_NAME);

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

	@Override
	public String getTableName() {
		String parentTableName = RdbUtils.unquoteIdentifier(universe
				.getTableName());
		String viewName = parentTableName + VIEW_SUFFIX;

		return RdbUtils.quoteIdentifier(viewName);
	}

	@Override
	public int getArity() {
		return 1;
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

	public static RdbUniverseView getInstance(Connection connection)
			throws SQLException {
		RdbUniverseView universeView = universeViews.get(connection);

		if (universeView == null) {
			universeView = new RdbUniverseView(connection);
			universeViews.put(connection, universeView);
		}

		return universeView;
	}

	@Override
	public CloseableIterator<ITuple> iterator() {
		return viewRelation.iterator();
	}

}
