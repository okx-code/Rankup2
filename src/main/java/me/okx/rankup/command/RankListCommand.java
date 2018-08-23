package me.okx.rankup.command;

import me.okx.rankup.Rankup;
import me.okx.rankup.Utils;
import me.okx.rankup.exception.NotInLadderException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class RankListCommand implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
    Rankup r = Rankup.getInstance();
    FileConfiguration config = r.getConfig();
    String str = "messages.listRanks.ranks.complete";
    String dStr = str;
    boolean isPlayer = (cs instanceof Player);
    boolean trigger = false;

    String header = getSurround("ranks.header");
    if(header != null) {
      cs.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
    }

    for (int x = 1; x < r.ranks.size(); x++) {
      str = dStr;
      if (trigger || !isPlayer) {
        str = "messages.listRanks.ranks.default";
      } else {
        try {
          if (Utils.getRank((Player) cs, true).getName().equalsIgnoreCase(r.ranks.get(x).getName())) {
            str = "messages.listRanks.ranks.inProgress";
            trigger = true;
          }
        } catch (NotInLadderException | ArrayIndexOutOfBoundsException ignored) {
        }
      }

      String[] sl;
      try {
        sl = config.getString(str).split("\n");
      } catch (NullPointerException e) {
        // custom format doesn't exist.
        sl = config.getString(dStr).split("\n");
      }

      for (String s : sl) {
        double cost;
        if (isPlayer) {
          cost = Utils.getNthRankCost((Player) cs, x);
        } else {
          cost = r.ranks.get(x).getCost();
        }
        s = Utils.replace(cs.getName(), r.ranks.get(x).getName(), r.ranks.get(x - 1).getName() + "", cost, s);
        if (isPlayer) {
          double percentDoneInt = (r.economy.getBalance(Bukkit.getOfflinePlayer(((Player) cs).getUniqueId())) / cost) * 100;
          DecimalFormat df = new DecimalFormat("#.##");
          if (percentDoneInt > 100) {
            percentDoneInt = 100;
          }
          String percentLeft = df.format(100 - percentDoneInt);
          String percentDone = df.format(percentDoneInt);
          s = s.replace("%PERCENTLEFT%", percentLeft).replace("%PERCENTDONE%", percentDone);
        }
        s = ChatColor.translateAlternateColorCodes('&', s);
        cs.sendMessage(s);
      }
    }

    String footer = getSurround("ranks.footer");
    if(footer != null) {
      cs.sendMessage(ChatColor.translateAlternateColorCodes('&', footer));
    }
    return true;
  }

  public String getSurround(String type) {
    String surround = Rankup.getInstance().getConfig().getString("messages.listRanks." + type);
    if(surround == null || surround.equals("none")) {
      return null;
    }

    int ranks = Rankup.getInstance().ranks.size();
    return ChatColor.translateAlternateColorCodes('&', surround)
        .replace("%TOTALRANKS%", ranks + "")
        .replace("%TOTALRANKSEXCLUSIVE%", ranks-1 + "");
  }
}
