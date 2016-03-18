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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.rdb.utils.RdbUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractRdbRelationTest {

	protected static IPredicate predicate1;

	protected static IPredicate predicate2;

	protected static ITuple tuple1;

	protected static ITuple tuple2;

	protected static ITuple tuple3;

	protected static ITuple tuple4;

	protected static ITerm stringTerm;

	protected static ITerm intTerm;

	protected RdbRelation relation1;

	protected RdbRelation relation2;

	protected Connection connection;

	protected File tempDirectory;

	@Before
	public void setUp() throws IOException, ClassNotFoundException,
			SQLException {
		tempDirectory = RdbUtils.createTempDirectory();
		connection = RdbUtils.createConnection(tempDirectory);
		relation1 = new RdbRelation(connection, predicate1);
		relation2 = new RdbRelation(connection, predicate2);
	}

	@After
	public void shutDown() throws SQLException, IOException {
		relation1.drop();
		relation2.drop();
		connection.close();
		FileUtils.deleteDirectory(tempDirectory);
	}

	@BeforeClass
	public static void beforeClass() {
		predicate1 = Factory.BASIC.createPredicate("A", 2);
		predicate2 = Factory.BASIC.createPredicate("B", 2);

		stringTerm = Factory.TERM.createString("string");
		intTerm = Factory.CONCRETE.createInt(1337);

		tuple1 = Factory.BASIC.createTuple(stringTerm, intTerm);
		tuple2 = Factory.BASIC.createTuple(intTerm, stringTerm);
		tuple3 = Factory.BASIC.createTuple(stringTerm, stringTerm);
		tuple4 = Factory.BASIC.createTuple(intTerm, intTerm);
	}

}
