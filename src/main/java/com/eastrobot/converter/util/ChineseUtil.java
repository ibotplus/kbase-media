package com.eastrobot.converter.util;

import java.util.regex.Pattern;

/**
 * ChineseUtil 移除文本中的乱码
 * 1.去掉各种标点字符、控制字符
 * 2.非标点 非中文 非用户定义 为乱码
 * see http://www.cnblogs.com/zztt/p/3427452.html
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-24 11:57
 */
public class ChineseUtil {
    //使用UnicodeBlock方法判断中文
    private static boolean isChinese(char c) {
        Character.UnicodeScript sc = Character.UnicodeScript.of(c);
        return Character.UnicodeScript.HAN == sc;
    }

    // 根据UnicodeBlock方法判断中文标点符号
    private static boolean isPunctuation(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                // symbols and punctuation in the unified Chinese, Japanese and Korean script
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                // fullwidth character or a halfwidth character
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                // vertical glyph variants for east Asian compatibility
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS
                // vertical punctuation for compatibility characters with the Chinese Standard GB 18030
                || ub == Character.UnicodeBlock.VERTICAL_FORMS
                // ascii
                || ub == Character.UnicodeBlock.BASIC_LATIN;
    }

    private static Boolean isUserDefined(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.NUMBER_FORMS
                || ub == Character.UnicodeBlock.ENCLOSED_ALPHANUMERICS
                || ub == Character.UnicodeBlock.LETTERLIKE_SYMBOLS
                || c == '\ufeff'
                || c == '\u00a0';
    }

    // 使用Unicode编码范围来判断汉字；这个方法不准确,因为还有很多汉字不在这个范围之内
    public boolean isChineseByRange(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("[\\u4E00-\\u9FCC]+");

        return pattern.matcher(str.trim()).find();
    }

    private static Boolean isMessy(char c) {
        return !isPunctuation(c) && !isUserDefined(c) && !isChinese(c);
    }

    /**
     *
     * 移除乱码
     *
     * @author Yogurt_lei
     * @date 2018-04-24 12:02
     */
    public static String removeMessy(String str) {
        StringBuilder sb = new StringBuilder();
        str = str.replaceAll("\\\\r\\\\n|\\\\r|\\\\n|\\\\t|\\s*","");

        //去掉中文之间的空格，并保留英文之间的空格 https://www.zhihu.com/question/39636240
        str = str.replaceAll("(\\w) +(\\w)", "$1@$2");
        str = str.replaceAll(" ", "").replaceAll("@", " ");

        for (char c : str.toCharArray()) {
            if (!isMessy(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
