package thecodewarrior.circuitry.gui

import com.teamwizardry.librarianlib.features.container.ContainerBase
import com.teamwizardry.librarianlib.features.container.GuiHandler
import com.teamwizardry.librarianlib.features.container.InventoryWrapper
import com.teamwizardry.librarianlib.features.container.builtin.BaseWrappers
import com.teamwizardry.librarianlib.features.guicontainer.GuiContainerBase
import com.teamwizardry.librarianlib.features.network.PacketHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.items.ItemStackHandler
import thecodewarrior.circuitry.CircuitryMod
import thecodewarrior.circuitry.internals.CircuitryAPI
import thecodewarrior.circuitry.internals.Condition
import thecodewarrior.circuitry.network.PacketConditionSync
import thecodewarrior.circuitry.tile.EnumBooleanOperation
import thecodewarrior.circuitry.tile.EnumSpecialSignal
import thecodewarrior.circuitry.tile.SignalConfig

/**
 * Created by TheCodeWarrior
 */
class ContainerConfigureCondition(player: EntityPlayer, var conditions: Array<Condition>?) : ContainerBase(player), IContainerButtonHandler {

    val playerWrapper = BaseWrappers.player(player)
    val configWrapper = ConditionConfigWrapper(this)

    var selected: Int = 0

    // lateinit because kt doesn't detect the initialization in a method called from init
    lateinit var left: SignalConfig
    lateinit var right: SignalConfig
    lateinit var operation: EnumBooleanOperation

    init {
        addSlots(playerWrapper)
        addSlots(configWrapper)

        if(conditions == null) {
            conditions = conditionsClient
            conditionsClient = null
        }
        updateFromConditions()

        if(!player.world.isRemote && player is EntityPlayerMP) {
            PacketHandler.NETWORK.sendTo(PacketConditionSync().also { it.conditions = conditions }, player)
        }
    }

    override fun handleButton(button: Int, data: Int) {
        when(button) {
            0 -> {
                val list = EnumBooleanOperation.values()
                val index = list.indexOf(operation)
                operation = list[(index + 1) % list.size]
            }
            1 -> {
                right = SignalConfig.Number(data)
            }
            2 -> {
                updateToConditions()
                selected = data % (conditions?.size ?: 1)
                updateFromConditions()
            }
        }
    }

    fun onSyncFromServer(conditions: Array<Condition>?) {
        this.conditions = conditions
        updateFromConditions()
    }

    fun updateFromConditions() {
        left = conditions?.get(selected)?.left ?: SignalConfig.Special(EnumSpecialSignal.NULL)
        right = conditions?.get(selected)?.right ?: SignalConfig.Special(EnumSpecialSignal.NULL)
        operation = conditions?.get(selected)?.operation ?: EnumBooleanOperation.EQUAL
    }

    fun updateToConditions() {
        if(player.world.isRemote) return
        conditions?.get(selected)?.left = left
        conditions?.get(selected)?.right = right
        conditions?.get(selected)?.operation = operation
    }

    override fun onClosed() {
        updateToConditions()
    }

    companion object {
        val NAME = ResourceLocation(CircuitryMod.MODID, "configure_condition")

        var conditionsClient: Array<Condition>? = null

        init {
            val rawServer: (EntityPlayer, World, BlockPos) -> ContainerConfigureCondition = { player, world, pos ->
                ContainerConfigureCondition(player, CircuitryAPI.getConditions(world, pos))
            }

            val rawClient: (EntityPlayer, World, BlockPos) -> GuiContainerBase = { player, world, pos ->
                GuiContainerConfigureCondition(rawServer(player, world, pos))
            }

            GuiHandler.registerRaw(NAME, rawServer, rawClient)
        }
    }
}

class ConditionConfigWrapper(val container: ContainerConfigureCondition) : InventoryWrapper(ItemStackHandler(2)) {

    val left = slots[0]
    val right = slots[1]

    init {
        left.type = object : SlotTypeSignalConfig() {
            override val specials: Array<EnumSpecialSignal>
                get() = arrayOf(EnumSpecialSignal.ANYTHING, EnumSpecialSignal.EVERYTHING)
            override var config: SignalConfig
                get() = container.left
                set(value) { container.left = value }

        }
        right.type = object : SlotTypeSignalConfig() {
            override val specials: Array<EnumSpecialSignal>
                get() = arrayOf()
            override var config: SignalConfig
                get() = container.right
                set(value) {container.right = value}
        }
    }
}

