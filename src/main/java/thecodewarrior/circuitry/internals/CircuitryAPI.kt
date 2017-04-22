package thecodewarrior.circuitry.internals

import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager

/**
 * Created by TheCodeWarrior
 */
object CircuitryAPI {
    @JvmStatic
    @CapabilityInject(ICircuitHandler::class)
    lateinit var CIRCUIT_HANDLER_CAPABILITY: Capability<ICircuitHandler>

    @JvmStatic
    fun getCircuitHandler(world: World, pos: BlockPos): ICircuitHandler? {
        val block = world.getBlockState(pos).block
        if(block is ICircuitHandler)
            return block

        val tile = world.getTileEntity(pos) ?: return null

        if(tile.hasCapability(CIRCUIT_HANDLER_CAPABILITY, null))
            return tile.getCapability(CIRCUIT_HANDLER_CAPABILITY, null)

        return null
    }

    @JvmStatic
    fun getConditions(world: World, pos: BlockPos): Array<Condition>? {
        if(world.isRemote) return null
        return world.getCapability(CapabilityCircuitWorld.cap, null)?.getConditions(pos)
    }

    init {
        CapabilityManager.INSTANCE.register(ICircuitHandler::class.java, object : Capability.IStorage<ICircuitHandler> {
            override fun writeNBT(capability: Capability<ICircuitHandler>, instance: ICircuitHandler, side: EnumFacing?): NBTBase {
                return NBTTagCompound()
            }

            override fun readNBT(capability: Capability<ICircuitHandler>, instance: ICircuitHandler, side: EnumFacing?, nbt: NBTBase?) {
            }

        }, NullCircuitHandler::class.java)
    }
}
