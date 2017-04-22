package thecodewarrior.circuitry.item

import com.teamwizardry.librarianlib.features.base.block.ItemModBlock
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.*
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import thecodewarrior.circuitry.tile.TileCombinator

/**
 * Created by TheCodeWarrior
 */
class ItemBlockCombinator(block: Block) : ItemModBlock(block) {
    override fun placeBlockAt(stack: ItemStack, player: EntityPlayer, world: World, pos: BlockPos, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, newState: IBlockState): Boolean {
        if(!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState))
            return false

        val tile = world.getTileEntity(pos) as? TileCombinator ?: return true // true because block _was_ placed, it just had an issue

        val attached = side.opposite

        var hitVector = player.lookVec * 0.2 + vec(hitX, hitY, hitZ) - vec(0.5, 0.5, 0.5)

        if(side.axis == EnumFacing.Axis.X) hitVector = hitVector.withX(0)
        if(side.axis == EnumFacing.Axis.Y) hitVector = hitVector.withY(0)
        if(side.axis == EnumFacing.Axis.Z) hitVector = hitVector.withZ(0)

        val facing = EnumFacing.getFacingFromVector(hitVector.xCoord.toFloat(), hitVector.yCoord.toFloat(), hitVector.zCoord.toFloat())

        tile.attachedSide = attached
        tile.facing = facing

        return true
    }
}
