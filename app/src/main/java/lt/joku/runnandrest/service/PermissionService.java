package lt.joku.runnandrest.service;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionService {

    private int messageId;
    private String[] requestedPermissions;
    private int requestCode;
    @Nullable
    private PermissionsCallback permissionsCallback;

    public PermissionService(int requestCode, int messageId, String... requestedPermissions) {
        this.requestCode = requestCode;
        this.messageId = messageId;
        this.requestedPermissions = requestedPermissions;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (verifyPermissions(requestCode, grantResults)) {
            if (permissionsCallback != null) {
                permissionsCallback.onGrant();
            }
        }
    }

    public void onGrant(Activity activity, @Nullable PermissionsCallback permissionsCallback) {
        if (hasPermissions(activity) && permissionsCallback != null) {
            permissionsCallback.onGrant();
        } else {
            requestPermissions(activity, permissionsCallback);
        }
    }

    private boolean hasPermissions(Activity activity) {
        for (String permission : requestedPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions(Activity activity, @Nullable PermissionsCallback permissionsCallback) {
        this.permissionsCallback = permissionsCallback;
        if (shouldShowExplanation(activity)) {
            Snackbar.make(activity.getWindow().getDecorView().getRootView(), messageId, Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", view -> {
                        ActivityCompat.requestPermissions(activity, requestedPermissions, requestCode);
                    }).show();
            return;
        }
        ActivityCompat.requestPermissions(activity, requestedPermissions, requestCode);
    }

    private boolean shouldShowExplanation(Activity activity) {
        for (String permission : requestedPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    private boolean verifyPermissions(int requestCode, int[] grantResults) {
        if (this.requestCode == requestCode) {
            if (grantResults.length < 1) {
                return false;
            }

            // Verify that each required permission has been granted, otherwise return false.
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
