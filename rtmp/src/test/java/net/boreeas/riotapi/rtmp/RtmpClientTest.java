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

package net.boreeas.riotapi.rtmp;

import junit.framework.TestCase;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import net.boreeas.riotapi.Shard;
import net.boreeas.riotapi.com.riotgames.platform.game.GameMode;
import net.boreeas.riotapi.com.riotgames.platform.game.QueueType;
import net.boreeas.riotapi.com.riotgames.platform.login.impl.ClientVersionMismatchException;
import net.boreeas.riotapi.com.riotgames.platform.matchmaking.GameQueueConfig;
import net.boreeas.riotapi.com.riotgames.platform.matchmaking.MatchMakerParams;
import net.boreeas.riotapi.com.riotgames.platform.matchmaking.SearchingForMatchNotification;
import net.boreeas.riotapi.com.riotgames.platform.summoner.SummonerSkillLevel;
import net.boreeas.riotapi.com.riotgames.platform.summoner.runes.SummonerRuneInventory;
import net.boreeas.riotapi.com.riotgames.platform.summoner.spellbook.RunePageBook;
import net.boreeas.riotapi.com.riotgames.team.dto.Team;
import net.boreeas.riotapi.constants.Season;
import net.boreeas.riotapi.loginqueue.LoginQueue;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.Ignore;

@Log4j
@Ignore
public class RtmpClientTest extends TestCase {

    private static Properties testConfig = new Properties();;
    private static Shard shard = Shard.EUW;
    private static RtmpClient client;
    private static long summonerId;
    private static long accountId;
    private static AsyncMessageReceiver asyncMessageReceiver;

    @Log4j
    private static class AsyncMessageReceiver implements Consumer<AsyncMessageEvent> {

        private CountDownLatch asynMessageReceived = new CountDownLatch(1);

        public void accept(AsyncMessageEvent event) {
            asynMessageReceived.countDown();
        }
    }

    static {
        staticSetup(); // Because @BeforeClass apparently isn't working
    }

    @SneakyThrows
    public static void staticSetup() {

        testConfig.load(new InputStreamReader(new FileInputStream("testconfig.properties")));

        client = new DefaultRtmpClient(shard.prodUrl, Shard.RTMPS_PORT, true);
        //client.setDebug(true);
        asyncMessageReceiver = new AsyncMessageReceiver();
        client.addAsyncChannelListener(asyncMessageReceiver);
        //client = new DefaultRtmpClient("localhost", 2099, true);

        String user = testConfig.getProperty("user");
        String pass = testConfig.getProperty("pass");
        String authKey = new LoginQueue(shard).waitInQueueBlocking(user, pass);

        try {
            client.connect();
            client.authenticate(user, pass, authKey, "5.5.15_03_09_13_59");
        } catch (ClientVersionMismatchException ex) {
            log.info("Reconnecting with version " + ex.getCurrentVersion());
            client.authenticate(user, pass, new LoginQueue(shard).waitInQueue(user, pass).await(), ex.getCurrentVersion());
        }

        summonerId = client.getLoginDataPacket().getAllSummonerData().getSummoner().getSumId();
        accountId = client.getLoginDataPacket().getAllSummonerData().getSummoner().getAcctId();
    }

    public void testAsyncMessageReceived() throws InterruptedException {
        if (!asyncMessageReceiver.asynMessageReceived.await(2, TimeUnit.SECONDS)) {
            fail("No async message received");
        }
    }

    public void testGetAccountState() {
        client.accountService.getAccountState();
    }

    public void testGetMasteryBook() {
        client.bookService.getMasteryBook(summonerId);
    }

    public void testGetSpellBook() {
        RunePageBook book = client.bookService.getSpellBook(summonerId);
    }

    public void testSaveMasteryBook() {
        client.bookService.saveMasteryBook(client.bookService.getMasteryBook(summonerId));
    }

    public void testSaveSpellbook() {
        client.bookService.saveSpellBook(client.bookService.getSpellBook(summonerId));
    }

    public void testSelectDefaultSpellBookPage() {
        client.bookService.selectDefaultSpellBookPage(client.bookService.getSpellBook(summonerId).getBookPages().get(0));
    }

    public void testGetAvailableChampions() {
        client.inventoryService.getAvailableChampions();
    }

    public void testGetSummonerActiveBoosts() {
        client.inventoryService.getSummonerActiveBoosts();
    }

    public void testGetPointsBalance() {
        client.lcdsRerollService.getPointsBalance();
    }

    public void testGetAllLeaguesForPlayer() {
        client.leaguesServiceProxy.getAllLeaguesForPlayer(summonerId);
    }

    public void testGetAllMyLeagues() {
        client.leaguesServiceProxy.getAllMyLeagues();
    }

    public void testGetChallengerLeague() {
        client.leaguesServiceProxy.getChallengerLeague(QueueType.RANKED_SOLO_5x5);
    }

    public void testGetMyLeaguePositions() {
        client.leaguesServiceProxy.getMyLeaguePositions();
    }

    public void testGetLeaguesForTeam() {
        client.leaguesServiceProxy.getLeaguesForTeam(testConfig.getProperty("lookupTeamname"));
    }

    public void testGetStoreUrl() {
        client.loginService.getStoreUrl();
    }

    public void testLcdsHeartBeat() {
        String s = client.loginService.performLcdsHeartBeat(client.getLoginDataPacket().getAllSummonerData().getSummoner().getAcctId(), client.getSession().getToken(), 1);
        System.out.println(s);
    }

    public void testGetAvailableQueues() {
        client.matchmakerService.getAvailableQueues();
    }

    public void testGetQueueInfo() {
        client.matchmakerService.getQueueInfo(client.matchmakerService.getAvailableQueues().get(0).getId());
    }

    public void testQueueAttachDetach() {
        MatchMakerParams params = new MatchMakerParams();
        List<Long> ids = client.matchmakerService.getAvailableQueues().parallelStream().map(GameQueueConfig::getId).collect(Collectors.toList());
        params.setQueueIds(ids);
        params.setBotDifficulty("BEGINNER");

        SearchingForMatchNotification search = client.matchmakerService.attachToQueue(params);
//        assertTrue(client.matchmakerService.cancelFromQueueIfPossible(client.getLoginDataPacket().getAllSummonerData().getSummoner().getAcctId()));
    }

    public void testPurgeFromQueues() {
        MatchMakerParams params = new MatchMakerParams();
        List<Long> ids = client.matchmakerService.getAvailableQueues().parallelStream().map(GameQueueConfig::getId).collect(Collectors.toList());
        params.setQueueIds(ids);
        params.setBotDifficulty("BEGINNER");

        SearchingForMatchNotification search = client.matchmakerService.attachToQueue(params);
        Object o = client.matchmakerService.purgeFromQueues();
        System.out.println("Inspect: matchmakerService.purgeFromQueues");
        System.out.println(o);
    }

    public void testProcessEloQuestionaire() {
        System.out.println("Inspect: playerStatsService.processEloQuestionaire");
        Object obj = client.playerStatsService.processEloQuestionaire(SummonerSkillLevel.BEGINNER);
        System.out.println(obj);
    }

    public void testGetAggregatedStats() {
        client.playerStatsService.getAggregatedStats(accountId, GameMode.CLASSIC, Season.SEASON2014);
    }

    public void testGetRecentGames() {
        client.playerStatsService.getRecentGames(accountId);
    }

    public void testGetTeamAggregatedStats() {
        Team team = client.summonerTeamService.findTeamByName(testConfig.getProperty("lookupTeamname"));
        client.playerStatsService.getTeamAggregatedStats(team.getTeamId());
    }

    public void testGetSummonerIconInventory() {
        client.summonerIconService.getSummonerIconInventory(summonerId);
    }

    public void testGetSummonerRuneInventory() {
        SummonerRuneInventory runes = client.summonerRuneService.getSummonerRuneInventory(summonerId);
        System.out.println("Inspect: SummonerRuneInventory.json");
        System.out.println(runes.getSummonerRunesJson());
    }

    public void testGetAllPublicSummonerDataByAccount() {
        client.summonerService.getAllPublicSummonerDataByAccount(accountId);
    }

    public void testGetAllSummonerDataByAccount() {
        client.summonerService.getAllSummonerDataByAccount(accountId);
    }

    public void testGetSummonerByName() {
        client.summonerService.getSummonerByName(client.getLoginDataPacket().getAllSummonerData().getSummoner().getName());
    }

    public void testGetSummonerInternalNameByName() {
        client.summonerService.getSummonerInternalNameByName(client.getLoginDataPacket().getAllSummonerData().getSummoner().getName());
    }

    public void testUpdateProfileIcon() {
        // IconInventory is empty
        //client.summonerService.updateProfileIconId(client.summonerIconService.getSummonerIconInventory(summonerId).getSummonerIcons().get(0).getIconId());
    }

    public void testFindTeamByName() {
        client.summonerTeamService.findTeamByName(testConfig.getProperty("lookupTeamname"));
    }

    public void testFindTeamByTeamId() {
        client.summonerTeamService.findTeamById(client.summonerTeamService.findTeamByName(testConfig.getProperty("lookupTeamname")).getTeamId());
    }

    public void testIsNameValidAndAvailable() {
        client.summonerTeamService.isNameValidAndAvailable("Test Team");
    }

    public void testIsTagValidAndAvailable() {
        client.summonerTeamService.isTagValidAndAvailable("TEST");
    }


    /*
    public void testStartCustomGame() throws Exception {
        PracticeGameConfig config = new PracticeGameConfig();
        config.setAllowSpectators("ALL");
        config.setPassbackUrl(null);
        config.setRegion("");
        config.setGameName("My super secret test game " + UUID.randomUUID());
        config.setPassbackDataPacket(null);
        config.setGamePassword("yoyoyo");
        config.setGameTypeConfig(1);
        config.setGameMap(GameMap.SUMMONERS_RIFT_NEW);
        config.setGameMode(GameMode.CLASSIC.name());
        config.setMaxNumPlayers(10);
        Game practiceGame = client.gameService.createPracticeGame(config);

        client.gameService.startChampionSelect(practiceGame.getId(), 2);
    }
    */
    /*
    CountDownLatch latch = new CountDownLatch(1);
    public void testAcceptExternGameInvite() throws InterruptedException {
        client.addAsyncChannelListener(this::callback, client.getClientNewsChannel());
        latch.await();
    }

    private void callback(AsyncMessageEvent evt) {
        if (evt.getBody() instanceof InvitationRequest) {
            System.out.println("got invite, accept");
            //LobbyStatus accept = client.lcdsGameInvitationService.accept(((InvitationRequest) evt.getBody()).getInvitationId());
            client.sendRpcToDefault(LcdsGameInvitationService.SERVICE, "accept", ((InvitationRequest) evt.getBody()).getInvitationId());
            System.out.println("" + client.sendRpcAndWait(LcdsGameInvitationService.SERVICE, "checkLobbyStatus"));
            System.out.println(client.lcdsGameInvitationService.checkLobbyStatus());
            System.out.println("Status done");
            latch.countDown();
        }
    }
    */
}