package pl.kacpermajkowski.ChunkyPlots.manager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.util.InventoryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CraftingManager {
	private static CraftingManager instance;

	private CraftingManager(){
		loadShapedRecipes(createShapedRecipes());
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
		NamespacedKey key = new NamespacedKey(ChunkyPlots.getInstance(), "plot_block");
		ShapedRecipe recipe = new ShapedRecipe(key, PlotManager.getInstance().getPlotItem());
		recipe.shape("fff", "fef", "fgf");
		recipe.setIngredient('f', Material.OAK_FENCE);
		recipe.setIngredient('g', Material.OAK_FENCE_GATE);
		recipe.setIngredient('e', Material.EMERALD);

		return recipe;
	}

	public static CraftingManager getInstance(){
		if(instance == null){
			instance = new CraftingManager();
		}
		return instance;
	}
}
