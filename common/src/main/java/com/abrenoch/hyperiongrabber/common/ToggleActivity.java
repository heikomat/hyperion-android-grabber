package com.abrenoch.hyperiongrabber.common;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

public class ToggleActivity extends AppCompatActivity {
    public static final int REQUEST_MEDIA_PROJECTION = 1;

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            boolean running = intent.getBooleanExtra(HyperionScreenService.BROADCAST_TAG, false);
//            if (running){
//                stopService();
//                finish();
//            } else {
//               requestPermission();
//            }
//
//            // we are not interested in updates anymore
//            LocalBroadcastManager.getInstance(ToggleActivity.this).unregisterReceiver(this);
//
//        }
//    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        LocalBroadcastManager.getInstance(this).registerReceiver(
//                mMessageReceiver, new IntentFilter(BROADCAST_FILTER));

        boolean serviceRunning = checkForInstance();

        if (serviceRunning) {
            stopService();
            finish();
        } else {
            requestPermission();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK) {
                startScreenRecorder(this, resultCode, data);
            }

            finish();

        }
    }

    private static void startScreenRecorder(Context context, int resultCode, Intent data) {
        Intent intent = new Intent(context, HyperionScreenService.class);
        intent.setAction(HyperionScreenService.ACTION_START);
        intent.putExtra(HyperionScreenService.EXTRA_RESULT_CODE, resultCode);
        intent.putExtras(data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    /** @return whether the service is running */
    private boolean checkForInstance() {
        if (isServiceRunning()) {
            Intent intent = new Intent(this, HyperionScreenService.class);
            intent.setAction(HyperionScreenService.GET_STATUS);
            startService(intent);
            return true;
        } else {
            return false;
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (HyperionScreenService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void requestPermission(){
        MediaProjectionManager manager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (manager != null) {
            startActivityForResult(manager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        }
    }

    /** stop recording & stop service */

     private void stopService() {
        Intent stopIntent = new Intent(ToggleActivity.this, HyperionScreenService.class);
        stopIntent.setAction(HyperionScreenService.ACTION_EXIT);
        startService(stopIntent);
    }
}
