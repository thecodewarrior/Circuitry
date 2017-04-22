package thecodewarrior.circuitry.tile

import com.teamwizardry.librarianlib.features.autoregister.TileRegister
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import thecodewarrior.circuitry.CircuitryMod
import thecodewarrior.circuitry.internals.NetworkColor
import thecodewarrior.circuitry.internals.SigSetBuilder
import java.util.*

/**
 * Created by TheCodeWarrior
 */
@TileRegister("combinator_decider")
class TileDeciderCombinator : TileComputingCombinatorBase() {
    @Save var operation = EnumBooleanOperation.EQUAL
    @Save var outModeOne = true

    override fun addOutput(world: World, pos: BlockPos, port: Int, color: NetworkColor, builder: SigSetBuilder) {
        if(port == 0)
            return

        // so that type inference works
        val operation = this.operation
        val right = this.right
        val left = this.left
        val out = this.out

        if(left == SignalConfig.Special(EnumSpecialSignal.NULL)) return
        if(out == SignalConfig.Special(EnumSpecialSignal.NULL)) return
        if(right == SignalConfig.Special(EnumSpecialSignal.NULL)) return

        val v = getInput(world, pos, 0)
        val rightVal = when(right) {
            is SignalConfig.Number -> right.num
            is SignalConfig.Signal -> v.getRaw(right.type, right.key)
            is SignalConfig.Special -> 0
        }

        // these conditions mean that either all the signals are passed through, or none of them are. Never a subset
        var all_or_nothing = false
        all_or_nothing = all_or_nothing || (
                left is SignalConfig.Special && left.type == EnumSpecialSignal.EVERYTHING &&
                        v.all { signal -> operation.perform(signal.value, rightVal) }
                )
        all_or_nothing = all_or_nothing || (
                left is SignalConfig.Special && left.type == EnumSpecialSignal.ANYTHING &&
                        v.any { signal -> operation.perform(signal.value, rightVal) }
                )
        all_or_nothing = all_or_nothing || (
                left is SignalConfig.Signal && operation.perform(v.getRaw(left.type, left.key), rightVal)
                )

        if(all_or_nothing) {
            if(out is SignalConfig.Special && out.type == EnumSpecialSignal.EVERYTHING) {
                if(outModeOne) {
                    v.forEach { signal ->
                        builder.addRaw(signal.type, signal.key, 1)
                    }
                } else {
                    builder.add(v)
                }
            } else if(out is SignalConfig.Signal){
                if(outModeOne) {
                    builder.addRaw(out.type, out.key, 1)
                } else {
                    builder.addRaw(out.type, out.key, v.getRaw(out.type, out.key))
                }
            }
        }

        if(left is SignalConfig.Special && left.type == EnumSpecialSignal.EACH) {
            if(out is SignalConfig.Special && out.type == EnumSpecialSignal.EACH) {
                if(outModeOne) {
                    v.forEach { signal ->
                        if(operation.perform(signal.value, rightVal))
                            builder.addRaw(signal.type, signal.key, 1)
                    }
                } else {
                    v.forEach { signal ->
                        if(operation.perform(signal.value, rightVal))
                            builder.addRaw(signal.type, signal.key, signal.value)
                    }
                }
            } else if(out is SignalConfig.Signal){
                if(outModeOne) {
                    if(v.any { signal -> operation.perform(signal.value, rightVal) }) {
                        builder.addRaw(out.type, out.key, 1)
                    }
                } else {
                    builder.addRaw(out.type, out.key, v.getRaw(out.type, out.key))
                }
            }
        }
    }

    override fun getIcon(): ResourceLocation {
        return operation.icon
    }

    override fun possibleOperators(): Array<out IOperator> {
        return EnumBooleanOperation.values()
    }

    override var operatorConfig: IOperator
        get() = operation
        set(value) { operation = value as EnumBooleanOperation }

    override fun getAllowedSpecialLeft(): Array<EnumSpecialSignal> {
        return arrayOf(EnumSpecialSignal.EVERYTHING, EnumSpecialSignal.ANYTHING, EnumSpecialSignal.EACH)
    }

    override fun getAllowedSpecialOut(left: SignalConfig): Array<EnumSpecialSignal> {
        if((left as? SignalConfig.Special)?.type == EnumSpecialSignal.EACH) {
            return arrayOf(EnumSpecialSignal.EACH)
        }
        return arrayOf(EnumSpecialSignal.EVERYTHING)
    }
}

enum class EnumBooleanOperation : IOperator {
    EQUAL { override fun perform(a: Int, b: Int) = a == b },
    NOTEQUAL { override fun perform(a: Int, b: Int) = a != b },
    LESS { override fun perform(a: Int, b: Int) = a < b },
    GREATER { override fun perform(a: Int, b: Int) = a > b };

    abstract fun perform(a: Int, b: Int): Boolean

    val icon: ResourceLocation = ResourceLocation(CircuitryMod.MODID, "blocks/combinator_overlay_" + name.toLowerCase(Locale.ROOT))
    override val inventoryIcon: ResourceLocation = icon
}
