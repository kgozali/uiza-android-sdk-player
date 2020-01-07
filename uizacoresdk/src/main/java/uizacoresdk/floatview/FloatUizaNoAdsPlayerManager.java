package uizacoresdk.floatview;

import android.os.Handler;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.List;

import timber.log.Timber;
import vn.uiza.core.common.Constants;
import vn.uiza.models.Subtitle;

public final class FloatUizaNoAdsPlayerManager extends FloatUizaPlayerManagerAbs {

    public FloatUizaNoAdsPlayerManager(final FloatUizaVideoView fuzVideo, String linkPlay, List<Subtitle> subtitleList) {
        this.timestampPlayed = System.currentTimeMillis();
        isCanAddViewWatchTime = true;
        this.context = fuzVideo.getContext();
        this.fuzVideo = fuzVideo;
        this.linkPlay = linkPlay;
        this.subtitleList = subtitleList;
        this.videoWidth = 0;
        this.videoHeight = 0;

        manifestDataSourceFactory = new DefaultDataSourceFactory(context, Constants.USER_AGENT);
        mediaDataSourceFactory =
                new DefaultDataSourceFactory(context, Constants.USER_AGENT, new DefaultBandwidthMeter());
        handler = new Handler();
        runnable = () -> {
            if (fuzVideo.getPlayerView() != null) {

                if (progressListener != null) {
                    if (player != null) {
                        long mls = player.getCurrentPosition();
                        long duration = player.getDuration();
                        int percent = 0;
                        if (duration != 0) {
                            percent = (int) (mls * 100 / duration);
                        }
                        int s = Math.round(mls / 1000);
                        progressListener.onVideoProgress(mls, s, duration, percent);
                    }
                }
                if (handler != null && runnable != null) {
                    handler.postDelayed(runnable, 1000);
                }
            }
        };
        handler.postDelayed(runnable, 0);
        fuzVideo.getPlayerView().setControllerShowTimeoutMs(0);
    }

    @Override
    public void init(boolean isLivestream, long contentPosition) {
        Timber.d("miniplayer STEP 1 FUZPLayerManager init isLivestream: %b, contentPosition: %d ", isLivestream, contentPosition);
        reset();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        fuzVideo.getPlayerView().setPlayer(player);
        MediaSource mediaSourceVideo = createMediaSourceVideo();
        //merge title to media source video
        //SUBTITLE
        MediaSource mediaSourceWithSubtitle = createMediaSourceWithSubtitle(mediaSourceVideo);
        //merge ads to media source subtitle
        //IMA ADS
        // Compose the content media source into a new AdsMediaSource with both ads and content.
        //Prepare the player with the source.
        player.addListener(new FUZPlayerEventListener());
        player.addVideoListener(new FUZVideoListener());

        player.prepare(mediaSourceWithSubtitle);
        //setVolumeOff();
        if (isLivestream) {
            player.seekToDefaultPosition();
        } else {
            seekTo(contentPosition);
        }
        player.setPlayWhenReady(true);
    }
}
