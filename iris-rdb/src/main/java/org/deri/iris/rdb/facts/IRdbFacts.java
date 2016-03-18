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
package org.deri.iris.rdb.facts;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.storage.IRdbRelation;

/**
 * Facts that are stored in a relational database.
 */
public interface IRdbFacts extends IFacts {

	/**
	 * Returns the {@link IRdbRelation} associated with the given predicate and
	 * creates one if it does not already exist.
	 * 
	 * @param predicate
	 *            The predicate identifying the relation.
	 * @return The relation associated with the given predicate.
	 */
	public IRdbRelation get(IPredicate predicate);

	/**
	 * Copies all relations (and the tuples they contain) of the specified
	 * {@link IFacts} to this facts.
	 * 
	 * @param source
	 *            The facts to add to this facts.
	 */
	public void addAll(IFacts source);

	/**
	 * Drops all relations this instance keeps hold of.
	 */
	public void dropAll();

}
