package net.llamadevelopment.PlayerSync;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import net.llamadevelopment.PlayerSync.listener.PlayerListener;
import net.llamadevelopment.PlayerSync.provider.MongoProvider;
import net.llamadevelopment.PlayerSync.provider.MySQLProvider;
import net.llamadevelopment.PlayerSync.provider.Provider;
import net.llamadevelopment.PlayerSync.utils.Language;
import net.llamadevelopment.PlayerSync.utils.Manager;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PlayerSync extends PluginBase {

    private final static int configVersion = 2;

    private static PlayerSync instance;
    public static Provider provider;
    public static Map<String, Provider> providers = new HashMap<>();
    public static boolean sounds;

    @Override
    public void onLoad() {
        instance = this;
        registerProvider(new MongoProvider());
        registerProvider(new MySQLProvider());
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        updateConfigIfRequired();
        Config c = getConfig();
        try {
            Language.init();
            Manager.inventory = c.getBoolean("sync.inventory");
            Manager.enderchest = c.getBoolean("sync.enderchest");
            Manager.health = c.getBoolean("sync.health");
            Manager.food = c.getBoolean("sync.food");
            Manager.exp = c.getBoolean("sync.exp");

            String idMethod = c.getString("idMethod");
            sounds = c.getBoolean("sounds");
            switch (idMethod.toLowerCase()) {
                case "name":
                    Manager.idMethod = "name";
                    break;
                case "uuid":
                    Manager.idMethod = "uuid";
                    break;
                default:
                    System.out.println(idMethod + " is not a valid id-method.");
                    return;
            }
            String prov = c.getString("provider").toLowerCase();
            if (prov.equalsIgnoreCase("ENTER_PROVIDER")) {
                getLogger().info("§a-+-+-+ PlayerSync +-+-+-");
                getLogger().info("§aThanks for downloading PlayerSync! Please choose your provider and fill out the required information in the config.yml file.");
                getLogger().info("§a-+-+-+ PlayerSync +-+-+-");
                return;
            }
            if (!providers.containsKey(prov)) {
                getLogger().info("§c" + prov + " is not a valid provider. Please check if the provider has been correctly installed or check the name defined in config.yml.");
                return;
            }
            provider = providers.get(prov);
            provider.open(getConfig());

            Manager.loadDelay = c.getInt("loadDelay");
            getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(((uuid, player) -> Manager.savePlayer(player)));
    }

    public static PlayerSync getInstance() {
        return instance;
    }

    public void updateConfigIfRequired() {
        if (!getConfig().exists("version") || getConfig().getInt("version") != configVersion) {
            getConfig().set("version", configVersion);
            Config c = getConfig();
            try {
                Files.delete(Paths.get(getDataFolder() + "/config.yml"));
                saveDefaultConfig();
                reloadConfig();
                Config newConf = getConfig();
                c.getAll().forEach(newConf::set);
                newConf.save();
                System.out.println("The config has been updated to version " + configVersion + ".");
            } catch (Exception ignored) { }
        }
    }

    public void registerProvider(Provider provider) {
        providers.put(provider.getName(), provider);
    }
}
