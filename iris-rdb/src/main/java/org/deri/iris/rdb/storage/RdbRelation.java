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

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.rdb.utils.RdbUtils;
import org.deri.iris.storage.IRelation;

/**
 * This relation does not return duplicate tuples.
 */
public class RdbRelation extends AbstractRdbRelation {

	private static boolean USE_INDEX_ON_EACH_COLUMN = false;

	public static final String ID_NAME = "id";

	private IRdbRelation viewRelation;

	private String tableName;

	private int arity;

	public RdbRelation(Connection connection, IPredicate predicate)
			throws SQLException {
		this(connection, predicate, "");
	}

	public RdbRelation(Connection connection, IPredicate predicate,
			String prefix) throws SQLException {
		this(connection, prefix + predicate.getPredicateSymbol(), predicate
				.getArity());
	}

	public RdbRelation(Connection connection, String tableName, int arity)
			throws SQLException {
		super(connection);

		this.tableName = tableName;
		this.arity = arity;

		createModel();

		this.viewRelation = new SimpleRdbRelation(connection, getTableName(),
				getArity());
	}

	@Override
	public boolean add(ITuple tuple) {
		return viewRelation.add(tuple);
	}

	@Override
	public boolean addAll(IRelation relation) {
		return this.viewRelation.addAll(relation);
	}

	@Override
	public boolean contains(ITuple tuple) {
		return viewRelation.contains(tuple);
	}

	@Override
	public ITuple get(int index) {
		return viewRelation.get(index);
	}

	@Override
	public int getArity() {
		return arity;
	}

	@Override
	public int size() {
		return viewRelation.size();
	}

	public String getTableName() {
		return RdbUtils.quoteIdentifier(tableName);
	}

	public String getIndexName() {
		return RdbUtils.quoteIdentifier(tableName + "_uniqueIndex");
	}

	private void createModel() throws SQLException {
		StringBuilder attributesBuilder = new StringBuilder();
		StringBuilder unqiueBuilder = new StringBuilder();

		for (int i = 1; i <= getArity(); i++) {
			if (i > 1) {
				attributesBuilder.append(", ");
				unqiueBuilder.append(", ");
			}

			String attributeName = IRdbRelation.ATTRIBUTE_PREFIX + i;

			// We only store the ID of the term in the universe relation.
			attributesBuilder.append("attr" + i);
			attributesBuilder.append(" INT");

			unqiueBuilder.append(attributeName);
		}

		// Create the table if it does not exist yet.
		String createTableFormat = "CREATE TABLE IF NOT EXISTS %s"
				+ "(%s IDENTITY PRIMARY KEY %s)";

		String additionalAttributes = "";
		if (attributesBuilder.length() > 0) {
			additionalAttributes = ", " + attributesBuilder.toString();
		}

		String createTableSql = String.format(createTableFormat,
				getTableName(), ID_NAME, additionalAttributes);

		Connection connection = getConnection();
		CallableStatement statement = null;

		try {
			statement = connection.prepareCall(createTableSql);

			logger.debug("Executing " + statement);
			statement.execute();
		} finally {
			RdbUtils.closeStatement(statement);
		}

		if (unqiueBuilder.length() > 0) {
			String createUniqueIndexFormat = "CREATE UNIQUE INDEX IF NOT EXISTS %s ON %s(%s)";
			String createUniqueIndexSql = String.format(
					createUniqueIndexFormat, getIndexName(), getTableName(),
					unqiueBuilder.toString());

			try {
				statement = connection.prepareCall(createUniqueIndexSql);

				logger.debug("Executing " + statement);
				statement.execute();
			} finally {
				RdbUtils.closeStatement(statement);
			}
		}

		if (USE_INDEX_ON_EACH_COLUMN) {
			for (int i = 1; i <= getArity(); i++) {
				String indexName = RdbUtils.quoteIdentifier(tableName
						+ "_index_on_attr" + i);
				String createIndexFormat = "CREATE INDEX IF NOT EXISTS %s ON %s(%s)";
				String createIndexSql = String.format(createIndexFormat,
						indexName, getTableName(), "attr" + i);

				try {
					statement = connection.prepareCall(createIndexSql);

					logger.debug("Executing " + statement);
					statement.execute();
				} finally {
					RdbUtils.closeStatement(statement);
				}
			}
		}
	}

	@Override
	public void drop() {
		close();

		// Drop the view relation.
		viewRelation.drop();

		// Drops the table and all depending views, if it exists.
		String sqlFormat = "DROP TABLE IF EXISTS %s CASCADE";
		String sql = String.format(sqlFormat, getTableName());

		Connection connection = getConnection();
		CallableStatement statement = null;

		try {
			statement = connection.prepareCall(sql);

			logger.debug("Executing " + statement);
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.error("Failed to drop table " + getTableName(), e);
		} finally {
			RdbUtils.closeStatement(statement);
		}

		// Drop the index if it exists.
		sqlFormat = "DROP INDEX IF EXISTS %s";
		sql = String.format(sqlFormat, getIndexName());

		try {
			statement = connection.prepareCall(sql);

			logger.debug("Executing " + statement);
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.error("Failed to drop index " + getIndexName(), e);
		} finally {
			RdbUtils.closeStatement(statement);
		}

		if (USE_INDEX_ON_EACH_COLUMN) {
			for (int i = 1; i <= getArity(); i++) {
				String indexName = RdbUtils.quoteIdentifier(tableName
						+ "_index_on_attr" + i);
				sqlFormat = "DROP INDEX IF EXISTS %s";
				sql = String.format(sqlFormat, indexName);

				try {
					statement = connection.prepareCall(sql);

					logger.debug("Executing " + statement);
					statement.execute();
				} catch (SQLException e) {
					logger.error("Failed to drop index " + indexName, e);
				} finally {
					RdbUtils.closeStatement(statement);
				}
			}
		}
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
