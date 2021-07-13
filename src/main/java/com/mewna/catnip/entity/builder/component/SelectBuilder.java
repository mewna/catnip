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

package com.mewna.catnip.entity.builder.component;

import com.mewna.catnip.entity.impl.message.component.SelectImpl;
import com.mewna.catnip.entity.impl.message.component.SelectImpl.SelectOptionImpl;
import com.mewna.catnip.entity.message.component.Select;
import com.mewna.catnip.entity.message.component.Select.SelectOption;
import com.mewna.catnip.entity.misc.Emoji;
import lombok.Setter;

import java.util.List;

/**
 * @author amy
 * @since 7/12/21.
 */
@Setter
public class SelectBuilder {
    private List<SelectOption> options;
    private String placeholder;
    private int minValues;
    private int maxValues;
    private boolean disabled;
    private String customId;
    
    public Select build() {
        return new SelectImpl(options, placeholder, minValues, maxValues, disabled, customId);
    }
    
    @Setter
    public static class SelectOptionBuilder {
        private String label;
        private String value;
        private String description;
        private Emoji emoji;
        private boolean isDefault;
        
        public SelectOption build() {
            return new SelectOptionImpl(label, value, description, emoji, isDefault);
        }
    }
}
