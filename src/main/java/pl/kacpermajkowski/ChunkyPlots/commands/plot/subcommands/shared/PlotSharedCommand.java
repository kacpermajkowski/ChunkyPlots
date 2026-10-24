package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.shared;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

import java.util.List;

public class PlotSharedCommand implements PlotSubcommand {
    private final PlotManager plotManager;

    public PlotSharedCommand(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @Override
    public String getName() {
        return "shared";
    }

    @Override
    public List<String> getAliases() {
        return List.of("member", "memberplots", "sharedplots");
    }

    @Override
    public String getDescription() {
        return "lista działek, których jesteś członkiem";
    }

    @Override
    public String getSyntax() {
        return "/plot shared";
    }

    @Override
    public String getPermission() {
        return "chunkyplots.player";
    }

    @Override
    public void execute(Player sender, String[] args) {
        if (sender instanceof Player player) {
            new MessageBuilder(Message.WIDE_HEADER).sendChat(player);
            player.sendMessage(TextUtil.toComponent("&eID Działki &8(&eKod&8) &9» &eWłaściciel &9» &eKoordynaty &8(kliknij, aby zarządzać)"));
            List<Plot> memberPlots = plotManager != null ? plotManager.getPlotsByMember(player.getUniqueId()) : List.of();
            boolean hasPlots = !memberPlots.isEmpty();
            for (Plot plot : memberPlots) {
                int blockX = (plot.getChunkX() << 4) + 8;
                int blockZ = (plot.getChunkZ() << 4) + 8;

                Component line = Component.text()
                        .append(Component.text("(", NamedTextColor.DARK_GRAY))
                        .append(Component.text(plot.getID(), NamedTextColor.AQUA))
                        .append(Component.text(") [", NamedTextColor.DARK_GRAY))
                        .append(Component.text(plot.getMnemonicId(), NamedTextColor.YELLOW))
                        .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("» ", NamedTextColor.GREEN))
                        .append(Component.text(plot.getOwnerName() + " ", NamedTextColor.WHITE))
                        .append(Component.text("» ", NamedTextColor.DARK_AQUA))
                        .append(Component.text("X:", NamedTextColor.GRAY))
                        .append(Component.text(blockX + " ", NamedTextColor.WHITE))
                        .append(Component.text("Z:", NamedTextColor.GRAY))
                        .append(Component.text(blockZ + " ", NamedTextColor.WHITE))
                        .append(Component.text("[" + plot.getWorldName() + "]", NamedTextColor.DARK_AQUA))
                        .hoverEvent(HoverEvent.showText(Component.text("Kliknij, aby zarządzać działką /plot info " + plot.getMnemonicId(), NamedTextColor.YELLOW)))
                        .clickEvent(ClickEvent.runCommand("/plot info " + plot.getMnemonicId()))
                        .build();

                player.sendMessage(line);
            }
            if (!hasPlots) {
                new MessageBuilder(Message.NO_MEMBER_PLOTS).sendChat(player);
            }
            new MessageBuilder(Message.WIDE_HEADER).sendChat(player);
        }
    }

    @Override
    public List<String> getTabCompletion(Player player, String[] args) {
        return List.of();
    }
}
