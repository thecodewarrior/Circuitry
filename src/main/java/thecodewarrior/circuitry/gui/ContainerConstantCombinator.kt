package thecodewarrior.circuitry.gui

import com.teamwizardry.librarianlib.features.container.ContainerBase
import com.teamwizardry.librarianlib.features.container.GuiHandler
import com.teamwizardry.librarianlib.features.container.InventoryWrapper
import com.teamwizardry.librarianlib.features.container.builtin.BaseWrappers
import com.teamwizardry.librarianlib.features.kotlin.clamp
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.items.ItemStackHandler
import thecodewarrior.circuitry.CircuitryMod
import thecodewarrior.circuitry.tile.EnumSpecialSignal
import thecodewarrior.circuitry.tile.SignalConfig
import thecodewarrior.circuitry.tile.TileConstantCombinator

/**
 * Created by TheCodeWarrior
 */
class ContainerConstantCombinator(player: EntityPlayer, val combinator: TileConstantCombinator) : ContainerBase(player), IContainerButtonHandler {

    val playerWrapper = BaseWrappers.player(player)
    val configWrapper = ConstantConfigWrapper(combinator, this)

    var signals: Array<SignalConfig> = combinator.signals.copyOf()
    var selected: Int = 0

    init {
        addSlots(playerWrapper)
        addSlots(configWrapper)
    }

    override fun handleButton(button: Int, data: Int) {
        when(button) {
            -1 -> {
                selected = data.clamp(0, TileConstantCombinator.signalCount-1)
            }
            in 0..TileConstantCombinator.signalCount-1 -> {
                val sig = signals[button]
                if(sig is SignalConfig.Number) {
                    signals[button] = SignalConfig.Number(data)
                }
                if(sig is SignalConfig.Signal) {
                    signals[button] = SignalConfig.Signal(sig.type, sig.key, data)
                }
            }
        }
    }

    override fun onClosed() {
        signals.forEachIndexed { index, signalConfig ->
            combinator.signals[index] = signalConfig
        }
        combinator.markDirty()
    }

    companion object {
        val NAME = ResourceLocation(CircuitryMod.MODID, "configure_constant_combinator")

        init {
            GuiHandler.registerBasicContainer(NAME,
                    { player, _, tile -> ContainerConstantCombinator(player, tile as TileConstantCombinator) },
                    { _, container -> GuiContainerConstantCombinator(container) })
        }
    }
}


class ConstantConfigWrapper(val tile: TileConstantCombinator, val container: ContainerConstantCombinator) : InventoryWrapper(ItemStackHandler(TileConstantCombinator.signalCount)) {

    val all = slots[0..TileConstantCombinator.signalCount-1]

    init {
        all.forEachIndexed { i, slot ->
            slot.type = object : SlotTypeSignalConfig() {
                override val specials: Array<EnumSpecialSignal>
                    get() = arrayOf(EnumSpecialSignal.NULL)
                override var config: SignalConfig
                    get() = container.signals[i]
                    set(value) { container.signals[i] = value }

                override fun click() {
                    container.selected = i
                }
            }
        }
    }

}
