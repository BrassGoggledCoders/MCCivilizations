package xyz.brassgoggledcoders.mccivilizations.command;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.command.suggestion.CivilizationIdSuggestionProvider;
import xyz.brassgoggledcoders.mccivilizations.command.suggestion.LocationIdSuggestionProvider;
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

    public static SuggestionProvider<CommandSourceStack> LOCATION_UUID_SUGGESTS = SuggestionProviders.register(
            MCCivilizations.rl("location_uuid"),
            new LocationIdSuggestionProvider<>(location -> location.getId().toString())
    );

    public static SuggestionProvider<CommandSourceStack> LOCATION_NAME_SUGGESTS = SuggestionProviders.register(
            MCCivilizations.rl("location_name"),
            new LocationIdSuggestionProvider<>(location -> location.getId().toString())
    );

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher()
                .register(Commands.literal("mccivilizations")
                        .then(CivilizationCommand.create())
                        .then(LandClaimCommand.create())
                        .then(LocationCommand.create())
                        .then(ResourceCommand.create())
                        .then(Commands.literal("sync")
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
                        )
                        .then(Commands.literal("save-all")
                                .requires(commandSourceStack -> commandSourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .executes(context -> {
                                    RepositoryManager.INSTANCE.save(true);
                                    context.getSource().sendSuccess(MCCivilizationsText.SAVE_ALL, true);
                                    return 1;
                                })
                        )
                );
    }
}
