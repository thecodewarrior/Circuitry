package thecodewarrior.circuitry.block

import com.teamwizardry.librarianlib.features.base.block.BlockModContainer
import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import thecodewarrior.circuitry.item.ItemBlockCombinator
import thecodewarrior.circuitry.tile.TileCombinator

/**
 * Created by TheCodeWarrior
 */
abstract class BlockCombinator(type: String) : BlockModContainer("combinator_$type", Material.IRON) {
    companion object {
        val ATTACHED_SIDE = PropertyEnum.create("attached", EnumFacing::class.java)
        val FACING = PropertyEnum.create("facing", EnumFacing::class.java)
    }

    abstract override fun createTileEntity(world: World, state: IBlockState): TileCombinator
    open fun addProps(props: Array<IProperty<*>>): Array<IProperty<*>> { return props }

    override fun onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        (worldIn.getTileEntity(pos) as? TileCombinator)?.openGui(playerIn)
        return true
    }

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB {
        val tile = source.getTileEntity(pos) as? TileCombinator ?: return FULL_BLOCK_AABB
        return tile.getBlockBounds()
    }

    override fun createItemForm(): ItemBlock? {
        return ItemBlockCombinator(this)
    }

    override fun getActualState(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): IBlockState {
        val tile = worldIn.getTileEntity(pos) as? TileCombinator ?: return state
        return state.withProperty(ATTACHED_SIDE, tile.attachedSide).withProperty(FACING, tile.facing)
    }

    override fun createBlockState(): BlockStateContainer {
        return BlockStateContainer(this, *addProps(arrayOf(ATTACHED_SIDE, FACING)))
    }

    override fun getMetaFromState(state: IBlockState?): Int {
        return 0
    }

    override fun getStateFromMeta(meta: Int): IBlockState {
        return defaultState
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

