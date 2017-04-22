package thecodewarrior.circuitry.internals

import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

/**
 * Created by TheCodeWarrior
 */
interface ICircuitHandler {
    val portCount: Int

    fun getRenderPos(world: World, pos: BlockPos, color: NetworkColor, port: Int): Vec3d
    fun addOutput(world: World, pos: BlockPos, port: Int, color: NetworkColor, builder: SigSetBuilder)
    fun getPort(world: World, pos: BlockPos, sideClicked: EnumFacing, hitPos: Vec3d): Int
    fun tick(world: World, pos: BlockPos, port: Int)
    fun shouldRefreshWires(world: World, pos: BlockPos, oldState: IBlockState): Boolean {
        return world.getBlockState(pos).block != oldState.block
    }

    fun getNetwork(world: World, pos: BlockPos, port: Int, color: NetworkColor): CircuitNetwork? {
        val cap = world.getCapability(CapabilityCircuitWorld.cap, null) ?: error("World " + world.chunkProvider.makeString() + " (" + world.provider.dimension + ")")
		return cap.getConnectedNetwork(CircuitPos(pos, port), color)
    }

    fun getInput(world: World, pos: BlockPos, port: Int): SigSet {
        val redNet = getNetwork(world, pos, port, NetworkColor.RED)
        val greenNet = getNetwork(world, pos, port, NetworkColor.GREEN)
        val redSig = redNet?.signal
        val greenSig = greenNet?.signal
        return (redSig ?: SigSet.ZERO) + (greenSig ?: SigSet.ZERO)
    }

}

class NullCircuitHandler : ICircuitHandler {
    init {
        TODO("Null circuit handler instantiated!")
    }

    override val portCount: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun getRenderPos(world: World, pos: BlockPos, color: NetworkColor, port: Int): Vec3d {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addOutput(world: World, pos: BlockPos, port: Int, color: NetworkColor, builder: SigSetBuilder) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPort(world: World, pos: BlockPos, sideClicked: EnumFacing, hitPos: Vec3d): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun tick(world: World, pos: BlockPos, port: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
