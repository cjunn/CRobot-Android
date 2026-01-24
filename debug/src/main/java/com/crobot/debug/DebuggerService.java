package com.crobot.debug;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.crobot.core.BackendService;
import com.crobot.core.infra.tool.FileOperation;
import com.crobot.core.project.Project;
import com.crobot.core.util.Base64Util;
import com.crobot.debug.file.FileModify;
import com.crobot.debug.file.FileRefresher;
import com.crobot.debug.file.FileService;
import com.crobot.debug.rpc.ClientApi;
import com.crobot.debug.rpc.Mapping;
import com.crobot.debug.rpc.Param;
import com.crobot.debug.rpc.RpcService;
import com.crobot.debug.scan.ScanService;
import com.crobot.engine.cjs.JsContextFactory;
import com.crobot.runtime.engine.ContextFactory;
import com.crobot.utils.BitmapUtil;

import java.util.List;

public class DebuggerService extends BackendService {
    private static final int DEFAULT_SCAN = 15562;
    private static final int DEFAULT_PORT = 15563;
    private static final int ATTACH_PORT = 15564;
    private final RpcService rpcService = new RpcService(DEFAULT_PORT);
    private final FileService fileService = new FileService(ATTACH_PORT);
    private final ScanService scanService = new ScanService(DEFAULT_SCAN);
    private final FileRefresher fileRefresher = new FileRefresher();
    private final DebuggerClientApi clientApi;


    public DebuggerService() {
        this.clientApi = rpcService.getClientApi(DebuggerClientApi.class);
        this.rpcService.regisMapping(new DebuggerServiceMapping());
    }

    @Override
    protected String appVer() {
        return "1.0";
    }

    @Override
    protected Class<? extends Activity> openActivityClz() {
        return DebuggerActivity.class;
    }


    @Override
    public ContextFactory getContextFactory() {
        return new JsContextFactory();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        rpcService.start();
        FileOperation attachFile = this.project.getAttachFile();
        fileService.start(attachFile.pwd());
        scanService.start();
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        scanService.close();
        rpcService.close();
        fileService.close();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void stopEvent() {
        super.stopEvent();
        clientApi.end();
    }

    @Override
    public void output(String tag, String source, int currentLine, String message) {
        clientApi.output(source, currentLine, message, tag);
    }

    public interface DebuggerClientApi extends ClientApi {
        void output(@Param("source") String source, @Param("line") int line, @Param("message") String message, @Param("tag") String tag);

        void end();
    }


    public class DebuggerServiceMapping {

        @Mapping
        public void launch(@Param("zip") String zip, @Param("uiXml") String uiXml) {
            byte[] decodeZip = Base64Util.decode(zip);
            DebuggerService.this.install(uiXml, decodeZip, 0);
            scriptRuntime.start(decodeZip, Project.MAIN, Project.MAIN);
        }

        @Mapping
        public void terminate() {
            scriptRuntime.stop();
        }


        @Mapping
        public String screenShot() {
            Bitmap bitmap = BitmapUtil.convertImageToBitmap(screenCapture.capture());
            return Base64Util.encode(BitmapUtil.bitmap2Byte(bitmap));
        }

        @Mapping
        public String runScript(@Param("script") String script) throws Exception {
            return scriptRuntime.start(script).get().asString();
        }

        @Mapping
        public boolean updateUI(@Param("name") String name, @Param("xml") String xml) {
            DebuggerService.this.updateUI(xml);
            openActivity();
            return true;
        }

        @Mapping
        public List<String> updateFileList(@Param(value = "list", clazz = FileModify.class) List<FileModify> list) {
            return fileRefresher.invoke(DebuggerService.this.project.getAttachFile(), list);
        }


    }

}
