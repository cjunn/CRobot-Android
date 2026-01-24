package android.boostrap;

import androidx.annotation.Keep;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface CBootstrap {
    @Keep
    Map<Integer, Object> LINK = new HashMap<>();
}
