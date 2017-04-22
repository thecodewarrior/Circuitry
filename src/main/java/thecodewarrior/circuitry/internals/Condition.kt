package thecodewarrior.circuitry.internals

import com.teamwizardry.librarianlib.features.saving.Savable
import com.teamwizardry.librarianlib.features.saving.Save
import thecodewarrior.circuitry.tile.EnumBooleanOperation
import thecodewarrior.circuitry.tile.EnumSpecialSignal
import thecodewarrior.circuitry.tile.SignalConfig

/**
 * Created by TheCodeWarrior
 */
@Savable
class Condition {

    @Save var left: SignalConfig = SignalConfig.Special(EnumSpecialSignal.NULL)
    @Save var right: SignalConfig = SignalConfig.Number(0)
    @Save var operation: EnumBooleanOperation = EnumBooleanOperation.EQUAL

    fun apply(set: SigSet): ConditionResult {
        val left = left
        val right = right
        if(left is SignalConfig.Special && left.type == EnumSpecialSignal.NULL) return ConditionResult(false)
        if(right is SignalConfig.Special && right.type == EnumSpecialSignal.NULL) return ConditionResult(false)

        val rightVal = when(right) {
            is SignalConfig.Number -> right.num
            is SignalConfig.Signal -> set.getRaw(right.type, right.key)
            is SignalConfig.Special -> 0
        }

        if(left is SignalConfig.Special) {
            when(left.type) {
                EnumSpecialSignal.ANYTHING -> {
                    val bestGuess = when(operation) {
                        EnumBooleanOperation.LESS -> set.minBy { it.value }
                        EnumBooleanOperation.GREATER -> set.maxBy { it.value }
                        EnumBooleanOperation.EQUAL -> set.find { it.value == rightVal }
                        EnumBooleanOperation.NOTEQUAL -> set.find { it.value != rightVal }
                    }
                    if(bestGuess == null) return ConditionResult(false)
                    else if(operation.perform(bestGuess.value, rightVal))
                        return ConditionResult(true, SigSet( listOf(bestGuess) ))
                    else
                        return ConditionResult(false)
                }
                EnumSpecialSignal.EVERYTHING -> {
                    val matchedAll = set.all { operation.perform(it.value, rightVal) }

                    if(matchedAll) {
                        return ConditionResult(true, set)
                    }
                    return ConditionResult(false)
                }
            }
        }
        if(left is SignalConfig.Signal) {
            val value = set.getRaw(left.type, left.key)
            if(operation.perform(value, rightVal)) {
                return ConditionResult(true, SigSet(listOf(Signal(left.type, left.key, value))))
            } else {
                return ConditionResult(false)
            }
        }
        return ConditionResult(false)
    }
}

data class ConditionResult(val passed: Boolean, val passedSignals: SigSet = SigSet.ZERO)
