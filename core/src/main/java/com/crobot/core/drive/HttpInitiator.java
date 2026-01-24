package com.crobot.core.drive;

import com.crobot.core.util.FileUtil;
import com.crobot.runtime.engine.CallBack;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.Varargs;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpInitiator implements Initiator {
    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Http", new HttpApt(context));
    }

    public static class HttpApt extends ObjApt {
        private ClientApt defaultClient;
        private List<OkHttpClient> clientList = new ArrayList<>();

        private HttpApt(ContextProxy context) {
            context.addClosingEvent(() -> {
                clientList.forEach(client -> client.dispatcher().cancelAll());
                clientList.clear();
            });
        }

        public OkHttpClient addOkHttpClient(long connectTimeout, long writeTimeout, long readTimeout) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                    .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                    .build();
            clientList.add(client);
            return client;
        }


        @Caller("open")
        public ClientApt open(Number connectTimeout, Number writeTimeout, Number readTimeout) {
            return new ClientApt(addOkHttpClient(connectTimeout.longValue(),
                    writeTimeout.longValue(),
                    readTimeout.longValue())
            );
        }

        @Caller("default")
        public synchronized ClientApt _default() {
            if (defaultClient != null) {
                return defaultClient;
            }
            defaultClient = new ClientApt(addOkHttpClient(6000, 6000, 6000));
            return defaultClient;
        }
    }

    public static class ClientApt extends ObjApt {
        private OkHttpClient client;

        public ClientApt(OkHttpClient client) {
            this.client = client;
        }

        private Request.Builder createRequestBuilder(String url, Map<String, String> header) {
            Request.Builder builder = new Request.Builder();
            builder.url(url);
            builder.removeHeader("User-Agent");
            builder.addHeader("User-Agent", System.getProperty("http.agent"));
            header.forEach((key, val) -> builder.addHeader(key, val));
            return builder;
        }

        @Caller("get")
        private CallApt get(String url, Map<String, String> header) {
            Request request = createRequestBuilder(url, header).get().build();
            return new CallApt(client.newCall(request));
        }

        @Caller("formPost")
        public CallApt formPost(String url, Map<String, String> header, Map<String, String> form) {
            FormBody.Builder builder = new FormBody.Builder();
            form.forEach((key, val) -> builder.add(key, val));
            Request request = createRequestBuilder(url, header).post(builder.build()).build();
            return new CallApt(client.newCall(request));
        }

        @Caller("jsonPost")
        public CallApt jsonPost(String url, Map<String, String> header, String json) {
            RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
            Request request = createRequestBuilder(url, header).post(requestBody).build();
            return new CallApt(client.newCall(request));
        }

    }


    public static class CallApt extends ObjApt {
        private Call call;
        private Response response;
        private ResponseBody responseBody;

        public CallApt(Call call) {
            this.call = call;
        }

        private Response execute() throws IOException {
            if (response != null) {
                return response;
            }
            response = call.execute();
            return response;
        }

        private ResponseBody body() throws IOException {
            if (responseBody != null) {
                return responseBody;
            }
            responseBody = execute().body();
            return responseBody;
        }

        @Caller("code")
        public Number code() throws IOException {
            return execute().code();
        }

        @Caller("bytes")
        public byte[] bytes() throws IOException {
            return body().bytes();
        }

        @Caller("text")
        public String text() throws IOException {
            return body().string();
        }

        @Caller("contentLength")
        public Number contentLength() throws IOException {
            return body().contentLength();
        }

        @Caller("contentType")
        public String contentType() throws IOException {
            return body().contentType().type();
        }

        @Caller("saveAsFile")
        public boolean saveAsFile(String filePath, Varargs varargs) throws IOException {
            ResponseBody body = body();
            AtomicReference<Integer> lstProgress = new AtomicReference<>(0);
            CallBack callback = varargs.getValue(0, CallBack.class);
            FileUtil.ProgressCallback progressCallback = (callback != null ? (currentBytes, totalBytes, progress) -> {
                if (lstProgress.get() == progress) {
                    return;
                }
                lstProgress.set(progress);
                callback.apply(Varargs.create(currentBytes, totalBytes, progress));
            } : null);
            return FileUtil.writeFileStream(new File(filePath), body.contentLength(), body.byteStream(), progressCallback);
        }

    }

}
