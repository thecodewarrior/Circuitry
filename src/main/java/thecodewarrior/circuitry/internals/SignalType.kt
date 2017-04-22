package thecodewarrior.circuitry.internals

import com.teamwizardry.librarianlib.features.autoregister.SerializerFactoryRegister
import com.teamwizardry.librarianlib.features.kotlin.readString
import com.teamwizardry.librarianlib.features.kotlin.safeCast
import com.teamwizardry.librarianlib.features.kotlin.writeString
import com.teamwizardry.librarianlib.features.saving.FieldType
import com.teamwizardry.librarianlib.features.saving.serializers.Serializer
import com.teamwizardry.librarianlib.features.saving.serializers.SerializerFactory
import com.teamwizardry.librarianlib.features.saving.serializers.SerializerFactoryMatch
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagString
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.IForgeRegistry
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry
import net.minecraftforge.fml.common.registry.RegistryBuilder
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11
import thecodewarrior.circuitry.CircuitryMod

/**
 * Created by TheCodeWarrior
 */
abstract class SignalType<T>(var name: ResourceLocation) : IForgeRegistryEntry<SignalType<*>> {
    override fun getRegistryName() = name
    override fun getRegistryType() = this.javaClass as Class<SignalType<*>>
    override fun setRegistryName(name: ResourceLocation): SignalType<T> {
        this.name = name
        return this
    }

    abstract fun toInt(value: T): Int
    abstract fun fromInt(value: Int): T
    @SideOnly(Side.CLIENT)
    abstract fun render(key: T, x: Int, y: Int)
    abstract fun tooltip(key: T): List<String>

    protected fun drawIcon(texture: ResourceLocation, x: Int, y: Int) {
        val tess = Tessellator.getInstance()
        val vb = tess.buffer

        val xMin = x.toDouble()
        val yMin = y.toDouble()
        val xMax = xMin + 16
        val yMax = yMin + 16

        Minecraft.getMinecraft().renderEngine.bindTexture(texture)

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        vb.pos(xMin, yMax, 0.0).tex(0.0, 1.0).endVertex()
        vb.pos(xMax, yMax, 0.0).tex(1.0, 1.0).endVertex()
        vb.pos(xMax, yMin, 0.0).tex(1.0, 0.0).endVertex()
        vb.pos(xMin, yMin, 0.0).tex(0.0, 0.0).endVertex()
        tess.draw()
    }

    init {
        @Suppress("LeakingThis") REGISTRY.register(this)
    }

    companion object {

        val REGISTRY: IForgeRegistry<SignalType<*>> = RegistryBuilder<SignalType<*>>().setName(ResourceLocation("circuitry:signaltypes")).setIDRange(0, 65535).setType(SignalType::class.java).also { builder ->
            val field = RegistryBuilder::class.java.declaredFields.find { it.name == "optionalDefaultKey" }!!
            field.isAccessible = true
            field.set(builder, ResourceLocation(CircuitryMod.MODID, "error"))
        }.create()

    }


}

@SerializerFactoryRegister
object SerializerSignalTypeFactory : SerializerFactory("SerializerType") {
    override fun canApply(type: FieldType): SerializerFactoryMatch {
        return canApplySubclass(type, SignalType::class.java)
    }

    override fun create(type: FieldType): Serializer<*> {
        return SerializeSignalType
    }

}

object SerializeSignalType : Serializer<SignalType<*>>(FieldType.create(SignalType::class.java)) {
    override fun readNBT(nbt: NBTBase, existing: SignalType<*>?, syncing: Boolean): SignalType<*> {
        val tag = nbt.safeCast(NBTTagString::class.java)
        return SignalType.REGISTRY.getValue(ResourceLocation(tag.string))!!
    }

    override fun writeNBT(value: SignalType<*>, syncing: Boolean): NBTBase {
        val tag = NBTTagString(value.registryName.toString())
        return tag
    }

    override fun readBytes(buf: ByteBuf, existing: SignalType<*>?, syncing: Boolean): SignalType<*> {
        return SignalType.REGISTRY.getValue(ResourceLocation(buf.readString()))!!
    }

    override fun writeBytes(buf: ByteBuf, value: SignalType<*>, syncing: Boolean) {
        buf.writeString(value.registryName.toString())
    }
}
