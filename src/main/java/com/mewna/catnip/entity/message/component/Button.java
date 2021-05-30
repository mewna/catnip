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

import com.grack.nanojson.JsonObject;
import com.mewna.catnip.entity.misc.Emoji;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.misc.Emoji.UnicodeEmoji;
import lombok.Getter;

/**
 * @author amy
 * @since 5/30/21.
 */
public interface Button extends MessageComponent {
    @Override
    default MessageComponentType type() {
        return MessageComponentType.BUTTON;
    }
    
    ButtonStyle style();
    
    String label();
    
    Emoji emoji();
    
    String customId();
    
    String url();
    
    boolean disabled();
    
    @Override
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
        o.put("type", type().id());
        o.put("style", style().id());
        o.put("label", label());
        o.put("emoji", emojiObject);
        o.put("custom_id", customId());
        o.put("url", url());
        o.put("disabled", disabled());
        return o;
    }
    
    enum ButtonStyle {
        PRIMARY(1),
        SECONDARY(2),
        SUCCESS(3),
        DANGER(4),
        LINK(5),
        ;
        
        @Getter
        private final int id;
        
        ButtonStyle(final int id) {
            this.id = id;
        }
    }
}
