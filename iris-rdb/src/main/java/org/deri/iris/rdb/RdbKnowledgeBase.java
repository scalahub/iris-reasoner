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
package org.deri.iris.rdb;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.ProgramNotStratifiedException;
import org.deri.iris.RuleUnsafeException;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.evaluation.IEvaluationStrategy;
import org.deri.iris.evaluation.stratifiedbottomup.IRuleEvaluatorFactory;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.evaluation.RdbOptimizedProgramStrategyAdaptor;
import org.deri.iris.rdb.evaluation.RdbSemiNaiveEvaluatorFactory;
import org.deri.iris.rdb.evaluation.RdbStratifiedBottomUpEvaluationStrategyFactory;
import org.deri.iris.rdb.facts.IRdbFacts;
import org.deri.iris.rdb.facts.RdbFacts;
import org.deri.iris.rdb.utils.RdbUtils;
import org.deri.iris.rules.RuleManipulator;
import org.deri.iris.rules.safety.AugmentingRuleSafetyProcessor;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A knowledge bases, that allows evaluation of a Datalog program and the
 * execution of queries using an underlying relational database system.
 * </p>
 * <p>
 * Currently, only the H2 database system is supported. However, there may be
 * other database system, which support the non-standard SQL statements of the
 * dialect supported by H2. For this, the class provides a constructor, to which
 * a {@link Connection} object to any kind of database can be passed.
 */
public class RdbKnowledgeBase implements IKnowledgeBase {

	private static final Logger logger = LoggerFactory
			.getLogger(RdbKnowledgeBase.class);

	private Connection connection;

	private IFacts simpleFacts;

	private IRdbFacts facts;

	private List<IRule> rules;

	private Configuration configuration;

	private File tempDirectory;

	private IEvaluationStrategy evaluationStrategy;

	private boolean isLoaded;

	private boolean isEvaluated;

	private boolean isDisposed;

	private boolean useInMemory;

	/**
	 * Creates a {@link RdbKnowledgeBase} for the specified facts, rules and
	 * configuration. This constructor creates a persistent H2 database stored
	 * in the temporary directory of the user executing the Java program.
	 * 
	 * @param facts
	 *            The facts of the Datalog program.
	 * @param rules
	 *            The rules of the Datalog program.
	 * @param configuration
	 *            The configuration for the knowledge base, <code>null</code> if
	 *            the default configuration should be used.
	 * @throws IOException
	 *             If the directory containing the database files can not be
	 *             created.
	 * @throws ClassNotFoundException
	 *             If the driver for the H2 database can not be found.
	 * @throws SQLException
	 *             If an SQL error occurs during initialization or during the
	 *             evaluation of the program.
	 */
	public RdbKnowledgeBase(IFacts facts, List<IRule> rules,
			Configuration configuration) throws EvaluationException,
			IOException, ClassNotFoundException, SQLException {
		this(null, facts, rules, configuration);
	}

	/**
	 * Creates a {@link RdbKnowledgeBase} for the specified facts, rules and
	 * configuration. This constructor creates an in-memory H2 database.
	 * 
	 * @param facts
	 *            The facts of the Datalog program.
	 * @param rules
	 *            The rules of the Datalog program.
	 * @param configuration
	 *            The configuration for the knowledge base, <code>null</code> if
	 *            the default configuration should be used.
	 * @throws IOException
	 *             If the directory containing the database files can not be
	 *             created. This should not happen, since there is no directory
	 *             created for an in-memory database.
	 * @throws ClassNotFoundException
	 *             If the driver for the H2 database can not be found.
	 * @throws SQLException
	 *             If an SQL error occurs during initialization or during the
	 *             evaluation of the program.
	 */
	public RdbKnowledgeBase(IFacts facts, List<IRule> rules,
			Configuration configuration, boolean useInMemory)
			throws EvaluationException, IOException, ClassNotFoundException,
			SQLException {
		this(null, facts, rules, configuration, useInMemory);
	}

	/**
	 * Creates a {@link RdbKnowledgeBase} for the specified facts, rules and
	 * configuration. The knowledge bases uses the database represented by the
	 * specified {@link Connection} object.
	 * 
	 * @param facts
	 *            The facts of the Datalog program.
	 * @param rules
	 *            The rules of the Datalog program.
	 * @param configuration
	 *            The configuration for the knowledge base, <code>null</code> if
	 *            the default configuration should be used.
	 * @throws IOException
	 *             If the directory containing the database files can not be
	 *             created. This should not happen, since no directory is
	 *             created.
	 * @throws ClassNotFoundException
	 *             If the driver for the H2 database can not be found.
	 * @throws SQLException
	 *             If an SQL error occurs during initialization or during the
	 *             evaluation of the program.
	 */
	public RdbKnowledgeBase(Connection connection, IFacts facts,
			List<IRule> rules, Configuration configuration)
			throws EvaluationException, IOException, ClassNotFoundException,
			SQLException {
		this(null, facts, rules, configuration, false);
	}

	// TODO Refactor this constructor.
	private RdbKnowledgeBase(Connection connection, IFacts facts,
			List<IRule> rules, Configuration configuration, boolean useInMemory)
			throws IOException, ClassNotFoundException, SQLException {
		if (connection == null) {
			long startTime = System.currentTimeMillis();

			if (useInMemory) {
				connection = RdbUtils.createConnection();
			} else {
				tempDirectory = RdbUtils.createTempDirectory();
				connection = RdbUtils.createConnection(tempDirectory);
			}

			long stopTime = System.currentTimeMillis();
			double duration = (double) (stopTime - startTime) / 1000.0;

			logger.debug("Creation of database took {} seconds",
					Double.toString(duration));
		}

		this.connection = connection;
		this.simpleFacts = facts;
		this.rules = rules;
		this.useInMemory = useInMemory;

		// Set up the rule-base if it does not exist.
		if (rules == null) {
			this.rules = new ArrayList<IRule>();
		}

		this.configuration = configuration;

		if (configuration == null) {
			this.configuration = new Configuration();
		}
	}

	/**
	 * Loads the facts into the database.
	 * 
	 * @throws EvaluationException
	 *             If this method is called, after the knowledge bases has been
	 *             disposed.
	 */
	public void load() throws EvaluationException {
		if (isDisposed) {
			throw new EvaluationException(
					"Knowledge base has already been disposed");
		}

		if (isLoaded) {
			return;
		}

		if (simpleFacts == null) {
			simpleFacts = new RdbFacts(connection);
		}

		// Set up the facts object.
		if (simpleFacts instanceof IRdbFacts) {
			this.facts = (IRdbFacts) simpleFacts;
		} else {
			long startTime = System.currentTimeMillis();

			this.facts = new RdbFacts(connection);
			this.facts.addAll(simpleFacts);

			long stopTime = System.currentTimeMillis();
			double duration = (double) (stopTime - startTime) / 1000.0;

			logger.debug("Loading facts into database took {} seconds",
					Double.toString(duration));
		}

		isLoaded = true;
	}

	/**
	 * Evaluates the knowledge base by evaluating all the rules against the
	 * facts.
	 * 
	 * @throws EvaluationException
	 *             If an error occurs during the evaluation of the knowledge
	 *             base, or this method is called after the knowledge base has
	 *             been disposed.
	 */
	public void evaluate() throws EvaluationException {
		if (isDisposed) {
			throw new EvaluationException(
					"Knowledge base has already been disposed");
		}

		if (isEvaluated) {
			return;
		}

		// Loads in-memory facts into the database.
		load();

		// Enable augmenting rule safety processor.
		configuration.ruleSafetyProcessor = new AugmentingRuleSafetyProcessor();

		IRuleEvaluatorFactory ruleEvaluatorFactory = new RdbSemiNaiveEvaluatorFactory(
				connection);
		configuration.evaluationStrategyFactory = new RdbStratifiedBottomUpEvaluationStrategyFactory(
				connection, ruleEvaluatorFactory);

		long startTime = System.currentTimeMillis();

		if (configuration.programOptmimisers.size() > 0) {
			evaluationStrategy = new RdbOptimizedProgramStrategyAdaptor(
					connection, facts, rules, configuration);
		} else {
			evaluationStrategy = configuration.evaluationStrategyFactory
					.createEvaluator(facts, rules, configuration);
		}

		long stopTime = System.currentTimeMillis();
		double duration = (double) (stopTime - startTime) / 1000.0;

		logger.debug("Evaluation of rules against facts took {} seconds",
				Double.toString(duration));

		isEvaluated = true;
	}

	@Override
	public IRelation execute(IQuery query)
			throws ProgramNotStratifiedException, RuleUnsafeException,
			EvaluationException {
		return execute(query, null);
	}

	@Override
	public IRelation execute(IQuery query, List<IVariable> variableBindings)
			throws ProgramNotStratifiedException, RuleUnsafeException,
			EvaluationException {
		if (isDisposed) {
			throw new EvaluationException(
					"Knowledge base has already been disposed");
		}

		if (query == null) {
			throw new IllegalArgumentException("Query must not be null");
		}

		// This prevents every strategy having to check for this.
		if (variableBindings == null) {
			variableBindings = new ArrayList<IVariable>();
		}

		// Evaluate the rules against the facts.
		evaluate();

		long startTime = System.currentTimeMillis();

		IRelation result = evaluationStrategy.evaluateQuery(
				RuleManipulator.removeDuplicateLiterals(query),
				variableBindings);

		long stopTime = System.currentTimeMillis();
		double duration = (double) (stopTime - startTime) / 1000.0;

		logger.debug("Evaluating query \"{}\" took {} seconds",
				query.toString(), duration);

		return result;
	}

	@Override
	public List<IRule> getRules() {
		return rules;
	}

	/**
	 * Once disposed, the knowledge base can not be used to execute queries
	 * anymore.
	 */
	public void dispose() {
		if (isDisposed) {
			return;
		}

		// Only close the connection and remove the directory, if the directory
		// was created by this class, i.e. there was no connection passed to the
		// constructor of this class or the database is store in memory.
		if (useInMemory || (tempDirectory != null && tempDirectory.exists())) {
			long startTime = System.currentTimeMillis();

			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("Could not close connection to database", e);
			}

			if (tempDirectory != null) {
				try {

					FileUtils.deleteDirectory(tempDirectory);
				} catch (IOException e) {
					logger.error(
							"Could not delete directory containing database files",
							e);
				}
			}

			long stopTime = System.currentTimeMillis();
			double deletionDuration = (double) (stopTime - startTime) / 1000.0;

			logger.debug("Deletion of database took {} seconds",
					deletionDuration);
		}

		isDisposed = true;
	}

	@Override
	protected void finalize() throws Throwable {
		dispose();

		super.finalize();
	}

}
