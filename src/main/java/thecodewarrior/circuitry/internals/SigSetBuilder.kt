package thecodewarrior.circuitry.internals

import com.teamwizardry.librarianlib.features.saving.Save
import gnu.trove.map.TIntIntMap
import gnu.trove.map.hash.TIntIntHashMap
import gnu.trove.procedure.TIntIntProcedure

/**
 * Created by TheCodeWarrior
 */
class SigSetBuilder {
    @Save private var map = mutableMapOf<SignalType<*>, TIntIntHashMap>()

    fun add(other: SigSet) {
        other.forEachType { type ->
            val m = map.getOrPut(type, { TIntIntHashMap() })
            other.forEachEntry(type, TIntIntProcedure { k, v ->
                m.put(k, m.get(k) + v)
                true
            })
        }
    }

    fun <T> add(type: SignalType<T>, key: T, amount: Int) {
        val keyI = type.toInt(key)
        val m = map.getOrPut(type, { TIntIntHashMap() })
                m.put(keyI, m.get(keyI) + amount)
    }

    fun add(other: Signal) {
        addRaw(other.type, other.key, other.value)
    }

    fun addRaw(type: SignalType<*>, key: Int, amount: Int) {
        val m = map.getOrPut(type, { TIntIntHashMap() })
        m.put(key, m.get(key) + amount)
    }

    fun build(): SigSet {
        val zeroSet = mutableSetOf<Int>()
        val zeroTypes = mutableSetOf<SignalType<*>>()

        map.forEach { type, m ->
            zeroSet.clear()
            var empty = true
            m.forEachEntry({ k, v ->
                if(v == 0)
                    zeroSet.add(k)
                else
                    empty = false
                true
            })
            if(empty) zeroTypes.add(type)
            zeroSet.forEach { m.remove(it) }
        }
        zeroTypes.forEach { map.remove(it) }

        return SigSet(map as MutableMap<SignalType<*>, TIntIntMap>)
    }
}
