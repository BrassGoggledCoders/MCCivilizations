package xyz.brassgoggledcoders.mccivilizations.eventhandler;


import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.network.queue.ClientNetworkQueue;

@EventBusSubscriber(modid = MCCivilizations.MODID, bus = Bus.FORGE)
public class NetworkEventHandler {
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent clientTickEvent) {
        ClientNetworkQueue.getInstance()
                .doQueueTick();
    }
}
