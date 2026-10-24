package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.group;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.plot.group.Group;
import pl.kacpermajkowski.ChunkyPlots.plot.group.gui.GroupGuiManager;
import pl.kacpermajkowski.ChunkyPlots.user.User;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.*;

public class PlotGroupCommand implements PlotSubcommand {
    private final PlotManager plotManager;
    private final UserManager userManager;
    private final GroupGuiManager groupGuiManager;

    private static final List<String> BASE_ACTIONS = List.of(
            "gui", "list", "create", "delete", "remove", "info", "addplot", "removeplot",
            "add", "trust", "whitelist", "untrust", "unwhitelist", "deny", "blacklist",
            "undeny", "unblacklist", "flag"
    );

    private static final List<String> GROUP_ACTIONS = List.of(
            "info", "gui", "addplot", "removeplot", "add", "remove", "deny", "undeny", "flag"
    );

    private static final List<String> BOOLEAN_VALUES = List.of("true", "false", "toggle");

    public PlotGroupCommand(PlotManager plotManager, UserManager userManager, GroupGuiManager groupGuiManager) {
        this.plotManager = plotManager;
        this.userManager = userManager;
        this.groupGuiManager = groupGuiManager;
    }

    @Override
    public String getName() {
        return "group";
    }

    @Override
    public List<String> getAliases() {
        return List.of("groups", "g", "grupa", "grupy");
    }

    @Override
    public String getDescription() {
        return "pozwala zarządzać grupami działek i zarządzać nimi hurtowo";
    }

    @Override
    public String getSyntax() {
        return "/plot group [gui|list|create|delete|info|addplot|removeplot|<nazwa>] [akcja]";
    }

    @Override
    public String getPermission() {
        return "chunkyplots.player";
    }

    @Override
    public void execute(Player player, String[] args) {
        User user = userManager != null ? userManager.getUser(player) : null;
        if (user == null) return;

        // No arguments: open Group GUI!
        if (args.length == 0) {
            if (groupGuiManager != null) {
                groupGuiManager.openGroupListGui(player);
            }
            return;
        }

        String sub = args[0].toLowerCase();

        // /plot group gui [name]
        if (sub.equals("gui")) {
            if (args.length >= 2) {
                Group group = user.getGroup(args[1]);
                if (group == null) {
                    new MessageBuilder(Message.NULL_GROUP).groupName(args[1]).sendChat(player);
                    return;
                }
                if (groupGuiManager != null) groupGuiManager.openGroupDashboardGui(player, group);
            } else {
                if (groupGuiManager != null) groupGuiManager.openGroupListGui(player);
            }
            return;
        }

        // /plot group list
        if (sub.equals("list")) {
            sendGroupList(player, user);
            return;
        }

        // /plot group create <nazwa>
        if (sub.equals("create")) {
            if (args.length < 2) {
                new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                return;
            }
            String groupName = args[1].trim();
            if (groupName.equalsIgnoreCase("all")) {
                new MessageBuilder(Message.GROUP_ALREADY_EXISTS).groupName("all").sendChat(player);
                return;
            }
            if (!groupName.matches("^[a-zA-Z0-9_-]{1,32}$")) {
                player.sendMessage(TextUtil.toComponent("&cNazwa grupy może zawierać tylko litery, cyfry, myślniki i podkreślenia (1-32 znaków)."));
                return;
            }
            if (user.hasGroup(groupName)) {
                new MessageBuilder(Message.GROUP_ALREADY_EXISTS).groupName(groupName).sendChat(player);
                return;
            }
            user.createGroup(groupName);
            if (userManager != null) userManager.saveUser(user);
            new MessageBuilder(Message.GROUP_CREATE).groupName(groupName).sendChat(player);
            return;
        }

        // /plot group delete <nazwa> / remove <nazwa>
        if (sub.equals("delete") || sub.equals("remove")) {
            if (args.length < 2) {
                new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                return;
            }
            String groupName = args[1].trim();
            Group group = user.getGroup(groupName);
            if (group == null) {
                new MessageBuilder(Message.NULL_GROUP).groupName(groupName).sendChat(player);
                return;
            }
            if (group.isDefault()) {
                new MessageBuilder(Message.CANNOT_DELETE_DEFAULT_GROUP).group(group).sendChat(player);
                return;
            }
            user.removeGroup(groupName);
            if (userManager != null) userManager.saveUser(user);
            new MessageBuilder(Message.GROUP_DELETE).groupName(groupName).sendChat(player);
            return;
        }

        // /plot group info <nazwa> / show <nazwa>
        if (sub.equals("info") || sub.equals("show")) {
            if (args.length < 2) {
                new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                return;
            }
            Group group = user.getGroup(args[1]);
            if (group == null) {
                new MessageBuilder(Message.NULL_GROUP).groupName(args[1]).sendChat(player);
                return;
            }
            sendGroupInfo(player, group);
            return;
        }

        // /plot group addplot <nazwa> [plotId]
        if (sub.equals("addplot") || sub.equals("add-plot")) {
            if (args.length < 2) {
                new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                return;
            }
            String groupName = args[1].trim();
            Group group = user.getGroup(groupName);
            if (group == null) {
                new MessageBuilder(Message.NULL_GROUP).groupName(groupName).sendChat(player);
                return;
            }
            Plot targetPlot = resolveTargetPlot(player, args, 2);
            handleAddPlot(player, user, group, targetPlot);
            return;
        }

        // /plot group removeplot <nazwa> [plotId]
        if (sub.equals("removeplot") || sub.equals("remove-plot")) {
            if (args.length < 2) {
                new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                return;
            }
            String groupName = args[1].trim();
            Group group = user.getGroup(groupName);
            if (group == null) {
                new MessageBuilder(Message.NULL_GROUP).groupName(groupName).sendChat(player);
                return;
            }
            Plot targetPlot = resolveTargetPlot(player, args, 2);
            handleRemovePlot(player, user, group, targetPlot);
            return;
        }

        // Syntax: /plot group <groupName> <action> [args...]
        Group group = user.getGroup(args[0]);
        if (group != null) {
            handleGroupDirectAction(player, user, group, args, 1);
            return;
        }

        // Syntax: /plot group <action> <groupName> [args...]
        if (args.length >= 2) {
            Group secondaryGroup = user.getGroup(args[1]);
            if (secondaryGroup != null) {
                handleGroupActionKeyword(player, user, secondaryGroup, sub, args, 2);
                return;
            }
        }

        new MessageBuilder(Message.NULL_GROUP).groupName(args[0]).sendChat(player);
    }

    private void handleGroupDirectAction(Player player, User user, Group group, String[] args, int offset) {
        if (args.length <= offset) {
            sendGroupInfo(player, group);
            return;
        }

        String action = args[offset].toLowerCase();

        switch (action) {
            case "gui" -> {
                if (groupGuiManager != null) groupGuiManager.openGroupDashboardGui(player, group);
            }
            case "info", "show" -> sendGroupInfo(player, group);
            case "addplot", "add-plot" -> {
                Plot targetPlot = resolveTargetPlot(player, args, offset + 1);
                handleAddPlot(player, user, group, targetPlot);
            }
            case "removeplot", "remove-plot" -> {
                Plot targetPlot = resolveTargetPlot(player, args, offset + 1);
                handleRemovePlot(player, user, group, targetPlot);
            }
            case "add", "trust", "whitelist" -> {
                if (args.length <= offset + 1) {
                    new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                    return;
                }
                handleGroupWhitelist(player, group, args[offset + 1]);
            }
            case "remove", "untrust", "unwhitelist" -> {
                if (args.length <= offset + 1) {
                    new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                    return;
                }
                handleGroupUnwhitelist(player, group, args[offset + 1]);
            }
            case "deny", "blacklist" -> {
                if (args.length <= offset + 1) {
                    new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                    return;
                }
                handleGroupBlacklist(player, group, args[offset + 1]);
            }
            case "undeny", "unblacklist" -> {
                if (args.length <= offset + 1) {
                    new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                    return;
                }
                handleGroupUnblacklist(player, group, args[offset + 1]);
            }
            case "flag", "flags" -> handleGroupFlag(player, group, args, offset + 1);
            default -> {
                PlotFlag flag = PlotFlag.getByName(action);
                if (flag != null) {
                    handleGroupFlag(player, group, args, offset);
                } else {
                    new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                }
            }
        }
    }

    private void handleGroupActionKeyword(Player player, User user, Group group, String action, String[] args, int targetOffset) {
        switch (action) {
            case "add", "trust", "whitelist" -> {
                if (args.length <= targetOffset) {
                    new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                    return;
                }
                handleGroupWhitelist(player, group, args[targetOffset]);
            }
            case "remove", "untrust", "unwhitelist" -> {
                if (args.length <= targetOffset) {
                    new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                    return;
                }
                handleGroupUnwhitelist(player, group, args[targetOffset]);
            }
            case "deny", "blacklist" -> {
                if (args.length <= targetOffset) {
                    new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                    return;
                }
                handleGroupBlacklist(player, group, args[targetOffset]);
            }
            case "undeny", "unblacklist" -> {
                if (args.length <= targetOffset) {
                    new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                    return;
                }
                handleGroupUnblacklist(player, group, args[targetOffset]);
            }
            case "flag", "flags" -> handleGroupFlag(player, group, args, targetOffset);
            default -> {
                PlotFlag flag = PlotFlag.getByName(action);
                if (flag != null) {
                    handleGroupFlag(player, group, args, targetOffset - 1);
                } else {
                    new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                }
            }
        }
    }

    private void handleAddPlot(Player player, User user, Group group, Plot targetPlot) {
        if (group.isDefault()) {
            new MessageBuilder(Message.CANNOT_ADD_PLOT_TO_DEFAULT_GROUP).group(group).sendChat(player);
            return;
        }
        if (targetPlot == null) {
            new MessageBuilder(Message.NULL_PLOT).sendChat(player);
            return;
        }
        if (!targetPlot.isPlayerOwner(player)) {
            new MessageBuilder(Message.NOT_OWNER).sendChat(player);
            return;
        }
        if (group.contains(targetPlot)) {
            new MessageBuilder(Message.PLOT_ALREADY_IN_GROUP).group(group).plot(targetPlot).sendChat(player);
            return;
        }
        group.add(targetPlot);
        if (userManager != null) userManager.saveUser(user);
        new MessageBuilder(Message.PLOT_ADDED_TO_GROUP).group(group).plot(targetPlot).sendChat(player);
    }

    private void handleRemovePlot(Player player, User user, Group group, Plot targetPlot) {
        if (group.isDefault()) {
            new MessageBuilder(Message.CANNOT_REMOVE_PLOT_FROM_DEFAULT_GROUP).group(group).sendChat(player);
            return;
        }
        if (targetPlot == null) {
            new MessageBuilder(Message.NULL_PLOT).sendChat(player);
            return;
        }
        if (!targetPlot.isPlayerOwner(player)) {
            new MessageBuilder(Message.NOT_OWNER).sendChat(player);
            return;
        }
        if (!group.contains(targetPlot)) {
            new MessageBuilder(Message.PLOT_NOT_IN_GROUP).group(group).plot(targetPlot).sendChat(player);
            return;
        }
        group.remove(targetPlot);
        if (userManager != null) userManager.saveUser(user);
        new MessageBuilder(Message.PLOT_REMOVED_FROM_GROUP).group(group).plot(targetPlot).sendChat(player);
    }

    private void handleGroupWhitelist(Player player, Group group, String targetName) {
        if (group.getPlots().isEmpty()) {
            new MessageBuilder(Message.EMPTY_GROUP).group(group).sendChat(player);
            return;
        }
        OfflinePlayer target = resolveOfflinePlayer(targetName);
        if (target == null) return;

        if (target.getUniqueId().equals(player.getUniqueId())) {
            new MessageBuilder(Message.CANNOT_ADD_OWNER_AS_MEMBER).sendChat(player);
            return;
        }

        group.whitelistPlayer(target.getUniqueId());
        saveGroupPlots(group);
        new MessageBuilder(Message.ADDED_MEMBER_TO_GROUP)
                .username(target.getName() != null ? target.getName() : targetName)
                .group(group)
                .sendChat(player);
    }

    private void handleGroupUnwhitelist(Player player, Group group, String targetName) {
        if (group.getPlots().isEmpty()) {
            new MessageBuilder(Message.EMPTY_GROUP).group(group).sendChat(player);
            return;
        }
        OfflinePlayer target = resolveOfflinePlayer(targetName);
        if (target == null) return;

        group.unwhitelistPlayer(target.getUniqueId());
        saveGroupPlots(group);
        new MessageBuilder(Message.REMOVED_MEMBER_FROM_GROUP)
                .username(target.getName() != null ? target.getName() : targetName)
                .group(group)
                .sendChat(player);
    }

    private void handleGroupBlacklist(Player player, Group group, String targetName) {
        if (group.getPlots().isEmpty()) {
            new MessageBuilder(Message.EMPTY_GROUP).group(group).sendChat(player);
            return;
        }
        OfflinePlayer target = resolveOfflinePlayer(targetName);
        if (target == null) return;

        if (target.getUniqueId().equals(player.getUniqueId())) {
            new MessageBuilder(Message.CANNOT_ADD_OWNER_TO_BLACKLIST).sendChat(player);
            return;
        }

        group.blacklistPlayer(target.getUniqueId());
        saveGroupPlots(group);
        new MessageBuilder(Message.BLACKLIST_ADDED_TO_GROUP)
                .username(target.getName() != null ? target.getName() : targetName)
                .group(group)
                .sendChat(player);
    }

    private void handleGroupUnblacklist(Player player, Group group, String targetName) {
        if (group.getPlots().isEmpty()) {
            new MessageBuilder(Message.EMPTY_GROUP).group(group).sendChat(player);
            return;
        }
        OfflinePlayer target = resolveOfflinePlayer(targetName);
        if (target == null) return;

        group.unblacklistPlayer(target.getUniqueId());
        saveGroupPlots(group);
        new MessageBuilder(Message.BLACKLIST_REMOVED_FROM_GROUP)
                .username(target.getName() != null ? target.getName() : targetName)
                .group(group)
                .sendChat(player);
    }

    private void handleGroupFlag(Player player, Group group, String[] args, int offset) {
        if (group.getPlots().isEmpty()) {
            new MessageBuilder(Message.EMPTY_GROUP).group(group).sendChat(player);
            return;
        }

        if (args.length <= offset || args[offset].equalsIgnoreCase("list")) {
            sendGroupFlagsList(player, group);
            return;
        }

        if (args[offset].equalsIgnoreCase("reset")) {
            if (args.length <= offset + 1) {
                new MessageBuilder(Message.COMMAND_USAGE).subcommand(this).sendChat(player);
                return;
            }
            if (args[offset + 1].equalsIgnoreCase("all")) {
                group.resetAllFlags();
                saveGroupPlots(group);
                player.sendMessage(TextUtil.toComponent("&aZresetowano wszystkie flagi dla grupy &f" + group.getName()));
                return;
            }
            PlotFlag flag = PlotFlag.getByName(args[offset + 1]);
            if (flag == null) {
                new MessageBuilder(Message.NULL_FLAG).flagName(args[offset + 1]).sendChat(player);
                return;
            }
            group.resetFlag(flag);
            saveGroupPlots(group);
            player.sendMessage(TextUtil.toComponent("&aZresetowano flagę &f" + flag.getName() + " &adla grupy &f" + group.getName()));
            return;
        }

        PlotFlag flag = PlotFlag.getByName(args[offset]);
        if (flag == null) {
            new MessageBuilder(Message.NULL_FLAG).flagName(args[offset]).sendChat(player);
            return;
        }

        // Query status: /p group <name> flag <flag>
        if (args.length <= offset + 1) {
            Group.GroupFlagStatus status = group.getFlagStatus(flag);
            if (status == Group.GroupFlagStatus.ALL_TRUE) {
                new MessageBuilder(Message.FLAG_VALUE_ON_GROUP)
                        .group(group)
                        .flagName(flag.getName())
                        .flagValue("true")
                        .sendChat(player);
            } else if (status == Group.GroupFlagStatus.ALL_FALSE) {
                new MessageBuilder(Message.FLAG_VALUE_ON_GROUP)
                        .group(group)
                        .flagName(flag.getName())
                        .flagValue("false")
                        .sendChat(player);
            } else if (status == Group.GroupFlagStatus.DIFFERENT) {
                new MessageBuilder(Message.FLAG_VALUES_IN_GROUP_ARE_DIFFERENT)
                        .group(group)
                        .flagName(flag.getName())
                        .sendChat(player);
            }
            player.sendMessage(TextUtil.toComponent("&7Opis: &f" + flag.getDescription()));
            return;
        }

        // Set value: /p group <name> flag <flag> <value>
        Group.GroupFlagStatus status = group.getFlagStatus(flag);
        boolean currentVal = (status == Group.GroupFlagStatus.ALL_TRUE);
        Boolean newVal = parseBooleanOrToggle(args[offset + 1], currentVal);
        if (newVal == null) {
            new MessageBuilder(Message.WRONG_FLAG_VALUE).sendChat(player);
            return;
        }

        group.setFlag(flag, newVal);
        saveGroupPlots(group);
        new MessageBuilder(Message.FLAG_SET_ON_GROUP)
                .group(group)
                .flagName(flag.getName())
                .flagValue(newVal ? "true" : "false")
                .sendChat(player);
    }

    private void sendGroupFlagsList(Player player, Group group) {
        new MessageBuilder(Message.WIDE_HEADER).sendChat(player);
        player.sendMessage(TextUtil.toComponent("&6Flagi dla grupy &e" + group.getName() + " &8(" + group.getPlots().size() + " działek):"));
        for (PlotFlag flag : PlotFlag.values()) {
            Group.GroupFlagStatus status = group.getFlagStatus(flag);
            String label = switch (status) {
                case ALL_TRUE -> "&a[WŁĄCZONA / true]";
                case ALL_FALSE -> "&c[WYŁĄCZONA / false]";
                case DIFFERENT -> "&e[RÓŻNE WARTOŚCI]";
                case EMPTY -> "&7[BRAK DZIAŁEK]";
            };
            player.sendMessage(TextUtil.toComponent("&7• &f" + flag.getName() + " " + label + " &8- &7" + flag.getDescription()));
        }
        player.sendMessage(TextUtil.toComponent("&7Użyj &f/plot group " + group.getName() + " flag <flaga> <true|false|toggle> &7aby zmienić."));
    }

    private void sendGroupList(Player player, User user) {
        new MessageBuilder(Message.WIDE_HEADER).sendChat(player);
        player.sendMessage(TextUtil.toComponent("&6Twoje grupy działek:"));
        for (Group group : user.getGroups()) {
            int count = group.getPlots().size();
            String typeLabel = group.isDefault() ? "&7(Domyślna)" : "&b(Własna)";
            player.sendMessage(TextUtil.toComponent("&7• &e" + group.getName() + " &8(&f" + count + " działek&8) " + typeLabel));
        }
        player.sendMessage(TextUtil.toComponent("&7Zarządzaj w panelu: &f/plot group gui"));
    }

    private void sendGroupInfo(Player player, Group group) {
        new MessageBuilder(Message.WIDE_HEADER).sendChat(player);
        player.sendMessage(TextUtil.toComponent("&6Informacje o grupie: &e" + group.getName()));
        player.sendMessage(TextUtil.toComponent("&7Typ: &f" + (group.isDefault() ? "Domyślna (all)" : "Własna")));
        player.sendMessage(TextUtil.toComponent("&7Liczba działek: &a" + group.getPlots().size()));
        if (!group.getPlots().isEmpty()) {
            List<String> plotLabels = group.getPlots().stream()
                    .map(p -> p.getMnemonicId() + " (" + p.getID() + ")")
                    .toList();
            player.sendMessage(TextUtil.toComponent("&7Działki: &f" + String.join(", ", plotLabels)));
        } else {
            player.sendMessage(TextUtil.toComponent("&7Działki: &8brak przypisanych"));
        }
        new MessageBuilder(Message.WIDE_HEADER).sendChat(player);
    }

    private Plot resolveTargetPlot(Player player, String[] args, int index) {
        if (args.length > index) {
            return plotManager.getPlot(args[index]);
        }
        return plotManager.getPlot(player.getLocation());
    }

    private void saveGroupPlots(Group group) {
        if (plotManager != null) {
            for (Plot p : group.getPlots()) {
                if (p != null) plotManager.savePlot(p);
            }
        }
    }

    private OfflinePlayer resolveOfflinePlayer(String name) {
        if (name == null || name.isEmpty()) return null;
        if (Bukkit.getServer() != null) {
            return Bukkit.getOfflinePlayer(name);
        }
        return null;
    }

    private Boolean parseBooleanOrToggle(String input, boolean currentValue) {
        if (input == null) return null;
        String lower = input.trim().toLowerCase();
        if (lower.equals("true") || lower.equals("tak") || lower.equals("yes") || lower.equals("on") || lower.equals("1") || lower.equals("włącz") || lower.equals("wlacz") || lower.equals("enable")) {
            return true;
        }
        if (lower.equals("false") || lower.equals("nie") || lower.equals("no") || lower.equals("off") || lower.equals("0") || lower.equals("wyłącz") || lower.equals("wylacz") || lower.equals("disable")) {
            return false;
        }
        if (lower.equals("toggle") || lower.equals("przełącz") || lower.equals("przelacz")) {
            return !currentValue;
        }
        return null;
    }

    @Override
    public List<String> getTabCompletion(Player player, String[] args) {
        User user = userManager != null ? userManager.getUser(player) : null;
        List<String> completions = new ArrayList<>();
        if (user == null) return completions;

        List<String> userGroupNames = user.getGroups().stream().map(Group::getName).toList();

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (String action : BASE_ACTIONS) {
                if (action.startsWith(prefix)) completions.add(action);
            }
            for (String groupName : userGroupNames) {
                if (groupName.toLowerCase().startsWith(prefix)) completions.add(groupName.toLowerCase());
            }
            return completions;
        }

        if (args.length == 2) {
            String first = args[0].toLowerCase();
            String prefix = args[1].toLowerCase();

            if (first.equals("addplot") || first.equals("removeplot")) {
                for (String g : userGroupNames) {
                    if (!g.equalsIgnoreCase("all") && g.toLowerCase().startsWith(prefix)) completions.add(g.toLowerCase());
                }
                return completions;
            }
            if (first.equals("gui") || first.equals("info") || first.equals("show")) {
                for (String g : userGroupNames) {
                    if (g.toLowerCase().startsWith(prefix)) completions.add(g.toLowerCase());
                }
                return completions;
            }
            if (first.equals("delete") || first.equals("remove")) {
                for (String g : userGroupNames) {
                    if (!g.equalsIgnoreCase("all") && g.toLowerCase().startsWith(prefix)) completions.add(g.toLowerCase());
                }
                return completions;
            }

            // If first arg is a group name
            Group g = user.getGroup(first);
            if (g != null) {
                for (String action : GROUP_ACTIONS) {
                    if (action.startsWith(prefix)) completions.add(action);
                }
                for (PlotFlag flag : PlotFlag.values()) {
                    if (flag.getName().startsWith(prefix)) completions.add(flag.getName());
                }
                return completions;
            }
        }

        if (args.length == 3) {
            String first = args[0].toLowerCase();
            String second = args[1].toLowerCase();
            String prefix = args[2].toLowerCase();

            Group g = user.getGroup(first);
            if (g != null) {
                if (second.equals("flag") || second.equals("flags")) {
                    if ("list".startsWith(prefix)) completions.add("list");
                    if ("reset".startsWith(prefix)) completions.add("reset");
                    for (PlotFlag flag : PlotFlag.values()) {
                        if (flag.getName().startsWith(prefix)) completions.add(flag.getName());
                    }
                    return completions;
                }
                PlotFlag directFlag = PlotFlag.getByName(second);
                if (directFlag != null) {
                    for (String val : BOOLEAN_VALUES) {
                        if (val.startsWith(prefix)) completions.add(val);
                    }
                    return completions;
                }
                if (second.equals("addplot") || second.equals("removeplot")) {
                    List<Plot> candidatePlots = second.equals("addplot")
                            ? plotManager.getPlotsByOwner(player.getUniqueId())
                            : g.getPlots();
                    for (Plot p : candidatePlots) {
                        if (p.getMnemonicId().startsWith(prefix)) completions.add(p.getMnemonicId());
                        if (p.getID().startsWith(prefix)) completions.add(p.getID());
                    }
                    return completions;
                }
                if (second.equals("add") || second.equals("trust") || second.equals("deny")) {
                    if (Bukkit.getServer() != null) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getName().toLowerCase().startsWith(prefix)) completions.add(p.getName());
                        }
                    }
                    return completions;
                }
            }

            if (first.equals("addplot") || first.equals("removeplot")) {
                Group targetGroup = user.getGroup(second);
                if (targetGroup != null) {
                    List<Plot> candidatePlots = first.equals("addplot")
                            ? plotManager.getPlotsByOwner(player.getUniqueId())
                            : targetGroup.getPlots();
                    for (Plot p : candidatePlots) {
                        if (p.getMnemonicId().startsWith(prefix)) completions.add(p.getMnemonicId());
                        if (p.getID().startsWith(prefix)) completions.add(p.getID());
                    }
                    return completions;
                }
            }
        }

        if (args.length == 4) {
            String first = args[0].toLowerCase();
            String second = args[1].toLowerCase();
            String third = args[2].toLowerCase();
            String prefix = args[3].toLowerCase();

            Group g = user.getGroup(first);
            if (g != null && (second.equals("flag") || second.equals("flags"))) {
                if (third.equals("reset")) {
                    if ("all".startsWith(prefix)) completions.add("all");
                    for (PlotFlag flag : PlotFlag.values()) {
                        if (flag.getName().startsWith(prefix)) completions.add(flag.getName());
                    }
                    return completions;
                }
                PlotFlag flag = PlotFlag.getByName(third);
                if (flag != null) {
                    for (String val : BOOLEAN_VALUES) {
                        if (val.startsWith(prefix)) completions.add(val);
                    }
                    return completions;
                }
            }
        }

        return completions;
    }
}
