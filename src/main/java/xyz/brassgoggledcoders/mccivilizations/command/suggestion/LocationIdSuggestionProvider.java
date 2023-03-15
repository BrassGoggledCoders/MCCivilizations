package xyz.brassgoggledcoders.mccivilizations.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.location.Location;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class LocationIdSuggestionProvider<T> implements SuggestionProvider<T> {
    private final Function<Location, String> fromLocationToString;

    public LocationIdSuggestionProvider(Function<Location, String> fromLocationToString) {
        this.fromLocationToString = fromLocationToString;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<T> context, SuggestionsBuilder builder) {
        boolean admin = context.getNodes()
                .stream()
                .anyMatch(node -> node.getNode().getName().equals("admin"));

        if (admin) {
            return SharedSuggestionProvider.suggest(
                    CivilizationRepositories.getLocationRepository()
                            .getAllLocations()
                            .stream()
                            .map(fromLocationToString),
                    builder
            );
        } else if (context.getSource() instanceof IHasPlayer hasPlayer) {
            Civilization civilization = CivilizationRepositories.getCivilizationRepository()
                    .getCivilizationByCitizen(hasPlayer.getPlayer());

            if (civilization != null) {
                return SharedSuggestionProvider.suggest(
                        CivilizationRepositories.getLocationRepository()
                                .getLocations(civilization)
                                .stream()
                                .map(fromLocationToString),
                        builder
                );
            }
        }

        return builder.buildFuture();
    }
}
