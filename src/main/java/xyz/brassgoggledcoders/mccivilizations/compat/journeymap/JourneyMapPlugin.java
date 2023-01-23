package xyz.brassgoggledcoders.mccivilizations.compat.journeymap;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.ModPopupMenu;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.FullscreenMapEvent;
import journeymap.client.api.event.forge.PopupMenuEvent;
import journeymap.client.api.model.IBlockInfo;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.util.PolygonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.LandClaimChangedEvent;
import xyz.brassgoggledcoders.mccivilizations.api.location.Location;
import xyz.brassgoggledcoders.mccivilizations.api.location.LocationChangedEvent;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;
import xyz.brassgoggledcoders.mccivilizations.network.LandClaimClaimPacket;
import xyz.brassgoggledcoders.mccivilizations.network.NetworkHandler;

import java.util.*;

@ClientPlugin
public class JourneyMapPlugin implements IClientPlugin {
    private final Table<ResourceKey<Level>, ChunkPos, CivilizationDisplayable> claimedChunks = HashBasedTable.create();
    private final Map<UUID, LocationDisplayable> locations = new HashMap<>();
    private IBlockInfo lastPosition;

    private IClientAPI clientAPI;

    @Override
    public void initialize(@NotNull IClientAPI clientAPI) {
        this.clientAPI = clientAPI;
        clientAPI.subscribe(this.getModId(), EnumSet.of(ClientEvent.Type.MAP_MOUSE_MOVED, ClientEvent.Type.MAPPING_STARTED,
                ClientEvent.Type.MAPPING_STOPPED, ClientEvent.Type.MAP_CLICKED));
        MinecraftForge.EVENT_BUS.addListener(this::onPopup);
        MinecraftForge.EVENT_BUS.addListener(this::onClaimChange);
        MinecraftForge.EVENT_BUS.addListener(this::onLocationChange);
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
        if (clientEvent.type == ClientEvent.Type.MAPPING_STARTED) {

            this.clientAPI.getAllWaypoints()
                    .removeIf(waypoint -> waypoint.getModId().equals(MCCivilizations.MODID));
        }
    }

    private void onPopup(PopupMenuEvent popupMenuEvent) {
        Player player = Minecraft.getInstance().player;
        if (lastPosition != null && lastPosition.getChunkPos() != null && player != null) {
            ModPopupMenu civilizationsMenu = popupMenuEvent.getPopupMenu()
                    .createSubItemList("Civilizations");
            ILandClaimRepository claimedLand = CivilizationRepositories.getLandClaimRepository();
            Civilization chunkCivilization = claimedLand.getClaimOwner(player.getLevel().dimension(), lastPosition.getChunkPos());
            Civilization userCivilization = CivilizationRepositories.getCivilizationRepository()
                    .getCivilizationByCitizen(player);

            ResourceKey<Level> levelResourceKey = popupMenuEvent.getFullscreen()
                    .getUiState()
                    .dimension;

            if (userCivilization == null) {
                civilizationsMenu.addMenuItem(MCCivilizationsText.CITIZENSHIP_REQUIRED.getString(), blockPos -> {

                });
            } else if (chunkCivilization == null) {
                civilizationsMenu.addMenuItem(MCCivilizationsText.CLAIM_CHUNK.getString(), blockPos ->
                        NetworkHandler.getInstance().sendPacketToServer(new LandClaimClaimPacket(
                                levelResourceKey,
                                new ChunkPos(blockPos),
                                ChangeType.ADD
                        ))
                );
            } else if (chunkCivilization.equals(userCivilization)) {
                civilizationsMenu.addMenuItem(MCCivilizationsText.UNCLAIM_CHUNK.getString(), blockPos ->
                        NetworkHandler.getInstance().sendPacketToServer(new LandClaimClaimPacket(
                                levelResourceKey,
                                new ChunkPos(blockPos),
                                ChangeType.REMOVE
                        ))
                );
            }
        }
    }

    private void onMouseMove(FullscreenMapEvent.MouseMoveEvent mouseMoveEvent) {
        this.lastPosition = mouseMoveEvent.getInfo();
    }

    private void onClaimChange(LandClaimChangedEvent claimChangedEvent) {
        if (claimChangedEvent.getChangeType() == ChangeType.REMOVE) {
            for (ChunkPos chunkPos : claimChangedEvent.getChunkPositions()) {
                CivilizationDisplayable displayable = this.claimedChunks.remove(claimChangedEvent.getLevel(), chunkPos);
                if (displayable != null && clientAPI.exists(displayable.displayable())) {
                    clientAPI.remove(displayable.displayable());
                }
            }
        } else if (claimChangedEvent.getChangeType() == ChangeType.ADD) {
            DyeColor dyeColor = claimChangedEvent.getCivilization().getDyeColor();
            for (ChunkPos chunkPos : claimChangedEvent.getChunkPositions()) {
                CivilizationDisplayable newDisplayable = new CivilizationDisplayable(
                        claimChangedEvent.getCivilization(),
                        claimChangedEvent.getLevel(),
                        chunkPos,
                        new PolygonOverlay(
                                MCCivilizations.MODID,
                                "chunk_%d_%d".formatted(chunkPos.x, chunkPos.z),
                                claimChangedEvent.getLevel(),
                                new ShapeProperties()
                                        .setFillColor(dyeColor.getTextColor())
                                        .setFillOpacity(0.1F)
                                        .setStrokeColor(dyeColor.getTextColor())
                                        .setStrokeOpacity(0.15F),
                                PolygonHelper.createChunkPolygon(
                                        chunkPos.x,
                                        0,
                                        chunkPos.z
                                )
                        )
                );
                CivilizationDisplayable displayable = this.claimedChunks.put(claimChangedEvent.getLevel(), chunkPos, newDisplayable);
                if (displayable != null && clientAPI.exists(displayable.displayable())) {
                    clientAPI.remove(displayable.displayable());
                }
                try {
                    clientAPI.show(newDisplayable.displayable());
                } catch (Exception e) {
                    MCCivilizations.LOGGER.error("Failed to Display Claimed Chunk", e);
                }
            }
        }
    }

    public void onLocationChange(LocationChangedEvent event) {
        Location location = event.getLocation();
        if (locations.containsKey(location.getId())) {
            this.clientAPI.remove(locations.get(location.getId()).displayable());
        }

        Player player = Minecraft.getInstance().player;
        if (player != null && event.getChangeType() == ChangeType.ADD) {
            Civilization playerCivilization = CivilizationRepositories.getCivilizationRepository()
                    .getCivilizationByCitizen(player);

            if (playerCivilization != null && playerCivilization.equals(event.getCivilization())) {
                Waypoint locationWaypoint = new Waypoint(
                        MCCivilizations.MODID,
                        location.getId().toString(),
                        location.getName().getString(),
                        location.getPosition().dimension(),
                        location.getPosition().pos()
                );

                this.locations.put(location.getId(), new LocationDisplayable(location, locationWaypoint));

                try {
                    this.clientAPI.show(locationWaypoint);
                } catch (Exception e) {
                    MCCivilizations.LOGGER.error("Failed to Display Location Waypoint", e);
                }
            }
        }
    }
}
