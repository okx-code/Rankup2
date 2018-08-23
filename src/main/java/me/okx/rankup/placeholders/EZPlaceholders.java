package me.okx.rankup.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.okx.rankup.Rankup;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class EZPlaceholders extends PlaceholderExpansion {
  @Override
  public String onPlaceholderRequest(Player player, String id) {
    return Placeholders.onPlaceholderRequest(player, id);
  }

  @Override
  public String getIdentifier() {
    return "rankup";
  }

  @Override
  public String getAuthor() {
    return "Okx";
  }

  @Override
  public String getVersion() {
    return "2.7";
  }
}