package com.example.client.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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

public class RequestManager {
    private OkHttpClient client;
    private Request request;
    private Response response;
    private Context mycontext;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public RequestManager(Context context){
        mycontext = context;

    }

    public void get(String url){

        client = new OkHttpClient();
        String sessionid = GetSessionid();
        Log.d("foreground","(get) session id is "+sessionid);
        request = new Request.Builder()
                .url(url)
                .addHeader("cookie",sessionid)
                .build();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("foreground","get error: "+e);
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d("foreground","get ok: "+response.body().string());
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("foreground","asasfasfa");
    }
    public void post(String url, String json){
        Log.d("foreground","this is post");
        client = new OkHttpClient();
        RequestBody body = RequestBody.create(json,JSON);
        request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            StoreSession(response);
            Log.d("foreground",response.body().string()+"");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("foreground","post error "+e);
        }
    }
    public void createwebsocket(String ws_url){
        client = new OkHttpClient();
        request = new Request.Builder()
                .url(ws_url)
                .build();
        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
                Log.d("foreground","websocket on open");
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
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

        /* store */
        SharedPreferences share = mycontext.getSharedPreferences("Session",Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = share.edit();
        edit.putString("sessionid",sessionid);
        edit.commit();

    }

    /* get session from the local storage */
    private String GetSessionid(){
        SharedPreferences share = mycontext.getSharedPreferences("Session",Context.MODE_PRIVATE);
        String sessionid= share.getString("sessionid","null");


        return sessionid;
    }

}

