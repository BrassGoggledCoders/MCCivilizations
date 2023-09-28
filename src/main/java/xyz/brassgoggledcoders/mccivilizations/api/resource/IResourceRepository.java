package xyz.brassgoggledcoders.mccivilizations.api.resource;

import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;

import java.util.Map;

public interface IResourceRepository {
    Map<Resource, Long> getResourceCounts(Civilization civilization);
}
