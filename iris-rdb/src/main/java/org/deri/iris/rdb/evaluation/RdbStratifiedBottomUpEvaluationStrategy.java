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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.evaluation.IEvaluationStrategy;
import org.deri.iris.evaluation.stratifiedbottomup.EvaluationUtilities;
import org.deri.iris.evaluation.stratifiedbottomup.IRuleEvaluator;
import org.deri.iris.evaluation.stratifiedbottomup.IRuleEvaluatorFactory;
import org.deri.iris.factory.Factory;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rdb.facts.IRdbFacts;
import org.deri.iris.rdb.facts.RdbFacts;
import org.deri.iris.rdb.rules.compiler.IRdbCompiledRule;
import org.deri.iris.rdb.rules.compiler.RdbRuleCompiler;
import org.deri.iris.rdb.rules.optimization.EmptyRuleBodyOptimizer;
import org.deri.iris.rdb.storage.FixedSizeRelation;
import org.deri.iris.rdb.storage.IRdbRelation;
import org.deri.iris.rdb.storage.RdbProjectedRelation;
import org.deri.iris.rules.IRuleHeadEqualityPreProcessor;
import org.deri.iris.rules.RuleHeadEqualityRewriter;
import org.deri.iris.rules.compiler.ICompiledRule;
import org.deri.iris.storage.IRelation;
import org.deri.iris.utils.UniqueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A strategy that uses bottom up evaluation on a stratified rule set. This
 * evaluation strategy uses the {@link RuleHeadEqualityRewriter} technique for
 * rules with rule head equality.
 */
public class RdbStratifiedBottomUpEvaluationStrategy implements
		IEvaluationStrategy {

	private static final Logger logger = LoggerFactory
			.getLogger(RdbStratifiedBottomUpEvaluationStrategy.class);

	private final Connection connection;

	protected final Configuration configuration;

	protected final IRdbFacts facts;

	protected final IRuleEvaluatorFactory ruleEvaluatorFactory;

	public RdbStratifiedBottomUpEvaluationStrategy(Connection connection,
			IFacts facts, List<IRule> rules,
			IRuleEvaluatorFactory ruleEvaluatorFactory,
			Configuration configuration) throws EvaluationException {
		this.connection = connection;
		this.ruleEvaluatorFactory = ruleEvaluatorFactory;
		this.configuration = configuration;

		if (!(facts instanceof IRdbFacts)) {
			this.facts = new RdbFacts(connection);
			this.facts.addAll(facts);
			facts = this.facts;
		} else {
			this.facts = (IRdbFacts) facts;
		}

		// Execute rule head equality rewriter.
		IRuleHeadEqualityPreProcessor rewriter = new RuleHeadEqualityRewriter();
		rules = rewriter.process(rules, facts);

		// Add the empty rule body optimizer to the list of rule optimizers.
		configuration.ruleOptimisers.add(new EmptyRuleBodyOptimizer());

		EvaluationUtilities utils = new EvaluationUtilities(configuration);

		// Rule safety processing.
		List<IRule> safeRules = utils.applyRuleSafetyProcessor(rules);

		// Stratify the rule base.
		List<List<IRule>> stratifiedRules = utils.stratify(safeRules);

		RdbRuleCompiler compiler = new RdbRuleCompiler(connection, facts);

		int stratumNumber = 0;
		for (List<IRule> stratum : stratifiedRules) {
			// Re-order stratum.
			List<IRule> reorderedRules = utils.reOrderRules(stratum);

			// Rule optimization.
			List<IRule> optimisedRules = utils
					.applyRuleOptimisers(reorderedRules);

			List<ICompiledRule> compiledRules = new ArrayList<ICompiledRule>();

			for (IRule rule : optimisedRules) {
				try {
					compiledRules.add(compiler.compile(rule));
				} catch (SQLException e) {
					throw new EvaluationException(e.getLocalizedMessage());
				}
			}

			IRuleEvaluator evaluator = ruleEvaluatorFactory.createEvaluator();

			evaluator.evaluateRules(compiledRules, facts, configuration);

			stratumNumber++;
		}
	}

	@Override
	public IRelation evaluateQuery(IQuery query, List<IVariable> outputVariables)
			throws EvaluationException {
		if (query == null) {
			throw new IllegalArgumentException("Query must not be null.");
		}

		if (outputVariables == null) {
			throw new IllegalArgumentException(
					"OutputVariables must not be null.");
		}

		RdbRuleCompiler compiler = new RdbRuleCompiler(connection, facts);

		IRdbCompiledRule compiledQuery;

		try {
			compiledQuery = compiler.compile(query);
		} catch (SQLException e) {
			throw new EvaluationException(e.getLocalizedMessage());
		}

		IRdbRelation result = compiledQuery.evaluate();

		// The output tuple of the compiled rule.
		ITuple outputTuple = compiledQuery.getOutputTuple();

		// Create a tuple containing all variables of the output in the correct
		// order. A variable appears only once in the tuple.
		UniqueList<IVariable> variables = new UniqueList<IVariable>();
		variables.addAll(outputTuple.getAllVariables());
		ITuple queryTuple = Factory.BASIC.createTuple(new ArrayList<ITerm>(
				variables));

		outputVariables.clear();
		outputVariables.addAll(variables);

		try {
			// If there are no variables in the query tuple, we return a
			// relation with n empty tuples, where n is the size of the output
			// relation.
			if (outputVariables.isEmpty()) {
				return new FixedSizeRelation(result.size());
			}

			// Return a projection on the output relation, which
			// returns only the values for the variables in the order given by
			// the query tuple.
			return new RdbProjectedRelation(connection, result, queryTuple,
					outputTuple);
		} catch (SQLException e) {
			logger.error("Failed to create a projected relation for the query " + query,
					e);
			throw new EvaluationException(
					"Could not create a selection on the output relation");
		}
	}

}
