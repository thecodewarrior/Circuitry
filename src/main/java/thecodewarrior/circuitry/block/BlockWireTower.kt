package thecodewarrior.circuitry.block

import com.teamwizardry.librarianlib.features.base.block.BlockModDirectional
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.minus
import com.teamwizardry.librarianlib.features.kotlin.plus
import com.teamwizardry.librarianlib.features.kotlin.times
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import thecodewarrior.circuitry.internals.ICircuitHandler
import thecodewarrior.circuitry.internals.NetworkColor
import thecodewarrior.circuitry.internals.SigSetBuilder
import thecodewarrior.circuitry.times

/**
 * Created by TheCodeWarrior
 */
class BlockWireTower : BlockModDirectional("wire_tower", Material.IRON, false), ICircuitHandler {
    override val portCount: Int = 1

    override fun getRenderPos(world: World, pos: BlockPos, color: NetworkColor, port: Int): Vec3d {
        val state = world.getBlockState(pos)
        val facing = state.getValue(property)

        val l = if(color == NetworkColor.GREEN) 0.2 else 0.25
        return vec(0.5, 0.5, 0.5) + Vec3d(facing.directionVec) * vec(l, l, l)
    }

    override fun addOutput(world: World, pos: BlockPos, port: Int, color: NetworkColor, builder: SigSetBuilder) {
        // NOOP
    }

    override fun getPort(world: World, pos: BlockPos, sideClicked: EnumFacing, hitPos: Vec3d): Int {
        return 0
    }

    override fun tick(world: World, pos: BlockPos, port: Int) {}

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB {
        val facing = state.getValue(property)
        val r = 1.5
        val h = 12
        var dir = Vec3d(facing.directionVec)
        var side = vec(8, 8, 8) - dir * 8
        var inside = side + dir * h
        side += dir*r
        inside -= dir*r

        var aabb = AxisAlignedBB(side.xCoord, side.yCoord, side.zCoord, inside.xCoord, inside.yCoord, inside.zCoord).expandXyz(r)
        aabb = AxisAlignedBB(aabb.minX/16, aabb.minY/16, aabb.minZ/16, aabb.maxX/16, aabb.maxY/16, aabb.maxZ/16)
        return aabb
    }

    override fun getLightOpacity(state: IBlockState?) = 0
    override fun isFullBlock(state: IBlockState?) = false
    override fun isFullCube(state: IBlockState?) = false
    override fun isFullyOpaque(state: IBlockState?) = false
    override fun isOpaqueCube(state: IBlockState?) = false
    override fun isBlockNormalCube(state: IBlockState?) = false
    override fun isBlockSolid(worldIn: IBlockAccess?, pos: BlockPos?, side: EnumFacing?) = false
    override fun isNormalCube(state: IBlockState?) = false
    override fun isNormalCube(state: IBlockState?, world: IBlockAccess?, pos: BlockPos?) = false
    override fun isSideSolid(base_state: IBlockState?, world: IBlockAccess?, pos: BlockPos?, side: EnumFacing?) = false
}
