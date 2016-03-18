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

import java.io.IOException;
import java.sql.SQLException;

import junit.framework.Assert;

import org.deri.iris.factory.Factory;
import org.junit.After;
import org.junit.Test;

public class RdbEmptyTupleRelationTest extends AbstractRdbRelationTest {

	private RdbEmptyTupleRelation emptyTupleRelation;

	@After
	public void shutDown() throws SQLException, IOException {
		if (emptyTupleRelation != null) {
			emptyTupleRelation.drop();
		}

		super.shutDown();
	}

	@Test
	public void testAdd() throws SQLException {
		emptyTupleRelation = new RdbEmptyTupleRelation(connection, 5);

		Assert.assertTrue(emptyTupleRelation.add(Factory.BASIC.createTuple()));
		Assert.assertEquals(6, emptyTupleRelation.size());
	}

}
