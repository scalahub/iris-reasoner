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
package org.deri.iris.rdb.utils;

import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.rdb.storage.CloseableIterator;
import org.deri.iris.rdb.storage.IRdbRelation;
import org.deri.iris.storage.IRelation;
import org.deri.iris.utils.UniqueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdbUtils {

	private static final Logger logger = LoggerFactory
			.getLogger(RdbUtils.class);

	public static List<String> createAttributeList(IRdbRelation relation) {
		List<String> attributeList = new ArrayList<String>();

		for (int i = 1; i <= relation.getArity(); i++) {
			attributeList.add(IRdbRelation.ATTRIBUTE_PREFIX + i);
		}

		return attributeList;
	}

	public static void analyze(Connection connection) {
		String sql = "ANALYZE";

		CallableStatement statement = null;

		try {
			statement = connection.prepareCall(sql);

			logger.debug("Executing " + statement);
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.error("Failed to analyze database", e);
		} finally {
			RdbUtils.closeStatement(statement);
		}
	}

	public static void closeResultSet(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				logger.error("Failed to close result set", e);
			}
		}
	}

	public static void closeStatement(PreparedStatement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				logger.error("Failed to close prepared statement", e);
			}
		}
	}

	public static void closeStatement(CallableStatement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				logger.error("Failed to close callable statement", e);
			}
		}
	}

	public static File createTempDirectory() throws IOException {
		File temp = File.createTempFile("temp",
				Long.toString(System.nanoTime()));

		if (!(temp.delete())) {
			throw new IOException("Could not delete temp file: "
					+ temp.getAbsolutePath());
		}

		if (!(temp.mkdir())) {
			throw new IOException("Could not create temp directory: "
					+ temp.getAbsolutePath());
		}

		return temp;
	}

	public static Connection createConnection(File directory)
			throws IOException, ClassNotFoundException, SQLException {
		String url = "jdbc:h2:file:" + directory.getAbsolutePath()
				+ "/iris-rdb;";
		Class.forName("org.h2.Driver");

		return DriverManager.getConnection(url);
	}

	public static Connection createConnection() throws IOException,
			ClassNotFoundException, SQLException {
		String url = "jdbc:h2:mem:";
		Class.forName("org.h2.Driver");

		return DriverManager.getConnection(url);
	}

	public static String quoteIdentifier(String identifier) {
		String trimmed = identifier.trim();

		if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
			return trimmed;
		}

		return "\"" + trimmed + "\"";
	}

	public static String unquoteIdentifier(String identifier) {
		String trimmed = identifier.trim();

		if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
			return trimmed.substring(1, trimmed.length() - 1);
		}

		return trimmed;
	}

	public static String join(List<String> strings, String delimiter) {
		StringBuilder builder = new StringBuilder();

		int i = 0;
		for (String string : strings) {
			if (i++ > 0) {
				builder.append(delimiter);
			}

			builder.append(string);
		}

		return builder.toString();
	}

	public static String toString(IRelation relation) {
		if (relation instanceof IRdbRelation) {
			// Uses the iterator to iterate over the tuples in the relation.
			return toString((IRdbRelation) relation);
		}

		StringBuilder builder = new StringBuilder();

		builder.append("[");

		int size = relation.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				builder.append(", ");
			}

			ITuple tuple = relation.get(i);

			if (tuple != null) {
				builder.append(tuple.toString());
			}
		}

		builder.append("]");

		return builder.toString();
	}

	public static String toString(IRdbRelation relation) {
		StringBuilder builder = new StringBuilder();

		builder.append("[");

		CloseableIterator<ITuple> iterator = relation.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			if (i++ > 0) {
				builder.append(", ");
			}

			ITuple tuple = iterator.next();

			if (tuple != null) {
				builder.append(tuple.toString());
			}
		}

		iterator.close();

		builder.append("]");

		return builder.toString();
	}

	public static UniqueList<IVariable> uniqueVariables(ITuple tuple) {
		UniqueList<IVariable> variables = new UniqueList<IVariable>();
		variables.addAll(tuple.getAllVariables());

		return variables;
	}

}
