package net.gqrden.spleef.game;

import net.gartexapi.GartexAPI;
import net.gartexapi.gameapi.GameAPI;
import net.gartexapi.gameapi.enums.GameSettings;
import net.gartexapi.gameapi.enums.GameState;
import net.gartexapi.gameapi.enums.TeamManager;
import net.gartexapi.gameapi.game.Game;
import net.gartexapi.gameapi.game.depend.PlayerKillEvent;
import net.gartexapi.gameapi.game.gamemodes.SpectatorMode;
import net.gartexapi.gameapi.game.listener.EndGameEvent;
import net.gartexapi.gameapi.game.listener.RestartGameEvent;
import net.gartexapi.gameapi.game.listener.StartGameEvent;
import net.gartexapi.gameapi.game.module.EndModule;
import net.gartexapi.gameapi.game.team.SelectionTeam;
import net.gartexapi.gameapi.stats.Stats;
import net.gartexapi.gameapi.util.GameUtil;
import net.gartexapi.nms.scoreboard.Board;
import net.gartexapi.util.other.BukkitUtil;
import net.gartexapi.util.other.ItemUtil;
import net.gqrden.spleef.SUser;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;

public class GameFactory implements Listener {

    public static Stats getStats() {
        return Game.getInstance().getStats();
    }

    private void check() {

        BukkitUtil.runTaskLater(1, () -> {

            int aliveTeam = 0;

            for (SUser sUser : SUser.getTeams().values()) {

                if (sUser.isAlive()) {

                    aliveTeam++;

                }

            }

            if (aliveTeam <= 1) {

                if (GameState.END == GameState.getCurrent())

                    return;

                new EndModule();

            }

        });
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();

        if ((GameState.getCurrent() == GameState.GAME)) {
            if (e.getBlock().getType() == Material.SNOW_BLOCK) {
                e.setCancelled(false);
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onStartGame(StartGameEvent e) {
        Map<Player, TeamManager> selectedTeams = SelectionTeam.getSelectedTeams();
        Map<String, SUser> teams = SUser.getTeams();

        for (Map.Entry<Player, TeamManager> selected : selectedTeams.entrySet()) {
            teams.get(selected.getValue().getTeam()).addPlayer(selected.getKey());
        }

        GartexAPI.getScoreBoardAPI().removeDefaultTags();

        SUser sUser = teams.values().stream().findFirst().orElse(null);

        for (Player player : GameUtil.getAlivePlayers()) {
            for (SUser team : teams.values()) {
                if (team.size() < GameSettings.playersInTeam) {
                    sUser = team;
                    break;
                }
            }
            if (!selectedTeams.containsKey(player)) {
                assert sUser != null;
                sUser.addPlayer(player);
            }
            Board board = GartexAPI.getScoreBoardAPI().createBoard();

            board.setDisplayName("§e§l" + GameSettings.displayName);
            board.showTo(player);
        }

        for (SUser teamm : SUser.getTeams().values()) {
            teamm.setTags();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§e§l" + GameSettings.displayName, "Игра началась");

            player.setGameMode(GameMode.SURVIVAL);

            player.setHealth(20L);
            player.setFoodLevel(20);

            player.getInventory().clear();
            player.getInventory().addItem(ItemUtil.newBuilder(Material.DIAMOND_SPADE).addEnchantment(Enchantment.DIG_SPEED, 10).addEnchantment(Enchantment.DURABILITY, 5)
                    .setUnbreakable(true)
                    .build());

        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (GameUtil.isSpectator(p)) return;

        if (e.getTo().getBlock().getType() == Material.WATER || e.getTo().getBlock().getType() == Material.STATIONARY_WATER) {
            BukkitUtil.callEvent(new PlayerKillEvent(p, e.getPlayer().getKiller()));
        }
    }

    @EventHandler
    public void onKill(PlayerKillEvent e) {
        Player player = e.getPlayer();
        Player killer = (Player) e.getKiller();

        if (killer != null && killer != player) {
            getStats().addPlayerStats(killer, "Kills", 1);
            GartexAPI.getScoreBoardAPI().setScoreTab(killer, getStats().getPlayerStats(killer, "Kills"));
        }

        if (GameUtil.isSpectator(player)) {
            player.teleport(GameSettings.spectatorLoc);
            return;
        }

        GameAPI.sendDamageCause(player);
        SpectatorMode.setSpectatorMode(player);
        SUser playerTeam = SUser.getPlayerTeam(player);
        if (playerTeam != null)
            playerTeam.removePlayer(player);
        check();
    }

    @EventHandler
    public void onEndGame(final EndGameEvent e) {

        SUser winner = null;

        for (SUser sUser : SUser.getTeams().values()) {

            if (sUser.isAlive()) {

                winner = sUser;

                break;

            }

        }

        if (winner != null) {

            Player winPlayer = winner.getPlayersInTeam().iterator().next();

            e.setWinMsg("§rПобедил игрок §8- " + winPlayer.getDisplayName());

            for (Player player : winner.getPlayersInTeam()) {

                getStats().addPlayerStats(player, "Wins", 1);

                e.addWinner(player);
            }

            for (Player player : Bukkit.getOnlinePlayers()) {

                if (winner.playerInTeam(player)) {

                    player.sendTitle("§6Победа", "");

                } else {

                    player.sendTitle("§7Победил игрок", winner.getDisplayName());

                }

            }

        }
    }


    @EventHandler
    public void onRestart(RestartGameEvent e) {
        SUser.getTeams().clear();
    }

}
