package thecodewarrior.circuitry.item

import com.teamwizardry.librarianlib.features.base.item.ItemMod
import com.teamwizardry.librarianlib.features.container.GuiHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import thecodewarrior.circuitry.gui.ContainerConfigureCondition
import thecodewarrior.circuitry.internals.CircuitryAPI
import thecodewarrior.circuitry.internals.ICircuitConditionHolder

/**
 * Created by TheCodeWarrior
 */
class ItemConditionConfigurator : ItemMod("condition_configurator") {
    override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
        if(CircuitryAPI.getCircuitHandler(world, pos) is ICircuitConditionHolder) {
            GuiHandler.open(ContainerConfigureCondition.NAME, player, pos)
            return EnumActionResult.SUCCESS
        }
        return EnumActionResult.PASS
    }
}
