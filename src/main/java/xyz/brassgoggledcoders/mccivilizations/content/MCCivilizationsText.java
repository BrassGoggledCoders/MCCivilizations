package xyz.brassgoggledcoders.mccivilizations.content;

import net.minecraft.network.chat.Component;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;

public class MCCivilizationsText {

    public static final Component ENTERING_CIVILIZATION = MCCivilizations.getRegistrate()
            .addLang("text", MCCivilizations.rl("entering_civilization"), "Entering %s");

    public static final Component LEAVING_CIVILIZATION = MCCivilizations.getRegistrate()
            .addLang("text", MCCivilizations.rl("leaving_civilization"), "Leaving %s");

    public static final Component NO_NAME_CIVILIZATION = MCCivilizations.getRegistrate()
            .addLang("text", MCCivilizations.rl("no_name_civilization"), "No Name");

    public static final Component CLAIM_CHUNK = MCCivilizations.getRegistrate()
            .addLang("text", MCCivilizations.rl("claim_chunk"), "Claim Chunk for %s");


    public static void setup() {

    }
}