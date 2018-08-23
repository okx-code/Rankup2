package me.okx.rankup;

import me.okx.rankup.command.InfoCommand;
import me.okx.rankup.command.PrestigeCommand;
import me.okx.rankup.command.PrestigeListCommand;
import me.okx.rankup.command.RankListCommand;
import me.okx.rankup.command.RankupCommand;
import me.okx.rankup.data.Rank;
import me.okx.rankup.exception.NotInLadderException;
import me.okx.rankup.placeholders.EZPlaceholders;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Rankup
 *
 * @author Okx
 */
public class Rankup extends JavaPlugin {
  public Permission permission = null;
  public Economy economy = null;
  public Chat chat = null;
  public List<Rank> ranks;
  public List<Rank> prestigeRanks;
  public Map<String, String> placeholderAliases;

  private static Rankup plugin;

  public static Rankup getInstance() {
    return plugin;
  }

  private boolean setupPermissions() {
    RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
    permission = rsp.getProvider();
    return permission != null;
  }

  private boolean setupEconomy() {
    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return false;
    }
    economy = rsp.getProvider();
    return economy != null;
  }

  private boolean setupChat() {
    RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
    chat = rsp.getProvider();
    return chat != null;
  }

  @Override
  public void onEnable() {
    plugin = this;

    if (!setupPermissions()) {
      getLogger().severe("sUnable to find permission plugin");
    }
    if (!setupEconomy()) {
      getLogger().severe("Unable to find economy plugin");
    }
    if (!setupChat()) {
      getLogger().warning("Unable to find chat plugin");
    }

    Metrics metrics = new Metrics(this);
    metrics.addCustomChart(new Metrics.SimplePie("prestige") {
      @Override
      public String getValue() {
        return getConfig().getBoolean("options.prestigeDisabled") ? "disabled" : "enabled";
      }
    });
    metrics.addCustomChart(new Metrics.SimplePie("confirmation") {
      @Override
      public String getValue() {
        return getConfig().getString("confirmation.type");
      }
    });

    this.saveDefaultConfig();

    Utils.reload();
    setup();
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      new EZPlaceholders().register();
    }
    getCommand("rankup").setExecutor(new RankupCommand());
    getCommand("ranks").setExecutor(new RankListCommand());
    getCommand("pru").setExecutor(new InfoCommand());
  }

  public void setup() {
    rankupInv = new ItemStack[getConfig().getInt("gui.size")];
    if (getConfig().getString("confirmation.type").equalsIgnoreCase("gui")) {
      setupInventory();
      if (!Bukkit.getVersion().contains("1.13")) {
        getLogger().warning("Using legacy material parsing.");
      }
      if (!EventHandlers.registered) {
        getServer().getPluginManager().registerEvents(new EventHandlers(), this);
      }
    }

    if (!getConfig().getBoolean("options.prestigeDisabled")) {
      getCommand("prestige").setExecutor(new PrestigeCommand());
      getCommand("prestiges").setExecutor(new PrestigeListCommand());
    }

    this.ranks = new ArrayList<>();
    for (String rp : getConfig().getStringList("ranks")) {
      String[] rpa = rp.replace(" ", "").split(":");
      this.ranks.add(new Rank(rpa[0], Double.parseDouble(rpa[1].replace(",", ""))));
    }

    this.prestigeRanks = new ArrayList<>();
    for (String rp : getConfig().getStringList("prestige")) {
      String[] rpa = rp.replace(" ", "").split(":");
      this.prestigeRanks.add(new Rank(rpa[0], Double.parseDouble(rpa[1].replace(",", ""))));
    }

    this.placeholderAliases = new HashMap<>();
    for(String placeholderAlias : getConfig().getStringList("placeholders.aliases")) {
      String[] parts = placeholderAlias.replace(" ", "").split(":");
      placeholderAliases.put(parts[0], parts[1]);
    }

    Bukkit.getScheduler().runTask(this, this::checkVersion);
  }

  private void checkVersion() {
    String fullConfigVersion = getConfig().getString("version");
    if (!getConfig().isSet("version")) {
      fullConfigVersion = "2.7";
    }
    String configVersion = getMinorVersion(fullConfigVersion);
    String version = getMinorVersion(getDescription().getVersion());
    if (version.equals(configVersion)) {
      // we are on the latest config version
      return;
    }

    getLogger().severe("You are using an outdated config!");
    getLogger().severe("Config file is from " + configVersion + ", latest config is " + version + ".");
    getLogger().severe("This means that some of the plugin might break!");
    getLogger().severe("To update, please rename your config file, and run /pru reload to generate a new config file.");
    getLogger().severe("If that does not work, restart your server.");
    getLogger().severe("You may then copy in your config values from the old config.");
  }

  private String getMinorVersion(String version) {
    String[] parts = version.split("\\.");
    return parts[0] + "." + parts[1];
  }

  @Override
  public void onDisable() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (Arrays.equals(p.getOpenInventory().getTopInventory().getContents(), rankupInv)) {
        p.closeInventory();
      }
    }
  }

  public ItemStack[] rankupInv;

  public void setupInventory() {
    ItemStack yes = buildItem("yes");
    ItemStack no = buildItem("no");
    ItemStack filler = buildItem("fill");

    for (int i = 0; i < rankupInv.length; i++) {
      int j = i;
      if (getIndexes("yes").anyMatch(x -> x == j)) {
        rankupInv[i] = yes;
      } else if (getIndexes("no").anyMatch(x -> x == j)) {
        rankupInv[i] = no;
      } else {
        rankupInv[i] = filler;
      }
    }
  }

  private IntStream getIndexes(String name) {
    String[] indexes = getConfig().getString("gui." + name + ".index").split("-");
    return IntStream.rangeClosed(Integer.valueOf(indexes[0]), Integer.valueOf(indexes[1]));
  }

  public ItemStack buildItem(String name) {
    String matStr = getString(name, "material");
    ItemStack item;
    if (!Bukkit.getVersion().contains("1.13")) {
      if (matStr.equals("BLACK_STAINED_GLASS_PANE")) {
        matStr = "STAINED_GLASS_PANE:15";
      }
      String[] parts = matStr.split(":", 2);
      short data = 0;
      if (parts.length == 2) {
        data = Short.parseShort(parts[1]);
      }

      item = new ItemStack(Material.getMaterial(parts[0].toUpperCase()), 1, data);
    } else {
      item = new ItemStack(Material.getMaterial(matStr.toUpperCase()));
    }
    ItemMeta im = item.getItemMeta();
    String lore = getString(name, "lore");
    if (!lore.equals("")) {
      im.setLore(Arrays.asList(lore.split("\n")));
    }
    im.setDisplayName(ChatColor.translateAlternateColorCodes('&', getString(name, "name")));
    item.setItemMeta(im);
    return item;
  }

  private String getString(String name, String value) {
    return Utils.getString("gui." + name + "." + value);
  }

  public void doRankup(Player player) {
    Rank currentRank;
    try {
      currentRank = Utils.getRank(player, false);
    } catch (NotInLadderException ignored) {
      return;
    }

    Rank nextRank;
    try {
      nextRank = Utils.getRank(player, true);
    } catch (IndexOutOfBoundsException ignored) {
      player.sendMessage(Utils.replace(
          player.getName(),
          currentRank.getName(),
          currentRank.getCost(),
          Utils.getString("messages.highestRank")));
      return;
    } catch (NotInLadderException ignored) {
      return;
    }
    String nextRankName = nextRank.getName();
    double nextCost;
    try {
      nextCost = nextRank.getCost() * Utils.getPrestigeGroupMultiplier(player);
    } catch (NotInLadderException ignored) {
      nextCost = nextRank.getCost();
    }
    if (economy.getBalance(player) < nextCost) {
      String msg = Utils.getString("messages.noMoneyToRankup");
      msg = msg.replaceAll("%AMOUNTLEFT%",
          String.valueOf(nextCost - economy.getBalance(player))).replaceAll("%AMOUNTTOTAL%",
          String.valueOf(nextCost));
      msg = Utils.replace(player.getName(), nextRankName, nextCost, msg);
      for (String s : msg.split("\n")) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
      }
      return;
    }
    if (getConfig().getBoolean("messages.publicMessage")) {
      Bukkit.broadcastMessage(Utils.replace(player.getName(), nextRankName, nextCost, Utils.getString("messages.successfulRankupPublic")));
    }
    player.sendMessage(Utils.replace(player.getName(), nextRankName, nextCost, Utils.getString("messages.successfulRankupPrivate")));
    economy.withdrawPlayer(player, nextCost);
    permission.playerRemoveGroup(null, player, currentRank.getName());
    permission.playerAddGroup(null, player, nextRankName);
    for (String cmd : getConfig().getStringList("runCommands")) {
      cmd = Utils.replace(player.getName(), nextRankName, currentRank.getName(), nextCost, cmd);
      RunCommand rc = new RunCommand(player, cmd);
      rc.runCommand(nextRankName, false);
    }
  }

  public void doPrestige(Player player) {
    String rankupGroup;
    try {
      rankupGroup = Utils.getRank(player, false).getName();
    } catch (NotInLadderException e) {
      return;
    }
    String currentPrestigeGroup;
    try {
      currentPrestigeGroup = Utils.getPrestigeRank(player, false).getName();
    } catch (NotInLadderException e) {
      currentPrestigeGroup = Utils.getString("placeholders.noPrestigeRank");
    }
    String nextPrestigeGroup;
    try {
      nextPrestigeGroup = Utils.getPrestigeRank(player, true).getName();
    } catch (NotInLadderException | IndexOutOfBoundsException e) {
      return;
    }

    double cost = 0;
    try {
      cost = Utils.getPrestigeRank(player, true).getCost();
    } catch (NotInLadderException e) {
      // this should never be thrown
      e.printStackTrace();
    }
    Rankup r = Rankup.getInstance();
    if (r.economy.getBalance(player) < cost) {
      String msg = Utils.getString("messages.noMoneyToPrestige");
      msg = Utils.replace(player.getName(), nextPrestigeGroup, cost, msg)
          .replaceAll("%AMOUNTTOTAL%", Utils.getShortened(cost))
          .replaceAll("%AMOUNTLEFT%", Utils.getShortened(cost - r.economy.getBalance(player)));
      for (String s : msg.split("\n")) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
      }
      return;
    }
    r.economy.withdrawPlayer(player, cost);
    if (currentPrestigeGroup != null) {
      r.permission.playerRemoveGroup(null, player, currentPrestigeGroup);
    }
    r.permission.playerAddGroup(null, player, nextPrestigeGroup);
    r.permission.playerRemoveGroup(null, player, rankupGroup);
    r.permission.playerAddGroup(null, player, r.getConfig().getString("options.prestigeToRank"));
    if (r.getConfig().getBoolean("messages.publicPrestigeMessage")) {
      Bukkit.broadcastMessage(Utils.replace(player.getName(), nextPrestigeGroup, cost, Utils.getString("messages.successfulPrestigePublic")));
    }
    player.sendMessage(Utils.replace(player.getName(), nextPrestigeGroup, cost, Utils.getString("messages.successfulPrestigePrivate")));
    for (String cmd : r.getConfig().getStringList("runCommands")) {
      cmd = Utils.replace(player.getName(), nextPrestigeGroup, rankupGroup, cost, cmd);
      RunCommand rc = new RunCommand(player, cmd);
      rc.runCommand(nextPrestigeGroup, true);
    }
  }
}


// A -> B -> C
// A /rankup + $10,000 -> B