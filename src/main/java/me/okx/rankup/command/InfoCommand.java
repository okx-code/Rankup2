package me.okx.rankup.command;

import me.okx.rankup.Rankup;
import me.okx.rankup.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class InfoCommand implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {
    Rankup r = Rankup.getInstance();
    int len = args.length;
    if (len > 0) {
      if (args[0].equalsIgnoreCase("reload") && cs.hasPermission("rankup.reload")) {
        r.saveDefaultConfig();
        r.reloadConfig();
        r.setup();
        r.setupInventory();
        Utils.reload();
        cs.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Rankup " + ChatColor.YELLOW + "Reloaded config!");
      } else {
        cs.sendMessage(ChatColor.RED + "Cannot run command");
      }
    } else {
      PluginDescriptionFile desc = r.getDescription();
      cs.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + desc.getName() + " " + desc.getVersion() +
          ChatColor.YELLOW + " by " + ChatColor.BLUE + ChatColor.BOLD + "Okx");
      if (cs.hasPermission("rankup.reload")) {
        cs.sendMessage(ChatColor.GREEN + "/pru reload " + ChatColor.YELLOW + "Reloads config");
      }
    }
    return true;
  }

}
