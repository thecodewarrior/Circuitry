package thecodewarrior.circuitry.proxy

import com.teamwizardry.librarianlib.features.utilities.client.CustomBlockMapSprites
import net.minecraftforge.event.world.ChunkWatchEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import thecodewarrior.circuitry.render.CircuitHudRenderHandler
import thecodewarrior.circuitry.render.CircuitRenderHandler
import thecodewarrior.circuitry.render.TESRCombinatorDisplay
import thecodewarrior.circuitry.tile.EnumArithmeticOperation
import thecodewarrior.circuitry.tile.EnumBooleanOperation
import thecodewarrior.circuitry.tile.TileArithmeticCombinator
import thecodewarrior.circuitry.tile.TileDeciderCombinator


/**
 * Created by TheCodeWarrior
 */
class Client : Common() {
    override fun pre(e: FMLPreInitializationEvent) {
        super.pre(e)
        CircuitRenderHandler
        CircuitHudRenderHandler

        EnumArithmeticOperation.values().forEach {
            CustomBlockMapSprites.register(it.icon)
        }
        EnumBooleanOperation.values().forEach {
            CustomBlockMapSprites.register(it.icon)
        }

        ClientRegistry.bindTileEntitySpecialRenderer(TileArithmeticCombinator::class.java, TESRCombinatorDisplay)
        ClientRegistry.bindTileEntitySpecialRenderer(TileDeciderCombinator::class.java, TESRCombinatorDisplay)
    }

    override fun init(e: FMLInitializationEvent) {
        super.init(e)

    }

    override fun post(e: FMLPostInitializationEvent) {
        super.post(e)

    }

    @SubscribeEvent
    fun unwatchClient(e: ChunkWatchEvent.UnWatch) {
        CircuitRenderHandler.unwatch(e.chunk)
    }

    @SubscribeEvent
    fun switchWorld(e: WorldEvent.Unload) {
        CircuitRenderHandler.clear()
        CircuitRenderHandler.vbCache = null
        CircuitHudRenderHandler.value = null
    }
}
