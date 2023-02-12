/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class RecipeRewriter<C extends ClientboundPacketType> {

    protected final Protocol<C, ?, ?, ?> protocol;
    protected final Map<String, RecipeConsumer> recipeHandlers = new HashMap<>();

    protected RecipeRewriter(Protocol<C, ?, ?, ?> protocol) {
        this.protocol = protocol;
    }

    public void handleRecipeType(PacketWrapper wrapper, String type) throws Exception {
        RecipeConsumer handler = recipeHandlers.get(type);
        if (handler != null) {
            handler.accept(wrapper);
        }
    }

    public void registerDefaultHandler(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int size = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < size; i++) {
                String type = wrapper.passthrough(Type.STRING).replace("minecraft:", "");
                wrapper.passthrough(Type.STRING); // Recipe Identifier
                handleRecipeType(wrapper, type);
            }
        });
    }

    protected void rewrite(@Nullable Item item) {
        if (protocol.getItemRewriter() != null) {
            protocol.getItemRewriter().handleItemToClient(item);
        }
    }

    @FunctionalInterface
    public interface RecipeConsumer {

        void accept(PacketWrapper wrapper) throws Exception;
    }
}
