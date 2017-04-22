package thecodewarrior.circuitry.internals

import com.teamwizardry.librarianlib.features.saving.Savable
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.WorldServer
import thecodewarrior.circuitry.AStar
import thecodewarrior.circuitry.IdContainer
import thecodewarrior.circuitry.UndirectedGraph

/**
 * Created by TheCodeWarrior
 */
@Savable
class CircuitNetwork : IdContainer {
    @Save override var id: Int = 0

    @Save var color: NetworkColor = NetworkColor.RED

    @Save var signal: SigSet = SigSet.ZERO
        private set
    private var back: SigSet = SigSet.ZERO

    @Save private var contained = UndirectedGraph<CircuitPos>()
    @Save private var chunks_ = mutableSetOf<ChunkPos>()

    var loaded = true
        private set

    val edges: Iterable<Pair<CircuitPos, CircuitPos>>
        get() = contained.edges
    val nodes: Iterable<CircuitPos>
        get() = contained.nodes
    val chunks: Set<ChunkPos>
        get() = chunks_
    val size: Int
        get() = contained.size

    fun add(a: CircuitPos, b: CircuitPos) {
        contained.addEdge(a, b)
        rebuildChunks()
    }

    fun addAll(other: CircuitNetwork) {
        other.contained.edges.forEach {
            this.contained.addEdge(it.first, it.second)
        }
        rebuildChunks()
    }

    fun addAll(set: Iterable<Pair<CircuitPos, CircuitPos>>) {
        set.forEach {
            add(it.first, it.second)
        }
    }

    fun hasEdge(a: CircuitPos, b: CircuitPos): Boolean {
        return contained.exists(a, b)
    }

    fun removeEdge(a: CircuitPos, b: CircuitPos) {
        contained.removeEdge(a, b)
    }

    fun removeAll(set: Iterable<Pair<CircuitPos, CircuitPos>>) {
        set.forEach {
            removeEdge(it.first, it.second)
        }
    }

    fun pathExists(a: CircuitPos, b: CircuitPos): Boolean {
        return AStar.pathfind(a, b, contained, {
            return@pathfind 0.0 + // to convert to a double and make it all line up pretty
                    (a.block.x - it.block.x) * (a.block.x - it.block.x) +
                    (a.block.y - it.block.y) * (a.block.y - it.block.y) +
                    (a.block.z - it.block.z) * (a.block.z - it.block.z)
        }).isNotEmpty()
    }

    fun floodFill(aNode: CircuitPos): List<Pair<CircuitPos, CircuitPos>> {
        val list = mutableListOf<Pair<CircuitPos, CircuitPos>>()

        val queue = mutableSetOf<CircuitPos>()
        val finished = mutableSetOf<CircuitPos>()
        queue.add(aNode)
        while(queue.isNotEmpty()) {
            val node = queue.first()
            queue.remove(node)
            finished.add(node)
            val neighbors = contained.getNeighbors(node)
            neighbors.forEach {
                list.add(node to it)
                if(it !in finished)
                    queue.add(it)
            }
        }

        return list
    }

    fun rebuildChunks() {
        chunks_.clear()
        contained.nodes.forEach {
            chunks_.add(ChunkPos(it.block))
        }
    }

    fun getNeighbors(cp: CircuitPos) = contained.getNeighbors(cp)
    fun checkLoaded(world: WorldServer) {
        loaded = chunks.any {
            world.isBlockLoaded(BlockPos(it.xStart + 8, 0, it.zStart + 8))
        }
    }

    fun tickStart(world: WorldServer) {
        val builder = SigSetBuilder()
        contained.nodes.forEach { circuitPos ->
            if(world.isBlockLoaded(circuitPos.block)) {
                CircuitryAPI.getCircuitHandler(world, circuitPos.block)?.also {
                    it.addOutput(world, circuitPos.block, circuitPos.port, this.color, builder)
                }
            }
        }
        this.back = builder.build()
    }

    fun tickEnd(world: WorldServer) {
        this.signal = back
    }
}
