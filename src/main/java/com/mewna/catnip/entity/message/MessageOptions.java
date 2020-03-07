/*
 * Copyright (c) 2018 amy, All rights reserved.
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

package com.mewna.catnip.entity.message;

import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.impl.message.MessageImpl;
import com.mewna.catnip.entity.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

/**
 * Options used for creating a new message.
 *
 * @author SamOphis
 * @since 10/10/2018
 */
@Getter(onMethod_ = {@CheckReturnValue, @Nullable})
@Setter(onParam_ = @Nullable, onMethod_ = @Nonnull)
@NoArgsConstructor
@Accessors(fluent = true)
@SuppressWarnings("unused")
public class MessageOptions {
    private String content;
    private Embed embed;
    
    @Setter(AccessLevel.NONE)
    private List<ImmutablePair<String, byte[]>> files;
    
    /**
     * Restricts who get mentioned by this message. This does <b>NOT</b> get added to constructed {@link Message message} instances.
     * Set to {@link EnumSet#noneOf(Class) an empty enum set} or {@link Collection#clear() clear} to disable all parsing of mentions except for whitelisted {@link #users user} and {@link #roles role} mentions.
     * Set to null to allow all mentions. This may be overwritten by {@link #users user} and {@link #roles role} mention whitelists.
     *
     * @see #roles
     * @see #users
     * @see #parseNoMentions()
     * @see #parseAllMentions()
     * @see #parseMention(MessageParseFlag)
     * @see #parseMentions(MessageParseFlag...)
     * @see #parseMentions(Collection)
     * @see #parseMentionByName(String)
     * @see #parseMentionsByName(String...)
     * @see #parseMentionsByName(Collection)
     */
    @Setter(AccessLevel.NONE)
    private EnumSet<MessageParseFlag> parseFlags;
    /**
     * A whitelist of role IDs that gets mentioned by this message. This does <b>NOT</b> get added to constructed {@link Message message} instances.
     * This has no effect if {@link MessageParseFlag#ROLES roles} is present in the {@link #parseFlags() parse} set.
     * Set to {@link Collections#emptySet() an empty set} or {@link Collection#clear() clear} to disable all parsing of role mentions.
     * Set to null to allow all roles to be mentioned by this message.
     *
     * @see #mentionedUsers()
     * @see #mentionNoUsers()
     * @see #mentionAllUsers()
     * @see #mentionUser(User)
     * @see #mentionUserById(long)
     * @see #mentionUserById(String)
     * @see #mentionUsers(User...)
     * @see #mentionUsers(Collection)
     * @see #mentionUsersById(long...)
     * @see #mentionUsersById(String...)
     * @see #mentionUsersById(Collection)
     * @see #mentionMember(Member)
     * @see #mentionMembers(Member...)
     * @see #mentionMembers(Collection)
     */
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Set<String> roles;
    /**
     * A whitelist of user IDs that gets mentioned by this message. This does <b>NOT</b> get added to constructed {@link Message message} instances.
     * This has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse} set.
     * Set to {@link Collections#emptySet() an empty set} or {@link Collection#clear() clear} to disable all parsing of user mentions.
     * Set to null to allow all users to be mentioned by this message.
     *
     * @see #mentionedRoles()
     * @see #mentionNoRoles()
     * @see #mentionAllRoles()
     * @see #mentionRole(Role)
     * @see #mentionRoleById(long)
     * @see #mentionRoleById(String)
     * @see #mentionRoles(Role...)
     * @see #mentionRoles(Collection)
     * @see #mentionRolesById(long...)
     * @see #mentionRolesById(String...)
     * @see #mentionRolesById(Collection)
     */
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Set<String> users;
    
    public MessageOptions(@Nonnull final MessageOptions options) {
        content = options.content;
        embed = options.embed;
        files = options.files;
        parseFlags = options.parseFlags;
        roles = options.roles;
        users = options.users;
    }
    
    public MessageOptions(@Nonnull final Message message) {
        content = message.content();
        final List<Embed> embeds = message.embeds();
        if (!embeds.isEmpty()) {
            embed = embeds.get(0);
        }
    }
    
    /**
     * Adds a file, used when sending messages. Files are <b>NOT</b> added to constructed {@link Message Message} instances.
     * <br><p>The name of the file/attachment is taken from {@link File#getName()}.</p>
     *
     * @param file A <b>non-null, existing, readable</b> {@link File File} instance.
     * @return Itself.
     * @see #addFile(String, File)
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions addFile(@Nonnull final File file) {
        return addFile(file.getName(), file);
    }
    
    /**
     * Adds a file, used when sending messages. Files are <b>NOT</b> added to constructed {@link Message Message} instances.
     * <br><p>This allows you to specify a custom name for the file, unlike {@link #addFile(File)}.</p>
     * @param name A <b>not-null</b> name for the file.
     * @param file A <b>not-null, existing, readable</b> {@link File File} instance.
     * @return Itself.
     * @see #addFile(File)
     * @see #addFile(String, InputStream)
     */
    @CheckReturnValue
    @Nonnull
    @SuppressWarnings("WeakerAccess")
    public MessageOptions addFile(@Nonnull final String name, @Nonnull final File file) {
        if(!file.exists()) {
            throw new IllegalArgumentException("file doesn't exist!");
        }
        if(!file.canRead()) {
            throw new IllegalArgumentException("file cannot be read!");
        }
        try {
            return addFile(name, Files.readAllBytes(file.toPath()));
        } catch(final IOException exc) {
            throw new IllegalArgumentException("cannot read data from file!", exc);
        }
    }
    
    /**
     * Adds an input stream/file, used when sending messages. Files are <b>NOT</b> added to constructed {@link Message Message} instances.
     * <br><p>This allows you to specify a custom name for the input stream data, unlike {@link #addFile(File)}.</p>
     * @param name A <b>not-null</b> name for the file.
     * @param stream A <b>not-null, readable</b> {@link InputStream InputStream}.
     * @return Itself.
     * @see #addFile(String, File)
     * @see #addFile(String, byte[])
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions addFile(@Nonnull final String name, @Nonnull final InputStream stream) {
        try {
            return addFile(name, stream.readAllBytes());
        } catch(final IOException exc) {
            throw new IllegalArgumentException("cannot read data from inputstream!", exc);
        }
    }
    
    /**
     * Adds raw data/a file, used when sending messages. Files are <b>NOT</b> added to constructed {@link Message Message} instances.
     * <br><p>This allows you to specify a custom name for the raw data, unlike {@link #addFile(File)}.</p>
     * @param name A <b>not-null</b> name for the file.
     * @param data A <b>not-null</b> byte array containing the raw data for the file.
     * @return Itself.
     * @see #addFile(String, File)
     * @see #addFile(String, InputStream)
     */
    @CheckReturnValue
    @Nonnull
    @SuppressWarnings("WeakerAccess")
    public MessageOptions addFile(@Nonnull final String name, @Nonnull final byte[] data) {
        if(files == null) {
            files = new ArrayList<>(10);
        }
        if(files.size() == 10) {
            throw new UnsupportedOperationException("maximum limit of 10 attachments!");
        }
        files.add(new ImmutablePair<>(name, data));
        return this;
    }
    
    /**
     * Checks to see whether or not this MessageOptions instance has any files attached.
     * <br><p>This should be used over {@code !files().isEmpty()} because it doesn't construct a new list for each read.</p>
     * @return True or false.
     */
    @CheckReturnValue
    public boolean hasFiles() {
        return files != null; // because checking via getter creates a new list each time.
    }
    
    /**
     * Constructs a new immutable list containing all of the raw file data. Each immutable pair contains the name and the data buffer.
     * <br><p>This method is <b>expensive!</b> It constructs a new list each time and should be used sparingly.</p>
     *
     * @return A copy of the raw file list.
     */
    @CheckReturnValue
    @Nonnull
    public List<ImmutablePair<String, byte[]>> files() {
        return hasFiles() ? List.copyOf(files) : List.of();
    }
    
    /**
     * Clears {@link #parseFlags parse flags} or sets it to an empty set to mark as mention none.
     * This may be overwritten by {@link #users user} and {@link #roles role} mention whitelists.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions parseNoMentions() {
        if(parseFlags == null) {
            parseFlags = EnumSet.noneOf(MessageParseFlag.class);
        } else {
            parseFlags.clear();
        }
        return this;
    }
    
    /**
     * Nullifies the {@link #parseFlags parse flags} set to mark as mention all.
     * This may be overwritten by {@link #users user} and {@link #roles role} mention whitelists.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions parseAllMentions() {
        parseFlags = null;
        return this;
    }
    
    /**
     * Add a restriction on who gets mentioned by this message. This does <b>NOT</b> get added to constructed {@link Message Message} instances.
     *
     * @param flags A collection of mentions to allow parsing for.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions parseMentionsByName(final Collection<String> flags) {
        for(final String id : flags) {
            //noinspection ResultOfMethodCallIgnored
            parseMention(MessageParseFlag.byName(id));
        }
        return this;
    }
    
    /**
     * Add a restriction on who gets mentioned by this message. This does <b>NOT</b> get added to constructed {@link Message Message} instances.
     *
     * @param flags An array of mentions to allow parsing for.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions parseMentionsByName(final String... flags) {
        for(final String id : flags) {
            //noinspection ResultOfMethodCallIgnored
            parseMention(MessageParseFlag.byName(id));
        }
        return this;
    }
    
    /**
     * Add a restriction on who gets mentioned by this message. This does <b>NOT</b> get added to constructed {@link Message Message} instances.
     *
     * @param flags A collection of mentions to allow parsing for.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions parseMentions(final Collection<MessageParseFlag> flags) {
        if(parseFlags == null) {
            parseFlags = EnumSet.copyOf(flags);
        } else {
            parseFlags.addAll(flags);
        }
        return this;
    }
    
    /**
     * Add a restriction on who gets mentioned by this message. This does <b>NOT</b> get added to constructed {@link Message Message} instances.
     *
     * @param flags An array of mentions to allow parsing for.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions parseMentions(final MessageParseFlag... flags) {
        if(parseFlags == null) {
            parseFlags = EnumSet.noneOf(MessageParseFlag.class);
        }
        Collections.addAll(parseFlags, flags);
        return this;
    }
    
    /**
     * Add a restriction on who gets mentioned by this message. This does <b>NOT</b> get added to constructed {@link Message Message} instances.
     *
     * @param flag A mention to allow parsing for.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions parseMentionByName(final String flag) {
        return parseMention(MessageParseFlag.byName(flag));
    }
    
    /**
     * Add a restriction on who gets mentioned by this message. This does <b>NOT</b> get added to constructed {@link Message Message} instances.
     *
     * @param flag A mention to allow parsing for.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions parseMention(final MessageParseFlag flag) {
        if(parseFlags == null) {
            parseFlags = EnumSet.of(flag);
        } else {
            parseFlags.add(flag);
        }
        return this;
    }
    
    /**
     * Clears {@link #users users} or sets it to an empty set to mark as mention none.
     * This has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionNoUsers() {
        if(users == null) {
            users = Collections.emptySet();
        } else {
            users.clear();
        }
        return this;
    }
    
    /**
     * Nullifies the {@link #users users} set to mark as mention all.
     * This has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionAllUsers() {
        users = null;
        return this;
    }
    
    /**
     * Adds users to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     *
     * @param users A collection of members to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionMembers(final Collection<Member> users) {
        if(this.users == null || this.users == Collections.EMPTY_SET) {
            this.users = new HashSet<>();
        }
        for(final Member user : users) {
            this.users.add(user.id());
        }
        return this;
    }
    
    /**
     * Adds users to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     *
     * @param users An array of members to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionMembers(final Member... users) {
        if(this.users == null || this.users == Collections.EMPTY_SET) {
            this.users = new HashSet<>();
        }
        for(final Member user : users) {
            this.users.add(user.id());
        }
        return this;
    }
    
    /**
     * Adds users to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     *
     * @param users A collection of users to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionUsers(final Collection<User> users) {
        if(this.users == null || this.users == Collections.EMPTY_SET) {
            this.users = new HashSet<>();
        }
        for(final User user : users) {
            this.users.add(user.id());
        }
        return this;
    }
    
    /**
     * Adds users to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     *
     * @param users An array of users to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionUsers(final User... users) {
        if(this.users == null || this.users == Collections.EMPTY_SET) {
            this.users = new HashSet<>();
        }
        for(final User user : users) {
            this.users.add(user.id());
        }
        return this;
    }
    
    /**
     * Adds users to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     * There is <b>NO</b> validation of if any of these strings are a proper snowflake.
     *
     * @param users A collection of user IDs to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionUsersById(final Collection<String> users) {
        if(this.users == null || this.users == Collections.EMPTY_SET) {
            this.users = new HashSet<>();
        }
        this.users.addAll(users);
        return this;
    }
    
    /**
     * Adds users to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     * There is <b>NO</b> validation of if any of these strings are a proper snowflake.
     *
     * @param users An array of user IDs to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionUsersById(final String... users) {
        if(this.users == null || this.users == Collections.EMPTY_SET) {
            this.users = new HashSet<>();
        }
        Collections.addAll(this.users, users);
        return this;
    }
    
    /**
     * Adds users to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     *
     * @param users An array of user IDs to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionUsersById(final long... users) {
        if(this.users == null || this.users == Collections.EMPTY_SET) {
            this.users = new HashSet<>();
        }
        for(final long user : users) {
            this.users.add(Long.toUnsignedString(user));
        }
        return this;
    }
    
    /**
     * Adds a user to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     *
     * @param member A member to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionMember(final Member member) {
        return mentionUserById(member.id());
    }
    
    /**
     * Adds a user to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     *
     * @param user A user to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionUser(final User user) {
        return mentionUserById(user.id());
    }
    
    /**
     * Adds a user to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     * There is <b>NO</b> validation of if the string is a proper snowflake.
     *
     * @param id A user ID to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionUserById(final String id) {
        if(users == null || users == Collections.EMPTY_SET) {
            users = new HashSet<>();
        }
        users.add(id);
        return this;
    }
    
    /**
     * Adds a user to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     *
     * @param id A user ID to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionUserById(final long id) {
        return mentionUserById(Long.toUnsignedString(id));
    }
    
    /**
     * A whitelist of mentioned users. Mentions returned from here are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#USERS users} is present in the {@link #parseFlags() parse flags} set.
     * There is <b>NO</b> validation of if any of the strings are a proper snowflake.
     *
     * @return Mutable whitelist of user IDs, or null for all.
     */
    @CheckReturnValue
    @Nullable
    public Set<String> mentionedUsers() {
        return users;
    }
    
    /**
     * Clears {@link #roles roles} or sets it to an empty set to mark as mention none.
     * This has no effect if {@link MessageParseFlag#ROLES roles} is present in the {@link #parseFlags() parse flags} set.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionNoRoles() {
        if(roles == null) {
            roles = Collections.emptySet();
        } else {
            roles.clear();
        }
        return this;
    }
    
    /**
     * Nullifies the {@link #roles roles} set to mark as mention all.
     * This has no effect if {@link MessageParseFlag#ROLES roles} is present in the {@link #parseFlags() parse flags} set.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionAllRoles() {
        roles = null;
        return this;
    }
    
    /**
     * Adds roles to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#ROLES roles} is present in the {@link #parseFlags() parse flags} set.
     *
     * @param roles A collection of roles to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionRoles(final Collection<Role> roles) {
        if(this.roles == null || this.roles == Collections.EMPTY_SET) {
            this.roles = new HashSet<>();
        }
        for(final Role role : roles) {
            this.roles.add(role.id());
        }
        return this;
    }
    
    /**
     * Adds roles to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#ROLES roles} is present in the {@link #parseFlags() parse flags} set.
     *
     * @param roles An array of roles to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionRoles(final Role... roles) {
        if(this.roles == null || this.roles == Collections.EMPTY_SET) {
            this.roles = new HashSet<>();
        }
        for(final Role role : roles) {
            this.roles.add(role.id());
        }
        return this;
    }
    
    /**
     * Adds roles to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#ROLES roles} is present in the {@link #parseFlags() parse flags} set.
     * There is <b>NO</b> validation of if any of the strings are a proper snowflake.
     *
     * @param roles A collection of role IDs to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionRolesById(final Collection<String> roles) {
        if(this.roles == null || this.roles == Collections.EMPTY_SET) {
            this.roles = new HashSet<>();
        }
        this.roles.addAll(roles);
        return this;
    }
    
    /**
     * Adds roles to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#ROLES roles} is present in the {@link #parseFlags() parse flags} set.
     * There is <b>NO</b> validation of if any of the strings are a proper snowflake.
     *
     * @param roles An array of role IDs to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionRolesById(final String... roles) {
        if(this.roles == null || this.roles == Collections.EMPTY_SET) {
            this.roles = new HashSet<>();
        }
        Collections.addAll(this.roles, roles);
        return this;
    }
    
    /**
     * Adds roles to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#ROLES roles} is present in the {@link #parseFlags() parse flags} set.
     *
     * @param roles An array of role IDs to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionRolesById(final long... roles) {
        if(this.roles == null || this.roles == Collections.EMPTY_SET) {
            this.roles = new HashSet<>();
        }
        for(final long role : roles) {
            this.roles.add(Long.toUnsignedString(role));
        }
        return this;
    }
    
    /**
     * Adds a role to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#ROLES roles} is present in the {@link #parseFlags() parse flags} set.
     *
     * @param role A role to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionRole(final Role role) {
        return mentionRoleById(role.id());
    }
    
    /**
     * Adds a role to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#ROLES roles} is present in the {@link #parseFlags() parse flags} set.
     * There is <b>NO</b> validation of if the string is a proper snowflake.
     *
     * @param id An ID for a role to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionRoleById(final String id) {
        if(roles == null || roles == Collections.EMPTY_SET) {
            roles = new HashSet<>();
        }
        roles.add(id);
        return this;
    }
    
    /**
     * Adds a role to the mention whitelist. Mentions are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#ROLES roles} is present in the {@link #parseFlags() parse flags} set.
     *
     * @param id A role ID to add.
     *
     * @return Itself.
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions mentionRoleById(final long id) {
        return mentionRoleById(Long.toUnsignedString(id));
    }
    
    /**
     * A whitelist of mentioned roles. Mentions returned from here are <b>NOT</b> added to constructed {@link Message Message} instances.
     * This list has no effect if {@link MessageParseFlag#ROLES roles} is present in the {@link #parseFlags() parse flags} set.
     * There is <b>NO</b> validation of if any of the strings are a proper snowflake.
     *
     * @return Mutable whitelist of roles IDs, or null for all.
     */
    @CheckReturnValue
    @Nullable
    public Set<String> mentionedRoles() {
        return roles;
    }
    
    /**
     * Resets this MessageOptions class back to its initial state where there is no content, no embeds or no files.
     *
     * @return Itself (but with a clean state).
     */
    @CheckReturnValue
    @Nonnull
    public MessageOptions clear() {
        content = null;
        embed = null;
        files = null;
        parseFlags = null;
        roles = null;
        users = null;
        return this;
    }
    
    /**
     * Constructs a new {@link Message Message} from the content and {@link Embed Embed} this MessageOptions class stores.
     * <br><p>Creating messages this way does <b>NOT</b> include the added files, only the content and the embed.
     * Try to pass the actual options class instead of a {@link Message Message} when sending messages, as otherwise you'll be
     * performing unnecessary operations.
     *
     * @return A new {@link Message Message} instance with the content and {@link Embed Embed} set in this class.
     */
    @CheckReturnValue
    @Nonnull
    public Message buildMessage() {
        if (embed == null && content == null) {
            throw new IllegalStateException("messages must have an embed or text content!");
        }
        final MessageImpl impl = new MessageImpl();
        impl.content(content);
        if (embed != null) {
            impl.embeds(Collections.singletonList(embed));
        } else {
            impl.embeds(Collections.emptyList());
        }
        return impl;
    }
}
