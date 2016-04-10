/*
 * Copyright 2014 The LolDevs team (https://github.com/loldevs)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.boreeas.riotapi.rest;

import lombok.Getter;
import net.boreeas.riotapi.com.riotgames.platform.game.BannedChampion;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Malte Schütze
 */
@Getter
public class PostMatchTeamOverview {
    private List<BannedChampion> bans = new ArrayList<>();
    private int baronKills;
    private int dragonKills;
    private int vilemawKills;
    private long dominionVictoryScore;
    private boolean firstBaron;
    private boolean firstBlood;
    private boolean firstDragon;
    private boolean firstInhibitor;
    private boolean firstRiftHerald;
    private boolean firstTower;
    private int inhibitorKills;
    private int teamId;
    private int towerKills;
    private int riftHeraldKills;
    private boolean winner;
}
