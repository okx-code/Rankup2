package me.okx.rankup.command;

import me.okx.rankup.Rankup;
import me.okx.rankup.Utils;
import me.okx.rankup.data.Rank;
import me.okx.rankup.exception.NotInLadderException;
import me.okx.rankup.placeholders.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class PrestigeListCommand implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
    Rankup r = Rankup.getInstance();
    FileConfiguration config = r.getConfig();
    boolean isPlayer = cs instanceof Player;

    String header = getSurround("prestiges.header");
    if(header != null) {
      cs.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
    }

    int nextPrestige;
    try {
      Rank nextPrestigeRank = Utils.getPrestigeRank((Player) cs, true);
      nextPrestige = Utils.getPrestigeGroupLevel(nextPrestigeRank.getName()) - 1;
    } catch(NotInLadderException ex) {
      throw new AssertionError();
    } catch(ArrayIndexOutOfBoundsException ex) {
      nextPrestige = r.prestigeRanks.size();
    }

    boolean canPrestige = false;
    String oldRank = "";
    if(isPlayer) {
      try {
        oldRank = Utils.getRank((Player) cs, false).getName();
        for (String prestigeRank : Utils.getString("options.prestigeAtRanks").replace(" ", "").split(",")) {
          if (oldRank.equalsIgnoreCase(prestigeRank)) {
            canPrestige = true;
            break;
          }
        }
      } catch (NotInLadderException ignored) {
      }
    }

    for (int x = 0; x < r.prestigeRanks.size(); x++) {
      String messageType;
      if(nextPrestige == x) {
        if(canPrestige) {
          messageType = "inProgress";
        } else {
          messageType = "current";
        }
      } else if(nextPrestige > x) {
        messageType = "complete";
      } else {
        messageType = "default";
      }

      Rank rank = r.prestigeRanks.get(x);
      String percentLeftMessage = "";
      String percentDoneMessage = "";
      if(isPlayer) {
        double percentLeft = getPercentLeft((Player) cs);
        percentLeftMessage = formatPercent(new DecimalFormat(config.getString("placeholders.percentLeftFormat")), percentLeft);
        percentDoneMessage = formatPercent(new DecimalFormat(config.getString("placeholders.percentDoneFormat")), 100 - percentLeft);
      }

      String message = config.getString("messages.listRanks.prestiges." + messageType)
          .replace("%PERCENTLEFT%", percentLeftMessage)
          .replace("%PERCENTDONE%", percentDoneMessage);
      message = Utils.replace(isPlayer ? "" : cs.getName(), rank.getName(), oldRank, rank.getCost(), message);
      message = ChatColor.translateAlternateColorCodes('&', message);

      cs.sendMessage(ChatColor
          .translateAlternateColorCodes('&',
              Utils.replace(isPlayer ? "" : cs.getName(), rank.getName(), oldRank, rank.getCost(), message)));
    }

    String footer = getSurround("prestiges.footer");
    if(footer != null) {
      cs.sendMessage(ChatColor.translateAlternateColorCodes('&', footer));
    }
    return true;
  }

  private String formatPercent(DecimalFormat df, double percent) {
    return df.format(Math.max(0, Math.min(100, percent)));
  }

  private double getPercentLeft(Player player) {
    double percentLeft = 0;
    try {
      Rank rank = Utils.getPrestigeRank(player, true);
      if (rank != null) {
        double cost = rank.getCost();
        percentLeft = ((cost - Rankup.getInstance().economy.getBalance(player)) / cost) * 100;
      }
    } catch (NotInLadderException ignored) {
    }
    return percentLeft;
  }

  private String getSurround(String type) {
    String surround = Rankup.getInstance().getConfig().getString("messages.listRanks." + type);
    if(surround == null || surround.equals("none")) {
      return null;
    }

    int ranks = Rankup.getInstance().prestigeRanks.size();
    return ChatColor.translateAlternateColorCodes('&', surround)
        .replace("%TOTALRANKS%", ranks + "")
        .replace("%TOTALRANKSEXCLUSIVE%", ranks-1 + "");
  }
}
