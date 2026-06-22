package pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.subcommands.bypass;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.user.User;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.List;

public class PlotAdminBypassCommand implements Subcommand {
    @Override
    public String getName() {
        return "bypass";
    }

    @Override
    public String getDescription() {
        return "bypass all plot protections";
    }

    @Override
    public String getSyntax() {
        return "/plotadmin bypass";
    }

    @Override
    public String getPermission() {
        return "chunkyplots.plotadmin";
    }


    private static BossBar bypassBossBar;

    public PlotAdminBypassCommand() {
        setupBypassBossBar();
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            return;
        }
        User adminUser = UserManager.getInstance().getUser(player);
        boolean currentBypasssStatus = adminUser.isBypassingRestrictions();

        UserManager.getInstance().getUser(player).setBypassingRestrictions(!currentBypasssStatus);

        MessageBuilder mb = new MessageBuilder().user(player);
        if(currentBypasssStatus){
            mb.message(Message.PADMIN_BYPASS_DISABLED);
            bypassBossBar.removePlayer(player);
        } else {
            mb.message(Message.PADMIN_BYPASS_ENABLED);
            bypassBossBar.addPlayer(player);
        }
        mb.sendChat(player);


    }

    private void setupBypassBossBar() {
        bypassBossBar = Bukkit.createBossBar(
                new MessageBuilder(Message.PADMIN_BYPASS_BOSSBAR).build(),
                BarColor.RED,
                BarStyle.SEGMENTED_10
        );
        bypassBossBar.setVisible(true);
        Bukkit.getScheduler().runTaskTimer(ChunkyPlots.instance(), () -> {
            cycleBossBar(bypassBossBar);
        }, 0, 2);
    }

    private void cycleBossBar(BossBar bypassBossBar){
        double progress = bypassBossBar.getProgress();
        progress += 0.1;
        if(progress >= 1) progress = 0;
        bypassBossBar.setProgress(progress);
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, String[] args) {
        return List.of();
    }
}
