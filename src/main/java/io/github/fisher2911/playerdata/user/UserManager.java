package io.github.fisher2911.playerdata.user;

import io.github.fisher2911.playerdata.database.Database;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

    private final Database database = Database.getInstance();

    private final Map<UUID, User> userMap = new ConcurrentHashMap<>() {
    };

    public void saveUser(
            final ServerPlayerEntity player,
            final UUID uuid) {
        final User user = this.userMap.get(uuid);

        if (user == null) {
            return;
        }

        user.setLogoutLocation(player.getBlockPos());
        user.setLogoutInstant(Instant.now());

        final BlockPos spawnPos = player.getSpawnPointPosition();

        if (spawnPos != null) {
            user.setSpawnLocation(spawnPos);
        }
        this.database.savePlayer(player, user);
        this.userMap.remove(uuid);
    }

    public void loadUser(final UUID uuid, final ServerPlayerEntity player) {
        this.database.load(uuid, player, user -> {
            user.setLoginInstant(Instant.now());
            user.setLoginLocation(player.getBlockPos());
            this.userMap.put(uuid, user);
        });

    }

    public @Nullable User getUser(final UUID uuid) {
        return this.userMap.get(uuid);
    }

}
