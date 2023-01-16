package xyz.brassgoggledcoders.mccivilizations.eventhandler;

import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsItemTags;

@EventBusSubscriber(modid = MCCivilizations.MODID, bus = Bus.FORGE)
public class CivilizationEventHandler {

    @SubscribeEvent
    public static void anvilUpdate(AnvilUpdateEvent event) {
        boolean leftNoCost = event.getLeft().is(MCCivilizationsItemTags.NO_COST_NAMING) && event.getRight().isEmpty();
        boolean rightNoCost = event.getLeft().isEmpty() && event.getRight().is(MCCivilizationsItemTags.NO_COST_NAMING);

        if (leftNoCost != rightNoCost) {
            event.setCost(0);
        }
    }
}
