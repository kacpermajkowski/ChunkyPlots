package pl.kacpermajkowski.ChunkyPlots.plot.group.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.group.Group;
import pl.kacpermajkowski.ChunkyPlots.plot.gui.PlayerSelectAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupGuiHolder implements InventoryHolder {
    @FunctionalInterface
    public interface ItemFactory {
        ItemStack create(Material material);
    }

    private static ItemFactory itemFactory = ItemStack::new;

    public static void setItemFactory(ItemFactory factory) {
        itemFactory = factory != null ? factory : ItemStack::new;
    }

    public static void resetItemFactory() {
        itemFactory = ItemStack::new;
    }

    public static ItemStack createItem(Material material) {
        return itemFactory.create(material);
    }

    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = itemFactory.create(material);
        ItemMeta meta = item != null ? item.getItemMeta() : null;
        if (meta != null) {
            meta.displayName(TextUtil.toComponent(name));
            List<Component> componentLore = new ArrayList<>();
            for (String line : lore) {
                componentLore.add(line.isEmpty() ? Component.empty() : TextUtil.toComponent(line));
            }
            meta.lore(componentLore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private final GroupGuiType type;
    private final Player player;
    private final Group group;
    private final int page;
    private final PlayerSelectAction selectAction;
    private final Map<Integer, Object> slotData = new HashMap<>();
    private Inventory inventory;

    public GroupGuiHolder(GroupGuiType type, Player player, Group group) {
        this(type, player, group, 0, null);
    }

    public GroupGuiHolder(GroupGuiType type, Player player, Group group, int page, PlayerSelectAction selectAction) {
        this.type = type;
        this.player = player;
        this.group = group;
        this.page = page;
        this.selectAction = selectAction;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public GroupGuiType getType() {
        return type;
    }

    public Player getPlayer() {
        return player;
    }

    public Group getGroup() {
        return group;
    }

    public int getPage() {
        return page;
    }

    public PlayerSelectAction getSelectAction() {
        return selectAction;
    }

    public void setSlotData(int slot, Object data) {
        slotData.put(slot, data);
    }

    public Object getSlotData(int slot) {
        return slotData.get(slot);
    }

    @SuppressWarnings("unchecked")
    public <T> T getSlotData(int slot, Class<T> clazz) {
        Object val = slotData.get(slot);
        if (val != null && clazz.isInstance(val)) {
            return (T) val;
        }
        return null;
    }
}
