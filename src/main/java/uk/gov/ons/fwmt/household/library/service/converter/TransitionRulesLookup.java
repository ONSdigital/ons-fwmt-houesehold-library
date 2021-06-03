package uk.gov.ons.fwmt.household.library.service.converter;

import java.util.HashMap;
import java.util.Map;

public class TransitionRulesLookup {

  private final Map<String, uk.gov.ons.fwmt.household.library.service.converter.TransitionRule> transitionRulesMap = new HashMap<>();

  public uk.gov.ons.fwmt.household.library.service.converter.TransitionRule getLookup(String cacheType, String rmRequest, String recordAge) {
    String requiredLookup = cacheType + "|" + rmRequest + "|" + recordAge;
    return transitionRulesMap.get(requiredLookup);
  }

  public void add (String transitionRuleSelector, uk.gov.ons.fwmt.household.library.service.converter.TransitionRule transitionRule) {
    transitionRulesMap.put(transitionRuleSelector, transitionRule);
  }
}
