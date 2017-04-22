package thecodewarrior.circuitry.internals

import com.teamwizardry.librarianlib.features.base.capability.CapabilityMod
import com.teamwizardry.librarianlib.features.base.capability.ICapabilityObjectProvider
import com.teamwizardry.librarianlib.features.network.PacketHandler
import com.teamwizardry.librarianlib.features.saving.NoSync
import com.teamwizardry.librarianlib.features.saving.Save
import com.teamwizardry.librarianlib.features.saving.SaveInPlace
import com.teamwizardry.librarianlib.features.utilities.profile
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.fml.relauncher.Side
import thecodewarrior.circuitry.CircuitryMod
import thecodewarrior.circuitry.IDStore
import thecodewarrior.circuitry.IDefaultedWorldEventListener
import thecodewarrior.circuitry.network.PacketInspectSync
import thecodewarrior.circuitry.network.PacketNetworkSync
import java.util.*

/**
 * Created by TheCodeWarrior
 */
class CapabilityCircuitWorld(val world: WorldServer) : CapabilityMod(name), IDefaultedWorldEventListener {
    companion object {
        val name = ResourceLocation(CircuitryMod.MODID, "circuit_world")

        @JvmStatic
        @CapabilityInject(CapabilityCircuitWorld::class)
        lateinit var cap: Capability<CapabilityCircuitWorld>

        init {
            register(CapabilityCircuitWorld::class.java, ICapabilityObjectProvider {
                cap
            })
            MinecraftForge.EVENT_BUS.register(CapabilityCircuitWorld::class.java)
        }
    }

    init {
        world.addEventListener(this)
    }

    @Save val networkTracker = NetworkTracker()
    @Save var conditions = mutableMapOf<BlockPos, Array<Condition>>()

    fun  getConnectedNetwork(circuitPos: CircuitPos, color: NetworkColor): CircuitNetwork? {
        return networkTracker.getNetworkId(circuitPos, color)
    }

    fun addConnection(color: NetworkColor, a: CircuitPos, b: CircuitPos) {
        if(a == b)
            return
        val aNet = getConnectedNetwork(a, color)
        val bNet = getConnectedNetwork(b, color)

        if(aNet != null && bNet != null) {
            if(aNet == bNet) {
                if(aNet.hasEdge(a, b)) {
                    removeConnection(aNet, a, b)
                } else {
                    aNet.add(a, b)
                    updateAllAround(aNet)
                }
            } else {
                val newNet = CircuitNetwork()

                newNet.color = color
                newNet.addAll(aNet)
                newNet.addAll(bNet)
                newNet.add(a, b)


                networkTracker.removeNetwork(aNet, bNet)
                networkTracker.addNetwork(newNet)
                updateAllAround(newNet)
                updateAllAround(aNet)
                updateAllAround(bNet)
            }
            return
        }
        if(aNet == null && bNet == null) {
            val newNet = CircuitNetwork()
            newNet.color = color
            newNet.add(a, b)

            networkTracker.addNetwork(newNet)
            updateAllAround(newNet)

            return
        }
        val net = aNet ?: bNet ?: return
        net.add(a, b)
        networkTracker.invalidate()
        updateAllAround(net)

    }

    fun removeConnection(net: CircuitNetwork, a: CircuitPos, b: CircuitPos) {
        net.removeEdge(a, b)
        if(net.size == 0) {
            networkTracker.removeNetwork(net)
            updateAllAround(net)
        } else {
            possiblySplitNetwork(net, a, b)
        }
    }

    fun possiblySplitNetwork(net: CircuitNetwork, vararg possibleOrigins: CircuitPos) {
        val originSet = mutableSetOf(*possibleOrigins)

        while(originSet.size > 0) {
            val origin = originSet.first()
            originSet.remove(origin)

            val flood = net.floodFill(origin)
            if(flood.isEmpty())
                continue

            flood.forEach {
                originSet.remove(it.first)
                originSet.remove(it.second)
            }

            if(originSet.isEmpty()) // meaning that all the remaining nodes were caught in the flood fill
                break // we don't need to split it off any more

            val newNet = CircuitNetwork()
            net.removeAll(flood)
            newNet.addAll(flood)

            networkTracker.addNetwork(newNet)
            updateAllAround(newNet)
        }

        if(net.size == 0)
            networkTracker.removeNetwork(net)
        updateAllAround(net)
    }

    fun updateAllAround(net: CircuitNetwork) {
        world.playerEntities.forEach { player ->
            if(player is EntityPlayerMP &&
                    net.chunks.any {
                        world.playerChunkMap.isPlayerWatchingChunk(player, it.chunkXPos, it.chunkZPos)
                    })
                PacketHandler.NETWORK.sendTo(PacketNetworkSync().also {
                    it.id = net.id
                    it.network = networkTracker.networks[net.id]
                }, player)
        }
    }

    fun tick() {
        profile("circuit", Side.SERVER) {
            val networks = networkTracker.networks.id2obj.values
            profile("tickStart", Side.SERVER) {
                networks.forEach { net ->
                    if (!net.loaded)
                        return@forEach
                    net.tickStart(world)
                }
            }

            profile("tickEnd", Side.SERVER) {
                networks.forEach { net ->
                    if (!net.loaded)
                        return@forEach
                    net.tickEnd(world)
                }
            }
            profile("tickNodes", Side.SERVER) {
                networkTracker.nodes.forEach { node ->
                    if (world.isBlockLoaded(node.block)) {
                        CircuitryAPI.getCircuitHandler(world, node.block)?.also {
                            it.tick(world, node.block, node.port)
                        }
                    }
                }
            }
            profile("sync", Side.SERVER) {
                world.playerEntities.forEach { player ->
                    if (player !is EntityPlayerMP) return@forEach

                    val netId = networkTracker.playerWatchingTracker[player.uniqueID] ?: return@forEach
                    val net = networkTracker.networks[netId]
                    if (net == null) {
                        networkTracker.playerWatchingTracker.remove(player.uniqueID)
                        return@forEach
                    }
                    if (net.chunks.none { world.playerChunkMap.isPlayerWatchingChunk(player, it.chunkXPos, it.chunkZPos) }) {
                        networkTracker.playerWatchingTracker.remove(player.uniqueID)
                        if (player is EntityPlayerMP)
                            PacketHandler.NETWORK.sendTo(PacketInspectSync(), player)
                        return@forEach
                    }
                    PacketHandler.NETWORK.sendTo(PacketInspectSync().also {
                        it.value = net.signal
                    }, player)
                }
            }
        }
    }

    fun sendChunkToPlayer(chunk: ChunkPos, player: EntityPlayerMP) {
        networkTracker.networkByChunk[chunk]?.forEach {
            trySyncNet(it, player)
        }
    }
    fun unwatch(chunk: ChunkPos, player: EntityPlayerMP) {
        val toRemove = mutableSetOf<Int>()
        networkTracker.networkByPlayer.get(player.uniqueID)?.forEach {
            val net = networkTracker.networks[it]
            if(net == null) {
                toRemove.add(it)
            } else {
                if (net.chunks.none {
                    world.playerChunkMap.isPlayerWatchingChunk(player, it.chunkXPos, it.chunkZPos)
                })
                    toRemove.add(it)
            }
        }
        toRemove.forEach { id ->
            PacketHandler.NETWORK.sendTo(PacketNetworkSync().also { it.id = id; it.network = null }, player)
        }
        networkTracker.networkByPlayer.get(player.uniqueID)?.removeAll(toRemove)
    }

    fun trySyncNet(id: Int, player: EntityPlayerMP) {
        if(!(networkTracker.networkByPlayer.get(player.uniqueID)?.contains(id) ?: false)) {
            networkTracker.networkByPlayer.getOrPut(player.uniqueID, ::mutableSetOf).add(id)
            val net = networkTracker.networks[id]
            if(net != null) {
                PacketHandler.NETWORK.sendTo(PacketNetworkSync().also { it.id = id; it.network = net }, player)
            }
        }
    }


    fun checkChunkLoaded(xPosition: Int, zPosition: Int) {
        networkTracker.networkByChunk[ChunkPos(xPosition, zPosition)]
                ?.map { networkTracker.networks[it] }
                ?.forEach {
                    it?.checkLoaded(world)
                }
    }

    fun getConditions(pos: BlockPos): Array<Condition>? {
        conditions.get(pos)?.also { return@getConditions it }

        val circuitHandler = CircuitryAPI.getCircuitHandler(world, pos)
        if(circuitHandler is ICircuitConditionHolder) {
            conditions.put(pos, Array(circuitHandler.conditionCount) { Condition() } )
            return conditions.get(pos)!!
        }

        return null
    }

    override fun notifyBlockUpdate(worldIn: World, pos: BlockPos, oldState: IBlockState, newState: IBlockState, flags: Int) {
        val handler = CircuitryAPI.getCircuitHandler(world, pos)
        if(handler == null) {
            if(pos in networkTracker.maxPortByBlockPos)
                removeNode(pos)
            if(pos in conditions)
                conditions.remove(pos)
        } else {
            if (handler is ICircuitConditionHolder && handler.shouldRefreshConditions(world, pos, oldState))
                conditions.remove(pos)
            if (handler.shouldRefreshWires(world, pos, oldState))
                removeNode(pos)
        }
    }

    fun removeNode(pos: BlockPos) {
        val maxPort = networkTracker.maxPortByBlockPos.get(pos) ?: 0
        val ports = (0..maxPort)

        networkTracker.cachePaused = true
        ports.forEach { port ->
            val cp = CircuitPos(pos, port)
            networkTracker.networkByCPByColor.get(NetworkColor.RED)?.get(cp)?.let { networkTracker.networks[it] }?.also { network ->
                val neighbors = network.getNeighbors(cp).toTypedArray()
                neighbors.forEach {
                    network.removeEdge(cp, it)
                }
                if(network.size == 0) {
                    networkTracker.removeNetwork(network)
                    updateAllAround(network)
                } else {
                    possiblySplitNetwork(network, cp, *neighbors)
                }
            }
            networkTracker.networkByCPByColor.get(NetworkColor.GREEN)?.get(cp)?.let { networkTracker.networks[it] }?.also { network ->
                val neighbors = network.getNeighbors(cp).toTypedArray()
                neighbors.forEach {
                    network.removeEdge(cp, it)
                }
                if(network.size == 0) {
                    networkTracker.removeNetwork(network)
                    updateAllAround(network)
                } else {
                    possiblySplitNetwork(network, cp, *neighbors)
                }
            }
        }
        networkTracker.cachePaused = false
        networkTracker.invalidate()

        conditions.remove(pos)
    }
}

@SaveInPlace
class NetworkTracker {
    @Save var networks = IDStore<CircuitNetwork>()
    var networkByCPByColor = mutableMapOf<NetworkColor, MutableMap<CircuitPos, Int>>()
    var networkByChunk = mutableMapOf<ChunkPos, MutableSet<Int>>()
    var nodes = mutableSetOf<CircuitPos>()
    var maxPortByBlockPos = mutableMapOf<BlockPos, Int>()

    var networkByPlayer = mutableMapOf<UUID, MutableSet<Int>>()
    var playerWatchingTracker = mutableMapOf<UUID, Int>()

    fun getNetworkId(pos: CircuitPos, color: NetworkColor): CircuitNetwork? {
        val id = networkByCPByColor.getOrPut(color, {mutableMapOf()}).get(pos)
        val net = id?.let { networks[it] }
        if(id != null && net == null) {
            invalidate()
        }
        return net
    }

    fun removeNetwork(vararg nets: CircuitNetwork) {
        nets.forEach {
            networks.remove(it.id)
        }
        invalidate()
    }

    fun addNetwork(net: CircuitNetwork): Int {
        val id = networks.add(net)
        invalidate()
        return id
    }

    var cachePaused = false


    fun invalidate() {
        byCP(NetworkColor.RED).clear()
        byCP(NetworkColor.GREEN).clear()
        networkByChunk.clear()

        networks.id2obj.forEach { k, v ->
            v.rebuildChunks()
            v.chunks.forEach { chunk ->
                networkByChunk(chunk).add(k)
            }
            val m = byCP(v.color)
            v.nodes.forEach { node ->
                m.put(node, k)
                nodes.add(node)
                maxPortByBlockPos.put(node.block, Math.max(node.port, maxPortByBlockPos.get(node.block) ?: 0))
            }
        }
    }

    private fun byCP(color: NetworkColor) = networkByCPByColor.getOrPut(color, ::mutableMapOf)
    private fun networkByChunk(chunk: ChunkPos) = networkByChunk.getOrPut(chunk, ::mutableSetOf)

    @NoSync
    @Save
    var saveListener: Int? = null
        set(value) {
            invalidate()
        }
}
