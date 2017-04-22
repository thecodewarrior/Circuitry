package thecodewarrior.circuitry.internals

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import thecodewarrior.circuitry.CircuitryMod

/**
 * Created by TheCodeWarrior
 */
object BaseSignalTypes {
    val ITEMS: SignalType<ItemStack>
    val VIRTUAL: SignalType<VirtualSignal> = VirtualSignalType
    val ERROR: SignalType<Any>

    init {
        ITEMS = object : SignalType<ItemStack>(ResourceLocation(CircuitryMod.MODID, "items")) {
            override fun tooltip(key: ItemStack): List<String> {
                val list = mutableListOf<String>()
                key.item.addInformation(key, Minecraft.getMinecraft().player, list, false)
                return list
            }

            override fun toInt(value: ItemStack): Int {
                val itemId = Item.getIdFromItem(value.item) and 0xFFFF
                val meta = if(value.item.hasSubtypes) value.metadata and 0xFFFF else 0
                return (itemId shl 16) or meta
            }

            override fun fromInt(value: Int): ItemStack {
                val itemId = (value and (0xFFFF shl 16)) shr 16
                val meta = value and 0xFFFF
                return ItemStack(Item.getItemById(itemId), 1, meta)
            }

            override fun render(key: ItemStack, x: Int, y: Int) {
                RenderHelper.enableGUIStandardItemLighting()
                GlStateManager.enableRescaleNormal()

                val itemRender = Minecraft.getMinecraft().renderItem
                itemRender.zLevel = 200.0f

                itemRender.renderItemAndEffectIntoGUI(key, x, y)

                itemRender.zLevel = 0.0f
                GlStateManager.disableRescaleNormal()
                RenderHelper.disableStandardItemLighting()
            }
        }
        val obj = Any()
        ERROR = object : SignalType<Any>(ResourceLocation(CircuitryMod.MODID, "error")) {
            override fun tooltip(key: Any): List<String> { return mutableListOf() }

            override fun toInt(value: Any) = 0
            override fun fromInt(value: Int) = obj
            override fun render(key: Any, x: Int, y: Int) {
                drawIcon(ResourceLocation(CircuitryMod.MODID, "textures/signal/error.png"), x, y)
            }
        }

        VirtualSignalType.registerEnum(AlphaNumericSignal::class.java, CircuitryMod.MODID)
        VirtualSignalType.registerEnum(EnumDyeColor::class.java, CircuitryMod.MODID)
    }
}

enum class AlphaNumericSignal {
    N0, N1, N2, N3, N4, N5, N6, N7, N8, N9,
    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
}


