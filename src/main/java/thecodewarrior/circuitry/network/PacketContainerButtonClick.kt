package thecodewarrior.circuitry.network

import com.teamwizardry.librarianlib.features.autoregister.PacketRegister
import com.teamwizardry.librarianlib.features.container.internal.ContainerImpl
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.Side
import thecodewarrior.circuitry.gui.IContainerButtonHandler

/**
 * Created by TheCodeWarrior
 */
@PacketRegister(Side.SERVER)
class PacketContainerButtonClick : PacketBase() {
    @Save var button: Int? = null
    @Save var data: Int = 0

    override fun handle(ctx: MessageContext) {
        val container = (ctx.serverHandler.playerEntity.openContainer as? ContainerImpl)?.container ?: return

        if(button == null || container !is IContainerButtonHandler) { return }

        container.handleButton(button!!, data)
    }
}
