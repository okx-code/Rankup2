package me.okx.rankup.holder;

import me.okx.rankup.Rankup;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PruInventory implements InventoryHolder {
  private Inventory inventory;

  public PruInventory(String name) {
    this.inventory = Bukkit.createInventory(this,
        Rankup.getInstance().getConfig().getInt("gui.size"), name);
    this.inventory.setContents(Rankup.getInstance().rankupInv);
  }

  @Override
  public Inventory getInventory() {
    return inventory;
  }

  public void open(Player player) {
    player.openInventory(inventory);
  }
}
