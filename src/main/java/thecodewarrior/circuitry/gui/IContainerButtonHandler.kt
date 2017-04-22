package thecodewarrior.circuitry.gui

import com.teamwizardry.librarianlib.features.network.PacketHandler
import com.teamwizardry.librarianlib.features.utilities.client.ClientRunnable
import thecodewarrior.circuitry.network.PacketContainerButtonClick

/**
 * Created by TheCodeWarrior
 */
interface IContainerButtonHandler {
    fun handleButton(button: Int, data: Int)

    fun sendButton(button: Int, data: Int = 0) {
        ClientRunnable.run {
            PacketHandler.NETWORK.sendToServer(PacketContainerButtonClick().also { it.button = button; it.data = data})
        }
        this.handleButton(button, data)
    }
}
