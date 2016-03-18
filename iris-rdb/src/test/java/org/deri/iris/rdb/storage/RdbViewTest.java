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

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RdbViewTest extends AbstractRdbRelationTest {

	private static IVariable variable;

	private static ITuple viewCriteria;

	private RdbView view;

	@Before
	public void setUp() throws IOException, ClassNotFoundException,
			SQLException {
		super.setUp();

		view = new RdbView(connection, relation1, viewCriteria);
	}

	@After
	public void shutDown() throws SQLException, IOException {
		view.drop();

		super.shutDown();
	}

	@BeforeClass
	public static void beforeClass() {
		AbstractRdbRelationTest.beforeClass();

		variable = Factory.TERM.createVariable("X");

		viewCriteria = Factory.BASIC.createTuple(stringTerm, variable);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAdd() throws SQLException {
		view.add(tuple1);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAddAll() throws SQLException {
		view.addAll(relation1);
	}

	@Test
	public void testSizeAndContains() {
		relation1.add(tuple1);
		relation1.add(tuple2);
		relation1.add(tuple3);

		Assert.assertEquals(2, view.size());
		Assert.assertTrue(view.contains(tuple1));
		Assert.assertTrue(view.contains(tuple3));
	}

	@Test
	public void testGet() {
		relation1.add(tuple1);

		Assert.assertEquals(1, view.size());
		Assert.assertEquals(tuple1, view.get(0));
	}
	
	@Test
	public void testGetArity() {
		relation1.add(tuple1);

		Assert.assertEquals(2, view.getArity());
	}

}
