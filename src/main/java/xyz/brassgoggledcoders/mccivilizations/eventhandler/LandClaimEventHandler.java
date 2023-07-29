package xyz.brassgoggledcoders.mccivilizations.eventhandler;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsEntityTags;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;

@EventBusSubscriber(modid = MCCivilizations.MODID, bus = Bus.FORGE)
public class LandClaimEventHandler {

    @SubscribeEvent
    public static void mobGriefingEvent(EntityMobGriefingEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Enemy && !entity.getType().is(MCCivilizationsEntityTags.IGNORES_CLAIMS)) {
            if (CivilizationRepositories.getLandClaimRepository().isClaimed(entity.getLevel().dimension(), entity.chunkPosition())) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void enteringChunk(EntityEvent.EnteringSection event) {
        if (event.didChunkChange() && event.getEntity() instanceof Player player && !player.getLevel().isClientSide()) {
            ILandClaimRepository claimedLand = CivilizationRepositories.getLandClaimRepository();
            ResourceKey<Level> levelKey = player.getLevel().dimension();
            Civilization lastChunkCiv = claimedLand.getClaimOwner(levelKey, event.getOldPos().chunk());
            Civilization newChunkCiv = claimedLand.getClaimOwner(levelKey, event.getNewPos().chunk());

            if (lastChunkCiv != newChunkCiv) {
                if (lastChunkCiv != null) {
                    player.sendSystemMessage(MCCivilizationsText.translate(MCCivilizationsText.LEAVING_CIVILIZATION, lastChunkCiv.getName()));
                }
                if (newChunkCiv != null) {
                    player.sendSystemMessage(MCCivilizationsText.translate(MCCivilizationsText.ENTERING_CIVILIZATION, newChunkCiv.getName()));
                }
            }
        }
    }
}
