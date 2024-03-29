package xyz.brassgoggledcoders.mccivilizations.eventhandler;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.repository.RepositoryManager;

@EventBusSubscriber(modid = MCCivilizations.MODID, bus = Bus.FORGE)
public class RepositoryEventHandler {
    @SubscribeEvent
    public static void serverStarting(ServerAboutToStartEvent event) {
        if (RepositoryManager.INSTANCE != null) {
            RepositoryManager.INSTANCE.save();
            RepositoryManager.INSTANCE = null;
        }

        RepositoryManager.INSTANCE = new RepositoryManager(event.getServer());
        RepositoryManager.INSTANCE.load();
    }

    @SubscribeEvent
    public static void levelSaving(LevelEvent.Save event) {
        if (RepositoryManager.INSTANCE != null) {
            RepositoryManager.INSTANCE.save();
        }

    }

    @SubscribeEvent
    public static void serverStopping(ServerStoppingEvent serverStoppingEvent) {
        RepositoryManager.INSTANCE.close();
        RepositoryManager.INSTANCE = null;
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
        RepositoryManager.INSTANCE.playerLoggedIn(playerLoggedInEvent.getEntity());
    }
}
