package vn.uiza.restapi.uiza;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import retrofit2.HttpException;
import rx.observers.TestSubscriber;
import vn.uiza.base.FakeData;
import vn.uiza.restapi.model.ErrorBody;
import vn.uiza.restapi.model.UTCTime;
import vn.uiza.restapi.model.tracking.muiza.Muiza;
import vn.uiza.restapi.model.v2.listallentity.Subtitle;
import vn.uiza.restapi.model.v3.ad.AdWrapper;
import vn.uiza.restapi.model.v3.linkplay.gettokenstreaming.ResultGetTokenStreaming;
import vn.uiza.restapi.model.v3.linkplay.gettokenstreaming.SendGetTokenStreaming;
import vn.uiza.restapi.model.v3.livestreaming.gettimestartlive.ResultTimeStartLive;
import vn.uiza.restapi.model.v3.livestreaming.getviewalivefeed.ResultGetViewALiveFeed;
import vn.uiza.restapi.model.v3.livestreaming.retrievealive.ResultRetrieveALive;
import vn.uiza.restapi.model.v3.metadata.createmetadata.ResultCreateMetadata;
import vn.uiza.restapi.model.v3.metadata.deleteanmetadata.ResultDeleteAnMetadata;
import vn.uiza.restapi.model.v3.metadata.getdetailofmetadata.ResultGetDetailOfMetadata;
import vn.uiza.restapi.model.v3.metadata.getlistmetadata.ResultGetListMetadata;
import vn.uiza.restapi.model.v3.metadata.updatemetadata.ResultUpdateMetadata;
import vn.uiza.restapi.model.v3.skin.listskin.ResultGetListSkin;
import vn.uiza.restapi.model.v3.videoondeman.listallentity.ResultListEntity;
import vn.uiza.restapi.model.v3.videoondeman.retrieveanentity.ResultRetrieveAnEntity;
import vn.uiza.restapi.model.v4.subtitle.ResultGetSubtitles;
import vn.uiza.utils.util.FileUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static vn.uiza.base.FakeData.API_VERSION;
import static vn.uiza.base.FakeData.createMetadata;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest({ TextUtils.class })
public class UZServiceTest {

    private Gson gson;
    private MockWebServer mockServer;
    private UZService uzService;

    @Before
    public void setup() {
        PowerMockito.mockStatic(TextUtils.class);
        gson = new Gson();
        mockServer = new MockWebServer();
        String mockServerBaseURL = mockServer.url(FakeData.BASE_URL).toString();
        RestClient.init(mockServerBaseURL, FakeData.TOKEN);
        uzService = RestClient.createService(UZService.class);
    }

    @After
    public void tearDown() throws IOException {
        mockServer.shutdown();
    }

    /////////////////////////////////////
    ///////////////// USER-MANAGEMENT ///
    /////////////////////////////////////
    @Test
    public void retrieveAnUser_Success() {
        String responseString =
                getJsonFromResource(FakeData.USER_MANAGEMENT_PATH, "retrieveAnUser_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        uzService.retrieveAnUser(API_VERSION, "508d46b7-afa7-45e7-8141-fdeafe32a4cc")
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        Object expect = gson.fromJson(responseString, JsonObject.class);
        Object actual = gson.toJsonTree(testSubscriber.getOnNextEvents().get(0));
        assertEquals(expect, actual);
    }

    @Test
    public void retrieveAnUser_Failed_ClientError() throws Exception {
        String error400Str = getJsonFromResource(FakeData.ERROR_PATH, "error_400.json");

        MockResponse response = new MockResponse().setResponseCode(400).setBody(error400Str);
        mockServer.enqueue(response);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        uzService.retrieveAnUser(API_VERSION, "508d46b7-afa7-45e7-8141-fdeafe32a4cc")
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        assertTrue(testSubscriber.getOnErrorEvents().get(0) instanceof HttpException);
        HttpException error = (HttpException) testSubscriber.getOnErrorEvents().get(0);
        String responseBody = error.response().errorBody().string();
        ErrorBody errorBody = gson.fromJson(responseBody, ErrorBody.class);

        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(error);
        assertEquals(errorBody.getCode(), 400);
        assertNotNull(errorBody.getData());
        assertEquals("BAD_REQUEST", errorBody.getType());
    }

    @Test
    public void retrieveAnUser_Failed_ServerError() {
        MockResponse response = new MockResponse().setResponseCode(502);
        mockServer.enqueue(response);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        uzService.retrieveAnUser(API_VERSION, "508d46b7-afa7-45e7-8141-fdeafe32a4cc")
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        assertTrue(testSubscriber.getOnErrorEvents().get(0) instanceof HttpException);
        HttpException error = (HttpException) testSubscriber.getOnErrorEvents().get(0);

        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(error);
        assertEquals(502, error.code());
        assertEquals("Server Error", error.message());
    }

    @Test
    public void updateAnUser_Success() {
        String responseString =
                getJsonFromResource(FakeData.USER_MANAGEMENT_PATH, "updateAnUser_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        uzService.updateAnUser(API_VERSION, FakeData.updateUser()).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        Object expect = gson.fromJson(responseString, JsonObject.class);
        Object actual = gson.toJsonTree(testSubscriber.getOnNextEvents().get(0));

        assertEquals(expect, actual);
    }

    /////////////////////////////////////
    ///////////////// ANALYTIC //////////
    /////////////////////////////////////
    @Test
    public void track_Success() {
        String responseString = getJsonFromResource(FakeData.ANALYTIC_PATH, "track_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        uzService.track(FakeData.uizaTracking()).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        Object expect = gson.fromJson(responseString, JsonObject.class);
        Object actual = gson.toJsonTree(testSubscriber.getOnNextEvents().get(0));

        assertEquals(expect, actual);
    }

    @Test
    public void pingHeartBeat_Success() {
        String responseString = getJsonFromResource(FakeData.ANALYTIC_PATH, "pingHeartBeat_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        uzService.pingHeartBeat("asia-southeast1-vod.uizacdn.net", "97375c91-53a7-4ce7-9fea-61a2930019f6")
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        Object expect = gson.fromJson(responseString, JsonObject.class);
        Object actual = gson.toJsonTree(testSubscriber.getOnNextEvents().get(0));

        assertEquals(expect, actual);
    }

    @Test
    public void trackMuiza_Success() {
        String responseString = getJsonFromResource(FakeData.ANALYTIC_PATH, "trackMuiza_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        uzService.trackMuiza(new ArrayList<Muiza>()).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        Object expect = gson.fromJson(responseString, JsonObject.class);
        Object actual = gson.toJsonTree(testSubscriber.getOnNextEvents().get(0));

        assertEquals(expect, actual);
    }

    /////////////////////////////////////
    ///////////////// CATEGORIZATION ////
    /////////////////////////////////////
    @Test
    public void createMetadata_Success() {
        String responseString =
                getJsonFromResource(FakeData.CATEGORIZATION_PATH, "createMetadata_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultCreateMetadata> testSubscriber = new TestSubscriber<>();
        uzService.createMetadata(API_VERSION, FakeData.createMetadata()).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultCreateMetadata expect = FakeData.resultCreateMetadata();
        ResultCreateMetadata actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(gson.toJson(expect), gson.toJson(actual));
    }

    @Test
    public void getDetailOfMetadata_Success() {
        String responseString =
                getJsonFromResource(FakeData.CATEGORIZATION_PATH, "getDetailOfMetadata_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultGetDetailOfMetadata> testSubscriber = new TestSubscriber<>();
        uzService.getDetailOfMetadata(API_VERSION, "f932aa79-852a-41f7-9adc-19935034f944")
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultGetDetailOfMetadata expect = FakeData.resultGetDetailOfMetadata();
        ResultGetDetailOfMetadata actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(gson.toJson(expect), gson.toJson(actual));
    }

    @Test
    public void getListMetadata_Success() {
        String responseString =
                getJsonFromResource(FakeData.CATEGORIZATION_PATH, "getListMetadata_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultGetListMetadata> testSubscriber = new TestSubscriber<>();
        uzService.getListMetadata(API_VERSION).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultGetListMetadata expect = FakeData.resultGetListMetadata();
        ResultGetListMetadata actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(gson.toJson(expect), gson.toJson(actual));
    }

    @Test
    public void updateMetadata_Success() {
        String responseString =
                getJsonFromResource(FakeData.CATEGORIZATION_PATH, "updateMetadata_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultUpdateMetadata> testSubscriber = new TestSubscriber<>();
        uzService.updateMetadata(API_VERSION, createMetadata()).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultUpdateMetadata expect = FakeData.resultUpdateMetadata();
        ResultUpdateMetadata actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(gson.toJson(expect), gson.toJson(actual));
    }

    @Test
    public void deleteMetadata_Success() {
        String responseString =
                getJsonFromResource(FakeData.CATEGORIZATION_PATH, "deleteAnMetadata_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultDeleteAnMetadata> testSubscriber = new TestSubscriber<>();
        uzService.deleteAnMetadata(API_VERSION, "f932aa79-852a-41f7-9adc-19935034f944")
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultDeleteAnMetadata expect = FakeData.resultDeleteMetadata();
        ResultDeleteAnMetadata actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(gson.toJson(expect), gson.toJson(actual));
    }

    /////////////////////////////////////
    ///////////////// VIDEO /////////////
    /////////////////////////////////////
    @Test
    public void getListAllEntity_Success() {
        String responseString = getJsonFromResource(FakeData.VIDEO_PATH, "getListAllEntity_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultListEntity> testSubscriber = new TestSubscriber<>();
        uzService.getListAllEntity(API_VERSION).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultListEntity expect = FakeData.resultResultListEntity();
        ResultListEntity actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(gson.toJson(expect), gson.toJson(actual));
    }

    @Test
    public void retrieveAnEntity_Success() {
        String responseString = getJsonFromResource(FakeData.VIDEO_PATH, "retrieveAnEntity_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultRetrieveAnEntity> testSubscriber = new TestSubscriber<>();
        uzService.retrieveAnEntity(API_VERSION, "8b83886e-9cc3-4eab-9258-ebb16c0c73de", "appId")
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultRetrieveAnEntity expect = FakeData.resultRetrieveAnEntity();
        ResultRetrieveAnEntity actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(gson.toJson(expect), gson.toJson(actual));
    }

    @Test
    public void searchEntity_Success() {
        String responseString = getJsonFromResource(FakeData.VIDEO_PATH, "searchEntity_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultListEntity> testSubscriber = new TestSubscriber<>();
        uzService.searchEntity(API_VERSION, "keyword").subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultListEntity expect = FakeData.resultResultListEntity();
        ResultListEntity actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(gson.toJson(expect), gson.toJson(actual));
    }

    // Ads, the test return data with 3 ads data
    @Test
    public void getCuePoint_Success() {
        String responseString = getJsonFromResource(FakeData.VIDEO_PATH, "getCuePoint_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<AdWrapper> testSubscriber = new TestSubscriber<>();
        uzService.getCuePoint(API_VERSION, "entityId", "appId").subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        AdWrapper actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(3, actual.getData().size());
    }

    @Test
    public void getSubtitles_Success() {
        String responseString = getJsonFromResource(FakeData.VIDEO_PATH, "getSubtitles_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultGetSubtitles> testSubscriber = new TestSubscriber<>();
        uzService.getSubtitles(API_VERSION, "entityId", "appId").subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultGetSubtitles actual = testSubscriber.getOnNextEvents().get(0);

        assertNotNull(actual);
        assertFalse(actual.getData().isEmpty());
        assertEquals(3, actual.getData().size());

        Subtitle subtitle0 = actual.getData().get(0);
        assertNotNull(subtitle0.getLanguage());
        assertNotNull(subtitle0.getUrl());
    }

    /////////////////////////////////////
    ///////////////// LIVE STREAMING ////
    /////////////////////////////////////
    @Test
    public void startALiveEvent_Success() {
        String responseString =
                getJsonFromResource(FakeData.LIVE_STREAMING_PATH, "startALiveEvent_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        uzService.startALiveEvent(API_VERSION, FakeData.bodyStartALiveFeed()).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        Object expect = gson.fromJson(responseString, JsonObject.class);
        Object actual = gson.toJsonTree(testSubscriber.getOnNextEvents().get(0));

        assertEquals(expect, actual);
    }

    @Test
    public void retrieveALiveEvent_Success() {
        String responseString =
                getJsonFromResource(FakeData.LIVE_STREAMING_PATH, "retrieveALiveEvent_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultRetrieveALive> testSubscriber = new TestSubscriber<>();
        uzService.retrieveALiveEvent(API_VERSION, "8b83886e-9cc3-4eab-9258-ebb16c0c73de", "AppId")
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultRetrieveALive expect = FakeData.resultRetrieveALive();
        ResultRetrieveALive actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(gson.toJson(expect), gson.toJson(actual));
    }

    @Test
    public void getViewALiveFeed_Success() {
        String responseString =
                getJsonFromResource(FakeData.LIVE_STREAMING_PATH, "getViewALiveFeed_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultGetViewALiveFeed> testSubscriber = new TestSubscriber<>();
        uzService.getViewALiveFeed(API_VERSION, "8e133d0d-5f67-45e8-8812-44b2ddfd9fe2", "AppId")
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultGetViewALiveFeed expect = FakeData.resultGetViewALiveFeed();
        ResultGetViewALiveFeed actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(gson.toJson(expect), gson.toJson(actual));
    }

    @Test
    public void getTimeStartLive_Success() {
        String responseString =
                getJsonFromResource(FakeData.LIVE_STREAMING_PATH, "getTimeStartLive_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultTimeStartLive> testSubscriber = new TestSubscriber<>();
        uzService.getTimeStartLive(API_VERSION, "8e133d0d-5f67-45e8-8812-44b2ddfd9fe2", "feedId","AppId")
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultTimeStartLive expect = FakeData.resultTimeStartLive();
        ResultTimeStartLive actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(gson.toJson(expect), gson.toJson(actual));
    }

    @Test
    public void getTokenStreaming_Success() {
        String responseString =
                getJsonFromResource(FakeData.LIVE_STREAMING_PATH, "getTokenStreaming_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultGetTokenStreaming> testSubscriber = new TestSubscriber<>();
        uzService.getTokenStreaming(API_VERSION, new SendGetTokenStreaming())
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultGetTokenStreaming expect = FakeData.resultGetTokenStreaming();
        ResultGetTokenStreaming actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(gson.toJson(expect), gson.toJson(actual));
    }

    /////////////////////////////////////
    ///////////////// SKIN //////////////
    /////////////////////////////////////

    @Test
    public void getListSkin_Success() {
        String responseString =
                getJsonFromResource(FakeData.SKIN_PATH, "getListSkin_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<ResultGetListSkin> testSubscriber = new TestSubscriber<>();
        uzService.getListSkin(API_VERSION, "Android")
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        ResultGetListSkin expect = FakeData.resultGetListSkin();
        ResultGetListSkin actual = testSubscriber.getOnNextEvents().get(0);

        assertEquals(gson.toJson(expect), gson.toJson(actual));
    }

    @Test
    public void getSkinConfig_Success() {
        String responseString =
                getJsonFromResource(FakeData.SKIN_PATH, "getSkinConfig_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        uzService.getSkinConfig(API_VERSION, "id")
                .subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        Object expect = gson.fromJson(responseString, JsonObject.class);
        Object actual = gson.toJsonTree(testSubscriber.getOnNextEvents().get(0));

        assertEquals(expect, actual);
    }

    /////////////////////////////////////
    ///////////////// OTHER /////////////
    /////////////////////////////////////

    @Test
    public void getCurrentUTCTime_Success() {
        String responseString =
                getJsonFromResource(FakeData.OTHER_PATH, "getCurrentUTCTime_success.json");
        MockResponse response = new MockResponse().setResponseCode(200).setBody(responseString);
        mockServer.enqueue(response);

        TestSubscriber<UTCTime> testSubscriber = new TestSubscriber<>();
        uzService.getCurrentUTCTime().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        UTCTime utcTime = testSubscriber.getOnNextEvents().get(0);

        assertTrue(utcTime.getCurrentDateTimeMs() > 0);
        assertNotNull(utcTime.getCurrentDateTimeStr());
    }

    private String getJsonFromResource(String path, String responseFileName) {
        URL url = Objects.requireNonNull(getClass().getClassLoader()).getResource(path + responseFileName);
        File file = new File(url.getPath());
        return FileUtils.readFile2String(file, "UTF-8");
    }
}