package vn.uiza.core.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class LConnectivityUtil {

    /**
     * Get the network info
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        if (context == null) {
            return null;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity
     */
    public static boolean isConnected(Context context) {
        NetworkInfo info = LConnectivityUtil.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity to a Wifi network
     */
    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = LConnectivityUtil.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     */
    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = LConnectivityUtil.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is fast connectivity
     */
    public static boolean isConnectedFast(Context context) {
        NetworkInfo info = LConnectivityUtil.getNetworkInfo(context);
        return (info != null && info.isConnected() && LConnectivityUtil.isConnectionFast(info.getType(), info.getSubtype()));
    }

    /**
     * Check if the connection is fast
     */
    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS: // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8, // ~25 kbps
                    return false;
                case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA: // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA: // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11, ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9, ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13, ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11, // ~ 10+ Mbps
                    return true;
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

}