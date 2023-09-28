package xyz.brassgoggledcoders.mccivilizations.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RegistryValueArgumentType<VALUE> implements ArgumentType<VALUE> {
    private final IForgeRegistry<VALUE> repository;
    private final List<String> examples;
    private final DynamicCommandExceptionType exceptionType = new DynamicCommandExceptionType((input) ->
            MCCivilizationsText.translate(MCCivilizationsText.NO_VALUE_FOUND, input)
    );

    public RegistryValueArgumentType(IForgeRegistry<VALUE> datapackValueProvider) {
        this.repository = datapackValueProvider;
        this.examples = createExamples(datapackValueProvider);
    }


    private List<String> createExamples(IForgeRegistry<VALUE> registry) {
        List<String> examples = new ArrayList<>();
        Iterator<ResourceLocation> iterator = registry.getKeys().iterator();
        int i = 0;
        while (iterator.hasNext() & i < 5) {
            i++;
            examples.add(iterator.next().toString());
        }
        return examples;
    }

    @Override
    public VALUE parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation resourceLocation = ResourceLocation.read(reader);
        return Optional.ofNullable(repository.getValue(resourceLocation))
                .orElseThrow(() -> exceptionType.create(resourceLocation));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(repository.getKeys().stream().map(Objects::toString), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return examples;
    }
}
