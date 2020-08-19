package com.example.client.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.Timeout;

public class RequestManager {
    private OkHttpClient client;
    private Request request;
    private Response response;
    private Context mycontext;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public RequestManager(Context context){
        mycontext = context;

    }

    public void get(final String url,final String endpoint){
        Thread t = new Thread(){
            @Override
            public void run() {
                super.run();
                client = new OkHttpClient();
                String sessionid = GetSessionid();
                Log.d("foreground","(get) session id is "+sessionid);
                request = new Request.Builder()
                        .url(url+endpoint)
                        .addHeader("cookie",sessionid)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    Log.d("foreground","get response "+response.body().string()+"");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("foreground","the last");
            }

        };
        t.start();

    }
    public void post(final String url,final String endpoint,final String json){
        Thread t = new Thread(){
            @Override
            public void run() {
                super.run();
                client = new OkHttpClient();
                RequestBody body = RequestBody.create(json,JSON);
                request = new Request.Builder()
                        .url(url+endpoint)
                        .post(body)
                        .build();
                try {
                    /* .newCall.execute is a synchronize request */
                    Response response = client.newCall(request).execute();
                    StoreSession(response);
                    Log.d("foreground","post response "+response.body().string()+"");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("foreground","the last");
            }
        };
        t.start();


    }
    public void createwebsocket(String ws_url){
        OkHttpClient client2 = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .pingInterval(8,TimeUnit.SECONDS)//websocket heartbeat
                .build();
        //client = new OkHttpClient();
        String sessionid = GetSessionid();
        request = new Request.Builder()
                .url(ws_url)
                .addHeader("cookie",sessionid)
                .build();
        client2.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
                Log.d("foreground","websocket on open(connection sucess )");
            }
            @Override
            public void onMessage(@NotNull final WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
                Log.d("foreground","websocket on message: "+text);

            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
                Log.d("foreground","websocket on close");
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                Log.d("foreground","websocket on failure "+t);
            }

        });
    }
    /* store session id in local storage */
    private void StoreSession(Response response){

        /* get session info (in response header) */
        Headers headers = response.headers();
        List cookies = headers.values("Set-Cookie");
        if(cookies.size()==0)
            return;

        /* split session id */
        String session = (String) cookies.get(0);
        String sessionid = session.substring(0,session.indexOf(";"));

        /* store into local storage*/
        SharedPreferences share = mycontext.getSharedPreferences("Session",Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = share.edit();
        edit.putString("sessionid",sessionid);
        edit.commit();
        Log.d("foreground","session id is "+sessionid);

    }

    /* get session from the local storage */
    private String GetSessionid(){
        SharedPreferences share = mycontext.getSharedPreferences("Session",Context.MODE_PRIVATE);
        String sessionid= share.getString("sessionid","null");


        return sessionid;
    }

}

