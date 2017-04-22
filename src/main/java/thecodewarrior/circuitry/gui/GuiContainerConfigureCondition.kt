package thecodewarrior.circuitry.gui

import com.teamwizardry.librarianlib.features.gui.GuiComponent
import com.teamwizardry.librarianlib.features.gui.components.ComponentSprite
import com.teamwizardry.librarianlib.features.gui.components.ComponentVoid
import com.teamwizardry.librarianlib.features.gui.mixin.DragMixin
import com.teamwizardry.librarianlib.features.guicontainer.ComponentSlot
import com.teamwizardry.librarianlib.features.guicontainer.GuiContainerBase
import com.teamwizardry.librarianlib.features.guicontainer.builtin.BaseLayouts
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.clamp
import com.teamwizardry.librarianlib.features.kotlin.withRealDefault
import com.teamwizardry.librarianlib.features.kotlin.withX
import com.teamwizardry.librarianlib.features.sprite.Sprite
import com.teamwizardry.librarianlib.features.sprite.Texture
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import thecodewarrior.circuitry.CircuitryMod
import thecodewarrior.circuitry.tile.SignalConfig

/**
 * Created by TheCodeWarrior
 */
class GuiContainerConfigureCondition(val container_: ContainerConfigureCondition) : GuiContainerBase(container_, 176, 139) {
    init {
        val compInv = ComponentSprite(inv, 0, 45)
        val compCard = ComponentSprite(card, 53, 0)
        mainComponents.add(compInv, compCard)

        val playerLayout = BaseLayouts.player(container_.playerWrapper)
        playerLayout.mainWrapper.pos = vec(8, 12)
        compInv.add(playerLayout.root)

        val leftSlot = ComponentSlot(container_.configWrapper.left, 5, 20)
        val rightSlot = ComponentSlot(container_.configWrapper.right, 49, 20)
        compCard.add(leftSlot, rightSlot)

        val leftRender = ComponentSignalConfig(5, 20, {container_.left})
        val rightRender = ComponentSignalConfig(49, 20, {container_.right})
        compCard.add(leftRender, rightRender)

        val operator = createOperator()
        operator.pos = vec(31.5, 24.5)
        compCard.add(operator)

        val sideCard = SignalStrengthInbuiltConfigTemplate(container_, 1, {container_.right}, {container_.right is SignalConfig.Number})
        sideCard.textInput.pos = vec(5, 5)
        sideCard.slideTrack.pos = vec(31, -7)
        compCard.add(sideCard.root)
    }

    val sprites = mutableMapOf<ResourceLocation, Sprite>().withRealDefault { Sprite(ResourceLocation(it.resourceDomain, "textures/" + it.resourcePath + ".png")) }

    fun createOperator(): ComponentSprite {
        val comp = ComponentSprite(null, 0, 0, 7, 7)
        comp.BUS.hook(GuiComponent.ComponentTickEvent::class.java) {
            comp.sprite = sprites[container_.operation.inventoryIcon]
        }
        comp.BUS.hook(GuiComponent.MouseClickEvent::class.java) {
            container_.sendButton(0)
        }
        return comp
    }


    companion object {
        val TEXTURE = Texture(ResourceLocation(CircuitryMod.MODID, "textures/gui/config_condition.png"))
        val card = TEXTURE.getSprite("card", 70, 45)
        val inv = TEXTURE.getSprite("inv", 176, 94)
        val slider = TEXTURE.getSprite("slider", 5, 8)
    }

}

class SignalStrengthInbuiltConfigTemplate(val container: IContainerButtonHandler, var buttonId: Int, var signal: () -> SignalConfig, enabled: () -> Boolean) {
    val root = ComponentVoid(0, 0)
    val slideTrack = ComponentVoid(5, -7, 32, 0)
    val slide = ComponentSprite(GuiContainerConfigureCondition.slider, 0, 0)
    val textInput = ComponentTextInput(7, 6, 61, 10)
    val drag: DragMixin<ComponentSprite>

    private var updating = false

    init {
        root.add(slideTrack)
        slideTrack.add(slide)

        drag = DragMixin(slide) {
            vec(it.x.clamp(0.0, slideTrack.size.x), 0)
        }

        root.add(textInput)

        textInput.textField.setValidator(this::validate)

        root.BUS.hook(GuiComponent.ComponentTickEvent::class.java) {
            val wasVisible = root.isVisible
            root.isVisible = enabled()
            if(wasVisible && !root.isVisible) {
                drag.mouseDown = null
            }
        }

        slide.BUS.hook(DragMixin.DragMoveEvent::class.java) {
            val x = it.newPos.x-1
            val num = if(x < 0) 0 else Math.pow(2.0, x).toInt()
            updateNum(num)
        }

        update()
    }

    fun update() {
        val sig = signal()
        if(sig is SignalConfig.Number) {
            updateField(sig.num)
            updateSlide(sig.num)
        } else if(sig is SignalConfig.Signal) {
            updateField(sig.count)
            updateSlide(sig.count)
        } else {
            updateField(0)
            updateSlide(0)
        }
    }

    private fun validate(it: String?): Boolean {
            if(updating) return true
            if(it == null)
                return true
            if(it == "") {
                updateNum(0)
                return true
            }
            val t = if(it.endsWith('k', true) && it.endsWith('m', true) && it.endsWith('g', true)) {
                it.substring(0, it.length-1)
            } else it
            try {
                val i = Integer.parseInt(t)
                val transformed =
                        if(it.endsWith('k', true)) i * 1_000
                        else if(it.endsWith('m', true)) i * 1_000_000
                        else if(it.endsWith('g', true)) i * 1_000_000_000
                        else i
                updateNum(transformed)
                return true
            } catch (e: NumberFormatException) {
                return false
            }
        }

    private fun updateSlide(num: Int) {
        val x = if(num < 2) 0 else MathHelper.log2(num).clamp(0, 31)+1
        slide.pos = slide.pos.withX(x)
    }

    private fun updateField(num: Int) {
        var s = num.toString()
        var postFix = 0
        (0..2).forEach {
            if(s.endsWith("000")) {
                s = s.substring(0, s.length - 3)
                postFix++
            }
        }
        val text = s + arrayOf("", "k", "m", "b")[postFix]
        updating = true
        textInput.textField.text = text
        updating = false
    }

    private fun updateNum(num: Int) {
        container.sendButton(buttonId, num)
        if(drag.mouseDown == null) {
            updateSlide(num)
        }
        if(!textInput.textField.isFocused) {
            updateField(num)
        }
    }
}

