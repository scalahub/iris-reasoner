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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IConcreteTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.rdb.utils.RdbUtils;
import org.deri.iris.rdb.utils.TermDenormalizer;
import org.deri.iris.rdb.utils.TermNormalizer;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This relation does not return duplicate tuples.
 */
public class RdbUniverseRelation implements IRelation {

	private static Logger logger = LoggerFactory
			.getLogger(RdbUniverseRelation.class);

	private static boolean USE_BATCH_MODE = false;

	public static final String UNIVERSE_NAME = "\"__universe__\"";

	public static final String MUTEX_NAME = "\"__mutex__\"";

	private static final String INDEX_COMMON_NAME = "__universe_index_on_common__";

	public static final String ID_NAME = "id";

	public static final String COMMON_NAME = "common";

	public static final String CANONICAL_NAME = "canonical";

	public static final String TYPE_NAME = "type";

	private static final Map<Connection, RdbUniverseRelation> universes;

	private PreparedStatement insertStatement;

	private PreparedStatement getTermStatement;

	private PreparedStatement getIdStatement;

	private PreparedStatement sizeStatement;

	private static final TermNormalizer termNormalizer;

	private static final TermDenormalizer termDenormalizer;

	private final Connection connection;

	static {
		universes = new WeakHashMap<Connection, RdbUniverseRelation>();
		termNormalizer = new TermNormalizer();
		termDenormalizer = new TermDenormalizer();
	}

	private RdbUniverseRelation(Connection connection) throws SQLException {
		this.connection = connection;
		createTable();
	}

	@Override
	public boolean add(ITuple tuple) {
		if (tuple.size() != 1) {
			return false;
		}

		return add(tuple.get(0));
	}

	public boolean add(ITerm term) {
		if (!term.isGround() || !(term instanceof IConcreteTerm)) {
			return false;
		}

		IConcreteTerm constant = (IConcreteTerm) term;

		String common = termNormalizer.createString(constant);
		String canonical = constant.toCanonicalString();
		String type = constant.getDatatypeIRI().toString();

		try {
			createInsertStatement();

			insertStatement.setString(1, common);
			insertStatement.setString(2, canonical);
			insertStatement.setString(3, type);

			if (USE_BATCH_MODE) {
				insertStatement.setString(4, canonical);
				insertStatement.setString(5, type);
			}

			logger.debug("Executing " + insertStatement);
			return insertStatement.executeUpdate() > 0;
		} catch (SQLException e) {
			// If the INSERT fails due to a unique index violation, we can
			// ignore the exception.
			if (e.getErrorCode() != 23001) {
				logger.error(
						"Failed to execute query (error code: "
								+ e.getErrorCode() + ")", e);
			}
		}

		return false;
	}

	private void createInsertStatement() throws SQLException {
		if (insertStatement == null) {
			String querySql;

			if (USE_BATCH_MODE) {
				String queryFormat = "INSERT INTO %s(%s, %s, %s) "
						+ "SELECT ?, ?, ? FROM %s "
						+ "LEFT OUTER JOIN %s ON %s = ? AND %s = ? "
						+ "WHERE %s.i = 1 AND %s IS NULL AND %s IS NULL "
						+ "GROUP BY %s AND %s";

				String canonicalAttribute = UNIVERSE_NAME + "." + CANONICAL_NAME;
				String typeAttribute = UNIVERSE_NAME + "." + TYPE_NAME;

				querySql = String.format(queryFormat, UNIVERSE_NAME,
						COMMON_NAME, CANONICAL_NAME, TYPE_NAME, MUTEX_NAME,
						UNIVERSE_NAME, canonicalAttribute, typeAttribute,
						MUTEX_NAME, canonicalAttribute, typeAttribute,
						canonicalAttribute, typeAttribute);
			} else {
				String queryFormat = "INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)";
				querySql = String.format(queryFormat, UNIVERSE_NAME,
						COMMON_NAME, CANONICAL_NAME, TYPE_NAME);
			}

			insertStatement = connection.prepareStatement(querySql);
		}
	}

	public int getId(ITerm term) {
		if (!(term instanceof IConcreteTerm)) {
			return -1;
		}

		IConcreteTerm constant = (IConcreteTerm) term;

		String canonical = constant.toCanonicalString();
		String type = constant.getDatatypeIRI().toString();

		ResultSet resultSet = null;

		try {
			createGetIdStatement();

			getIdStatement.setString(1, canonical);
			getIdStatement.setString(2, type);

			resultSet = getIdStatement.executeQuery();

			if (resultSet.next()) {
				int termId = resultSet.getInt(1);
				return termId;
			}
		} catch (SQLException e) {
			logger.error("Failed to execute query " + getIdStatement, e);
		} finally {
			RdbUtils.closeResultSet(resultSet);
		}

		return -1;
	}

	private void createGetIdStatement() throws SQLException {
		if (getIdStatement == null) {
			String sqlFormat = "SELECT %s FROM %s WHERE %s = ? AND %s = ?";
			String sql = String.format(sqlFormat, ID_NAME, UNIVERSE_NAME,
					CANONICAL_NAME, TYPE_NAME);

			getIdStatement = connection.prepareStatement(sql);
		}
	}

	@Override
	public boolean addAll(IRelation relation) {
		boolean addedAll = false;

		int size = relation.size();
		for (int i = 0; i < size; i++) {
			ITuple tuple = relation.get(i);
			addedAll |= add(tuple);
		}

		return addedAll;
	}

	@Override
	public boolean contains(ITuple tuple) {
		if (tuple.size() != 1) {
			return false;
		}

		ITerm term = tuple.get(0);
		return getId(term) != -1;
	}

	@Override
	public ITuple get(int index) {
		ITerm term = getTerm(index);

		if (term != null) {
			return Factory.BASIC.createTuple(term);
		}

		return null;
	}

	public ITerm getTerm(int id) {
		ResultSet resultSet = null;

		try {
			createGetTermStatement();

			getTermStatement.setInt(1, id);

			logger.debug("Executing " + getTermStatement);
			resultSet = getTermStatement.executeQuery();

			if (resultSet.next()) {
				String canonical = resultSet.getString("canonical");
				String type = resultSet.getString("type");

				if (canonical != null && type != null) {
					return termDenormalizer.createTerm(canonical, type);
				}
			}
		} catch (SQLException e) {
			logger.error("Failed to execute query", e);
		} finally {
			RdbUtils.closeResultSet(resultSet);
		}

		return null;
	}

	private void createGetTermStatement() throws SQLException {
		if (getTermStatement == null) {
			String queryFormat = "SELECT %s, %s FROM %s WHERE %s = ?";
			String query = String.format(queryFormat, CANONICAL_NAME, TYPE_NAME,
					UNIVERSE_NAME, ID_NAME);

			getTermStatement = connection.prepareStatement(query);
		}
	}

	@Override
	public int size() {
		ResultSet resultSet = null;

		try {
			createSizeStatement();

			logger.debug("Executing " + sizeStatement);
			resultSet = sizeStatement.executeQuery();

			if (resultSet.next()) {
				return resultSet.getInt("size");
			}
		} catch (SQLException e) {
			logger.error("Failed to execute query", e);
		} finally {
			RdbUtils.closeResultSet(resultSet);
		}

		return 0;
	}

	private void createSizeStatement() throws SQLException {
		if (sizeStatement == null) {
			String queryFormat = "SELECT COUNT(*) AS size FROM %s";
			String query = String.format(queryFormat, UNIVERSE_NAME);

			sizeStatement = connection.prepareStatement(query);
		}
	}

	private void createTable() throws SQLException {
		// Create the table if it does not exist yet.
		String createTableFormat = "CREATE TABLE IF NOT EXISTS %s"
				+ "(%s IDENTITY PRIMARY KEY, %s %s, %s %s, %s %s, UNIQUE(%s, %s))";
		String stringType = "VARCHAR(" + Integer.MAX_VALUE + ")";

		String createTableSql = String.format(createTableFormat, UNIVERSE_NAME,
				ID_NAME, COMMON_NAME, stringType, CANONICAL_NAME,
				stringType, TYPE_NAME, stringType, CANONICAL_NAME, TYPE_NAME);

		String createIndexOnCommonFormat = "CREATE INDEX IF NOT EXISTS %s ON %s(%s)";
		String createIndexOnCommonSql = String.format(
				createIndexOnCommonFormat, INDEX_COMMON_NAME,
				UNIVERSE_NAME, COMMON_NAME);

		CallableStatement call = connection.prepareCall(createTableSql);
		call.execute();
		call.close();

		call = connection.prepareCall(createIndexOnCommonSql);
		call.execute();
		call.close();

		String createMutexFormat = "CREATE TABLE %s(i INT NOT NULL PRIMARY KEY)";
		String createMutexSql = String.format(createMutexFormat, MUTEX_NAME);

		call = connection.prepareCall(createMutexSql);
		call.execute();
		call.close();

		String insertMutexFormat = "INSERT INTO %s(i) VALUES(1)";
		String insertMutexSql = String.format(insertMutexFormat, MUTEX_NAME);

		call = connection.prepareCall(insertMutexSql);
		call.execute();
		call.close();
	}

	public String getTableName() {
		return UNIVERSE_NAME;
	}

	public void close() {
		RdbUtils.closeStatement(getIdStatement);
		getIdStatement = null;

		RdbUtils.closeStatement(getTermStatement);
		getTermStatement = null;

		RdbUtils.closeStatement(insertStatement);
		insertStatement = null;

		RdbUtils.closeStatement(sizeStatement);
		sizeStatement = null;
	}

	public static RdbUniverseRelation getInstance(Connection connection)
			throws SQLException {
		RdbUniverseRelation universe = universes.get(connection);

		if (universe == null) {
			universe = new RdbUniverseRelation(connection);
			universes.put(connection, universe);
		}

		return universe;
	}

}
