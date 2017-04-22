package thecodewarrior.circuitry.internals

import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry
import net.minecraftforge.fml.common.registry.RegistryBuilder
import thecodewarrior.circuitry.CircuitryMod
import java.util.*

/**
 * Created by TheCodeWarrior
 */
object VirtualSignalType : SignalType<VirtualSignal>(ResourceLocation(CircuitryMod.MODID, "virtual")) {
    override fun tooltip(key: VirtualSignal): List<String> { return mutableListOf() }

    override fun toInt(value: VirtualSignal): Int {
        return REGISTRY.getIDForObject(value)
    }
    override fun fromInt(value: Int): VirtualSignal {
        return REGISTRY.getObjectById(value)
    }
    override fun render(key: VirtualSignal, x: Int, y: Int) {
        drawIcon(key.icon, x, y)
    }

    @Suppress("UNCHECKED_CAST")
    val REGISTRY = RegistryBuilder<VirtualSignal>().setName(ResourceLocation(CircuitryMod.MODID, "virtual_signals")).setIDRange(0, 65535).setType(VirtualSignal::class.java).also { builder ->
        val field = RegistryBuilder::class.java.declaredFields.find { it.name == "optionalDefaultKey" }!!
        field.isAccessible = true
        field.set(builder, ResourceLocation(CircuitryMod.MODID, "error"))
    }.create() as FMLControlledNamespacedRegistry<VirtualSignal>

    init {
        val sig = VirtualSignal(
                ResourceLocation(CircuitryMod.MODID, "error"),
                ResourceLocation(CircuitryMod.MODID, "textures/signal/error.png"),
                "circuitry:signal.virtual.error.name",
                "unlisted"
        )
    }

    fun registerEnum(enum: Class<out Enum<*>>, modid: String) {
        enum.enumConstants.forEach {
            val elemName = it.name.toLowerCase(Locale.ROOT)
            val typeName = enum.simpleName.toLowerCase(Locale.ROOT)
            val signal = VirtualSignal(
                    ResourceLocation(modid, "${typeName}_$elemName"),
                    ResourceLocation(modid, "textures/signal/$typeName/$elemName.png"),
                    "$modid:signal.virtual.enum.$typeName.$elemName.name",
                    "$modid:signal.virtual.enum.$typeName.name"
                    )

            REGISTRY.register(signal)
        }
    }

}

class VirtualSignal(
        name: ResourceLocation,
        val icon: ResourceLocation,
        val unlocalizedName: String,
        val unlocalizedGroup: String,
        val tooltip: Array<String> = arrayOf()
) : IForgeRegistryEntry<VirtualSignal> {
    var name: ResourceLocation = name
        private set

    override fun getRegistryType(): Class<in VirtualSignal> { return this.javaClass }

    override fun setRegistryName(name: ResourceLocation?): VirtualSignal {
        if(name != null)
            this.name = name
        return this
    }

    override fun getRegistryName(): ResourceLocation? {
        return name
    }
}
