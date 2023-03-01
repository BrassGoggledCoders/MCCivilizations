package xyz.brassgoggledcoders.mccivilizations.command;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;
import xyz.brassgoggledcoders.mccivilizations.repository.RepositoryManager;

public class MCCivilizationsCommand {
    public static SuggestionProvider<CommandSourceStack> CIVILIZATION_UUID_SUGGESTS = SuggestionProviders.register(
            MCCivilizations.rl("civilization_uuid"),
            new CivilizationIdSuggestionProvider<>(civilization -> civilization.getId().toString())
    );

    public static SuggestionProvider<CommandSourceStack> CIVILIZATION_NAME_SUGGESTS = SuggestionProviders.register(
            MCCivilizations.rl("civilization_name"),
            new CivilizationIdSuggestionProvider<>(civilization -> civilization.getName().getString())
    );

    public static boolean alreadyMember(CommandSourceStack commandSourceStack) {
        return CivilizationRepositories.getCivilizationRepository()
                .isCitizen(commandSourceStack.getEntity());
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher()
                .register(Commands.literal("mccivilizations")
                        .then(CivilizationCommand.create())
                        .then(LandClaimCommand.create())
                        .then(LocationCommand.create())
                        .then(Commands.literal("sync"))
                        .executes(context -> {
                            CommandSourceStack sourceStack = context.getSource();
                            ServerPlayer serverPlayer = sourceStack.getPlayer();
                            if (serverPlayer != null) {
                                sourceStack.sendSuccess(MCCivilizationsText.SYNCING, true);
                                RepositoryManager.INSTANCE.playerLoggedIn(serverPlayer);
                                return 1;
                            } else {
                                sourceStack.sendFailure(MCCivilizationsText.FAILED_SYNCING);
                                return 0;
                            }
                        })
                );
    }
}
