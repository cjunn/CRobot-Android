package com.crobot.runtime.engine.boot;

import static com.crobot.runtime.engine.boot.BootType.CALLBACK_CONS;
import static com.crobot.runtime.engine.boot.BootType.FUNCTION_CONS;
import static com.crobot.runtime.engine.boot.BootType.JNI_BASE_APT_OBJECTS_MET;
import static com.crobot.runtime.engine.boot.BootType.JNI_FUNC_APT_CLZ;
import static com.crobot.runtime.engine.boot.BootType.JNI_JSON_BEAN_CLZ;
import static com.crobot.runtime.engine.boot.BootType.JNI_JSON_BEAN_GET_MET;
import static com.crobot.runtime.engine.boot.BootType.JNI_OBJECT_PTR_CLZ;
import static com.crobot.runtime.engine.boot.BootType.JNI_OBJECT_PTR_CONS;
import static com.crobot.runtime.engine.boot.BootType.JNI_OBJECT_PTR_GET_MET;
import static com.crobot.runtime.engine.boot.BootType.JNI_OBJ_APT_CLZ;
import static com.crobot.runtime.engine.boot.BootType.SCRIPT_EXCEPTION_CONS;
import static com.crobot.runtime.engine.boot.BootType.VARARGS_CONS;

import android.boostrap.CBootGen;
import android.boostrap.CBootLink;

import com.crobot.runtime.engine.CallBack;
import com.crobot.runtime.engine.Function;
import com.crobot.runtime.engine.JsonBean;
import com.crobot.runtime.engine.ObjectPtr;
import com.crobot.runtime.engine.ScriptException;
import com.crobot.runtime.engine.Varargs;
import com.crobot.runtime.engine.apt.JniBaseApt;
import com.crobot.runtime.engine.apt.JniFuncApt;
import com.crobot.runtime.engine.apt.JniObjApt;

import java.lang.reflect.Method;

public class BootInitiator {
    public static void initNative(Class clz) {
        BootInitiator.initException();
        BootInitiator.initFunction();
        BootInitiator.initVarargs();
        BootInitiator.initCallback();
        BootInitiator.initObjectPtr();
        BootInitiator.initJniApt();
        BootInitiator.initJsonBean();
        CBootLink.addNative(clz);
    }

    private static void initException() {
        CBootLink.addBoot(SCRIPT_EXCEPTION_CONS, ScriptException.getJniConstructor());
    }

    private static void initFunction() {
        CBootLink.addBoot(FUNCTION_CONS, Function.getJniConstructor());
    }

    private static void initVarargs() {
        CBootLink.addBoot(VARARGS_CONS, Varargs.getJniConstructor());
    }

    private static void initCallback() {
        CBootLink.addBoot(CALLBACK_CONS, CallBack.getJniConstructor());
    }

    private static void initObjectPtr() {
        Method jniGetPtrMethod = ObjectPtr.getJniGetPtrMethod();
        CBootLink.addBoot(JNI_OBJECT_PTR_CONS, ObjectPtr.getJniConstructor());
        CBootLink.addBoot(JNI_OBJECT_PTR_GET_MET, ObjectPtr.class, jniGetPtrMethod.getName(), CBootGen.getSignature(jniGetPtrMethod));
        CBootLink.addBoot(JNI_OBJECT_PTR_CLZ, ObjectPtr.class);
    }

    private static void initJsonBean() {
        Method jniGetJsonMethod = JsonBean.getJniGetJson();
        CBootLink.addBoot(JNI_JSON_BEAN_CLZ, JsonBean.class);
        CBootLink.addBoot(JNI_JSON_BEAN_GET_MET,JsonBean.class,jniGetJsonMethod.getName(), CBootGen.getSignature(jniGetJsonMethod));
    }


    private static void initJniApt() {
        Method jniReg = JniBaseApt.getJniReg();
        CBootLink.addBoot(JNI_FUNC_APT_CLZ, JniFuncApt.class);
        CBootLink.addBoot(JNI_OBJ_APT_CLZ, JniObjApt.class);
        CBootLink.addBoot(JNI_BASE_APT_OBJECTS_MET, JniBaseApt.class, jniReg.getName(), CBootGen.getSignature(jniReg));
    }

}
