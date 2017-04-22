package thecodewarrior.circuitry

import com.google.common.reflect.TypeToken
import com.teamwizardry.librarianlib.core.LibrarianLib
import com.teamwizardry.librarianlib.features.kotlin.withRealDefault
import com.teamwizardry.librarianlib.features.saving.FieldType
import com.teamwizardry.librarianlib.features.saving.serializers.Serializer
import com.teamwizardry.librarianlib.features.saving.serializers.SerializerRegistry
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NBTBase
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.Vec3d

/**
 * Created by TheCodeWarrior
 */
fun String.translate(vararg args: Any?) = LibrarianLib.PROXY.translate(this, args)

operator fun Vec3d.times(other: EnumFacing) = Vec3d(this.xCoord * other.frontOffsetX, this.yCoord * other.frontOffsetY, this.zCoord * other.frontOffsetZ)

fun EnumFacing.Axis.getQuantity(vec: Vec3d): Double {
    if(this == EnumFacing.Axis.X)
        return vec.xCoord
    if(this == EnumFacing.Axis.Y)
        return vec.yCoord
    if(this == EnumFacing.Axis.Z)
        return vec.zCoord
    return 0.0
}

fun EnumFacing.getQuantity(vec: Vec3d) =
        if(this.axisDirection == EnumFacing.AxisDirection.NEGATIVE)
            -this.axis.getQuantity(vec)
        else
            this.axis.getQuantity(vec)

operator fun EnumFacing.unaryMinus(): EnumFacing = this.opposite

val serializerCache = mutableMapOf<FieldType, Serializer<Any>>()
        .withRealDefault { SerializerRegistry.getOrCreate(it) }

inline fun <reified T : Any> T.autoToNBT(syncing: Boolean = false): NBTBase {
    val token = object : TypeToken<T>() {}
    val type = FieldType.create(token.type)
    return serializerCache[type].write(this, syncing)
}

inline fun <reified T : Any> NBTBase.autoFromNBT(syncing: Boolean = false, existing: T? = null): T {
    val token = object : TypeToken<T>() {}
    val type = FieldType.create(token.type)
    return serializerCache[type].read(this, existing, syncing) as T
}

inline fun <reified T : Any> ByteBuf.autoToBytes(value: T, syncing: Boolean = true) {
    val token = object : TypeToken<T>() {}
    val type = FieldType.create(token.type)
    serializerCache[type].write(this, value, syncing)
}

inline fun <reified T : Any> ByteBuf.autoFromBytes(syncing: Boolean = true, existing: T? = null): T {
    val token = object : TypeToken<T>() {}
    val type = FieldType.create(token.type)
    return serializerCache[type].read(this, existing, syncing) as T
}
