package vn.uiza.core.common;

/**
 * Created by loitp
 */

//https://github.com/nshmura/SnappySmoothScroller

public class Constants {
    public static boolean IS_DEBUG = false;
    public static final String PLAYER_NAME = "UZSDK";
    public static final String USER_AGENT = "Loitp93";
    public static final String PLAYER_SDK_VERSION = "3.1.9";

    public static void setDebugMode(boolean isDebugEnable) {
        IS_DEBUG = isDebugEnable;
    }

    public static final int NOT_FOUND = -404;
    public static final int UNKNOW = -400;

    public static int TYPE_ACTIVITY_TRANSITION_NO_ANIM = -1;
    public static int TYPE_ACTIVITY_TRANSITION_SYSTEM_DEFAULT = 0;
    public static int TYPE_ACTIVITY_TRANSITION_SLIDELEFT = 1;
    public static int TYPE_ACTIVITY_TRANSITION_SLIDERIGHT = 2;
    public static int TYPE_ACTIVITY_TRANSITION_SLIDEDOWN = 3;
    public static int TYPE_ACTIVITY_TRANSITION_SLIDEUP = 4;
    public static int TYPE_ACTIVITY_TRANSITION_FADE = 5;
    public static int TYPE_ACTIVITY_TRANSITION_ZOOM = 6;
    public static int TYPE_ACTIVITY_TRANSITION_WINDMILL = 7;
    public static int TYPE_ACTIVITY_TRANSITION_DIAGONAL = 8;
    public static int TYPE_ACTIVITY_TRANSITION_SPIN = 9;

    public final static String TEST_0 = "6E0762FF2B272D5BCE89FEBAAB872E34";
    public final static String TEST_1 = "8FA8E91902B43DCB235ED2F6BBA9CAE0";
    public final static String TEST_2 = "58844B2E50AF6E33DC818387CC50E593";
    public final static String TEST_3 = "179198315EB7B069037C5BE8DEF8319A";
    public final static String TEST_4 = "7DA8A5B216E868636B382A7B9756A4E6";
    public final static String TEST_5 = "A1EC01C33BD69CD589C2AF605778C2E6";
    public final static String TEST_6 = "13308851AEDCA44443112D80A8D182CA";

    public final static String KEY_UIZA_ENTITY_ID = "KEY_UIZA_ENTITY_ID";
    public final static String KEY_UIZA_ENTITY_COVER = "KEY_UIZA_ENTITY_COVER";
    public final static String KEY_UIZA_ENTITY_TITLE = "KEY_UIZA_ENTITY_TITLE";
    public final static String KEY_UIZA_METADATA_ENTITY_ID = "KEY_UIZA_METADATA_ENTITY_ID";
    public final static String KEY_UIZA_THUMBNAIL = "KEY_UIZA_THUMBNAIL";

    public final static String URL_IMG = "https://c1.staticflickr.com/9/8438/28818520263_c7ea1b3e3f_b.jpg";
    public final static String URL_IMG_16x9 = "https://static.uiza.io/2017/11/27/uiza-logo-demo-mobile.png";
    public final static String URL_IMG_9x16 = "https://c1.staticflickr.com/5/4771/38893576530_b585463c07_b.jpg";
    public final static String URL_IMG_LONG = "https://c2.staticflickr.com/6/5476/29412311793_8067369e64_b.jpg";

    public final static String TOKEN_DEV_V1 = "zHiQCup9CzTr1eP5ZQsbPK5sYNYa8kRL-1517457089350";
    public final static String TOKEN_WTT = "lsn9LZdm0MBrhGlyrFYqJYSjJfIXX27e-1512986583784";
    public final static String TOKEN_STAG = "zHiQCup9CzTr1eP5ZQsbPK5sYNYa8kRL-1517457089350";

    public final static String URL_IMG_POSTER_SPIDER_MAN = "https://ksassets.timeincuk.net/wp/uploads/sites/54/2018/06/Marvels-Spider-Man_2018_06-11-18_003-1024x576.jpg";
    public final static String URL_IMG_POSTER_MOMO = "https://kenh14cdn.com/2018/4/27/photo-15-15248224863571678048157.jpg";
    public final static String URL_IMG_POSTER = "https://static.uiza.io/2017/11/27/uiza-logo-demo-mobile.png";
    public final static String URL_IMG_THUMBNAIL_BLACK = "https://static.uiza.io/black_1px.jpg";
    public final static String URL_IMG_THUMBNAIL = "https://static.uiza.io/2017/11/27/uiza-logo-demo-mobile.png";
    public final static String URL_IMG_THUMBNAIL_2 = "https://static.uiza.io/2017/11/27/uiza-logo-1511755911349_1511755913189.png";

    public final static String PREFIX = "http://";
    public final static String PREFIXS = "https://";
    public final static String PREFIXS_SHORT = "https:";

    public final static String URL_GET_LINK_PLAY_DEV = "https://dev-ucc.uizadev.io/";
    public final static String URL_GET_LINK_PLAY_STAG = "https://stag-ucc.uizadev.io/";
    public final static String URL_GET_LINK_PLAY_PROD = "https://ucc.uiza.io/";

    public final static String URL_TRACKING_DEV = "https://dev-tracking.uizadev.io/analytic-tracking/";
    public final static String URL_TRACKING_STAG = "https://stag-tracking.uiza.io/analytic-tracking/";
    public final static String URL_TRACKING_PROD = "https://tracking.uiza.io/analytic-tracking/";

    public final static String TRACKING_ACCESS_TOKEN_DEV = "kv8O7hLkeDtN3EBviXLD01gzNz2RP2nA";
    public final static String TRACKING_ACCESS_TOKEN_STAG = "082c2cbf515648db96069fa660523247";
    public final static String TRACKING_ACCESS_TOKEN_PROD = "27cdc337bd65420f8a88cfbd9cf8577a";

    public final static String URL_HEART_BEAT_DEV = "https://dev-heartbeat.uizadev.io/";
    public final static String URL_HEART_BEAT_STAG = "https://stag-heartbeat.uizadev.io/";
    public final static String URL_HEART_BEAT_PROD = "https://heartbeat.uiza.io/";

    public final static String URL_DEV_UIZA_VERSION_2 = "http://dev-api.uiza.io/";
    public final static String URL_DEV_UIZA_VERSION_2_STAG = "https://uqc-api.uiza.io/";
    public final static String URL_DEV_UIZA_VERSION_2_DEMO = "https://demo-api.uiza.io/";

    public final static String MUIZA_EVENT_ENDED = "ended";

    public final static int ENVIRONMENT_DEV = 1;
    public final static int ENVIRONMENT_STAG = 2;
    public final static int ENVIRONMENT_PROD = 3;

    public final static String T = "true";
    public final static String F = "false";

    public final static int PLAYTHROUGH_25 = 25;
    public final static int PLAYTHROUGH_50 = 50;
    public final static int PLAYTHROUGH_75 = 75;
    public final static int PLAYTHROUGH_100 = 98;

    //public final static String FLOAT_CURRENT_POSITION = "FLOAT_CURRENT_POSITION";
    public final static String FLOAT_USER_USE_CUSTOM_LINK_PLAY = "FLOAT_USER_USE_CUSTOM_LINK_PLAY";
    public final static String FLOAT_LINK_PLAY = "FLOAT_LINK_PLAY";
    public final static String FLOAT_CONTENT_POSITION = "FLOAT_CONTENT_POSITION";
    public final static String FLOAT_PROGRESS_BAR_COLOR = "FLOAT_PROGRESS_BAR_COLOR";
    public final static String FLOAT_IS_LIVESTREAM = "FLOAT_IS_LIVESTREAM";
    public final static String FLOAT_UUID = "FLOAT_UUID";
    //public final static String FLOAT_IS_FREE_SIZE = "FLOAT_IS_FREE_SIZE";

    public static final String EVENT_TYPE_DISPLAY = "display";
    public static final String EVENT_TYPE_PLAYS_REQUESTED = "plays_requested";
    public static final String EVENT_TYPE_VIDEO_STARTS = "video_starts";
    public static final String EVENT_TYPE_VIEW = "view";
    public static final String EVENT_TYPE_REPLAY = "replay";
    public static final String EVENT_TYPE_PLAY_THROUGHT = "play_through";

    public static final int ANIMATION_DURATION = 200;

    public static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 6969;

    public static final int RATIO_LAND_TABLET = 24;
    public static final int RATIO_PORTRAIT_TABLET = 20;

    public static final int RATIO_LAND_MOBILE = 18;
    public static final int RATIO_PORTRAIT_MOBILE = 12;

    public static final String LAST_PROCESS_START = "start";
    public static final String LAST_PROCESS_STOP = "stop";
    public static final String LAST_PROCESS_INIT = "init";

    public static final String MODE_PULL = "pull";
    public static final String MODE_PUSH = "push";

    public static final String SUCCESS = "success";
    public static final String NOT_READY = "not-ready";
    public static final String ERROR = "error";

    public static final String PLATFORM_ANDROID = "android";
    public static final String PLATFORM_IOS = "ios";
    public static final String PLATFORM_WEBSITE = "website";

    public static final float RATIO_9_16 = 9f / 16f;
    public static final float RATIO_10_16 = 10f / 16f;
    public static final float RATIO_11_16 = 11f / 16f;
    public static final float RATIO_12_16 = 12f / 16f;
    public static final float RATIO_16_16 = 1;

    public static final String DRM_SCHEME_NULL = null;
    public static final String DRM_SCHEME_PLAYREADY = "playready";
    public static final String DRM_SCHEME_WIDEVINE = "widevine";

    public static final int W_320 = 320;
    public static final int W_180 = 180;
}
