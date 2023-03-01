package xyz.brassgoggledcoders.mccivilizations.command;

import com.mojang.brigadier.Command;
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
                                        .executes(executeDescribeCivilization(
                                                (repository, context) -> repository.getCivilizationById(UuidArgument.getUuid(context, "id"))
                                        ))
                                )
                        )
                        .then(Commands.literal("name")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .suggests(MCCivilizationsCommand.CIVILIZATION_NAME_SUGGESTS)
                                        .executes(executeDescribeCivilizations(
                                                (repository, context) -> repository.getCivilizationByName(
                                                        StringArgumentType.getString(context, "name")
                                                )
                                        ))
                                )
                        )
                        .then(Commands.literal("citizen")
                                .then(Commands.argument("citizen", EntityArgument.player())
                                        .executes(executeDescribeCivilization(
                                                (repository, context) -> repository.getCivilizationByCitizen(
                                                        EntityArgument.getPlayer(context, "citizen")
                                                )
                                        ))
                                )
                        )
                        .executes(executeDescribeCivilization(
                                (repository, context) -> repository.getCivilizationByCitizen(context.getSource()
                                        .getEntityOrException()
                                )
                        ))
                );
    }

    private static Command<CommandSourceStack> executeDescribeCivilization(
            ThrowingBiFunction<ICivilizationRepository, CommandContext<CommandSourceStack>, Civilization, CommandSyntaxException> getCivilization
    ) {
        return executeDescribeCivilizations(
                (repository, value) -> Optional.ofNullable(getCivilization.apply(repository, value))
                        .map(List::of)
                        .orElse(Collections.emptyList())
        );
    }

    private static Command<CommandSourceStack> executeDescribeCivilizations(
            ThrowingBiFunction<ICivilizationRepository, CommandContext<CommandSourceStack>, Collection<Civilization>, CommandSyntaxException> getCivilization
    ) {
        return context -> {
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
        };
    }
}
