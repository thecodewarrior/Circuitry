package thecodewarrior.circuitry.network

import com.teamwizardry.librarianlib.features.autoregister.PacketRegister
import com.teamwizardry.librarianlib.features.container.internal.ContainerImpl
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.Side
import thecodewarrior.circuitry.gui.ContainerConfigureCondition
import thecodewarrior.circuitry.internals.Condition

/**
 * Created by TheCodeWarrior
 */
@PacketRegister(Side.CLIENT)
class PacketConditionSync : PacketBase() {
    @Save var conditions: Array<Condition>? = null

    override fun handle(ctx: MessageContext) {
        val container = (Minecraft.getMinecraft().player.openContainer as? ContainerImpl)?.container as? ContainerConfigureCondition
        if(container != null) {
            container.onSyncFromServer(conditions)
        } else {
            ContainerConfigureCondition.conditionsClient = conditions
        }
    }
}
