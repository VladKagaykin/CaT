package com.example.coordsmod;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CoordsMod implements DedicatedServerModInitializer {
    private final Map<UUID, Long> lastActionBarTime = new HashMap<>();

    @Override
    public void onInitializeServer() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                checkAndSendCoords(player);
            }
        });
    }

    private void checkAndSendCoords(ServerPlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        boolean hasClockAndCompass = (mainHand.getItem() == Items.CLOCK && offHand.getItem() == Items.COMPASS) ||
                                   (mainHand.getItem() == Items.COMPASS && offHand.getItem() == Items.CLOCK);

        UUID playerId = player.getUuid();
        long currentTime = System.currentTimeMillis();

        if (hasClockAndCompass) {
            // Отправляем координаты каждые 1 секунду чтобы не спамить
            if (currentTime - lastActionBarTime.getOrDefault(playerId, 0L) > 1000) {
                int x = (int) player.getX();
                int y = (int) player.getY();
                int z = (int) player.getZ();

                String coordsString = String.format("§6Координаты: §e%d §7/ §e%d §7/ §e%d", x, y, z);
                player.sendMessage(Text.literal(coordsString), true); // true = actionbar

                lastActionBarTime.put(playerId, currentTime);
            }
        } else {
            // Если убрал предметы - убираем из мапы
            lastActionBarTime.remove(playerId);
        }
    }
}
