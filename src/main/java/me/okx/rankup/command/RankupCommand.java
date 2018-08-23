package me.okx.rankup.command;

import me.okx.rankup.Rankup;
import me.okx.rankup.Utils;
import me.okx.rankup.data.Rank;
import me.okx.rankup.exception.NotInLadderException;
import me.okx.rankup.holder.RankupInventory;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class RankupCommand implements CommandExecutor {
  private Map<Player, Long> confirming = new WeakHashMap<>();

  @Override
  public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
    if (!(cs instanceof Player)) {
      cs.sendMessage("You must be a player to do this!");
      return true;
    }
    Player p = (Player) cs;
    Rankup r = Rankup.getInstance();
    Rank rank;
    try {
      rank = Utils.getRank(p, true);
    } catch (NotInLadderException ignored) {
      String msg = Utils.getString("messages.notInLadder");
      msg = Utils.replace(p.getName(), Utils.getString("placeholders.notInLadder"), 0, msg);
      for (String s : msg.split("\n")) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
      }
      return true;
    } catch (ArrayIndexOutOfBoundsException ignored) {
      String currentGroup;
      try {
        currentGroup = Utils.getRank(p, false).getName();
      } catch (NotInLadderException e) {
        e.printStackTrace();
        return false;
      }
      if (!r.getConfig().getBoolean("options.prestigeDisabled")) {
        String msg = Utils.getString("messages.highestRankToPrestige");
        try {
          // try to get next prestige rank and see if it works
          Utils.getPrestigeRank(p, true);
        } catch (NotInLadderException e) {
        } catch (IndexOutOfBoundsException e) {
          // if there is no next prestige rank send a different message
          msg = Utils.getString("messages.highestRank");
        }
        msg = Utils.replace(p.getName(), currentGroup, 0, msg);
        for (String s : msg.split("\n")) {
          p.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
        }
      } else {
        String msg = Utils.getString("messages.highestRank");
        msg = Utils.replace(p.getName(), currentGroup, 0, msg);
        for (String s : msg.split("\n")) {
          p.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
        }
      }
      return true;
    }

    String group = rank.getName();
    double cost;
    try {
      cost = rank.getCost() * Utils.getPrestigeGroupMultiplier(p);
    } catch (NotInLadderException ignored) {
      cost = rank.getCost();
    }

    long timeoutMillis = r.getConfig().getLong("confirmation.timeout") * 1_000;
    Long added = confirming.remove(p);
    if (added != null && System.currentTimeMillis() - added < timeoutMillis) {
      r.doRankup(p);
      return true;
    }

    if (canAfford(p, cost)) {
      switch (r.getConfig().getString("confirmation.type").toLowerCase()) {
        case "gui":
          String name = Utils.getString("gui.name.rankup");
          name = Utils.replace(p.getName(), group, cost, name);
          name = name.substring(0, Math.min(32, name.length()));
          new RankupInventory(name).open(p);
          return true;
        case "text":
          p.sendMessage(Utils.replace(p.getName(), group, cost, Utils.getString("confirmation.areYouSureRankup")));
          confirming.put(p, System.currentTimeMillis());
          return true;
        case "none":
          r.doRankup(p);
          return true;
        default:
          throw new IllegalArgumentException("Invalid confirmation type.");
      }
    } else {
      double balance = Rankup.getInstance().economy.getBalance(p);
      String msg = Utils.getString("messages.noMoneyToRankup");
      String sTotal = Utils.getShortened(cost);
      String sLeft = Utils.getShortened(cost - balance);
      msg = msg.replaceAll("%AMOUNTLEFT%", sLeft).replaceAll("%AMOUNTTOTAL%", sTotal);
      msg = Utils.replace(p.getName(), group, cost, msg);
      for (String s : msg.split("\n")) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
      }
      return true;
    }
  }

  private boolean canAfford(Player player, double cost) {
    double balance = Rankup.getInstance().economy.getBalance(player);
    return balance >= cost;
  }
}
