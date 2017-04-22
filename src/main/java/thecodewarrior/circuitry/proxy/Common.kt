package thecodewarrior.circuitry.proxy

import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.ifCap
import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.event.world.ChunkWatchEvent
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import thecodewarrior.circuitry.CircuitryMod
import thecodewarrior.circuitry.block.ModBlocks
import thecodewarrior.circuitry.internals.*
import thecodewarrior.circuitry.item.ModItems

/**
 * Created by TheCodeWarrior
 */
open class Common {

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    open fun pre(e: FMLPreInitializationEvent) {
        CircuitryAPI
        CapabilityCircuitWorld
        BaseSignalTypes
        ModBlocks
        ModItems
    }

    open fun init(e: FMLInitializationEvent) {

    }

    open fun post(e: FMLPostInitializationEvent) {

    }

    @SubscribeEvent
    fun attachWorld(e: AttachCapabilitiesEvent<World>) {
        if(e.`object` is WorldServer)
            CapabilityCircuitWorld(e.`object` as WorldServer).attach(e)
    }

    @SubscribeEvent
    fun worldTick(e: TickEvent.WorldTickEvent) {
        val world = e.world
        if(!world.isRemote) {
            world.ifCap(CapabilityCircuitWorld.cap, null) {
                it.tick()
            }
        }
    }

    @SubscribeEvent
    fun chunkLoad(e: ChunkEvent.Load) {
        val world = e.world
        if(!world.isRemote) {
            world.ifCap(CapabilityCircuitWorld.cap, null) {
                it.checkChunkLoaded(e.chunk.xPosition, e.chunk.zPosition)
            }
        }
    }

    @SubscribeEvent
    fun chunkUnload(e: ChunkEvent.Unload) {
        val world = e.world
        if(!world.isRemote) {
            world.ifCap(CapabilityCircuitWorld.cap, null) {
                it.checkChunkLoaded(e.chunk.xPosition, e.chunk.zPosition)
            }
        }
    }

    @SubscribeEvent
    fun enterLoadDistance(e: ChunkWatchEvent.Watch) {
        e.player.world.getCapability(CapabilityCircuitWorld.cap, null)?.also {
            it.sendChunkToPlayer(e.chunk, e.player)
        }
    }

    @SubscribeEvent
    fun unwatch(e: ChunkWatchEvent.UnWatch) {
        e.player.world.getCapability(CapabilityCircuitWorld.cap, null)?.also {
            it.unwatch(e.chunk, e.player)
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    fun attachTile(e: AttachCapabilitiesEvent<TileEntity>) {
        if(hasCapAnySide(e, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) && !hasCap(e, CircuitryAPI.CIRCUIT_HANDLER_CAPABILITY, null)) {
            e.addCapability(ResourceLocation(CircuitryMod.MODID, "itemHandler"), CapabilityItemHandlerCircuitHandler(e.`object`))
        }
    }

    fun <T: ICapabilityProvider> hasCapAnySide(e: AttachCapabilitiesEvent<T>, capability: Capability<*>): Boolean {
        return hasCap(e, capability, null) || EnumFacing.values().any { hasCap(e, capability, it) }
    }

    fun <T: ICapabilityProvider> hasCap(e: AttachCapabilitiesEvent<T>, capability: Capability<*>, side: EnumFacing?): Boolean {
        if(e.`object`.hasCapability(capability, side))
            return true
        if(e.capabilities.values.any { it.hasCapability(capability, side) })
            return true
        return false
    }
}
open class CapabilityItemHandlerCircuitHandler(val tileEntity: TileEntity) : ICapabilitySerializable<NBTTagCompound>, ICircuitHandler {
    override val portCount: Int
        get() = 1

    fun doCap(lambda: (IItemHandler) -> Unit) {
        val set = mutableSetOf<IItemHandler>()
        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)?.also {
            lambda(it)
            set.add(it)
        }
//        EnumFacing.values().forEach { side ->
//            tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)?.also {
//                if(it !in set)
//                    lambda(it)
//                set.add(it)
//            }
//        }
    }

    override fun getRenderPos(world: World, pos: BlockPos, color: NetworkColor, port: Int): Vec3d {
        return vec(0.5, 0.5, 0.5)
    }

    override fun getPort(world: World, pos: BlockPos, sideClicked: EnumFacing, hitPos: Vec3d): Int {
        return 0
    }

    override fun addOutput(world: World, pos: BlockPos, port: Int, color: NetworkColor, builder: SigSetBuilder) {
        val type = BaseSignalTypes.ITEMS
        doCap { cap ->
            for(i in 0..cap.slots-1) {
                val stack = cap.getStackInSlot(i)
                builder.add(type, stack, stack.getCount())
            }
        }
        val noop = 0
    }

    override fun tick(world: World, pos: BlockPos, port: Int) {}

    override fun serializeNBT(): NBTTagCompound {
        return NBTTagCompound()
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {}

    override fun shouldRefreshWires(world: World, pos: BlockPos, oldState: IBlockState): Boolean {
        return world.getTileEntity(pos) !== tileEntity
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
        return if (capability == CircuitryAPI.CIRCUIT_HANDLER_CAPABILITY) this as T else null
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
        return capability == CircuitryAPI.CIRCUIT_HANDLER_CAPABILITY
    }

}
