package thecodewarrior.circuitry.network

import com.teamwizardry.librarianlib.features.autoregister.PacketRegister
import com.teamwizardry.librarianlib.features.kotlin.nbt
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.Side
import thecodewarrior.circuitry.autoToNBT
import thecodewarrior.circuitry.item.ModItems

/**
 * Created by TheCodeWarrior
 */
@PacketRegister(Side.SERVER)
class PacketSelectSignal : PacketBase() {
    @Save var key: Int = -1

    override fun handle(ctx: MessageContext) {
        EnumHand.values().forEach {
            val stack = ctx.serverHandler.playerEntity.getHeldItem(it)
            if(stack.item != ModItems.signalCard) return@forEach
            if(key == -1) {
                stack.nbt["key"] = null
            } else {
                stack.nbt["key"] = key.autoToNBT()
            }
        }
    }

}
