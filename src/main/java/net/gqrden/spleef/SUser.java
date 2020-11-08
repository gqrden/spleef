package net.gqrden.spleef;

import net.gartexapi.GartexAPI;
import net.gartexapi.nms.scoreboard.PlayerTag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SUser {


    private static final Map<String, SUser> teams = new ConcurrentHashMap<>();
    private final Location spawn;
    private String name;
    private ChatColor chatColor;
    private Color color;
    private short subID;
    private String shortName;
    private Set<String> players = new HashSet<>();

    public SUser(Player player, Location spawn) {

        this.players.add(player.getName().toLowerCase());

        this.spawn = spawn;

        addPlayer(player);

        teams.put(player.getName().toLowerCase(), this);

    }

    public SUser(String team, String name, ChatColor chatColor, Color color, short subID, Location spawn, String shortName) {

        this.name = name;

        this.chatColor = chatColor;

        this.color = color;

        this.subID = subID;

        this.spawn = spawn;

        this.shortName = shortName;

        this.players = new HashSet<>();

        teams.put(team, this);

    }

    public static Map<String, SUser> getTeams() {
        return teams;
    }

    public static SUser getPlayerTeam(Player player) {

        for (SUser SWTeam : teams.values()) {

            if (SWTeam.playerInTeam(player)) {

                return SWTeam;

            }

        }

        return null;

    }

    public String getShortName() {
        return shortName;
    }

    public void addPlayer(Player player) {

        if (player == null) {

            return;

        }

        players.add(player.getName().toLowerCase());

        spawn(player);

    }

    public void spawn(Player player) {

        if (player == null || spawn == null) {

            return;

        }

        player.teleport(spawn.clone());
    }

    public boolean isAlive() {

        return size() > 0;

    }

    public String getDisplayName() {

        return chatColor + name;

    }

    public void removePlayer(Player player) {

        players.remove(player.getName().toLowerCase());

    }

    public List<Player> getPlayersInTeam() {

        return players.stream()

                .filter(s -> Bukkit.getPlayer(s) != null)

                .map(Bukkit::getPlayer)

                .collect(Collectors.toList());

    }

    public int size() {

        return getPlayers().size();

    }

    public boolean playerInTeam(Player player) {

        return getPlayersInTeam().contains(player);

    }


    public Set<String> getPlayers() {

        return players;

    }


    public void setTags() {

        PlayerTag friendTags = GartexAPI.getScoreBoardAPI().createTag(((this.shortName == null) ? "" : ("§a§l" + this.shortName + " ")) + "§a");

        friendTags.addPlayersToTeam(getPlayersInTeam());

        friendTags.setPrefix("§a[" + shortName + "] ");

        friendTags.sendTo(getPlayersInTeam());

        for (SUser team : SUser.teams.values()) {

            if (!team.equals(this)) {

                PlayerTag enemyTags = GartexAPI.getScoreBoardAPI().createTag(((team.getShortName() == null) ? "" : ("§c§l" + team.getShortName() + " ")) + "§c");

                enemyTags.addPlayersToTeam(team.getPlayersInTeam());

                enemyTags.setPrefix("§c[" + shortName + "] ");

                enemyTags.sendTo(getPlayersInTeam());

            }

        }
    }
}
