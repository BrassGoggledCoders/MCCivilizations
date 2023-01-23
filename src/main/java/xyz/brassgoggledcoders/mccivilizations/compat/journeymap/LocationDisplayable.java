package xyz.brassgoggledcoders.mccivilizations.compat.journeymap;

import journeymap.client.api.display.Displayable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.location.Location;


public record LocationDisplayable(
        Location location,
        Displayable displayable
) {
}
