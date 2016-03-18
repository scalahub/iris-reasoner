package org.deri.iris.rdb.evaluation.orb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.KnowledgeBase;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.facts.Facts;
import org.deri.iris.facts.IFacts;
import org.deri.iris.optimisations.magicsets.MagicSets;
import org.deri.iris.optimisations.rulefilter.RuleFilter;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.simple.SimpleRelationFactory;

public class IrisLargeJoin extends LargeJoin {

	public static void main(String[] args) throws Exception {
		IrisLargeJoin largeJoinTest = new IrisLargeJoin();

		largeJoinTest.join1(LargeJoin.Join1Query.BF_A,
				LargeJoin.Join1Data.DATA0);
	}

	@Override
	protected IFacts createFacts() {
		return new Facts(new SimpleRelationFactory());
	}

	@Override
	protected IKnowledgeBase createKnowledgeBase(IFacts facts, List<IRule> rules)
			throws EvaluationException {
		Configuration configuration = new Configuration();

		configuration.programOptmimisers.add(new RuleFilter());
		configuration.programOptmimisers.add(new MagicSets());

		Map<IPredicate, IRelation> rawFacts = new HashMap<IPredicate, IRelation>();
		for (IPredicate predicate : facts.getPredicates()) {
			rawFacts.put(predicate, facts.get(predicate));
		}

		return new KnowledgeBase(rawFacts, rules, configuration);
	}

	@Override
	protected void dispose() {
		// Do nothing.
	}

}
