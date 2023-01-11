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
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.service.CivilizationServices;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.claim.IClaimedLand;

import java.util.EnumSet;

@ClientPlugin
public class JourneyMapPlugin implements IClientPlugin {
    private IBlockInfo lastPosition;

    @Override
    public void initialize(@NotNull IClientAPI clientAPI) {
        clientAPI.subscribe(this.getModId(), EnumSet.of(ClientEvent.Type.MAP_MOUSE_MOVED));
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
        if (lastPosition != null && lastPosition.getChunkPos() != null) {
            ModPopupMenu civilizationsMenu = popupMenuEvent.getPopupMenu()
                    .createSubItemList("Civilizations");
            IClaimedLand claimedLand = CivilizationServices.getClaimedLand(Minecraft.getInstance().level);
            Civilization chunkCivilization = claimedLand.getClaimOwner(lastPosition.getChunkPos());
            Civilization userCivilization = CivilizationServices.getCivilizationService(Minecraft.getInstance().level)
                    .getCivilizationByCitizen(Minecraft.getInstance().player);

            if (userCivilization == null) {
                civilizationsMenu.addMenuItem("Must be a Citizen of a Civilization", blockPos -> {

                });
            } else if (chunkCivilization == null) {
                civilizationsMenu.addMenuItem("Claim Chunk For ".formatted(), blockPos -> {

                });
            } else if (chunkCivilization.equals(userCivilization)) {
                civilizationsMenu.addMenuItem("Un-claim Chunk", blockPos -> {

                });
            } else {
                civilizationsMenu.addMenuItem("Can't interact with other Civilizations", blockPos -> {

                });
            }

        }
    }

    private void onMouseMove(FullscreenMapEvent.MouseMoveEvent mouseMoveEvent) {
        this.lastPosition = mouseMoveEvent.getInfo();
    }
}