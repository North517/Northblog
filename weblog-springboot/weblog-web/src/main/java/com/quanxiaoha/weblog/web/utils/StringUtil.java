package com.quanxiaoha.weblog.web.utils;

/**
 * @Author: North001
 * @Date: 2026-04-21
 * @Version: v1.0.0
 * @Description: TODO
 **/
public class StringUtil {

    /**
     * 判断字符串是否是纯数字
     * @param str
     * @return
     */
    public static boolean isPureNumber(String str) {
        return str.matches("\\d+");
    }

}
