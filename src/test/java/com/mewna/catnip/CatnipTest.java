/*
 * Copyright (c) 2020 amy, All rights reserved.
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

package com.mewna.catnip;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author amy
 * @since 12/11/20.
 */
public class CatnipTest {
    @Test
    public void testEd25519Verification() {
        assertDoesNotThrow(() -> {
            final var data = "{\"id\":\"787239352152883232\",\"token\":\"aW50ZXJhY3Rpb246Nzg3MjM5MzUyMTUyODgzMjMyOktYcHc0aHI5aVI5YjY1RmV6TFRuRTJ0OVJ5M0pzRm9QTVE4bm5vM21LS0ZUN3lrYzRsTG02SHJJcVJSYVJ5djZnNHRTYVpjd1pJandwSVpUR0V3OVlWSEVBYXlUbFRtRWhxeHZaaUhhWU1pSnMxc0VsOVh6V1lGSWV4a3NKSVlZ\",\"type\":1,\"version\":1}";
            final var pubkey = "48c85c48446ce9580ccc41427b5dee61d33b3b03e9219cbfadb4e815e59e8e94";
            final var sig = "d1ddb08ddc2b1bc9fa78e346c518e31381b8c43a7e993bf1ea81649e256770a4b4f5dc1647646ec2328fa1669a32e289510b53f76e687e97f69fe6c8280ac00a";
            final var ts = "1607762888";
            final var catnip = Catnip.catnip(new CatnipOptions("")
                    .publicKey(pubkey)
                    .validateToken(false));
            assertTrue(catnip.validateSignature(sig, ts, data));
        });
    }
}
