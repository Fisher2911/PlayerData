package io.github.fisher2911.playerdata.mixin;

import io.github.fisher2911.playerdata.user.UserManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    private final UserManager userManager = new UserManager();

    @Inject(at = @At("TAIL"), method = "remove")
    private void remove(final ServerPlayerEntity player, final CallbackInfo ci) {
        userManager.saveUser(player, player.getUuid());
    }

    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    private void onPlayerJoin(
            final ClientConnection connection,
            final ServerPlayerEntity player,
            final CallbackInfo ci) {
        try {
            this.userManager.loadUser(player.getUuid(), player);
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }
}
