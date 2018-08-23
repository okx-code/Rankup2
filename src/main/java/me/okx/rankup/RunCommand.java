package me.okx.rankup;

import me.okx.rankup.exception.NotInLadderException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RunCommand {
  private boolean _console;
  private String _rank;
  private boolean _prestige;
  private String _command;
  private Player _player;
  private boolean _alwaysRun;

  public RunCommand(Player player, String runCommand) {
    _player = player;

    String[] parts = runCommand.split(":", 3);
    _console = !parts[0].equalsIgnoreCase("PLAYER");

    _prestige = Utils.isPrestigeGroup(parts[1]);
    if (parts[1].equalsIgnoreCase("RANKUP")) {
      _prestige = false;
      _alwaysRun = true;
    } else if (parts[1].equalsIgnoreCase("PRESTIGE")) {
      _prestige = true;
      _alwaysRun = true;
    } else {
      _rank = parts[1];
      _alwaysRun = false;
    }

    _command = parts[2];
  }

  public void runCommand(String rank, boolean isPrestiging) {
    if (!shouldRun(rank, isPrestiging)) {
      return;
    }
    Bukkit.dispatchCommand(_console ? Bukkit.getConsoleSender() : _player, _command);
  }

  public boolean shouldRun(String rank, boolean isPrestiging) {
    // should be run after player ranks up
    if (_alwaysRun) {
      return _prestige == isPrestiging;
    }

//    String group;
//    try {
//      if(_prestige) {
//        group = Utils.getPrestigeRank(_player, false).getName();
//      } else {
//        group = Utils.getRank(_player, false).getName();
//      }
//    } catch (NotInLadderException e) {
//      // the player just ranked up... how are they not in the ladder?
//      throw new IllegalStateException();
//    }
    return rank.equalsIgnoreCase(_rank);
  }
}
