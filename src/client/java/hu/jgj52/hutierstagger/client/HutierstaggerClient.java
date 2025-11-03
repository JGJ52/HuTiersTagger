package hu.jgj52.hutierstagger.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.text.Text;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.Set;

public class HutierstaggerClient implements ClientModInitializer {

    private String gamemode;
    private static boolean enabled;

    private static HutierstaggerClient instance;

    private static final HttpClient client = HttpClient.newHttpClient();

    @Override
    public void onInitializeClient() {
        instance = this;
        ConfigFile.load();
        gamemode = ConfigFile.get("gamemode", "Vanilla");
        enabled = Boolean.parseBoolean(ConfigFile.get("enabled", "true"));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("setgamemode")
                .then(ClientCommandManager.argument("mode", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(Set.of("Vanilla", "UHC", "Pot", "NethPot", "SMP", "Sword", "Axe", "Mace", "Cart", "Creeper", "DiaSMP", "OGVanilla", "ShieldlessUHC"), builder))
                        .executes(context -> {
                            String mode = StringArgumentType.getString(context, "mode");

                            if (!List.of("Vanilla", "UHC", "Pot", "NethPot", "SMP", "Sword", "Axe", "Mace", "Cart", "Creeper", "DiaSMP", "OGVanilla", "ShieldlessUHC").contains(mode)) {
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
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("getgamemodekit")
                .then(ClientCommandManager.argument("mode", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(Set.of("Vanilla", "UHC", "Pot", "NethPot", "SMP", "Sword", "Axe", "Mace", "Cart", "Creeper", "DiaSMP", "OGVanilla", "ShieldlessUHC"), builder))
                        .executes(context -> {
                            String mode = StringArgumentType.getString(context, "mode");

                            if (!List.of("Vanilla", "UHC", "Pot", "NethPot", "SMP", "Sword", "Axe", "Mace", "Cart", "Creeper", "DiaSMP", "OGVanilla", "ShieldlessUHC").contains(mode)) {
                                context.getSource().sendFeedback(Text.literal("§cNincs ilyen gamemode!"));
                                return 0;
                            }

                            if (!MinecraftClient.getInstance().player.isCreative()) {
                                context.getSource().sendFeedback(Text.literal("§cKreatívban kell lenned!"));
                                return 0;
                            }

                            try {
                                HttpRequest request = HttpRequest.newBuilder()
                                        .uri(new URI("https://api.hutiers.hu/v2/gamemode/kit/" + mode))
                                        .GET()
                                        .build();

                                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                                ItemStack[] items = InventoryManager.itemStackArrayFromBase64(response.body());

                                for (int i = 0; i < items.length && i <= 40; i++) {
                                    MinecraftClient.getInstance().player.getInventory().setStack(i, items[i]);

                                    int serverSlot;
                                    if (i < 9) {
                                        serverSlot = i + 36;
                                    } else if (i < 36) {
                                        serverSlot = i;
                                    } else if (i == 36) {
                                        serverSlot = 8;
                                    } else if (i == 37) {
                                        serverSlot = 7;
                                    } else if (i == 38) {
                                        serverSlot = 6;
                                    } else if (i == 39) {
                                        serverSlot = 5;
                                    } else {
                                        serverSlot = 45;
                                    }

                                    MinecraftClient.getInstance().getNetworkHandler().sendPacket(
                                            new CreativeInventoryActionC2SPacket(serverSlot, items[i])
                                    );
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }


                            context.getSource().sendFeedback(Text.literal("§aGamemode kitjét megkaptad: " + mode));
                            return 1;
                        })
                )
        ));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("togglehutierstagger")
                .executes(context -> {
                    enabled = !enabled;
                    ConfigFile.set("enabled", String.valueOf(enabled));
                    context.getSource().sendFeedback(Text.literal("§aSikeresen átállítva!"));
                    return 1;
                })
        ));

    }

    public String getGamemode() {
        return gamemode;
    }

    public static boolean getEnabled() {
        return enabled;
    }

    public static HutierstaggerClient getInstance() {
        return instance;
    }
}
