package com.example.client.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.client.MainActivity;
import com.example.client.R;
import com.example.client.manager.RequestManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import static com.example.client.App.CH_ID;

public class ForegroundService extends Service {
    Timer timer = new Timer();
    private RequestManager requestManager;
    @Override
    public void onCreate() {
        Log.d("foreground","oncreate fore");
        super.onCreate();
        requestManager = new RequestManager(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("foreground","onstartcommand");
        Intent noti_intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,noti_intent,0);
        Notification notification = new NotificationCompat.Builder(this,CH_ID)
                .setContentTitle("example service")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1,notification);

//       timer.scheduleAtFixedRate(new TimerTask() {
//           @Override
//           public void run() {
//               connect();
//           }
//       },0,60000);

        /*  if we want to use okhttp, we must create a new thread  */

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void CreateWebSocket(String ws_url){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(ws_url)
                .build();
        Log.d("foreground","create web socket server");
        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
                try {

                    Log.d("foreground",response.body().string()+"");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("foreground","web socket error");
                }
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @org.jetbrains.annotations.Nullable Response response) {
                super.onFailure(webSocket, t, response);
                Log.d("foreground",""+t);
                Log.d("foreground","web sock fail "+response);
            }
        });
        client.dispatcher().executorService().shutdown();
    }
}
