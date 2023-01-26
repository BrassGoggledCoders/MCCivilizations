package xyz.brassgoggledcoders.mccivilizations.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;

import java.util.function.BiFunction;

public class CivilizationCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("civilization")
                .then(Commands.literal("describe")
                        .then(Commands.argument("id", UuidArgument.uuid())
                                .suggests(MCCivilizationsCommand.CIVILIZATION_UUID_SUGGESTS)
                                .executes(context -> executeDescribeCivilization(
                                        context,
                                        (repository, value) -> repository.getCivilizationById(UuidArgument.getUuid(value, "id"))
                                ))
                        )
                        .executes(context -> executeDescribeCivilization(
                                context,
                                (repository, value) -> {
                                    Entity caller = value.getSource()
                                            .getEntity();
                                    if (caller != null) {
                                        return repository.getCivilizationByCitizen(value.getSource()
                                                .getEntity()
                                        );
                                    }

                                    return null;
                                }
                        ))
                );
    }

    private static int executeDescribeCivilization(
            CommandContext<CommandSourceStack> context,
            BiFunction<ICivilizationRepository, CommandContext<CommandSourceStack>, Civilization> getCivilization
    ) {
        Civilization civilization = getCivilization.apply(
                CivilizationRepositories.getCivilizationRepository(),
                context
        );

        if (civilization != null) {
            context.getSource().sendSuccess(Component.literal(civilization.toString()), true);
            return 1;
        } else {
            context.getSource().sendFailure(MCCivilizationsText.CIVILIZATION_DOES_NOT_EXIST);
            return 0;
        }
    }
}
