package com.eastrobot.kbs.media.util;

import org.apache.commons.lang3.StringUtils;

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
    /**
     * use UnicodeBlock check the character is chinese
     *
     * @param c characters for ready check
     *
     * @return true if this character is chinese, false otherwise
     */
    private static boolean isChinese(char c) {
        Character.UnicodeScript sc = Character.UnicodeScript.of(c);
        return Character.UnicodeScript.HAN == sc;
    }

    /**
     * use UnicodeBlock check the character is punctuation
     *
     * @param c characters for ready check
     *
     * @return true if this character is punctuation, false otherwise
     */
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

    private static final Pattern CHINESE_UNICODE_PATTERN = Pattern.compile("[\\u4E00-\\u9FCC]+");

    /**
     * use unicode range check the string is chinese<br/>
     * note: This method is not accurate, because there are still many chinese characters are not in this range.
     *
     * @param str characters for ready check
     *
     * @return true if this String range is chinese, false otherwise
     */
    public static boolean isChineseByRange(String str) {
        return !StringUtils.isBlank(str) && CHINESE_UNICODE_PATTERN.matcher(str).find();
    }

    private static Boolean isMessy(char c) {
        return !isPunctuation(c) && !isUserDefined(c) && !isChinese(c);
    }

    /**
     * remove messy characters
     *
     * @author Yogurt_lei
     * @date 2018-04-24 12:02
     */
    public static String removeMessy(String str) {
        StringBuilder sb = new StringBuilder();
        str = str.replaceAll("\\\\r\\\\n|\\\\r|\\\\n|\\\\t|\\s*", "");
        //去除标点符号
        str = str.replaceAll("[\\pP\\p{Punct}]", "");

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
