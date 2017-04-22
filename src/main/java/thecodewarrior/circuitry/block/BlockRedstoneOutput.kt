package thecodewarrior.circuitry.block

import com.teamwizardry.librarianlib.features.base.block.BlockModDirectional
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.minus
import com.teamwizardry.librarianlib.features.kotlin.plus
import com.teamwizardry.librarianlib.features.kotlin.times
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import thecodewarrior.circuitry.internals.CircuitryAPI
import thecodewarrior.circuitry.internals.ICircuitConditionHolder
import thecodewarrior.circuitry.internals.NetworkColor
import thecodewarrior.circuitry.internals.SigSetBuilder

/**
 * Created by TheCodeWarrior
 */
class BlockRedstoneOutput(val isOn: Boolean) : BlockModDirectional("redstone_out_" + if(isOn) "on" else "off", Material.IRON, false), ICircuitConditionHolder {
    override val conditionCount: Int = 1
    override val portCount: Int = 1

    override fun createItemForm(): ItemBlock? {
        if(isOn) return ModBlocks.redstone_output_off.itemForm
        return super.createItemForm()
    }

    override fun getRenderPos(world: World, pos: BlockPos, color: NetworkColor, port: Int): Vec3d {
        return vec(0.5, 0.5, 0.5)
    }

    override fun addOutput(world: World, pos: BlockPos, port: Int, color: NetworkColor, builder: SigSetBuilder) {}

    override fun getPort(world: World, pos: BlockPos, sideClicked: EnumFacing, hitPos: Vec3d): Int { return 0 }

    override fun tick(world: World, pos: BlockPos, port: Int) {
        val enabled = isEnabled(world, pos)
        if(enabled != isOn) {
            val block = if(enabled) ModBlocks.redstone_output_on else ModBlocks.redstone_output_off
            val facing = world.getBlockState(pos).getValue(property)
            val newState = block.defaultState.withProperty(block.property, facing)
            world.setBlockState(pos, newState)
        }
    }

    override fun canProvidePower(state: IBlockState?): Boolean {
        return true
    }

    override fun getStrongPower(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Int {
        val facing = blockState.getValue(property)
        if(!isOn || side != facing)
            return 0
        return 15
    }

    override fun getWeakPower(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Int {
        val facing = blockState.getValue(property)
        if(!isOn || side != facing)
            return 0
        return 15
    }

    fun isEnabled(world: World, pos: BlockPos): Boolean {
        val conditions = CircuitryAPI.getConditions(world, pos) ?: return false
        return conditions[0].apply(getInput(world, pos, 0)).passed
    }

    override fun shouldRefreshConditions(world: World, pos: BlockPos, oldState: IBlockState): Boolean {
        if(oldState.block == ModBlocks.redstone_output_off || oldState.block == ModBlocks.redstone_output_on)
            return false
        return super.shouldRefreshConditions(world, pos, oldState)
    }

    override fun shouldRefreshWires(world: World, pos: BlockPos, oldState: IBlockState): Boolean {
        if(oldState.block == ModBlocks.redstone_output_off || oldState.block == ModBlocks.redstone_output_on)
            return false
        return super.shouldRefreshWires(world, pos, oldState)
    }

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB {
        val facing = state.getValue(property)
        val r = 4.0
        val h = 8
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
