package org.uturano.api.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Relations {
    public static Map<String,String> MAP = new ConcurrentHashMap<>();
    static {
        // student1111 ok
        MAP.put("36c3125911d528d937cfad378ef33203","org.uturano.api.utils.v2.tests.student111Maintest");
        // add-sub is ok
        MAP.put("36ee74ab983dc9a02267474e9c841ed0","org.uturano.tests.JUnitFile");
    }

}
