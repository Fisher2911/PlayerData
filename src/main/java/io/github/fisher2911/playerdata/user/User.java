package io.github.fisher2911.playerdata.user;

import net.minecraft.util.math.BlockPos;

import java.time.Instant;
import java.util.UUID;

public class User {

    private final UUID uuid;
    private Instant loginInstant;
    private Instant logoutInstant;
    private BlockPos logoutLocation;
    private BlockPos loginLocation;
    private BlockPos spawnLocation;

    public User(final UUID uuid,
                final Instant loginInstant,
                final Instant logoutInstant,
                final BlockPos loginLocation,
                final BlockPos logoutLocation,
                final BlockPos spawnLocation) {
        this.uuid = uuid;
        this.loginInstant = loginInstant;
        this.logoutInstant = logoutInstant;
        this.logoutLocation = logoutLocation;
        this.loginLocation = loginLocation;
        this.spawnLocation = spawnLocation;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Instant getLoginInstant() {
        return loginInstant;
    }

    public void setLoginInstant(final Instant loginInstant) {
        this.loginInstant = loginInstant;
    }

    public Instant getLogoutInstant() {
        return logoutInstant;
    }

    public void setLogoutInstant(final Instant logoutInstant) {
        this.logoutInstant = logoutInstant;
    }

    public BlockPos getLogoutLocation() {
        return logoutLocation;
    }

    public void setLogoutLocation(final BlockPos logoutLocation) {
        this.logoutLocation = logoutLocation;
    }

    public BlockPos getLoginLocation() {
        return loginLocation;
    }

    public void setLoginLocation(final BlockPos loginLocation) {
        this.loginLocation = loginLocation;
    }

    public BlockPos getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(final BlockPos spawnLocation) {
        this.spawnLocation = spawnLocation;
    }
}
