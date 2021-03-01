package com.qw.datacenter.ad.consume.utils;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;

/**
 * <p>
 * 验证时间
 * </p>
 *
 * @author Sliven
 * @since 2019-10-31
 */
public class DateUtil {

    /**
     * 验证时间格式，是否 YYYY-MM-DD HH-mm-ss 固定格式
     *
     * @param date
     * @return
     */
    public static boolean verifyDateIsDateTime(String date) {
        if (StrUtil.isBlank(date)) {
            return false;
        }

        //验证长度
        if (19 != date.length()) {
            return false;
        }

        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);
        String hour = date.substring(11, 13);
        String min = date.substring(14, 16);
        String second = date.substring(17, 19);

        //验证年
        if (!NumberUtil.isNumber(year)) {
            return false;
        }

        //验证月
        if (!NumberUtil.isNumber(month)) {
            return false;
        }

        if (Integer.parseInt(month) > 12 || Integer.parseInt(month) < 1) {
            return false;
        }

        //验证日
        if (!NumberUtil.isNumber(day)) {
            return false;
        }

        if (Integer.parseInt(day) > 31 || Integer.parseInt(day) < 1) {
            return false;
        }

        //验证时
        if (!NumberUtil.isNumber(hour)) {
            return false;
        }

        if (Integer.parseInt(hour) > 23) {
            return false;
        }

        //验证分
        if (!NumberUtil.isNumber(min)) {
            return false;
        }

        if (Integer.parseInt(min) > 59) {
            return false;
        }

        //验证秒
        if (!NumberUtil.isNumber(second)) {
            return false;
        }

        if (Integer.parseInt(second) > 59) {
            return false;
        }

        return true;
    }
}
