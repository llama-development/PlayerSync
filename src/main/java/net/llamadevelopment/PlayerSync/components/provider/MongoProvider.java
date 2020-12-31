package net.llamadevelopment.PlayerSync.components.provider;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import net.llamadevelopment.PlayerSync.PlayerSync;
import net.llamadevelopment.PlayerSync.components.utils.ItemAPI;
import net.llamadevelopment.PlayerSync.components.utils.SyncPlayer;
import org.bson.Document;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoProvider extends Provider {

    private MongoClient mongoClient;
    private MongoCollection<Document> invDB;

    public MongoProvider(PlayerSync plugin) {
        super(plugin);
    }

    @Override
    public void open(Config c) {
        mongoClient = new MongoClient(new MongoClientURI(c.getString("mongo.uri")));
        invDB = mongoClient.getDatabase(c.getString("mongo.database")).getCollection(c.getString("mongo.collection"));

        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.OFF);

    }

    @Override
    public void close() {
        mongoClient.close();
    }

    @Override
    public void savePlayer(String uuid, String invString, String ecString, String health, int food, int level, int exp) {
        CompletableFuture.runAsync(() -> {
            Document doc = invDB.find(new Document("_id", uuid)).first();
            if (doc != null) {
                Document newSave = new Document("inventory", invString)
                        .append("enderchest", ecString)
                        .append("health", health)
                        .append("food", food)
                        .append("level", level)
                        .append("exp", exp);
                Document update = new Document("$set", newSave);
                invDB.updateOne(new Document("_id", uuid), update);
            } else {
                Document newData = new Document("_id", uuid)
                        .append("inventory", invString)
                        .append("enderchest", ecString)
                        .append("health", "" + health)
                        .append("food", food)
                        .append("level", level)
                        .append("exp", exp);
                invDB.insertOne(newData);
            }

        });
    }


    @Override
    public void getPlayer(Player player, Consumer<SyncPlayer> callback) {
        CompletableFuture.runAsync(() -> {
            try {
                Document doc = invDB.find(new Document("_id", this.getPlugin().getManager().getUserID(player))).first();
                if (doc != null) {
                    callback.accept(new SyncPlayer(doc.getString("inventory"), doc.getString("enderchest"), Float.parseFloat(doc.getString("health")), doc.getInteger("food"), doc.getInteger("level"), doc.getInteger("exp")));
                } else {
                    String inv = "empty";
                    String ecInv = "empty";

                    if (player.getInventory().getContents().size() > 0) {
                        inv = this.getPlugin().getItemAPI().invToString(player.getInventory());
                    }

                    if (player.getEnderChestInventory().getContents().size() > 0) {
                        ecInv = this.getPlugin().getItemAPI().invToString(player.getEnderChestInventory());
                    }

                    savePlayer(this.getPlugin().getManager().getUserID(player), inv, ecInv, "20.0", 20, 0, 0);
                    callback.accept(new SyncPlayer(inv, ecInv, 20.0f, 20, 0, 0));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public String getName() {
        return "mongodb";
    }
}


