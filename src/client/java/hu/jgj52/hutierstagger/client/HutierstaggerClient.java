package hu.jgj52.hutierstagger.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Set;

public class HutierstaggerClient implements ClientModInitializer {

    private String gamemode;

    private static HutierstaggerClient instance;

    @Override
    public void onInitializeClient() {
        instance = this;
        ConfigFile.load();
        gamemode = ConfigFile.get("gamemode", "vanilla");
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("setgamemode")
                .then(ClientCommandManager.argument("mode", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(Set.of("vanilla", "uhc", "pot", "nethpot", "smp", "sword", "axe", "mace", "cart", "creeper", "diasmp", "ogvanilla", "shieldlessuhc"), builder))
                        .executes(context -> {
                            String mode = StringArgumentType.getString(context, "mode");

                            if (!List.of("vanilla", "uhc", "pot", "nethpot", "smp", "sword", "axe", "mace", "cart", "creeper", "diasmp", "ogvanilla", "shieldlessuhc").contains(mode)) {
                                context.getSource().sendFeedback(Text.literal("§cNincs ilyen gamemode!"));
                                return 0;
                            }

                            gamemode = mode;

                            ConfigFile.set("gamemode", mode);

                            PlayerPrefixManager.resetMap();

                            context.getSource().sendFeedback(Text.literal("§aGamemode beállítva erre: " + mode));
                            return 1;
                        })
                )
        ));
    }

    public String getGamemode() {
        return gamemode;
    }

    public static HutierstaggerClient getInstance() {
        return instance;
    }
}
