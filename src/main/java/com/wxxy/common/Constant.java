package com.wxxy.common;

import java.util.Arrays;
import java.util.List;

public class Constant {

    public static String[] annos = {
            "/login",
            "/register",
            "/logout"
    };

    public static List<String> getAnnosList() {
        return Arrays.asList(annos);
    }
}
