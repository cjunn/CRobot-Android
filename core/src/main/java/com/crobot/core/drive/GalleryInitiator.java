package com.crobot.core.drive;

import com.crobot.core.infra.tool.Gallery;
import com.crobot.core.util.FileUtil;
import com.crobot.runtime.engine.Context;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

public class GalleryInitiator implements Initiator {
    private Gallery gallery;

    public GalleryInitiator(Gallery gallery) {
        this.gallery = gallery;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Gallery", new ObjApt() {
            @Caller("showByPath")
            public void showByPath(String filepath) {
                FileUtil.checkFile(filepath, "gallery");
                gallery.show(filepath);
            }

            @Caller("close")
            public void close() {
                gallery.close();
            }

            @Override
            public int onGc(Context context) {
                this.close();
                return super.onGc(context);
            }
        });
    }
}
