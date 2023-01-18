package xyz.brassgoggledcoders.mccivilizations.compat.journeymap;

import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.ModPopupMenu;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.FullscreenMapEvent;
import journeymap.client.api.event.forge.PopupMenuEvent;
import journeymap.client.api.model.IBlockInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;
import xyz.brassgoggledcoders.mccivilizations.network.ChangeType;
import xyz.brassgoggledcoders.mccivilizations.network.LandClaimUpdatePacket;
import xyz.brassgoggledcoders.mccivilizations.network.NetworkHandler;

import java.util.Collections;
import java.util.EnumSet;

@ClientPlugin
public class JourneyMapPlugin implements IClientPlugin {
    private IBlockInfo lastPosition;

    @Override
    public void initialize(@NotNull IClientAPI clientAPI) {
        clientAPI.subscribe(this.getModId(), EnumSet.of(ClientEvent.Type.MAP_MOUSE_MOVED, ClientEvent.Type.MAPPING_STARTED,
                ClientEvent.Type.MAPPING_STOPPED, ClientEvent.Type.DISPLAY_UPDATE));
        MinecraftForge.EVENT_BUS.addListener(this::onPopup);
    }

    @Override
    public String getModId() {
        return MCCivilizations.MODID;
    }

    @Override
    public void onEvent(@NotNull ClientEvent clientEvent) {
        if (clientEvent instanceof FullscreenMapEvent.MouseMoveEvent mouseMoveEvent) {
            this.onMouseMove(mouseMoveEvent);
        }
    }

    private void onPopup(PopupMenuEvent popupMenuEvent) {
        Player player = Minecraft.getInstance().player;
        if (lastPosition != null && lastPosition.getChunkPos() != null && player != null) {
            ModPopupMenu civilizationsMenu = popupMenuEvent.getPopupMenu()
                    .createSubItemList("Civilizations");
            ILandClaimRepository claimedLand = CivilizationRepositories.getLandClaimRepository();
            Civilization chunkCivilization = claimedLand.getClaimOwner(lastPosition.getChunkPos());
            Civilization userCivilization = CivilizationRepositories.getCivilizationRepository()
                    .getCivilizationByCitizen(player);

            if (userCivilization == null) {
                civilizationsMenu.addMenuItem(MCCivilizationsText.CITIZENSHIP_REQUIRED.getString(), blockPos -> {

                });
            } else if (chunkCivilization == null) {
                civilizationsMenu.addMenuItem(MCCivilizationsText.CLAIM_CHUNK.getString(), blockPos ->
                        NetworkHandler.getInstance().sendPacketToServer(new LandClaimUpdatePacket(
                                userCivilization.getId(),
                                Collections.singletonList(new ChunkPos(blockPos)),
                                ChangeType.ADD
                        ))
                );
            } else if (chunkCivilization.equals(userCivilization)) {
                civilizationsMenu.addMenuItem(MCCivilizationsText.UNCLAIM_CHUNK.getString(), blockPos ->
                        NetworkHandler.getInstance().sendPacketToServer(new LandClaimUpdatePacket(
                                userCivilization.getId(),
                                Collections.singletonList(new ChunkPos(blockPos)),
                                ChangeType.DELETE
                        ))
                );
            }
        }
    }

    private void onMouseMove(FullscreenMapEvent.MouseMoveEvent mouseMoveEvent) {
        this.lastPosition = mouseMoveEvent.getInfo();
    }
}
