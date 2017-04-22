package thecodewarrior.circuitry.render

import com.teamwizardry.librarianlib.features.gui.GuiComponent
import com.teamwizardry.librarianlib.features.gui.GuiOverlay
import com.teamwizardry.librarianlib.features.math.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import thecodewarrior.circuitry.internals.SigSet
import thecodewarrior.circuitry.internals.SignalType
import java.util.function.BooleanSupplier
import java.util.function.Consumer

/**
 * Created by TheCodeWarrior
 */
object CircuitHudRenderHandler {
    var value: SigSet? = null

    init {
        GuiOverlay.getOverlayComponent(BooleanSupplier {
            value != null && value?.size ?: 0 != 0
        }, Consumer<GuiComponent<*>> {
            initComponents(it)
        })
    }

    fun initComponents(component: GuiComponent<*>) {
        component.add(ComponentCircuit(20, 20))
    }

}

class ComponentCircuit(posX: Int, posY: Int) : GuiComponent<ComponentCircuit>(posX, posY) {
    override fun drawComponent(mousePos: Vec2d, partialTicks: Float) {
        val sigSet = CircuitHudRenderHandler.value ?: return

        val fr = Minecraft.getMinecraft().fontRendererObj
        val oldFlag = fr.unicodeFlag
        fr.unicodeFlag = true

        val columns = 5
        val scale = 1.0f

        var col = 0
        var row = 0

        sigSet.forEach { signal ->
            val type = signal.type as SignalType<Any>
            type.render(type.fromInt(signal.key), col * 18, row * 18)

            GlStateManager.disableDepth()
            GlStateManager.scale(scale, scale, 1f)

            val str = shorten(signal.value)
            val w = fr.getStringWidth(str)
            fr.drawStringWithShadow(str, (col * 18 + 17f)/scale - w, (row * 18 + 10f)/scale, 0xFFFFFF)
            col++
            if(col > columns) {
                col = 0
                row++
            }

            GlStateManager.scale(1/scale, 1/scale, 1f)
            GlStateManager.enableDepth()
        }

        fr.unicodeFlag = oldFlag
    }

    fun shorten(num: Int): String {
        val absNum = Math.abs(num)
        if(absNum >= 1_000_000_000) {
            return formatShortVersion(num, 1_000_000_000, 'b')
        }
        if(absNum >= 1_000_000) {
            return formatShortVersion(num, 1_000_000, 'm')
        }
        if(absNum >= 1_000) {
            return formatShortVersion(num, 1_000, 'k')
        }
        return num.toString()
    }

    fun formatShortVersion(num: Int, divisor_: Int, postfix: Char): String {
        val divisor = divisor_.toFloat()
        val maxLen = 4

        val precut = String.format("%.5f", num / divisor)
        return precut.substring(0, Math.min(maxLen, precut.length)) + postfix
    }
}
