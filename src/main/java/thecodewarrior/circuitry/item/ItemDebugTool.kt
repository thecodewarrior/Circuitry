package thecodewarrior.circuitry.item

import com.teamwizardry.librarianlib.features.base.item.ItemMod
import com.teamwizardry.librarianlib.features.kotlin.ifCap
import com.teamwizardry.librarianlib.features.kotlin.nbt
import com.teamwizardry.librarianlib.features.network.PacketHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagByte
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import thecodewarrior.circuitry.block.ModBlocks
import thecodewarrior.circuitry.internals.CapabilityCircuitWorld
import thecodewarrior.circuitry.internals.CircuitryAPI
import thecodewarrior.circuitry.internals.NetworkColor
import thecodewarrior.circuitry.network.PacketInspectSync

/**
 * Created by TheCodeWarrior
 */
class ItemDebugTool : ItemMod("circuit_debugger") {
    override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
        val stack = player.getHeldItem(hand)
        if(world.getBlockState(pos).block != ModBlocks.wire_tower)
            return EnumActionResult.PASS
        val handler = CircuitryAPI.getCircuitHandler(world, pos)!!
        if(world.isRemote) return EnumActionResult.SUCCESS

        val color = if(stack.nbt["green"] != null)
            NetworkColor.GREEN
        else
            NetworkColor.RED
        val id = handler.getNetwork(world, pos, 0, color)?.id
        world.ifCap(CapabilityCircuitWorld.cap, null) { cap ->
            if(id == null) {
                if(player is EntityPlayerMP)
                    PacketHandler.NETWORK.sendTo(PacketInspectSync(), player)
                cap.networkTracker.playerWatchingTracker.remove(player.uniqueID)
            } else {
                cap.networkTracker.playerWatchingTracker.put(player.uniqueID, id)
            }
        }
        return EnumActionResult.SUCCESS
    }

    override fun onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack> {
        if(!world.isRemote) {
            world.ifCap(CapabilityCircuitWorld.cap, null) { cap ->
                cap.networkTracker.playerWatchingTracker.remove(player.uniqueID)
                if(player is EntityPlayerMP)
                    PacketHandler.NETWORK.sendTo(PacketInspectSync(), player)
            }
        }
        val stack = player.getHeldItem(hand)
        if(player.isSneaking) {
            stack.nbt["green"] = if(stack.nbt["green"] == null) NBTTagByte(0.toByte()) else null
        }
        return ActionResult(EnumActionResult.SUCCESS, stack)
    }
}
