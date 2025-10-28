package hu.jgj52.hutierstagger.mixin;

import hu.jgj52.hutierstagger.client.ConfigFile;
import hu.jgj52.hutierstagger.client.PlayerPrefixManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    public void getNameHook(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        String playerName = getPlayerName(player);

        String prefix = PlayerPrefixManager.getPrefix(playerName);
        if (prefix == null) {
            PlayerPrefixManager.fetchPlayer(playerName);
        } else if (prefix.isEmpty()) {
            cir.setReturnValue(Text.literal(playerName));
        } else {
            Boolean retired = PlayerPrefixManager.getRetired(playerName);
            String color = switch (prefix) {
                case "HT1" -> "ffcf4a";
                case "LT1" -> "d5b355";
                case "HT2" -> "a4b3c7";
                case "LT2" -> "888d95";
                case "HT3" -> "dd8849";
                case "LT3" -> "b36830";
                case "HT4" -> "655b79";
                case "LT4" -> "484058";
                case "HT5" -> "437ab4";
                case "LT5" -> "2e5176";
                default -> "ffffff";
            };
            String icon = switch (ConfigFile.get("gamemode", "vanilla")) {
                case "vanilla" -> "";
                case "uhc" -> "";
                case "pot" -> "";
                case "nethpot" -> "";
                case "smp" -> "";
                case "sword" -> "";
                case "axe" -> "";
                case "mace" -> "";
                case "cart" -> "";
                case "diasmp" -> "";
                case "shieldlessuhc" -> "";
                default -> "";
            };
            Text iconT = Text.literal(icon).setStyle(Style.EMPTY.withFont(Identifier.of("hutierstagger:default")));
            if (retired) {
                if (prefix.startsWith("HT")) {
                    color = "651d6e";
                } else if (prefix.startsWith("LT")) {
                    color = "301234";
                }
                prefix = "R" + prefix;
            }
            cir.setReturnValue(Text.literal("").append(iconT).append(Text.literal(prefix).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt(color, 16))))).append(Text.literal(" §8| §r")).append(Text.literal(playerName)));
        }
    }

    private static String getPlayerName(PlayerEntity player) {
        try {
            return player.getGameProfile().getName();
        } catch (NoSuchMethodError e) {
            try {
                return (String) player.getGameProfile()
                        .getClass()
                        .getMethod("getName")
                        .invoke(player.getGameProfile());
            } catch (Exception ex) {
                ex.printStackTrace();
                return "unknown";
            }
        }
    }

}
