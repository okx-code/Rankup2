package me.okx.rankup.placeholders;

import me.okx.rankup.Rankup;
import me.okx.rankup.Utils;
import me.okx.rankup.data.Rank;
import me.okx.rankup.exception.NotInLadderException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Map;

public class Placeholders {
  public static String onPlaceholderRequest(Player player, String id) {
    if (player == null) {
      return "";
    }
    Rankup r = Rankup.getInstance();
    FileConfiguration config = r.getConfig();

    // map aliases
    for(Map.Entry<String, String> entry : r.placeholderAliases.entrySet()) {
      if(id.equalsIgnoreCase(entry.getValue())) {
        id = entry.getKey();
        break;
      }
    }

    if (id.equalsIgnoreCase("player")) {
      return player.getName();
    }
    if (id.equalsIgnoreCase("current_rank")) {
      try {
        return Utils.getRank(player, false).getName();
      } catch (NotInLadderException e) {
        return Utils.getString("placeholders.notInLadder");
      }
    }
    if (id.equalsIgnoreCase("next_rank")) {
      try {
        return Utils.getRank(player, true).getName();
      } catch (NotInLadderException e) {
        return Utils.getString("placeholders.notInLadder");
      } catch(ArrayIndexOutOfBoundsException e) {
        return Utils.getString("placeholders.highestRank");
      }
    }
    if (id.equalsIgnoreCase("current_rank")) {
      try {
        return Utils.getRank(player, false).getName();
      } catch (NotInLadderException e) {
        return Utils.getString("placeholders.notInLadder");
      }
    }
    if (id.equalsIgnoreCase("next_rank_cost")) {
      return getCost(player, true);
    }
    if (id.equalsIgnoreCase("next_rank_cost_formatted")) {
      return getRankCost(config, getCost(player, true));
    }
    if (id.equalsIgnoreCase("current_rank_cost")) {
      return getCost(player, false);
    }
    if (id.equalsIgnoreCase("current_rank_cost_formatted")) {
      return getRankCost(config, getCost(player, false));
    }
    // PRESTIGE START
    if (id.equalsIgnoreCase("current_prestige_rank")) {
      try {
        return Utils.getPrestigeRank(player, false).getName();
      } catch (NotInLadderException ignored) {
        return Utils.getString("placeholders.noPrestigeRank");
      } catch(ArrayIndexOutOfBoundsException ignored) {
      }
    }
    if (id.equalsIgnoreCase("next_prestige_rank")) {
      try {
        return Utils.getPrestigeRank(player, true).getName();
      } catch (NotInLadderException ignored) {
        return Utils.getString("placeholders.notInLadder");
      } catch (ArrayIndexOutOfBoundsException e) {
        return Utils.getString("placeholders.highestRank");
      }
    }
    if (id.equalsIgnoreCase("current_prestige_rank_prefix")) {
      try {
        return r.chat.getGroupPrefix((String) null, Utils.getPrestigeRank(player, false).getName());
      } catch (NotInLadderException e) {
        return Utils.getString("placeholders.notInLadder");
      }
    }
    if (id.equalsIgnoreCase("next_prestige_rank_prefix")) {
      try {
        return r.chat.getGroupPrefix((String) null, Utils.getPrestigeRank(player, true).getName());
      } catch (NotInLadderException e) {
        return Utils.getString("placeholders.notInLadder");
      } catch (ArrayIndexOutOfBoundsException e) {
        return Utils.getString("placeholders.highestRank");
      }
    }
    if (id.equalsIgnoreCase("next_prestige_cost")) {
      return getPrestigeCost(player, true);
    }
    if (id.equalsIgnoreCase("next_prestige_cost_formatted")) {
      return Utils.getShortened(Double.valueOf(getPrestigeCost(player, true)));
    }
    // PRESTIGE END
    if (id.equalsIgnoreCase("percent_done")) {
      if (isAtLastRank(player)) {
        return "100";
      }
      double percentLeft = getPercentLeft(player);
      String s = String.valueOf(100 - percentLeft);
      if (s.startsWith("-")) {
        return "0";
      } else if (Double.valueOf(s) > 100) {
        return "100";
      } else {
        return s;
      }
    }
    if (id.equalsIgnoreCase("percent_done_formatted")) {
      DecimalFormat df = new DecimalFormat(config.getString("placeholders.percentDoneFormat"));
      if (isAtLastRank(player)) {
        return df.format(100);
      }
      double percentLeft = getPercentLeft(player);
      String s = df.format(100 - percentLeft);
      if (s.startsWith("-")) {
        return df.format(0);
      } else if (Double.valueOf(s) > 100) {
        return df.format(100);
      } else {
        return s;
      }
    }
    if (id.equalsIgnoreCase("percent_left")) {
      if (isAtLastRank(player)) {
        return "0";
      }
      double percentLeft = getPercentLeft(player);
      String s = String.valueOf(percentLeft);
      if (s.startsWith("-")) {
        return "0";
      } else {
        return s;
      }
    }
    if (id.equalsIgnoreCase("percent_left_formatted")) {
      DecimalFormat df = new DecimalFormat(config.getString("placeholders.percentLeftFormat"));
      if (isAtLastRank(player)) {
        return df.format("0");
      }
      double percentLeft = getPercentLeft(player);
      String s = df.format(percentLeft);
      if (s.startsWith("-")) {
        return df.format(0);
      } else {
        return s;
      }
    }
    if (id.equalsIgnoreCase("current_rank_prefix")) {
      try {
        return r.chat.getGroupPrefix((String) null, Utils.getRank(player, false).getName());
      } catch (NotInLadderException e) {
        return Utils.getString("placeholders.notInLadder");
      }
    }
    if (id.equalsIgnoreCase("next_rank_prefix")) {
      try {
        return r.chat.getGroupPrefix((String) null, Utils.getRank(player, true).getName());
      } catch (NotInLadderException e) {
        return Utils.getString("placeholders.notInLadder");
      } catch (IndexOutOfBoundsException e) {
        return Utils.getString("placeholders.highestRank");
      }
    }
    return null;
  }

  private static double getPercentLeft(Player player) {
    double percentLeft = 0;
    try {
      Rank rank = Utils.getRank(player, true);
      if (rank != null) {
        double cost = rank.getCostWithPrestige(player);
        percentLeft = ((cost - Rankup.getInstance().economy.getBalance(player)) / cost) * 100;
      }
    } catch (NotInLadderException ignored) {
    }
    return percentLeft;
  }

  private static String getRankCost(FileConfiguration config, String rankCost) {
    DecimalFormat df = new DecimalFormat(config.getString("placeholders.rankCostFormat"));
    double cost = Double.parseDouble(rankCost);
    String formatted;
    if (config.getBoolean("placeholders.useShortening")) {
      formatted = Utils.getShortened(cost);
      // if we can't shorten it
      if(formatted.equals(String.valueOf(cost))) {
        // just add commas
        formatted = df.format(cost);
      }
    } else {
      formatted = String.valueOf(cost);
    }
    return formatted;
  }

  public static boolean isAtLastRank(Player p) {
    try {
      return Rankup.getInstance().ranks.get(Rankup.getInstance().ranks.size() - 1).getName()
          .equals(Utils.getRank(p, false).getName());
    } catch (NotInLadderException e) {
      return false;
    }
  }

  private static String getCost(Player player, boolean next) {
    try {
      return String.valueOf(Utils.getRank(player, next).getCostWithPrestige(player));
    } catch (NotInLadderException | ArrayIndexOutOfBoundsException e) {
      return "0";
    }
  }

  private static String getPrestigeCost(Player player, boolean next) {
    try {
      return String.valueOf(Utils.getPrestigeRank(player, next).getCost());
    } catch (NotInLadderException | ArrayIndexOutOfBoundsException e) {
      return "0";
    }
  }
}