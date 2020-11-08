package net.gqrden.spleef.game;

import net.gartexapi.gameapi.enums.GameSettings;
import net.gartexapi.gameapi.enums.TeamManager;
import net.gartexapi.gameapi.enums.TypeGame;
import net.gartexapi.gameapi.game.Game;
import net.gartexapi.global.util.ConfigManager;
import net.gartexapi.util.location.LocationUtil;
import net.gqrden.spleef.SUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class GameInfo extends Game {

    protected String getTable() {
        return "spleef_" + GameSettings.typeGame.getType();
    }

    protected Collection<? extends String> getColumns() {
        return Arrays.asList("Wins", "Deaths");
    }

    protected void loadInfo() {
        GameSettings.displayName = "Spleef";
        GameSettings.prefix = "§8[§6Spleef§8] §e";

        GameSettings.food = true;
        GameSettings.damage = false;
        GameSettings.fallDamage = false;
        GameSettings.canDropOnDeath = false;

        GameSettings.gameWorld = Bukkit.getWorlds().get(0).getName();

        GameSettings.hubs = Arrays.asList("slobby-1");

        String world = GameSettings.gameWorld;

        String path = "Worlds." + world + ".";

        ConfigManager cmSettings = new ConfigManager(new File("D:\\test\\configs\\" + GameSettings.displayName.toLowerCase(), "config.yml"));
        FileConfiguration configSettings = cmSettings.getConfig();

        GameSettings.lobbyLoc = LocationUtil.stringToLocation(configSettings.getString("Lobby"), true);

        GameSettings.spectatorLoc = LocationUtil.stringToLocation(configSettings.getString(path + "Spectator"), true);
        List<Location> teamSpawn = configSettings.getStringList(path + "Locations").stream().map(location -> LocationUtil.stringToLocation(location, true)).collect(Collectors.toList());

        GameSettings.playersInTeam = configSettings.getInt(path + "PlayersInTeam");
        GameSettings.numberOfTeams = teamSpawn.size();

        TeamManager.getTeams().clear();
        new TeamManager();

        Iterator<Location> iterator = teamSpawn.iterator();

        for (TeamManager team : TeamManager.getTeams().values()) {
            new SUser(team.getTeam(), team.getName(), team.getChatColor(), team.getColor(), team.getSubID(), iterator.next(), team.getShortName());
        }

        GameSettings.slots = GameSettings.numberOfTeams * GameSettings.playersInTeam;
        GameSettings.toStart = GameSettings.slots - (GameSettings.slots / 3);

        setSettings();
    }

    private void setSettings() {

        String world = GameSettings.gameWorld;

        String path = "Worlds." + world + ".";

        ConfigManager cmSettings = new ConfigManager(new File("D:\\test\\configs\\" + GameSettings.displayName.toLowerCase(), "config.yml"));
        FileConfiguration configSettings = cmSettings.getConfig();

        if (GameSettings.playersInTeam == 1) {
            if (TypeGame.getTypeByName(configSettings.getString(path + "gameType")) != null) {
                GameSettings.teamMode = false;
                GameSettings.typeGame = TypeGame.getTypeByName(configSettings.getString(path + "gameType"));
            }
        } else {
            if (TypeGame.getTypeByName(configSettings.getString(path + "gameType")) != null) {
                if (GameSettings.playersInTeam == 2) {
                    GameSettings.teamMode = true;
                    GameSettings.typeGame = TypeGame.DOUBLES;
                } else {
                    GameSettings.teamMode = true;
                    GameSettings.typeGame = TypeGame.getTypeByName(configSettings.getString(path + "gameType"));
                }
            }
        }
    }
}
