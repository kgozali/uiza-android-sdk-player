package uizacoresdk.floatview;

/**
 * Created by www.muathu@gmail.com on 14/1/2019.
 */

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import uizacoresdk.R;
import uizacoresdk.util.TmpParamData;
import uizacoresdk.util.UizaData;
import uizacoresdk.util.UizaTrackingUtil;
import uizacoresdk.util.UizaUtil;
import uizacoresdk.view.VideoViewBase;
import vn.uiza.core.common.Constants;
import vn.uiza.core.exception.UizaException;
import vn.uiza.models.PlaybackInfo;
import vn.uiza.models.Subtitle;
import vn.uiza.models.tracking.Muiza;
import vn.uiza.models.tracking.UizaTracking;
import vn.uiza.models.tracking.UizaTrackingCCU;
import vn.uiza.restapi.RxBinder;
import vn.uiza.restapi.UizaClientFactory;
import vn.uiza.restapi.UizaHeartBeatService;
import vn.uiza.restapi.UizaTrackingService;
import vn.uiza.restapi.restclient.UizaTrackingClient;
import vn.uiza.utils.AppUtils;
import vn.uiza.utils.DateUtils;
import vn.uiza.utils.DeviceUtils;
import vn.uiza.utils.UIUtils;

public class FloatUizaVideoView extends VideoViewBase {
    private static final String M3U8_EXTENSION = ".m3u8";
    private static final String MPD_EXTENSION = ".mpd";
    private static final String PLAY_THROUGH_100 = "100";
    private static final String PLAY_THROUGH_75 = "75";
    private static final String PLAY_THROUGH_50 = "50";
    private static final String PLAY_THROUGH_25 = "25";
    private static final int INTERVAL_TRACK_CCU = 3000;

    private PlayerView playerView;
    private FloatUizaPlayerManagerAbs fuzUizaPlayerManager;
    private ProgressBar progressBar;
    private String cdnHost;
    private String uuid;
    private String linkPlay;
    private boolean isLivestream;
    private long contentPosition;
    private boolean isOnStateReadyFirst;
    private boolean isInitCustomLinkPlay;
    private int progressBarColor = Color.WHITE;
    private long timestampRebufferStart;
    private int oldPercent = -1;
    private boolean isTracked25;
    private boolean isTracked50;
    private boolean isTracked75;
    private boolean isTracked100;
    private VideoViewBase.ProgressListener progressListener;
    private boolean isTrackingMuiza;

    private Handler handler = new Handler();

    public FloatUizaVideoView(Context context) {
        super(context);
    }

    public FloatUizaVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatUizaVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FloatUizaVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onCreateView() {
        inflate(getContext(), R.layout.float_uiza_video, this);
        progressBar = findViewById(R.id.pb);
        UIUtils.setColorProgressBar(progressBar, progressBarColor);
        playerView = findViewById(R.id.player_view);
    }

    public void setProgressBarColor(int progressBarColor) {
        if (progressBar != null) {
            this.progressBarColor = progressBarColor;
            UIUtils.setColorProgressBar(progressBar, progressBarColor);
        }
    }

    //=============================================================================================================START CONFIG

    @Override
    public long getCurrentPosition() {
        return (getPlayer() == null) ? 0 : getPlayer().getCurrentPosition();
    }

    public long getContentBufferedPosition() {
        return (getPlayer() == null) ? 0 : getPlayer().getContentBufferedPosition();
    }

    @Override
    public void seekTo(long position) {
        if (fuzUizaPlayerManager != null) {
            fuzUizaPlayerManager.seekTo(position);
        }
    }

    //return true if toggleResume
    //return false if togglePause
    protected boolean togglePauseResume() {
        return (fuzUizaPlayerManager != null) && fuzUizaPlayerManager.togglePauseResume();
    }

    @Override
    public void pause() {
        if (fuzUizaPlayerManager != null) {
            fuzUizaPlayerManager.pauseVideo();
        }
    }

    @Override
    public void resume() {
        if (fuzUizaPlayerManager != null) {
            fuzUizaPlayerManager.resumeVideo();
        }
    }

    @Override
    public int getVideoWidth() {
        return (fuzUizaPlayerManager == null) ? 0 : fuzUizaPlayerManager.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return (fuzUizaPlayerManager == null) ? 0 : fuzUizaPlayerManager.getVideoHeight();
    }

    @Override
    public SimpleExoPlayer getPlayer() {
        return (fuzUizaPlayerManager == null) ? null : fuzUizaPlayerManager.getPlayer();
    }

    private void releasePlayerManager() {
        if (fuzUizaPlayerManager != null) {
            fuzUizaPlayerManager.release();
        }
    }

    @Override
    public boolean play(@NonNull PlaybackInfo playback) {
        init(playback.getLinkPlay(), "", "", playback.isLive(), 0, false, Color.WHITE, null);
        return true;
    }

    /**
     * @param entityId : String
     * @param isLive:  boolean
     * @return boolean
     * @deprecated : Not call PlaybackInfo from entity in this class
     */
    @Override
    public boolean play(@NonNull String entityId, boolean isLive) {
        return false;
    }

    //=============================================================================================================END CONFIG

    //=============================================================================================================START VIEW
    public PlayerView getPlayerView() {
        return playerView;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }


    protected void onVideoSizeChanged(int width, int height) {
        if (callback != null) {
            callback.onVideoSizeChanged(width, height);
        }
    }

    protected void hideProgress() {
        UIUtils.hideProgressBar(progressBar);
    }

    protected void showProgress() {
        UIUtils.showProgressBar(progressBar);
    }
    //=============================================================================================================END VIEW

    public void init(String linkPlay, String cdnHost, String uuid, boolean isLivestream, long contentPosition, boolean isInitCustomLinkPlay, int progressBarColor, Callback callback) {
        if (TextUtils.isEmpty(linkPlay)) {
            Timber.e("init failed: linkPlay == null || linkPlay.isEmpty()");
            return;
        }
        showProgress();
        this.linkPlay = linkPlay;
        this.cdnHost = cdnHost;
        this.uuid = uuid;
        this.isLivestream = isLivestream;
        this.contentPosition = contentPosition;
        this.isInitCustomLinkPlay = isInitCustomLinkPlay;
        this.progressBarColor = progressBarColor;
        UIUtils.setColorProgressBar(progressBar, this.progressBarColor);
        isOnStateReadyFirst = false;
        Timber.d("init linkPlay: %s, isLivestream: %s, isInitCustomLinkPlay: %s", linkPlay, isLivestream, isInitCustomLinkPlay);
        this.callback = callback;
        releasePlayerManager();
        checkToSetUp();
        //track event eventype display
        if (!UizaTrackingUtil.isTrackedEventTypeDisplay(getContext())) {
            trackUiza(UizaData.getInstance().createTrackingInput(getContext(), Constants.EVENT_TYPE_DISPLAY),
                    () -> UizaTrackingUtil.setTrackingDoneWithEventTypeDisplay(getContext(), true));
        }

        //track event plays_requested
        if (!UizaTrackingUtil.isTrackedEventTypePlaysRequested(getContext())) {
            trackUiza(UizaData.getInstance()
                            .createTrackingInput(getContext(), Constants.EVENT_TYPE_PLAYS_REQUESTED),
                    () -> UizaTrackingUtil.setTrackingDoneWithEventTypePlaysRequested(getContext(), true));
        }
    }

    private void checkToSetUp() {
        initData(linkPlay, null, null, AppUtils.isAdsDependencyAvailable());
        onResume();
    }

    public void initData(String linkPlay, String urlIMAAd, List<Subtitle> subtitleList, boolean includeAd) {
        if (includeAd) {
            fuzUizaPlayerManager =
                    new FloatUizaPlayerManager(this, linkPlay, urlIMAAd, subtitleList);
            fuzUizaPlayerManager.setProgressListener(new VideoViewBase.ProgressListener() {
                @Override
                public void onAdEnded() {
                }

                @Override
                public void onAdProgress(int s, int duration, int percent) {
                    if (progressListener != null) {
                        progressListener.onAdProgress(s, duration, percent);
                    }
                }

                @Override
                public void onVideoProgress(long currentMls, int s, long duration, int percent) {
                    TmpParamData.getInstance().setPlayerPlayheadTime(s);
                    trackProgress(s, percent);
                    callAPITrackMuiza(s);
                    if (progressListener != null) {
                        progressListener.onVideoProgress(currentMls, s, duration, percent);
                    }
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                }

                @Override
                public void onBufferProgress(long bufferedPosition, int bufferedPercentage, long duration) {
                }
            });
        } else {
            fuzUizaPlayerManager =
                    new FloatUizaNoAdsPlayerManager(this, linkPlay,
                            subtitleList);
        }
    }

    //=============================================================================================================START LIFE CIRCLE
    public void onDestroy() {
        releasePlayerManager();
        cdnHost = null;
    }

    public void onResume() {
        if (fuzUizaPlayerManager != null) {
            fuzUizaPlayerManager.init(isLivestream, contentPosition);
        }
    }

    public void onPause() {
        if (fuzUizaPlayerManager != null) {
            fuzUizaPlayerManager.reset();
        }
    }

    //=============================================================================================================END LIFE CIRCLE
    //=============================================================================================================START API
//    private void callAPIGetTokenStreaming(final String entityId, final CallbackGetLinkPlay callbackGetLinkPlay) {
//        UZService service = UizaClientFactory.getUizaService();
//        SendGetTokenStreaming sendGetTokenStreaming = new SendGetTokenStreaming();
//        sendGetTokenStreaming.setAppId(UizaData.getInstance().getAppId());
//        sendGetTokenStreaming.setEntityId(entityId);
//        sendGetTokenStreaming.setContentType(SendGetTokenStreaming.STREAM);
//        RxBinder.bind(service.getTokenStreaming(UizaData.getInstance().getAPIVersion(), sendGetTokenStreaming),
//                result -> {
//                    if (result == null || result.getData() == null || result.getData().getToken() == null || result.getData().getToken().isEmpty()) {
//                        callbackGetLinkPlay.onSuccess(null);
//                        return;
//                    }
//                    String tokenStreaming = result.getData().getToken();
//                    Timber.d("tokenStreaming %s", tokenStreaming);
//                    callAPIGetLinkPlay(entityId, tokenStreaming, callbackGetLinkPlay);
//                }, throwable -> callbackGetLinkPlay.onSuccess(null));
//    }

//    private void callAPIGetLinkPlay(String entityId, String tokenStreaming, final CallbackGetLinkPlay callbackGetLinkPlay) {
//        if (tokenStreaming == null || tokenStreaming.isEmpty()) {
//            callbackGetLinkPlay.onSuccess(null);
//            return;
//        }
//        UizaClientFactory.getLinkPlayClient().addAuthorization(tokenStreaming);
//        UizaLinkPlayService service = UizaClientFactory.getLinkPlayService();
//        String typeContent = SendGetTokenStreaming.STREAM;
//        RxBinder.bind(service.getLinkPlay(appId, entityId, typeContent), result -> {
//            Timber.d("getLinkPlayVOD onSuccess");
//            checkToSetUpResource(result, callbackGetLinkPlay);
//        }, throwable -> callbackGetLinkPlay.onSuccess(null));
//    }

    private void checkToSetUpResource(CallbackGetLinkPlay callbackGetLinkPlay) {
        if (UizaData.getInstance().getPlaybackInfo() != null) {
            List<String> listLinkPlay = new ArrayList<>();
            PlaybackInfo info = UizaData.getInstance().getPlaybackInfo();
            List<String> urlList = info.getUrls();
            if (isLivestream) {
                // Bat buoc dung linkplay m3u8 cho nay, do bug cua system
                for (String url : urlList) {
                    if (url.toLowerCase().endsWith(M3U8_EXTENSION)) {
                        listLinkPlay.add(url);
                    }
                }
            } else {
                for (String url : urlList) {
                    if (url.toLowerCase().endsWith(MPD_EXTENSION)) {
                        listLinkPlay.add(url);
                    }
                }
                for (String url : urlList) {
                    if (url.toLowerCase().endsWith(M3U8_EXTENSION)) {
                        listLinkPlay.add(url);
                    }
                }
            }

            if (listLinkPlay.isEmpty()) {
                Timber.e("checkToSetUpResource listLinkPlay == null || listLinkPlay.isEmpty()");
                callbackGetLinkPlay.onSuccess(null);
                return;
            }
            String lp = listLinkPlay.get(0);
            callbackGetLinkPlay.onSuccess(lp);
        }
    }

    //=============================================================================================================END API

    //===========================================================================================================START TRACKING


    private void trackProgress(int s, int percent) {
        //track event view (after video is played 5s), only track if isLivestream false
        if (s == (isLivestream ? 3 : 5)) {
            if (!UizaTrackingUtil.isTrackedEventTypeView(getContext())) {
                trackUiza(UizaData.getInstance().createTrackingInput(getContext(), Constants.EVENT_TYPE_VIEW), () -> UizaTrackingUtil.setTrackingDoneWithEventTypeView(getContext(), true));
            }
        }
        //if current link play is livestream link play
        //we dont track progress play throught 25, 50, 75, 100
        if (isLivestream) {
            Timber.d("isLivestream true -> we dont track progress play throught 25, 50, 75, 100");
            return;
        }
        if (oldPercent == percent) {
            return;
        }
        oldPercent = percent;
        if (percent >= Constants.PLAYTHROUGH_100) {
            if (isTracked100) {
                return;
            }
            if (UizaTrackingUtil.isTrackedEventTypePlayThrought100(getContext())) {
                isTracked100 = true;
            } else {
                trackUiza(UizaData.getInstance().createTrackingInput(getContext(), PLAY_THROUGH_100, Constants.EVENT_TYPE_PLAY_THROUGHT),
                        () -> UizaTrackingUtil.setTrackingDoneWithEventTypePlayThrought100(getContext(), true));
            }
        } else if (percent >= Constants.PLAYTHROUGH_75) {
            if (isTracked75) {
                return;
            }
            if (UizaTrackingUtil.isTrackedEventTypePlayThrought75(getContext())) {
                isTracked75 = true;
            } else {
                trackUiza(UizaData.getInstance().createTrackingInput(getContext(), PLAY_THROUGH_75, Constants.EVENT_TYPE_PLAY_THROUGHT),
                        () -> UizaTrackingUtil.setTrackingDoneWithEventTypePlayThrought75(getContext(), true));
            }
        } else if (percent >= Constants.PLAYTHROUGH_50) {
            if (isTracked50) {
                return;
            }
            if (UizaTrackingUtil.isTrackedEventTypePlayThrought50(getContext())) {
                isTracked50 = true;
            } else {
                trackUiza(UizaData.getInstance().createTrackingInput(getContext(), PLAY_THROUGH_50, Constants.EVENT_TYPE_PLAY_THROUGHT),
                        () -> UizaTrackingUtil.setTrackingDoneWithEventTypePlayThrought50(getContext(), true));
            }
        } else if (percent >= Constants.PLAYTHROUGH_25) {
            if (isTracked25) {
                return;
            }
            if (UizaTrackingUtil.isTrackedEventTypePlayThrought25(getContext())) {
                isTracked25 = true;
            } else {
                trackUiza(UizaData.getInstance().createTrackingInput(getContext(), PLAY_THROUGH_25, Constants.EVENT_TYPE_PLAY_THROUGHT),
                        () -> UizaTrackingUtil.setTrackingDoneWithEventTypePlayThrought25(getContext(), true));
            }
        }
    }

    protected void setDefaultValueForFlagIsTracked() {
        UizaTrackingUtil.clearAllValues(getContext());
        isTracked25 = false;
        isTracked50 = false;
        isTracked75 = false;
        isTracked100 = false;
    }

    private void trackUiza(final UizaTracking uizaTracking, final UizaTrackingUtil.UizaTrackingCallback uizaTrackingCallback) {
        if (UizaTrackingClient.getInstance().getRetrofit() == null) {
            String currentApiTrackingEndPoint = UizaUtil.getApiTrackEndPoint();
            if (currentApiTrackingEndPoint == null || currentApiTrackingEndPoint.isEmpty()) {
                Timber.e("trackUiza failed pip urrentApiTrackingEndPoint == null || currentApiTrackingEndPoint.iuizacoresdk/view/floatview/FUZVideo.java:419sEmpty()");
                return;
            }
            UizaTrackingClient.getInstance().init(currentApiTrackingEndPoint);
        }
        UizaTrackingService service = UizaClientFactory.getTrackingService();
        RxBinder.bind(service.track(uizaTracking), tracking -> {
            if (uizaTrackingCallback != null) {
                uizaTrackingCallback.onTrackingSuccess();
            }
        }, Timber::e);
    }

    private void pingHeartBeat() {
        if (fuzUizaPlayerManager == null || TextUtils.isEmpty(cdnHost)) {
            Timber.e("Error cannot call API pingHeartBeat() -> destroy");
            return;
        }
        UizaHeartBeatService service = UizaClientFactory.getHeartBeatService();
        String cdnName = cdnHost;
        String session = uuid;
        RxBinder.bind(service.pingHeartBeat(cdnName, session),
                result -> {
                    Timber.d("pingHeartBeat onSuccess %s", UizaData.getInstance().getEntityName());
                    handler.postDelayed(this::pingHeartBeat, 10000);
                }, throwable -> {
                    Timber.e(throwable, "pingHeartBeat onFail:");
                    handler.postDelayed(this::pingHeartBeat, 10000);
                });
    }

    private void trackUizaCCUForLivestream() {
        if (fuzUizaPlayerManager == null || !isLivestream || isInitCustomLinkPlay) {
            Timber.e("Error cannot trackUizaCCUForLivestream() -> return %s", UizaData.getInstance().getEntityName());
            return;
        }
        UizaTrackingService service = UizaClientFactory.getTrackingService();
        UizaTrackingCCU uizaTrackingCCU = new UizaTrackingCCU();
        uizaTrackingCCU.setDt(DateUtils.getCurrent(DateUtils.FORMAT_1));
        uizaTrackingCCU.setHo(cdnHost);
        uizaTrackingCCU.setSn(UizaData.getInstance().getChannelName()); // stream name
        uizaTrackingCCU.setDi(DeviceUtils.getDeviceId(getContext()));
        uizaTrackingCCU.setUa(Constants.USER_AGENT);
        RxBinder.bind(service.trackCCU(uizaTrackingCCU), result -> {
            Timber.d("trackCCU success: %s", UizaData.getInstance().getEntityName());
            handler.postDelayed(this::trackUizaCCUForLivestream, INTERVAL_TRACK_CCU);
        }, throwable -> handler.postDelayed(this::trackUizaCCUForLivestream, INTERVAL_TRACK_CCU));
    }

    protected void addTrackingMuizaError(String event, UizaException e) {
        if (isInitCustomLinkPlay) {
            return;
        }
        UizaData.getInstance().addTrackingMuiza(getContext(), event, e);
    }

    protected void addTrackingMuiza(String event) {
        if (isInitCustomLinkPlay) {
            return;
        }
        UizaData.getInstance().addTrackingMuiza(getContext(), event);
    }

    private void callAPITrackMuiza(int s) {
        if (isInitCustomLinkPlay) {
            return;
        }
        if (s <= 0 || s % 10 != 0) {
            return;
        }
        if (isTrackingMuiza) {
            return;
        }
        if (UizaData.getInstance().isMuizaListEmpty()) {
            return;
        }
        isTrackingMuiza = true;
        final List<Muiza> muizaListToTracking = new ArrayList<>(UizaData.getInstance().getMuizaList());
        UizaData.getInstance().clearMuizaList();
        UizaTrackingService service = UizaClientFactory.getTrackingService();
        RxBinder.bind(service.trackMuiza(muizaListToTracking),
                result -> isTrackingMuiza = false,
                throwable -> {
                    isTrackingMuiza = false;
                    UizaData.getInstance().addListTrackingMuiza(muizaListToTracking);
                });
    }

    //================================ END TRACKING

    //================================ START CALLBACK
    public void setProgressListener(VideoViewBase.ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public interface Callback {
        void isInitResult(boolean isInitSuccess);

        void onPlayerStateChanged(boolean playWhenReady, int playbackState);

        void onVideoSizeChanged(int width, int height);

        void onPlayerError(UizaException error);
    }


    protected void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                showProgress();
                timestampRebufferStart = System.currentTimeMillis();
                addTrackingMuiza(Constants.MUIZA_EVENT_REBUFFERSTART);
                TmpParamData.getInstance().addViewRebufferCount();
                if (!playWhenReady) {
                    addTrackingMuiza(Constants.MUIZA_EVENT_WAITING);
                }
                break;
            case Player.STATE_ENDED:
                timestampRebufferStart = 0;
                setDefaultValueForFlagIsTracked();
                addTrackingMuiza(Constants.MUIZA_EVENT_VIEWENDED);
                break;
            case Player.STATE_IDLE:
                timestampRebufferStart = 0;
                showProgress();
                break;
            case Player.STATE_READY:
                if (timestampRebufferStart != 0) {
                    TmpParamData.getInstance().setViewRebufferDuration(System.currentTimeMillis() - timestampRebufferStart);
                    addTrackingMuiza(Constants.MUIZA_EVENT_REBUFFEREND);
                }
                timestampRebufferStart = 0;

                if (!isOnStateReadyFirst) {
                    onStateReadyFirst();
                    isOnStateReadyFirst = true;
                }
                hideProgress();
                break;
        }
        if (callback != null) {
            callback.onPlayerStateChanged(playWhenReady, playbackState);
        }
    }

    protected void onPlayerError(UizaException error) {
        addTrackingMuizaError(Constants.MUIZA_EVENT_ERROR, error);
        if (callback != null) {
            callback.onPlayerError(error);
        }
    }

    private Callback callback;

    private void onStateReadyFirst() {
        if (callback != null) {
            callback.isInitResult(true);
        }
        pingHeartBeat();
        trackUizaCCUForLivestream();
    }

    protected void getLinkPlayOfNextItem(CallbackGetLinkPlay callbackGetLinkPlay) {
        if (UizaData.getInstance().getPlayList() == null) {
            Timber.e("playPlaylistPosition error: incorrect position");
            callbackGetLinkPlay.onSuccess(null);
            return;
        }
        int currentPositionOfDataList = UizaData.getInstance().getCurrentPositionOfPlayList();
        int position = currentPositionOfDataList + 1;
        if (position < 0) {
            Timber.e("This is the first item");
            callbackGetLinkPlay.onSuccess(null);
            return;
        }
        if (position > UizaData.getInstance().getPlayList().size() - 1) {
            Timber.e("This is the last item");
            callbackGetLinkPlay.onSuccess(null);
            return;
        }
        UizaData.getInstance().setCurrentPositionOfPlayList(position);
        PlaybackInfo data = UizaData.getInstance().getDataWithPositionOfPlayList(position);
        if (data == null || data.getId() == null || data.getId().isEmpty()) {
            Timber.e("playPlaylistPosition error: data null or cannot get id");
            callbackGetLinkPlay.onSuccess(null);
            return;
        }
        Timber.d("-----------------------> playPlaylistPosition %d -> %s", position, data.getName());
//        callAPIGetTokenStreaming(data.getId(), callbackGetLinkPlay);
    }

    public interface CallbackGetLinkPlay {
        void onSuccess(String linkPlay);
    }
    //=============================================================================================================END CALLBACK
}