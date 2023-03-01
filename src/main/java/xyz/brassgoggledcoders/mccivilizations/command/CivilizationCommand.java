package xyz.brassgoggledcoders.mccivilizations.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;
import xyz.brassgoggledcoders.mccivilizations.util.function.ThrowingBiFunction;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CivilizationCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("civilization")
                .then(withCivilization("describe", CivilizationCommand::executeDescribeCivilizations))
                .requires(CommandSourceStack::isPlayer)
                .then(withCivilization("join", CivilizationCommand::joinCivilization))
                .requires(MCCivilizationsCommand::alreadyMember)
                .then(Commands.literal("leave"))
                .executes(context -> {
                    ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
                    ICivilizationRepository civilizationRepository = CivilizationRepositories.getCivilizationRepository();
                    Civilization civilization = civilizationRepository.getCivilizationByCitizen(serverPlayer);

                    if (civilization != null) {
                        if (civilizationRepository.leaveCivilization(civilization, serverPlayer)) {
                            context.getSource().sendSuccess(MCCivilizationsText.CIVILIZATION_LEFT, true);
                            return 1;
                        }
                    }
                    context.getSource().sendFailure(MCCivilizationsText.CIVILIZATION_DOES_NOT_EXIST);
                    return 0;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> withCivilization(
            String commandName,
            Function<ThrowingBiFunction<ICivilizationRepository, CommandContext<CommandSourceStack>, Stream<Civilization>, CommandSyntaxException>, Command<CommandSourceStack>> command
    ) {
        return Commands.literal(commandName)
                .then(Commands.literal("id")
                        .then(Commands.argument("id", UuidArgument.uuid())
                                .suggests(MCCivilizationsCommand.CIVILIZATION_UUID_SUGGESTS)
                                .executes(command.apply(
                                        (repository, context) -> Optional.ofNullable(repository.getCivilizationById(UuidArgument.getUuid(context, "id")))
                                                .stream()
                                ))
                        )
                )
                .then(Commands.literal("name")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests(MCCivilizationsCommand.CIVILIZATION_NAME_SUGGESTS)
                                .executes(command.apply(
                                        (repository, context) -> repository.getCivilizationByName(
                                                StringArgumentType.getString(context, "name")
                                        ).stream()
                                ))
                        )
                )
                .then(Commands.literal("citizen")
                        .then(Commands.argument("citizen", EntityArgument.player())
                                .executes(command.apply(
                                        (repository, context) -> Optional.ofNullable(repository.getCivilizationByCitizen(
                                                EntityArgument.getPlayer(context, "citizen")
                                        )).stream()
                                ))
                        )
                )
                .requires(CommandSourceStack::isPlayer)
                .executes(command.apply(
                        (repository, context) -> Optional.ofNullable(repository.getCivilizationByCitizen(context.getSource()
                                .getEntityOrException()
                        )).stream()
                ));

    }

    private static Command<CommandSourceStack> executeDescribeCivilizations(
            ThrowingBiFunction<ICivilizationRepository, CommandContext<CommandSourceStack>, Stream<Civilization>, CommandSyntaxException> getCivilization
    ) {
        return context -> {
            Collection<Civilization> civilizations = getCivilization.apply(
                    CivilizationRepositories.getCivilizationRepository(),
                    context
            ).collect(Collectors.toSet());

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

    private static Command<CommandSourceStack> joinCivilization(
            ThrowingBiFunction<ICivilizationRepository, CommandContext<CommandSourceStack>, Stream<Civilization>, CommandSyntaxException> getCivilization
    ) {
        return context -> {
            Optional<Civilization> civilization = getCivilization.apply(
                    CivilizationRepositories.getCivilizationRepository(),
                    context
            ).findFirst();

            ServerPlayer player = context.getSource()
                    .getPlayerOrException();

            ICivilizationRepository civilizationRepository = CivilizationRepositories.getCivilizationRepository();

            if (civilizationRepository.getCivilizationByCitizen(player) != null) {
                context.getSource().sendFailure(MCCivilizationsText.CIVILIZATION_ALREADY_MEMBER);
                return 0;
            } else if (civilization.isPresent()) {
                civilizationRepository.joinCivilization(civilization.get(), player);
                context.getSource().sendSuccess(MCCivilizationsText.CIVILIZATION_JOINED, true);
                return 1;
            } else {
                context.getSource().sendFailure(MCCivilizationsText.CIVILIZATION_DOES_NOT_EXIST);
                return 0;
            }
        };
    }
}
