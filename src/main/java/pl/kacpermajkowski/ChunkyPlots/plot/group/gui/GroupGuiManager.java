package pl.kacpermajkowski.ChunkyPlots.plot.group.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.plot.group.Group;
import pl.kacpermajkowski.ChunkyPlots.plot.gui.PlayerSelectAction;
import pl.kacpermajkowski.ChunkyPlots.plot.map.PlotMapManager;
import pl.kacpermajkowski.ChunkyPlots.user.User;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.*;

public class GroupGuiManager {
    private final PlotManager plotManager;
    private final UserManager userManager;
    private final PlotMapManager mapManager;

    public GroupGuiManager(PlotManager plotManager, UserManager userManager, PlotMapManager mapManager) {
        this.plotManager = plotManager;
        this.userManager = userManager;
        this.mapManager = mapManager;
    }

    public void openGroupListGui(Player player) {
        if (player == null) return;
        User user = userManager != null ? userManager.getUser(player) : null;
        if (user == null) return;

        GroupGuiHolder holder = new GroupGuiHolder(GroupGuiType.GROUP_LIST, player, null);
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent("§8Twoje grupy działek"));
        holder.setInventory(inv);

        List<Group> groups = user.getGroups();
        int slot = 0;
        for (Group group : groups) {
            if (slot >= 36) break;
            List<Plot> groupPlots = group.getPlots();
            boolean isDef = group.isDefault();

            List<String> lore = new ArrayList<>();
            if (isDef) {
                lore.add("§7Domyślna grupa encapsulująca wszystkie Twoje działki.");
            } else {
                lore.add("§7Własna grupa działek zdefiniowana przez Ciebie.");
            }
            lore.add("§7Liczba działek: §e" + groupPlots.size());

            if (!groupPlots.isEmpty()) {
                lore.add("");
                lore.add("§7Działki w grupie:");
                int maxShow = Math.min(groupPlots.size(), 4);
                for (int i = 0; i < maxShow; i++) {
                    Plot p = groupPlots.get(i);
                    lore.add("§8• §f" + p.getMnemonicId() + " §7(" + p.getID() + ")");
                }
                if (groupPlots.size() > 4) {
                    lore.add("§8... i jeszcze " + (groupPlots.size() - 4) + " więcej");
                }
            } else {
                lore.add("");
                lore.add("§7Brak przypisanych działek.");
            }

            lore.add("");
            lore.add("§eLPM: §fZarządzaj tą grupą");
            if (!groupPlots.isEmpty()) {
                lore.add("§6PPM: §fPokaż na mapie chunków");
            }

            Material mat = isDef ? Material.BEACON : Material.CHEST;
            String title = (isDef ? "§6§lGrupa domyślna: §e" : "§b§lGrupa: §f") + group.getName();
            inv.setItem(slot, GroupGuiHolder.createItem(mat, title, lore));
            holder.setSlotData(slot, group);
            slot++;
        }

        // Bottom control row
        ItemStack frame = GroupGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, frame);
        }

        inv.setItem(45, GroupGuiHolder.createItem(Material.ARROW, "§e§l← Powrót", List.of("§7Kliknij, aby zamknąć lub wrócić.")));
        holder.setSlotData(45, "BACK");

        inv.setItem(49, GroupGuiHolder.createItem(
                Material.EMERALD,
                "§a§l+ Utwórz nową grupę",
                List.of(
                        "§7Kliknij, aby dowiedzieć się jak utworzyć grupę",
                        "§7lub wpisz: §f/plot group create <nazwa>"
                )
        ));
        holder.setSlotData(49, "CREATE");

        inv.setItem(53, GroupGuiHolder.createItem(Material.BARRIER, "§c§lZamknij", List.of("§7Kliknij, aby zamknąć menu.")));
        holder.setSlotData(53, "CLOSE");

        player.openInventory(inv);
    }

    public void openGroupDashboardGui(Player player, Group group) {
        if (player == null || group == null) return;

        GroupGuiHolder holder = new GroupGuiHolder(GroupGuiType.GROUP_DASHBOARD, player, group);
        String title = (group.isDefault() ? "§8Grupa domyślna: §6" : "§8Grupa: §6") + group.getName();
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent(title));
        holder.setInventory(inv);

        // Frame
        ItemStack frame = GroupGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, frame);
            inv.setItem(i + 45, frame);
        }
        for (int r = 1; r < 5; r++) {
            inv.setItem(r * 9, frame);
            inv.setItem(r * 9 + 8, frame);
        }

        List<Plot> plots = group.getPlots();

        // Slot 4: Group Header
        List<String> headerLore = new ArrayList<>();
        headerLore.add("§7Nazwa grupy: §e" + group.getName());
        headerLore.add("§7Typ: §f" + (group.isDefault() ? "Domyślna (wszystkie Twoje działki)" : "Własna grupa"));
        headerLore.add("§7Liczba działek: §a" + plots.size());
        if (!plots.isEmpty()) {
            headerLore.add("");
            headerLore.add("§7Działki:");
            int show = Math.min(plots.size(), 5);
            for (int i = 0; i < show; i++) {
                Plot p = plots.get(i);
                headerLore.add("§8• §f" + p.getMnemonicId() + " §7[" + p.getChunkX() + ", " + p.getChunkZ() + "]");
            }
            if (plots.size() > 5) {
                headerLore.add("§8... i jeszcze " + (plots.size() - 5));
            }
        }
        inv.setItem(4, GroupGuiHolder.createItem(
                group.isDefault() ? Material.BEACON : Material.GOLD_BLOCK,
                "§6§lInformacje o grupie: §e" + group.getName(),
                headerLore
        ));

        // Slot 19: Plots in group
        inv.setItem(19, GroupGuiHolder.createItem(
                Material.CHEST,
                "§e§lDziałki w grupie (" + plots.size() + ")",
                List.of(
                        "§7Przeglądaj działki należące do tej grupy,",
                        group.isDefault() ? "§7(Grupa 'all' zawiera automatycznie wszystkie działki)." : "§7dodawaj nowe lub usuwaj przypisane działki.",
                        "",
                        "§eKliknij, aby otworzyć listę działek!"
                )
        ));
        holder.setSlotData(19, "PLOTS");

        // Slot 21: Group flags
        inv.setItem(21, GroupGuiHolder.createItem(
                Material.REDSTONE_TORCH,
                "§c§lFlagi ochrony grupy",
                List.of(
                        "§7Konfiguruj flagi bezpieczeństwa hurtowo",
                        "§7dla wszystkich działek w tej grupie.",
                        "",
                        "§eKliknij, aby zarządzać flagami grupy!"
                )
        ));
        holder.setSlotData(21, "FLAGS");

        // Slot 23: Group Whitelist
        Set<UUID> commonMembers = getGroupMembers(group);
        inv.setItem(23, GroupGuiHolder.createItem(
                Material.PLAYER_HEAD,
                "§a§lBiała lista grupy (Członkowie)",
                List.of(
                        "§7Zarządzaj członkami uprawnionymi do budowania",
                        "§7na wszystkich działkach tej grupy.",
                        "§7Wspólni członkowie: §b" + commonMembers.size(),
                        "",
                        "§eKliknij, aby zarządzać członkami grupy!"
                )
        ));
        holder.setSlotData(23, "WHITELIST");

        // Slot 25: Group Blacklist
        Set<UUID> commonBlacklist = getGroupBlacklist(group);
        inv.setItem(25, GroupGuiHolder.createItem(
                Material.WITHER_SKELETON_SKULL,
                "§c§lCzarna lista grupy (Zablokowani)",
                List.of(
                        "§7Zarządzaj graczami z zakazem wstępu",
                        "§7na teren wszystkich działek tej grupy.",
                        "§7Zablokowani: §c" + commonBlacklist.size(),
                        "",
                        "§eKliknij, aby zarządzać czarną listą grupy!"
                )
        ));
        holder.setSlotData(25, "BLACKLIST");

        // Slot 31: Open on Chunk Map
        if (!plots.isEmpty() && mapManager != null) {
            inv.setItem(31, GroupGuiHolder.createItem(
                    Material.FILLED_MAP,
                    "§6§lPokaż działki na mapie chunków",
                    List.of(
                            "§7Otwiera interaktywną mapę chunków wyśrodkowaną",
                            "§7na pierwszej działce z tej grupy.",
                            "",
                            "§eKliknij, aby otworzyć mapę!"
                    )
            ));
            holder.setSlotData(31, "MAP");
        }

        // Slot 33: Delete custom group
        if (!group.isDefault()) {
            inv.setItem(33, GroupGuiHolder.createItem(
                    Material.TNT,
                    "§4§lUsuń tę grupę",
                    List.of(
                            "§cUsuwa tę grupę działek.",
                            "§7Same działki NIE zostaną usunięte,",
                            "§7jedynie ich przypisanie do tej grupy.",
                            "",
                            "§cKliknij, aby usunąć grupę!"
                    )
            ));
            holder.setSlotData(33, "DELETE");
        }

        // Bottom navigation
        inv.setItem(45, GroupGuiHolder.createItem(Material.ARROW, "§e§l← Powrót do listy grup", List.of()));
        holder.setSlotData(45, "BACK");

        inv.setItem(53, GroupGuiHolder.createItem(Material.BARRIER, "§c§lZamknij", List.of()));
        holder.setSlotData(53, "CLOSE");

        player.openInventory(inv);
    }

    public void openGroupPlotsGui(Player player, Group group, int page) {
        if (player == null || group == null) return;

        GroupGuiHolder holder = new GroupGuiHolder(GroupGuiType.GROUP_PLOTS, player, group, page, null);
        String title = "§8Działki w grupie: §6" + group.getName();
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent(title));
        holder.setInventory(inv);

        List<Plot> plots = group.getPlots();
        int pageSize = 36;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, plots.size());

        for (int i = startIndex; i < endIndex; i++) {
            Plot p = plots.get(i);
            int slot = i - startIndex;

            int wl = p.getWhitelist() != null ? p.getWhitelist().size() : 0;
            int bl = p.getBlacklist() != null ? p.getBlacklist().size() : 0;

            List<String> lore = new ArrayList<>();
            lore.add("§7Kod: §e" + p.getMnemonicId());
            lore.add("§7Świat: §f" + p.getWorldName());
            lore.add("§7Chunk: §e[" + p.getChunkX() + ", " + p.getChunkZ() + "]");
            lore.add("§7Członkowie: §b" + wl + " §8| §7Zablokowani: §c" + bl);
            lore.add("");
            lore.add("§eLPM: §fPokaż na mapie chunków");
            if (!group.isDefault()) {
                lore.add("§cPPM: §fUsuń tę działkę z grupy");
            }

            inv.setItem(slot, GroupGuiHolder.createItem(
                    Material.GRASS_BLOCK,
                    "§a§lDziałka: §f" + p.getID() + " §7(" + p.getMnemonicId() + ")",
                    lore
            ));
            holder.setSlotData(slot, p);
        }

        // Bottom control row
        ItemStack frame = GroupGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, frame);
        }

        inv.setItem(45, GroupGuiHolder.createItem(Material.ARROW, "§e§l← Powrót do grupy", List.of()));
        holder.setSlotData(45, "BACK");

        if (!group.isDefault()) {
            inv.setItem(49, GroupGuiHolder.createItem(
                    Material.EMERALD,
                    "§a§l+ Dodaj działkę do grupy",
                    List.of(
                            "§7Wybierz z listy Twoich pozostałych działek,",
                            "§7aby przypisać ją do tej grupy."
                    )
            ));
            holder.setSlotData(49, "ADD_PLOT");
        } else {
            inv.setItem(49, GroupGuiHolder.createItem(
                    Material.BEACON,
                    "§6§lGrupa domyślna",
                    List.of("§7Grupa 'all' zawiera automatycznie wszystkie Twoje działki.")
            ));
        }

        if (page > 0) {
            inv.setItem(47, GroupGuiHolder.createItem(Material.PAPER, "§e← Poprzednia strona (" + page + ")", List.of()));
            holder.setSlotData(47, "PREV_PAGE");
        }
        if (endIndex < plots.size()) {
            inv.setItem(51, GroupGuiHolder.createItem(Material.PAPER, "§eNastępna strona (" + (page + 2) + ") →", List.of()));
            holder.setSlotData(51, "NEXT_PAGE");
        }

        inv.setItem(53, GroupGuiHolder.createItem(Material.BARRIER, "§c§lZamknij", List.of()));
        holder.setSlotData(53, "CLOSE");

        player.openInventory(inv);
    }

    public void openGroupAddPlotGui(Player player, Group group, int page) {
        if (player == null || group == null) return;

        GroupGuiHolder holder = new GroupGuiHolder(GroupGuiType.GROUP_ADD_PLOT, player, group, page, null);
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent("§8Dodaj działkę do: §6" + group.getName()));
        holder.setInventory(inv);

        List<Plot> allOwned = plotManager != null ? plotManager.getPlotsByOwner(player.getUniqueId()) : List.of();
        List<Plot> availableToAdd = new ArrayList<>();
        for (Plot p : allOwned) {
            if (!group.contains(p)) {
                availableToAdd.add(p);
            }
        }

        int pageSize = 36;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, availableToAdd.size());

        for (int i = startIndex; i < endIndex; i++) {
            Plot p = availableToAdd.get(i);
            int slot = i - startIndex;

            inv.setItem(slot, GroupGuiHolder.createItem(
                    Material.OAK_SAPLING,
                    "§a§lDodaj działkę: §f" + p.getID(),
                    List.of(
                            "§7Kod: §e" + p.getMnemonicId(),
                            "§7Świat: §f" + p.getWorldName(),
                            "§7Chunk: §e[" + p.getChunkX() + ", " + p.getChunkZ() + "]",
                            "",
                            "§aKliknij LPM, aby dodać tę działkę do grupy! "
                    )
            ));
            holder.setSlotData(slot, p);
        }

        // Bottom control row
        ItemStack frame = GroupGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, frame);
        }

        inv.setItem(45, GroupGuiHolder.createItem(Material.ARROW, "§e§l← Powrót", List.of()));
        holder.setSlotData(45, "BACK");

        inv.setItem(49, GroupGuiHolder.createItem(
                Material.COMPASS,
                "§6§lDostępne działki (" + availableToAdd.size() + ")",
                List.of("§7Wybierz działkę, którą chcesz dołączyć do tej grupy.")
        ));

        if (page > 0) {
            inv.setItem(47, GroupGuiHolder.createItem(Material.PAPER, "§e← Poprzednia strona (" + page + ")", List.of()));
            holder.setSlotData(47, "PREV_PAGE");
        }
        if (endIndex < availableToAdd.size()) {
            inv.setItem(51, GroupGuiHolder.createItem(Material.PAPER, "§eNastępna strona (" + (page + 2) + ") →", List.of()));
            holder.setSlotData(51, "NEXT_PAGE");
        }

        inv.setItem(53, GroupGuiHolder.createItem(Material.BARRIER, "§c§lZamknij", List.of()));
        holder.setSlotData(53, "CLOSE");

        player.openInventory(inv);
    }

    public void openGroupFlagsGui(Player player, Group group, int page) {
        if (player == null || group == null) return;

        GroupGuiHolder holder = new GroupGuiHolder(GroupGuiType.GROUP_FLAGS, player, group, page, null);
        String title = "§8Flagi grupy: §6" + group.getName();
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent(title));
        holder.setInventory(inv);

        PlotFlag[] allFlags = PlotFlag.values();
        int pageSize = 36;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allFlags.length);

        for (int i = startIndex; i < endIndex; i++) {
            PlotFlag flag = allFlags[i];
            int slot = i - startIndex;

            Group.GroupFlagStatus status = group.getFlagStatus(flag);
            Material dyeMat = switch (status) {
                case ALL_TRUE -> Material.LIME_DYE;
                case ALL_FALSE -> Material.GRAY_DYE;
                case DIFFERENT -> Material.ORANGE_DYE;
                case EMPTY -> Material.BONE_MEAL;
            };

            String statusLabel = switch (status) {
                case ALL_TRUE -> "§a§l[WŁĄCZONA NA WSZYSTKICH / true]";
                case ALL_FALSE -> "§c§l[WYŁĄCZONA NA WSZYSTKICH / false]";
                case DIFFERENT -> "§e§l[RÓŻNE WARTOŚCI NA DZIAŁKACH]";
                case EMPTY -> "§7§l[BRAK DZIAŁEK W GRUPIE]";
            };

            List<String> lore = new ArrayList<>();
            lore.add("§7Opis: §f" + flag.getDescription());
            lore.add("§7Domyślna wartość: §8" + (flag.getDefaultValue() ? "true" : "false"));
            lore.add("");
            lore.add("§7Status grupy: " + statusLabel);
            lore.add("");
            lore.add("§eLPM: §fWłącz na wszystkich działkach grupy (true)");
            lore.add("§6PPM: §fWyłącz na wszystkich działkach grupy (false)");
            lore.add("§bShift + LPM: §fZresetuj do wartości domyślnej");

            inv.setItem(slot, GroupGuiHolder.createItem(dyeMat, "§f§l" + flag.getName(), lore));
            holder.setSlotData(slot, flag);
        }

        // Bottom control row
        ItemStack frame = GroupGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, frame);
        }

        inv.setItem(45, GroupGuiHolder.createItem(Material.ARROW, "§e§l← Powrót do grupy", List.of()));
        holder.setSlotData(45, "BACK");

        inv.setItem(49, GroupGuiHolder.createItem(
                Material.TNT,
                "§4§lZresetuj wszystkie flagi grupy",
                List.of(
                        "§cResetuje wszystkie 26 flag do ich",
                        "§cwartości domyślnych na wszystkich działkach grupy."
                )
        ));
        holder.setSlotData(49, "RESET_ALL");

        if (page > 0) {
            inv.setItem(47, GroupGuiHolder.createItem(Material.PAPER, "§e← Poprzednia strona (" + page + ")", List.of()));
            holder.setSlotData(47, "PREV_PAGE");
        }
        if (endIndex < allFlags.length) {
            inv.setItem(51, GroupGuiHolder.createItem(Material.PAPER, "§eNastępna strona (" + (page + 2) + ") →", List.of()));
            holder.setSlotData(51, "NEXT_PAGE");
        }

        inv.setItem(53, GroupGuiHolder.createItem(Material.BARRIER, "§c§lZamknij", List.of()));
        holder.setSlotData(53, "CLOSE");

        player.openInventory(inv);
    }

    public void openGroupMembersGui(Player player, Group group, int page) {
        if (player == null || group == null) return;

        GroupGuiHolder holder = new GroupGuiHolder(GroupGuiType.GROUP_MEMBERS, player, group, page, null);
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent("§8Biała lista grupy: §6" + group.getName()));
        holder.setInventory(inv);

        List<UUID> members = new ArrayList<>(getGroupMembers(group));
        int pageSize = 36;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, members.size());

        for (int i = startIndex; i < endIndex; i++) {
            UUID memberUuid = members.get(i);
            int slot = i - startIndex;
            String memberName = resolvePlayerName(memberUuid);

            ItemStack skull = GroupGuiHolder.createItem(Material.PLAYER_HEAD);
            ItemMeta meta = skull != null ? skull.getItemMeta() : null;
            if (meta instanceof SkullMeta skullMeta) {
                try {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(memberUuid);
                    skullMeta.setOwningPlayer(op);
                } catch (Throwable ignored) {}
            }
            if (meta != null) {
                meta.displayName(TextUtil.toComponent("§b§l" + memberName));
                meta.lore(List.of(
                        TextUtil.toComponent("§7UUID: §8" + memberUuid),
                        Component.empty(),
                        TextUtil.toComponent("§c§lKliknij LPM, aby usunąć z białej listy grupy!")
                ));
                skull.setItemMeta(meta);
            }
            inv.setItem(slot, skull);
            holder.setSlotData(slot, memberUuid);
        }

        // Bottom control row
        ItemStack frame = GroupGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, frame);
        }

        inv.setItem(45, GroupGuiHolder.createItem(Material.ARROW, "§e§l← Powrót do grupy", List.of()));
        holder.setSlotData(45, "BACK");

        inv.setItem(49, GroupGuiHolder.createItem(
                Material.EMERALD,
                "§a§l+ Dodaj gracza do grupy",
                List.of(
                        "§7Wybierz gracza online, aby dodać go",
                        "§7jako członka na wszystkich działkach tej grupy."
                )
        ));
        holder.setSlotData(49, "ADD_MEMBER");

        if (page > 0) {
            inv.setItem(47, GroupGuiHolder.createItem(Material.PAPER, "§e← Poprzednia strona (" + page + ")", List.of()));
            holder.setSlotData(47, "PREV_PAGE");
        }
        if (endIndex < members.size()) {
            inv.setItem(51, GroupGuiHolder.createItem(Material.PAPER, "§eNastępna strona (" + (page + 2) + ") →", List.of()));
            holder.setSlotData(51, "NEXT_PAGE");
        }

        inv.setItem(53, GroupGuiHolder.createItem(Material.BARRIER, "§c§lZamknij", List.of()));
        holder.setSlotData(53, "CLOSE");

        player.openInventory(inv);
    }

    public void openGroupBlacklistGui(Player player, Group group, int page) {
        if (player == null || group == null) return;

        GroupGuiHolder holder = new GroupGuiHolder(GroupGuiType.GROUP_BLACKLIST, player, group, page, null);
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent("§8Czarna lista grupy: §6" + group.getName()));
        holder.setInventory(inv);

        List<UUID> blacklisted = new ArrayList<>(getGroupBlacklist(group));
        int pageSize = 36;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, blacklisted.size());

        for (int i = startIndex; i < endIndex; i++) {
            UUID bannedUuid = blacklisted.get(i);
            int slot = i - startIndex;
            String bannedName = resolvePlayerName(bannedUuid);

            ItemStack skull = GroupGuiHolder.createItem(Material.WITHER_SKELETON_SKULL);
            ItemMeta meta = skull != null ? skull.getItemMeta() : null;
            if (meta != null) {
                meta.displayName(TextUtil.toComponent("§c§l" + bannedName));
                meta.lore(List.of(
                        TextUtil.toComponent("§7UUID: §8" + bannedUuid),
                        Component.empty(),
                        TextUtil.toComponent("§a§lKliknij LPM, aby zdjąć blokadę w całej grupie!")
                ));
                skull.setItemMeta(meta);
            }
            inv.setItem(slot, skull);
            holder.setSlotData(slot, bannedUuid);
        }

        // Bottom control row
        ItemStack frame = GroupGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, frame);
        }

        inv.setItem(45, GroupGuiHolder.createItem(Material.ARROW, "§e§l← Powrót do grupy", List.of()));
        holder.setSlotData(45, "BACK");

        inv.setItem(49, GroupGuiHolder.createItem(
                Material.REDSTONE,
                "§c§l+ Zablokuj gracza w grupie",
                List.of(
                        "§7Wybierz gracza online, aby zablokować mu wstęp",
                        "§7na wszystkie działki tej grupy."
                )
        ));
        holder.setSlotData(49, "ADD_BLACKLIST");

        if (page > 0) {
            inv.setItem(47, GroupGuiHolder.createItem(Material.PAPER, "§e← Poprzednia strona (" + page + ")", List.of()));
            holder.setSlotData(47, "PREV_PAGE");
        }
        if (endIndex < blacklisted.size()) {
            inv.setItem(51, GroupGuiHolder.createItem(Material.PAPER, "§eNastępna strona (" + (page + 2) + ") →", List.of()));
            holder.setSlotData(51, "NEXT_PAGE");
        }

        inv.setItem(53, GroupGuiHolder.createItem(Material.BARRIER, "§c§lZamknij", List.of()));
        holder.setSlotData(53, "CLOSE");

        player.openInventory(inv);
    }

    public void openGroupSelectPlayerGui(Player player, Group group, PlayerSelectAction action, int page) {
        if (player == null || group == null || action == null) return;

        GroupGuiHolder holder = new GroupGuiHolder(GroupGuiType.GROUP_SELECT_PLAYER, player, group, page, action);
        String actionTitle = action == PlayerSelectAction.ADD_WHITELIST ? "Dodaj członka do grupy" : "Zablokuj gracza w grupie";
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent("§8" + actionTitle));
        holder.setInventory(inv);

        List<Player> eligible = new ArrayList<>();
        if (Bukkit.getServer() != null) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target == null) continue;
                if (target.getUniqueId().equals(player.getUniqueId())) continue;
                eligible.add(target);
            }
        }

        int pageSize = 36;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, eligible.size());

        for (int i = startIndex; i < endIndex; i++) {
            Player target = eligible.get(i);
            int slot = i - startIndex;

            ItemStack skull = GroupGuiHolder.createItem(Material.PLAYER_HEAD);
            ItemMeta meta = skull != null ? skull.getItemMeta() : null;
            if (meta instanceof SkullMeta skullMeta) {
                try {
                    skullMeta.setOwningPlayer(target);
                } catch (Throwable ignored) {}
            }
            if (meta != null) {
                meta.displayName(TextUtil.toComponent("§f§l" + target.getName()));
                meta.lore(List.of(
                        TextUtil.toComponent("§7UUID: §8" + target.getUniqueId()),
                        Component.empty(),
                        TextUtil.toComponent(action == PlayerSelectAction.ADD_WHITELIST
                                ? "§a§lKliknij, aby dodać do białej listy grupy!"
                                : "§c§lKliknij, aby zablokować w całej grupie!")
                ));
                skull.setItemMeta(meta);
            }
            inv.setItem(slot, skull);
            holder.setSlotData(slot, target.getUniqueId());
        }

        // Bottom control row
        ItemStack frame = GroupGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, frame);
        }

        inv.setItem(45, GroupGuiHolder.createItem(Material.ARROW, "§e§l← Powrót", List.of()));
        holder.setSlotData(45, "BACK");

        inv.setItem(49, GroupGuiHolder.createItem(
                Material.COMPASS,
                "§6§lDostępni gracze (" + eligible.size() + ")",
                List.of("§7Wybierz gracza, aby zastosować działanie do grupy.")
        ));

        if (page > 0) {
            inv.setItem(47, GroupGuiHolder.createItem(Material.PAPER, "§e← Poprzednia strona (" + page + ")", List.of()));
            holder.setSlotData(47, "PREV_PAGE");
        }
        if (endIndex < eligible.size()) {
            inv.setItem(51, GroupGuiHolder.createItem(Material.PAPER, "§eNastępna strona (" + (page + 2) + ") →", List.of()));
            holder.setSlotData(51, "NEXT_PAGE");
        }

        inv.setItem(53, GroupGuiHolder.createItem(Material.BARRIER, "§c§lZamknij", List.of()));
        holder.setSlotData(53, "CLOSE");

        player.openInventory(inv);
    }

    public void showGroupOnMap(Player player, Group group) {
        if (player == null || group == null || mapManager == null) return;
        List<Plot> plots = group.getPlots();
        if (plots.isEmpty()) {
            new MessageBuilder(Message.EMPTY_GROUP).group(group).sendChat(player);
            return;
        }
        Plot target = plots.get(0);
        mapManager.openMap(player, target);
    }

    private Set<UUID> getGroupMembers(Group group) {
        Set<UUID> members = new LinkedHashSet<>();
        for (Plot p : group.getPlots()) {
            if (p != null && p.getWhitelist() != null) {
                members.addAll(p.getWhitelist());
            }
        }
        return members;
    }

    private Set<UUID> getGroupBlacklist(Group group) {
        Set<UUID> blacklisted = new LinkedHashSet<>();
        for (Plot p : group.getPlots()) {
            if (p != null && p.getBlacklist() != null) {
                blacklisted.addAll(p.getBlacklist());
            }
        }
        return blacklisted;
    }

    private String resolvePlayerName(UUID uuid) {
        if (uuid == null) return "Unknown";
        if (Bukkit.getServer() != null) {
            try {
                OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                if (op != null && op.getName() != null) return op.getName();
            } catch (Throwable ignored) {}
        }
        return uuid.toString().substring(0, 8);
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }
}
