package xyz.brassgoggledcoders.mccivilizations.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.data.CivilizationDatapackValues;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.api.resource.IResourceRepository;
import xyz.brassgoggledcoders.mccivilizations.api.resource.Resource;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;
import xyz.brassgoggledcoders.mccivilizations.util.function.ThrowingBiFunction;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class ResourceCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("mccivilizations")
                .then(Commands.literal("resources")
                        .then(CivilizationCommand.withCivilization(
                                "list",
                                ResourceCommand::listResourcesFor
                        ))
                        .executes(ResourceCommand::listAllResources)
                );
    }

    private static Command<CommandSourceStack> listResourcesFor(
            ThrowingBiFunction<ICivilizationRepository, CommandContext<CommandSourceStack>, Stream<Civilization>, CommandSyntaxException> withCivilization
    ) {
        return context -> {
            IResourceRepository storage = CivilizationRepositories.getResourceRepository();
            withCivilization.apply(CivilizationRepositories.getCivilizationRepository(), context)
                    .forEach(civilization -> {
                        context.getSource().sendSuccess(MCCivilizationsText.translate(
                                MCCivilizationsText.RESOURCE_COUNTS_HEADER,
                                civilization.getName()
                        ), false);

                        storage.getResourceCounts(civilization)
                                .entrySet()
                                .stream()
                                .map(entry -> MCCivilizationsText.translate(
                                        MCCivilizationsText.RESOURCE_COUNTS,
                                        entry.getValue(),
                                        entry.getKey().group(),
                                        entry.getKey().name()
                                ))
                                .forEach(message(context));
                    });
            return 1;
        };
    }

    private static int listAllResources(CommandContext<CommandSourceStack> context) {
        CivilizationDatapackValues.getResourceProvider()
                .getValues()
                .stream()
                .map(Resource::name)
                .forEach(message(context));
        return 1;
    }

    private static Component createTranslation(String additionalText, Object... inputs) {
        return Component.translatable("mccivilizations.resources.command." + additionalText, inputs);
    }

    private static Consumer<Component> message(CommandContext<CommandSourceStack> context) {
        return textComponent -> context.getSource().sendSuccess(textComponent, true);
    }
}
