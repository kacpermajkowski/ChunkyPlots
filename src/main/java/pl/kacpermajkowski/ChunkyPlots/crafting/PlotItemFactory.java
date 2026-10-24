package pl.kacpermajkowski.ChunkyPlots.crafting;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import pl.kacpermajkowski.ChunkyPlots.config.Config;

import java.util.HashMap;

public class PlotItemFactory {
    private final Config config;

    public PlotItemFactory(Config config) {
        this.config = config;
    }

    public ItemStack createPlotItem() {
        HashMap<Enchantment, Integer> enchantments = new HashMap<>();
        enchantments.put(Enchantment.UNBREAKING, 1);
        return InventoryUtil.createItemStack(
                config.getPlotItemMaterial(),
                1,
                config.getPlotItemName(),
                config.getPlotItemLore(),
                enchantments,
                false);
    }
}
