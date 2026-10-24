package pl.kacpermajkowski.ChunkyPlots.plot.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.hook.vault.VaultHook;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.border.PlotBorderVisualizer;
import pl.kacpermajkowski.ChunkyPlots.plot.group.Group;
import pl.kacpermajkowski.ChunkyPlots.plot.limit.PlotLimitManager;
import pl.kacpermajkowski.ChunkyPlots.plot.map.PlotMapManager;
import pl.kacpermajkowski.ChunkyPlots.user.User;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.*;

/**
 * Service managing opening and constructing all Plot Management GUI windows.
 */
public class PlotGuiManager {
    private final Plugin plugin;
    private final PlotManager plotManager;
    private final UserManager userManager;
    private final PlotBorderVisualizer borderVisualizer;
    private final PlotLimitManager limitManager;
    private final VaultHook vaultHook;
    private final PlotMapManager mapManager;

    public PlotGuiManager(Plugin plugin, PlotManager plotManager, UserManager userManager,
                          PlotBorderVisualizer borderVisualizer, PlotLimitManager limitManager,
                          VaultHook vaultHook, PlotMapManager mapManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.userManager = userManager;
        this.borderVisualizer = borderVisualizer;
        this.limitManager = limitManager;
        this.vaultHook = vaultHook;
        this.mapManager = mapManager;
    }

    /**
     * Entry point: opens appropriate GUI for the player.
     * If player is standing on their plot -> opens plot dashboard.
     * Otherwise, if they own plots -> opens plot list.
     * Otherwise opens general stats & controls dashboard.
     */
    public void openMainGui(Player player) {
        if (player == null) return;

        Plot standingPlot = plotManager != null ? plotManager.getPlot(player.getLocation()) : null;
        if (standingPlot != null && standingPlot.isPlayerOwner(player)) {
            openPlotDashboard(player, standingPlot);
            return;
        }

        List<Plot> ownedPlots = plotManager != null ? plotManager.getPlotsByOwner(player.getUniqueId()) : List.of();
        if (!ownedPlots.isEmpty()) {
            if (standingPlot != null) {
                // Standing on another plot or member plot, but owns plots: open dashboard for standing or first owned
                openPlotDashboard(player, standingPlot);
            } else {
                openPlotListGui(player, 0);
            }
        } else {
            // Player owns no plots
            if (standingPlot != null) {
                openPlotDashboard(player, standingPlot);
            } else {
                openPlotListGui(player, 0);
            }
        }
    }

    /**
     * Opens the main management dashboard for a specific plot.
     */
    public void openPlotDashboard(Player player, Plot plot) {
        if (player == null) return;

        PlotGuiHolder holder = new PlotGuiHolder(PlotGuiType.DASHBOARD, player, plot);
        String title = plot != null ? "§8Panel działki: §6" + plot.getID() : "§8Panel działek";
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent(title));
        holder.setInventory(inv);

        // Frame decoration
        ItemStack frame = PlotGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, frame);
            inv.setItem(i + 45, frame);
        }
        for (int r = 1; r < 5; r++) {
            inv.setItem(r * 9, frame);
            inv.setItem(r * 9 + 8, frame);
        }

        boolean isOwner = plot != null && plot.isPlayerOwner(player);
        int chunkX = plot != null ? plot.getChunkX() : player.getLocation().getChunk().getX();
        int chunkZ = plot != null ? plot.getChunkZ() : player.getLocation().getChunk().getZ();
        String worldName = plot != null ? plot.getWorldName() : player.getWorld().getName();
        int minX = chunkX << 4;
        int maxX = minX + 15;
        int minZ = chunkZ << 4;
        int maxZ = minZ + 15;

        // Slot 4: Plot Overview Info
        if (plot != null) {
            String ownerName = plot.getOwnerName();
            int wlCount = plot.getWhitelist() != null ? plot.getWhitelist().size() : 0;
            int blCount = plot.getBlacklist() != null ? plot.getBlacklist().size() : 0;
            inv.setItem(4, PlotGuiHolder.createItem(
                    isOwner ? Material.BEACON : Material.GOLD_BLOCK,
                    (isOwner ? "§a§lTwoja działka: §e" : "§6§lDziałka: §e") + plot.getID(),
                    List.of(
                            "§7Właściciel: §f" + ownerName,
                            "§7Świat: §f" + worldName,
                            "§7Koordynaty chunka: §eX: " + chunkX + ", Z: " + chunkZ,
                            "§7Zakres bloków: §8X: " + minX + ".." + maxX + ", Z: " + minZ + ".." + maxZ,
                            "§7Liczba członków: §b" + wlCount,
                            "§7Liczba zablokowanych: §c" + blCount,
                            "",
                            isOwner ? "§aJesteś właścicielem tej działki." : "§7Status: §fDziałka innego gracza"
                    )
            ));
        } else {
            inv.setItem(4, PlotGuiHolder.createItem(
                    Material.COMPASS,
                    "§e§lAktualny obszar (Dzicz)",
                    List.of(
                            "§7Stoisz na wolnym terenie (brak działki).",
                            "§7Świat: §f" + worldName,
                            "§7Koordynaty chunka: §eX: " + chunkX + ", Z: " + chunkZ,
                            "§7Zakres bloków: §8X: " + minX + ".." + maxX + ", Z: " + minZ + ".." + maxZ,
                            "",
                            "§aMożesz postawić tu blok działki!"
                    )
            ));
        }

        // Slot 19: Whitelist (Members)
        int wlCount = (plot != null && plot.getWhitelist() != null) ? plot.getWhitelist().size() : 0;
        inv.setItem(19, PlotGuiHolder.createItem(
                Material.PLAYER_HEAD,
                "§a§lBiała lista (Członkowie)",
                List.of(
                        "§7Zarządzaj graczami uprawnionymi do budowania",
                        "§7i korzystania z Twojej działki.",
                        "§7Aktualna liczba członków: §b" + wlCount,
                        "",
                        isOwner ? "§eKliknij, aby zarządzać członkami!" : "§7Dostępne tylko dla właściciela."
                )
        ));
        holder.setSlotData(19, "WHITELIST");

        // Slot 21: Blacklist (Banned players)
        int blCount = (plot != null && plot.getBlacklist() != null) ? plot.getBlacklist().size() : 0;
        inv.setItem(21, PlotGuiHolder.createItem(
                Material.WITHER_SKELETON_SKULL,
                "§c§lCzarna lista (Zablokowani)",
                List.of(
                        "§7Zarządzaj graczami z zakazem wstępu",
                        "§7na teren Twojej działki.",
                        "§7Aktualna liczba zablokowanych: §c" + blCount,
                        "",
                        isOwner ? "§eKliknij, aby zarządzać zablokowanymi!" : "§7Dostępne tylko dla właściciela."
                )
        ));
        holder.setSlotData(21, "BLACKLIST");

        // Slot 23: Particle Border Toggle
        boolean isVisualizing = borderVisualizer != null && borderVisualizer.isVisualizing(player);
        inv.setItem(23, PlotGuiHolder.createItem(
                Material.ENDER_EYE,
                "§b§lGranica działki (Cząsteczki)",
                List.of(
                        "§7Aktualny status: " + (isVisualizing ? "§a§lWŁĄCZONA" : "§c§lWYŁĄCZONA"),
                        "§7Wyświetla cząsteczkową granicę chunku",
                        "§7widoczną tylko dla Ciebie (przez 10 minut).",
                        "",
                        "§eKliknij, aby przełączyć widoczność granicy!"
                )
        ));
        holder.setSlotData(23, "BORDER");

        // Slot 25: Interactive Chunk Map
        inv.setItem(25, PlotGuiHolder.createItem(
                Material.FILLED_MAP,
                "§6§lInteraktywna mapa chunków",
                List.of(
                        "§7Otwiera pełną mapę chunków 9x5 wokół działki",
                        "§7ze sterowaniem kierunkami i podglądem sąsiadów.",
                        "",
                        "§eKliknij, aby otworzyć mapę chunków!"
                )
        ));
        holder.setSlotData(25, "MAP");

        // Slot 29: My Plots List
        int ownedCount = limitManager != null ? limitManager.getPlotCount(player) : 0;
        int plotLimit = limitManager != null ? limitManager.getPlotLimit(player) : 1;
        String limitStr = plotLimit == Integer.MAX_VALUE ? "∞" : String.valueOf(plotLimit);
        inv.setItem(29, PlotGuiHolder.createItem(
                Material.CHEST,
                "§e§lTwoje działki (" + ownedCount + "/" + limitStr + ")",
                List.of(
                        "§7Przejrzyj wszystkie posiadane przez Ciebie działki",
                        "§7i szybko przełączaj się między nimi.",
                        "",
                        "§eKliknij, aby wyświetlić listę Twoich działek!"
                )
        ));
        holder.setSlotData(29, "PLOT_LIST");

        // Slot 31: Stats & Limits
        int remaining = limitManager != null ? limitManager.getRemainingPlots(player) : 0;
        String remainingStr = plotLimit == Integer.MAX_VALUE ? "∞" : String.valueOf(remaining);
        String groupName = (vaultHook != null && vaultHook.isHooked()) ? vaultHook.getPrimaryGroup(player) : "default";
        inv.setItem(31, PlotGuiHolder.createItem(
                Material.BOOK,
                "§3§lInformacje i Limity konta",
                List.of(
                        "§7Gracz: §f" + player.getName(),
                        "§7Posiadane działki: §f" + ownedCount,
                        "§7Maksymalny limit: §a" + limitStr,
                        "§7Pozostało wolnych: §e" + remainingStr,
                        "§7Grupa uprawnień: §d" + groupName
                )
        ));

        // Slot 33: Dispose Plot (Only visible and usable by owner)
        if (isOwner) {
            inv.setItem(33, PlotGuiHolder.createItem(
                    Material.TNT,
                    "§4§lUsuń tę działkę (Dispose)",
                    List.of(
                            "§cTrwale usuwa tę działkę z serwera!",
                            "§7Otrzymasz z powrotem 1 blok działki.",
                            "",
                            "§cKliknij, aby przejść do potwierdzenia!"
                    )
            ));
            holder.setSlotData(33, "DISPOSE");
        }

        // Slot 49: Close
        inv.setItem(49, PlotGuiHolder.createItem(
                Material.BARRIER,
                "§c§lZamknij menu",
                List.of("§7Kliknij, aby zamknąć to menu.")
        ));
        holder.setSlotData(49, "CLOSE");

        player.openInventory(inv);
    }

    /**
     * Opens whitelist management GUI showing current members and add button.
     */
    public void openWhitelistGui(Player player, Plot plot, int page) {
        if (player == null || plot == null) return;

        PlotGuiHolder holder = new PlotGuiHolder(PlotGuiType.WHITELIST, player, plot, page, null);
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent("§8Biała lista: §6" + plot.getID()));
        holder.setInventory(inv);

        List<UUID> members = new ArrayList<>(plot.getWhitelist());
        int pageSize = 36;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, members.size());

        for (int i = startIndex; i < endIndex; i++) {
            UUID memberUuid = members.get(i);
            int slot = i - startIndex;
            String memberName = resolvePlayerName(memberUuid);

            ItemStack skull = PlotGuiHolder.createItem(Material.PLAYER_HEAD);
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
                        TextUtil.toComponent("§c§lKliknij LPM, aby usunąć z białej listy!")
                ));
                skull.setItemMeta(meta);
            }
            inv.setItem(slot, skull);
            holder.setSlotData(slot, memberUuid);
        }

        // Bottom control row
        ItemStack frame = PlotGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, frame);
        }

        inv.setItem(45, PlotGuiHolder.createItem(Material.ARROW, "§e§l← Powrót do panelu", List.of()));
        holder.setSlotData(45, "BACK");

        inv.setItem(49, PlotGuiHolder.createItem(
                Material.EMERALD,
                "§a§l+ Dodaj gracza do białej listy",
                List.of(
                        "§7Kliknij, aby wybrać gracza online",
                        "§7i dodać go do tej działki."
                )
        ));
        holder.setSlotData(49, "ADD");

        if (page > 0) {
            inv.setItem(47, PlotGuiHolder.createItem(Material.PAPER, "§e← Poprzednia strona (" + page + ")", List.of()));
            holder.setSlotData(47, "PREV_PAGE");
        }
        if (endIndex < members.size()) {
            inv.setItem(51, PlotGuiHolder.createItem(Material.PAPER, "§eNastępna strona (" + (page + 2) + ") →", List.of()));
            holder.setSlotData(51, "NEXT_PAGE");
        }

        inv.setItem(53, PlotGuiHolder.createItem(Material.BARRIER, "§c§lZamknij", List.of()));
        holder.setSlotData(53, "CLOSE");

        player.openInventory(inv);
    }

    /**
     * Opens blacklist management GUI showing banned players and add button.
     */
    public void openBlacklistGui(Player player, Plot plot, int page) {
        if (player == null || plot == null) return;

        PlotGuiHolder holder = new PlotGuiHolder(PlotGuiType.BLACKLIST, player, plot, page, null);
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent("§8Czarna lista: §6" + plot.getID()));
        holder.setInventory(inv);

        List<UUID> blacklisted = new ArrayList<>(plot.getBlacklist());
        int pageSize = 36;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, blacklisted.size());

        for (int i = startIndex; i < endIndex; i++) {
            UUID bannedUuid = blacklisted.get(i);
            int slot = i - startIndex;
            String bannedName = resolvePlayerName(bannedUuid);

            ItemStack skull = PlotGuiHolder.createItem(Material.WITHER_SKELETON_SKULL);
            ItemMeta meta = skull != null ? skull.getItemMeta() : null;
            if (meta != null) {
                meta.displayName(TextUtil.toComponent("§c§l" + bannedName));
                meta.lore(List.of(
                        TextUtil.toComponent("§7UUID: §8" + bannedUuid),
                        Component.empty(),
                        TextUtil.toComponent("§a§lKliknij LPM, aby zdjąć blokadę (odblokować)!")
                ));
                skull.setItemMeta(meta);
            }
            inv.setItem(slot, skull);
            holder.setSlotData(slot, bannedUuid);
        }

        // Bottom control row
        ItemStack frame = PlotGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, frame);
        }

        inv.setItem(45, PlotGuiHolder.createItem(Material.ARROW, "§e§l← Powrót do panelu", List.of()));
        holder.setSlotData(45, "BACK");

        inv.setItem(49, PlotGuiHolder.createItem(
                Material.REDSTONE,
                "§c§l+ Dodaj gracza do czarnej listy",
                List.of(
                        "§7Kliknij, aby wybrać gracza online",
                        "§7i zablokować mu wstęp na tę działkę."
                )
        ));
        holder.setSlotData(49, "ADD");

        if (page > 0) {
            inv.setItem(47, PlotGuiHolder.createItem(Material.PAPER, "§e← Poprzednia strona (" + page + ")", List.of()));
            holder.setSlotData(47, "PREV_PAGE");
        }
        if (endIndex < blacklisted.size()) {
            inv.setItem(51, PlotGuiHolder.createItem(Material.PAPER, "§eNastępna strona (" + (page + 2) + ") →", List.of()));
            holder.setSlotData(51, "NEXT_PAGE");
        }

        inv.setItem(53, PlotGuiHolder.createItem(Material.BARRIER, "§c§lZamknij", List.of()));
        holder.setSlotData(53, "CLOSE");

        player.openInventory(inv);
    }

    /**
     * Opens player picker GUI showing online eligible players to whitelist or blacklist.
     */
    public void openPlayerSelectGui(Player player, Plot plot, PlayerSelectAction action, int page) {
        if (player == null || plot == null || action == null) return;

        PlotGuiHolder holder = new PlotGuiHolder(PlotGuiType.PLAYER_SELECT, player, plot, page, action);
        String actionTitle = action == PlayerSelectAction.ADD_WHITELIST ? "Dodaj do białej listy" : "Zablokuj gracza";
        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent("§8" + actionTitle));
        holder.setInventory(inv);

        // Gather eligible online players
        List<Player> eligible = new ArrayList<>();
        if (Bukkit.getServer() != null) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target == null) continue;
                // Cannot select owner
                if (target.getUniqueId().equals(player.getUniqueId()) || plot.isPlayerOwner(target)) continue;
                // Whitelist filter: not already whitelisted and not blacklisted
                if (action == PlayerSelectAction.ADD_WHITELIST) {
                    if (plot.isPlayerWhitelisted(target) || plot.isPlayerBlacklisted(target)) continue;
                }
                // Blacklist filter: not already blacklisted and not whitelisted
                if (action == PlayerSelectAction.ADD_BLACKLIST) {
                    if (plot.isPlayerBlacklisted(target)) continue;
                }
                eligible.add(target);
            }
        }

        int pageSize = 36;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, eligible.size());

        for (int i = startIndex; i < endIndex; i++) {
            Player target = eligible.get(i);
            int slot = i - startIndex;

            ItemStack skull = PlotGuiHolder.createItem(Material.PLAYER_HEAD);
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
                                ? "§a§lKliknij, aby dodać do białej listy!"
                                : "§c§lKliknij, aby zablokować wstęp!")
                ));
                skull.setItemMeta(meta);
            }
            inv.setItem(slot, skull);
            holder.setSlotData(slot, target.getUniqueId());
        }

        // Bottom control row
        ItemStack frame = PlotGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, frame);
        }

        inv.setItem(45, PlotGuiHolder.createItem(Material.ARROW, "§e§l← Powrót", List.of()));
        holder.setSlotData(45, "BACK");

        inv.setItem(49, PlotGuiHolder.createItem(
                Material.COMPASS,
                "§6§lDostępni gracze online (" + eligible.size() + ")",
                List.of(
                        "§7Wybierz gracza z listy powyżej,",
                        "§7aby natychmiast zastosować działanie."
                )
        ));

        if (page > 0) {
            inv.setItem(47, PlotGuiHolder.createItem(Material.PAPER, "§e← Poprzednia strona (" + page + ")", List.of()));
            holder.setSlotData(47, "PREV_PAGE");
        }
        if (endIndex < eligible.size()) {
            inv.setItem(51, PlotGuiHolder.createItem(Material.PAPER, "§eNastępna strona (" + (page + 2) + ") →", List.of()));
            holder.setSlotData(51, "NEXT_PAGE");
        }

        inv.setItem(53, PlotGuiHolder.createItem(Material.BARRIER, "§c§lZamknij", List.of()));
        holder.setSlotData(53, "CLOSE");

        player.openInventory(inv);
    }

    /**
     * Opens the list of plots owned by the player.
     */
    public void openPlotListGui(Player player, int page) {
        if (player == null) return;

        PlotGuiHolder holder = new PlotGuiHolder(PlotGuiType.PLOT_LIST, player, null, page, null);
        int ownedCount = limitManager != null ? limitManager.getPlotCount(player) : 0;
        int plotLimit = limitManager != null ? limitManager.getPlotLimit(player) : 1;
        String limitStr = plotLimit == Integer.MAX_VALUE ? "∞" : String.valueOf(plotLimit);

        Inventory inv = Bukkit.createInventory(holder, 54, TextUtil.toComponent("§8Twoje działki: §6" + ownedCount + "/" + limitStr));
        holder.setInventory(inv);

        List<Plot> plots = plotManager != null ? plotManager.getPlotsByOwner(player.getUniqueId()) : List.of();
        int pageSize = 36;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, plots.size());

        for (int i = startIndex; i < endIndex; i++) {
            Plot p = plots.get(i);
            int slot = i - startIndex;

            int minX = p.getChunkX() << 4;
            int maxX = minX + 15;
            int minZ = p.getChunkZ() << 4;
            int maxZ = minZ + 15;
            int wl = p.getWhitelist() != null ? p.getWhitelist().size() : 0;
            int bl = p.getBlacklist() != null ? p.getBlacklist().size() : 0;

            inv.setItem(slot, PlotGuiHolder.createItem(
                    Material.GRASS_BLOCK,
                    "§a§lDziałka: §f" + p.getID(),
                    List.of(
                            "§7Świat: §f" + p.getWorldName(),
                            "§7Chunk: §e[" + p.getChunkX() + ", " + p.getChunkZ() + "]",
                            "§7Bloki: §8X: " + minX + ".." + maxX + ", Z: " + minZ + ".." + maxZ,
                            "§7Członkowie: §b" + wl + " graczy",
                            "§7Zablokowani: §c" + bl + " graczy",
                            "",
                            "§eLPM: §fZarządzaj tą działką w panelu",
                            "§6PPM: §fOtwórz mapę chunków na tej działce"
                    )
            ));
            holder.setSlotData(slot, p);
        }

        // Bottom control row
        ItemStack frame = PlotGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, frame);
        }

        inv.setItem(45, PlotGuiHolder.createItem(Material.ARROW, "§e§l← Powrót do panelu", List.of()));
        holder.setSlotData(45, "BACK");

        inv.setItem(49, PlotGuiHolder.createItem(
                Material.BOOK,
                "§6§lPodsumowanie konta",
                List.of(
                        "§7Liczba posiadanych działek: §f" + ownedCount,
                        "§7Maksymalny limit działek: §a" + limitStr,
                        "§7Pozostało wolnych: §e" + (plotLimit == Integer.MAX_VALUE ? "∞" : Math.max(0, plotLimit - ownedCount))
                )
        ));

        if (page > 0) {
            inv.setItem(47, PlotGuiHolder.createItem(Material.PAPER, "§e← Poprzednia strona (" + page + ")", List.of()));
            holder.setSlotData(47, "PREV_PAGE");
        }
        if (endIndex < plots.size()) {
            inv.setItem(51, PlotGuiHolder.createItem(Material.PAPER, "§eNastępna strona (" + (page + 2) + ") →", List.of()));
            holder.setSlotData(51, "NEXT_PAGE");
        }

        inv.setItem(53, PlotGuiHolder.createItem(Material.BARRIER, "§c§lZamknij", List.of()));
        holder.setSlotData(53, "CLOSE");

        player.openInventory(inv);
    }

    /**
     * Opens modal confirmation GUI before disposing a plot.
     */
    public void openConfirmDisposeGui(Player player, Plot plot) {
        if (player == null || plot == null) return;

        PlotGuiHolder holder = new PlotGuiHolder(PlotGuiType.CONFIRM_DISPOSE, player, plot);
        Inventory inv = Bukkit.createInventory(holder, 27, TextUtil.toComponent("§4§lPotwierdzenie usunięcia działki"));
        holder.setInventory(inv);

        ItemStack frame = PlotGuiHolder.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, frame);
        }

        // Warning Icon in Slot 4
        inv.setItem(4, PlotGuiHolder.createItem(
                Material.TNT,
                "§4§lCZY NA PEWNO CHCESZ USUNĄĆ TĘ DZIAŁKĘ?",
                List.of(
                        "§7Działka: §c" + plot.getID() + " §7(Świat: §f" + plot.getWorldName() + "§7)",
                        "§7Koordynaty: §e[" + plot.getChunkX() + ", " + plot.getChunkZ() + "]",
                        "",
                        "§cTa operacja jest trwała i nieodwracalna!",
                        "§7Otrzymasz z powrotem 1 blok działki."
                )
        ));

        // Confirm in Slot 11
        inv.setItem(11, PlotGuiHolder.createItem(
                Material.LIME_CONCRETE,
                "§a§l✔ TAK, USUŃ DZIAŁKĘ",
                List.of(
                        "§7Kliknij, aby trwale usunąć działkę",
                        "§7i odebrać blok działki."
                )
        ));
        holder.setSlotData(11, "CONFIRM");

        // Cancel in Slot 15
        inv.setItem(15, PlotGuiHolder.createItem(
                Material.RED_CONCRETE,
                "§c§l✖ NIE, ANULUJ",
                List.of(
                        "§7Kliknij, aby powrócić do panelu działki",
                        "§7bez usuwania niczego."
                )
        ));
        holder.setSlotData(15, "CANCEL");

        player.openInventory(inv);
    }

    /**
     * Executes disposal of a plot for a player (same business logic as /plot dispose).
     */
    public boolean disposePlot(Player player, Plot plot) {
        if (player == null || plot == null || plotManager == null) return false;

        if (!plot.isPlayerOwner(player)) {
            new MessageBuilder(Message.NOT_OWNER).sendChat(player);
            return false;
        }

        boolean disposed = plotManager.disposePlot(plot, player);
        if (!disposed) {
            new MessageBuilder(Message.NULL_PLOT).sendChat(player);
            return false;
        }

        // Refund plot item
        try {
            player.getInventory().addItem(plotManager.getPlotItem());
        } catch (Throwable ignored) {}

        // Remove from user groups
        if (userManager != null) {
            User owner = userManager.getUser(player);
            if (owner != null) {
                for (Group group : owner.getGroups()) {
                    group.remove(plot);
                    if (!group.isDefault()) {
                        new MessageBuilder(Message.PLOT_REMOVED_FROM_GROUP)
                                .group(group)
                                .sendChat(player);
                    }
                }
            }
        }

        new MessageBuilder(Message.PLOT_DELETED).plot(plot).sendChat(player);
        return true;
    }

    public PlotBorderVisualizer getBorderVisualizer() {
        return borderVisualizer;
    }

    public PlotMapManager getMapManager() {
        return mapManager;
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
}
