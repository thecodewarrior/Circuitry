package thecodewarrior.circuitry.item

import com.teamwizardry.librarianlib.features.base.item.ItemMod
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.nbt
import com.teamwizardry.librarianlib.features.kotlin.safeCast
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagLong
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import thecodewarrior.circuitry.autoFromNBT
import thecodewarrior.circuitry.autoToNBT
import thecodewarrior.circuitry.internals.CapabilityCircuitWorld
import thecodewarrior.circuitry.internals.CircuitPos
import thecodewarrior.circuitry.internals.CircuitryAPI
import thecodewarrior.circuitry.internals.NetworkColor
import thecodewarrior.circuitry.translate
import java.util.*

/**
 * Created by TheCodeWarrior
 */
class ItemWire : ItemMod("wire", *NetworkColor.values().map { "wire_" + it.name.toLowerCase(Locale.ENGLISH) }.toTypedArray()) {
    override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
        val stack = player.getHeldItem(hand)
        val nbt = stack.nbt
        val tag = nbt["pos"]
        if(CircuitryAPI.getCircuitHandler(world, pos) != null && player.isSneaking) {
            val thisPos = CircuitPos(pos, CircuitryAPI.getCircuitHandler(world, pos)?.getPort(world, pos, facing, vec(hitX, hitY, hitZ)) ?: 0)
            if (tag != null) {
                val otherPos: CircuitPos = tag.autoFromNBT()
                if (otherPos.block != pos && !world.isRemote) {
                    val cap = world.getCapability(CapabilityCircuitWorld.cap, null) ?: return EnumActionResult.FAIL
                    cap.addConnection(netColor(stack),
                            otherPos,
                            thisPos
                    )
                }
            }
            nbt["pos"] = thisPos.autoToNBT()
            return EnumActionResult.SUCCESS
        } else {
            nbt["pos"] = null
            return EnumActionResult.PASS
        }
    }

    override fun onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack> {
        val nbt = player.getHeldItem(hand).nbt
        nbt["pos"] = null
        return ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand))
    }

    fun netColor(stack: ItemStack): NetworkColor {
        return NetworkColor.values()[stack.metadata % NetworkColor.values().size]
    }

    override fun addInformation(stack: ItemStack, playerIn: EntityPlayer?, tooltip: MutableList<String>, advanced: Boolean) {
        val tag = stack.nbt["pos"]
        if(tag != null) {
            val blockPos = BlockPos.fromLong(tag.safeCast<NBTTagLong>().long)
            tooltip.add("circuitry:item.wire.pos_tooltip".translate(blockPos.x, blockPos.y, blockPos.z))
        }
    }
}
