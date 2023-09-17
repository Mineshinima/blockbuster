package com.mineshinima.mclib.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityUtils {
    @OnlyIn(Dist.CLIENT)
    public static GameType getGameMode(Player player) {
        PlayerInfo info = EntityUtils.getPlayerInfo(player);

        return info == null ? GameType.SURVIVAL : info.getGameMode();
    }

    @OnlyIn(Dist.CLIENT)
    public static void setGameMode(LocalPlayer player, GameType gamemode) {
        //TODO do it with packets later because executing a chat command will write messages in the chat
        player.connection.sendUnsignedCommand("gamemode " + gamemode.getName());
    }

    @OnlyIn(Dist.CLIENT)
    public static PlayerInfo getPlayerInfo(Player player) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();

        if (connection == null) {
            return null;
        }

        return connection.getPlayerInfo(player.getUUID());
    }
}
