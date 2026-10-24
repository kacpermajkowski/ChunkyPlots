package pl.kacpermajkowski.ChunkyPlots.crafting;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class CraftingManager {
	private static CraftingManager instance;

	private final JavaPlugin plugin;
	private final ItemStack plotItem;

	public CraftingManager(JavaPlugin plugin, ItemStack plotItem){
		this.plugin = plugin;
		this.plotItem = plotItem;
		instance = this;
		loadShapedRecipes(createShapedRecipes());
	}

	@Deprecated
	public static CraftingManager getInstance(){
		return instance;
	}

	private void loadShapedRecipes(List<ShapedRecipe> recipes){
		for(ShapedRecipe recipe:recipes){
			Bukkit.addRecipe(recipe);
		}
	}

	private List<ShapedRecipe> createShapedRecipes(){
		List<ShapedRecipe> list = new ArrayList<>();
		list.add(createPlotBlockRecipe());
		return list;
	}

	private ShapedRecipe createPlotBlockRecipe(){
		NamespacedKey key = new NamespacedKey(plugin, "plot_block");
		ShapedRecipe recipe = new ShapedRecipe(key, plotItem);
		recipe.shape("fff", "fef", "fgf");
		recipe.setIngredient('f', Material.OAK_FENCE);
		recipe.setIngredient('g', Material.OAK_FENCE_GATE);
		recipe.setIngredient('e', Material.EMERALD);

		return recipe;
	}
}
