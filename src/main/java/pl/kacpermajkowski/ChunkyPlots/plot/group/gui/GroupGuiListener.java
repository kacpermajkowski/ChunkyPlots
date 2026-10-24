package pl.kacpermajkowski.ChunkyPlots.plot.group.gui;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.plot.group.Group;
import pl.kacpermajkowski.ChunkyPlots.plot.gui.PlayerSelectAction;
import pl.kacpermajkowski.ChunkyPlots.user.User;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.UUID;

public class GroupGuiListener implements Listener {
    private final GroupGuiManager groupGuiManager;

    public GroupGuiListener(GroupGuiManager groupGuiManager) {
        this.groupGuiManager = groupGuiManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GroupGuiHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GroupGuiHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) {
            return;
        }

        GroupGuiType type = holder.getType();
        Group group = holder.getGroup();
        int page = holder.getPage();
        Object data = holder.getSlotData(slot);

        UserManager userManager = groupGuiManager.getUserManager();
        User user = userManager != null ? userManager.getUser(player) : null;

        switch (type) {
            case GROUP_LIST -> handleGroupListClick(player, slot, data);
            case GROUP_DASHBOARD -> handleGroupDashboardClick(player, group, slot, data, user, userManager);
            case GROUP_PLOTS -> handleGroupPlotsClick(player, group, slot, data, page, user, userManager, event.isRightClick());
            case GROUP_ADD_PLOT -> handleGroupAddPlotClick(player, group, slot, data, page, user, userManager);
            case GROUP_FLAGS -> handleGroupFlagsClick(player, group, slot, data, page, event.isRightClick(), event.isShiftClick());
            case GROUP_MEMBERS -> handleGroupMembersClick(player, group, slot, data, page);
            case GROUP_BLACKLIST -> handleGroupBlacklistClick(player, group, slot, data, page);
            case GROUP_SELECT_PLAYER -> handleGroupSelectPlayerClick(player, group, slot, data, page, holder.getSelectAction());
        }
    }

    private void handleGroupListClick(Player player, int slot, Object data) {
        if ("BACK".equals(data) || "CLOSE".equals(data)) {
            player.closeInventory();
            return;
        }
        if ("CREATE".equals(data)) {
            player.closeInventory();
            player.sendMessage(TextUtil.toComponent("&aAby utworzyć nową grupę, wpisz na czacie:"));
            player.sendMessage(TextUtil.toComponent("&e/plot group create <nazwa>"));
            return;
        }
        if (data instanceof Group group) {
            groupGuiManager.openGroupDashboardGui(player, group);
        }
    }

    private void handleGroupDashboardClick(Player player, Group group, int slot, Object data, User user, UserManager userManager) {
        if ("BACK".equals(data)) {
            groupGuiManager.openGroupListGui(player);
            return;
        }
        if ("CLOSE".equals(data)) {
            player.closeInventory();
            return;
        }
        if ("PLOTS".equals(data)) {
            groupGuiManager.openGroupPlotsGui(player, group, 0);
            return;
        }
        if ("FLAGS".equals(data)) {
            groupGuiManager.openGroupFlagsGui(player, group, 0);
            return;
        }
        if ("WHITELIST".equals(data)) {
            groupGuiManager.openGroupMembersGui(player, group, 0);
            return;
        }
        if ("BLACKLIST".equals(data)) {
            groupGuiManager.openGroupBlacklistGui(player, group, 0);
            return;
        }
        if ("MAP".equals(data)) {
            groupGuiManager.showGroupOnMap(player, group);
            return;
        }
        if ("DELETE".equals(data) && !group.isDefault()) {
            if (user != null) {
                user.removeGroup(group.getName());
                if (userManager != null) userManager.saveUser(user);
                new MessageBuilder(Message.GROUP_DELETE).group(group).sendChat(player);
            }
            groupGuiManager.openGroupListGui(player);
        }
    }

    private void handleGroupPlotsClick(Player player, Group group, int slot, Object data, int page, User user, UserManager userManager, boolean isRightClick) {
        if ("BACK".equals(data)) {
            groupGuiManager.openGroupDashboardGui(player, group);
            return;
        }
        if ("CLOSE".equals(data)) {
            player.closeInventory();
            return;
        }
        if ("ADD_PLOT".equals(data)) {
            groupGuiManager.openGroupAddPlotGui(player, group, 0);
            return;
        }
        if ("PREV_PAGE".equals(data)) {
            groupGuiManager.openGroupPlotsGui(player, group, Math.max(0, page - 1));
            return;
        }
        if ("NEXT_PAGE".equals(data)) {
            groupGuiManager.openGroupPlotsGui(player, group, page + 1);
            return;
        }
        if (data instanceof Plot plot) {
            if (isRightClick && !group.isDefault()) {
                group.remove(plot);
                if (user != null && userManager != null) {
                    userManager.saveUser(user);
                }
                new MessageBuilder(Message.PLOT_REMOVED_FROM_GROUP).group(group).plot(plot).sendChat(player);
                groupGuiManager.openGroupPlotsGui(player, group, page);
            } else {
                // Show on map
                groupGuiManager.showGroupOnMap(player, group);
            }
        }
    }

    private void handleGroupAddPlotClick(Player player, Group group, int slot, Object data, int page, User user, UserManager userManager) {
        if ("BACK".equals(data)) {
            groupGuiManager.openGroupPlotsGui(player, group, 0);
            return;
        }
        if ("CLOSE".equals(data)) {
            player.closeInventory();
            return;
        }
        if ("PREV_PAGE".equals(data)) {
            groupGuiManager.openGroupAddPlotGui(player, group, Math.max(0, page - 1));
            return;
        }
        if ("NEXT_PAGE".equals(data)) {
            groupGuiManager.openGroupAddPlotGui(player, group, page + 1);
            return;
        }
        if (data instanceof Plot plot) {
            group.add(plot);
            if (user != null && userManager != null) {
                userManager.saveUser(user);
            }
            new MessageBuilder(Message.PLOT_ADDED_TO_GROUP).group(group).plot(plot).sendChat(player);
            groupGuiManager.openGroupPlotsGui(player, group, 0);
        }
    }

    private void handleGroupFlagsClick(Player player, Group group, int slot, Object data, int page, boolean isRightClick, boolean isShiftClick) {
        if ("BACK".equals(data)) {
            groupGuiManager.openGroupDashboardGui(player, group);
            return;
        }
        if ("CLOSE".equals(data)) {
            player.closeInventory();
            return;
        }
        if ("PREV_PAGE".equals(data)) {
            groupGuiManager.openGroupFlagsGui(player, group, Math.max(0, page - 1));
            return;
        }
        if ("NEXT_PAGE".equals(data)) {
            groupGuiManager.openGroupFlagsGui(player, group, page + 1);
            return;
        }
        if ("RESET_ALL".equals(data)) {
            group.resetAllFlags();
            if (groupGuiManager.getPlotManager() != null) {
                for (Plot p : group.getPlots()) {
                    groupGuiManager.getPlotManager().savePlot(p);
                }
            }
            player.sendMessage(TextUtil.toComponent("&aZresetowano wszystkie flagi dla grupy &f" + group.getName()));
            groupGuiManager.openGroupFlagsGui(player, group, page);
            return;
        }
        if (data instanceof PlotFlag flag) {
            if (isShiftClick) {
                group.resetFlag(flag);
                player.sendMessage(TextUtil.toComponent("&aZresetowano flagę &f" + flag.getName() + " &adla grupy &f" + group.getName()));
            } else if (isRightClick) {
                group.setFlag(flag, false);
                new MessageBuilder(Message.FLAG_SET_ON_GROUP)
                        .group(group)
                        .flagName(flag.getName())
                        .flagValue(false)
                        .sendChat(player);
            } else {
                group.setFlag(flag, true);
                new MessageBuilder(Message.FLAG_SET_ON_GROUP)
                        .group(group)
                        .flagName(flag.getName())
                        .flagValue(true)
                        .sendChat(player);
            }

            if (groupGuiManager.getPlotManager() != null) {
                for (Plot p : group.getPlots()) {
                    groupGuiManager.getPlotManager().savePlot(p);
                }
            }
            groupGuiManager.openGroupFlagsGui(player, group, page);
        }
    }

    private void handleGroupMembersClick(Player player, Group group, int slot, Object data, int page) {
        if ("BACK".equals(data)) {
            groupGuiManager.openGroupDashboardGui(player, group);
            return;
        }
        if ("CLOSE".equals(data)) {
            player.closeInventory();
            return;
        }
        if ("ADD_MEMBER".equals(data)) {
            groupGuiManager.openGroupSelectPlayerGui(player, group, PlayerSelectAction.ADD_WHITELIST, 0);
            return;
        }
        if ("PREV_PAGE".equals(data)) {
            groupGuiManager.openGroupMembersGui(player, group, Math.max(0, page - 1));
            return;
        }
        if ("NEXT_PAGE".equals(data)) {
            groupGuiManager.openGroupMembersGui(player, group, page + 1);
            return;
        }
        if (data instanceof UUID targetUuid) {
            group.unwhitelistPlayer(targetUuid);
            if (groupGuiManager.getPlotManager() != null) {
                for (Plot p : group.getPlots()) {
                    groupGuiManager.getPlotManager().savePlot(p);
                }
            }
            String targetName = resolvePlayerName(targetUuid);
            new MessageBuilder(Message.REMOVED_MEMBER_FROM_GROUP)
                    .username(targetName)
                    .group(group)
                    .sendChat(player);
            groupGuiManager.openGroupMembersGui(player, group, page);
        }
    }

    private void handleGroupBlacklistClick(Player player, Group group, int slot, Object data, int page) {
        if ("BACK".equals(data)) {
            groupGuiManager.openGroupDashboardGui(player, group);
            return;
        }
        if ("CLOSE".equals(data)) {
            player.closeInventory();
            return;
        }
        if ("ADD_BLACKLIST".equals(data)) {
            groupGuiManager.openGroupSelectPlayerGui(player, group, PlayerSelectAction.ADD_BLACKLIST, 0);
            return;
        }
        if ("PREV_PAGE".equals(data)) {
            groupGuiManager.openGroupBlacklistGui(player, group, Math.max(0, page - 1));
            return;
        }
        if ("NEXT_PAGE".equals(data)) {
            groupGuiManager.openGroupBlacklistGui(player, group, page + 1);
            return;
        }
        if (data instanceof UUID targetUuid) {
            group.unblacklistPlayer(targetUuid);
            if (groupGuiManager.getPlotManager() != null) {
                for (Plot p : group.getPlots()) {
                    groupGuiManager.getPlotManager().savePlot(p);
                }
            }
            String targetName = resolvePlayerName(targetUuid);
            new MessageBuilder(Message.BLACKLIST_REMOVED_FROM_GROUP)
                    .username(targetName)
                    .group(group)
                    .sendChat(player);
            groupGuiManager.openGroupBlacklistGui(player, group, page);
        }
    }

    private void handleGroupSelectPlayerClick(Player player, Group group, int slot, Object data, int page, PlayerSelectAction action) {
        if ("BACK".equals(data)) {
            if (action == PlayerSelectAction.ADD_WHITELIST) {
                groupGuiManager.openGroupMembersGui(player, group, 0);
            } else {
                groupGuiManager.openGroupBlacklistGui(player, group, 0);
            }
            return;
        }
        if ("CLOSE".equals(data)) {
            player.closeInventory();
            return;
        }
        if ("PREV_PAGE".equals(data)) {
            groupGuiManager.openGroupSelectPlayerGui(player, group, action, Math.max(0, page - 1));
            return;
        }
        if ("NEXT_PAGE".equals(data)) {
            groupGuiManager.openGroupSelectPlayerGui(player, group, action, page + 1);
            return;
        }
        if (data instanceof UUID targetUuid) {
            String targetName = resolvePlayerName(targetUuid);
            if (action == PlayerSelectAction.ADD_WHITELIST) {
                group.whitelistPlayer(targetUuid);
                if (groupGuiManager.getPlotManager() != null) {
                    for (Plot p : group.getPlots()) {
                        groupGuiManager.getPlotManager().savePlot(p);
                    }
                }
                new MessageBuilder(Message.ADDED_MEMBER_TO_GROUP)
                        .username(targetName)
                        .group(group)
                        .sendChat(player);
                groupGuiManager.openGroupMembersGui(player, group, 0);
            } else {
                group.blacklistPlayer(targetUuid);
                if (groupGuiManager.getPlotManager() != null) {
                    for (Plot p : group.getPlots()) {
                        groupGuiManager.getPlotManager().savePlot(p);
                    }
                }
                new MessageBuilder(Message.BLACKLIST_ADDED_TO_GROUP)
                        .username(targetName)
                        .group(group)
                        .sendChat(player);
                groupGuiManager.openGroupBlacklistGui(player, group, 0);
            }
        }
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
