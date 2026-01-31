package com.crobot.core;

import static com.crobot.core.infra.tool.ScreenCaptureImpl.ORIENTATION_AUTO;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.crobot.core.drive.AppInitiator;
import com.crobot.core.drive.AttachInitiator;
import com.crobot.core.drive.Base64Initiator;
import com.crobot.core.drive.CanvasInitiator;
import com.crobot.core.drive.ConfigInitiator;
import com.crobot.core.drive.CryptoInitiator;
import com.crobot.core.drive.DeviceInitiator;
import com.crobot.core.drive.DialogInitiator;
import com.crobot.core.drive.DisplayInitiator;
import com.crobot.core.drive.FileSystemInitiator;
import com.crobot.core.drive.GalleryInitiator;
import com.crobot.core.drive.HttpInitiator;
import com.crobot.core.drive.LibInstallerInitiator;
import com.crobot.core.drive.OutputInitiator;
import com.crobot.core.drive.ProgressInitiator;
import com.crobot.core.drive.SQLiteInitiator;
import com.crobot.core.drive.SelectorInitiator;
import com.crobot.core.drive.SystemInitiator;
import com.crobot.core.drive.TestInitiator;
import com.crobot.core.drive.TextInitiator;
import com.crobot.core.drive.ThreadInitiator;
import com.crobot.core.drive.ToasterInitiator;
import com.crobot.core.drive.TouchInitiator;
import com.crobot.core.drive.UIInitiator;
import com.crobot.core.drive.lib.OcrLiteInitiator;
import com.crobot.core.drive.lib.YoloV8Initiator;
import com.crobot.core.infra.AccessibilityComponent;
import com.crobot.core.infra.InfraConst;
import com.crobot.core.infra.Logger;
import com.crobot.core.infra.NotificationBuilder;
import com.crobot.core.infra.tool.AppImpl;
import com.crobot.core.infra.tool.ConfigFactory;
import com.crobot.core.infra.tool.ConfigFactoryImpl;
import com.crobot.core.infra.tool.DeviceImpl;
import com.crobot.core.infra.tool.DialogImpl;
import com.crobot.core.infra.tool.DisplayCanvasLazy;
import com.crobot.core.infra.tool.FileOperationFactory;
import com.crobot.core.infra.tool.FileOperationFactoryImpl;
import com.crobot.core.infra.tool.GalleryImpl;
import com.crobot.core.infra.tool.Output;
import com.crobot.core.infra.tool.OutputImpl;
import com.crobot.core.infra.tool.ProgressLazy;
import com.crobot.core.infra.tool.SQLiteFactoryImpl;
import com.crobot.core.infra.tool.ScreenCapture;
import com.crobot.core.infra.tool.ScreenCaptureLazy;
import com.crobot.core.infra.tool.ScreenMetrics;
import com.crobot.core.infra.tool.ScreenMetricsImpl;
import com.crobot.core.infra.tool.ScreenSelectorImpl;
import com.crobot.core.infra.tool.ScreenTouchImpl;
import com.crobot.core.infra.tool.Toaster;
import com.crobot.core.infra.tool.ToasterLazy;
import com.crobot.core.project.Project;
import com.crobot.core.project.ProjectImpl;
import com.crobot.core.side.SideBar;
import com.crobot.core.side.SideBarEvent;
import com.crobot.core.ui.core.UIContext;
import com.crobot.runtime.ScriptRuntime;
import com.crobot.runtime.ScriptRuntimeBuilder;
import com.crobot.runtime.engine.ContextFactory;
import com.crobot.runtime.engine.RuntimeEvent;


public abstract class BackendService extends Service implements RuntimeEvent, SideBarEvent, Logger {
    private static final int NOTIFICATION_ID = 10087;
    public static String CORE_VER = "1.0";
    private final NotificationBuilder notificationBuilder = new NotificationBuilder(this);
    private final IBinder binder = new BackendBinder();
    protected Intent intent;
    protected SideBar sideBar;
    protected Output output;
    protected ConfigFactory configFactory;
    protected FileOperationFactory fileSystemFactory;
    protected ScreenCapture screenCapture;
    protected ScreenMetrics screenMetrics;
    protected Toaster toaster;
    protected ScriptRuntime scriptRuntime;
    protected UIContext uiContext;
    protected Project project;

    public BackendService() {
    }

    protected void openActivity() {
        Intent intent = new Intent(this, openActivityClz());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            this.startActivity(intent);
        }
    }


    protected void updateUI(String xml) {
        this.project.writeUIXml(xml);
        this.uiContext.flushUI(xml);
    }

    protected abstract String appVer();

    protected void install(String xml, byte[] zip, Integer version) {
        this.project.install(xml, zip, version);
        this.uiContext.flushUI(xml);
    }

    protected void install(String xml, byte[] zip, byte[] attach, Integer version) {
        this.project.install(xml, zip, attach, version);
        this.uiContext.flushUI(xml);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        this.screenMetrics = new ScreenMetricsImpl(this);
        this.fileSystemFactory = new FileOperationFactoryImpl(this);
        this.configFactory = new ConfigFactoryImpl(this.fileSystemFactory);
        this.toaster = new ToasterLazy(this);
        this.output = new OutputImpl(this);
        this.project = new ProjectImpl(this.fileSystemFactory, this.configFactory);
        this.screenCapture = new ScreenCaptureLazy(this, () -> this.intent.getParcelableExtra(InfraConst.ScreenCapture), ORIENTATION_AUTO, this.screenMetrics);
        this.startForeground(NOTIFICATION_ID, notificationBuilder.get(this.channelName(), () -> openActivityClz()));
    }

    protected abstract Class<? extends Activity> openActivityClz();

    protected String channelName() {
        return "CRobot";
    }

    public void initServiceEnv(Intent intent, UIContext uiContext, SideBar sideBar) {
        this.intent = intent;
        this.sideBar = sideBar;
        ContextFactory contextFactory = getContextFactory();
        this.scriptRuntime = ScriptRuntimeBuilder.getBuilder()
                .setContextFactory(contextFactory)
                .setRuntimeEvent(this)
                .addInitiator(new AttachInitiator(this.project.getAttachFile()))
                .addInitiator(new ThreadInitiator(contextFactory, this.output))
                .addInitiator(new OutputInitiator(this.output))
                .addInitiator(new ToasterInitiator(this.toaster))
                .addInitiator(new TouchInitiator(new ScreenTouchImpl(AccessibilityComponent.get())))
                .addInitiator(new DisplayInitiator(this.screenCapture))
                .addInitiator(new UIInitiator(uiContext))
                .addInitiator(new ConfigInitiator(this.configFactory))
                .addInitiator(new SelectorInitiator(new ScreenSelectorImpl(AccessibilityComponent.get())))
                .addInitiator(new AppInitiator(new AppImpl(this), appVer(), this.CORE_VER))
                .addInitiator(new Base64Initiator())
                .addInitiator(new HttpInitiator())
                .addInitiator(new CanvasInitiator(new DisplayCanvasLazy(this), this.screenMetrics))
                .addInitiator(new DeviceInitiator(new DeviceImpl(this)))
                .addInitiator(new DialogInitiator(new DialogImpl(this)))
                .addInitiator(new GalleryInitiator(new GalleryImpl(this)))
                .addInitiator(new CryptoInitiator())
                .addInitiator(new TextInitiator())
                .addInitiator(new OcrLiteInitiator())
                .addInitiator(new YoloV8Initiator())
                .addInitiator(new FileSystemInitiator(this.fileSystemFactory))
                .addInitiator(new TestInitiator())
                .addInitiator(new LibInstallerInitiator())
                .addInitiator(new SQLiteInitiator(new SQLiteFactoryImpl(this)))
                .addInitiator(new ProgressInitiator(new ProgressLazy(this)))
                .addInitiator(new SystemInitiator())
                .builder();
        this.uiContext.bindViewGroup(() -> this.sideBar.getSettingBody());
        this.uiContext.bindUIValueListen(vo -> this.project.setUISetting(vo.getId(), vo.getValue().get()));
        this.uiContext.bindUIValueInit(() -> this.project.getAllUISetting());
        this.sideBar.addEvent(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        this.intent = intent;
        return binder;
    }

    public abstract ContextFactory getContextFactory();

    public abstract void output(String tag, String source, int currentLine, String message);

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void startEvent() {
        sideBar.setPauseStatus();
        this.toaster.show("脚本启动!");
    }

    @Override
    public void stopEvent() {
        sideBar.setPlayStatus();
        this.toaster.show("脚本结束!");
    }

    @Override
    public void barPlayEvent() {
        byte[] coreZip = this.project.getCoreZip();
        if (coreZip == null) {
            sideBar.setPlayStatus();
            toaster.show("未找到CoreZip！");
            return;
        }
        scriptRuntime.start(coreZip, Project.MAIN, Project.MAIN);
    }

    @Override
    public void barPauseEvent() {
        scriptRuntime.stop();
    }

    @Override
    public void barUpdateEvent() {

    }

    @Override
    public void errorEvent(Exception exception) {
        this.output.error(exception);
    }

    @Override
    public void successEvent() {
        this.output.print("INFO", "", 0, "The program has been executed completely!");
    }

    @Override
    public void joinEvent() {
        this.output.print("INFO", "", -1, "Join the unfinished tasks!");
    }

    public class BackendBinder extends Binder {
        public void initEnv(UIContext uiContext, SideBar sideBar) {
            BackendService.this.uiContext = uiContext;
            BackendService.this.sideBar = sideBar;
            initServiceEnv(BackendService.this.intent, uiContext, sideBar);
        }
    }
}
