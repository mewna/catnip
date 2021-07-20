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

package com.mewna.catnip.entity.builder.command;

import com.mewna.catnip.entity.impl.interaction.command.ApplicationCommandOptionImpl;
import com.mewna.catnip.entity.interaction.command.ApplicationCommandOption;
import com.mewna.catnip.entity.interaction.command.ApplicationCommandOptionChoice;
import com.mewna.catnip.entity.interaction.command.ApplicationCommandOptionType;
import com.mewna.catnip.util.Validators;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * @author amy
 * @since 12/10/20.
 */
public class CommandOptionBuilder {
    private final List<ApplicationCommandOption> options = new LinkedList<>();
    private final List<ApplicationCommandOptionChoice<?>> choices = new LinkedList<>();
    private String name;
    private String description;
    private ApplicationCommandOptionType type;
    private boolean required;
    private boolean defaultOption;
    
    public CommandOptionBuilder name(@Nonnull final String name) {
        this.name = name;
        return this;
    }
    
    public CommandOptionBuilder description(@Nonnull final String description) {
        this.description = description;
        return this;
    }
    
    public CommandOptionBuilder type(@Nonnull final ApplicationCommandOptionType type) {
        this.type = type;
        return this;
    }
    
    public CommandOptionBuilder addOption(@Nonnull final ApplicationCommandOption option) {
        options.add(option);
        return this;
    }
    
    public CommandOptionBuilder addChoice(@Nonnull final ApplicationCommandOptionChoice<?> choice) {
        choices.add(choice);
        return this;
    }
    
    public CommandOptionBuilder required(final boolean required) {
        this.required = required;
        return this;
    }
    
    public CommandOptionBuilder defaultOption(final boolean defaultOption) {
        this.defaultOption = defaultOption;
        return this;
    }
    
    public ApplicationCommandOption build() {
        Validators.assertStringLength(name, "name", 3, 32);
        Validators.assertStringLength(description, "description", 1, 100);
        Validators.assertListSize(options, "options", 0, 25);
        Validators.assertListSize(choices, "choices", 0, 25);
        return ApplicationCommandOptionImpl.builder()
                .type(type)
                .choices(choices)
                .options(options)
                .required(required)
                .defaultOption(defaultOption)
                .description(description)
                .name(name)
                .build();
    }
}
