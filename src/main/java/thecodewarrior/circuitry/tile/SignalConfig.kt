package thecodewarrior.circuitry.tile

import com.teamwizardry.librarianlib.features.autoregister.SerializerRegister
import com.teamwizardry.librarianlib.features.kotlin.readString
import com.teamwizardry.librarianlib.features.kotlin.safeCast
import com.teamwizardry.librarianlib.features.kotlin.writeString
import com.teamwizardry.librarianlib.features.saving.FieldType
import com.teamwizardry.librarianlib.features.saving.serializers.Serializer
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import thecodewarrior.circuitry.CircuitryMod
import thecodewarrior.circuitry.internals.SignalType
import java.util.*

/**
 * Created by TheCodeWarrior
 */
sealed class SignalConfig {

    class Number(val num: Int) : SignalConfig()
    class Special(val type: EnumSpecialSignal) : SignalConfig()
    class Signal(val type: SignalType<*>, val key: Int, val count: Int = 1) : SignalConfig()

}

enum class EnumSpecialSignal{
    ANYTHING, EVERYTHING, EACH, NULL;
    val icon = ResourceLocation(CircuitryMod.MODID, "textures/signal/" + this.name.toLowerCase(Locale.ROOT) + ".png")
}

@SerializerRegister(SignalConfig::class)
object SerializeSignalConfig : Serializer<SignalConfig>(FieldType.create(SignalConfig::class.java)) {
    override fun readNBT(nbt: NBTBase, existing: SignalConfig?, syncing: Boolean): SignalConfig {
        val tag = nbt.safeCast(NBTTagCompound::class.java)
        val type = tag.getString("type")
        when(type) {
            "number" -> {
                return SignalConfig.Number(tag.getInteger("value"))
            }
            "special" -> {
                return SignalConfig.Special(EnumSpecialSignal.values()[tag.getInteger("value") % EnumSpecialSignal.values().size])
            }
            "signal" -> {
                val child = tag.getCompoundTag("value")
                val signal = SignalType.REGISTRY.getValue(ResourceLocation(child.getString("signalType")))!!
                return SignalConfig.Signal(signal, child.getInteger("key"), child.getInteger("count"))
            }
        }
        return SignalConfig.Number(0)
    }

    override fun writeNBT(value: SignalConfig, syncing: Boolean): NBTBase {
        val tag = NBTTagCompound()
        when(value) {
            is SignalConfig.Number -> {
                tag.setString("type", "number")
                tag.setInteger("value", value.num)
            }
            is SignalConfig.Special -> {
                tag.setString("type", "special")
                tag.setInteger("value", value.type.ordinal)
            }
            is SignalConfig.Signal -> {
                tag.setString("type", "signal")
                val child = NBTTagCompound()
                tag.setTag("value", child)
                child.setString("signalType", SignalType.REGISTRY.getKey(value.type).toString())
                child.setInteger("key", value.key)
                child.setInteger("count", value.count)
            }
        }
        return tag
    }

    override fun readBytes(buf: ByteBuf, existing: SignalConfig?, syncing: Boolean): SignalConfig {
        when(buf.readByte().toInt()) {
            0 -> { // SignalConfig.Number
                return SignalConfig.Number(buf.readInt())
            }
            1 -> { // SignalConfig.Special
                return SignalConfig.Special(EnumSpecialSignal.values()[buf.readByte().toInt() % EnumSpecialSignal.values().size])
            }
            2 -> { // SignalConfig.Signal
                val signal = SignalType.REGISTRY.getValue(ResourceLocation(buf.readString()))!!
                return SignalConfig.Signal(signal, buf.readInt(), buf.readInt())
            }
        }
        return SignalConfig.Number(0)
    }

    override fun writeBytes(buf: ByteBuf, value: SignalConfig, syncing: Boolean) {
        when(value) {
            is SignalConfig.Number -> {
                buf.writeByte(0)
                buf.writeInt(value.num)
            }
            is SignalConfig.Special -> {
                buf.writeByte(1)
                buf.writeByte(value.type.ordinal)
            }
            is SignalConfig.Signal -> {
                buf.writeByte(2)
                buf.writeString(SignalType.REGISTRY.getKey(value.type).toString())
                buf.writeInt(value.key)
                buf.writeInt(value.count)
            }
        }
    }
}

