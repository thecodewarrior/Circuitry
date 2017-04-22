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
@TileRegister("combinator_arithmetic")
class TileArithmeticCombinator : TileComputingCombinatorBase() {
    @Save var operation = EnumArithmeticOperation.ADD

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

        when(left) {
            is SignalConfig.Special -> { // if left == EACH
                // only allowed specials are NULL and EACH. NULL is handled above, so it must be NULL
                when(out) {
                    is SignalConfig.Special -> { // if out == EACH
                        // only NULL & EACH allowed. NULL handled above, must be EACH
                        v.forEach { (signalType, key, value) ->
                            builder.addRaw(signalType, key, operation.perform(value, rightVal))
                        }
                    }
                    is SignalConfig.Signal -> {
                        builder.addRaw(out.type, out.key, v.sumBy { operation.perform(it.value, rightVal) })
                    }
                }
            }
            is SignalConfig.Signal -> {
                if(out is SignalConfig.Signal) {
                    builder.addRaw(out.type, out.key, operation.perform(v.getRaw(left.type, left.key), rightVal))
                }
            }
        }
    }

    override fun getIcon(): ResourceLocation {
        return operation.icon
    }

    override fun possibleOperators(): Array<out IOperator> {
        return EnumArithmeticOperation.values()
    }

    override var operatorConfig: IOperator
        get() = operation
        set(value) { operation = value as EnumArithmeticOperation }

    override fun getAllowedSpecialLeft(): Array<EnumSpecialSignal> {
        return arrayOf(EnumSpecialSignal.EACH)
    }

    override fun getAllowedSpecialOut(left: SignalConfig): Array<EnumSpecialSignal> {
        if((left as? SignalConfig.Special)?.type == EnumSpecialSignal.EACH)
            return arrayOf(EnumSpecialSignal.EACH)
        return arrayOf(EnumSpecialSignal.NULL)
    }
}

enum class EnumArithmeticOperation : IOperator {
    ADD { override fun p(a: Long, b: Long) = a + b },
    SUBTRACT { override fun p(a: Long, b: Long) = a - b },
    MULTIPLY { override fun p(a: Long, b: Long) = a * b },
    DIVIDE { override fun p(a: Long, b: Long) = if(b == 0L) 0L else a / b };

    fun perform(a: Int, b: Int): Int {
        val v = p(a.toLong(), b.toLong())
        val s = if(v < 0) -1 else 1
        return ( (v*s) and (Integer.MAX_VALUE.toLong()) ).toInt() * s // perform math and clip overflow
    }

    abstract fun p(a: Long, b: Long): Long

    val icon: ResourceLocation = ResourceLocation(CircuitryMod.MODID, "blocks/combinator_overlay_" + name.toLowerCase(Locale.ROOT))
    override val inventoryIcon: ResourceLocation = icon
}
