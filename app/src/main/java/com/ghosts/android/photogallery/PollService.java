package com.ghosts.android.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.ghosts.android.photogallery.GalleryItem.PhotosBean.PhotoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Allam on 04/07/2016.
 */
public class PollService extends IntentService {
    private static final String TAG = "PollService";
    private static final int Poll_Interval = 1000 * 2;

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isNetworkConnected()) {
            return;
        }

        String query = QueryPreferences.getStoredQuery(this);
        String lastResult = QueryPreferences.getLastResultId(this);
        List<PhotoBean> mList;
        if(query != null){
            mList = new FlickerFetcher().fetchItems(1 , FlickerFetcher.SEARCH_METHOD , query);
        }else {
            mList = new FlickerFetcher().fetchItems(1 , FlickerFetcher.SEARCH_METHOD , null);

        }

        if(mList.isEmpty()){
            return;
        }
        String resultId = mList.get(0).getId();
        if(resultId.equals(lastResult)){
            Log.i(TAG, "Got an old result: " + resultId );
        } else {
            Log.i(TAG, "Got a new result: " + resultId );
        }

        QueryPreferences.setLastResultId(this, resultId);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isAvalible = cm.getActiveNetworkInfo() != null;
        boolean isConnected = cm.getActiveNetworkInfo().isConnected() && isAvalible;
        return isConnected;
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent
                .getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public static void setServiceAlarm(Context context, boolean isOn ){
        Intent intent = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(isOn){
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),Poll_Interval, pendingIntent);
        }else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

    }
}






















