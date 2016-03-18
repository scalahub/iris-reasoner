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
package org.deri.iris.rdb.evaluation;

import java.sql.Connection;
import java.util.List;

import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.IProgramOptimisation;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.evaluation.IEvaluationStrategy;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.facts.IRdbFacts;
import org.deri.iris.rdb.facts.RdbFacts;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * An evaluation strategy adaptor that uses the program optimization techniques
 * specified in the configuration.
 * </p>
 * <p>
 * This adaptor applies the optimizations each time a query is executed. This
 * requires that the original facts are copied and a new evaluation strategy to
 * be created for each query.
 * </p>
 */
public class RdbOptimizedProgramStrategyAdaptor implements IEvaluationStrategy {

	private static final Logger logger = LoggerFactory
			.getLogger(RdbOptimizedProgramStrategyAdaptor.class);

	/** The original facts. */
	private final IFacts facts;

	/** The original rules. */
	private final List<IRule> rules;

	/** The knowledge-base configuration. */
	private final Configuration configuration;

	/** This flag is set if no optimizations can be applied. */
	private boolean isMinimalModelComputed = false;

	/** The last 'real' evaluation strategy used to answer a query. */
	private IEvaluationStrategy strategy;

	/** The connection to the database. */
	private Connection connection;

	/**
	 * Creates a {@link RdbOptimizedProgramStrategyAdaptor} for the specified
	 * facts, rules and configuration. The database connection is used to copy
	 * the original facts, therefore, the facts must reside in the database
	 * represented by the connection.
	 * 
	 * @param connection
	 *            The connection to the database.
	 * @param facts
	 *            The original program's facts (which will not get modified).
	 * @param rules
	 *            The original program's rules.
	 * @param configuration
	 *            The knowledge-base configuration object.
	 */
	public RdbOptimizedProgramStrategyAdaptor(Connection connection,
			IRdbFacts facts, List<IRule> rules, Configuration configuration) {
		this.connection = connection;
		this.facts = facts;
		this.rules = rules;
		this.configuration = configuration;
	}

	// This implementation remembers if it failed to optimize anything and then
	// will never try to optimize again.
	@Override
	public IRelation evaluateQuery(IQuery query, List<IVariable> outputVariables)
			throws EvaluationException {
		if (isMinimalModelComputed) {
			return strategy.evaluateQuery(query, outputVariables);
		} else {
			List<IRule> rules = this.rules;
			boolean optimised = false;

			for (IProgramOptimisation optimisation : configuration.programOptmimisers) {
				IProgramOptimisation.Result result = optimisation.optimise(
						rules, query);

				// If the optimization succeeded then replace the rules and
				// query with the optimized version.
				if (result != null) {
					query = result.query;
					rules = result.rules;
					optimised = true;
				}
			}

			IRelation result;
			
			long start = 0;

			if (optimised) {
				// Copy the facts to a new fact base.
				IRdbFacts newFacts = new RdbFacts(connection, "opt"
						+ hashCode());
				newFacts.addAll(facts);
				
				start = System.currentTimeMillis();

				strategy = configuration.evaluationStrategyFactory
						.createEvaluator(newFacts, rules, configuration);

				result = strategy.evaluateQuery(query, outputVariables);
			} else {
				// Couldn't optimize at all, so the entire minimal model must be
				// calculated.
				strategy = configuration.evaluationStrategyFactory
						.createEvaluator(facts, this.rules, configuration);

				isMinimalModelComputed = true;

				start = System.currentTimeMillis();
				
				result = strategy.evaluateQuery(query, outputVariables);
			}

			if (logger.isDebugEnabled()) {
				long end = System.currentTimeMillis();
				double duration = (end - start) / 1000.0;
				
				logger.debug("Evaluating optimized query took {} seconds",
						duration);
			}

			return result;
		}
	}

}
