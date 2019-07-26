/*
 * Copyright (c) 2019 amy, All rights reserved.
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

package com.mewna.catnip.util;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author AdrianTodt
 * @since 7/26/19.
 */
public class ReentrantLockWebSocket implements WebSocket {
    private final WebSocket webSocket;
    private final ReentrantLock lock;
    
    public ReentrantLockWebSocket(final WebSocket webSocket) {
        this.webSocket = webSocket;
        lock = new ReentrantLock();
    }
    
    @Override
    public CompletableFuture<WebSocket> sendText(final CharSequence data, final boolean last) {
        lock.lock();
        try {
            return webSocket.sendText(data, last).thenApply(__ -> this);
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public CompletableFuture<WebSocket> sendBinary(final ByteBuffer data, final boolean last) {
        lock.lock();
        try {
            return webSocket.sendBinary(data, last).thenApply(__ -> this);
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public CompletableFuture<WebSocket> sendPing(final ByteBuffer message) {
        lock.lock();
        try {
            return webSocket.sendPing(message).thenApply(__ -> this);
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public CompletableFuture<WebSocket> sendPong(final ByteBuffer message) {
        lock.lock();
        try {
            return webSocket.sendPong(message).thenApply(__ -> this);
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public CompletableFuture<WebSocket> sendClose(final int statusCode, final String reason) {
        lock.lock();
        try {
            return webSocket.sendClose(statusCode, reason).thenApply(__ -> this);
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void request(final long n) {
        lock.lock();
        try {
            webSocket.request(n);
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public String getSubprotocol() {
        return webSocket.getSubprotocol();
    }
    
    @Override
    public boolean isOutputClosed() {
        return webSocket.isOutputClosed();
    }
    
    @Override
    public boolean isInputClosed() {
        return webSocket.isInputClosed();
    }
    
    @Override
    public void abort() {
        lock.lock();
        try {
            webSocket.abort();
        } finally {
            lock.unlock();
        }
    }
}