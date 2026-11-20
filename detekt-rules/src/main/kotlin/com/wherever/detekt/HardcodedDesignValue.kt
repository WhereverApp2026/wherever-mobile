package com.wherever.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

/**
 * Prohíbe medidas y colores escritos a mano (`16.dp`, `14.sp`, `Color(0xFF…)`) fuera del sistema de diseño.
 * Las pantallas usan los tokens de `core/designsystem` (docs/diseno.md); la exención va en `excludes`.
 */
class HardcodedDesignValue(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        id = "HardcodedDesignValue",
        severity = Severity.Style,
        description = "Usa los tokens de core/designsystem (Spacing, Radius, Sizes, Strokes, Elevations, " +
            "colores del tema) en lugar de valores escritos a mano.",
        debt = Debt.FIVE_MINS
    )

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val unit = expression.selectorExpression?.text ?: return
        if (unit in UNITS && expression.receiverExpression is KtConstantExpression) {
            report(
                CodeSmell(issue, Entity.from(expression), "`${expression.text}` escrito a mano: usa un token del tema.")
            )
        }
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text != "Color") return
        val first = expression.valueArguments.firstOrNull()?.getArgumentExpression() as? KtConstantExpression ?: return
        if (first.text.startsWith("0x", ignoreCase = true)) {
            report(
                CodeSmell(issue, Entity.from(expression), "Color hex `${first.text}` escrito a mano: usa el tema.")
            )
        }
    }

    private companion object {
        val UNITS = setOf("dp", "sp")
    }
}
