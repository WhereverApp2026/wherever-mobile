package com.wherever.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/** Reglas propias de Wherever para detekt. */
class WhereverRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "wherever"

    override fun instance(config: Config): RuleSet = RuleSet(ruleSetId, listOf(HardcodedDesignValue(config)))
}
