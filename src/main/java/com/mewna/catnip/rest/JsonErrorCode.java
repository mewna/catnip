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

package com.mewna.catnip.rest;

import com.mewna.catnip.Catnip;
import lombok.Getter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 10/13/20.
 */
@Getter
public enum JsonErrorCode {
    UNKNOWN_ERROR_CODE(-1, "UNKNOWN ERROR CODE"),
    GENERAL_ERROR(0, "General error (such as a malformed request body, amongst other things)"),
    UNKNOWN_ACCOUNT(10001, "Unknown account"),
    UNKNOWN_APPLICATION(10002, "Unknown application"),
    UNKNOWN_CHANNEL(10003, "Unknown channel"),
    UNKNOWN_GUILD(10004, "Unknown guild"),
    UNKNOWN_INTEGRATION(10005, "Unknown integration"),
    UNKNOWN_INVITE(10006, "Unknown invite"),
    UNKNOWN_MEMBER(10007, "Unknown member"),
    UNKNOWN_MESSAGE(10008, "Unknown message"),
    UNKNOWN_PERMISSION_OVERWRITE(10009, "Unknown permission overwrite"),
    UNKNOWN_PROVIDER(10010, "Unknown provider"),
    UNKNOWN_ROLE(10011, "Unknown role"),
    UNKNOWN_TOKEN(10012, "Unknown token"),
    UNKNOWN_USER(10013, "Unknown user"),
    UNKNOWN_EMOJI(10014, "Unknown emoji"),
    UNKNOWN_WEBHOOK(10015, "Unknown webhook"),
    UNKNOWN_BAN(10026, "Unknown ban"),
    UNKNOWN_SKU(10027, "Unknown SKU"),
    UNKNOWN_STORE_LISTING(10028, "Unknown Store Listing"),
    UNKNOWN_ENTITLEMENT(10029, "Unknown entitlement"),
    UNKNOWN_BUILD(10030, "Unknown build"),
    UNKNOWN_LOBBY(10031, "Unknown lobby"),
    UNKNOWN_BRANCH(10032, "Unknown branch"),
    UNKNOWN_STORE_DIRECTORY_LAYOUT(10033, "Unknown store directory layout"),
    UNKNOWN_REDISTRIBUTABLE(10036, "Unknown redistributable"),
    BOTS_CANT_USE(20001, "Bots cannot use this endpoint"),
    ONLY_BOTS_CAN_USE(20002, "Only bots can use this endpoint"),
    MESSAGE_CANT_EDIT_PUBLISH_RATELIMIT(20022, "This message cannot be edited due to announcement rate limits"),
    CHANNEL_RATE_LIMIT(20028, "The channel you are writing has hit the write rate limit"),
    MAX_GUILD_COUNT(30001, "Maximum number of guilds reached (100)"),
    MAX_FRIEND_COUNT(30002, "Maximum number of friends reached (1000)"),
    MAX_PIN_COUNT(30003, "Maximum number of pins reached for the channel (50)"),
    MAX_ROLE_COUNT(30005, "Maximum number of guild roles reached (250)"),
    MAX_WEBHOOK_COUNT(30007, "Maximum number of webhooks reached (10)"),
    MAX_REACTION_COUNT(30010, "Maximum number of reactions reached (20)"),
    MAX_CHANNEL_COUNT(30013, "Maximum number of guild channels reached (500)"),
    MAX_ATTACHMENT_COUNT(30015, "Maximum number of attachments in a message reached (10)"),
    MAX_INVITE_COUNT(30016, "Maximum number of invites reached (1000)"),
    UNAUTHORIZED(40001, "Unauthorized. Provide a valid token and try again"),
    ACCOUNT_NEEDS_VERIFICATION(40002, "You need to verify your account in order to perform this action"),
    ENTITY_TOO_LARGE(40005, "Request entity too large. Try sending something smaller in size"),
    FEATURE_SERVER_SIDE_DISABLED(40006, "This feature has been temporarily disabled server-side"),
    USER_BANNED(40007, "The user is banned from this guild"),
    MESSAGE_ALREADY_PUBLISHED(40033, "This message has already been crossposted"),
    MISSING_ACCESS(50001, "Missing access"),
    INVALID_ACCOUNT_TYPE(50002, "Invalid account type"),
    CANT_USE_IN_DMS(50003, "Cannot execute action on a DM channel"),
    GUILD_WIDGET_DISABLED(50004, "Guild widget disabled"),
    CANT_EDIT_OTHER_USERS_MESSAGE(50005, "Cannot edit a message authored by another user"),
    CANT_SEND_EMPTY_MESSAGE(50006, "Cannot send an empty message"),
    CANT_DM_USER(50007, "Cannot send messages to this user"),
    CANT_MESSAGE_VOICE_CHANNEL(50008, "Cannot send messages in a voice channel"),
    CHANNEL_VERIFICATION_TOO_HIGH(50009, "Channel verification level is too high for you to gain access"),
    OAUTH_APP_NO_BOT(50010, "OAuth2 application does not have a bot"),
    OAUTH_APP_LIMIT_REACHED(50011, "OAuth2 application limit reached"),
    INVALID_OAUTH_STATE(50012, "Invalid OAuth2 state"),
    NO_PERMISSIONS(50013, "You lack permissions to perform that action"),
    NO_AUTH_TOKEN(50014, "Invalid authentication token provided"),
    NOTE_TOO_LONG(50015, "Note was too long"),
    INVALID_DELETE_COUNT(50016, "Provided too few or too many messages to delete. Must provide at least 2 and fewer than 100 messages to delete"),
    PIN_MESSAGE_WRONG_CHANNEL(50019, "A message can only be pinned to the channel it was sent in"),
    INVITE_CODE_INVALID_OR_TAKEN(50020, "Invite code was either invalid or taken"),
    CANT_RUN_ON_SYSTEM_MESSAGE(50021, "Cannot execute action on a system message"),
    CANT_RUN_ON_CHANNEL_TYPE(50024, "Cannot execute action on this channel type"),
    INVALID_OAUTH_ACCESS_TOKEN(50025, "Invalid OAuth2 access token provided"),
    INVALID_RECIPIENTS(50033, "Invalid Recipient(s)"),
    MESSAGE_BULK_DELETE_TOO_OLD(50034, "A message provided was too old to bulk delete"),
    INVALID_FORM_BODY(50035, "Invalid form body (returned for both application/json and multipart/form-data bodies), or invalid Content-Type provided"),
    INVITE_TO_GUILD_WITHOUT_BOT(50036, "An invite was accepted to a guild the application's bot is not in"),
    INVALID_API_VERSION(50041, "Invalid API version provided"),
    CANT_OPERATE_ON_ARCHIVED_THREAD(50083, "Tried to perform an operation on an archived thread, such as editing a message or adding a user to the thread"),
    INVALID_THREAD_NOTIFICATION_SETTINGS(50084, "Invalid thread notification settings"),
    BEFORE_THREAD_CREATION_DATE(50085, "before value is earlier than the thread creation date"),
    TWO_FACTOR_AUTH_REQUIRED(60003, "Two factor is required for this operation"),
    REACTION_BLOCKED(90001, "Reaction was blocked"),
    API_RESOURCE_OVERLOADED(130000, "API resource is currently overloaded. Try again a little later"),
    ;
    
    private final int code;
    private final String message;
    
    JsonErrorCode(final int code, final String message) {
        this.code = code;
        this.message = message;
    }
    
    public static JsonErrorCode byCode(@Nonnull final Catnip catnip, @Nonnegative final int code) {
        for(final JsonErrorCode value : values()) {
            if(value.code == code) {
                return value;
            }
        }
        catnip.logAdapter().warn(
                "Unknown JSON error code {}, returning {} ({})!",
                code, UNKNOWN_ERROR_CODE.name(),
                UNKNOWN_ERROR_CODE.code());
        return UNKNOWN_ERROR_CODE;
    }
}
