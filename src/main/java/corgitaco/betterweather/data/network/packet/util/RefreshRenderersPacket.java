package corgitaco.betterweather.data.network.packet.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("InstantiationOfUtilityClass")
public class RefreshRenderersPacket {

    public static void encode(RefreshRenderersPacket packet, FriendlyByteBuf buf) {
    }

    public static RefreshRenderersPacket decode(FriendlyByteBuf buf) {
        return new RefreshRenderersPacket();
    }

    public static void handle(RefreshRenderersPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level != null && minecraft.player != null) {
                    minecraft.levelRenderer.allChanged();
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
