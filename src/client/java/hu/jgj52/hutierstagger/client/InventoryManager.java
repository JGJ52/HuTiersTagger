package hu.jgj52.hutierstagger.client;

import net.minecraft.block.GrindstoneBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class InventoryManager {
    public static String itemStackArrayToBase64(ItemStack[] items) {
        try {
            RegistryWrapper.WrapperLookup registryLookup = MinecraftClient.getInstance().world.getRegistryManager();

            NbtList list = new NbtList();
            for (ItemStack item : items) {
                NbtCompound compound = (NbtCompound) ItemStack.CODEC.encode(
                        item,
                        registryLookup.getOps(NbtOps.INSTANCE),
                        new NbtCompound()
                ).result().orElse(new NbtCompound());
                list.add(compound);
            }

            NbtCompound root = new NbtCompound();
            root.put("Items", list);

            String nbtString = root.toString();
            return Base64.getEncoder().encodeToString(nbtString.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static ItemStack[] itemStackArrayFromBase64(String base64) {
        try {
            RegistryWrapper.WrapperLookup registryLookup = MinecraftClient.getInstance().world.getRegistryManager();

            byte[] bytes = Base64.getDecoder().decode(base64);
            String nbtString = new String(bytes, StandardCharsets.UTF_8);

            NbtCompound root = StringNbtReader.readCompound(nbtString);

            NbtList list = root.getList("Items").orElse(new NbtList());
            ItemStack[] stacks = new ItemStack[list.size()];

            for (int i = 0; i < list.size(); i++) {
                stacks[i] = ItemStack.CODEC.parse(
                        registryLookup.getOps(NbtOps.INSTANCE),
                        list.get(i)
                ).result().orElse(ItemStack.EMPTY);
            }
            return stacks;
        } catch (Exception e) {
            e.printStackTrace();
            return new ItemStack[0];
        }
    }
}
