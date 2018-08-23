package me.okx.rankup.command;

import me.okx.rankup.Rankup;
import me.okx.rankup.Utils;
import me.okx.rankup.exception.NotInLadderException;
import me.okx.rankup.holder.PrestigeInventory;
import me.okx.rankup.holder.RankupInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.WeakHashMap;

public class PrestigeCommand implements CommandExecutor {
  private Map<Player, Long> confirming = new WeakHashMap<>();

  @Override
  public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
    if (!(cs instanceof Player)) {
      cs.sendMessage("You must be a player to do this!");
      return true;
    }
    Player player = (Player) cs;
    String rGroup;
    try {
      rGroup = Utils.getRank(player, false).getName();
    } catch (NotInLadderException e) {
      String msg = Utils.getString("messages.notInLadder");
      msg = Utils.replace(player.getName(), "", 0, msg);
      for (String s : msg.split("\n")) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
      }
      return true;
    }
    String curGroup;
    try {
      curGroup = Utils.getPrestigeRank(player, false).getName();
    } catch (NotInLadderException e) {
      curGroup = Utils.getString("placeholders.noPrestigeRank");
    }
    String group;
    try {
      group = Utils.getPrestigeRank(player, true).getName();
    } catch (NotInLadderException e) {
      // next is true so this will not be thrown
      e.printStackTrace();
      return true;
    } catch (IndexOutOfBoundsException e) {
      String msg = Utils.getString("messages.highestPrestigeGroup");
      msg = Utils.replace(player.getName(), curGroup, 0, msg);
      for (String s : msg.split("\n")) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
      }
      return true;
    }

    boolean tooLow = true;
    for (String rank : Utils.getString("options.prestigeAtRanks").replace(" ", "").split(",")) {
      if (rank.equalsIgnoreCase(rGroup)) {
        tooLow = false;
        break;
      }
    }
    if (tooLow) {
      String msg = Utils.getString("messages.rankTooLowToPrestige");
      msg = Utils.replace(player.getName(), group, rGroup, 0, msg);
      for (String s : msg.split("\n")) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
      }
      return true;
    }

    Rankup r = Rankup.getInstance();

    long timeoutMillis = r.getConfig().getLong("confirmation.timeout") * 1_000;
    Long added = confirming.remove(player);
    if (added != null && System.currentTimeMillis() - added < timeoutMillis) {
      r.doPrestige(player);
      return true;
    }

    double cost;
    try {
      cost = Utils.getPrestigeRank(player, true).getCost();
    } catch (NotInLadderException e) {
      // this should never be thrown
      e.printStackTrace();
      return true;
    }

    if (canAfford(player, cost)) {
      switch (r.getConfig().getString("confirmation.type")) {
        case "gui":
          String name = Utils.getString("gui.name.prestige");
          name = Utils.replace(player.getName(), group, cost, name);
          name = name.substring(0, Math.min(32, name.length()));
          new PrestigeInventory(name).open(player);
          return true;
        case "text":
          player.sendMessage(Utils.replace(player.getName(), group, cost, Utils.getString("confirmation.areYouSurePrestige")));
          confirming.put(player, System.currentTimeMillis());
          return true;
        case "none":
          r.doPrestige(player);
          return true;
        default:
          throw new IllegalArgumentException("Invalid confirmation type.");
      }
    } else {
      double balance = Rankup.getInstance().economy.getBalance(player);
      String msg = Utils.getString("messages.noMoneyToPrestige");
      String sTotal = Utils.getShortened(cost);
      String sLeft = Utils.getShortened(cost - balance);
      msg = msg.replaceAll("%AMOUNTLEFT%", sLeft).replaceAll("%AMOUNTTOTAL%", sTotal);
      msg = Utils.replace(player.getName(), group, curGroup, cost, msg);
      for (String s : msg.split("\n")) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
      }
      return true;
    }
  }

  private boolean canAfford(Player player, double cost) {
    double balance = Rankup.getInstance().economy.getBalance(player);
    return balance >= cost;
  }
}

