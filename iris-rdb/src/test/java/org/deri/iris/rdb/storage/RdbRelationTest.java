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

import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.Test;

public class RdbRelationTest extends AbstractRdbRelationTest {

	@Test
	public void testAdd() {
		Assert.assertTrue(relation1.add(tuple1));
		Assert.assertTrue(relation1.add(tuple2));
		Assert.assertFalse(relation1.add(tuple1));
		Assert.assertFalse(relation1.add(tuple2));
	}

	@Test
	public void testAddAll() {
		relation1.add(tuple1);
		relation1.add(tuple2);
		relation2.add(tuple1);
		relation2.add(tuple3);

		Assert.assertTrue(relation1.addAll(relation2));
		Assert.assertEquals(3, relation1.size());
	}

	@Test
	public void testContains() {
		Assert.assertFalse(relation1.contains(tuple1));

		Assert.assertTrue(relation1.add(tuple1));
		Assert.assertTrue(relation1.add(tuple2));

		Assert.assertTrue(relation1.contains(tuple1));
		Assert.assertTrue(relation1.contains(tuple2));

		Assert.assertFalse(relation1.add(tuple1));
	}

	@Test
	public void testCreateInternalName() throws SQLException {
		RdbRelation otherRelation = new RdbRelation(connection, "$foobar", 2);
		Assert.assertTrue(otherRelation.add(tuple1));
		otherRelation.drop();
	}

	@Test
	public void testCreateUrlName() throws SQLException {
		RdbRelation otherRelation = new RdbRelation(connection,
				"http://foo.com", 2);
		Assert.assertTrue(otherRelation.add(tuple1));
		otherRelation.drop();
	}

}
