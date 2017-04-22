package thecodewarrior.circuitry.gui

import com.google.common.collect.HashMultimap
import com.teamwizardry.librarianlib.features.gui.GuiBase
import com.teamwizardry.librarianlib.features.gui.GuiComponent
import com.teamwizardry.librarianlib.features.gui.components.*
import com.teamwizardry.librarianlib.features.gui.mixin.DragMixin
import com.teamwizardry.librarianlib.features.gui.mixin.ScissorMixin
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.*
import com.teamwizardry.librarianlib.features.math.Vec2d
import com.teamwizardry.librarianlib.features.network.PacketHandler
import com.teamwizardry.librarianlib.features.sprite.Texture
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import thecodewarrior.circuitry.CircuitryMod
import thecodewarrior.circuitry.internals.SignalType
import thecodewarrior.circuitry.internals.VirtualSignal
import thecodewarrior.circuitry.internals.VirtualSignalType
import thecodewarrior.circuitry.network.PacketSelectSignal

/**
 * Created by TheCodeWarrior
 */
class GuiSignalSelect() : GuiBase(195, 136) {

    val TEXTURE = Texture(ResourceLocation(CircuitryMod.MODID, "textures/gui/signal_select.png"))
    val signal_bg = TEXTURE.getSprite("signal_bg", 195, 136)
    val slot = TEXTURE.getSprite("slot", 18, 18)
    val scroll = TEXTURE.getSprite("scroll", 12, 15)

    init {
        val signalView = ComponentSprite(signal_bg, 0, 0)
        val search = ComponentTextInput(81, 5, 88, 10)
        mainComponents.add(signalView, search)

        val scrollView = ScrollTemplate()
        scrollView.vScrollBounds.pos = vec(181, 25)
        scrollView.vScrollBounds.size = vec(0, 95)

        scrollView.vScrollHandle.add(ComponentSprite(scroll, -6, -7))

        scrollView.bounds.pos = vec(4.5, 9)
        scrollView.bounds.size = vec(160, 110)
        scrollView.content.pos = vec(4.5, 9)
        signalView.add(scrollView.root)

        val map = HashMultimap.create<String, VirtualSignal>()
        VirtualSignalType.REGISTRY.forEach {
            map.put(it.unlocalizedGroup, it)
        }
        map.removeAll("unlisted")

        val list = ComponentList(0, 0)
        scrollView.content.add(list)
        map.asMap().toSortedMap().forEach {
            addGroup(list, it.key, it.value)
        }
    }

    private fun addGroup(list: ComponentList, key: String, value: Collection<VirtualSignal>) {
        val comp = ComponentVoid(0, 0)
        list.add(comp)

        val text = ComponentText(5, 5)
        text.text.setValue(key.localize())
        comp.add(text)

        val grid = ComponentGrid(7, 15, 18, 18, 8)
        comp.add(grid)
        value.sortedBy { it.unlocalizedName.localize() }.forEach { signal ->
            val c = ComponentSignal(VirtualSignalType, signal, 0, 0)
            c.BUS.hook(GuiComponent.MouseClickEvent::class.java) {
                select(signal)
            }
            grid.add(c)
        }
    }

    private fun select(signal: VirtualSignal) {
        Minecraft.getMinecraft().displayGuiScreen(null)
        PacketHandler.NETWORK.sendToServer(PacketSelectSignal().also { it.key = VirtualSignalType.toInt(signal) })
    }
}

class ComponentSignal<out T: SignalType<*>>(val signalType: T, val key: Any, posX: Int, posY: Int) : GuiComponent<ComponentSignal<SignalType<*>>>(posX, posY, 16, 16) {
    override fun drawComponent(mousePos: Vec2d, partialTicks: Float) {
        GlStateManager.color(1f, 1f, 1f, 1f)
        (signalType as SignalType<Any>).render(key, pos.xi, pos.yi)
        if(mouseOver)
            setTooltip(signalType.tooltip(key))
    }
}

class ScrollTemplate(vertical: Boolean = true, horizontal: Boolean = false) {
    val root = ComponentVoid(0, 0)
    val bounds = ComponentVoid(0, 0)
    val content = ComponentVoid(0, 0)
    val vScrollBounds = ComponentVoid(0, 0)
    val vScrollHandle = ComponentVoid(0, 0)
    val hScrollBounds = ComponentVoid(0, 0)
    val hScrollHandle = ComponentVoid(0, 0)

    init {
        root.add(bounds)
        if(vertical) root.add(vScrollBounds)
        if(horizontal) root.add(hScrollBounds)
        bounds.add(content)
        vScrollBounds.add(vScrollHandle)
        hScrollBounds.add(hScrollHandle)

        ScissorMixin.scissor(bounds)
        DragMixin(vScrollHandle, { vec(0, it.y.clamp(0.0, vScrollBounds.size.y)) })
        DragMixin(hScrollHandle, { vec(it.x.clamp(0.0, hScrollBounds.size.x), 0) })

        vScrollHandle.BUS.hook(DragMixin.DragMoveEvent::class.java) { e ->
            val scrollAmount = outOfBoundsAmount.y
            val scrollPercent = e.pos.y / vScrollBounds.size.y
            vScroll = (scrollPercent * scrollAmount).toInt()
        }
        hScrollHandle.BUS.hook(DragMixin.DragMoveEvent::class.java) { e ->
            val scrollAmount = outOfBoundsAmount.x
            val scrollPercent = e.pos.x / vScrollBounds.size.x
            hScroll = (scrollPercent * scrollAmount).toInt()
        }
    }

    val outOfBoundsAmount: Vec2d
        get() {
            val v = (content.getLogicalSize()?.max ?: vec(0, 0)) + content.pos*2 + bounds.pos*2 - bounds.size
            return vec(Math.max(0.0, v.x), Math.max(0.0, v.y))
        }

    var vScroll: Int
        get() = -content.childTranslation.yi
        set(value) {
            val scrollAmount = outOfBoundsAmount.yi
            content.childTranslation = content.childTranslation.withY(-( if(value > scrollAmount) scrollAmount else value ))
        }
    var hScroll: Int
        get() = -content.childTranslation.xi
        set(value) {
            val scrollAmount = outOfBoundsAmount.yi
            content.childTranslation = content.childTranslation.withY(-( if(value > scrollAmount) scrollAmount else value ))
        }
}

class ComponentTextInput(posX: Int, posY: Int, width: Int, height: Int) : GuiComponent<ComponentTextInput>(posX, posY, width, height) {
    val textField = GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, 0, 0, width, height)

    override fun drawComponent(mousePos: Vec2d, partialTicks: Float) {
        GlStateManager.translate(pos.x, pos.y, 0.0)
        textField.width = size.xi
        textField.height = size.yi
        textField.drawTextBox()
        GlStateManager.translate(-pos.x, -pos.y, 0.0)
    }

    init {
        this.textField.enableBackgroundDrawing = false
        this.BUS.hook(ComponentTickEvent::class.java) {
            this.textField.updateCursorCounter();
        }
        this.BUS.hook(KeyDownEvent::class.java) {
            if(this.textField.textboxKeyTyped(it.key, it.keyCode)) {
                it.cancel()
            }
        }
        this.BUS.hook(MouseClickEvent::class.java) {
            this.textField.mouseClicked(it.mousePos.xi, it.mousePos.yi, it.button.mouseCode)
        }
        this.BUS.hook(MouseDownEvent::class.java) {
            this.textField.setFocused(this.mouseOver)
        }
    }


}
