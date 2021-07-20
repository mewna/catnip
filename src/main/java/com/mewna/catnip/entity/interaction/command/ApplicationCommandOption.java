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

package com.mewna.catnip.entity.interaction.command;

import com.grack.nanojson.JsonObject;
import com.mewna.catnip.entity.partials.HasDescription;
import com.mewna.catnip.entity.partials.HasName;

import java.util.List;

/**
 * @author amy
 * @since 12/10/20.
 */
public interface ApplicationCommandOption extends HasName, HasDescription {
    ApplicationCommandOptionType type();
    
    boolean defaultOption();
    
    boolean required();
    
    List<ApplicationCommandOptionChoice<?>> choices();
    
    List<ApplicationCommandOption> options();
    
    default JsonObject toJson() {
        final var choices = choices() != null ? choices() : List.of();
        final var options = options() != null ? options() : List.of();
        final var builder = JsonObject.builder();
        builder.value("type", type().key());
        builder.value("name", name());
        builder.value("description", description());
        builder.value("default", defaultOption());
        builder.value("required", required());
        builder.value("choices", choices);
        builder.value("options", options);
        return builder.done();
    }
}
