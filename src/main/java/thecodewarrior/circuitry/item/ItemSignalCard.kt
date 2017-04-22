package thecodewarrior.circuitry.item

import com.teamwizardry.librarianlib.features.base.item.ItemMod
import com.teamwizardry.librarianlib.features.kotlin.localize
import com.teamwizardry.librarianlib.features.kotlin.nbt
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumHand
import net.minecraft.world.World
import thecodewarrior.circuitry.autoFromNBT
import thecodewarrior.circuitry.gui.GuiSignalSelect
import thecodewarrior.circuitry.internals.VirtualSignalType

/**
 * Created by TheCodeWarrior
 */
class ItemSignalCard : ItemMod("signal_card") {
    override fun onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack> {

        if(world.isRemote)
            Minecraft.getMinecraft().displayGuiScreen(GuiSignalSelect())

        return super.onItemRightClick(world, player, hand)
    }

    override fun addInformation(stack: ItemStack, playerIn: EntityPlayer, tooltip: MutableList<String>, advanced: Boolean) {
        val key = stack.nbt["key"]
        if(key == null) {
            tooltip.add("circuitry:item.signal_card.tooltip.no_config".localize())
        } else {
            val k = key.autoFromNBT<Int>()
            tooltip.add("circuitry:item.signal_card.tooltip.config".localize(VirtualSignalType.fromInt(k).unlocalizedName.localize()))
            if(advanced) {
                tooltip.add("circuitry:item.signal_card.tooltip.config_advanced".localize(k))
            }
        }
    }
}
