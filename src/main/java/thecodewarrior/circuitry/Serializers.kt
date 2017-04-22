package thecodewarrior.circuitry

import com.teamwizardry.librarianlib.features.autoregister.SerializerRegister
import com.teamwizardry.librarianlib.features.kotlin.safeCast
import com.teamwizardry.librarianlib.features.saving.FieldType
import com.teamwizardry.librarianlib.features.saving.serializers.Serializer
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.ChunkPos

/**
 * Created by TheCodeWarrior
 */
@SerializerRegister(ChunkPos::class)
object SerializeChunkPos : Serializer<ChunkPos>(FieldType.create(ChunkPos::class.java)) {
    override fun readNBT(nbt: NBTBase, existing: ChunkPos?, syncing: Boolean): ChunkPos {
        val tag = nbt.safeCast(NBTTagCompound::class.java)
        return ChunkPos(tag.getInteger("x"), tag.getInteger("z"))
    }

    override fun writeNBT(value: ChunkPos, syncing: Boolean): NBTBase {
        val tag = NBTTagCompound()
        tag.setInteger("x", value.chunkXPos)
        tag.setInteger("z", value.chunkZPos)
        return tag
    }

    override fun readBytes(buf: ByteBuf, existing: ChunkPos?, syncing: Boolean): ChunkPos {
        return ChunkPos(buf.readInt(), buf.readInt())
    }

    override fun writeBytes(buf: ByteBuf, value: ChunkPos, syncing: Boolean) {
        buf.writeInt(value.chunkXPos)
        buf.writeInt(value.chunkZPos)
    }
}
