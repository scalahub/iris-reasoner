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

import java.sql.Connection;
import java.util.NoSuchElementException;

import junit.framework.Assert;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.rdb.utils.RdbUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TupleIteratorTest {

	private static IRdbRelation relation;

	private static IRdbRelation emptyRelation;

	private static Connection connection;

	private static ITuple tuple;

	private TupleIterator iterator;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		connection = RdbUtils.createConnection();
		relation = new RdbRelation(connection, Factory.BASIC.createPredicate(
				"test", 2));

		ITerm term1 = Factory.TERM.createString("foo");
		ITerm term2 = Factory.TERM.createString("bar");
		tuple = Factory.BASIC.createTuple(term1, term2);

		relation.add(tuple);

		emptyRelation = new RdbTempRelation(connection, 10);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		relation.drop();
		emptyRelation.drop();
		connection.close();
	}

	@Before
	public void setUp() throws Exception {
		iterator = new TupleIterator(connection, relation);
	}

	@After
	public void tearDown() throws Exception {
		iterator.close();
	}

	@Test
	public void testNext() {
		ITuple tuple = iterator.next();

		Assert.assertNotNull(tuple);
		Assert.assertEquals(2, tuple.size());
		Assert.assertEquals(TupleIteratorTest.tuple, tuple);
		Assert.assertFalse(iterator.hasNext());
	}
	
	@Test(expected=NoSuchElementException.class)
	public void testIllegalNext() {
		iterator.next();
		iterator.next();
	}

	@Test
	public void testEmptyRelation() {
		CloseableIterator<ITuple> iterator = emptyRelation.iterator();

		Assert.assertFalse(iterator.hasNext());
	}

	@Test(expected = NoSuchElementException.class)
	public void testNextOnEmptyRelation() {
		CloseableIterator<ITuple> iterator = emptyRelation.iterator();

		iterator.next();
	}

}
