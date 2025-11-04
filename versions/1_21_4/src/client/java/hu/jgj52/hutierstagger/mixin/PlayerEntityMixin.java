package hu.jgj52.hutierstagger.mixin;

import hu.jgj52.hutierstagger.client.ConfigFile;
import hu.jgj52.hutierstagger.client.HutierstaggerClient;
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

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    public void getDisplayNameHook(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        String playerName = player.getGameProfile().getName();

        String prefix = PlayerPrefixManager.getPrefix(playerName);
        if (prefix == null) {
            PlayerPrefixManager.fetchPlayer(playerName);
            return;
        }

        if (!prefix.isEmpty()) {
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

            String icon = switch (PlayerPrefixManager.getNowGamemode(playerName).isEmpty() ? ConfigFile.get("gamemode", "Vanilla") : PlayerPrefixManager.getNowGamemode(playerName)) {
                case "Vanilla" -> "";
                case "UHC" -> "";
                case "Pot" -> "";
                case "NethPot" -> "";
                case "SMP" -> "";
                case "Sword" -> "";
                case "Axe" -> "";
                case "Mace" -> "";
                case "Cart" -> "";
                case "Creeper" -> "";
                case "DiaSMP" -> "";
                case "OGVanilla" -> "";
                case "ShieldlessUHC" -> "";
                default -> "";
            };

            if (retired) {
                if (prefix.startsWith("HT")) {
                    color = "651d6e";
                } else if (prefix.startsWith("LT")) {
                    color = "301234";
                }
                prefix = "R" + prefix;
            }

            Text prefixedName = Text.literal("")
                    .append(Text.literal(icon).setStyle(Style.EMPTY.withFont(Identifier.of("hutierstagger:default"))))
                    .append(Text.literal(" " + prefix).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt(color, 16)))))
                    .append(Text.literal(" §8| §r"))
                    .append(cir.getReturnValue());

            if (HutierstaggerClient.getEnabled()) {
                cir.setReturnValue(prefixedName);
            }
        }
    }
}
