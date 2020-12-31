package net.llamadevelopment.PlayerSync;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import lombok.Getter;
import net.llamadevelopment.PlayerSync.components.listener.PlayerListener;
import net.llamadevelopment.PlayerSync.components.provider.MongoProvider;
import net.llamadevelopment.PlayerSync.components.provider.MySQLProvider;
import net.llamadevelopment.PlayerSync.components.provider.Provider;
import net.llamadevelopment.PlayerSync.components.utils.ItemAPI;
import net.llamadevelopment.PlayerSync.components.utils.Language;
import net.llamadevelopment.PlayerSync.components.utils.Manager;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PlayerSync extends PluginBase {

    @Getter
    private Provider provider;
    @Getter
    private final Map<String, Provider> providers = new HashMap<>();
    @Getter
    private boolean sounds;
    @Getter
    private Manager manager;
    @Getter
    private final ItemAPI itemAPI = new ItemAPI();

    @Override
    public void onLoad() {
        this.registerProvider(new MongoProvider(this));
        this.registerProvider(new MySQLProvider(this));
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.updateConfigIfRequired();
        Config c = getConfig();

        try {
            Language.init(this);

            final String idMethod = c.getString("idMethod").toLowerCase();

            if (!idMethod.equals("uuid") && !idMethod.equals("name")) {
                this.getLogger().info(idMethod + " is not a valid id-method.");
                return;
            }

            this.manager = new Manager(
                    this,
                    c.getBoolean("sync.inventory"),
                    c.getBoolean("sync.enderchest"),
                    c.getBoolean("sync.health"),
                    c.getBoolean("sync.food"),
                    c.getBoolean("sync.exp"),
                    c.getInt("loadDelay"),
                    idMethod
            );

            this.sounds = c.getBoolean("sounds");

            final String prov = c.getString("provider").toLowerCase();
            if (prov.equalsIgnoreCase("ENTER_PROVIDER")) {
                this.getLogger().info("§a-+-+-+ PlayerSync +-+-+-");
                this.getLogger().info("§aThanks for downloading PlayerSync! Please choose your provider and fill out the required information in the config.yml file.");
                this.getLogger().info("§a-+-+-+ PlayerSync +-+-+-");
                return;
            }
            if (!providers.containsKey(prov)) {
                this.getLogger().info("§c" + prov + " is not a valid provider. Please check if the provider has been correctly installed or check the name defined in config.yml.");
                return;
            }
            this.provider = providers.get(prov);
            this.provider.open(getConfig());

            this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(((uuid, player) -> this.manager.savePlayer(player)));
        provider.close();
    }

    public void updateConfigIfRequired() {
        int configVersion = 2;
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
            } catch (Exception ignored) {
            }
        }
    }

    public void registerProvider(Provider provider) {
        providers.put(provider.getName(), provider);
    }
}
