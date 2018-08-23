package me.okx.rankup;

import me.okx.rankup.data.Rank;
import me.okx.rankup.exception.NotInLadderException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {
  private static FileConfiguration config;
  private static DecimalFormat moneyFormat;

  public static void reload() {
    config = Rankup.getInstance().getConfig();
    moneyFormat = new DecimalFormat(config.getString("placeholders.rankCostFormat"));
  }

  private static boolean containsIgnoreCase(String[] groups, String group) {
    for(String _group : groups) {
      if(_group.equalsIgnoreCase(group)) {
        return true;
      }
    }
    return false;
  }

  public static Rank getRank(Player op, boolean next) throws NotInLadderException {
    String[] groups = Rankup.getInstance().permission.getPlayerGroups(op);

    List<Rank> ranks = new ArrayList<>(Rankup.getInstance().ranks);
    Collections.reverse(ranks);

    // highest rank to lowest
    for (int i = 0; i < ranks.size(); i++) {
      Rank rank = ranks.get(i);
      if (containsIgnoreCase(groups, rank.getName())) {
        return ranks.get(i - (next ? 1 : 0));
      }
    }

    throw new NotInLadderException();
  }

  public static double getNthRankCost(Player player, int rank) {
    List<Rank> prices = Rankup.getInstance().ranks;
    double cost = prices.get(rank).getCost();
    try {
      return cost * Utils.getPrestigeGroupMultiplier(player);
    } catch (NotInLadderException e) {
      return cost;
    }
  }

  public static boolean isPrestigeGroup(String group) {
    return Rankup.getInstance().prestigeRanks
        .stream()
        .anyMatch(rank -> rank.equalsIgnoreCase(group));
  }

  public static Rank getPrestigeRank(Player op, boolean next) throws NotInLadderException {
    String[] groups = Rankup.getInstance().permission.getPlayerGroups(op);

    List<Rank> ranks = new ArrayList<>(Rankup.getInstance().prestigeRanks);
    Collections.reverse(ranks);

    // highest rank to lowest
    for (int i = 0; i < ranks.size(); i++) {
      Rank rank = ranks.get(i);
      if (containsIgnoreCase(groups, rank.getName())) {
        return ranks.get(i - (next ? 1 : 0));
      }
    }

    if (next) {
      return Rankup.getInstance().prestigeRanks.get(0);
    }
    throw new NotInLadderException();
  }

  public static int getPrestigeGroupLevel(String group) {
    List<Rank> ranks = Rankup.getInstance().prestigeRanks;
    for (int i = 0; i < ranks.size(); i++) {
      Rank rank = ranks.get(i);
      if (rank.getName().equalsIgnoreCase(group)) {
        return i + 1;
      }
    }

    return -1;
  }

  public static double getPrestigeGroupMultiplier(Player player) throws NotInLadderException {
    int lvl = Utils.getPrestigeGroupLevel(Utils.getPrestigeRank(player, false).getName());
    if (lvl == 0) {
      return 1;
    } else {
      return lvl * config.getDouble("options.prestigeRankMultiplier");
    }
  }

  public static String getShortened(double number) {
    if (!config.getBoolean("placeholders.useShortening")) {
      return String.valueOf(number);
    }

    List<String> shortened = config.getStringList("placeholders.shortened");
    String suffix = "";

    for (int i = shortened.size(); i > 0; i--) {
      double value = Math.pow(10, 3 * i);
      if (number >= value) {
        number /= value;
        suffix = shortened.get(i - 1);
      }
    }
    String result = moneyFormat.format(number);
    if (config.getBoolean("options.removePointZero") && result.endsWith(".0")) {
      result = result.substring(0, result.length() - 2);
    }
    result += suffix;
    return result;
  }

  public static String replace(String player, String rank, double amount, String string) {
    String endAmount;
    if (config.getBoolean("placeholders.useShortening")) {
      endAmount = getShortened(amount);
    } else {
      endAmount = String.valueOf(amount);
    }
    return string
        .replace("%PLAYER%", player)
        .replace("%RANK%", rank)
        .replace("%AMOUNT%", endAmount);
  }

  public static String replace(String player, String rank, String oldRank, double amount, String string) {
    String endAmount;
    if (config.getBoolean("placeholders.useShortening")) {
      endAmount = getShortened(amount);
    } else {
      endAmount = String.valueOf(amount);
    }
    return string
        .replace("%PLAYER%", player)
        .replace("%RANK%", rank)
        .replace("%AMOUNT%", endAmount)
        .replace("%OLDRANK%", oldRank);
  }

  public static String getString(String path) {
    return ChatColor.translateAlternateColorCodes('&', config.getString(path));
  }
}
