package thecodewarrior.circuitry.tile

import com.teamwizardry.librarianlib.features.base.block.TileMod
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.minus
import com.teamwizardry.librarianlib.features.kotlin.plus
import com.teamwizardry.librarianlib.features.kotlin.times
import com.teamwizardry.librarianlib.features.kotlin.unaryMinus
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import thecodewarrior.circuitry.getQuantity
import thecodewarrior.circuitry.internals.CircuitryAPI
import thecodewarrior.circuitry.internals.ICircuitHandler

/**
 * Created by TheCodeWarrior
 */
abstract class TileCombinator : TileMod(), ICircuitHandler {
    @Save var attachedSide: EnumFacing = EnumFacing.DOWN
    @Save var facing: EnumFacing = EnumFacing.NORTH
    val perpendicular: EnumFacing
        get() {
            val fil = EnumFacing.Axis.values().filter { it != attachedSide.axis }.indexOf(facing.axis) +
                    (if(facing.axisDirection != attachedSide.axisDirection) 1 else 0) +
                    (if(attachedSide.axis == EnumFacing.Axis.Y) 1 else 0)
            val paxisdirection = arrayOf(EnumFacing.AxisDirection.NEGATIVE, EnumFacing.AxisDirection.POSITIVE)[fil % 2]
            return EnumFacing.values().find {
                it.axis != facing.axis && it.axis != attachedSide.axis &&
                        it.axisDirection == paxisdirection
            }!!
        }

    abstract val bounds: AxisAlignedBB
    abstract fun getPortTransformed(sideClicked: EnumFacing, vec: Vec3d): Int
    abstract fun openGui(player: EntityPlayer)

    fun transform(v: Vec3d): Vec3d {
        val x = Vec3d(this.perpendicular.directionVec)
        val y = -Vec3d(this.attachedSide.directionVec)
        val z = Vec3d(this.facing.directionVec)

        return vec(0.5, 0.5, 0.5) + x*(v.xCoord/16) + y*(v.yCoord/16-0.5) + z*(v.zCoord/16)
    }

    override fun getPort(world: World, pos: BlockPos, sideClicked: EnumFacing, hitPos: Vec3d): Int {
        val hitPos = hitPos - vec(0.5, 0.5, 0.5)
        val perpendicularAxis = perpendicular.getQuantity(hitPos)
        val attachedAxis = -attachedSide.getQuantity(hitPos)
        val facingAxis = facing.getQuantity(hitPos)

        return getPortTransformed(sideClicked, vec(perpendicularAxis, attachedAxis, facingAxis))
    }

    fun getBlockBounds(): AxisAlignedBB {
        val min = transform(vec(bounds.minX, bounds.minY, bounds.minZ))
        val max = transform(vec(bounds.maxX, bounds.maxY, bounds.maxZ))

        return AxisAlignedBB(min.xCoord, min.yCoord, min.zCoord, max.xCoord, max.yCoord, max.zCoord)
    }

    override fun tick(world: World, pos: BlockPos, port: Int) {}

    override fun <T : Any> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
        if(capability == CircuitryAPI.CIRCUIT_HANDLER_CAPABILITY)
            return this as T
        return super.getCapability(capability, facing)
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
        if(capability == CircuitryAPI.CIRCUIT_HANDLER_CAPABILITY)
            return true
        return super.hasCapability(capability, facing)
    }
}

