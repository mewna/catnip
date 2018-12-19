/*
 * Copyright (c) 2018 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.entity.store;

import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public enum SkuGenre {
    ACTION(1),
    ACTION_RPG(2),
    BRAWLER(3),
    HACK_AND_SLASH(4),
    PLATFORMER(5),
    STEALTH(6),
    SURVIVAL(7),
    ADVENTURE(8),
    ACTION_ADVENTURE(9),
    METROIDVANIA(10),
    OPEN_WORLD(11),
    PSYCHOLOGICAL_HORROR(12),
    SANDBOX(13),
    SURVIVAL_HORROR(14),
    VISUAL_NOVEL(15),
    DRIVING_RACING(16),
    VEHICULAR_COMBAT(17),
    MASSIVELY_MULTIPLAYER(18),
    MMORPG(19),
    ROLE_PLAYING(20),
    DUNGEON_CRAWLER(21),
    ROGUELIKE(22),
    SHOOTER(23),
    LIGHT_GUN(24),
    SHOOT_EM_UP(25),
    FPS(26),
    DUAL_JOYSTICK_SHOOTER(27),
    SIMULATION(28),
    FLIGHT_SIMULATOR(29),
    TRAIN_SIMULATOR(30),
    LIFE_SIMULATOR(31),
    FISHING(32),
    SPORTS(33),
    BASEBALL(34),
    BASKETBALL(35),
    BILLIARDS(36),
    BOWLING(37),
    BOXING(38),
    FOOTBALL(39),
    GOLF(40),
    HOCKEY(41),
    SKATEBOARDING_SKATING(42),
    SNOWBOARDING_SKIING(43),
    SOCCER(44),
    TRACK_FIELD(45),
    SURFING_WAKEBOARDING(46),
    WRESTLING(47),
    STRATEGY(48),
    FOUR_X(49),
    ARTILLERY(50),
    RTS(51),
    TOWER_DEFENSE(52),
    TURN_BASED_STRATEGY(53),
    WARGAME(54),
    MOBA(55),
    FIGHTING(56),
    PUZZLE(57),
    CARD_GAME(58),
    EDUCATION(59),
    FITNESS(60),
    GAMBLING(61),
    MUSIC_RHYTHM(62),
    PARTY_MINI_GAME(63),
    PINBALL(64),
    TRIVIA_BOARD_GAME(65),
    UNKNOWN(-1);
    
    @Getter
    private final int id;

    SkuGenre(final int id) {
        this.id = id;
    }
    
    @Nonnull
    @CheckReturnValue
    public static SkuGenre byId(final int id) {
        for(final SkuGenre m : values()) {
            if(m.id == id) {
                return m;
            }
        }
        //TODO add all types and throw here when docs come out
        return UNKNOWN;
    }
}
