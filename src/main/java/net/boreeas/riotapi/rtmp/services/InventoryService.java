/*
 * Copyright 2014 Malte Schütze
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

package net.boreeas.riotapi.rtmp.services;

import lombok.AllArgsConstructor;
import net.boreeas.riotapi.com.riotgames.platform.catalog.champion.Champion;
import net.boreeas.riotapi.com.riotgames.platform.summoner.boost.SummonerActiveBoostDto;
import net.boreeas.riotapi.rtmp.RtmpClient;

import java.util.List;

/**
 * Created by malte on 7/15/2014.
 */
@AllArgsConstructor
public class InventoryService {
    private static final String SERVICE = "inventoryService";
    private RtmpClient client;

    public SummonerActiveBoostDto getSummonerActiveBoosts() {
        return client.sendRpcAndWait(SERVICE, "getSummonerActiveBoosts");
    }

    public List<Champion> getAvailableChampions() {
        return client.sendRpcAndWait(SERVICE, "getAvailableChampions");
    }
}
