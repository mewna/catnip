/*
 * Copyright (c) 2021 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.entity.message.component;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.mewna.catnip.entity.misc.Emoji;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.misc.Emoji.UnicodeEmoji;
import com.mewna.catnip.entity.partials.HasCustomId;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author amy
 * @since 7/12/21.
 */
public interface Select extends MessageComponent, HasCustomId {
    @Override
    default MessageComponentType type() {
        return MessageComponentType.SELECT;
    }
    
    @Nonnull
    List<SelectOption> options();
    
    @Nullable
    String placeholder();
    
    @Nonnegative
    int minValues();
    
    @Nonnegative
    int maxValues();
    
    boolean disabled();
    
    @Override
    default JsonObject toJson() {
        final JsonObject o = new JsonObject();
        o.put("type", type().id());
        o.put("custom_id", customId());
        o.put("options", new JsonArray(options().stream().map(SelectOption::toJson).collect(Collectors.toList())));
        o.put("placeholder", placeholder());
        o.put("min_values", minValues());
        o.put("max_values", maxValues());
        o.put("disabled", disabled());
        return o;
    }
    
    interface SelectOption {
        @Nonnull
        String label();
        
        @Nonnull
        String value();
        
        @Nullable
        String description();
        
        @Nullable
        Emoji emoji();
        
        boolean isDefault();
        
        @SuppressWarnings("ConstantConditions")
        default JsonObject toJson() {
            JsonObject emojiObject = null;
            if(emoji() != null) {
                emojiObject = new JsonObject();
                if(emoji() instanceof UnicodeEmoji) {
                    final var uni = (UnicodeEmoji) emoji();
                    emojiObject.put("name", uni.name());
                } else if(emoji() instanceof CustomEmoji) {
                    final var custom = (CustomEmoji) emoji();
                    emojiObject.put("id", custom.id());
                    emojiObject.put("name", custom.name());
                    emojiObject.put("animated", custom.animated());
                }
            }
            
            final var o = new JsonObject();
            o.put("label", label());
            o.put("value", value());
            o.put("emoji", emojiObject);
            o.put("description", description());
            o.put("default", isDefault());
            return o;
        }
    }
}
