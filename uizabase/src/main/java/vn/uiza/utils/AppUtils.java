package vn.uiza.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class AppUtils {

    private AppUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }


    public static boolean checkChromeCastAvailable() {
        return AppUtils.isDependencyAvailable("com.google.android.gms.cast.framework.OptionsProvider")
                && AppUtils.isDependencyAvailable("androidx.mediarouter.app.MediaRouteButton");
    }

    public static boolean isAdsDependencyAvailable() {
        return AppUtils.isDependencyAvailable("com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer");
    }

    public static boolean isDependencyAvailable(String dependencyClass) {
        try {
            Class.forName(dependencyClass);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void installApp(Activity activity, File file, String authority, int requestCode) {
        if (!FileUtils.isFileExists(file)) return;
        activity.startActivityForResult(IntentUtils.getInstallAppIntent(activity, file, authority), requestCode);
    }

    public static boolean installAppSilent(Context context, String filePath) {
        File file = FileUtils.getFileByPath(filePath);
        if (!FileUtils.isFileExists(file)) return false;
        String command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install " + filePath;
        ShellUtils.CommandResult commandResult = ShellUtils.execCmd(command, !isSystemApp(context), true);
        return commandResult.successMsg != null && commandResult.successMsg.toLowerCase().contains("success");
    }

    public static void uninstallApp(Activity activity, String packageName, int requestCode) {
        if (isSpace(packageName)) return;
        activity.startActivityForResult(IntentUtils.getUninstallAppIntent(packageName), requestCode);
    }

    public static boolean uninstallAppSilent(Context context, boolean isKeepData) {
        if (isSpace(context.getPackageName())) return false;
        String command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm uninstall " + (isKeepData ? "-k " : "") + context.getPackageName();
        ShellUtils.CommandResult commandResult = ShellUtils.execCmd(command, !isSystemApp(context), true);
        return commandResult.successMsg != null && commandResult.successMsg.toLowerCase().contains("success");
    }

    public static void launchApp(Activity activity, String packageName, int requestCode) {
        if (isSpace(packageName)) return;
        activity.startActivityForResult(IntentUtils.getLaunchAppIntent(activity, packageName), requestCode);
    }


    public static void getAppDetailsSettings(Context context) {
        getAppDetailsSettings(context, context.getPackageName());
    }

    public static void getAppDetailsSettings(Context context, String packageName) {
        if (isSpace(packageName)) return;
        context.startActivity(IntentUtils.getAppDetailsSettingsIntent(packageName));
    }

    public static String getAppName(Context context) {
        return getAppName(context.getPackageManager(), context.getPackageName());
    }

    public static String getAppName(PackageManager pm, String packageName) {
        if (isSpace(packageName)) return null;
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? null : pi.applicationInfo.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            SentryUtils.captureException(e);
            return null;
        }
    }

    public static Drawable getAppIcon(Context context) {
        return getAppIcon(context.getPackageManager(), context.getPackageName());
    }

    public static Drawable getAppIcon(PackageManager pm, String packageName) {
        if (isSpace(packageName)) return null;
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? null : pi.applicationInfo.loadIcon(pm);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            SentryUtils.captureException(e);
            return null;
        }
    }

    public static String getAppPath(Context context) {
        return getAppPath(context.getPackageManager(), context.getPackageName());
    }

    public static String getAppPath(PackageManager pm, String packageName) {
        if (isSpace(packageName)) return null;
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? null : pi.applicationInfo.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            SentryUtils.captureException(e);
            return null;
        }
    }

    public static String getAppVersionName(Context context) {
        return getAppVersionName(context.getPackageManager(), context.getPackageName());
    }

    public static String getAppVersionName(PackageManager pm, String packageName) {
        if (isSpace(packageName)) return null;
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? null : pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            SentryUtils.captureException(e);
            return null;
        }
    }

    public static int getAppVersionCode(Context context) {
        return getAppVersionCode(context.getPackageManager(), context.getPackageName());
    }


    public static int getAppVersionCode(PackageManager pm, String packageName) {
        if (isSpace(packageName)) return -1;
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? -1 : pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            SentryUtils.captureException(e);
            return -1;
        }
    }

    public static boolean isSystemApp(Context context) {
        return isSystemApp(context.getPackageManager(), context.getPackageName());
    }

    public static boolean isSystemApp(PackageManager pm, String packageName) {
        if (isSpace(packageName)) return false;
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return ai != null && (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            SentryUtils.captureException(e);
            return false;
        }
    }

    public static int getVolumePercentage(@NonNull Context context, int streamType) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(streamType);
        int maxVolume = audioManager.getStreamMaxVolume(streamType);
        return Math.round(currentVolume * 1f / maxVolume * 100);
    }

    public static boolean isAppForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> info = manager.getRunningAppProcesses();
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningAppProcessInfo aInfo : info) {
            if (aInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return aInfo.processName.equals(context.getPackageName());
            }
        }
        return false;
    }

    public static boolean isAppForeground(Context context, String packageName) {
        return !isSpace(packageName) && packageName.equals(ProcessUtils.getForegroundProcessName(context));
    }

    public static class AppInfo {

        private String name;
        private Drawable icon;
        private String packageName;
        private String packagePath;
        private String versionName;
        private int versionCode;
        private boolean isSystem;

        public Drawable getIcon() {
            return icon;
        }

        public void setIcon(Drawable icon) {
            this.icon = icon;
        }

        public boolean isSystem() {
            return isSystem;
        }

        public void setSystem(boolean isSystem) {
            this.isSystem = isSystem;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getPackagePath() {
            return packagePath;
        }

        public void setPackagePath(String packagePath) {
            this.packagePath = packagePath;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public AppInfo(String packageName, String name, Drawable icon, String packagePath,
                       String versionName, int versionCode, boolean isSystem) {
            this.setName(name);
            this.setIcon(icon);
            this.setPackageName(packageName);
            this.setPackagePath(packagePath);
            this.setVersionName(versionName);
            this.setVersionCode(versionCode);
            this.setSystem(isSystem);
        }

        @Override
        public String toString() {
            return "pkg name: " + getPackageName() +
                    "\napp name: " + getName() +
                    "\napp path: " + getPackagePath() +
                    "\napp v name: " + getVersionName() +
                    "\napp v code: " + getVersionCode() +
                    "\nis system: " + isSystem();
        }
    }

    public static AppInfo getAppInfo(Context context) {
        return getAppInfo(context.getPackageManager(), context.getPackageName());
    }

    public static AppInfo getAppInfo(PackageManager pm, String packageName) {
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return getBean(pm, pi);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            SentryUtils.captureException(e);
            return null;
        }
    }

    private static AppInfo getBean(PackageManager pm, PackageInfo pi) {
        if (pm == null || pi == null) return null;
        ApplicationInfo ai = pi.applicationInfo;
        String packageName = pi.packageName;
        String name = ai.loadLabel(pm).toString();
        Drawable icon = ai.loadIcon(pm);
        String packagePath = ai.sourceDir;
        String versionName = pi.versionName;
        int versionCode = pi.versionCode;
        boolean isSystem = (ApplicationInfo.FLAG_SYSTEM & ai.flags) != 0;
        return new AppInfo(packageName, name, icon, packagePath, versionName, versionCode, isSystem);
    }

    public static List<AppInfo> getAppsInfo(PackageManager pm) {
        List<AppInfo> list = new ArrayList<>();
        List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
        for (PackageInfo pi : installedPackages) {
            AppInfo ai = getBean(pm, pi);
            if (ai == null) continue;
            list.add(ai);
        }
        return list;
    }

    public static boolean isSpace(String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }


}