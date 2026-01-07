package hu.jgj52.hutierstagger.client;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = "hutierstagger")
@Config(name = "hutierstagger", wrapperName = "Config", defaultHook = true)
public class ConfigModel {
    public enum gamemodes {
        Vanilla,
        UHC,
        Pot,
        NethPot,
        SMP,
        Sword,
        Axe,
        Mace,
        Cart,
        Creeper,
        DiaSMP,
        OGVanilla,
        ShieldlessUHC
    }
    public gamemodes gamemode = gamemodes.Vanilla;
    public boolean enabled = true;
}
