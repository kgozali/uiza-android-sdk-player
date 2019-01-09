package uizalivestream.view;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pedro.encoder.input.gl.SpriteGestureController;
import com.pedro.encoder.input.gl.render.filters.BaseFilterRender;
import com.pedro.encoder.input.gl.render.filters.object.GifObjectFilterRender;
import com.pedro.encoder.input.gl.render.filters.object.ImageObjectFilterRender;
import com.pedro.encoder.input.gl.render.filters.object.TextObjectFilterRender;
import com.pedro.encoder.input.video.CameraHelper;
import com.pedro.encoder.utils.gl.TranslateTo;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.pedro.rtplibrary.view.OpenGlView;

import net.ossrs.rtmp.ConnectCheckerRtmp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.HttpException;
import uizalivestream.R;
import uizalivestream.interfaces.CameraCallback;
import uizalivestream.interfaces.UZLivestreamCallback;
import uizalivestream.model.PresetLiveStreamingFeed;
import vn.uiza.core.common.Constants;
import vn.uiza.core.exception.UZException;
import vn.uiza.core.utilities.LAnimationUtil;
import vn.uiza.core.utilities.LConnectivityUtil;
import vn.uiza.core.utilities.LDialogUtil;
import vn.uiza.core.utilities.LLog;
import vn.uiza.core.utilities.LScreenUtil;
import vn.uiza.core.utilities.LUIUtil;
import vn.uiza.restapi.ApiMaster;
import vn.uiza.restapi.restclient.UZRestClient;
import vn.uiza.restapi.uiza.UZService;
import vn.uiza.restapi.uiza.model.ErrorBody;
import vn.uiza.restapi.uiza.model.v3.livestreaming.startALiveFeed.BodyStartALiveFeed;
import vn.uiza.restapi.uiza.model.v3.metadata.getdetailofmetadata.Data;
import vn.uiza.rxandroid.ApiSubscriber;
import vn.uiza.utils.CallbackGetDetailEntity;
import vn.uiza.utils.UZUtilBase;
import vn.uiza.utils.util.AppUtils;
import vn.uiza.views.LToast;

/**
 * Created by loitp on 9/1/2019.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class UZLivestream extends RelativeLayout implements ConnectCheckerRtmp, SurfaceHolder.Callback, View.OnTouchListener {
    private final String TAG = "TAG" + getClass().getSimpleName();
    private Gson gson = new Gson();
    private RtmpCamera1 rtmpCamera1;
    private String currentDateAndTime = "";
    private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + AppUtils.getAppName());
    private OpenGlView openGlView;
    private SpriteGestureController spriteGestureController = new SpriteGestureController();
    private PresetLiveStreamingFeed presetLiveStreamingFeed;
    private ProgressBar progressBar;
    private TextView tvLiveStatus;
    private UZLivestreamCallback uzLivestreamCallback;
    private String mainStreamUrl;
    private CameraCallback cameraCallback;

    public void setCameraCallback(CameraCallback cameraCallback) {
        this.cameraCallback = cameraCallback;
    }

    public SpriteGestureController getSpriteGestureController() {
        return spriteGestureController;
    }

    public UZLivestream(Context context) {
        super(context);
    }

    public UZLivestream(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UZLivestream(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public UZLivestream(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public TextView getTvLiveStatus() {
        return tvLiveStatus;
    }

    public void hideTvLiveStatus() {
        if (tvLiveStatus != null) {
            tvLiveStatus.setVisibility(View.GONE);
            tvLiveStatus = null;
        }
    }

    public RtmpCamera1 getRtmpCamera() {
        return rtmpCamera1;
    }

    public OpenGlView getOpenGlView() {
        return openGlView;
    }

    public PresetLiveStreamingFeed getPresetLiveStreamingFeed() {
        return presetLiveStreamingFeed;
    }

    private void onCreate() {
        inflate(getContext(), R.layout.layout_uz_livestream, this);
        tvLiveStatus = (TextView) findViewById(R.id.tv_live_status);
        progressBar = (ProgressBar) findViewById(R.id.pb);
        LUIUtil.setColorProgressBar(progressBar, Color.WHITE);
        openGlView = (OpenGlView) findViewById(R.id.surfaceView);
        //Number of filters to use at same time.
        //You must modify it before create rtmp or rtsp object.
        //ManagerRender.numFilters = 2;
        rtmpCamera1 = new RtmpCamera1(openGlView, this);
        openGlView.getHolder().addCallback(this);
        openGlView.setOnTouchListener(this);
        if (uzLivestreamCallback != null) {
            uzLivestreamCallback.onUICreate();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (spriteGestureController.spriteTouched(view, motionEvent)) {
            spriteGestureController.moveSprite(view, motionEvent);
            spriteGestureController.scaleSprite(motionEvent);
            return true;
        }
        return false;
    }

    //Stop listener for image, text and gif stream objects.
    public void setBaseObjectFilterRender() {
        if (spriteGestureController != null) {
            spriteGestureController.setBaseObjectFilterRender(null);
        }
    }

    public void onResume() {
        if (!isShowDialogCheck) {
            checkPermission();
        }
    }

    public void destroyApiMaster() {
        ApiMaster.getInstance().destroy();
    }

    private boolean isShowDialogCheck;

    private void checkPermission() {
        //LLog.d(TAG, "checkPermission");
        isShowDialogCheck = true;
        Dexter.withActivity((Activity) getContext())
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            //LLog.d(TAG, "onPermissionsChecked do you work now");
                            onCreate();
                            if (uzLivestreamCallback != null) {
                                uzLivestreamCallback.onPermission(true);
                            }
                        } else {
                            //LLog.d(TAG, "!areAllPermissionsGranted");
                            showShouldAcceptPermission();
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            //LLog.d(TAG, "onPermissionsChecked permission is denied permenantly, navigate user to app settings");
                            showSettingsDialog();
                        }
                        isShowDialogCheck = true;
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        LLog.d(TAG, "onPermissionRationaleShouldBeShown");
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showShouldAcceptPermission() {
        AlertDialog alertDialog = LDialogUtil.showDialog2(getContext(), "Need Permissions", "This app needs permission to use this feature.", "Okay", "Cancel", new LDialogUtil.Callback2() {
            @Override
            public void onClick1() {
                checkPermission();
            }

            @Override
            public void onClick2() {
                LLog.d(TAG, "showShouldAcceptPermission onClick2");
                if (uzLivestreamCallback != null) {
                    uzLivestreamCallback.onPermission(false);
                }
            }
        });
        alertDialog.setCancelable(false);
    }

    private void showSettingsDialog() {
        AlertDialog alertDialog = LDialogUtil.showDialog2(getContext(), "Need Permissions", "This app needs permission to use this feature. You can grant them in app settings.", "GOTO SETTINGS", "Cancel", new LDialogUtil.Callback2() {
            @Override
            public void onClick1() {
                isShowDialogCheck = false;
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", AppUtils.getAppPackageName(), null);
                intent.setData(uri);
                ((Activity) getContext()).startActivityForResult(intent, 101);
            }

            @Override
            public void onClick2() {
                LLog.d(TAG, "showSettingsDialog onClick2");
                if (uzLivestreamCallback != null) {
                    uzLivestreamCallback.onPermission(false);
                }
            }
        });
        alertDialog.setCancelable(false);
    }

    private void updateUISurfaceView(int width, int height) {
        if (openGlView == null) {
            return;
        }
        int screenWidth = LScreenUtil.getScreenWidth();
        openGlView.getLayoutParams().width = screenWidth;
        openGlView.getLayoutParams().height = width * screenWidth / height;
        //LLog.d(TAG, "updateUISurfaceView " + screenWidth + "x" + (width * screenWidth / height));
        openGlView.requestLayout();
    }

    public void setUzLivestreamCallback(UZLivestreamCallback uzLivestreamCallback) {
        this.uzLivestreamCallback = uzLivestreamCallback;
    }

    @Override
    public void onConnectionSuccessRtmp() {
        //LLog.d(TAG, "onConnectionSuccessRtmp");
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tvLiveStatus != null) {
                    tvLiveStatus.setVisibility(View.VISIBLE);
                    LAnimationUtil.blinking(tvLiveStatus);
                }
                LDialogUtil.hide(progressBar);
            }
        });
        if (uzLivestreamCallback != null) {
            uzLivestreamCallback.onConnectionSuccessRtmp();
        }
        if (rtmpCamera1 != null) {
            if (cameraCallback != null) {
                cameraCallback.onCameraChange(rtmpCamera1.isFrontCamera());
            }
        }
    }

    @Override
    public void onConnectionFailedRtmp(final String reason) {
        //LLog.d(TAG, "onConnectionFailedRtmp " + reason);
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tvLiveStatus != null) {
                    tvLiveStatus.setVisibility(View.GONE);
                    tvLiveStatus.clearAnimation();
                }
                rtmpCamera1.stopStream();
            }
        });
        if (uzLivestreamCallback != null) {
            uzLivestreamCallback.onConnectionFailedRtmp(reason);
        }
    }

    @Override
    public void onDisconnectRtmp() {
        //LLog.d(TAG, "onDisconnectRtmp");
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tvLiveStatus != null) {
                    tvLiveStatus.setVisibility(View.GONE);
                    tvLiveStatus.clearAnimation();
                }
                //rtmpCamera1.stopStream();
                LDialogUtil.hide(progressBar);
            }
        });
        if (uzLivestreamCallback != null) {
            uzLivestreamCallback.onDisconnectRtmp();
        }
    }

    @Override
    public void onAuthErrorRtmp() {
        //LLog.d(TAG, "onAuthErrorRtmp");
        if (uzLivestreamCallback != null) {
            uzLivestreamCallback.onAuthErrorRtmp();
        }
    }

    @Override
    public void onAuthSuccessRtmp() {
        //LLog.d(TAG, "onAuthSuccessRtmp");
        if (uzLivestreamCallback != null) {
            uzLivestreamCallback.onAuthSuccessRtmp();
            LDialogUtil.hide(progressBar);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //LLog.d(TAG, "surfaceCreated");
        if (uzLivestreamCallback != null) {
            uzLivestreamCallback.surfaceCreated();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //rtmpCamera1.startPreview();
        //rtmpCamera1.startPreview(Camera.CameraInfo.CAMERA_FACING_FRONT);
        //rtmpCamera1.startPreview(1280, 720);
        //rtmpCamera1.startPreview(Camera.CameraInfo.CAMERA_FACING_BACK, 1280, 720);
        //rtmpCamera1.startPreview(Camera.CameraInfo.CAMERA_FACING_FRONT, 1280, 720);
        //updateUISurfaceView();

        if (uzLivestreamCallback != null) {
            uzLivestreamCallback.surfaceChanged(new StartPreview() {
                @Override
                public void onSizeStartPreview(int width, int height) {
                    //rtmpCamera1.startPreview();
                    // optionally:
                    //rtmpCamera1.startPreview(CameraHelper.Facing.BACK);
                    //or
                    //rtmpCamera1.startPreview(CameraHelper.Facing.FRONT);

                    rtmpCamera1.startPreview(CameraHelper.Facing.FRONT, width, height);
                    updateUISurfaceView(width, height);
                    //LLog.d(TAG, "uzLivestreamCallback surfaceChanged " + width + "x" + height);
                }
            });
        }
    }

    public interface StartPreview {
        public void onSizeStartPreview(int width, int height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //LLog.d(TAG, "surfaceDestroyed");
        if (rtmpCamera1.isRecording()) {
            rtmpCamera1.stopRecord();
            LToast.show(getContext(), "File " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath());
            currentDateAndTime = "";
        }
        if (rtmpCamera1.isStreaming()) {
            rtmpCamera1.stopStream();
        }
        rtmpCamera1.stopPreview();
    }

    public boolean isStreaming() {
        if (rtmpCamera1 == null) {
            return false;
        }
        return rtmpCamera1.isStreaming();
    }

    public void startStream(String streamUrl) {
        startStream(streamUrl, false);
    }

    public void startStream(String streamUrl, boolean isSavedToDevice) {
        if (rtmpCamera1 == null) {
            return;
        }
        LDialogUtil.show(progressBar);
        rtmpCamera1.startStream(streamUrl);
        LLog.d(TAG, "startStream streamUrl " + streamUrl + ", isSavedToDevice: " + isSavedToDevice);
        this.isSavedToDevice = isSavedToDevice;
        if (isSavedToDevice) {
            startRecord();
        }
    }

    private boolean isSavedToDevice;

    public boolean isSavedToDevice() {
        return isSavedToDevice;
    }

    public void stopStream() {
        if (rtmpCamera1 == null) {
            return;
        }
        if (isRecording()) {
            stopRecord();
        }
        if (rtmpCamera1.isStreaming()) {
            rtmpCamera1.stopStream();
        }
    }

    public boolean prepareAudio() {
        return prepareAudio(512 * 1024, 44100, true, true, true);
    }

    public boolean prepareAudio(int bitrate, int sampleRate, boolean isStereo, boolean echoCanceler, boolean noiseSuppressor) {
        if (rtmpCamera1 == null) {
            return false;
        }
        LLog.d(TAG, "prepareAudio ===> bitrate " + bitrate + ", sampleRate: " + sampleRate + ", isStereo: " + isStereo + ", echoCanceler: " + echoCanceler + ", noiseSuppressor: " + noiseSuppressor);
        return rtmpCamera1.prepareAudio(bitrate, sampleRate, isStereo, echoCanceler, noiseSuppressor);
    }

    public boolean prepareVideoFullHDPortrait() {
        return prepareVideoFullHD(false);
    }

    public boolean prepareVideoFullHDLandscape() {
        return prepareVideoFullHD(true);
    }

    private boolean prepareVideoFullHD(boolean isLandscape) {
        if (presetLiveStreamingFeed == null) {
            Log.e(TAG, "prepareVideoFullHD false with presetLiveStreamingFeed null");
            return false;
        }
        List<Camera.Size> bestResolutionList = getBestResolutionList();
        if (bestResolutionList == null || bestResolutionList.isEmpty()) {
            Log.e(TAG, "prepareVideoFullHD false -> bestResolutionList null or empty");
            return false;
        }
        /*for (int i = 0; i < bestResolutionList.size(); i++) {
            LLog.d(TAG, "prepareVideoFullHD " + bestResolutionList.get(i).width + "x" + bestResolutionList.get(i).height);
        }*/
        Camera.Size bestSize = bestResolutionList.get(0);
        int bestBitrate = getBestBitrate();
        //return prepareVideo(1920, 1080, 30, presetLiveStreamingFeed.getS1080p(), false, isLandscape ? 0 : 90);
        return prepareVideo(bestSize.width, bestSize.height, 30, bestBitrate, false, isLandscape ? 0 : 90);
    }

    public boolean prepareVideoHDPortrait() {
        return prepareVideoHD(false);
    }

    public boolean prepareVideoHDLandscape() {
        return prepareVideoHD(true);
    }

    private boolean prepareVideoHD(boolean isLandscape) {
        if (presetLiveStreamingFeed == null) {
            Log.e(TAG, "prepareVideoHD false with presetLiveStreamingFeed null");
            return false;
        }
        List<Camera.Size> bestResolutionList = getBestResolutionList();
        if (bestResolutionList == null || bestResolutionList.isEmpty()) {
            Log.e(TAG, "prepareVideoHD false -> bestResolutionList null or empty");
            return false;
        }
        /*for (int i = 0; i < bestResolutionList.size(); i++) {
            LLog.d(TAG, "prepareVideoHD " + bestResolutionList.get(i).width + "x" + bestResolutionList.get(i).height);
        }*/
        int sizeList = bestResolutionList.size();
        int index;
        if (sizeList > 2) {
            index = sizeList / 2;
        } else if (sizeList == 2) {
            index = 1;
        } else {
            index = 0;
        }
        //LLog.d(TAG, "index " + index);
        Camera.Size bestSize = bestResolutionList.get(index);
        int bestBitrate = getBestBitrate();
        //return prepareVideo(1280, 720, 30, presetLiveStreamingFeed.getS720p(), false, isLandscape ? 0 : 90);
        return prepareVideo(bestSize.width, bestSize.height, 30, bestBitrate, false, isLandscape ? 0 : 90);
    }

    public boolean prepareVideoSDPortrait() {
        return prepareVideoSD(false);
    }

    public boolean prepareVideoSDLandscape() {
        return prepareVideoSD(true);
    }

    private boolean prepareVideoSD(boolean isLandscape) {
        if (presetLiveStreamingFeed == null) {
            Log.e(TAG, "prepareVideoSD false with presetLiveStreamingFeed null");
            return false;
        }
        List<Camera.Size> bestResolutionList = getBestResolutionList();
        if (bestResolutionList == null || bestResolutionList.isEmpty()) {
            Log.e(TAG, "prepareVideoSD false -> bestResolutionList null or empty");
            return false;
        }
        /*for (int i = 0; i < bestResolutionList.size(); i++) {
            LLog.d(TAG, "prepareVideoSD " + bestResolutionList.get(i).width + "x" + bestResolutionList.get(i).height);
        }*/
        Camera.Size bestSize = bestResolutionList.get(bestResolutionList.size() - 1);
        int bestBitrate = getBestBitrate();
        //return prepareVideo(640, 360, 30, presetLiveStreamingFeed.getS480p(), false, isLandscape ? 0 : 90);
        return prepareVideo(bestSize.width, bestSize.height, 30, bestBitrate, false, isLandscape ? 0 : 90);
    }

    public boolean prepareVideoPortrait() {
        return prepareVideo(false);
    }

    public boolean prepareVideoLandscape() {
        return prepareVideo(true);
    }

    private boolean prepareVideo(boolean isLandscape) {
        if (presetLiveStreamingFeed == null) {
            Log.e(TAG, "prepareVideo false with presetLiveStreamingFeed null");
            return false;
        }
        if (rtmpCamera1 == null) {
            Log.e(TAG, "prepareVideo false -> rtmpCamera1 == null");
            return false;
        }
        //boolean isFrontCamera = rtmpCamera1.isFrontCamera();
        //LLog.d(TAG, "isFrontCamera " + isFrontCamera);
        Camera.Size bestSize = getBestResolution();
        int bestBitrate = getBestBitrate();
        if (bestSize == null) {
            Log.e(TAG, "prepareVideo false -> bestSize == null");
            return false;
        }
        //return prepareVideo(640, 360, 30, presetLiveStreamingFeed.getS480p(), false, isLandscape ? 0 : 90);
        return prepareVideo(bestSize.width, bestSize.height, 30, bestBitrate, false, isLandscape ? 0 : 90);
    }

    private List<Camera.Size> getBestResolutionList() {
        //WORKS FINE
        /*List<Camera.Size> sizeListFront = rtmpCamera1.getResolutionsFront();
        List<Camera.Size> sizeListBack = rtmpCamera1.getResolutionsBack();
        if (sizeListFront == null || sizeListFront.isEmpty() || sizeListBack == null || sizeListBack.isEmpty()) {
            return null;
        }
        List<Camera.Size> bestList = new ArrayList<>();
        //scan sizeListFront
        List<Camera.Size> bestResolutionFrontList = new ArrayList<>();
        for (int i = 0; i < sizeListFront.size(); i++) {
            Camera.Size size = sizeListFront.get(i);
            float w = size.width;
            float h = size.height;
            float ratioWH = w / h;
            LLog.d(TAG, "front " + i + " -> " + w + "x" + h + " -> " + ratioWH);
            if (ratioWH == 16f / 9f) {
                bestResolutionFrontList.add(size);
            }
        }
        //scan sizeListBack
        List<Camera.Size> bestResolutionBackList = new ArrayList<>();
        for (int i = 0; i < sizeListFront.size(); i++) {
            Camera.Size size = sizeListFront.get(i);
            float w = size.width;
            float h = size.height;
            float ratioWH = w / h;
            LLog.d(TAG, "back " + i + " -> " + w + "x" + h + " -> " + ratioWH);
            if (ratioWH == 16f / 9f) {
                bestResolutionBackList.add(size);
            }
        }
        //get same size between front and back list
        for (int i = 0; i < bestResolutionFrontList.size(); i++) {
            Camera.Size sizeF = bestResolutionFrontList.get(i);
            for (int j = 0; j < bestResolutionBackList.size(); j++) {
                Camera.Size sizeB = bestResolutionBackList.get(j);
                if (sizeF.width == sizeB.width && sizeF.height == sizeB.height) {
                    bestList.add(sizeF);
                }
            }
        }
        for (int i = 0; i < bestList.size(); i++) {
            LLog.d(TAG, "final " + bestList.get(i).width + "x" + bestList.get(i).height);
        }
        return bestList;*/
        List<Camera.Size> sizeListFront = rtmpCamera1.getResolutionsFront();
        if (sizeListFront == null || sizeListFront.isEmpty()) {
            return null;
        }
        List<Camera.Size> bestResolutionFrontList = new ArrayList<>();
        for (int i = 0; i < sizeListFront.size(); i++) {
            Camera.Size size = sizeListFront.get(i);
            float w = size.width;
            float h = size.height;
            float ratioWH = w / h;
            //LLog.d(TAG, "front " + i + " -> " + w + "x" + h + " -> " + ratioWH);
            if (ratioWH == 16f / 9f) {
                bestResolutionFrontList.add(size);
            }
        }
        /*for (int i = 0; i < bestResolutionFrontList.size(); i++) {
            LLog.d(TAG, "final " + bestResolutionFrontList.get(i).width + "x" + bestResolutionFrontList.get(i).height);
        }*/
        return bestResolutionFrontList;
    }

    private Camera.Size getBestResolution() {
        List<Camera.Size> bestResolutionList = getBestResolutionList();
        if (bestResolutionList == null || bestResolutionList.isEmpty()) {
            return null;
        }
        int sizeList = bestResolutionList.size();
        int index;
        if (LConnectivityUtil.isConnectedFast(getContext()) && LConnectivityUtil.isConnectedWifi(getContext())) {
            index = 0;
        } else if (LConnectivityUtil.isConnectedFast(getContext()) && LConnectivityUtil.isConnectedMobile(getContext())) {
            if (sizeList > 2) {
                index = sizeList / 2;
            } else if (sizeList == 2) {
                index = 1;
            } else {
                index = 0;
            }
        } else {
            index = sizeList - 1;
        }
        return bestResolutionList.get(index);
    }

    private int getBestBitrate() {
        if (LConnectivityUtil.isConnectedFast(getContext()) && LConnectivityUtil.isConnectedWifi(getContext())) {
            return presetLiveStreamingFeed.getS1080p();
        } else if (LConnectivityUtil.isConnectedFast(getContext()) && LConnectivityUtil.isConnectedMobile(getContext())) {
            return presetLiveStreamingFeed.getS720p();
        } else {
            return presetLiveStreamingFeed.getS480p();
        }
    }

    public boolean prepareVideo(int width, int height, int fps, int bitrate, boolean hardwareRotation, int rotation) {
        if (rtmpCamera1 == null) {
            return false;
        }
        if (presetLiveStreamingFeed == null) {
            Log.e(TAG, "prepareVideoFullHD false with presetLiveStreamingFeed null");
            return false;
        }
        LLog.d(TAG, "prepareVideo ===> " + width + "x" + height + ", bitrate " + bitrate + ", fps: " + fps + ", rotation: " + rotation + ", hardwareRotation: " + hardwareRotation);
        rtmpCamera1.startPreview(width, height);
        return rtmpCamera1.prepareVideo(width, height, fps, bitrate, hardwareRotation, rotation);
    }

    public void switchCamera() {
        if (rtmpCamera1 != null) {
            rtmpCamera1.switchCamera();
            if (cameraCallback != null) {
                cameraCallback.onCameraChange(rtmpCamera1.isFrontCamera());
            }
        }
    }

    public boolean isRecording() {
        if (rtmpCamera1 == null) {
            return false;
        }
        return rtmpCamera1.isRecording();
    }

    private void startRecord() {
        if (rtmpCamera1 == null) {
            return;
        }
        if (!isStreaming()) {
            LLog.e(TAG, "startRecord !isStreaming() -> return");
            return;
        }
        try {
            if (!folder.exists()) {
                folder.mkdir();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            currentDateAndTime = sdf.format(new Date());
            rtmpCamera1.startRecord(folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
            LLog.d(TAG, "Recording...");
        } catch (IOException e) {
            rtmpCamera1.stopRecord();
            LLog.e(TAG, "Error startRecord " + e.toString());
        }
    }

    private void stopRecord() {
        if (rtmpCamera1 == null) {
            return;
        }
        rtmpCamera1.stopRecord();
        LToast.show(getContext(), "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath());
        currentDateAndTime = "";
    }

    public boolean isAAEnabled() {
        if (rtmpCamera1 == null) {
            return false;
        }
        return rtmpCamera1.getGlInterface().isAAEnabled();
    }

    /*
     **AAEnabled true is AA enabled, false is AA disabled. False by default.
     */
    public void enableAA(boolean isEnable) {
        if (rtmpCamera1 == null) {
            return;
        }
        rtmpCamera1.getGlInterface().enableAA(isEnable);
        //filters. NOTE: You can change filter values on fly without reset the filter.
        // Example:
        // ColorFilterRender color = new ColorFilterRender()
        // rtmpCamera1.setFilter(color);
        // color.setRGBColor(255, 0, 0); //red tint
    }

    public void setFilter(BaseFilterRender baseFilterRender) {
        if (rtmpCamera1 == null) {
            return;
        }
        rtmpCamera1.getGlInterface().setFilter(baseFilterRender);
    }

    public int getStreamWidth() {
        if (rtmpCamera1 == null) {
            return 0;
        }
        return rtmpCamera1.getStreamWidth();
    }

    public int getStreamHeight() {
        if (rtmpCamera1 == null) {
            return 0;
        }
        return rtmpCamera1.getStreamHeight();
    }

    public void setTextToStream(String text, int textSize, int textCorlor, TranslateTo translateTo) {
        if (rtmpCamera1 == null) {
            return;
        }
        TextObjectFilterRender textObjectFilterRender = new TextObjectFilterRender();
        rtmpCamera1.getGlInterface().setFilter(textObjectFilterRender);
        //textObjectFilterRender.setText("Hello world", 22, Color.RED);
        textObjectFilterRender.setText(text, textSize, textCorlor);
        textObjectFilterRender.setDefaultScale(rtmpCamera1.getStreamWidth(), rtmpCamera1.getStreamHeight());
        //textObjectFilterRender.setPosition(TranslateTo.CENTER);
        textObjectFilterRender.setPosition(translateTo);
        spriteGestureController.setBaseObjectFilterRender(textObjectFilterRender); //Optional
    }

    public void setImageToStream(int res, TranslateTo translateTo) {
        if (rtmpCamera1 == null) {
            return;
        }
        ImageObjectFilterRender imageObjectFilterRender = new ImageObjectFilterRender();
        rtmpCamera1.getGlInterface().setFilter(imageObjectFilterRender);
        imageObjectFilterRender.setImage(BitmapFactory.decodeResource(getResources(), res));
        imageObjectFilterRender.setDefaultScale(rtmpCamera1.getStreamWidth(), rtmpCamera1.getStreamHeight());
        imageObjectFilterRender.setPosition(translateTo);
        spriteGestureController.setBaseObjectFilterRender(imageObjectFilterRender); //Optional
    }

    public boolean setGifToStream(int res, TranslateTo translateTo) {
        if (rtmpCamera1 == null) {
            return false;
        }
        try {
            GifObjectFilterRender gifObjectFilterRender = new GifObjectFilterRender();
            gifObjectFilterRender.setGif(getResources().openRawResource(res));
            rtmpCamera1.getGlInterface().setFilter(gifObjectFilterRender);
            gifObjectFilterRender.setDefaultScale(rtmpCamera1.getStreamWidth(), rtmpCamera1.getStreamHeight());
            //gifObjectFilterRender.setPosition(TranslateTo.BOTTOM);
            gifObjectFilterRender.setPosition(translateTo);
            spriteGestureController.setBaseObjectFilterRender(gifObjectFilterRender); //Optional
            return true;
        } catch (IOException e) {
            LLog.e(TAG, "Error setGifToStream " + e.toString());
            return false;
        }
    }

    public String getMainStreamUrl() {
        return mainStreamUrl;
    }

    public void setId(final String entityLiveId) {
        if (entityLiveId == null || entityLiveId.isEmpty()) {
            throw new NullPointerException(UZException.ERR_5);
        }
        startLivestream(entityLiveId);
    }

    //Chi can goi start live thoi, khong can quan tam den ket qua cua api nay start success hay ko
    //Van tiep tuc goi detail entity de lay streamUrl
    private void startLivestream(final String entityLiveId) {
        LDialogUtil.show(progressBar);
        UZService service = UZRestClient.createService(UZService.class);
        BodyStartALiveFeed bodyStartALiveFeed = new BodyStartALiveFeed();
        bodyStartALiveFeed.setId(entityLiveId);
        ApiMaster.getInstance().subscribe(service.startALiveEvent(bodyStartALiveFeed), new ApiSubscriber<Object>() {
            @Override
            public void onSuccess(Object result) {
                //LLog.d(TAG, "startLivestream onSuccess " + gson.toJson(result));
                getDetailEntity(entityLiveId, false, null);
            }

            @Override
            public void onFail(Throwable e) {
                Log.e(TAG, ">>>>>>startLivestream onFail " + e.toString() + ", " + e.getMessage());
                try {
                    HttpException error = (HttpException) e;
                    String responseBody = null;
                    try {
                        responseBody = error.response().errorBody().string();
                        Log.e(TAG, "responseBody " + responseBody);
                        ErrorBody errorBody = gson.fromJson(responseBody, ErrorBody.class);
                        getDetailEntity(entityLiveId, true, errorBody.getMessage());
                    } catch (IOException e1) {
                        Log.e(TAG, "startLivestream IOException catch " + e1.toString());
                        getDetailEntity(entityLiveId, true, e1.getMessage());
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "startLivestream Exception catch " + ex.toString());
                    getDetailEntity(entityLiveId, true, ex.getMessage());
                }
            }
        });
    }

    private void getDetailEntity(String entityLiveId, final boolean isErrorStartLive, final String errorMsg) {
        UZUtilBase.getDataFromEntityIdLIVE((Activity) getContext(), entityLiveId, new CallbackGetDetailEntity() {
            @Override
            public void onSuccess(Data d) {
                //LLog.d(TAG, "init getDetailEntity onSuccess: " + gson.toJson(d));
                if (d == null || d.getLastPushInfo() == null || d.getLastPushInfo().isEmpty() || d.getLastPushInfo().get(0) == null) {
                    throw new NullPointerException("Data is null");
                }
                String streamKey = d.getLastPushInfo().get(0).getStreamKey();
                String streamUrl = d.getLastPushInfo().get(0).getStreamUrl();
                String mainUrl = streamUrl + "/" + streamKey;
                mainStreamUrl = mainUrl;
                LLog.d(TAG, ">>>>mainStreamUrl: " + mainStreamUrl);

                boolean isTranscode = d.getEncode() == 1;//1 is Push with Transcode, !1 Push-only, no transcode
                LLog.d(TAG, "isTranscode " + isTranscode);

                presetLiveStreamingFeed = new PresetLiveStreamingFeed();
                presetLiveStreamingFeed.setTranscode(isTranscode);

                boolean isConnectedFast = LConnectivityUtil.isConnectedFast(getContext());
                if (isTranscode) {
                    //Push with Transcode
                    presetLiveStreamingFeed.setS1080p(isConnectedFast ? 5000000 : 2500000);
                    presetLiveStreamingFeed.setS720p(isConnectedFast ? 3000000 : 1500000);
                    presetLiveStreamingFeed.setS480p(isConnectedFast ? 1500000 : 800000);
                } else {
                    //Push-only, no transcode
                    presetLiveStreamingFeed.setS1080p(isConnectedFast ? 2500000 : 1500000);
                    presetLiveStreamingFeed.setS720p(isConnectedFast ? 1500000 : 800000);
                    presetLiveStreamingFeed.setS480p(isConnectedFast ? 800000 : 400000);
                }
                LLog.d(TAG, "isErrorStartLive " + isErrorStartLive);
                if (isErrorStartLive) {
                    if (d.getLastProcess() == null) {
                        if (uzLivestreamCallback != null) {
                            //LLog.d(TAG, "isErrorStartLive -> onError Last process null");
                            uzLivestreamCallback.onError("Error: Last process null");
                        }
                    } else {
                        //LLog.d(TAG, "getLastProcess " + d.getLastProcess());
                        if ((d.getLastProcess().toLowerCase().equals(Constants.LAST_PROCESS_STOP))) {
                            LLog.d(TAG, "Start live 400 but last process STOP -> cannot livestream");
                            if (uzLivestreamCallback != null) {
                                uzLivestreamCallback.onError(errorMsg);
                            }
                        } else {
                            LLog.d(TAG, "Start live 400 but last process START || INIT -> can livestream");
                            if (uzLivestreamCallback != null) {
                                uzLivestreamCallback.onGetDataSuccess(d, mainStreamUrl, isTranscode, presetLiveStreamingFeed);
                            }
                        }
                    }
                } else {
                    if (uzLivestreamCallback != null) {
                        LLog.d(TAG, "onGetDataSuccess");
                        uzLivestreamCallback.onGetDataSuccess(d, mainStreamUrl, isTranscode, presetLiveStreamingFeed);
                    }
                }
                LUIUtil.hideProgressBar(progressBar);
                //LLog.d(TAG, "===================finish");
            }

            @Override
            public void onError(Throwable e) {
                //LLog.e(TAG, "setId onError " + e.toString());
                LUIUtil.hideProgressBar(progressBar);
                if (uzLivestreamCallback != null) {
                    uzLivestreamCallback.onError(e.getMessage());
                }
            }
        });
    }

    public int[] getBestSizePreview() {
        List<Camera.Size> sizeList = getBestResolutionList();
        int[] result = new int[2];
        if (sizeList == null || sizeList.isEmpty()) {
            result[0] = LScreenUtil.getScreenWidth();
            result[1] = LScreenUtil.getScreenHeight();
        } else {
            result[0] = sizeList.get(0).width;
            result[1] = sizeList.get(0).height;
        }
        return result;
    }

    public void enableLantern() {
        if (rtmpCamera1 == null) {
            return;
        }
        try {
            rtmpCamera1.enableLantern();
        } catch (Exception e) {
            LLog.e(TAG, "toggleFlash " + e.toString());
        }
    }

    public void disableLantern() {
        if (rtmpCamera1 == null) {
            return;
        }
        rtmpCamera1.disableLantern();
    }

    public void toggleLantern() {
        if (rtmpCamera1 == null) {
            return;
        }
        Boolean isLanternEnabled = isLanternEnabled();
        if (isLanternEnabled == null) {
            return;
        }
        if (isLanternEnabled) {
            disableLantern();
        } else {
            enableLantern();
        }
    }

    public Boolean isLanternEnabled() {
        if (rtmpCamera1 == null) {
            return null;
        }
        return rtmpCamera1.isLanternEnabled();
    }

    public void stopPreview() {
        if (rtmpCamera1 != null) {
            rtmpCamera1.stopPreview();
        }
    }
}