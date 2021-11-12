package io.github.fisher2911.playerdata;

import net.fabricmc.api.ModInitializer;

import java.time.format.DateTimeFormatter;

public class PlayerData implements ModInitializer {

    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(
                            "yy:MM:dd [HH:mm:ss]"
                    );

    @Override
    public void onInitialize() {
    }
}
