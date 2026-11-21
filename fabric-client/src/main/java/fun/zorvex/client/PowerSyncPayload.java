package fun.zorvex.client;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PowerSyncPayload(String power) implements CustomPacketPayload {
    public static final Type<PowerSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("zorvexpowers", "power"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PowerSyncPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PowerSyncPayload::power, PowerSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
