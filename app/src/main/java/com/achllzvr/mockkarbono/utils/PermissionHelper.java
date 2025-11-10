package com.achllzvr.mockkarbono.utils;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

public class PermissionHelper {

    /**
     * Check if Usage Stats permission is granted
     */
    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    /**
     * Show dialog and open Usage Stats settings
     */
    public static void requestUsageStatsPermission(Activity activity) {
        new AlertDialog.Builder(activity)
            .setTitle("Usage Access Required")
            .setMessage("Karbono needs Usage Access permission to track app usage and calculate your carbon footprint.\n\nThis helps estimate the energy consumption of your phone usage.")
            .setPositiveButton("Grant Permission", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                activity.startActivity(intent);
            })
            .setNegativeButton("Later", null)
            .setCancelable(false)
            .show();
    }

    /**
     * Check if Notification Listener service is enabled
     */
    public static boolean isNotificationServiceEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(
            context.getContentResolver(),
            "enabled_notification_listeners"
        );

        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Show dialog and open Notification Listener settings
     */
    public static void requestNotificationListenerPermission(Activity activity) {
        new AlertDialog.Builder(activity)
            .setTitle("Enable Notification Tracking")
            .setMessage("Karbono needs Notification Access to track notifications and estimate their carbon footprint.\n\nNotifications use data and energy, contributing to your carbon emissions.\n\nPlease enable 'Karbono' in the next screen.")
            .setPositiveButton("Open Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                activity.startActivity(intent);
            })
            .setNegativeButton("Later", null)
            .setCancelable(false)
            .show();
    }

    /**
     * Check all required permissions
     */
    public static boolean hasAllPermissions(Context context) {
        return hasUsageStatsPermission(context) && isNotificationServiceEnabled(context);
    }

    /**
     * Show comprehensive permission request dialog
     */
    public static void requestAllPermissions(Activity activity) {
        StringBuilder message = new StringBuilder();
        message.append("Karbono needs the following permissions to track your carbon footprint:\n\n");

        if (!hasUsageStatsPermission(activity)) {
            message.append("• Usage Access - Track app usage\n");
        }

        if (!isNotificationServiceEnabled(activity)) {
            message.append("• Notification Access - Track notifications\n");
        }

        message.append("\nThese permissions help calculate your digital carbon footprint accurately.");

        new AlertDialog.Builder(activity)
            .setTitle("Permissions Required")
            .setMessage(message.toString())
            .setPositiveButton("Grant Permissions", (dialog, which) -> {
                if (!hasUsageStatsPermission(activity)) {
                    requestUsageStatsPermission(activity);
                } else if (!isNotificationServiceEnabled(activity)) {
                    requestNotificationListenerPermission(activity);
                }
            })
            .setNegativeButton("Later", null)
            .show();
    }
}

