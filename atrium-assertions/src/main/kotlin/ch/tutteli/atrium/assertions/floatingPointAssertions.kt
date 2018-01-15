package ch.tutteli.atrium.assertions

import ch.tutteli.atrium.assertions.DescriptionFloatingPointAssertion.*
import ch.tutteli.atrium.creating.AssertionPlant
import ch.tutteli.atrium.creating.PlantHasNoSubjectException
import ch.tutteli.atrium.reporting.RawString
import ch.tutteli.atrium.reporting.translating.TranslatableWithArgs
import java.math.BigDecimal
import java.text.DecimalFormat
import kotlin.math.absoluteValue

fun _toBeWithErrorTolerance(plant: AssertionPlant<Float>, expected: Float, tolerance: Float): Assertion
    = toBeWithErrorToleranceOfFloatOrDouble(plant, expected, tolerance, { (plant.subject - expected).absoluteValue })

fun _toBeWithErrorTolerance(plant: AssertionPlant<Double>, expected: Double, tolerance: Double): Assertion
    = toBeWithErrorToleranceOfFloatOrDouble(plant, expected, tolerance, { (plant.subject - expected).absoluteValue })

fun <T : BigDecimal> _toBeWithErrorTolerance(plant: AssertionPlant<T>, expected: T, tolerance: T): Assertion {
    val absDiff = { (plant.subject - expected).abs() }
    return toBeWithErrorTolerance(expected, tolerance, absDiff) { df ->
        listOf(createToBeWithErrorToleranceExplained(df, plant, expected, absDiff, tolerance))
    }
}

private fun <T : Comparable<T>> toBeWithErrorToleranceOfFloatOrDouble(plant: AssertionPlant<T>, expected: T, tolerance: T, absDiff: () -> T): Assertion {
    return toBeWithErrorTolerance(expected, tolerance, absDiff) { df ->
        listOf(
            BasicExplanatoryAssertion(RawString.create(TranslatableWithArgs(FAILURE_DUE_TO_FLOATING_POINT_NUMBER, plant.subject::class.java.name))),
            createToBeWithErrorToleranceExplained(df, plant, expected, absDiff, tolerance)
        )
    }
}

private fun <T : Comparable<T>> createToBeWithErrorToleranceExplained(df: DecimalFormat, plant: AssertionPlant<T>, expected: T, absDiff: () -> T, tolerance: T)
    = BasicExplanatoryAssertion(RawString.create(TranslatableWithArgs(TO_BE_WITH_ERROR_TOLERANCE_EXPLAINED, df.format(plant.subject), df.format(expected), df.format(absDiff()), df.format(tolerance))))

private fun <T : Comparable<T>> toBeWithErrorTolerance(expected: T, tolerance: T, absDiff: () -> T, explanatoryAssertionCreator: (DecimalFormat) -> List<Assertion>): Assertion {
    val isWithinRange = try {
        absDiff() <= tolerance
    } catch (e: PlantHasNoSubjectException) {
        true //TODO that's a hack, we need a better solution
    }
    return if (isWithinRange) {
        BasicDescriptiveAssertion(TranslatableWithArgs(TO_BE_WITH_ERROR_TOLERANCE, tolerance), expected, isWithinRange)
    } else {
        //TODO that's not nice in case we use it in an Iterable contains assertion, for instance contains...entry { toBeWithErrorTolerance(x, 0.01) }
        //we do not want to see the failure nor the exact check in the 'an entry which...' part
        //same problematic applies to feature assertions within an identification lambda
        val df = DecimalFormat("###,##0.0")
        df.maximumFractionDigits = 340
        val explanatoryAssertion = listOf(AssertionGroup.Builder.explanatory.withDefault.create(
            explanatoryAssertionCreator(df)
        ))
        FixHoldsAssertionGroup(DefaultListAssertionGroupType, TranslatableWithArgs(TO_BE_WITH_ERROR_TOLERANCE, tolerance), expected, explanatoryAssertion, false)
    }
}
