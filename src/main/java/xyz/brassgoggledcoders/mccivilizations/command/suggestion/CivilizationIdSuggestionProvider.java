package xyz.brassgoggledcoders.mccivilizations.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CivilizationIdSuggestionProvider<T> implements SuggestionProvider<T> {
    private final Function<Civilization, String> fromCivToString;

    public CivilizationIdSuggestionProvider(Function<Civilization, String> fromCivToString) {
        this.fromCivToString = fromCivToString;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<T> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                CivilizationRepositories.getCivilizationRepository()
                        .getAllCivilizations()
                        .stream()
                        .map(fromCivToString),
                builder
        );
    }
}
