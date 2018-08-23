package me.okx.rankup.data;

import me.okx.rankup.Utils;
import me.okx.rankup.exception.NotInLadderException;
import org.bukkit.entity.Player;

public class Rank {
  private String name;
  private double cost;

  public Rank(String name, double cost) {
    this.name = name;
    this.cost = cost;
  }

  public boolean equalsIgnoreCase(String otherName) {
    return name.equalsIgnoreCase(otherName);
  }

  public String getName() {
    return name;
  }

  public double getCost() {
    return cost;
  }

  public double getCostWithPrestige(Player player) {
    try {
      return cost * Utils.getPrestigeGroupMultiplier(player);
    } catch (NotInLadderException ignored) {
      return cost;
    }
  }
}
