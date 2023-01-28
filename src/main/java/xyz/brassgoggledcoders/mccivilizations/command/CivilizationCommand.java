package xyz.brassgoggledcoders.mccivilizations.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;
import xyz.brassgoggledcoders.mccivilizations.util.function.ThrowingBiFunction;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CivilizationCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("civilization")
                .then(Commands.literal("describe")
                        .then(Commands.literal("id")
                                .then(Commands.argument("id", UuidArgument.uuid())
                                        .suggests(MCCivilizationsCommand.CIVILIZATION_UUID_SUGGESTS)
                                        .executes(context -> executeDescribeCivilization(
                                                context,
                                                (repository, value) -> repository.getCivilizationById(UuidArgument.getUuid(value, "id"))
                                        ))
                                )
                        )
                        .then(Commands.literal("name")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .suggests(MCCivilizationsCommand.CIVILIZATION_NAME_SUGGESTS)
                                        .executes(context -> executeDescribeCivilizations(
                                                context,
                                                (repository, value) -> repository.getCivilizationByName(
                                                        StringArgumentType.getString(value, "name")
                                                )
                                        ))
                                )
                        )
                        .then(Commands.literal("citizen")
                                .then(Commands.argument("citizen", EntityArgument.player())
                                        .executes(context -> executeDescribeCivilization(
                                                context,
                                                (repository, value) -> repository.getCivilizationByCitizen(
                                                        EntityArgument.getPlayer(value, "citizen")
                                                )
                                        ))
                                )
                        )
                        .executes(context -> executeDescribeCivilization(
                                context,
                                (repository, value) -> repository.getCivilizationByCitizen(value.getSource()
                                        .getEntityOrException()
                                )
                        ))
                );
    }

    private static int executeDescribeCivilization(
            CommandContext<CommandSourceStack> context,
            ThrowingBiFunction<ICivilizationRepository, CommandContext<CommandSourceStack>, Civilization, CommandSyntaxException> getCivilization
    ) throws CommandSyntaxException {
        return executeDescribeCivilizations(
                context,
                (repository, value) -> Optional.ofNullable(getCivilization.apply(repository, value))
                        .map(List::of)
                        .orElse(Collections.emptyList())
        );
    }

    private static int executeDescribeCivilizations(
            CommandContext<CommandSourceStack> context,
            ThrowingBiFunction<ICivilizationRepository, CommandContext<CommandSourceStack>, Collection<Civilization>, CommandSyntaxException> getCivilization
    ) throws CommandSyntaxException {
        Collection<Civilization> civilizations = getCivilization.apply(
                CivilizationRepositories.getCivilizationRepository(),
                context
        );

        if (!civilizations.isEmpty()) {
            for (Civilization civilization : civilizations) {
                context.getSource().sendSuccess(Component.literal(civilization.toString()), true);
            }

            return civilizations.size();
        } else {
            context.getSource().sendFailure(MCCivilizationsText.CIVILIZATION_DOES_NOT_EXIST);
            return 0;
        }
    }
}
