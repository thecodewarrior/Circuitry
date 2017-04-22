package thecodewarrior.circuitry.network

import com.teamwizardry.librarianlib.features.autoregister.PacketRegister
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.Side
import thecodewarrior.circuitry.internals.CircuitNetwork
import thecodewarrior.circuitry.render.CircuitRenderHandler

/**
 * Created by TheCodeWarrior
 */
@PacketRegister(Side.CLIENT)
class PacketNetworkSync : PacketBase() {
    @Save var id: Int = 0

    @Save var network: CircuitNetwork? = null

    override fun handle(ctx: MessageContext) {
        CircuitRenderHandler.setNetwork(id, network)
    }

}
