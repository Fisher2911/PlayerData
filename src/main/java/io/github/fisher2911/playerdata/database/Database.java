package io.github.fisher2911.playerdata.database;

import io.github.fisher2911.playerdata.PlayerData;
import io.github.fisher2911.playerdata.user.User;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Database {

    private static final DateTimeFormatter FORMATTER = PlayerData.FORMATTER;

    private Database() {
        this.init();
    }

    private static final Database INSTANCE = new Database();

    public static Database getInstance() {
        return INSTANCE;
    }

    private static final ExecutorService POOL = Executors.newCachedThreadPool();

    private Connection conn;

    private static final String PLAYER_TABLE_NAME = "player";
    private static final String PLAYER_UUID_COLUMN = "uuid";
    private static final String PLAYER_NAME_COLUMN = "name";

    private static final String PLAYER_LAST_LOGIN_COLUMN = "last_login";
    private static final String PLAYER_LAST_LOGOUT_COLUMN = "last_logout";

    private static final String PLAYER_LOGOUT_X_COLUMN = "logout_x";
    private static final String PLAYER_LOGOUT_Y_COLUMN = "logout_y";
    private static final String PLAYER_LOGOUT_Z_COLUMN = "logout_z";

    private static final String PLAYER_LOGIN_X_COLUMN = "login_x";
    private static final String PLAYER_LOGIN_Y_COLUMN = "login_y";
    private static final String PLAYER_LOGIN_Z_COLUMN = "login_z";

    private static final String PLAYER_RESPAWN_X_COLUMN = "respawn_x";
    private static final String PLAYER_RESPAWN_Y_COLUMN = "respawn_y";
    private static final String PLAYER_RESPAWN_Z_COLUMN = "respawn_z";

    private static final String CREATE_TABLE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS " + PLAYER_TABLE_NAME + " (" +
                    PLAYER_UUID_COLUMN + " CHAR(36), " +
                    PLAYER_NAME_COLUMN + " CHAR(20), " +

                    PLAYER_LAST_LOGIN_COLUMN + " TEXT, " +
                    PLAYER_LAST_LOGOUT_COLUMN + " TEXT, " +

                    PLAYER_LOGOUT_X_COLUMN + " INTEGER, " +
                    PLAYER_LOGOUT_Y_COLUMN + " INTEGER, " +
                    PLAYER_LOGOUT_Z_COLUMN + " INTEGER, " +

                    PLAYER_LOGIN_X_COLUMN + " INTEGER, " +
                    PLAYER_LOGIN_Y_COLUMN + " INTEGER, " +
                    PLAYER_LOGIN_Z_COLUMN + " INTEGER, " +

                    PLAYER_RESPAWN_X_COLUMN + " INTEGER, " +
                    PLAYER_RESPAWN_Y_COLUMN + " INTEGER, " +
                    PLAYER_RESPAWN_Z_COLUMN + " INTEGER, " +

                    "UNIQUE(" + PLAYER_UUID_COLUMN + "))";

    private static final String SAVE_PLAYER_STATEMENT =
            "INSERT INTO " +
                    PLAYER_TABLE_NAME + " (" +
                    PLAYER_UUID_COLUMN + ", " +
                    PLAYER_NAME_COLUMN + ", " +

                    PLAYER_LAST_LOGIN_COLUMN + ", " +
                    PLAYER_LAST_LOGOUT_COLUMN + ", " +

                    PLAYER_LOGOUT_X_COLUMN + ", " +
                    PLAYER_LOGOUT_Y_COLUMN + ", " +
                    PLAYER_LOGOUT_Z_COLUMN + ", " +

                    PLAYER_LOGIN_X_COLUMN + ", " +
                    PLAYER_LOGIN_Y_COLUMN + ", " +
                    PLAYER_LOGIN_Z_COLUMN + ", " +

                    PLAYER_RESPAWN_X_COLUMN + ", " +
                    PLAYER_RESPAWN_Y_COLUMN + ", " +
                    PLAYER_RESPAWN_Z_COLUMN + ") " +

                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +

                    "ON CONFLICT (" +
                    PLAYER_UUID_COLUMN + ") " +

                    "DO UPDATE SET " +

                    PLAYER_NAME_COLUMN + "=?, " +

                    PLAYER_LAST_LOGIN_COLUMN + "=?, " +
                    PLAYER_LAST_LOGOUT_COLUMN + "=?, " +

                    PLAYER_LOGOUT_X_COLUMN + "=?, " +
                    PLAYER_LOGOUT_Y_COLUMN + "=?, " +
                    PLAYER_LOGOUT_Z_COLUMN + "=?, " +

                    PLAYER_LOGIN_X_COLUMN + "=?, " +
                    PLAYER_LOGIN_Y_COLUMN + "=?, " +
                    PLAYER_LOGIN_Z_COLUMN + "=?, " +

                    PLAYER_RESPAWN_X_COLUMN + "=?, " +
                    PLAYER_RESPAWN_Y_COLUMN + "=?, " +
                    PLAYER_RESPAWN_Z_COLUMN + "=?";

    public static final String LOAD_PLAYER_STATEMENT =
            "SELECT " + PLAYER_LAST_LOGOUT_COLUMN + ", " +
                    PLAYER_LAST_LOGIN_COLUMN + ", " +

                    PLAYER_LOGOUT_X_COLUMN + ", " +
                    PLAYER_LOGOUT_Y_COLUMN + ", " +
                    PLAYER_LOGOUT_Z_COLUMN + ", " +

                    PLAYER_LOGIN_X_COLUMN + ", " +
                    PLAYER_LOGIN_Y_COLUMN + ", " +
                    PLAYER_LOGIN_Z_COLUMN + ", " +

                    PLAYER_RESPAWN_X_COLUMN + ", " +
                    PLAYER_RESPAWN_Y_COLUMN + ", " +
                    PLAYER_RESPAWN_Z_COLUMN + " " +
                    "FROM " + PLAYER_TABLE_NAME + " " +
                    "WHERE " +
                    PLAYER_UUID_COLUMN + "=?";

    public void init() {
        this.createTable();
    }

    private void createTable() {
        final Connection connection = this.getConnection();
        if (connection == null) {
            return;
        }

        try (final PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_STATEMENT)) {

            statement.executeUpdate();
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }

    private Connection getConnection() {
        if (this.conn != null) {
            return this.conn;
        }
        try {
            final File file = FabricLoader.getInstance().getConfigDir().resolve("playerdata").toFile();

            file.mkdirs();

            this.conn = DriverManager.getConnection("jdbc:sqlite:" +
                    file.getPath() + "\\" + "users.db");
            return conn;
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public void savePlayer(
            final ServerPlayerEntity player,
            final User user) {

        final String name = player.getGameProfile().getName();
        final BlockPos logoutLocation = user.getLogoutLocation();
        final BlockPos loginLocation = user.getLoginLocation();
        AtomicReference<BlockPos> spawnLocationReference = new AtomicReference<>(user.getSpawnLocation());

        if (spawnLocationReference.get() == null) {
            spawnLocationReference.set(player.getServerWorld().getSpawnPos());
        }

        POOL.submit(() -> {
            final Connection connection = this.getConnection();
            if (connection == null) {
                return;
            }

            try (final PreparedStatement statement = connection.prepareStatement(
                    SAVE_PLAYER_STATEMENT
            )) {

                final BlockPos spawnLocation = spawnLocationReference.get();

                final Instant loginInstant = user.getLoginInstant();
                final Instant logoutInstant = user.getLogoutInstant();

                final String loginTime = LocalDateTime.from(loginInstant.atZone(ZoneId.systemDefault())).format(FORMATTER);
                final String logoutTime = LocalDateTime.from(logoutInstant.atZone(ZoneId.systemDefault())).format(FORMATTER);

                statement.setString(1, user.getUuid().toString());
                statement.setString(2, name);

                statement.setString(3, loginTime);
                statement.setString(4, logoutTime);

                statement.setInt(5, logoutLocation.getX());
                statement.setInt(6, logoutLocation.getY());
                statement.setInt(7, logoutLocation.getZ());

                statement.setInt(8, loginLocation.getX());
                statement.setInt(9, loginLocation.getY());
                statement.setInt(10, loginLocation.getZ());

                statement.setInt(11, spawnLocation.getX());
                statement.setInt(12, spawnLocation.getY());
                statement.setInt(13, spawnLocation.getZ());

                statement.setString(14, name);

                statement.setString(15, loginTime);
                statement.setString(16, logoutTime);

                statement.setInt(17, logoutLocation.getX());
                statement.setInt(18, logoutLocation.getY());
                statement.setInt(19, logoutLocation.getZ());

                statement.setInt(20, loginLocation.getX());
                statement.setInt(21, loginLocation.getY());
                statement.setInt(22, loginLocation.getZ());

                statement.setInt(23, spawnLocation.getX());
                statement.setInt(24, spawnLocation.getY());
                statement.setInt(25, spawnLocation.getZ());

                statement.executeUpdate();

            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void load(final UUID uuid,
                     final ServerPlayerEntity player,
                     final Consumer<User> consumer) {
        POOL.submit(() -> {
            final Connection connection = this.getConnection();

            if (connection == null) {
                return;
            }

            ResultSet results = null;

            try (final PreparedStatement statement = connection.prepareStatement(LOAD_PLAYER_STATEMENT)) {

                statement.setString(1, uuid.toString());

                results = statement.executeQuery();

                if (!results.next()) {
                    consumer.accept(
                            new User(
                                    uuid,
                                    Instant.now(),
                                    Instant.now(),
                                    player.getBlockPos(),
                                    player.getBlockPos(),
                                    player.getSpawnPointPosition()
                            )
                    );

                    return;
                }

                final Instant loginInstant = LocalDateTime.parse(results.getString(PLAYER_LAST_LOGIN_COLUMN), FORMATTER).atZone(ZoneId.systemDefault()).toInstant();
                final Instant logoutInstant = LocalDateTime.parse(results.getString(PLAYER_LAST_LOGOUT_COLUMN), FORMATTER).atZone(ZoneId.systemDefault()).toInstant();

                final int logoutX = results.getInt(PLAYER_LOGOUT_X_COLUMN);
                final int logoutY = results.getInt(PLAYER_LOGOUT_Y_COLUMN);
                final int logoutZ = results.getInt(PLAYER_LOGOUT_Z_COLUMN);

                final int loginX = results.getInt(PLAYER_LOGIN_X_COLUMN);
                final int loginY = results.getInt(PLAYER_LOGIN_Y_COLUMN);
                final int loginZ = results.getInt(PLAYER_LOGIN_Z_COLUMN);

                final int respawnX = results.getInt(PLAYER_RESPAWN_X_COLUMN);
                final int respawnY = results.getInt(PLAYER_RESPAWN_Y_COLUMN);
                final int respawnZ = results.getInt(PLAYER_RESPAWN_Z_COLUMN);

                final User user = new User(
                        uuid,
                        loginInstant,
                        logoutInstant,
                        new BlockPos(loginX, loginY, loginZ),
                        new BlockPos(logoutX, logoutY, logoutZ),
                        new BlockPos(respawnX, respawnY, respawnZ)
                );
                consumer.accept(user);

            } catch (final SQLException exception) {
                exception.printStackTrace();
            } finally {
                if (results != null) {
                    try {
                        results.close();
                    } catch (final SQLException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });
    }

}
