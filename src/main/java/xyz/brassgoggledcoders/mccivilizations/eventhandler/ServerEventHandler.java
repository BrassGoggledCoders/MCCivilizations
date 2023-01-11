package xyz.brassgoggledcoders.mccivilizations.eventhandler;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.service.CivilizationServices;
import xyz.brassgoggledcoders.mccivilizations.civilization.CivilizationsSavedData;
import xyz.brassgoggledcoders.mccivilizations.service.CivilizationServiceProvider;

@EventBusSubscriber(modid = MCCivilizations.MODID, bus = Bus.FORGE)
public class ServerEventHandler {
    @SubscribeEvent
    public static void serverStarting(ServerStartedEvent event) {
        if (CivilizationServices.getProvider() instanceof CivilizationServiceProvider provider) {
            ServerLevel serverLevel = event.getServer()
                    .getLevel(Level.OVERWORLD);
            if (serverLevel != null) {
                provider.setServerCivilizations(serverLevel.getDataStorage()
                        .computeIfAbsent(
                                CivilizationsSavedData::new,
                                CivilizationsSavedData::new,
                                "civilizations"
                        )
                );
            } else {
                MCCivilizations.LOGGER.error("Failed to Find Overworld");
            }

        }
    }
}
