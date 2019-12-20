package uizacoresdk.view.rl.video;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.github.rubensousa.previewseekbar.PreviewView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.video.VideoListener;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;
import uizacoresdk.BuildConfig;
import uizacoresdk.R;
import uizacoresdk.interfaces.CallbackUZTimebar;
import uizacoresdk.interfaces.UZBufferCallback;
import uizacoresdk.interfaces.UZCallback;
import uizacoresdk.interfaces.UZItemClick;
import uizacoresdk.interfaces.UZLiveContentCallback;
import uizacoresdk.interfaces.UZTVCallback;
import uizacoresdk.listerner.ProgressCallback;
import uizacoresdk.util.SensorOrientationChangeNotifier;
import uizacoresdk.util.TmpParamData;
import uizacoresdk.util.UZData;
import uizacoresdk.util.UZInput;
import uizacoresdk.util.UZOsUtil;
import uizacoresdk.util.UZTrackingUtil;
import uizacoresdk.util.UZUtil;
import uizacoresdk.view.ComunicateMng;
import uizacoresdk.view.UZPlayerView;
import uizacoresdk.view.dlg.hq.UZItem;
import uizacoresdk.view.dlg.hq.UZTrackSelectionView;
import uizacoresdk.view.dlg.info.UZDlgInfoV1;
import uizacoresdk.view.dlg.playlistfolder.CallbackPlaylistFolder;
import uizacoresdk.view.dlg.playlistfolder.UZDlgPlaylistFolder;
import uizacoresdk.view.dlg.speed.UZDlgSpeed;
import uizacoresdk.view.floatview.FUZVideoService;
import uizacoresdk.view.rl.timebar.UZTimebar;
import uizacoresdk.view.rl.videoinfo.StatsForNerdsView;
import vn.uiza.core.common.Constants;
import vn.uiza.core.exception.UZException;
import vn.uiza.core.exception.UZExceptionUtil;
import vn.uiza.core.utilities.LActivityUtil;
import vn.uiza.core.utilities.LAnimationUtil;
import vn.uiza.core.utilities.LConnectivityUtil;
import vn.uiza.core.utilities.LDateUtils;
import vn.uiza.core.utilities.LDeviceUtil;
import vn.uiza.core.utilities.LDialogUtil;
import vn.uiza.core.utilities.LImageUtil;
import vn.uiza.core.utilities.LScreenUtil;
import vn.uiza.core.utilities.LSocialUtil;
import vn.uiza.core.utilities.LUIUtil;
import vn.uiza.core.utilities.connection.LConectifyService;
import vn.uiza.data.EventBusData;
import vn.uiza.restapi.RxBinder;
import vn.uiza.restapi.UizaLiveService;
import vn.uiza.restapi.UizaVodService;
import vn.uiza.restapi.heartbeat.UizaHeartBeatService;
import vn.uiza.restapi.linkplay.UizaLinkPlayService;
import vn.uiza.restapi.model.tracking.UizaTracking;
import vn.uiza.restapi.model.tracking.UizaTrackingCCU;
import vn.uiza.restapi.model.tracking.muiza.Muiza;
import vn.uiza.restapi.model.v2.listallentity.Subtitle;
import vn.uiza.restapi.model.v3.ad.Ad;
import vn.uiza.restapi.model.v3.linkplay.getlinkplay.ResultGetLinkPlay;
import vn.uiza.restapi.model.v3.linkplay.getlinkplay.Url;
import vn.uiza.restapi.model.v3.linkplay.gettokenstreaming.ResultGetTokenStreaming;
import vn.uiza.restapi.model.v3.linkplay.gettokenstreaming.SendGetTokenStreaming;
import vn.uiza.restapi.model.v3.metadata.getdetailofmetadata.Data;
import vn.uiza.restapi.model.v4.playerinfo.Logo;
import vn.uiza.restapi.model.v4.playerinfo.PlayerInfor;
import vn.uiza.restapi.model.v5.UizaPlayback;
import vn.uiza.restapi.model.v5.live.LiveEntity;
import vn.uiza.restapi.model.v5.vod.VODEntity;
import vn.uiza.restapi.restclient.UizaClientFactory;
import vn.uiza.restapi.tracking.UizaTrackingService;
import vn.uiza.restapi.uiza.UZService;
import vn.uiza.utils.util.AppUtils;
import vn.uiza.utils.util.ConvertUtils;
import vn.uiza.utils.util.SentryUtils;
import vn.uiza.utils.util.ViewUtils;
import vn.uiza.views.autosize.UZImageButton;
import vn.uiza.views.autosize.UZTextView;
import vn.uiza.views.seekbar.UZVerticalSeekBar;

/**
 * Created by loitp on 2/27/2019.
 */

public class UZVideo extends RelativeLayout
        implements PreviewView.OnPreviewChangeListener, View.OnClickListener, View.OnFocusChangeListener,
        UZPlayerView.ControllerStateCallback, SensorOrientationChangeNotifier.Listener {
    private int DEFAULT_VALUE_BACKWARD_FORWARD = 10000;//10000 mls
    private int DEFAULT_VALUE_CONTROLLER_TIMEOUT = 8000;//8000 mls
    private final int DELAY_FIRST_TO_GET_LIVE_INFORMATION = 100;
    private final int DELAY_TO_GET_LIVE_INFORMATION = 15000;
    private static final String M3U8_EXTENSION = ".m3u8";
    private static final String MPD_EXTENSION = ".mpd";
    private static final String PLAY_THROUGH_100 = "100";
    private static final String PLAY_THROUGH_75 = "75";
    private static final String PLAY_THROUGH_50 = "50";
    private static final String PLAY_THROUGH_25 = "25";
    private static final String HYPHEN = "-";
    private boolean isLivestream;
    private boolean isTablet;
    private String cdnHost;
    private boolean isTV;//current device is TV or not (smartphone, tablet)
    private View bkg;
    private RelativeLayout rootView, rlChromeCast;
    private IUZPlayerManager uzPlayerManager;
    private ProgressBar progressBar;
    private LinearLayout llTop, debugRootView;
    private RelativeLayout rlMsg, rlLiveInfo, rlEndScreen;
    private FrameLayout previewFrameLayout;
    private UZTimebar uzTimebar;
    private ImageView ivThumbnail, ivVideoCover, ivLogo;
    private UZTextView tvPosition, tvDuration;
    private TextView tvMsg, tvTitle, tvLiveStatus, tvLiveView, tvLiveTime;
    private UZImageButton ibFullscreenIcon, ibPauseIcon, ibPlayIcon, ibReplayIcon, ibRewIcon, ibFfwdIcon, ibBackScreenIcon, ibVolumeIcon,
            ibSettingIcon, ibCcIcon, ibPlaylistFolderIcon//danh sach playlist folder
            , ibHearingIcon, ibPictureInPictureIcon, ibSkipPreviousIcon, ibSkipNextIcon, ibSpeedIcon, ivLiveTime, ivLiveView, ibsCast;
    private TextView debugTextView, tvEndScreenMsg;
    private UZPlayerView uzPlayerView;

    private ResultGetLinkPlay mResultGetLinkPlay;
    private ResultGetTokenStreaming mResultGetTokenStreaming;
    private String urlIMAAd = null;
    private PlayerInfor playerInfor;
    private long startTime = Constants.UNKNOW;
    private boolean isSetUZTimebarBottom;
    private UZChromeCast uZChromeCast;
    private boolean isCastingChromecast = false;
    private List<Subtitle> subtitleList = new ArrayList<>();
    private boolean autoMoveToLiveEdge;
    private @LayoutRes
    int pipControlSkin;

    Handler handler = new Handler();

    public UZVideo(Context context) {
        super(context);
        onCreate();
    }

    public UZVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
        onCreate();
    }

    public UZVideo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public UZVideo(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onCreate();
    }

    private void onCreate() {
        EventBus.getDefault().register(this);
        startConectifyService();
        inflate(getContext(), R.layout.v3_uiza_ima_video_core_rl, this);
        checkDevices();
        rootView = findViewById(R.id.root_view);
        addPlayerView();
        findViews();
        resizeContainerView();
        updateUIEachSkin();
        setMarginPreviewTimeBar();
        setMarginRlLiveInfo();
        if (isCastDependencyAvailable()) {
            setupChromeCast();
        }
        updateUISizeThumbnail();
        scheduleJob();
    }

    private void checkDevices() {
        isTablet = LDeviceUtil.isTablet(getContext());
        isTV = LDeviceUtil.isTV(getContext());
    }

    private void resizeContainerView() {
        if (!isFreeSize) {
            setSize(getVideoW(), getVideoH());
        } else {
            setSize(this.getWidth(), this.getHeight());
        }
    }

    private void startConectifyService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent startServiceIntent = new Intent(getContext(), LConectifyService.class);
            getContext().startService(startServiceIntent);
        }
    }

    //========================================================================START CONFIG
    private boolean isAutoStart = Constants.DF_PLAYER_IS_AUTO_START;

    public void setAutoStart(boolean isAutoStart) {
        this.isAutoStart = isAutoStart;
        TmpParamData.getInstance().setPlayerAutoplayOn(isAutoStart);
        updateUIButtonPlayPauseDependOnIsAutoStart();
    }

    public boolean isAutoStart() {
        return isAutoStart;
    }

    private boolean isAutoSwitchItemPlaylistFolder = true;

    public void setAutoSwitchItemPlaylistFolder(boolean isAutoSwitchItemPlaylistFolder) {
        this.isAutoSwitchItemPlaylistFolder = isAutoSwitchItemPlaylistFolder;
    }

    public boolean isAutoSwitchItemPlaylistFolder() {
        return isAutoSwitchItemPlaylistFolder;
    }

    private boolean isAutoShowController;

    public void setControllerAutoShow(boolean isAutoShowController) {
        this.isAutoShowController = isAutoShowController;
        if (uzPlayerView != null) {
            uzPlayerView.setControllerAutoShow(isAutoShowController);
        }
    }

    public boolean getControllerAutoShow() {
        if (uzPlayerView == null) {
            return false;
        }
        return uzPlayerView.getControllerAutoShow();
    }

    public long getDuration() {
        if (getPlayer() == null) {
            return Constants.NOT_FOUND;
        }
        return getPlayer().getDuration();
    }

    // An estimate of the position in the current window up to which data is buffered.
    // If the length of the content is 100,00 ms, and played 50,000 ms already with extra 50,000 ms~ 60,000 ms buffered,
    // it returns 60,000 ms.
    public long getBufferedPosition() {
        if (getPlayer() == null) {
            return Constants.NOT_FOUND;
        }
        return getPlayer().getBufferedPosition();
    }

    // An estimate of the percentage in the current window up to which data is buffered.
    // If the length of the content is 100,00 ms, and played 50,000 ms already with extra 50,000 ms~ 60,000 ms buffered,
    // it returns 60(%).
    public int getBufferedPercentage() {
        if (getPlayer() == null) {
            return Constants.NOT_FOUND;
        }
        return getPlayer().getBufferedPercentage();
    }

    // Lay pixel dung cho custom UI like youtube, uzTimebar bottom of player controller
    public int getPixelAdded() {
        if (isSetUZTimebarBottom) {
            return getHeightUZTimeBar() / 2;
        }
        return 0;
    }

    public int getVideoW() {
        if (uzPlayerManager == null) {
            return 0;
        }
        return uzPlayerManager.getVideoW();
    }

    public int getVideoH() {
        if (uzPlayerManager == null) {
            return 0;
        }
        return uzPlayerManager.getVideoH();
    }

    //return pixel
    public int getHeightUZTimeBar() {
        return LUIUtil.getHeightOfView(uzTimebar);
    }

    //The current position of playing. the window means playable region, which is all of the content if vod, and some portion of the content if live.
    public long getCurrentPosition() {
        if (uzPlayerManager == null) {
            return Constants.NOT_FOUND;
        }
        return uzPlayerManager.getCurrentPosition();
    }

    public Format getVideoFormat() {
        if (getPlayer() == null) {
            return null;
        }
        Format format = getPlayer().getVideoFormat();
        if (format == null) {
            return null;
        }
        return format;
    }

    public Format getAudioFormat() {
        if (getPlayer() == null) {
            return null;
        }
        Format format = getPlayer().getAudioFormat();
        if (format == null) {
            return null;
        }
        return format;
    }

    public int getVideoProfileW() {
        if (uzPlayerManager == null) {
            return Constants.UNKNOW;
        }
        return uzPlayerManager.getVideoProfileW();
    }

    public int getVideoProfileH() {
        if (uzPlayerManager == null) {
            return Constants.UNKNOW;
        }
        return uzPlayerManager.getVideoProfileH();
    }

    public void setResizeMode(int resizeMode) {
        if (uzPlayerView != null) {
            uzPlayerView.setResizeMode(resizeMode);
        }
    }

    public void setSize(int width, int height) {
        UZUtil.resizeLayout(rootView, ivVideoCover, getPixelAdded(), width, height, isFreeSize);
    }

    private boolean isFreeSize;

    public void setFreeSize(boolean isFreeSize) {
        this.isFreeSize = isFreeSize;
        resizeContainerView();
    }

    private boolean isPlayerControllerAlwayVisible;

    public void setPlayerControllerAlwayVisible() {
        setControllerAutoShow(true);
        setHideControllerOnTouch(false);
        setControllerShowTimeoutMs(0);
        isPlayerControllerAlwayVisible = true;
    }
    //========================================================================END CONFIG

    private boolean isSetFirstRequestFocusDone;
    private boolean isHasError;

    protected void handleError(UZException uzException) {
        if (uzException == null) {
            return;
        }
        notifyError(uzException);
        // Capture by Sentry, in uzException already contains Message, Error Code
        SentryUtils.captureException(uzException.getException());
        addTrackingMuizaError(Constants.MUIZA_EVENT_ERROR, uzException);
        if (isHasError) {
            return;
        }
        isHasError = true;
        UZData.getInstance().setSettingPlayer(false);
    }

    private void notifyError(UZException exception) {
        if (uzCallback != null) {
            uzCallback.onError(exception);
        }
    }

    private String entityId;
    private UUID uuid;
    private long timestampBeforeInitNewSession;

    protected void init(String entityId, boolean isClearDataPlaylistFolder, boolean isLivestream) {
        Timber.d("*****NEW SESSION********************");
        Timber.d("entityId %s", entityId);
        this.isLivestream = isLivestream;
        this.uuid = UUID.randomUUID();
        if (isClearDataPlaylistFolder) {
            UZData.getInstance().clearDataForPlaylistFolder();
        }
        if (entityId == null) {
            Timber.e("init error because entityId == null -> called from PIP");
            try {
                if (TextUtils.isEmpty(UZData.getInstance().getPlayback().getHls())) {
                    notifyError(UZExceptionUtil.getExceptionEntityId());
                    Timber.e("init error: entityId null or empty");
                    return;
                } else {
                    entityId = UZData.getInstance().getEntityId();
                }
            } catch (NullPointerException e) {
                notifyError(UZExceptionUtil.getExceptionEntityId());
                SentryUtils.captureException(e);
                Timber.e(e, "init NullPointerException");
                return;
            }
        } else {
            timestampBeforeInitNewSession = System.currentTimeMillis();
            timestampOnStartPreview = 0;
            maxSeekLastDuration = 0;
            UZData.getInstance().clearUizaInput();
            TmpParamData.getInstance().clearAll();
            TmpParamData.getInstance().addPlayerViewCount();
            TmpParamData.getInstance().setSessionId(uuid.toString());
        }
        Timber.d("isPlayWithPlaylistFolder %s", UZData.getInstance().isPlayWithPlaylistFolder());
        handlePlayPlayListFolderUI();
        isCalledFromChangeSkin = false;
        isInitCustomLinkPlay = false;
        isCalledApiGetDetailEntity = false;
        isCalledAPIGetUrlIMAAdTag = false;
        isCalledAPIGetTokenStreaming = false;
        urlIMAAd = null;
        mResultGetTokenStreaming = null;
        isHasError = false;
        isOnPlayerEnded = false;
        this.entityId = entityId;
        Timber.d("get entityId: %s", entityId);
        UZData.getInstance().setSettingPlayer(true);
        hideLayoutMsg();
        setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT);
        updateUIEndScreen();
        if (!LConnectivityUtil.isConnected(getContext())) {
            notifyError(UZExceptionUtil.getExceptionNoConnection());
            Timber.d("!isConnected return");
            return;
        }
        callAPIGetDetailEntity();
        callAPIGetPlayerInfor();
        callAPIGetUrlIMAAdTag();
        callAPIGetTokenStreaming();
    }

    private void handlePlayPlayListFolderUI() {
        if (isPlayPlaylistFolder()) {
            setVisibilityOfPlaylistFolderController(VISIBLE);
        } else {
            setVisibilityOfPlaylistFolderController(GONE);
        }
    }

    /**
     * init player with entity id, ad, seekbar thumnail
     */
    public void init(@NonNull String entityId) {
        init(entityId, true, false);
    }

    /**
     * init player with livestream entity id
     */
    public void initPlayback(@NonNull UizaPlayback playback) {
        if(UZData.getInstance().getUzInput() != null)
            UZData.getInstance().getUzInput().setPlayback(playback);
        initPlayback(playback.getHls(), playback.isLive());
    }

    /**
     * init player with live entity
     */
    public void initLiveEntity(@NonNull String entityId) {
        init(entityId, true, true);
    }

    private boolean isInitCustomLinkPlay;//user pass any link (not use entityId or metadataId)

    public void initPlayback(@NonNull String linkPlay, boolean isLivestream) {
        Timber.d("init linkPlay %s", linkPlay);
        isInitCustomLinkPlay = true;
        isCalledFromChangeSkin = false;
        setVisibilityOfPlaylistFolderController(GONE);
        isCalledApiGetDetailEntity = false;
        isCalledAPIGetUrlIMAAdTag = false;
        isCalledAPIGetTokenStreaming = false;
        urlIMAAd = null;
        mResultGetTokenStreaming = null;
        this.entityId = null;
        UZData.getInstance().setSettingPlayer(true);
        hideLayoutMsg();
        setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT);
        isOnPlayerEnded = false;
        updateUIEndScreen();
        isHasError = false;
        this.isLivestream = isLivestream;
        if (isLivestream) {
            startTime = Constants.UNKNOW;
        }
        setDefaultValueForFlagIsTracked();
        if (uzPlayerManager != null) {
            releaseUzPlayerManager();
            mResultGetLinkPlay = null;
            resetCountTryLinkPlayError();
            showProgress();
        }
        updateUIDependOnLivestream();
        // TODO: Check how to get subtitle of a custom link play, because we have no idea about entityId or appId
        List<Subtitle> subtitleList = null;

        if (!LConnectivityUtil.isConnected(getContext())) {
            notifyError(UZExceptionUtil.getExceptionNoConnection());
            return;
        }
        initDataSource(linkPlay, UZData.getInstance().getUrlIMAAd(), UZData.getInstance().getUrlThumnailsPreviewSeekbar(), subtitleList, isAdsDependencyAvailable());
        if (uzCallback != null) {
            uzCallback.isInitResult(false, true, mResultGetLinkPlay, UZData.getInstance().getPlayback());
        }
        initUizaPlayerManager();
    }

    public void initPlaylistFolder(String metadataId) {
        if (metadataId == null) {
            Timber.d("initPlaylistFolder metadataId null -> called from PIP: %b", isGetClickedPip);
        } else {
            Timber.d("initPlaylistFolder metadataId %s, -> called from PIP: %b", metadataId, isGetClickedPip);
            UZData.getInstance().clearDataForPlaylistFolder();
        }
        isHasError = false;
        isClickedSkipNextOrSkipPrevious = false;
        callAPIGetListAllEntity(metadataId);
    }

    public void toggleStatsForNerds() {
        if (getPlayer() == null) return;
        boolean isEnableStatsForNerds =
                statsForNerdsView == null || statsForNerdsView.getVisibility() != View.VISIBLE;
        if (isEnableStatsForNerds) {
            ViewUtils.visibleViews(statsForNerdsView);
        } else {
            ViewUtils.goneViews(statsForNerdsView);
        }
    }

    private int countTryLinkPlayError = 0;

    protected void tryNextLinkPlay() {
        if (isLivestream) {
            // try to play 5 times
            if (countTryLinkPlayError >= 5) {
                if (uzLiveContentCallback != null) {
                    uzLiveContentCallback.onLivestreamUnAvailable();
                }
                return;
            }
            // if entity is livestreaming, dont try to next link play
            Timber.e("tryNextLinkPlay isLivestream true -> try to replay = count %d", countTryLinkPlayError);
            if (uzPlayerManager != null) {
                uzPlayerManager.initWithoutReset();
                uzPlayerManager.setRunnable();
            }
            countTryLinkPlayError++;
            return;
        }
        countTryLinkPlayError++;
        Timber.e("%s: %d", getContext().getString(R.string.cannot_play_will_try), countTryLinkPlayError);
        releaseUzPlayerManager();
        checkToSetUpResource();
    }

    //khi call api callAPIGetLinkPlay nhung json tra ve ko co data
    //se co gang choi video da play gan nhat
    //neu co thi se play
    //khong co thi bao loi
    private void handleErrorNoData() {
        removeVideoCover(true);
        if (uzCallback != null) {
            UZData.getInstance().setSettingPlayer(false);
            uzCallback.isInitResult(false, false, null, null);
        }
    }

    protected void resetCountTryLinkPlayError() {
        countTryLinkPlayError = 0;
    }

    public void onDestroy() {
        //cannot use isGetClickedPip (global variable), must use UZUtil.getClickedPip(activity)
        if (UZUtil.getClickedPip(getContext())) {
            UZUtil.stopMiniPlayer(getContext());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getContext().stopService(new Intent(getContext(), LConectifyService.class));
        }
        releasePlayerAnalytic();
        releaseUzPlayerManager();
        UZData.getInstance().setSettingPlayer(false);
        LDialogUtil.clearAll();
        isCastingChromecast = false;
        isCastPlayerPlayingFirst = false;
        cdnHost = null;
        EventBus.getDefault().unregister(this);
    }

    private void releasePlayerAnalytic() {
        if (getPlayer() != null) {
            getPlayer().removeAnalyticsListener(nerdAnalyticsListener);
        }
        getContext().getContentResolver().unregisterContentObserver(volumeObserver);
    }

    private void releaseUzPlayerManager() {
        if (uzPlayerManager != null) {
            uzPlayerManager.release();
        }
    }

    public void onResume() {
        SensorOrientationChangeNotifier.getInstance(getContext()).addListener(this);
        if (isCastingChromecast) {
            return;
        }
        activityIsPausing = false;
        if (uzPlayerManager != null) {
            if (ibPlayIcon == null || ibPlayIcon.getVisibility() != VISIBLE) {
                uzPlayerManager.resumeVideo();
            }
        }
        // try to move to the edge of livestream video
        if (autoMoveToLiveEdge && isLivestream()) {
            seekToLiveEdge();
        }
    }

    public boolean isPlaying() {
        if (getPlayer() == null) {
            return false;
        }
        return getPlayer().getPlayWhenReady();
    }

    /**
     * Set auto move the the last window of livestream, default is false
     *
     * @param autoMoveToLiveEdge true if always seek to last livestream video, otherwise false
     */
    public void setAutoMoveToLiveEdge(boolean autoMoveToLiveEdge) {
        this.autoMoveToLiveEdge = autoMoveToLiveEdge;
    }

    /**
     * Seek to live edge of a streaming video
     */
    public void seekToLiveEdge() {
        if (isLivestream() && getPlayer() != null) {
            getPlayer().seekToDefaultPosition();
        }
    }

    private boolean activityIsPausing = false;

    public void onPause() {
        activityIsPausing = true;
        SensorOrientationChangeNotifier.getInstance(getContext()).remove(this);
        if (uzPlayerManager != null) {
            uzPlayerManager.pauseVideo();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (getContext() == null) {
            return;
        }
        if (isCastingChromecast()) {
            Timber.e("Error: handleClickPictureInPicture isCastingChromecast -> return");
            return;
        }
        if (LDeviceUtil.isCanOverlay(getContext())) {
            initializePiP();
        }
    }

    private long timestampOnStartPreview;

    @Override
    public void onStartPreview(PreviewView previewView, int progress) {
        timestampOnStartPreview = System.currentTimeMillis();
        addTrackingMuiza(Constants.MUIZA_EVENT_SEEKING);
        if (callbackUZTimebar != null) {
            callbackUZTimebar.onStartPreview(previewView, progress);
        }
    }

    private boolean isOnPreview;

    @Override
    public void onPreview(PreviewView previewView, int progress, boolean fromUser) {
        isOnPreview = true;
        updateUIIbRewIconDependOnProgress(progress, true);
        if (callbackUZTimebar != null) {
            callbackUZTimebar.onPreview(previewView, progress, fromUser);
        }
    }

    private long maxSeekLastDuration;

    @Override
    public void onStopPreview(PreviewView previewView, int progress) {
        if (isCastingChromecast) {
            UZData.getInstance().getCasty().getPlayer().seek(progress);
        }
        TmpParamData.getInstance().addViewSeekCount();
        long seekLastDuration = System.currentTimeMillis() - timestampOnStartPreview;
        TmpParamData.getInstance().setViewSeekDuration(seekLastDuration);
        if (maxSeekLastDuration < seekLastDuration) {
            maxSeekLastDuration = seekLastDuration;
            TmpParamData.getInstance().setViewMaxSeekTime(maxSeekLastDuration);
        }
        isOnPreview = false;
        onStopPreview(progress);
        if (callbackUZTimebar != null) {
            callbackUZTimebar.onStopPreview(previewView, progress);
        }
        addTrackingMuiza(Constants.MUIZA_EVENT_SEEKED);
    }

    public void onStopPreview(int progress) {
        if (uzPlayerManager != null && !isCastingChromecast) {
            uzPlayerManager.seekTo(progress);
            uzPlayerManager.resumeVideo();
            isOnPlayerEnded = false;
            updateUIEndScreen();
        }
    }

    @Override
    public void onFocusChange(View view, boolean isFocus) {
        if (uztvCallback != null) {
            uztvCallback.onFocusChange(view, isFocus);
        } else {
            if (firstViewHasFocus == null) {
                firstViewHasFocus = view;
            }
        }
    }

    private boolean isLandscape;//current screen is landscape or portrait

    public boolean isLandscape() {
        return isLandscape;
    }

    @Override
    public void onOrientationChange(int orientation) {
        //270 land trai
        //0 portrait duoi
        //90 land phai
        //180 portrait tren
        boolean isDeviceAutoRotation = LDeviceUtil.isRotationPossible(getContext());
        if (orientation == 90 || orientation == 270) {
            if (isDeviceAutoRotation && !isLandscape) {
                LActivityUtil.changeScreenLandscape((Activity) getContext(), orientation);
            }
        } else {
            if (isDeviceAutoRotation && isLandscape) {
                LActivityUtil.changeScreenPortrait((Activity) getContext());
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getContext() == null) {
            return;
        }
        resizeContainerView();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LScreenUtil.hideDefaultControls(getContext());
            isLandscape = true;
            UZUtil.setUIFullScreenIcon(getContext(), ibFullscreenIcon, true);
            ViewUtils.goneViews(ibPictureInPictureIcon);
        } else {
            LScreenUtil.showDefaultControls(getContext());
            isLandscape = false;
            UZUtil.setUIFullScreenIcon(getContext(), ibFullscreenIcon, false);
            if (!isCastingChromecast()) {
                ViewUtils.visibleViews(ibPictureInPictureIcon);
            }
        }
        TmpParamData.getInstance().setPlayerIsFullscreen(isLandscape);
        setMarginPreviewTimeBar();
        setMarginRlLiveInfo();
        updateUISizeThumbnail();
        updateUIPositionOfProgressBar();
        if (isSetUZTimebarBottom) {
            setMarginDependOnUZTimeBar(uzPlayerView.getVideoSurfaceView());
        }
        if (uzCallback != null) {
            uzCallback.onScreenRotate(isLandscape);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == rlMsg) {
            LAnimationUtil.play(v, Techniques.Pulse);
        } else if (v == ibFullscreenIcon) {
            toggleFullscreen();
        } else if (v == ibBackScreenIcon) {
            handleClickBackScreen();
        } else if (v == ibVolumeIcon) {
            handleClickBtVolume();
        } else if (v == ibSettingIcon) {
            handleClickSetting();
        } else if (v == ibCcIcon) {
            handleClickCC();
        } else if (v == ibPlaylistFolderIcon) {
            handleClickPlaylistFolder();
        } else if (v == ibHearingIcon) {
            handleClickHearing();
        } else if (v == ibPictureInPictureIcon) {
            handleClickPictureInPicture();
        } else if (v.getParent() == debugRootView) {
            showUZTrackSelectionDialog(v, true);
        } else if (v == rlChromeCast) {
            Timber.e("dangerous to remove");
        } else if (v == ibFfwdIcon) {
            if (isCastingChromecast) {
                UZData.getInstance().getCasty().getPlayer().seekToForward(DEFAULT_VALUE_BACKWARD_FORWARD);
            } else {
                if (uzPlayerManager != null) {
                    uzPlayerManager.seekToForward(DEFAULT_VALUE_BACKWARD_FORWARD);
                }
            }
        } else if (v == ibRewIcon) {
            if (isCastingChromecast) {
                UZData.getInstance().getCasty().getPlayer().seekToBackward(DEFAULT_VALUE_BACKWARD_FORWARD);
            } else {
                if (uzPlayerManager != null) {
                    uzPlayerManager.seekToBackward(DEFAULT_VALUE_BACKWARD_FORWARD);
                    if (isPlaying()) {
                        isOnPlayerEnded = false;
                        updateUIEndScreen();
                    }
                }
            }
        } else if (v == ibPauseIcon) {
            pauseVideo();
        } else if (v == ibPlayIcon) {
            resumeVideo();
        } else if (v == ibReplayIcon) {
            replay();
        } else if (v == ibSkipNextIcon) {
            handleClickSkipNext();
        } else if (v == ibSkipPreviousIcon) {
            handleClickSkipPrevious();
        } else if (v == ibSpeedIcon) {
            showSpeed();
        } else if (v == tvEndScreenMsg) {
            LAnimationUtil.play(v, Techniques.Pulse);
        } else if (v == ivLogo) {
            LAnimationUtil.play(v, Techniques.Pulse);
            if (playerInfor == null || playerInfor.getData() == null || playerInfor.getData().getLogo() == null || playerInfor.getData().getLogo().getUrl() == null) {
                return;
            }
            LSocialUtil.openUrlInBrowser(getContext(), playerInfor.getData().getLogo().getUrl());
        }
        /*có trường hợp đang click vào các control thì bị ẩn control ngay lập tức, trường hợp này ta có thể xử lý khi click vào control thì reset count down để ẩn control ko
        default controller timeout là 8s, vd tới s thứ 7 bạn tương tác thì tới s thứ 8 controller sẽ bị ẩn*/
        if (isDefaultUseController) {
            if (rlMsg == null || rlMsg.getVisibility() != VISIBLE) {
                if (isPlayerControllerShowing()) {
                    showController();
                }
            }
        }
        if (uzItemClick != null) {
            uzItemClick.onItemClick(v);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void handleClickPictureInPicture() {
        if (getContext() == null) {
            notifyError(UZExceptionUtil.getExceptionShowPip());
            return;
        }
        if (!isInitMiniPlayerSuccess) {
            //dang init 1 instance mini player roi, khong cho init nua
            notifyError(UZExceptionUtil.getExceptionShowPip());
            return;
        }
        if (isCastingChromecast()) {
            notifyError(UZExceptionUtil.getExceptionShowPip());
            return;
        }
        if (LDeviceUtil.isCanOverlay(getContext())) {
            initializePiP();
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
            ((Activity) getContext()).startActivityForResult(intent, Constants.CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }

    private boolean isInitMiniPlayerSuccess = true;

    public void initializePiP() {
        if (getContext() == null || uzPlayerManager == null || uzPlayerManager.getLinkPlay() == null) {
            notifyError(UZExceptionUtil.getExceptionShowPip());
            return;
        }
        ViewUtils.goneViews(ibPictureInPictureIcon);
        if (uzCallback != null) {
            isInitMiniPlayerSuccess = false;
            uzCallback.onStateMiniPlayer(isInitMiniPlayerSuccess);
        }
        UZUtil.setVideoWidth(getContext(), getVideoW());
        UZUtil.setVideoHeight(getContext(), getVideoH());
        Intent intent = new Intent(getContext(), FUZVideoService.class);
        intent.putExtra(Constants.FLOAT_CONTENT_POSITION, getCurrentPosition());
        intent.putExtra(Constants.FLOAT_USER_USE_CUSTOM_LINK_PLAY, isInitCustomLinkPlay);
        intent.putExtra(Constants.FLOAT_UUID, uuid.toString());
        intent.putExtra(Constants.FLOAT_LINK_PLAY, uzPlayerManager.getLinkPlay());
        intent.putExtra(Constants.FLOAT_IS_LIVESTREAM, isLivestream);
        intent.putExtra(Constants.FLOAT_PROGRESS_BAR_COLOR, progressBarColor);
        intent.putExtra(Constants.FLOAT_CONTROL_SKIN_ID, pipControlSkin);
        getContext().startService(intent);
    }

    public SimpleExoPlayer getPlayer() {
        if (uzPlayerManager == null) {
            return null;
        }
        return uzPlayerManager.getPlayer();
    }

    public void seekTo(long positionMs) {
        if (uzPlayerManager != null) {
            uzPlayerManager.seekTo(positionMs);
        }
    }

    public void setControllerShowTimeoutMs(int controllerShowTimeoutMs) {
        DEFAULT_VALUE_CONTROLLER_TIMEOUT = controllerShowTimeoutMs;
        uzPlayerView.setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT);
    }

    public int getControllerShowTimeoutMs() {
        if (uzPlayerView != null) {
            return uzPlayerView.getControllerShowTimeoutMs();
        }
        return Constants.NOT_FOUND;
    }

    public boolean isPlayerControllerShowing() {
        if (uzPlayerView != null) {
            return uzPlayerView.isControllerVisible();
        }
        return false;
    }

    public void showController() {
        if (uzPlayerView != null) {
            uzPlayerView.showController();
        }
    }

    public void hideController() {
        if (isPlayerControllerAlwayVisible) {
            return;
        }
        if (!isCastingChromecast) {//dont hide if is casting chromecast
            if (uzPlayerView != null) {
                uzPlayerView.hideController();
            }
        }
    }

    private boolean isHideOnTouch = true;

    public void setHideControllerOnTouch(boolean isHide) {
        if (uzPlayerView != null) {
            this.isHideOnTouch = isHide;
            uzPlayerView.setControllerHideOnTouch(isHide);
        }
    }

    public boolean getControllerHideOnTouch() {
        if (uzPlayerView != null) {
            return uzPlayerView.getControllerHideOnTouch();
        }
        return false;
    }

    private boolean isDefaultUseController = true;

    public boolean isDefaultUseController() {
        return isDefaultUseController;
    }

    public void setDefaultUseController(boolean isDefaultUseController) {
        this.isDefaultUseController = isDefaultUseController;
        setUseController(this.isDefaultUseController);
    }

    protected void setUseController(final boolean isUseController) {
        if (uzPlayerView != null) {
            uzPlayerView.setUseController(isUseController);
        }
    }

    //===================================================================START FOR PLAYLIST/FOLDER
    private final int pfLimit = 100;
    private int pfPage = 0;
    private int pfTotalPage = Integer.MAX_VALUE;
    private final String pfOrderBy = "createdAt";
    private final String pfOrderType = "DESC";
    private final String publishToCdn = "success";

    private void callAPIGetListAllEntity(String metadataId) {
        showProgress();
        boolean isDataListExist = UZData.getInstance().getDataList() != null;
        if (isDataListExist) {
            playPlaylistPosition(UZData.getInstance().getCurrentPositionOfDataList());
            hideProgress();
        } else {
            UZService service = UizaClientFactory.getUizaService();
            RxBinder.bind(service.getListAllEntity(UZData.getInstance().getAPIVersion(), metadataId, pfLimit, pfPage, pfOrderBy, pfOrderType, publishToCdn, UZData.getInstance().getAppId()),
                    result -> {
                        if (result == null || result.getMetadata() == null || result.getData().isEmpty()) {
                            if (uzCallback != null) {
                                handleError(UZExceptionUtil.getExceptionListAllEntity());
                            }
                            return;
                        }
                        if (pfTotalPage == Integer.MAX_VALUE) {
                            int totalItem = (int) result.getMetadata().getTotal();
                            float ratio = (float) (totalItem / pfLimit);
                            if (ratio == 0) {
                                pfTotalPage = (int) ratio;
                            } else if (ratio > 0) {
                                pfTotalPage = (int) ratio + 1;
                            } else {
                                pfTotalPage = (int) ratio;
                            }
                        }
                        UZData.getInstance().setDataList(result.getData());
                        if (UZData.getInstance().getDataList() == null || UZData.getInstance().getDataList().isEmpty()) {
                            notifyError(UZExceptionUtil.getExceptionListAllEntity());
                            return;
                        }
                        playPlaylistPosition(UZData.getInstance().getCurrentPositionOfDataList());
                        hideProgress();
                    }, throwable -> {
                        Timber.e(throwable, "callAPIGetListAllEntity onFail");
                        notifyError(UZExceptionUtil.getExceptionListAllEntity());
                        hideProgress();
                    });
        }
    }

    protected boolean isPlayPlaylistFolder() {
        return UZData.getInstance().getDataList() != null;
    }

    private void playPlaylistPosition(int position) {
        if (!isPlayPlaylistFolder()) {
            Timber.e("playPlaylistPosition error: incorrect position");
            return;
        }
        Timber.d("playPlaylistPosition position: %d", position);
        if (position < 0) {
            Timber.e("This is the first item");
            notifyError(UZExceptionUtil.getExceptionPlaylistFolderItemFirst());
            return;
        }
        if (position > UZData.getInstance().getDataList().size() - 1) {
            Timber.e("This is the last item");
            notifyError(UZExceptionUtil.getExceptionPlaylistFolderItemLast());
            return;
        }
        urlImgThumbnail = null;
        pauseVideo();
        hideController();
        //update UI for skip next and skip previous button
        if (position == 0) {
            setSrcDrawableEnabledForViews(ibSkipPreviousIcon, ibSkipNextIcon);
        } else if (position == UZData.getInstance().getDataList().size() - 1) {
            setSrcDrawableEnabledForViews(ibSkipPreviousIcon, ibSkipNextIcon);
        } else {
            setSrcDrawableEnabledForViews(ibSkipPreviousIcon, ibSkipNextIcon);
        }
        //set disabled prevent double click, will enable onStateReadyFirst()
        setClickableForViews(false, ibSkipPreviousIcon, ibSkipNextIcon);
        //end update UI for skip next and skip previous button
        UZData.getInstance().setCurrentPositionOfDataList(position);
        Data data = UZData.getInstance().getDataWithPositionOfDataList(position);
        if (data == null || data.getId() == null || data.getId().isEmpty()) {
            Timber.e("playPlaylistPosition error: data null or cannot get id");
            return;
        }
        Timber.d("-----------------------> playPlaylistPosition %d", position);
        init(UZData.getInstance().getDataWithPositionOfDataList(position).getId(), false,
                !TextUtils.isEmpty(data.getLastFeedId()));
    }

    private void setSrcDrawableEnabledForViews(UZImageButton... views) {
        for (UZImageButton v : views) {
            if (v != null && !v.isFocused()) {
                v.setSrcDrawableEnabled();
            }
        }
    }

    private void setClickableForViews(boolean able, View... views) {
        for (View v : views) {
            if (v != null) {
                v.setClickable(able);
                v.setFocusable(able);
            }
        }
    }

    private boolean isOnPlayerEnded;

    protected void onPlayerEnded() {
        if (isPlaying()) {
            isOnPlayerEnded = true;
            if (isPlayPlaylistFolder() && isAutoSwitchItemPlaylistFolder) {
                hideController();
                autoSwitchNextVideo();
            } else {
                updateUIEndScreen();
            }
            addTrackingMuiza(Constants.MUIZA_EVENT_VIEWENDED);
        }
    }

    private void autoSwitchNextVideo() {
        playPlaylistPosition(UZData.getInstance().getCurrentPositionOfDataList() + 1);
    }

    private void autoSwitchPreviousLinkVideo() {
        playPlaylistPosition(UZData.getInstance().getCurrentPositionOfDataList() - 1);
    }

    private void handleClickPlaylistFolder() {
        UZDlgPlaylistFolder UZDlgPlaylistFolder = new UZDlgPlaylistFolder(getContext(), isLandscape, UZData.getInstance().getDataList(), UZData.getInstance().getCurrentPositionOfDataList(), new CallbackPlaylistFolder() {
            @Override
            public void onClickItem(Data data, int position) {
                UZData.getInstance().setCurrentPositionOfDataList(position);
                playPlaylistPosition(position);
            }

            @Override
            public void onFocusChange(Data data, int position) {
            }

            @Override
            public void onDismiss() {
            }
        });
        UZUtil.showUizaDialog(getContext(), UZDlgPlaylistFolder);
    }

    private boolean isClickedSkipNextOrSkipPrevious;

    private void handleClickSkipNext() {
        isClickedSkipNextOrSkipPrevious = true;
        isOnPlayerEnded = false;
        updateUIEndScreen();
        autoSwitchNextVideo();
    }

    private void handleClickSkipPrevious() {
        isClickedSkipNextOrSkipPrevious = true;
        isOnPlayerEnded = false;
        updateUIEndScreen();
        autoSwitchPreviousLinkVideo();
    }

    public boolean replay() {
        if (uzPlayerManager == null) {
            return false;
        }
        TmpParamData.getInstance().addPlayerViewCount();
        //TODO Chỗ này đáng lẽ chỉ clear value của tracking khi đảm bảo rằng seekTo(0) true
        setDefaultValueForFlagIsTracked();
        boolean result = uzPlayerManager.seekTo(0);
        if (result) {
            isSetFirstRequestFocusDone = false;
            isOnPlayerEnded = false;
            updateUIEndScreen();
            handlePlayPlayListFolderUI();
            trackUizaEventVideoStarts();
            trackUizaEventDisplay();
            trackUizaEventPlaysRequested();
        }
        if (isCastingChromecast) {
            replayChromeCast();
        }
        return result;
    }

    private void replayChromeCast() {
        lastCurrentPosition = 0;
        handleConnectedChromecast();
        showController();
    }

    //===================================================================END FOR PLAYLIST/FOLDER

    /*Nếu đang casting thì button này sẽ handle volume on/off ở cast player
     * Ngược lại, sẽ handle volume on/off ở exo player*/
    private void handleClickBtVolume() {
        if (isCastingChromecast) {
            boolean isMute = UZData.getInstance().getCasty().toggleMuteVolume();
            if (ibVolumeIcon != null) {
                if (isMute) {
                    ibVolumeIcon.setImageResource(R.drawable.ic_volume_off_black_48dp);
                } else {
                    ibVolumeIcon.setImageResource(R.drawable.baseline_volume_up_white_48);
                }
            }
        } else {
            if (uzPlayerManager != null) {
                uzPlayerManager.toggleVolumeMute(ibVolumeIcon);
            }
        }
    }

    private void handleClickBackScreen() {
        if (isLandscape) {
            toggleFullscreen();
        }
    }

    private void handleClickSetting() {
        View view = UZUtil.getBtVideo(debugRootView);
        ViewUtils.performClick(view);
    }

    private void handleClickCC() {
        if (uzPlayerManager.getSubtitleList() == null || uzPlayerManager.getSubtitleList().isEmpty()) {
            UZDlgInfoV1 uzDlgInfoV1 = new UZDlgInfoV1(getContext(), getContext().getString(R.string.text), getContext().getString(R.string.no_caption));
            UZUtil.showUizaDialog(getContext(), uzDlgInfoV1);
        } else {
            View view = UZUtil.getBtText(debugRootView);
            ViewUtils.performClick(view);
            if (view == null) {
                Timber.e("error handleClickCC null");
            }
        }
    }

    private void handleClickHearing() {
        View view = UZUtil.getBtAudio(debugRootView);
        ViewUtils.performClick(view);
    }

    public void resumeVideo() {
        TmpParamData.getInstance().setPlayerIsPaused(false);
        addTrackingMuiza(Constants.MUIZA_EVENT_PLAY);
        if (isCastingChromecast) {
            UZData.getInstance().getCasty().getPlayer().play();
        } else {
            if (uzPlayerManager != null) {
                uzPlayerManager.resumeVideo();
            }
        }
        ViewUtils.goneViews(ibPlayIcon);
        if (ibPauseIcon != null) {
            ViewUtils.visibleViews(ibPauseIcon);
            ibPauseIcon.requestFocus();
        }
    }

    public void pauseVideo() {
        TmpParamData.getInstance().setPlayerIsPaused(true);
        if (isCastingChromecast) {
            UZData.getInstance().getCasty().getPlayer().pause();
        } else {
            if (uzPlayerManager != null) {
                uzPlayerManager.pauseVideo();
            }
        }
        ViewUtils.goneViews(ibPauseIcon);
        if (ibPlayIcon != null) {
            ViewUtils.visibleViews(ibPlayIcon);
            ibPlayIcon.requestFocus();
        }
        addTrackingMuiza(Constants.MUIZA_EVENT_PAUSE);
    }

    public void setDefaultValueBackwardForward(int mls) {
        DEFAULT_VALUE_BACKWARD_FORWARD = mls;
    }

    public int getDefaultValueBackwardForward() {
        return DEFAULT_VALUE_BACKWARD_FORWARD;
    }

    /*
     ** Seek tu vi tri hien tai cong them bao nhieu mls
     */
    public void seekToForward(int mls) {
        setDefaultValueBackwardForward(mls);
        ViewUtils.performClick(ibFfwdIcon);
    }

    /*
     ** Seek tu vi tri hien tai tru di bao nhieu mls
     */
    public void seekToBackward(int mls) {
        setDefaultValueBackwardForward(mls);
        ViewUtils.performClick(ibRewIcon);
    }

    //chi toggle show hide controller khi video da vao dc onStateReadyFirst();
    public void toggleShowHideController() {
        if (uzPlayerView != null) {
            uzPlayerView.toggleShowHideController();
        }
    }

    public void togglePlayPause() {
        if (uzPlayerManager == null || getPlayer() == null) {
            return;
        }
        if (getPlayer().getPlayWhenReady()) {
            pauseVideo();
        } else {
            resumeVideo();
        }
    }

    public void toggleVolume() {
        ViewUtils.performClick(ibVolumeIcon);
    }

    public void toggleFullscreen() {
        addTrackingMuiza(Constants.MUIZA_EVENT_FULLSCREENCHANGE);
        LActivityUtil.toggleScreenOritation((Activity) getContext());
    }

    public void showCCPopup() {
        ViewUtils.performClick(ibCcIcon);
    }

    public void showHQPopup() {
        ViewUtils.performClick(ibSettingIcon);
    }

    /*
     ** Hiển thị picture in picture và close video view hiện tại
     * Chỉ work nếu local player đang không casting
     * Device phải là tablet
     */
    public void showPip() {
        if (isCastingChromecast()) {
            Timber.e(UZException.ERR_19);
            notifyError(UZExceptionUtil.getExceptionShowPip());
        } else {
            // [Re-check]: Why use performClick?
            // ViewUtils.performClick(ibPictureInPictureIcon);
            handleClickPictureInPicture();
        }
    }

    public void setPipControlSkin(@LayoutRes int skinId) {
        this.pipControlSkin = skinId;
    }

    public void showSpeed() {
        if (getPlayer() == null) {
            return;
        }
        final UZDlgSpeed uzDlgSpeed = new UZDlgSpeed(getContext(), getPlayer().getPlaybackParameters().speed,
                speed -> {
                    if (speed != null) {
                        setSpeed(speed.getValue());
                    }
                });
        UZUtil.showUizaDialog(getContext(), uzDlgSpeed);
    }

    /*
     ** Bo video hien tai va choi tiep theo 1 video trong playlist/folder
     */
    public void skipNextVideo() {
        handleClickSkipNext();
    }

    /*
     * Bo video hien tai va choi lui lai 1 video trong playlist/folder
     */
    public void skipPreviousVideo() {
        handleClickSkipPrevious();
    }

    public PlayerView getUzPlayerView() {
        return uzPlayerView;
    }

    protected TextView getDebugTextView() {
        return debugTextView;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public ImageView getIvThumbnail() {
        return ivThumbnail;
    }

    public boolean isLivestream() {
        return isLivestream;
    }

    public boolean isTablet() {
        return isTablet;
    }

    public IUZPlayerManager getUzPlayerManager() {
        return uzPlayerManager;
    }

    public TextView getTvMsg() {
        return tvMsg;
    }

    public ImageView getIvVideoCover() {
        return ivVideoCover;
    }

    public UZImageButton getIbFullscreenIcon() {
        return ibFullscreenIcon;
    }

    public TextView getTvTitle() {
        return tvTitle;
    }

    public UZImageButton getIbPauseIcon() {
        return ibPauseIcon;
    }

    public UZImageButton getIbPlayIcon() {
        return ibPlayIcon;
    }

    public UZImageButton getIbReplayIcon() {
        return ibReplayIcon;
    }

    public UZImageButton getIbRewIcon() {
        return ibRewIcon;
    }

    public UZImageButton getIbFfwdIcon() {
        return ibFfwdIcon;
    }

    public UZImageButton getIbBackScreenIcon() {
        return ibBackScreenIcon;
    }

    public UZImageButton getIbVolumeIcon() {
        return ibVolumeIcon;
    }

    public UZImageButton getIbSettingIcon() {
        return ibSettingIcon;
    }

    public UZImageButton getIbCcIcon() {
        return ibCcIcon;
    }

    public UZImageButton getIbPlaylistFolderIcon() {
        return ibPlaylistFolderIcon;
    }

    public UZImageButton getIbHearingIcon() {
        return ibHearingIcon;
    }

    public UZImageButton getIbPictureInPictureIcon() {
        return ibPictureInPictureIcon;
    }

    public UZImageButton getIbSkipPreviousIcon() {
        return ibSkipPreviousIcon;
    }

    public UZImageButton getIbSkipNextIcon() {
        return ibSkipNextIcon;
    }

    public UZImageButton getIbSpeedIcon() {
        return ibSpeedIcon;
    }

    public RelativeLayout getRlLiveInfo() {
        return rlLiveInfo;
    }

    public TextView getTvLiveView() {
        return tvLiveView;
    }

    public TextView getTvLiveTime() {
        return tvLiveTime;
    }

    public RelativeLayout getRlChromeCast() {
        return rlChromeCast;
    }

    public UZImageButton getIbsCast() {
        return ibsCast;
    }

    public String getEntityId() {
        return entityId;
    }

    public UZTextView getTvPosition() {
        return tvPosition;
    }

    public UZTextView getTvDuration() {
        return tvDuration;
    }

    public TextView getTvLiveStatus() {
        return tvLiveStatus;
    }

    public UZImageButton getIvLiveTime() {
        return ivLiveTime;
    }

    public UZImageButton getIvLiveView() {
        return ivLiveView;
    }

    public RelativeLayout getRlEndScreen() {
        return rlEndScreen;
    }

    public TextView getTvEndScreenMsg() {
        return tvEndScreenMsg;
    }

    public UZTimebar getUZTimeBar() {
        return uzTimebar;
    }

    public LinearLayout getLlTop() {
        return llTop;
    }

    public View getBkg() {
        return bkg;
    }

    public List<UZItem> getHQList() {
        View view = UZUtil.getBtVideo(debugRootView);
        if (view == null) {
            Timber.e("Error getHQList null");
            notifyError(UZExceptionUtil.getExceptionListHQ());
            return null;
        }
        return showUZTrackSelectionDialog(view, false);
    }

    public List<UZItem> getAudioList() {
        View view = UZUtil.getBtAudio(debugRootView);
        if (view == null) {
            Timber.e("Error audio null");
            notifyError(UZExceptionUtil.getExceptionListAudio());
            return null;
        }
        return showUZTrackSelectionDialog(view, false);
    }

    public void setVolume(float volume) {
        if (uzPlayerManager == null) {
            return;
        }
        uzPlayerManager.setVolume(volume);
    }

    public float getVolume() {
        if (uzPlayerManager == null) {
            return Constants.NOT_FOUND;
        }
        return uzPlayerManager.getVolume();
    }

    @Override
    public void onVisibilityChange(boolean isShow) {
        if (ivLogo != null) {
            ivLogo.setClickable(!isShow);
        }
        if (controllerStateCallback != null) {
            controllerStateCallback.onVisibilityChange(isShow);
        }
    }

    public void setSpeed(float speed) {
        if (getContext() == null) {
            return;
        }
        if (isLivestream) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_speed_live_content));
        }
        if (speed > 3 || speed < -3) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_speed_illegal));
        }
        PlaybackParameters playbackParameters = new PlaybackParameters(speed);
        if (getPlayer() != null) {
            getPlayer().setPlaybackParameters(playbackParameters);
        }
        addTrackingMuiza(Constants.MUIZA_EVENT_RATECHANGE);
    }

    //=============================================================================================START UI
    private void findViews() {
        bkg = findViewById(R.id.bkg);
        rlMsg = findViewById(R.id.rl_msg);
        rlMsg.setOnClickListener(this);
        tvMsg = findViewById(R.id.tv_msg);
        if (tvMsg != null) {
            LUIUtil.setTextShadow(tvMsg);
        }
        ivVideoCover = findViewById(R.id.iv_cover);
        llTop = findViewById(R.id.ll_top);
        progressBar = findViewById(R.id.pb);
        LUIUtil.setColorProgressBar(progressBar, progressBarColor);
        updateUIPositionOfProgressBar();
        uzPlayerView.setControllerStateCallback(this);
        uzTimebar = uzPlayerView.findViewById(R.id.exo_progress);
        previewFrameLayout = uzPlayerView.findViewById(R.id.preview_frame_layout);
        if (uzTimebar != null) {
            if (uzTimebar.getTag() == null) {
                isSetUZTimebarBottom = false;
                uzPlayerView.setVisibility(VISIBLE);
            } else {
                if (uzTimebar.getTag().toString().equals(getContext().getString(R.string.use_bottom_uz_timebar))) {
                    isSetUZTimebarBottom = true;
                    setMarginDependOnUZTimeBar(uzPlayerView.getVideoSurfaceView());
                } else {
                    isSetUZTimebarBottom = false;
                    uzPlayerView.setVisibility(VISIBLE);
                }
            }
            uzTimebar.addOnPreviewChangeListener(this);
            uzTimebar.setOnFocusChangeListener(this);
        } else {
            uzPlayerView.setVisibility(VISIBLE);
        }
        ivThumbnail = uzPlayerView.findViewById(R.id.image_view_thumnail);
        tvPosition = uzPlayerView.findViewById(R.id.uz_position);
        if (tvPosition != null) {
            tvPosition.setText(LDateUtils.convertMlsecondsToHMmSs(0));
        }
        tvDuration = uzPlayerView.findViewById(R.id.uz_duration);
        if (tvDuration != null) {
            tvDuration.setText("-:-");
        }
        ibFullscreenIcon = uzPlayerView.findViewById(R.id.exo_fullscreen_toggle_icon);
        tvTitle = uzPlayerView.findViewById(R.id.tv_title);
        ibPauseIcon = uzPlayerView.findViewById(R.id.exo_pause_uiza);
        ibPlayIcon = uzPlayerView.findViewById(R.id.exo_play_uiza);
        //If auto start true, show button play and gone button pause
        ViewUtils.goneViews(ibPlayIcon);
        ibReplayIcon = uzPlayerView.findViewById(R.id.exo_replay_uiza);
        ibRewIcon = uzPlayerView.findViewById(R.id.exo_rew);
        if (ibRewIcon != null) {
            ibRewIcon.setSrcDrawableDisabled();
        }
        ibFfwdIcon = uzPlayerView.findViewById(R.id.exo_ffwd);
        ibBackScreenIcon = uzPlayerView.findViewById(R.id.exo_back_screen);
        ibVolumeIcon = uzPlayerView.findViewById(R.id.exo_volume);
        ibSettingIcon = uzPlayerView.findViewById(R.id.exo_setting);
        ibCcIcon = uzPlayerView.findViewById(R.id.exo_cc);
        ibPlaylistFolderIcon = uzPlayerView.findViewById(R.id.exo_playlist_folder);
        ibHearingIcon = uzPlayerView.findViewById(R.id.exo_hearing);
        ibPictureInPictureIcon = uzPlayerView.findViewById(R.id.exo_picture_in_picture);
        ibSkipNextIcon = uzPlayerView.findViewById(R.id.exo_skip_next);
        ibSkipPreviousIcon = uzPlayerView.findViewById(R.id.exo_skip_previous);
        ibSpeedIcon = uzPlayerView.findViewById(R.id.exo_speed);
        LinearLayout debugLayout = findViewById(R.id.debug_layout);
        debugRootView = findViewById(R.id.controls_root);
        debugTextView = findViewById(R.id.debug_text_view);
        if (Constants.IS_DEBUG) {
            debugLayout.setVisibility(VISIBLE);
        } else {
            debugLayout.setVisibility(GONE);
            debugTextView = null;
        }
        rlLiveInfo = uzPlayerView.findViewById(R.id.rl_live_info);
        tvLiveStatus = uzPlayerView.findViewById(R.id.tv_live);
        tvLiveView = uzPlayerView.findViewById(R.id.tv_live_view);
        tvLiveTime = uzPlayerView.findViewById(R.id.tv_live_time);
        ivLiveView = uzPlayerView.findViewById(R.id.iv_live_view);
        ivLiveTime = uzPlayerView.findViewById(R.id.iv_live_time);
        ViewUtils.setFocusableViews(false, ivLiveView, ivLiveTime);
        rlEndScreen = uzPlayerView.findViewById(R.id.rl_end_screen);
        ViewUtils.goneViews(rlEndScreen);
        tvEndScreenMsg = uzPlayerView.findViewById(R.id.tv_end_screen_msg);
        if (tvEndScreenMsg != null) {
            LUIUtil.setTextShadow(tvEndScreenMsg, Color.WHITE);
            tvEndScreenMsg.setOnClickListener(this);
        }
        setEventForViews();
        //set visibility first, so scared if removed
        setVisibilityOfPlaylistFolderController(GONE);

        statsForNerdsView = findViewById(R.id.stats_for_nerds);
    }

    private void setEventForViews() {
        setClickAndFocusEventForViews(ibFullscreenIcon, ibBackScreenIcon, ibVolumeIcon, ibSettingIcon,
                ibCcIcon, ibPlaylistFolderIcon, ibHearingIcon, ibPictureInPictureIcon, ibFfwdIcon,
                ibRewIcon, ibPlayIcon, ibPauseIcon, ibReplayIcon, ibSkipNextIcon, ibSkipPreviousIcon, ibSpeedIcon);
    }

    private void setClickAndFocusEventForViews(View... views) {
        for (View v : views) {
            if (v != null) {
                v.setOnClickListener(this);
                v.setOnFocusChangeListener(this);
            }
        }
    }

    //If auto start true, show button play and gone button pause
    //if not, gone button play and show button pause
    private void updateUIButtonPlayPauseDependOnIsAutoStart() {
        if (isAutoStart) {
            ViewUtils.goneViews(ibPlayIcon);
            if (ibPauseIcon != null) {
                ViewUtils.visibleViews(ibPauseIcon);
                if (!isSetFirstRequestFocusDone) {
                    ibPauseIcon.requestFocus();//set first request focus if using player for TV
                    isSetFirstRequestFocusDone = true;
                }
            }
        } else {
            if (isPlaying()) {
                ViewUtils.goneViews(ibPlayIcon);
                if (ibPauseIcon != null) {
                    ViewUtils.visibleViews(ibPauseIcon);
                    if (!isSetFirstRequestFocusDone) {
                        ibPauseIcon.requestFocus();//set first request focus if using player for TV
                        isSetFirstRequestFocusDone = true;
                    }
                }
            } else {
                if (ibPlayIcon != null) {
                    ViewUtils.visibleViews(ibPlayIcon);
                    if (!isSetFirstRequestFocusDone) {
                        ibPlayIcon.requestFocus();//set first request focus if using player for TV
                        isSetFirstRequestFocusDone = true;
                    }
                }
                ViewUtils.goneViews(ibPauseIcon);
            }
        }
    }

    private String urlImgThumbnail;

    public void setUrlImgThumbnail(String urlImgThumbnail) {
        if (urlImgThumbnail == null || urlImgThumbnail.isEmpty()) {
            return;
        }
        this.urlImgThumbnail = urlImgThumbnail;
        if (ivVideoCover == null) {
            return;
        }
        if (ivVideoCover.getVisibility() != VISIBLE) {
            ivVideoCover.setVisibility(VISIBLE);
            LImageUtil.load(getContext(), urlImgThumbnail, ivVideoCover, R.drawable.background_black);
        }
    }

    private void setVideoCover() {
        if (ivVideoCover.getVisibility() != VISIBLE) {
            resetCountTryLinkPlayError();
            ivVideoCover.setVisibility(VISIBLE);
            ivVideoCover.invalidate();
            String urlCover;
            if (urlImgThumbnail == null || urlImgThumbnail.isEmpty()) {
                if (playback == null) {
                    urlCover = Constants.URL_IMG_THUMBNAIL_BLACK;
                } else {
                    urlCover = Constants.URL_IMG_THUMBNAIL_BLACK; //entity.getThumbnail();
                }
            } else {
                urlCover = urlImgThumbnail;
            }
            TmpParamData.getInstance().setEntityPosterUrl(urlCover);
            LImageUtil.load(getContext(), urlCover, ivVideoCover, R.drawable.background_black);
        }
    }

    protected void removeVideoCover(boolean isFromHandleError) {
        if (ivVideoCover.getVisibility() != GONE) {
            ivVideoCover.setVisibility(GONE);
            ivVideoCover.invalidate();
            if (isLivestream) {
                if (tvLiveTime != null) {
                    tvLiveTime.setText(HYPHEN);
                }
                if (tvLiveView != null) {
                    tvLiveView.setText(HYPHEN);
                }
                callAPIUpdateLiveInfoCurrentView(DELAY_FIRST_TO_GET_LIVE_INFORMATION);
                callAPIUpdateLiveInfoTimeStartLive(DELAY_FIRST_TO_GET_LIVE_INFORMATION);
            }
            if (!isFromHandleError) {
                onStateReadyFirst();
            }
        } else {
            //goi changeskin realtime thi no ko vao if nen ko update tvDuration dc
            updateTvDuration();
        }
    }

    private void updateUIEachSkin() {
        int currentPlayerId = UZData.getInstance().getCurrentPlayerId();
        if (currentPlayerId == R.layout.uz_player_skin_2 || currentPlayerId == R.layout.uz_player_skin_3) {
            if (ibPlayIcon != null) {
                ibPlayIcon.setRatioLand(7);
                ibPlayIcon.setRatioPort(5);
            }
            if (ibPauseIcon != null) {
                ibPauseIcon.setRatioLand(7);
                ibPauseIcon.setRatioPort(5);
            }
            if (ibReplayIcon != null) {
                ibReplayIcon.setRatioLand(7);
                ibReplayIcon.setRatioPort(5);
            }
        }
    }

    private void updateUIPositionOfProgressBar() {
        if (uzPlayerView == null || progressBar == null) {
            return;
        }
        uzPlayerView.post(() -> {
            int marginL = uzPlayerView.getMeasuredWidth() / 2 - progressBar.getMeasuredWidth() / 2;
            int marginT = uzPlayerView.getMeasuredHeight() / 2 - progressBar.getMeasuredHeight() / 2;
            LUIUtil.setMarginPx(progressBar, marginL, marginT, 0, 0);
        });
    }

    private void addPlayerView() {
        uzPlayerView = null;
        int skinId = UZData.getInstance().getCurrentPlayerId();
        uzPlayerView = (UZPlayerView) ((Activity) getContext()).getLayoutInflater().inflate(skinId, null);
        setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        uzPlayerView.setLayoutParams(lp);
        uzPlayerView.setVisibility(GONE);
        rootView.addView(uzPlayerView);
        setControllerAutoShow(isAutoShowController);
    }

    /*
     **Change skin via skin id resources
     * changeSkin(R.layout.uz_player_skin_1);
     */
    //TODO improve this func
    private boolean isRefreshFromChangeSkin;
    private long currentPositionBeforeChangeSkin;
    private boolean isCalledFromChangeSkin;

    /*
     ** change skin of player (realtime)
     * return true if success
     */
    public boolean changeSkin(@LayoutRes int skinId) {
        if (getContext() == null || uzPlayerManager == null) {
            return false;
        }
        if (UZData.getInstance().isUseWithVDHView()) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_change_skin_with_vdhview));
        }
        if (uzPlayerManager.isPlayingAd()) {
            notifyError(UZExceptionUtil.getExceptionChangeSkin());
            return false;
        }
        UZUtil.setCurrentPlayerId(skinId);
        isRefreshFromChangeSkin = true;
        isCalledFromChangeSkin = true;
        rootView.removeView(uzPlayerView);
        rootView.requestLayout();
        uzPlayerView = (UZPlayerView) ((Activity) getContext()).getLayoutInflater().inflate(skinId, null);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        uzPlayerView.setLayoutParams(lp);
        rootView.addView(uzPlayerView);
        rootView.requestLayout();
        findViews();
        resizeContainerView();
        updateUIEachSkin();
        setMarginPreviewTimeBar();
        setMarginRlLiveInfo();
        //setup chromecast
        if (isCastDependencyAvailable()) {
            setupChromeCast();
        }
        currentPositionBeforeChangeSkin = getCurrentPosition();
        releaseUzPlayerManager();
        updateUIDependOnLivestream();
        setTitle();
        checkToSetUpResource();
        updateUISizeThumbnail();
        if (uzCallback != null) {
            uzCallback.onSkinChange();
        }
        return true;
    }

    private void setupChromeCast() {
        uZChromeCast = new UZChromeCast();
        uZChromeCast.setUZChromeCastListener(new UZChromeCast.UZChromeCastListener() {
            @Override
            public void onConnected() {
                if (uzPlayerManager != null) {
                    lastCurrentPosition = getCurrentPosition();
                }
                handleConnectedChromecast();
            }

            @Override
            public void onDisconnected() {
                handleDisconnectedChromecast();
            }

            @Override
            public void addUIChromecast() {
                if (llTop != null) {
                    llTop.addView(uZChromeCast.getUzMediaRouteButton());
                }
                addUIChromecastLayer();
            }
        });
        uZChromeCast.setupChromeCast(getContext(), isTV);
    }

    private void updateTvDuration() {
        if (tvDuration != null) {
            if (isLivestream) {
                tvDuration.setText(LDateUtils.convertMlsecondsToHMmSs(0));
            } else {
                tvDuration.setText(LDateUtils.convertMlsecondsToHMmSs(getDuration()));
            }
        }
    }

    public void setProgressSeekbar(final UZVerticalSeekBar uzVerticalSeekBar, final int progressSeekbar) {
        if (uzVerticalSeekBar == null) {
            return;
        }
        uzVerticalSeekBar.setProgress(progressSeekbar);
    }

    private void setTextPosition(long currentMls) {
        if (tvPosition == null) return;
        if (isLivestream) {
            long duration = getDuration();
            long past = duration - currentMls;
            tvPosition.setText(HYPHEN + LDateUtils.convertMlsecondsToHMmSs(past));
        } else {
            tvPosition.setText(LDateUtils.convertMlsecondsToHMmSs(currentMls));
        }
    }

    private void updateUIIbRewIconDependOnProgress(long currentMls, boolean isCalledFromUZTimebarEvent) {
        if (isCalledFromUZTimebarEvent) {
            setTextPosition(currentMls);
        } else {
            if (isOnPreview) {//uzTimebar is displaying
                return;
            } else {
                setTextPosition(currentMls);
            }
        }
        if (isLivestream) return;
        if (ibRewIcon != null && ibFfwdIcon != null) {
            if (currentMls == 0) {
                if (ibRewIcon.isSetSrcDrawableEnabled()) {
                    ibRewIcon.setSrcDrawableDisabled();
                }
                if (!ibFfwdIcon.isSetSrcDrawableEnabled()) {
                    ibFfwdIcon.setSrcDrawableEnabled();
                }
            } else if (currentMls == getDuration()) {
                if (!ibRewIcon.isSetSrcDrawableEnabled()) {
                    ibRewIcon.setSrcDrawableEnabled();
                }
                if (ibFfwdIcon.isSetSrcDrawableEnabled()) {
                    ibFfwdIcon.setSrcDrawableDisabled();
                }
            } else {
                if (!ibRewIcon.isSetSrcDrawableEnabled()) {
                    ibRewIcon.setSrcDrawableEnabled();
                }
                if (!ibFfwdIcon.isSetSrcDrawableEnabled()) {
                    ibFfwdIcon.setSrcDrawableEnabled();
                }
            }
        }
    }

    //FOR TV
    public void updateUIFocusChange(View view, boolean isFocus) {
        if (view == null) {
            return;
        }
        if (isFocus) {
            if (view instanceof UZImageButton) {
                UZUtil.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus, R.drawable.bkg_tv_no_focus);
                ((UZImageButton) view).setColorFilter(Color.GRAY);
            } else if (view instanceof Button) {
                UZUtil.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus, R.drawable.bkg_tv_no_focus);
            } else if (view instanceof UZTimebar) {
                UZUtil.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus_uz_timebar, R.drawable.bkg_tv_no_focus_uz_timebar);
            }
        } else {
            if (view instanceof UZImageButton) {
                UZUtil.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus, R.drawable.bkg_tv_no_focus);
                ((UZImageButton) view).clearColorFilter();
            } else if (view instanceof Button) {
                UZUtil.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus, R.drawable.bkg_tv_no_focus);
            } else if (view instanceof UZTimebar) {
                UZUtil.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus_uz_timebar, R.drawable.bkg_tv_no_focus_uz_timebar);
            }
        }
    }

    private View firstViewHasFocus;

    private void handleFirstViewHasFocus() {
        if (firstViewHasFocus != null && uztvCallback != null) {
            uztvCallback.onFocusChange(firstViewHasFocus, true);
            firstViewHasFocus = null;
        }
    }

    private void updateUISizeThumbnail() {
        int screenWidth = LScreenUtil.getScreenWidth();
        int widthIv = isLandscape ? screenWidth / 4 : screenWidth / 5;
        if (previewFrameLayout != null) {
            RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.width = widthIv;
            layoutParams.height = (int) (widthIv * Constants.RATIO_9_16);
            previewFrameLayout.setLayoutParams(layoutParams);
            previewFrameLayout.requestLayout();
        }
    }

    private void setMarginPreviewTimeBar() {
        if (isLandscape) {
            LUIUtil.setMarginDimen(uzTimebar, 5, 0, 5, 0);
        } else {
            LUIUtil.setMarginDimen(uzTimebar, 0, 0, 0, 0);
        }
    }

    private void setMarginRlLiveInfo() {
        if (isLandscape) {
            LUIUtil.setMarginDimen(rlLiveInfo, 50, 0, 50, 0);
        } else {
            LUIUtil.setMarginDimen(rlLiveInfo, 5, 0, 5, 0);
        }
    }

    private void setTitle() {
        if (tvTitle != null) {
            tvTitle.setText(UZData.getInstance().getEntityName());
        }
    }

    private void updateUIDependOnLivestream() {
        if (isCastingChromecast) {
            ViewUtils.goneViews(ibPictureInPictureIcon);
        } else {
            if (isTablet && isTV) {//only hide ibPictureInPictureIcon if device is TV
                ViewUtils.goneViews(ibPictureInPictureIcon);
            }
        }
        if (isLivestream) {
            ViewUtils.visibleViews(rlLiveInfo);
            //TODO why set gone not work?
            setUIVisible(false, ibSpeedIcon, ibRewIcon, ibFfwdIcon);
        } else {
            ViewUtils.goneViews(rlLiveInfo);
            //TODO why set visible not work?
            setUIVisible(true, ibSpeedIcon, ibRewIcon, ibFfwdIcon);
        }
        if (isTV) {
            ViewUtils.goneViews(ibFullscreenIcon);
        }
    }

    private void setUIVisible(boolean visible, UZImageButton... views) {
        for (UZImageButton v : views) {
            if (v != null) {
                v.setUIVisible(visible);
            }
        }
    }

    protected void updateUIButtonVisibilities() {
        if (debugRootView != null) {
            debugRootView.removeAllViews();
        }
        if (getPlayer() == null) {
            return;
        }
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = uzPlayerManager.getTrackSelector().getCurrentMappedTrackInfo();
        if (mappedTrackInfo == null) {
            return;
        }
        for (int i = 0; i < mappedTrackInfo.length; i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                if (getContext() == null) {
                    return;
                }
                Button button = new Button(getContext());
                button.setSoundEffectsEnabled(false);
                int label;
                switch (uzPlayerManager.getPlayer().getRendererType(i)) {
                    case C.TRACK_TYPE_AUDIO:
                        label = R.string.audio;
                        break;
                    case C.TRACK_TYPE_VIDEO:
                        label = R.string.video;
                        break;
                    case C.TRACK_TYPE_TEXT:
                        label = R.string.text;
                        break;
                    default:
                        continue;
                }
                button.setText(label);
                button.setTag(i);
                button.setOnClickListener(this);
                if (debugRootView != null) {
                    debugRootView.addView(button);
                }
            }
        }
    }

    public void showLayoutMsg() {
        hideController();
        ViewUtils.visibleViews(rlMsg);
    }

    public void hideLayoutMsg() {
        ViewUtils.goneViews(rlMsg);
    }

    private void updateLiveInfoTimeStartLive() {
        if (!isLivestream || getContext() == null) {
            return;
        }
        long now = System.currentTimeMillis();
        long duration = now - startTime;
        String s = LDateUtils.convertMlsecondsToHMmSs(duration);
        if (tvLiveTime != null) {
            tvLiveTime.setText(s);
        }
        if (uzLiveContentCallback != null) {
            uzLiveContentCallback.onUpdateLiveInfoTimeStartLive(duration, s);
        }
        callAPIUpdateLiveInfoTimeStartLive(DELAY_TO_GET_LIVE_INFORMATION);
    }

    private void updateUIEndScreen() {
        if (getContext() == null) {
            return;
        }
        if (isOnPlayerEnded) {
            Timber.i("Video or Stream is ended !");
            setVisibilityOfPlayPauseReplay(true);
            showController();
            if (uzPlayerView != null) {
                uzPlayerView.setControllerShowTimeoutMs(0);
                uzPlayerView.setControllerHideOnTouch(false);
            }
        } else {
            setVisibilityOfPlayPauseReplay(false);
            if (uzPlayerView != null) {
                uzPlayerView.setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT);
            }
            setHideControllerOnTouch(isHideOnTouch);
        }
    }

    private void setVisibilityOfPlayPauseReplay(boolean isShowReplay) {
        if (isShowReplay) {
            ViewUtils.goneViews(ibPlayIcon, ibPauseIcon);
            if (ibReplayIcon != null) {
                ViewUtils.visibleViews(ibReplayIcon);
                ibReplayIcon.requestFocus();
            }
        } else {
            updateUIButtonPlayPauseDependOnIsAutoStart();
            ViewUtils.goneViews(ibReplayIcon);
        }
    }

    private void setVisibilityOfPlaylistFolderController(int visibilityOfPlaylistFolderController) {
        ViewUtils.setVisibilityViews(visibilityOfPlaylistFolderController, ibPlaylistFolderIcon,
                ibSkipNextIcon, ibSkipPreviousIcon);
        //Có play kiểu gì đi nữa thì cũng phải ibPlayIcon GONE và ibPauseIcon VISIBLE và ibReplayIcon GONE
        setVisibilityOfPlayPauseReplay(false);
    }

    public void hideUzTimebar() {
        ViewUtils.goneViews(previewFrameLayout, ivThumbnail, uzTimebar);
    }

    private List<UZItem> showUZTrackSelectionDialog(final View view, boolean showDialog) {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = uzPlayerManager.getTrackSelector().getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            CharSequence title = ((Button) view).getText();
            int rendererIndex = (int) view.getTag();
            boolean allowAdaptiveSelections = false;
            final Pair<AlertDialog, UZTrackSelectionView> dialogPair = UZTrackSelectionView.getDialog(getContext(), title, uzPlayerManager.getTrackSelector(), rendererIndex);
            dialogPair.second.setShowDisableOption(false);
            dialogPair.second.setAllowAdaptiveSelections(allowAdaptiveSelections);
            dialogPair.second.setCallback(() -> handler.postDelayed(() -> {
                {
                    if (dialogPair.first == null) {
                        return;
                    }
                    dialogPair.first.cancel();
                }
            }, 300));
            if (showDialog) {
                UZUtil.showUizaDialog(getContext(), dialogPair.first);
            }
            return dialogPair.second.getUZItemList();
        }
        return null;
    }

    public void setUzTimebarBottom() {
        if (uzPlayerView == null) {
            throw new NullPointerException("uzPlayerView cannot be null");
        }
        if (uzTimebar == null) {
            throw new NullPointerException("uzTimebar cannot be null");
        }
        if (uzPlayerView.getResizeMode() != AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT) {
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
        }
    }

    public int getHeightUZVideo() {
        if (rootView == null) {
            return 0;
        }
        if (isSetUZTimebarBottom) {
            int hRootView = LUIUtil.getHeightOfView(rootView);
            int hUZTimebar = getHeightUZTimeBar();
            return hRootView - hUZTimebar / 2;
        } else {
            return LUIUtil.getHeightOfView(rootView);
        }
    }

    public void setBackgroundColorBkg(int color) {
        if (bkg != null) {
            bkg.setBackgroundColor(color);
        }
    }

    public void setBackgroundColorUZVideoRootView(int color) {
        RelativeLayout uzVideoRootView = findViewById(R.id.root_view_uz_video);
        if (uzVideoRootView != null) {
            uzVideoRootView.setBackgroundColor(color);
        }
    }

    public void setMarginDependOnUZTimeBar(View view) {
        if (view == null || uzTimebar == null) {
            return;
        }
        int heightUZTimebar;
        if (isLandscape) {
            LUIUtil.setMarginPx(view, 0, 0, 0, 0);
        } else {
            heightUZTimebar = getHeightUZTimeBar();
            LUIUtil.setMarginPx(view, 0, 0, 0, heightUZTimebar / 2);
        }
    }

    private int progressBarColor = Color.WHITE;

    public void setProgressBarColor(int progressBarColor) {
        if (progressBar != null) {
            this.progressBarColor = progressBarColor;
            LUIUtil.setColorProgressBar(progressBar, progressBarColor);
        }
    }

    public void hideProgress() {
        if (uzPlayerManager != null) {
            uzPlayerManager.hideProgress();
        }
    }

    public void showProgress() {
        if (uzPlayerManager != null) {
            uzPlayerManager.showProgress();
        }
    }

    private void updateUIPlayerInfo() {
        if (playerInfor == null || uzPlayerView == null) {
            return;
        }
        vn.uiza.restapi.model.v4.playerinfo.Data data = playerInfor.getData();
        if (data == null) {
            return;
        }
        Logo logo = data.getLogo();
        if (logo == null) {
            return;
        }
        boolean isDisplay = logo.isDisplay();
        if (isDisplay) {
            String position = logo.getPosition();
            ivLogo = new ImageView(getContext());
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ConvertUtils.dp2px(50f), ViewGroup.LayoutParams.WRAP_CONTENT);
            if (position.equalsIgnoreCase("top-left")) {
                layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            } else if (position.equalsIgnoreCase("top-right")) {
                layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
            } else if (position.equalsIgnoreCase("bottom-left")) {
                layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
            } else if (position.equalsIgnoreCase("bottom-right")) {
                layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            } else {
                layoutParams = null;
                ivLogo = null;
                return;
            }
            uzPlayerView.getOverlayFrameLayout().addView(ivLogo, layoutParams);
            ivLogo.setOnClickListener(this);
            LImageUtil.load(getContext(), logo.getLogo(), ivLogo);
        } else {
            ivLogo = null;
        }
    }

    //=============================================================================================END UI
    //=============================================================================================START EVENT
    private UZLiveContentCallback uzLiveContentCallback;
    private ProgressCallback progressCallback;
    private CallbackUZTimebar callbackUZTimebar;
    private UZItemClick uzItemClick;
    private UZCallback uzCallback;
    private UZTVCallback uztvCallback;
    private UZPlayerView.ControllerStateCallback controllerStateCallback;
    protected AudioListener audioListener;
    protected MetadataOutput metadataOutput;
    protected Player.EventListener eventListener;
    protected VideoListener videoListener;
    protected TextOutput textOutput;

    public void addUZLiveContentCallback(UZLiveContentCallback uzLiveContentCallback) {
        this.uzLiveContentCallback = uzLiveContentCallback;
    }

    public void addUZCallback(UZCallback uzCallback) {
        this.uzCallback = uzCallback;
    }

    public void addUZTVCallback(UZTVCallback uztvCallback) {
        this.uztvCallback = uztvCallback;
        handleFirstViewHasFocus();
    }

    public void addProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public void addCallbackUZTimebar(CallbackUZTimebar callbackUZTimebar) {
        this.callbackUZTimebar = callbackUZTimebar;
    }

    public void addItemClick(UZItemClick uzItemClick) {
        this.uzItemClick = uzItemClick;
    }

    public void addControllerStateCallback(final UZPlayerView.ControllerStateCallback controllerStateCallback) {
        this.controllerStateCallback = controllerStateCallback;
    }

    public void addOnTouchEvent(UZPlayerView.OnTouchEvent onTouchEvent) {
        if (uzPlayerView != null) {
            uzPlayerView.setOnTouchEvent(onTouchEvent);
        }
    }

    public void addAudioListener(AudioListener audioListener) {
        this.audioListener = audioListener;
    }

    public void addPlayerEventListener(Player.EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void addVideoListener(VideoListener videoListener) {
        this.videoListener = videoListener;
    }

    public void addMetadataOutput(MetadataOutput metadataOutput) {
        this.metadataOutput = metadataOutput;
    }

    public void addTextOutput(TextOutput textOutput) {
        this.textOutput = textOutput;
    }

    //=============================================================================================END EVENT
    //=============================================================================================START CALL API
    private boolean isCalledApiGetDetailEntity;
    private boolean isCalledAPIGetUrlIMAAdTag;
    private boolean isCalledAPIGetTokenStreaming;
    private UizaPlayback playback;

    private void callAPIGetDetailEntity() {
        //Neu da ton tai Data roi thi no duoc goi tu pip, minh ko can phai call api lay detail entity lam gi nua
        boolean isDataExist = UZData.getInstance().getPlayback() != null;
        if (isDataExist) {
            //init player khi user click vào fullscreen của floating view (pic)
            isCalledApiGetDetailEntity = true;
            playback = UZData.getInstance().getPlayback();
            handleDataCallAPI();
        } else {
            if (isLivestream) {
                UizaLiveService liveService = UizaClientFactory.getLiveService();
                RxBinder.bind(liveService.getEntity(entityId).map(LiveEntity::getUizaPlayback), data -> {
                    if (data.canPlay())
                        handleDetailEntityResponse(data);
                    else {
                        UZData.getInstance().setSettingPlayer(false);
                        handleError(UZExceptionUtil.getExceptionCannotGetDetailEntitity());
                    }
                }, throwable -> {
                    UZData.getInstance().setSettingPlayer(false);
                    handleError(UZExceptionUtil.getExceptionCannotGetDetailEntitity());
                });
            } else {
                UizaVodService vodService = UizaClientFactory.getVideoService();
                RxBinder.bind(vodService.getEntity(entityId).map(VODEntity::getUizaPlayback), data -> {
                    if (data.canPlay())
                        handleDetailEntityResponse(data);
                    else {
                        UZData.getInstance().setSettingPlayer(false);
                        handleError(UZExceptionUtil.getExceptionCannotGetDetailEntitity());
                    }
                }, throwable -> {
                    UZData.getInstance().setSettingPlayer(false);
                    handleError(UZExceptionUtil.getExceptionCannotGetDetailEntitity());
                });
            }
        }
    }

    private void handleDetailEntityResponse(UizaPlayback playback) {
        isCalledApiGetDetailEntity = true;
        this.playback = playback;
        //set video cover o moi case, ngoai tru
        //click tu pip entity thi ko can show video cover
        //click tu pip playlist folder lan dau tien thi ko can show video cover, neu nhan skip next hoac skip prev thi se show video cover
        if (isPlayPlaylistFolder()) {
            if (isGetClickedPip) {
                if (isClickedSkipNextOrSkipPrevious) {
                    setVideoCover();
                }
            } else {
                setVideoCover();
            }
        } else {
            setVideoCover();
        }
        handleDataCallAPI();
    }

    private void callAPIGetPlayerInfor() {
        int resLayout = UZData.getInstance().getCurrentPlayerId();
        if (resLayout != R.layout.uz_player_skin_0 && resLayout != R.layout.uz_player_skin_1 && resLayout != R.layout.uz_player_skin_2 && resLayout != R.layout.uz_player_skin_3) {
            return;
        }
        UZService service = UizaClientFactory.getUizaService();
        String playerInforId = UZData.getInstance().getPlayerInforId();
        //LLog.d(TAG, "callAPIGetConfigSkin -> call api -> playerInforId: " + playerInforId);
        if (playerInforId == null || playerInforId.isEmpty()) {
            return;
        }
        RxBinder.bind(service.getPlayerInfo(UZData.getInstance().getAPIVersion(), playerInforId, UZData.getInstance().getAppId()),
                pi -> playerInfor = pi,
                throwable -> handleError(UZExceptionUtil.getExceptionPlayerInfor()));
    }

    private void callAPIGetUrlIMAAdTag() {
        boolean isUrlIMAAdExist = UZData.getInstance().getUrlIMAAd() != null;
        if (isUrlIMAAdExist) {
            isCalledAPIGetUrlIMAAdTag = true;
            urlIMAAd = UZData.getInstance().getUrlIMAAd();
            handleDataCallAPI();
        } else {
            UZService service = UizaClientFactory.getUizaService();
            RxBinder.bind(service.getCuePoint(UZData.getInstance().getAPIVersion(), entityId, UZData.getInstance().getAppId()), result -> {
                isCalledAPIGetUrlIMAAdTag = true;
                if (result == null || result.getData() == null || result.getData().isEmpty()) {
                    urlIMAAd = "";
                } else {
                    //Hien tai chi co the play ima ad o item thu 0
                    Ad ad = result.getData().get(0);
                    if (ad != null) {
                        urlIMAAd = ad.getLink();
                    }
                }
                handleDataCallAPI();
            }, throwable -> Timber.e(throwable, "callAPIGetUrlIMAAdTag onFail but ignored (dont care):"));
        }
    }

    private void callAPIGetTokenStreaming() {
        boolean isGetTokenStreamingExist = UZData.getInstance().getResultGetTokenStreaming() != null;
        if (isGetTokenStreamingExist) {
            mResultGetTokenStreaming = UZData.getInstance().getResultGetTokenStreaming();
            isCalledAPIGetTokenStreaming = true;
            handleDataCallAPI();
        } else {
            UZService service = UizaClientFactory.getUizaService();
            SendGetTokenStreaming sendGetTokenStreaming = new SendGetTokenStreaming();
            sendGetTokenStreaming.setAppId(UZData.getInstance().getAppId());
            sendGetTokenStreaming.setEntityId(entityId);
            sendGetTokenStreaming.setContentType(SendGetTokenStreaming.STREAM);
            RxBinder.bind(service.getTokenStreaming(UZData.getInstance().getAPIVersion(), sendGetTokenStreaming),
                    resultGetTokenStreaming -> {
                        if (resultGetTokenStreaming == null
                                || resultGetTokenStreaming.getData() == null
                                || resultGetTokenStreaming.getData().getToken() == null
                                || resultGetTokenStreaming.getData().getToken().isEmpty()) {
                            handleError(UZExceptionUtil.getExceptionNoTokenStreaming());
                            return;
                        }
                        mResultGetTokenStreaming = resultGetTokenStreaming;
                        isCalledAPIGetTokenStreaming = true;
                        handleDataCallAPI();
                    }, throwable -> {
                        Timber.e(throwable, "callAPIGetTokenStreaming onFail");
                        handleError(UZExceptionUtil.getExceptionNoTokenStreaming());
                    });
        }
    }

    private void callAPIGetLinkPlay() {
        if (mResultGetTokenStreaming == null || mResultGetTokenStreaming.getData() == null || mResultGetTokenStreaming.getData().getToken() == null) {
            handleError(UZExceptionUtil.getExceptionNoTokenStreaming());
            return;
        }
        boolean isResultGetLinkPlayExist = UZData.getInstance().getResultGetLinkPlay() != null;
        if (isResultGetLinkPlayExist) {
            mResultGetLinkPlay = UZData.getInstance().getResultGetLinkPlay();
            try {
                cdnHost = mResultGetLinkPlay.getData().getCdn().get(0).getHost();
            } catch (NullPointerException e) {
                Timber.e(e, "Error cannot find cdnHost");
                SentryUtils.captureException(e);
            }
            checkToSetUpResource();
        } else {
            String tokenStreaming = mResultGetTokenStreaming.getData().getToken();
            UizaClientFactory.getLinkPlayClient().addAuthorization(tokenStreaming);
            UizaLinkPlayService service = UizaClientFactory.getLinkPlayService();
            if (isLivestream) {
                String appId = UZData.getInstance().getAppId();
                String channelName = UZData.getInstance().getChannelName();
                RxBinder.bind(service.getLinkPlayLive(appId, channelName), resultGetLinkPlay -> {
                    handleLinkPlayResponse(resultGetLinkPlay);
                    checkToSetUpResource();
                }, throwable -> {
                    Timber.e(throwable, "getLinkPlayLive LIVE onFail");
                    handleError(UZExceptionUtil.getExceptionCannotGetLinkPlayLive());
                });
            } else {
                String appId = UZData.getInstance().getAppId();
                String typeContent = SendGetTokenStreaming.STREAM;
                RxBinder.bind(service.getLinkPlay(appId, entityId, typeContent), resultGetLinkPlay -> {
                    handleLinkPlayResponse(resultGetLinkPlay);
                    callAPIGetSubtitles();
                }, throwable -> {
                    Timber.e(throwable, "callAPIGetLinkPlay VOD onFail");
                    handleError(UZExceptionUtil.getExceptionCannotGetLinkPlayVOD());
                });
            }
        }
    }

    private void handleLinkPlayResponse(ResultGetLinkPlay resultGetLinkPlay) {
        mResultGetLinkPlay = resultGetLinkPlay;
        UZData.getInstance().setResultGetLinkPlay(resultGetLinkPlay);
        try {
            cdnHost = mResultGetLinkPlay.getData().getCdn().get(0).getHost();
            TmpParamData.getInstance().setEntityCnd(cdnHost);
            TmpParamData.getInstance().setEntitySourceDomain(cdnHost);
            TmpParamData.getInstance().setEntitySourceHostname(cdnHost);
        } catch (NullPointerException e) {
            Timber.e(e, "Error cannot find cdnHost");
            SentryUtils.captureException(e);
        }
    }

    private void callAPIGetSubtitles() {
        if (mResultGetLinkPlay.getData() == null) return;

        UZService service = UizaClientFactory.getUizaService();
        RxBinder.bind(service.getSubtitles(UZData.getInstance().getAPIVersion(),
                mResultGetLinkPlay.getData().getEntityId(),
                mResultGetLinkPlay.getData().getAppId()),
                result -> {
                    subtitleList.clear();
                    subtitleList.addAll(result.getData());
                    checkToSetUpResource();
                }, throwable -> checkToSetUpResource());
    }

    private void callAPIUpdateLiveInfoCurrentView(final int durationDelay) {
        if (!isLivestream || getContext() == null || activityIsPausing) {
            return;
        }
        handler.postDelayed(() -> {
            if (uzPlayerManager != null && uzPlayerView != null && (uzPlayerView.isControllerVisible() || durationDelay == DELAY_FIRST_TO_GET_LIVE_INFORMATION)) {
                UZService service = UizaClientFactory.getUizaService();
                String id = UZData.getInstance().getEntityId();
                RxBinder.bind(service.getViewALiveFeed(UZData.getInstance().getAPIVersion(), id, UZData.getInstance().getAppId()),
                        result -> {
                            if (result != null && result.getData() != null) {
                                if (tvLiveView != null) {
                                    tvLiveView.setText(String.valueOf(result.getData().getWatchnow()));
                                }
                                if (uzLiveContentCallback != null) {
                                    uzLiveContentCallback.onUpdateLiveInfoCurrentView(result.getData().getWatchnow());
                                }
                            }
                            callAPIUpdateLiveInfoCurrentView(DELAY_TO_GET_LIVE_INFORMATION);
                        }, throwable -> {
                            Timber.e(throwable, "getViewALiveFeed onFail");
                            callAPIUpdateLiveInfoCurrentView(DELAY_TO_GET_LIVE_INFORMATION);
                        });
            } else {
                callAPIUpdateLiveInfoCurrentView(DELAY_TO_GET_LIVE_INFORMATION);
            }
        }, durationDelay);
    }

    private void callAPIUpdateLiveInfoTimeStartLive(final int durationDelay) {
        if (!isLivestream || getContext() == null || activityIsPausing) {
            return;
        }
        handler.postDelayed(() -> {
            if (uzPlayerManager != null && uzPlayerView != null && (uzPlayerView.isControllerVisible() || durationDelay == DELAY_FIRST_TO_GET_LIVE_INFORMATION)) {
                if (startTime == Constants.UNKNOW) {
                    UZService service = UizaClientFactory.getUizaService();
                    String entityId = UZData.getInstance().getEntityId();
                    String feedId = UZData.getInstance().getLastFeedId();
                    RxBinder.bind(service.getTimeStartLive(UZData.getInstance().getAPIVersion(), entityId, feedId, UZData.getInstance().getAppId()),
                            result -> {
                                if (result != null && result.getData() != null && result.getData().getStartTime() != null) {
                                    startTime = LDateUtils.convertDateToTimeStamp(result.getData().getStartTime(), LDateUtils.FORMAT_1);
                                    updateLiveInfoTimeStartLive();
                                }
                            }, throwable -> {
                                Timber.e(throwable, "getTimeStartLive onFail");
                                callAPIUpdateLiveInfoTimeStartLive(DELAY_TO_GET_LIVE_INFORMATION);
                            });
                } else {
                    updateLiveInfoTimeStartLive();
                }
            } else {
                callAPIUpdateLiveInfoTimeStartLive(DELAY_TO_GET_LIVE_INFORMATION);
            }
        }, durationDelay);
    }
    //=============================================================================================END CALL API

    private void handleDataCallAPI() {
        if (isCalledApiGetDetailEntity && isCalledAPIGetUrlIMAAdTag && isCalledAPIGetTokenStreaming) {
            if (UZData.getInstance().getUzInput() == null) {
                UZInput uzInput = new UZInput();
                uzInput.setPlayback(playback);
                uzInput.setUrlIMAAd(urlIMAAd);
                uzInput.setResultGetTokenStreaming(mResultGetTokenStreaming);
                //TODO iplm url thumbnail seekbar
                uzInput.setUrlThumnailsPreviewSeekbar(null);
                UZData.getInstance().setUizaInput(uzInput);
            }
            checkData();
        }
    }

    private boolean isGetClickedPip;

    private void checkData() {
        UZData.getInstance().setSettingPlayer(true);
        isHasError = false;
        if (UZData.getInstance().getEntityId() == null || UZData.getInstance().getEntityId().isEmpty()) {
            Timber.e("checkData getEntityId null or empty -> return");
            handleError(UZExceptionUtil.getExceptionEntityId());
            UZData.getInstance().setSettingPlayer(false);
            return;
        }
        isLivestream = UZData.getInstance().isLivestream();
        isGetClickedPip = UZUtil.getClickedPip(getContext());
        Timber.d("checkData isLivestream: %b, isGetClickedPip: %b", isLivestream, isGetClickedPip);
        if (!isGetClickedPip) {
            setDefaultValueForFlagIsTracked();
        }
        if (uzPlayerManager != null) {
            releaseUzPlayerManager();
            mResultGetLinkPlay = null;
            resetCountTryLinkPlayError();
            showProgress();
        }
        setTitle();
        callAPIGetLinkPlay();
        trackUizaEventDisplay();
        trackUizaEventPlaysRequested();
    }

    private void checkToSetUpResource() {
        if (UZData.getInstance().getResultGetLinkPlay() != null && UZData.getInstance().getPlayback() != null) {
            List<String> listLinkPlay = new ArrayList<>();
            List<Url> urlList = mResultGetLinkPlay.getData().getUrls();
            if (isLivestream) {
                //Bat buoc dung linkplay m3u8 cho nay, do bug cua system
                for (Url url : urlList) {
                    if (url.getUrl().toLowerCase().endsWith(M3U8_EXTENSION)) {
                        listLinkPlay.add(url.getUrl());
                    }
                }
            } else {
                for (Url url : urlList) {
                    if (url.getUrl().toLowerCase().endsWith(MPD_EXTENSION)) {
                        listLinkPlay.add(url.getUrl());
                    }
                }
                for (Url url : urlList) {
                    if (url.getUrl().toLowerCase().endsWith(M3U8_EXTENSION)) {
                        listLinkPlay.add(url.getUrl());
                    }
                }
            }
            if (listLinkPlay.isEmpty()) {
                handleErrorNoData();
                return;
            }
            if (countTryLinkPlayError >= listLinkPlay.size()) {
                if (LConnectivityUtil.isConnected(getContext())) {
                    handleError(UZExceptionUtil.getExceptionTryAllLinkPlay());
                } else {
                    notifyError(UZExceptionUtil.getExceptionNoConnection());
                }
                return;
            }
            String linkPlay = listLinkPlay.get(countTryLinkPlayError);

            addTrackingMuiza(Constants.MUIZA_EVENT_READY);
            if (isCalledFromChangeSkin) {
                //if called from func changeSkin(), dont initDataSource with uilIMA Ad.
                initDataSource(linkPlay, null, UZData.getInstance().getUrlThumnailsPreviewSeekbar(), subtitleList, isAdsDependencyAvailable());
            } else {
                initDataSource(linkPlay, UZData.getInstance().getUrlIMAAd(), UZData.getInstance().getUrlThumnailsPreviewSeekbar(), subtitleList, isAdsDependencyAvailable());
            }
            if (uzCallback != null) {
                uzCallback.isInitResult(false, true, mResultGetLinkPlay, UZData.getInstance().getPlayback());
            }
            initUizaPlayerManager();
        } else {
            handleError(UZExceptionUtil.getExceptionSetup());
        }
    }

    private long timestampInitDataSource;

    private void initDataSource(String linkPlay, String urlIMAAd, String urlThumbnailsPreviewSeekbar, List<Subtitle> subtitleList, boolean includeAds) {

        if (subtitleList == null || subtitleList.isEmpty()) {
            // hide the cc (subtitle) button
            ViewUtils.goneViews(ibCcIcon);
        }

        timestampInitDataSource = System.currentTimeMillis();
        Timber.d("-------------------->initDataSource linkPlay %s", linkPlay);
        TmpParamData.getInstance().setEntitySourceUrl(linkPlay);
        TmpParamData.getInstance().setTimeFromInitEntityIdToAllApiCalledSuccess(System.currentTimeMillis() - timestampBeforeInitNewSession);
        if (includeAds) {
            uzPlayerManager = new UZPlayerManager(this, linkPlay, urlIMAAd, urlThumbnailsPreviewSeekbar, subtitleList);
            ((UZPlayerManager) uzPlayerManager).addAdPlayerCallback(new UZAdPlayerCallback() {
                @Override
                public void onPlay() {
                    updateTvDuration();
                    if (videoAdPlayerCallback != null) videoAdPlayerCallback.onPlay();
                }

                @Override
                public void onVolumeChanged(int i) {
                    if (videoAdPlayerCallback != null) videoAdPlayerCallback.onVolumeChanged(i);
                }

                @Override
                public void onPause() {
                    if (videoAdPlayerCallback != null) videoAdPlayerCallback.onPause();
                }

                @Override
                public void onLoaded() {
                    if (videoAdPlayerCallback != null) videoAdPlayerCallback.onLoaded();
                }

                @Override
                public void onResume() {
                    if (videoAdPlayerCallback != null) videoAdPlayerCallback.onResume();
                }

                @Override
                public void onEnded() {
                    updateTvDuration();
                    if (videoAdPlayerCallback != null) videoAdPlayerCallback.onEnded();
                }

                @Override
                public void onError() {
                    updateTvDuration();
                    if (videoAdPlayerCallback != null) videoAdPlayerCallback.onError();
                }

                @Override
                public void onBuffering() {
                    if (videoAdPlayerCallback != null) videoAdPlayerCallback.onBuffering();
                }
            });
        } else {
            uzPlayerManager =
                    new UZPlayerNoAdsManager(this, linkPlay, urlThumbnailsPreviewSeekbar, subtitleList);
        }
        if (uzTimebar != null) {
            if (urlThumbnailsPreviewSeekbar == null || urlThumbnailsPreviewSeekbar.isEmpty()) {
                uzTimebar.setPreviewEnabled(false);
            } else {
                uzTimebar.setPreviewEnabled(true);
            }
            uzTimebar.setPreviewLoader(uzPlayerManager);
        }
        uzPlayerManager.setProgressCallback(new ProgressCallback() {
            @Override
            public void onAdEnded() {
                setDefaultUseController(isDefaultUseController());
                if (progressCallback != null) {
                    progressCallback.onAdEnded();
                }
            }

            @Override
            public void onAdProgress(int s, int duration, int percent) {
                if (progressCallback != null) {
                    progressCallback.onAdProgress(s, duration, percent);
                }
            }

            @Override
            public void onVideoProgress(long currentMls, int s, long duration, int percent) {
                TmpParamData.getInstance().setPlayerPlayheadTime(s);
                updateUIIbRewIconDependOnProgress(currentMls, false);
                trackProgress(s, percent);
                callAPITrackMuiza(s);
                if (progressCallback != null) {
                    progressCallback.onVideoProgress(currentMls, s, duration, percent);
                }
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (progressCallback != null) {
                    progressCallback.onPlayerStateChanged(playWhenReady, playbackState);
                }
            }

            @Override
            public void onBufferProgress(long bufferedPosition, int bufferedPercentage, long duration) {
                if (progressCallback != null) {
                    progressCallback.onBufferProgress(bufferedPosition, bufferedPercentage, duration);
                }
            }
        });
        uzPlayerManager.setDebugCallback(new DebugCallback() {
            @Override
            public void onUpdateButtonVisibilities() {
                updateUIButtonVisibilities();
            }
        });

        uzPlayerManager.setBufferCallback(new UZBufferCallback() {
            @Override
            public void onBufferChanged(long bufferedDurationUs, float playbackSpeed) {
                UZVideo.this.bufferedDurationUs = bufferedDurationUs;
            }
        });
    }

    protected void onStateReadyFirst() {
        long pageLoadTime = System.currentTimeMillis() - timestampBeforeInitNewSession;
        TmpParamData.getInstance().setPageLoadTime(pageLoadTime);
        addTrackingMuiza(Constants.MUIZA_EVENT_VIEWSTART);
        TmpParamData.getInstance().setViewStart(System.currentTimeMillis());
        TmpParamData.getInstance().setViewTimeToFirstFrame(System.currentTimeMillis());
        updateTvDuration();
        updateUIButtonPlayPauseDependOnIsAutoStart();
        updateUIDependOnLivestream();
        if (isSetUZTimebarBottom) {
            ViewUtils.visibleViews(uzPlayerView);
        }
        resizeContainerView();
        //enable from playPlaylistPosition() prevent double click
        setClickableForViews(true, ibSkipPreviousIcon, ibSkipNextIcon);
        if (isGetClickedPip) {
            Timber.d("getClickedPip true -> setPlayWhenReady true");
            uzPlayerManager.getPlayer().setPlayWhenReady(true);
        }
        if (uzCallback != null) {
            Timber.d("onStateReadyFirst ===> isInitResult");
            uzCallback.isInitResult(true, true, mResultGetLinkPlay, UZData.getInstance().getPlayback());
        }
        if (isCastingChromecast) {
            replayChromeCast();
        }
        updateUIPlayerInfo();
        TmpParamData.getInstance().setSessionStart(System.currentTimeMillis());
        long playerStartUpTime = System.currentTimeMillis() - timestampInitDataSource;
        TmpParamData.getInstance().setPlayerStartupTime(playerStartUpTime);
        trackUizaEventVideoStarts();
        trackUizaCCUForLivestream();
        pingHeartBeat();
        UZData.getInstance().setSettingPlayer(false);
    }

    private void initUizaPlayerManager() {
        if (uzPlayerManager != null) {
            uzPlayerManager.init();
            addTrackingMuiza(Constants.MUIZA_EVENT_LOADSTART);
            if (isGetClickedPip && !isPlayPlaylistFolder()) {
                uzPlayerManager.getPlayer().setPlayWhenReady(false);
            } else {
                if (isRefreshFromChangeSkin) {
                    uzPlayerManager.seekTo(currentPositionBeforeChangeSkin);
                    isRefreshFromChangeSkin = false;
                    currentPositionBeforeChangeSkin = 0;
                }
            }
            if (isCalledFromConnectionEventBus) {
                uzPlayerManager.setRunnable();
                isCalledFromConnectionEventBus = false;
            }
            // Always using this options
            initStatsForNerds();
        }
    }

    //=============================================================================================START TRACKING
    private int oldPercent = Constants.NOT_FOUND;
    private boolean isTracked25;
    private boolean isTracked50;
    private boolean isTracked75;
    private boolean isTracked100;

    private void setDefaultValueForFlagIsTracked() {
        UZTrackingUtil.clearAllValues(getContext());
        isTracked25 = false;
        isTracked50 = false;
        isTracked75 = false;
        isTracked100 = false;
    }

    protected void trackProgress(int s, int percent) {
        //dont track if user use custom linkplay
        if (isInitCustomLinkPlay) {
            return;
        }
        //track event view (after video is played 5s)
        if (s == (isLivestream ? 3 : 5)) {
            if (!UZTrackingUtil.isTrackedEventTypeView(getContext())) {
                callAPITrackUiza(UZData.getInstance().createTrackingInput(getContext(), Constants.EVENT_TYPE_VIEW),
                        () -> UZTrackingUtil.setTrackingDoneWithEventTypeView(getContext(), true));
            }
        }
        //dont track play_throught if entity is live
        if (isLivestream) {
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
            if (UZTrackingUtil.isTrackedEventTypePlayThrought100(getContext())) {
                isTracked100 = true;
            } else {
                callAPITrackUiza(UZData.getInstance().createTrackingInput(getContext(), PLAY_THROUGH_100, Constants.EVENT_TYPE_PLAY_THROUGHT), new UZTrackingUtil.UizaTrackingCallback() {
                    @Override
                    public void onTrackingSuccess() {
                        UZTrackingUtil.setTrackingDoneWithEventTypePlayThrought100(getContext(), true);
                    }
                });
            }
        } else if (percent >= Constants.PLAYTHROUGH_75) {
            if (isTracked75) {
                return;
            }
            if (UZTrackingUtil.isTrackedEventTypePlayThrought75(getContext())) {
                isTracked75 = true;
            } else {
                callAPITrackUiza(UZData.getInstance().createTrackingInput(getContext(), PLAY_THROUGH_75, Constants.EVENT_TYPE_PLAY_THROUGHT), new UZTrackingUtil.UizaTrackingCallback() {
                    @Override
                    public void onTrackingSuccess() {
                        UZTrackingUtil.setTrackingDoneWithEventTypePlayThrought75(getContext(), true);
                    }
                });
            }
        } else if (percent >= Constants.PLAYTHROUGH_50) {
            if (isTracked50) {
                return;
            }
            if (UZTrackingUtil.isTrackedEventTypePlayThrought50(getContext())) {
                isTracked50 = true;
            } else {
                callAPITrackUiza(UZData.getInstance().createTrackingInput(getContext(), PLAY_THROUGH_50, Constants.EVENT_TYPE_PLAY_THROUGHT), new UZTrackingUtil.UizaTrackingCallback() {
                    @Override
                    public void onTrackingSuccess() {
                        UZTrackingUtil.setTrackingDoneWithEventTypePlayThrought50(getContext(), true);
                    }
                });
            }
        } else if (percent >= Constants.PLAYTHROUGH_25) {
            if (isTracked25) {
                return;
            }
            if (UZTrackingUtil.isTrackedEventTypePlayThrought25(getContext())) {
                isTracked25 = true;
            } else {
                callAPITrackUiza(UZData.getInstance().createTrackingInput(getContext(), PLAY_THROUGH_25, Constants.EVENT_TYPE_PLAY_THROUGHT), new UZTrackingUtil.UizaTrackingCallback() {
                    @Override
                    public void onTrackingSuccess() {
                        UZTrackingUtil.setTrackingDoneWithEventTypePlayThrought25(getContext(), true);
                    }
                });
            }
        }
    }

    private void callAPITrackUiza(final UizaTracking uizaTracking, final UZTrackingUtil.UizaTrackingCallback uizaTrackingCallback) {
        if (getContext() == null || isInitCustomLinkPlay) {
            return;
        }
        UizaTrackingService service = UizaClientFactory.getTrackingService();
        RxBinder.bind(service.track(uizaTracking), tracking -> {
            if (uizaTrackingCallback != null) {
                uizaTrackingCallback.onTrackingSuccess();
            }
        }, Timber::e);
    }

    private void trackUizaEventVideoStarts() {
        if (isInitCustomLinkPlay) {
            Timber.d("trackUizaEventVideoStarts return isInitCustomLinkPlay");
            return;
        }
        if (!UZTrackingUtil.isTrackedEventTypeVideoStarts(getContext())) {
            callAPITrackUiza(UZData.getInstance().createTrackingInput(getContext(), Constants.EVENT_TYPE_VIDEO_STARTS),
                    () -> UZTrackingUtil.setTrackingDoneWithEventTypeVideoStarts(getContext(), true));
        }
    }

    private void trackUizaEventDisplay() {
        if (isInitCustomLinkPlay) {
            Timber.d("trackUizaEventVideoStarts return isInitCustomLinkPlay");
            return;
        }
        if (!UZTrackingUtil.isTrackedEventTypeDisplay(getContext())) {
            callAPITrackUiza(UZData.getInstance().createTrackingInput(getContext(), Constants.EVENT_TYPE_DISPLAY),
                    () -> UZTrackingUtil.setTrackingDoneWithEventTypeDisplay(getContext(), true));
        }
    }

    private void trackUizaEventPlaysRequested() {
        if (isInitCustomLinkPlay) {
            Timber.d("trackUizaEventVideoStarts return isInitCustomLinkPlay");
            return;
        }
        if (!UZTrackingUtil.isTrackedEventTypePlaysRequested(getContext())) {
            callAPITrackUiza(UZData.getInstance().createTrackingInput(getContext(), Constants.EVENT_TYPE_PLAYS_REQUESTED),
                    () -> UZTrackingUtil.setTrackingDoneWithEventTypePlaysRequested(getContext(), true));
        }
    }

    private final int INTERVAL_HEART_BEAT = 10000;

    private void pingHeartBeat() {
        if (getContext() == null || cdnHost == null || cdnHost.isEmpty()) {
            Timber.e("Error cannot call API pingHeartBeat() -> cdnHost: %s, entityName: %s -> return", cdnHost, UZData.getInstance().getEntityName());
            return;
        }
        if (activityIsPausing) {
            rePingHeartBeat();
            Timber.e("Error cannot call API pingHeartBeat() because activity is pausing %s", UZData.getInstance().getEntityName());
            return;
        }
        UizaHeartBeatService service = UizaClientFactory.getHeartBeatService();
        String cdnName = cdnHost;
        String session = uuid.toString();
        RxBinder.bind(service.pingHeartBeat(cdnName, session),
                result -> rePingHeartBeat(),
                throwable -> {
                    Timber.e(throwable, "pingHeartBeat onFail:");
                    rePingHeartBeat();
                });
    }

    private void rePingHeartBeat() {
        handler.postDelayed(() -> pingHeartBeat(), INTERVAL_HEART_BEAT);
    }

    private final int INTERVAL_TRACK_CCU = 3000;

    private void trackUizaCCUForLivestream() {
        if (getContext() == null || !isLivestream || isInitCustomLinkPlay) {
            Timber.e("Error cannot trackUizaCCUForLivestream() -> return %s", UZData.getInstance().getEntityName());
            return;
        }
        if (activityIsPausing) {
            reTrackUizaCCUForLivestream();
            Timber.e("Error cannot trackUizaCCUForLivestream() because activity is pausing %s", UZData.getInstance().getEntityName());
            return;
        }
        UizaTrackingService service = UizaClientFactory.getTrackingService();
        UizaTrackingCCU uizaTrackingCCU = new UizaTrackingCCU();
        uizaTrackingCCU.setDt(LDateUtils.getCurrent(LDateUtils.FORMAT_1));
        uizaTrackingCCU.setHo(cdnHost);
        uizaTrackingCCU.setAi(UZData.getInstance().getAppId());
        uizaTrackingCCU.setSn(UZData.getInstance().getChannelName()); // stream name
        uizaTrackingCCU.setDi(UZOsUtil.getDeviceId(getContext()));
        uizaTrackingCCU.setUa(Constants.USER_AGENT);
        RxBinder.bind(service.trackCCU(uizaTrackingCCU),
                result -> reTrackUizaCCUForLivestream(),
                throwable -> reTrackUizaCCUForLivestream());
    }

    private void reTrackUizaCCUForLivestream() {
        handler.postDelayed(() -> trackUizaCCUForLivestream(), INTERVAL_TRACK_CCU);
    }

    protected void addTrackingMuizaError(String event, UZException e) {
        if (!isInitCustomLinkPlay) {
            UZData.getInstance().addTrackingMuiza(getContext(), event, e);
        }
    }

    protected void addTrackingMuiza(String event) {
        if (!isInitCustomLinkPlay) {
            UZData.getInstance().addTrackingMuiza(getContext(), event);
        }
    }

    private boolean isTrackingMuiza;

    private void callAPITrackMuiza(int s) {
        if (isInitCustomLinkPlay || (s <= 0 || s % 10 != 0) || isTrackingMuiza || UZData.getInstance().isMuizaListEmpty()) {
            return;
        }
        isTrackingMuiza = true;
        final List<Muiza> muizaListToTracking = new ArrayList<>(UZData.getInstance().getMuizaList());
        UZData.getInstance().clearMuizaList();
        UizaTrackingService service = UizaClientFactory.getTrackingService();
        RxBinder.bind(service.trackMuiza(muizaListToTracking),
                result -> isTrackingMuiza = false,
                throwable -> {
                    isTrackingMuiza = false;
                    UZData.getInstance().addListTrackingMuiza(muizaListToTracking);
                });
    }
    //=============================================================================================END TRACKING

    //=============================================================================================START EVENTBUS
    private boolean isCalledFromConnectionEventBus = false;

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusData.ConnectEvent event) {
        if (event == null) {
            return;
        }
        if (!event.isConnected()) {
            notifyError(UZExceptionUtil.getExceptionNoConnection());
        } else {
            if (uzPlayerManager == null) {
                return;
            }
            LDialogUtil.clearAll();
            if (uzPlayerManager.getExoPlaybackException() == null) {
                hideController();
                hideLayoutMsg();
            } else {
                isCalledFromConnectionEventBus = true;
                uzPlayerManager.setResumeIfConnectionError();
                if (!activityIsPausing) {
                    uzPlayerManager.init();
                    if (isCalledFromConnectionEventBus) {
                        uzPlayerManager.setRunnable();
                        isCalledFromConnectionEventBus = false;
                    }
                }
            }
            resumeVideo();
        }
    }

    private long positionMiniPlayer;

    public boolean isInitNewItem(String urlImgThumbnail) {
        if (positionMiniPlayer != 0) {
            seekTo(positionMiniPlayer);
            resumeVideo();
            sendEventInitSuccess();
            positionMiniPlayer = 0;
            return false;
        } else {
            setUrlImgThumbnail(urlImgThumbnail);
            pauseVideo();
            showProgress();
            positionMiniPlayer = 0;
            return true;
        }
    }

    //listen msg from service FUZVideoService
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ComunicateMng.MsgFromService msg) {
        if (msg == null || uzPlayerManager == null) {
            return;
        }
        //click open app of mini player
        if (msg instanceof ComunicateMng.MsgFromServiceOpenApp) {
            if (getContext() == null) {
                return;
            }
            Timber.d("miniplayer STEP 6");
            try {
                positionMiniPlayer = ((ComunicateMng.MsgFromServiceOpenApp) msg).getPositionMiniPlayer();
                Class classNamePfPlayer = Class.forName(((Activity) getContext()).getClass().getName());
                Intent intent = new Intent(getContext(), classNamePfPlayer);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(Constants.KEY_UIZA_ENTITY_ID, UZData.getInstance().getEntityId());
                getContext().startActivity(intent);
            } catch (ClassNotFoundException e) {
                Timber.e(e, "onMessageEvent open app ClassNotFoundException");
                SentryUtils.captureException(e);
            }
            return;
        }
        //when pip float view init success
        if (uzCallback != null && msg instanceof ComunicateMng.MsgFromServiceIsInitSuccess) {
            //Ham nay duoc goi khi player o FUZVideoService da init xong
            //Nhiem vu la minh se gui vi tri hien tai sang cho FUZVideoService no biet
            Timber.d("miniplayer STEP 3 UZVideo biet FUZVideoService da init xong -> gui lai content position cua UZVideo cho FUZVideoService");
            ComunicateMng.MsgFromActivityPosition msgFromActivityPosition = new ComunicateMng.MsgFromActivityPosition(null);
            msgFromActivityPosition.setPosition(getCurrentPosition());
            ComunicateMng.postFromActivity(msgFromActivityPosition);
            isInitMiniPlayerSuccess = true;
            uzCallback.onStateMiniPlayer(((ComunicateMng.MsgFromServiceIsInitSuccess) msg).isInitSuccess());
        }
    }

    public void sendEventInitSuccess() {
        ComunicateMng.MsgFromActivityIsInitSuccess msgFromActivityIsInitSuccess = new ComunicateMng.MsgFromActivityIsInitSuccess(null);
        msgFromActivityIsInitSuccess.setInitSuccess(true);
        ComunicateMng.postFromActivity(msgFromActivityIsInitSuccess);
    }
    //=============================================================================================END EVENTBUS

    //=============================================================================================START CHROMECAST


    private void handleConnectedChromecast() {
        isCastingChromecast = true;
        isCastPlayerPlayingFirst = false;
        playChromecast();
        updateUIChromecast();
    }

    private void handleDisconnectedChromecast() {
        isCastingChromecast = false;
        isCastPlayerPlayingFirst = false;
        updateUIChromecast();
    }

    public boolean isCastingChromecast() {
        return isCastingChromecast;
    }

    //last current position lúc từ exoplayer switch sang cast player
    private long lastCurrentPosition;

    private void playChromecast() {
        if (UZData.getInstance().getPlayback() == null || uzPlayerManager == null || uzPlayerManager.getPlayer() == null) {
            return;
        }
        showProgress();
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

//        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, UZData.getInstance().getPlayback().getDescription());
//        movieMetadata.putString(MediaMetadata.KEY_TITLE, UZData.getInstance().getPlayback().getEntityName());
//        movieMetadata.addImage(new WebImage(Uri.parse(UZData.getInstance().getPlayback().getThumbnail())));

        // NOTE: The receiver app (on TV) should Satisfy CORS requirements
        // https://developers.google.com/cast/docs/android_sender/media_tracks#satisfy_cors_requirements
        List<MediaTrack> mediaTrackList = buildMediaTracks();
        long duration = getDuration();
        if (duration < 0) {
            Timber.e("invalid duration -> cannot play chromecast");
            return;
        }

        MediaInfo mediaInfo = new MediaInfo.Builder(uzPlayerManager.getLinkPlay())
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/mp4")
                .setMetadata(movieMetadata)
                .setMediaTracks(mediaTrackList)
                .setStreamDuration(duration)
                .build();

        //play chromecast with full screen control
        //UZData.getInstance().getCasty().getPlayer().loadMediaAndPlay(mediaInfo, true, lastCurrentPosition);

        //play chromecast without screen control
        UZData.getInstance().getCasty().getPlayer().loadMediaAndPlayInBackground(mediaInfo, true, lastCurrentPosition);

        UZData.getInstance().getCasty().getPlayer().getRemoteMediaClient().addProgressListener((currentPosition, duration1) -> {
            if (currentPosition >= lastCurrentPosition && !isCastPlayerPlayingFirst) {
                hideProgress();
                isCastPlayerPlayingFirst = true;
            }

            if (currentPosition > 0) {
                uzPlayerManager.seekTo(currentPosition);
            }
        }, 1000);

    }

    private List<MediaTrack> buildMediaTracks() {
        List<MediaTrack> result = new ArrayList<>();
        if (subtitleList == null || subtitleList.isEmpty()) return result;
        for (int i = 0; i < subtitleList.size(); i++) {
            Subtitle subtitle = subtitleList.get(i);
            if (subtitle.getStatus() == Subtitle.Status.DISABLE) continue;
            MediaTrack subtitleTrack = new MediaTrack.Builder(i + 1001/* ID is unique */, MediaTrack.TYPE_TEXT)
                    .setName(subtitle.getName())
                    .setContentType("text/vtt")
                    .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                    .setContentId(subtitle.getUrl())
                    .setLanguage(subtitle.getLanguage())
                    .build();
            // Re-order default subtitle track
            if (subtitle.getIsDefault() == 1) {
                result.add(0, subtitleTrack);
            } else {
                result.add(subtitleTrack);
            }
        }
        return result;
    }

    private boolean isCastPlayerPlayingFirst;

    /* khi click vào biểu tượng casting
     * thì sẽ pause local player và bắt đầu loading lên cast player
     * khi disconnect thì local player sẽ resume*/
    private void updateUIChromecast() {
        if (uzPlayerManager == null || rlChromeCast == null || isTV) {
            return;
        }
        if (isCastingChromecast) {
            uzPlayerManager.pauseVideo();
            uzPlayerManager.setVolume(0f);
            ViewUtils.visibleViews(rlChromeCast, ibPlayIcon);
            ViewUtils.goneViews(ibPauseIcon);
//            ViewUtils.goneViews(ibSettingIcon, ibCcIcon, ibBackScreenIcon, ibPlayIcon, ibPauseIcon, ibVolumeIcon);
            //casting player luôn play first với volume not mute
            //UZData.getInstance().getCasty().setVolume(0.99);

            if (uzPlayerView != null) {
                uzPlayerView.setControllerShowTimeoutMs(0);
            }
        } else {
            uzPlayerManager.resumeVideo();
            uzPlayerManager.setVolume(0.99f);
            ViewUtils.goneViews(rlChromeCast, ibPlayIcon);
            ViewUtils.visibleViews(ibPauseIcon);
//            ViewUtils.visibleViews(ibSettingIcon, ibCcIcon, ibBackScreenIcon, ibPauseIcon, ibVolumeIcon);
            //TODO iplm volume mute on/off o cast player
            //khi quay lại exoplayer từ cast player thì mặc định sẽ bật lại âm thanh (dù cast player đang mute hay !mute)
            //uzPlayerManager.setVolume(0.99f);

            if (uzPlayerView != null) {
                uzPlayerView.setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT);
            }
        }
    }

    //=============================================================================================END CHROMECAST
    private void scheduleJob() {
        if (getContext() == null) {
            return;
        }
        JobInfo myJob;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myJob = new JobInfo.Builder(0, new ComponentName(getContext(), LConectifyService.class))
                    .setRequiresCharging(true)
                    .setMinimumLatency(1000)
                    .setOverrideDeadline(2000)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .build();
            JobScheduler jobScheduler = (JobScheduler) getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                jobScheduler.schedule(myJob);
            }
        }
    }

    private StatsForNerdsView statsForNerdsView;
    private UiUpdateHandler statsUIHandler = new UiUpdateHandler(this);
    private SettingsContentObserver volumeObserver = new SettingsContentObserver(UZVideo.this, new Handler());
    private long bufferedDurationUs;
    private long bitrateEstimate;
    private long bytesLoaded;
    private int droppedFrames;
    private int viewPortWidth, viewPortHeight;
    private int currentResWidth, currentResHeight;
    private int optimalResWidth, optimalResHeight;
    private static final int MSG_UPDATE_STATS = 10005;
    private static final int MSG_UPDATE_STATS_NW_ONLY = 10006;

    private AnalyticsListener nerdAnalyticsListener = new AnalyticsListener() {

        @Override
        public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs,
                                        long totalBytesLoaded, long bitrateEstimate) {
            UZVideo.this.bytesLoaded = totalBytesLoaded;
            UZVideo.this.bitrateEstimate = bitrateEstimate;
        }

        @Override
        public void onSurfaceSizeChanged(EventTime eventTime, int width, int height) {
            viewPortWidth = width;
            viewPortHeight = height;
            depictViewPortFrameInfo();
        }

        @Override
        public void onLoadCompleted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo,
                                    MediaSourceEventListener.MediaLoadData mediaLoadData) {
            Format downloadFormat = mediaLoadData.trackFormat;
            if (downloadFormat != null && downloadFormat.width != -1 && downloadFormat.height != -1) {
                if (downloadFormat.width != optimalResWidth && downloadFormat.height != optimalResHeight) {
                    optimalResWidth = downloadFormat.width;
                    optimalResHeight = downloadFormat.height;
                    depictVideoResolution();
                }
            }
        }

        @Override
        public void onVideoSizeChanged(EventTime eventTime, int width, int height,
                                       int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            if (width <= 0 || height <= 0) return;
            if (width != currentResWidth && height != currentResHeight) {
                currentResWidth = width;
                currentResHeight = height;
                depictVideoResolution();
            }
        }

        @Override
        public void onVolumeChanged(EventTime eventTime, float volume) {
            depictVolumeInfo(Math.round(volume * 100));
        }

        @Override
        public void onDecoderInputFormatChanged(EventTime eventTime, int trackType, Format format) {
            depictVideoDetailInfo(format);
        }

        @Override
        public void onDroppedVideoFrames(EventTime eventTime, int droppedFrames, long elapsedMs) {
            UZVideo.this.droppedFrames += droppedFrames;
            depictViewPortFrameInfo();
        }

        @Override
        public void onRenderedFirstFrame(EventTime eventTime, @Nullable Surface surface) {
            startPlayerStats();
            getContext().getContentResolver()
                    .registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, volumeObserver);
        }
    };

    // ===== Stats For Nerds =====
    private void initStatsForNerds() {
        getPlayer().addAnalyticsListener(nerdAnalyticsListener);
    }

    private static class UiUpdateHandler extends Handler {
        private WeakReference<UZVideo> weakUzVideo;

        UiUpdateHandler(UZVideo uzVideo) {
            weakUzVideo = new WeakReference<>(uzVideo);
        }

        @Override
        public void handleMessage(Message msg) {
            UZVideo uzVideo = weakUzVideo.get();
            if (null == uzVideo) return;
            switch (msg.what) {
                case MSG_UPDATE_STATS:
                    uzVideo.depictPlayerStats();
                    break;
                case MSG_UPDATE_STATS_NW_ONLY:
                    uzVideo.depictPlayerNWStats();
                    break;
            }
        }
    }

    private void startPlayerStats() {
        statsUIHandler.removeMessages(MSG_UPDATE_STATS);
        statsUIHandler.removeMessages(MSG_UPDATE_STATS_NW_ONLY);
        depictVideoInfo();
        depictDeviceInfo();
        depictVersionInfo();
        depictPlayerStats();
        depictPlayerNWStats();
        depictViewPortFrameInfo();
        depictVolumeInfo(AppUtils.getVolumePercentage(getContext(), AudioManager.STREAM_MUSIC));
    }

    private void depictVolumeInfo(int volumePercentage) {
        statsForNerdsView.setTextVolume(getResources().getString(R.string.format_volume, volumePercentage));
    }

    private void depictVideoDetailInfo(Format format) {
        if (format == null || TextUtils.isEmpty(format.sampleMimeType)) return;
        if (format.sampleMimeType.startsWith("audio")) {
            statsForNerdsView.setTextAudioFormat(
                    getResources().getString(R.string.format_audio_format, format.sampleMimeType, format.sampleRate));
        } else if (format.sampleMimeType.startsWith("video")) {
            statsForNerdsView.setTextVideoFormat(
                    getResources().getString(R.string.format_video_format, format.sampleMimeType, format.width,
                            format.height, Math.round(format.frameRate)));
        }
    }

    private void depictVideoResolution() {
        statsForNerdsView.setTextResolution(getResources().getString(R.string.format_resolution,
                currentResWidth, currentResHeight, optimalResWidth, optimalResHeight));
    }

    private void depictViewPortFrameInfo() {
        if (viewPortWidth == 0 && viewPortHeight == 0) {
            // at first time, surface view size or viewport equals to uzVideo size
            viewPortWidth = this.getWidth();
            viewPortHeight = this.getHeight();
        }
        statsForNerdsView.setTextViewPortFrame(
                getResources().getString(R.string.format_viewport_frame, viewPortWidth, viewPortHeight, droppedFrames));
    }

    private void depictVideoInfo() {
        statsForNerdsView.setEntityInfo(UZData.getInstance().getEntityId());
        statsForNerdsView.setTextHost(Constants.PREFIXS + UZData.getInstance().getDomainAPI());
    }

    private void depictVersionInfo() {
        statsForNerdsView.setTextVersion(getResources().getString(R.string.format_version,
                BuildConfig.VERSION_NAME, BuildConfig.EXO_VERSION, Constants.API_VERSION_4));
    }

    private void depictDeviceInfo() {
        statsForNerdsView.setTextDeviceInfo(
                getResources().getString(R.string.format_device_info, Build.MODEL, Build.VERSION.RELEASE));
    }

    private void depictPlayerStats() {
        String formattedValue;
        if (bitrateEstimate < 1e6) {
            formattedValue = getResources().getString(R.string.format_connection_speed_k,
                    ConvertUtils.getFormattedDouble((bitrateEstimate / Math.pow(10, 3)), 2));
        } else {
            formattedValue = getResources().getString(R.string.format_connection_speed_m,
                    ConvertUtils.getFormattedDouble((bitrateEstimate / Math.pow(10, 6)), 2));
        }
        statsForNerdsView.setTextConnectionSpeed(formattedValue);

        // Re-update after 0.5 second
        statsUIHandler.sendEmptyMessageDelayed(MSG_UPDATE_STATS, 500);
    }

    private void depictPlayerNWStats() {
        statsForNerdsView.setTextNetworkActivity(
                ConvertUtils.humanReadableByteCount(bytesLoaded, true, false));
        if (getPlayer() != null) {
            long remainingUs = (getPlayer().getDuration() - getPlayer().getCurrentPosition()) * 1000;
            remainingUs = remainingUs >= 0 ? remainingUs : 0;
            double buffered = bufferedDurationUs >= remainingUs ? remainingUs : bufferedDurationUs;
            String buffer = ConvertUtils.getFormattedDouble((buffered / Math.pow(10, 6)), 1);
            statsForNerdsView.setTextBufferHealth(getResources().getString(R.string.format_buffer_health, buffer));
        }

        // Re-update after 1 second
        statsUIHandler.sendEmptyMessageDelayed(MSG_UPDATE_STATS_NW_ONLY, 1000);
    }

    private static class SettingsContentObserver extends ContentObserver {
        private AudioManager audioManager;
        private WeakReference<UZVideo> weakUZVideo;

        SettingsContentObserver(UZVideo uzVideo, Handler handler) {
            super(handler);
            audioManager = (AudioManager) uzVideo.getContext().getSystemService(Context.AUDIO_SERVICE);
            weakUZVideo = new WeakReference<>(uzVideo);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange) {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            UZVideo uzVideo = weakUZVideo.get();
            if (uzVideo != null && uzVideo.uzPlayerManager != null) {
                uzVideo.uzPlayerManager.setVolume(currentVolume * 1.0f / maxVolume);
            }
        }
    }

    protected boolean isAdsDependencyAvailable() {
        return UZUtil.isDependencyAvailable("com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer");
    }

    protected boolean isCastDependencyAvailable() {
        return UZUtil.isDependencyAvailable("com.google.android.gms.cast.framework.OptionsProvider")
                && UZUtil.isDependencyAvailable("androidx.mediarouter.app.MediaRouteButton");
    }

    public UZChromeCast getuZChromeCast() {
        return uZChromeCast;
    }

    private void addUIChromecastLayer() {
        rlChromeCast = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams rlChromeCastParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rlChromeCast.setLayoutParams(rlChromeCastParams);
        rlChromeCast.setVisibility(GONE);
        rlChromeCast.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.Black));
        ibsCast = new UZImageButton(getContext());
        ibsCast.setBackgroundColor(Color.TRANSPARENT);
        ibsCast.setImageResource(R.drawable.cast);
        RelativeLayout.LayoutParams ibsCastParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ibsCastParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        ibsCast.setLayoutParams(ibsCastParams);
        ibsCast.setRatioPort(5);
        ibsCast.setRatioLand(5);
        ibsCast.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ibsCast.setColorFilter(Color.WHITE);
        rlChromeCast.addView(ibsCast);
        rlChromeCast.setOnClickListener(this);
        if (llTop != null) {
            if (llTop.getParent() instanceof ViewGroup) {
                ((RelativeLayout) llTop.getParent()).addView(rlChromeCast, 0);
            }
        } else if (rlLiveInfo != null) {
            if (rlLiveInfo.getParent() instanceof ViewGroup) {
                ((RelativeLayout) rlLiveInfo.getParent()).addView(rlChromeCast, 0);
            }
        }
    }

    private UZAdPlayerCallback videoAdPlayerCallback;

    public void addVideoAdPlayerCallback(UZAdPlayerCallback uzAdPlayerCallback) {
        if (isAdsDependencyAvailable()) {
            this.videoAdPlayerCallback = uzAdPlayerCallback;
        } else {
            throw new NoClassDefFoundError(UZException.ERR_506);
        }
    }

    protected void updateLiveStreamLatency(long latency) {
        statsForNerdsView.showTextLiveStreamLatency();
        statsForNerdsView.setTextLiveStreamLatency(ConvertUtils.groupingSeparatorLong(latency));
    }

    protected void hideTextLiveStreamLatency() {
        statsForNerdsView.hideTextLiveStreamLatency();
    }
}
