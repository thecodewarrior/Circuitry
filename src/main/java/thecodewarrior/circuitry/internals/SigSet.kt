package thecodewarrior.circuitry.internals

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.teamwizardry.librarianlib.features.saving.Savable
import com.teamwizardry.librarianlib.features.saving.SavableConstructorOrder
import com.teamwizardry.librarianlib.features.saving.Save
import gnu.trove.map.TIntIntMap
import gnu.trove.procedure.TIntIntProcedure

/**
 * Created by TheCodeWarrior
 */
@Savable
open class SigSet @SavableConstructorOrder("map") constructor(open protected @Save val map: MutableMap<SignalType<*>, TIntIntMap> = mutableMapOf()) : Iterable<Signal> {

    constructor(iterable: Iterable<Signal>) : this(iterableToMap(iterable))
    constructor(iterator: Iterator<Signal>) : this(iteratorToMap(iterator))

    open protected val signalSet: Set<Signal> by lazy {
        val set = mutableSetOf<Signal>()
        map.forEach { type, values ->
            values.forEachEntry { k, v ->
                set.add(Signal(type, k, v))
                true
            }
        }
        set
    }

    val size: Int
        get() = signalSet.size


    fun <T> get(type: SignalType<T>, key: T) = map.get(type)?.get(type.toInt(key)) ?: 0
    fun getRaw(type: SignalType<*>, key: Int) = map.get(type)?.get(key) ?: 0

    fun forEachType(l: (SignalType<*>) -> Unit) {
        map.forEach { type, _ -> l(type) }
    }

    fun forEachEntry(type: SignalType<*>, l: TIntIntProcedure) {
        map.get(type)?.forEachEntry(l)
    }

    operator fun plus(other: SigSet): SigSet {
        val builder = SigSetBuilder()
        builder.add(this)
        builder.add(other)
        return builder.build()
    }

    fun filter(l: (SignalType<*>, Int, Int) -> Boolean): SigSet {
        val builder = SigSetBuilder()
        map.forEach { type, values ->
            values.forEachEntry { k, v ->
                if(l(type, k, v)) builder.addRaw(type, k, v)
                true
            }
        }
        return builder.build()
    }

    override fun iterator(): Iterator<Signal> {
        return signalSet.iterator()
    }

    companion object {
        val ZERO: SigSet = SigSetZero

        private fun iterableToMap(iter: Iterable<Signal>): MutableMap<SignalType<*>, TIntIntMap> {
            return iteratorToMap(iter.iterator())
        }

        private fun iteratorToMap(iter: Iterator<Signal>): MutableMap<SignalType<*>, TIntIntMap> {
            val builder = SigSetBuilder()
            iter.forEach {
                builder.addRaw(it.type, it.key, it.value)
            }
            return builder.build().map
        }
    }
}

object SigSetZero : SigSet(ImmutableMap.of()) {
    override val map: MutableMap<SignalType<*>, TIntIntMap>
        get() = ImmutableMap.of()
    override val signalSet: MutableSet<Signal>
        get() = ImmutableSet.of()
}

data class Signal(val type: SignalType<*>, val key: Int, val value: Int)

fun Iterable<Signal>.toSigSet() = SigSet(this)
fun Iterator<Signal>.toSigSet() = SigSet(this)
