package net.gqrden.spleef;

import net.gartexapi.GartexAPI;
import net.gartexapi.gameapi.GameAPI;
import net.gqrden.spleef.game.GameInfo;
import net.gqrden.spleef.game.GameFactory;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public static Main instance;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        new GameInfo();
        Bukkit.getPluginManager().registerEvents(new GameFactory(), this);

        GameAPI.registerGame();
    }

    @Override
    public void onDisable() {
        GartexAPI.getScoreBoardAPI().removeDefaultTags();
    }
}
