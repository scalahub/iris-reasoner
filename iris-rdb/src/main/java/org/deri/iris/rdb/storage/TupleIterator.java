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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.rdb.utils.RdbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link CloseableIterator} that iterates over all tuples in a relation. The
 * iterator creates a {@link ResultSet} over all the rows in the table
 * represented by a {@link IRdbRelation} and successively resolves the tuples of
 * terms using the {@link RdbUniverseRelation}.
 */
public class TupleIterator implements CloseableIterator<ITuple> {

	private static Logger logger = LoggerFactory.getLogger(TupleIterator.class);

	private Connection connection;

	private ResultSet resultSet;

	private boolean isClosed;

	private final IRdbRelation relation;

	private ITuple next;

	/**
	 * Creates a new {@link TupleIterator} for the specified relation, which is
	 * stored in the database represented by the specified connection.
	 * 
	 * @param connection
	 *            The connection to the database.
	 * @param relation
	 *            The relation over which should be iterated.
	 * @throws SQLException
	 *             If the tuples of the relation can not be retrieved.
	 */
	public TupleIterator(Connection connection, IRdbRelation relation)
			throws SQLException {
		this.connection = connection;
		this.relation = relation;

		String attributes;

		if (relation.getArity() > 0) {
			List<String> attributeList = RdbUtils.createAttributeList(relation);
			attributes = RdbUtils.join(attributeList, ", ");
		} else {
			attributes = "*";
		}

		// Retrieve all rows of the relation, where each value is the ID of a
		// term in the universe relation.
		String sqlFormat = "SELECT %s FROM %s";
		String sql = String.format(sqlFormat, attributes,
				relation.getTableName());

		CallableStatement statement = connection.prepareCall(sql);
		resultSet = statement.executeQuery();
	}

	@Override
	public boolean hasNext() {
		if (isClosed) {
			return false;
		}

		prepareNext();

		return next != null;
	}

	@Override
	public ITuple next() {
		if (isClosed) {
			throw new NoSuchElementException("Iterator has already been closed");
		}

		if (next == null) {
			prepareNext();
		}

		ITuple result = next;
		next = null;

		if (!hasNext()) {
			close();
		}

		if (result == null) {
			throw new NoSuchElementException();
		}

		return result;
	}

	private void prepareNext() {
		try {
			if (resultSet.next()) {
				List<ITerm> terms = new ArrayList<ITerm>();

				for (int i = 1; i <= relation.getArity(); i++) {
					try {
						// The values of the row should be IDs of terms stored
						// in the universe relation.
						int id = resultSet.getInt(i);

						ITerm term = RdbUniverseRelation
								.getInstance(connection).getTerm(id);

						if (term != null) {
							terms.add(term);
						}
					} catch (SQLException e) {
						logger.error("Failed to retrive term ID", e);
						return;
					}
				}

				next = Factory.BASIC.createTuple(terms);
			}
		} catch (SQLException e) {
			logger.error("Failed to retrieve nex row", e);
		}
	}

	@Override
	public void remove() {
		// Removing is not supported, as no tuples can be removed from a
		// relation.
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				logger.error("Failed to close the result set", e);
			}
		}

		isClosed = true;
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
