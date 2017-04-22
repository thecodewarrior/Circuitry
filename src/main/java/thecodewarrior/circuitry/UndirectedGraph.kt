package thecodewarrior.circuitry

import com.google.common.collect.HashMultimap
import com.teamwizardry.librarianlib.features.autoregister.SerializerFactoryRegister
import com.teamwizardry.librarianlib.features.kotlin.forEach
import com.teamwizardry.librarianlib.features.kotlin.readBooleanArray
import com.teamwizardry.librarianlib.features.kotlin.safeCast
import com.teamwizardry.librarianlib.features.kotlin.writeBooleanArray
import com.teamwizardry.librarianlib.features.saving.FieldType
import com.teamwizardry.librarianlib.features.saving.serializers.Serializer
import com.teamwizardry.librarianlib.features.saving.serializers.SerializerFactory
import com.teamwizardry.librarianlib.features.saving.serializers.SerializerFactoryMatch
import com.teamwizardry.librarianlib.features.saving.serializers.SerializerRegistry
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import java.util.*

/**
 * @author Narendra Yadala
 * * Implementation of undirected graph represented using adjacency list.
 *
 * Modified by TheCodeWarrior
 */
class UndirectedGraph<T> : Iterable<T> {
    private val graph = HashMultimap.create<T, T>()
    private val edgeSet = mutableSetOf<Pair<T, T>>()

    fun addEdge(start: T, dest: T) {
        graph.put(start, dest)
        graph.put(dest, start)
        edgeSet.add(start to dest)
    }

    fun removeEdge(start: T, dest: T) {
        graph.remove(start, dest)
        graph.remove(dest, start)
        edgeSet.remove(start to dest)
        edgeSet.remove(dest to start)
    }

    fun exists(start: T, end: T): Boolean {
        return graph.containsEntry(start, end)
    }

    fun getNeighbors(node: T): Set<T> {
        val neighbors = graph[node] ?: setOf()

        return Collections.unmodifiableSet(neighbors)
    }

    override fun iterator(): Iterator<T> {
        return graph.keySet().iterator()
    }

    val nodes: Iterable<T>
        get() = graph.keySet()

    val edges: Iterable<Pair<T, T>>
        get() = edgeSet

    val size: Int
        get() = graph.size()

    val isEmpty: Boolean
        get() = graph.isEmpty

    fun clear() {
        graph.clear()
        edgeSet.clear()
    }
}

@SerializerFactoryRegister
object SerializeUndirectedGraph : SerializerFactory("UndirectedGraph") {
    override fun canApply(type: FieldType): SerializerFactoryMatch {
        return this.canApplyExact(type, UndirectedGraph::class.java)
    }

    override fun create(type: FieldType): Serializer<*> {
        return SerializeUndirectedGraph(type, type.resolveGeneric(UndirectedGraph::class.java, 0))
    }

    class SerializeUndirectedGraph(type: FieldType, val generic: FieldType) : Serializer<UndirectedGraph<Any?>>(type) {
        val serGeneric: Serializer<Any> by SerializerRegistry.lazy(generic)

        override fun readNBT(nbt: NBTBase, existing: UndirectedGraph<Any?>?, syncing: Boolean): UndirectedGraph<Any?> {
            val graph = existing ?: UndirectedGraph<Any?>()
            graph.clear()

            val listTag = nbt.safeCast<NBTTagList>()

            listTag.forEach<NBTTagCompound> { edgeTag ->
                graph.addEdge(
                        edgeTag.getTag("a")?.let { serGeneric.read(it, null, syncing) },
                        edgeTag.getTag("b")?.let { serGeneric.read(it, null, syncing) }
                )
            }

            return graph
        }

        override fun writeNBT(value: UndirectedGraph<Any?>, syncing: Boolean): NBTBase {
            val tag = NBTTagList()

            value.edges.forEach { edge ->
                val edgeTag = NBTTagCompound()
                tag.appendTag(edgeTag)
                edge.first?.also { edgeTag.setTag("a", serGeneric.write(it, syncing)) }
                edge.second?.also { edgeTag.setTag("b", serGeneric.write(it, syncing)) }
            }

            return tag
        }

        override fun readBytes(buf: ByteBuf, existing: UndirectedGraph<Any?>?, syncing: Boolean): UndirectedGraph<Any?> {
            val graph = existing ?: UndirectedGraph<Any?>()
            graph.clear()

            val aNulls = buf.readBooleanArray()
            val bNulls = buf.readBooleanArray()

            for(i in aNulls.indices) {
                val first = if(aNulls[i]) null else
                    serGeneric.read(buf, null, syncing)
                val second = if(bNulls[i]) null else
                    serGeneric.read(buf, null, syncing)
                graph.addEdge(first, second)
            }

            return graph
        }

        override fun writeBytes(buf: ByteBuf, value: UndirectedGraph<Any?>, syncing: Boolean) {
            val list = value.edges.toList()

            val aNulls = list.map { it.first == null }.toTypedArray().toBooleanArray()
            val bNulls = list.map { it.second == null }.toTypedArray().toBooleanArray()

            buf.writeBooleanArray(aNulls)
            buf.writeBooleanArray(bNulls)

            list.forEach { edge ->
                edge.first?.also { serGeneric.write(buf, it, syncing) }
                edge.second?.also { serGeneric.write(buf, it, syncing) }
            }
        }
    }
}
