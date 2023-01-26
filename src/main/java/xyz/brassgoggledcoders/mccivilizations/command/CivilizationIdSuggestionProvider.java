package xyz.brassgoggledcoders.mccivilizations.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CivilizationIdSuggestionProvider<T> implements SuggestionProvider<T> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<T> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                CivilizationRepositories.getCivilizationRepository()
                        .getAllCivilizations()
                        .stream()
                        .map(Civilization::getId)
                        .map(UUID::toString),
                builder
        );
    }
}
