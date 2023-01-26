package xyz.brassgoggledcoders.mccivilizations.content;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;

public class MCCivilizationsText {

    public static final Component ENTERING_CIVILIZATION = MCCivilizations.getRegistrate()
            .addLang("text", MCCivilizations.rl("entering_civilization"), "Entering %s");

    public static final Component LEAVING_CIVILIZATION = MCCivilizations.getRegistrate()
            .addLang("text", MCCivilizations.rl("leaving_civilization"), "Leaving %s");

    public static final Component NO_NAME_CIVILIZATION = MCCivilizations.getRegistrate()
            .addLang("text", MCCivilizations.rl("no_name_civilization"), "No Name");

    public static final Component CLAIM_CHUNK = MCCivilizations.getRegistrate()
            .addLang("text", MCCivilizations.rl("claim_chunk"), "Claim Chunk");

    public static final Component UNCLAIM_CHUNK = MCCivilizations.getRegistrate()
            .addLang("text", MCCivilizations.rl("unclaim_chunk"), "Unclaim Chunk");

    public static final Component CITIZENSHIP_REQUIRED = MCCivilizations.getRegistrate()
            .addLang("text", MCCivilizations.rl("citizenship_required"), "You must be a citizen of a Civilization");

    public static final Component CIVILIZATION_DOES_NOT_EXIST = MCCivilizations.getRegistrate()
            .addLang("text", MCCivilizations.rl("civilization_does_not_exist"), "Civilization does not exist");
    public static final Component SYNCING = MCCivilizations.getRegistrate()
            .addLang("text", MCCivilizations.rl("syncing"), "Syncing Civilization data to the Client");

    public static final Component FAILED_SYNCING = MCCivilizations.getRegistrate()
            .addLang("text", MCCivilizations.rl("failed_syncing"), "Failed ot Sync Civilization data to the Client");

    public static Component translate(Component component, Object... args) {
        if (component.getContents() instanceof TranslatableContents translatableContents) {
            return Component.translatable(translatableContents.getKey(), args);
        } else {
            return component;
        }
    }

    public static void setup() {

    }
}
