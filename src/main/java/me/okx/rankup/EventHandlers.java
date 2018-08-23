package me.okx.rankup;

import me.okx.rankup.holder.PrestigeInventory;
import me.okx.rankup.holder.RankupInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;

public class EventHandlers implements Listener {
  public static boolean registered = false;

  public EventHandlers() {
    registered = true;
  }

  @EventHandler
  public void onMove(InventoryClickEvent e) {
    if (e.getInventory() == null || e.getCurrentItem() == null) {
      return;
    }
    if (!Arrays.equals(e.getInventory().getContents(), Rankup.getInstance().rankupInv)) {
      return;
    }
    Rankup r = Rankup.getInstance();
    e.setCancelled(true);
    if (e.getCurrentItem().equals(r.buildItem("yes"))) {
      Bukkit.getScheduler().runTask(r, () -> e.getWhoClicked().closeInventory());

      Player p = (Player) e.getWhoClicked();

      Inventory inventory = e.getInventory();

      if(inventory.getHolder() instanceof PrestigeInventory) {
        r.doPrestige(p);
      } else if(inventory.getHolder() instanceof RankupInventory) {
        r.doRankup(p);
      }
    } else if (e.getCurrentItem().equals(r.buildItem("no"))) {
      Bukkit.getScheduler().runTask(r, () -> e.getWhoClicked().closeInventory());
    }
  }
}
