package uizacoresdk.view.rl.videoinfo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import uizacoresdk.R;
import uizacoresdk.util.UZData;
import vn.uiza.core.utilities.LDateUtils;
import vn.uiza.core.utilities.LDisplayUtils;
import vn.uiza.core.utilities.LUIUtil;
import vn.uiza.restapi.model.v2.listallentity.Item;
import vn.uiza.restapi.model.v3.metadata.getdetailofmetadata.Data;
import vn.uiza.restapi.model.v5.PlaybackInfo;
import vn.uiza.utils.util.SentryUtils;

/**
 * Created by www.muathu@gmail.com on 18/1/2019.
 */

public class UZVideoInfo extends RelativeLayout {
    private final String TAG = "TAG" + getClass().getSimpleName();
    private Activity activity;
    private ProgressBar progressBar;
    private TextView tvVideoName;
    private TextView tvVideoTime;
    private TextView tvVideoRate;
    private TextView tvVideoDescription;
    private TextView tvVideoStarring;
    private TextView tvVideoDirector;
    private TextView tvVideoGenres;
    private TextView tvDebug;
    private TextView tvMoreLikeThisMsg;
    private NestedScrollView nestedScrollView;
    private List<Item> itemList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ItemAdapterV1 mAdapter;
    private ItemAdapterV1.Callback callback;

    public void init(ItemAdapterV1.Callback callback) {
        this.callback = callback;
        clearAllViews();
    }

    public void clearAllViews() {
        itemList.clear();
        notifyViews();
        LUIUtil.showProgressBar(progressBar);

        String s = "...";
        tvVideoName.setText(s);
        tvVideoTime.setText(s);
        tvVideoRate.setText(s);
        tvVideoDescription.setText(s);
        tvVideoStarring.setText(s);
        tvVideoDirector.setText(s);
        tvVideoGenres.setText(s);
    }

    public UZVideoInfo(Context context) {
        super(context);
        onCreate();
    }

    public UZVideoInfo(Context context, AttributeSet attrs) {
        super(context, attrs);
        onCreate();
    }

    public UZVideoInfo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onCreate();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UZVideoInfo(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onCreate();
    }

    private void onCreate() {
        inflate(getContext(), R.layout.v3_uiza_ima_video_core_info_rl, this);
        activity = (Activity) getContext();
        findViews();
    }

    private void findViews() {
        nestedScrollView = (NestedScrollView) findViewById(R.id.scroll_view);
        //nestedScrollView.setNestedScrollingEnabled(false);
        progressBar = (ProgressBar) findViewById(R.id.pb);
        LUIUtil.setColorProgressBar(progressBar, Color.WHITE);
        recyclerView = (RecyclerView) findViewById(R.id.rv);
        tvVideoName = (TextView) findViewById(R.id.tv_video_name);
        tvVideoTime = (TextView) findViewById(R.id.tv_video_time);
        tvVideoRate = (TextView) findViewById(R.id.tv_video_rate);
        tvVideoDescription = (TextView) findViewById(R.id.tv_video_description);
        tvVideoStarring = (TextView) findViewById(R.id.tv_video_starring);
        tvVideoDirector = (TextView) findViewById(R.id.tv_video_director);
        tvVideoGenres = (TextView) findViewById(R.id.tv_video_genres);
        tvDebug = (TextView) findViewById(R.id.tv_debug);
        tvMoreLikeThisMsg = (TextView) findViewById(R.id.tv_more_like_this_msg);

        int sizeW = LDisplayUtils.getScreenW(activity) / 2;
        int sizeH = sizeW * 9 / 16;
        mAdapter = new ItemAdapterV1(activity, itemList, sizeW, sizeH, new ItemAdapterV1.Callback() {
            @Override
            public void onClickItemBottom(Item item, int position) {
                if (UZData.getInstance().isSettingPlayer()) {
                    return;
                }
                itemList.clear();
                notifyViews();
                if (callback != null) {
                    callback.onClickItemBottom(item, position);
                }
            }

            @Override
            public void onLoadMore() {
                loadMore();
                if (callback != null) {
                    callback.onLoadMore();
                }
            }
        });

        recyclerView.setNestedScrollingEnabled(false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(activity, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    public void setup(Data data) {
        if (data == null) {
            Timber.e("setup resultRetrieveAnEntity == null");
            return;
        }
        PlaybackInfo info = UZData.getInstance().getPlaybackInfo();
        if (info == null || !info.canPlay()) {
            Timber.e("setup data is null");
        }
        updateUI();
    }

    public void updateUI() {
        final String emptyS = "Empty string";
        final String nullS = "Data is null";
        try {
            tvVideoName.setText(UZData.getInstance().getPlaybackInfo().getName());
        } catch (NullPointerException e) {
            tvVideoName.setText(nullS);
            SentryUtils.captureException(e);
        }
        if (UZData.getInstance().getPlaybackInfo().getCreatedAt() != null) {
            tvVideoTime.setText(LDateUtils.getDateWithoutTime(UZData.getInstance().getPlaybackInfo().getCreatedAt().toString()));
        } else {
            tvVideoTime.setText(nullS);
        }
        //TODO
        tvVideoRate.setText("12+");
        try {
            tvVideoDescription.setText(UZData.getInstance().getPlaybackInfo().getDescription().isEmpty() ? emptyS : UZData.getInstance().getPlaybackInfo().getDescription());
        } catch (NullPointerException e) {
            tvVideoDescription.setText(nullS);
            SentryUtils.captureException(e);
        }

        //TODO
        tvVideoStarring.setText("Dummy starring");

        //TODO
        tvVideoDirector.setText("Dummy director");

        //TODO
        tvVideoGenres.setText("Dummy genres");

        //get more like this video
        getListAllEntityRelation();
    }

    private void getListAllEntityRelation() {
        //TODO
        tvMoreLikeThisMsg.setText(R.string.no_data);
        tvMoreLikeThisMsg.setVisibility(View.VISIBLE);
        LUIUtil.hideProgressBar(progressBar);
    }

    private void setupUIMoreLikeThis(List<Item> itemList) {
        //LLog.d(TAG, "setupUIMoreLikeThis itemList size: " + itemList.size());
        this.itemList.addAll(itemList);
        notifyViews();
    }

    private void notifyViews() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void loadMore() {
        //do nothing
    }
}