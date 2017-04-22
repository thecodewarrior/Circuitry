package thecodewarrior.circuitry

import com.teamwizardry.librarianlib.features.base.ModCreativeTab
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import thecodewarrior.circuitry.proxy.Common

/**
 * Created by TheCodeWarrior
 */
@Mod(modid = CircuitryMod.MODID, version = CircuitryMod.VERSION, name = CircuitryMod.MODNAME, dependencies = CircuitryMod.DEPENDENCIES, modLanguageAdapter = CircuitryMod.ADAPTER, acceptedMinecraftVersions = CircuitryMod.ALLOWED)
object CircuitryMod {

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        PROXY.pre(e)
    }

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        PROXY.init(e)
    }

    @Mod.EventHandler
    fun postInit(e: FMLPostInitializationEvent) {
        PROXY.post(e)
    }

    const val MODID = "circuitry"
    const val MODNAME = "Circuitry"
    const val VERSION = "1.0"
    const val ALLOWED = "[1.11,)"
    const val CLIENT = "thecodewarrior.circuitry.proxy.Client"
    const val SERVER = "thecodewarrior.circuitry.proxy.Common"
    const val DEPENDENCIES = "required-after:librarianlib"
    const val ADAPTER = "net.shadowfacts.forgelin.KotlinAdapter"

    @SidedProxy(clientSide = CLIENT, serverSide = SERVER)
    lateinit var PROXY: Common

    val creativeTab = object : ModCreativeTab() {
        init {
            this.registerDefaultTab()
        }
        override val iconStack: ItemStack by lazy {
            ItemStack(Items.REDSTONE) // TODO: Change to red wire coil
        }

    }
}
