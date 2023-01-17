package xyz.brassgoggledcoders.mccivilizations.eventhandler;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;

@EventBusSubscriber(modid = MCCivilizations.MODID, bus = Bus.FORGE)
public class LandClaimEventHandler {

    @SubscribeEvent
    public static void mobGriefingEvent(EntityMobGriefingEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Enemy) {
            if (CivilizationRepositories.getLandClaimRepository().isClaimed(entity.chunkPosition())) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void enteringChunk(EntityEvent.EnteringSection event) {
        if (event.didChunkChange() && event.getEntity() instanceof Player player) {
            ILandClaimRepository claimedLand = CivilizationRepositories.getLandClaimRepository();
            Civilization lastChunkCiv = claimedLand.getClaimOwner(event.getOldPos().chunk());
            Civilization newChunkCiv = claimedLand.getClaimOwner(event.getNewPos().chunk());

            if (lastChunkCiv != newChunkCiv) {
                if (newChunkCiv != null) {
                    player.sendSystemMessage(Component.translatable(MCCivilizationsText.ENTERING_CIVILIZATION.getString(), newChunkCiv.getName()));
                } else {
                    player.sendSystemMessage(Component.translatable(MCCivilizationsText.LEAVING_CIVILIZATION.getString(), lastChunkCiv.getName()));
                }
            }
        }
    }
}
