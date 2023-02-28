package xyz.brassgoggledcoders.mccivilizations.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.location.Location;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;

import java.util.Collection;
import java.util.Objects;

public class LocationCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("location")
                .requires(stack -> stack.isPlayer() && CivilizationRepositories.getCivilizationRepository()
                        .getCivilizationByCitizen(Objects.requireNonNull(stack.getPlayer())) != null
                )
                .then(Commands.literal("fix")
                        .executes(context -> {
                            Entity entity = context.getSource().getEntityOrException();
                            Civilization civilization = CivilizationRepositories.getCivilizationRepository()
                                    .getCivilizationByCitizen(entity);

                            if (civilization != null) {
                                Collection<Location> locations = CivilizationRepositories.getLocationRepository()
                                        .getLocations(civilization);

                                int fixed = 0;

                                MinecraftServer server = context.getSource().getServer();
                                for (Location location : locations) {
                                    ServerLevel level = server.getLevel(location.getPosition().dimension());
                                    if (level != null && level.isLoaded(location.getPosition().pos())) {
                                        BlockState currentState = level.getBlockState(location.getPosition().pos());
                                        if (currentState.isAir() || !currentState.is(location.getBlockState().getBlock())) {
                                            fixed++;
                                            CivilizationRepositories.getLocationRepository()
                                                    .removeLocation(civilization, location);
                                        }
                                    }
                                }
                                if (fixed > 0) {
                                    context.getSource().sendSuccess(MCCivilizationsText.translate(MCCivilizationsText.FIXED_LOCATIONS, fixed), true);
                                } else {
                                    context.getSource().sendFailure(MCCivilizationsText.NO_LOCATIONS_TO_FIX);
                                }
                                return fixed;
                            } else {
                                context.getSource().sendFailure(MCCivilizationsText.CIVILIZATION_DOES_NOT_EXIST);
                                return 0;
                            }
                        })
                );
    }
}