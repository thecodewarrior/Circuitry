package thecodewarrior.circuitry.network

import com.teamwizardry.librarianlib.features.autoregister.PacketRegister
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.Side
import thecodewarrior.circuitry.internals.SigSet
import thecodewarrior.circuitry.render.CircuitHudRenderHandler

/**
 * Created by TheCodeWarrior
 */
@PacketRegister(Side.CLIENT)
class PacketInspectSync : PacketBase() {
    @Save var value: SigSet? = null

    override fun handle(ctx: MessageContext) {
        CircuitHudRenderHandler.value = this.value
    }

}
