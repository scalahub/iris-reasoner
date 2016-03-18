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

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.storage.IRelation;

/**
 * <p>
 * An {@link IRdbRelation} is an extension of {@link IRelation}, which
 * represents a table or view in a relational database. As such, an
 * {@link IRdbRelation} and the underlying table (or view) have fixed number of
 * columns (arity), where each column corresponds to an element of a tuple, also
 * called attribute. The constant {@link IRdbRelation#ATTRIBUTE_PREFIX} is the
 * prefix for the name of these columns.
 * </p>
 * <p>
 * <b>Example:</b> The relation <code>foobar(t<sub>1</sub>, t<sub>2</sub>,
 * t<sub>3</sub>)</code> is mapped to the following table in the database:
 * </p>
 * <p>
 * <table border="1" cellpadding="1" cellspacing="0">
 * <tr>
 * <th colspan="3">foobar</th>
 * </tr>
 * <tr>
 * <td>attr1</td>
 * <td>attr2</td>
 * <td>attr3</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * The table or view may have an ID column, however, this column should not be
 * taken into account when determining the arity of the relation.
 * </p>
 * <p>
 * As a relation may represent a complex view, the {@link CloseableIterator}
 * should be used when iterating over the tuples of the relation, since
 * otherwise the view needs to be computed each time the
 * {@link IRelation#get(int)} method is called. Similarly, the size of a
 * relation should be computed outside a loop, in order to avoid the computation
 * of the size of a complex view each time the loop checks if the termination
 * condition is fulfilled.
 * </p>
 */
public interface IRdbRelation extends IRelation, Iterable<ITuple> {

	public static final String ATTRIBUTE_PREFIX = "attr";

	/**
	 * Returns the arity of this relation, i.e. the number attributes of this
	 * relation.
	 * 
	 * @return Thearity of this relation, i.e. the number attributes of this
	 *         relation.
	 */
	public int getArity();

	/**
	 * Returns the name of the table or view in the database.
	 * 
	 * @return The name of the table or view in the database.
	 */
	public String getTableName();

	/**
	 * Drops the relation and the corresponding table or view in the database.
	 * An {@link IRdbRelation} should be closed before being dropped.
	 */
	public void drop();

	/**
	 * Closes the relation and cleans up all the resources it keeps hold of.
	 */
	public void close();

	@Override
	public CloseableIterator<ITuple> iterator();

}
