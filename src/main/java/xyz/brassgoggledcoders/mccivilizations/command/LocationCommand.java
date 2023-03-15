package xyz.brassgoggledcoders.mccivilizations.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.brassgoggledcoders.mccivilizations.api.MCCivilizationsRegistries;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.location.ILocationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.location.Location;
import xyz.brassgoggledcoders.mccivilizations.api.location.LocationType;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;
import xyz.brassgoggledcoders.mccivilizations.util.function.ThrowingTriFunction;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

public class LocationCommand {
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_LOCATION_TYPE = new DynamicCommandExceptionType(
            (value) -> MCCivilizationsText.translate(MCCivilizationsText.UNKNOWN_LOCATION_TYPE, value)
    );

    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("location")
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

    @SuppressWarnings("unused")
    private static ArgumentBuilder<CommandSourceStack, ?> withLocation(
            String commandName,
            Function<ThrowingTriFunction<ILocationRepository, Civilization, CommandContext<CommandSourceStack>, Stream<Location>, CommandSyntaxException>, Command<CommandSourceStack>> command
    ) {
        return Commands.literal(commandName)
                .then(Commands.literal("id")
                        .then(Commands.argument("id", UuidArgument.uuid())
                                .suggests(MCCivilizationsCommand.LOCATION_UUID_SUGGESTS)
                                .executes(command.apply((repository, civilization, context) -> {
                                    UUID id = UuidArgument.getUuid(context, "id");
                                    return repository.getLocations(civilization)
                                            .stream()
                                            .filter(location -> location.getId().equals(id));
                                }))
                        )
                )
                .then(Commands.literal("name")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(command.apply((locationRepository, civilization, context) ->
                                        Stream.empty()
                                ))
                        )
                )
                .then(Commands.literal("type")
                        .then(Commands.argument("type", ResourceKeyArgument.key(MCCivilizationsRegistries.LOCATION_TYPE))
                                .executes(command.apply((repository, civilization, context) -> repository.getLocationsOf(
                                        civilization,
                                        getLocationType(context, "type")
                                ).stream()))
                        )
                );
    }

    public static LocationType getLocationType(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
        ResourceKey<?> argumentKey = pContext.getArgument(pName, ResourceKey.class);
        Registry<LocationType> locationTypes = pContext.getSource()
                .getServer()
                .registryAccess()
                .registryOrThrow(MCCivilizationsRegistries.LOCATION_TYPE);

        return argumentKey.cast(MCCivilizationsRegistries.LOCATION_TYPE)
                .flatMap(locationTypes::getOptional)
                .orElseThrow(() -> ERROR_UNKNOWN_LOCATION_TYPE.create(argumentKey.location()));
    }
}