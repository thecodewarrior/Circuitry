package thecodewarrior.circuitry.gui

import com.teamwizardry.librarianlib.features.container.ContainerBase
import com.teamwizardry.librarianlib.features.container.GuiHandler
import com.teamwizardry.librarianlib.features.container.InventoryWrapper
import com.teamwizardry.librarianlib.features.container.SlotType
import com.teamwizardry.librarianlib.features.container.builtin.BaseWrappers
import com.teamwizardry.librarianlib.features.container.internal.SlotBase
import com.teamwizardry.librarianlib.features.kotlin.nbt
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.items.ItemStackHandler
import thecodewarrior.circuitry.CircuitryMod
import thecodewarrior.circuitry.autoFromNBT
import thecodewarrior.circuitry.internals.BaseSignalTypes
import thecodewarrior.circuitry.internals.VirtualSignalType
import thecodewarrior.circuitry.item.ModItems
import thecodewarrior.circuitry.tile.*

/**
 * Created by TheCodeWarrior
 */
class ContainerConfigureCombinator(player: EntityPlayer, val combinator: TileComputingCombinatorBase) : ContainerBase(player), IContainerButtonHandler {

    val playerWrapper = BaseWrappers.player(player)
    val configWrapper = ComputingConfigWrapper(combinator, this)

    var left: SignalConfig = combinator.left
    var right: SignalConfig = combinator.right
    var out: SignalConfig = combinator.out
    var operation: IOperator = combinator.operatorConfig
    var outModeOne: Boolean = (combinator as? TileDeciderCombinator)?.outModeOne ?: false

    init {
        addSlots(playerWrapper)
        addSlots(configWrapper)
    }

    override fun handleButton(button: Int, data: Int) {
        when(button) {
            0 -> {
                val list = combinator.possibleOperators()
                val index = list.indexOf(operation)
                operation = list[(index + 1) % list.size]
            }
            1 -> {
                right = SignalConfig.Number(data)
            }
            2 -> {
                outModeOne = data != 0
            }
        }
    }

    override fun onClosed() {
        combinator.left = left
        combinator.right = right
        combinator.out = out
        combinator.operatorConfig = operation
        (combinator as? TileDeciderCombinator)?.outModeOne = outModeOne
        combinator.markDirty()
    }

    companion object {
        val NAME = ResourceLocation(CircuitryMod.MODID, "configure_computing_combinator")

        init {
            GuiHandler.registerBasicContainer(NAME,
                    { player, _, tile -> ContainerConfigureCombinator(player, tile as TileComputingCombinatorBase) },
                    { _, container -> GuiContainerConfigureCombinator(container) })
        }
    }
}

class ComputingConfigWrapper(val tile: TileComputingCombinatorBase, val container: ContainerConfigureCombinator) : InventoryWrapper(ItemStackHandler(3)) {

    val left = slots[0]
    val right = slots[1]
    val out = slots[2]

    init {
        left.type = object : SlotTypeSignalConfig() {
            override val specials: Array<EnumSpecialSignal>
                get() = tile.getAllowedSpecialLeft()
            override var config: SignalConfig
                get() = container.left
                set(value) {
                    container.left = value
                    val out = container.out
                    if(out is SignalConfig.Special) {
                        val outAllowed = container.combinator.getAllowedSpecialOut(container.left)
                        if(out.type != EnumSpecialSignal.NULL && out.type !in outAllowed)
                            if(outAllowed.isEmpty())
                                container.out = SignalConfig.Special(EnumSpecialSignal.NULL)
                            else
                                container.out = SignalConfig.Special(outAllowed.first())
                    }
                }

        }
        right.type = object : SlotTypeSignalConfig() {
            override val specials: Array<EnumSpecialSignal>
                get() = arrayOf()
            override var config: SignalConfig
                get() = container.right
                set(value) {container.right = value}
        }
        out.type = object : SlotTypeSignalConfig() {
            override val specials: Array<EnumSpecialSignal>
                get() = tile.getAllowedSpecialOut(container.left)
            override var config: SignalConfig
                get() = container.out
                set(value) {container.out = value}
        }
    }

}

abstract class SlotTypeSignalConfig : SlotType() {
    abstract var config: SignalConfig
    abstract val specials: Array<EnumSpecialSignal>
    open fun click() {}

    override fun handleClick(slot: SlotBase, container: ContainerBase, dragType: Int, clickType: ClickType?, player: EntityPlayer): Pair<Boolean, ItemStack> {

        val playerStack: ItemStack = player.inventory.itemStack

        if(playerStack.isEmpty()) { // isempty
            val specialList = specials
            if(dragType == 1) { // right click
                if(clickType == ClickType.QUICK_MOVE)
                    config = SignalConfig.Special(EnumSpecialSignal.NULL)
            } else if(specialList.isNotEmpty()) {
                val index =
                        if(config is SignalConfig.Special)
                            specialList.indexOf((config as SignalConfig.Special).type)
                        else
                            -1
                if(config is SignalConfig.Special || clickType == ClickType.QUICK_MOVE)
                    config = SignalConfig.Special(specialList[(index + 1) % specialList.size])
            } else {
                if(clickType == ClickType.QUICK_MOVE)
                    config = SignalConfig.Number(0)
            }
        } else if(playerStack.item == ModItems.signalCard && playerStack.nbt["key"] != null) {
            config = SignalConfig.Signal(VirtualSignalType, playerStack.nbt["key"]!!.autoFromNBT())
        } else {
            config = SignalConfig.Signal(BaseSignalTypes.ITEMS, BaseSignalTypes.ITEMS.toInt(playerStack))
        }

        click()

        return true to ItemStack.EMPTY
    }
}
