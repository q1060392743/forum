package com.xzp.forum.filter;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 敏感词过滤
 *
 * @author dxm
 */
@SuppressWarnings("rawtypes")
@Service
public class SensitiveWordFilter {

    // 单例
    private static SensitiveWordFilter inst = null;
    private Map sensitiveWordMap;

    /**
     * 构造函数，初始化敏感词库
     */
    private SensitiveWordFilter() {
        sensitiveWordMap = new SensitiveWordInit().initKeyWord();
    }

    /**
     * 获取单例
     */
    public static SensitiveWordFilter getInstance() {
        if (null == inst) {
            inst = new SensitiveWordFilter();
        }
        return inst;
    }

    /**
     * 检查文字中是否包含敏感字符，检查规则如下：
     * 如果存在，则返回敏感词字符的长度，不存在返回0
     */
    private int checkSensitiveWord(String txt, int beginIndex) {
        boolean flag = false;

        // 匹配标识数默认为0
        int matchFlag = 0;
        Map nowMap = sensitiveWordMap;
        for (int i = beginIndex; i < txt.length(); i++) {
            char word = txt.charAt(i);

            // 获取指定key
            nowMap = (Map) nowMap.get(word);

            // 存在，则判断是否为最后一个，不存在，直接返回
            if (nowMap != null) {
                // 找到相应key，匹配标识+1
                matchFlag++;

                // 如果为最后一个匹配规则,结束循环，返回匹配标识数
                if ("1".equals(nowMap.get("isEnd"))) {
                    // 结束标志位为true
                    flag = true;
                }
            } else {
                break;
            }
        }

        if (!flag) {
            matchFlag = 0;
        }
        return matchFlag;
    }

    /**
     * 获取文字中的敏感词
     */
    private Set<String> getSensitiveWord(String txt) {
        Set<String> sensitiveWordList = new HashSet<>();
        for (int i = 0; i < txt.length(); i++) {
            // 判断是否包含敏感字符
            int length = checkSensitiveWord(txt, i);

            // 存在,加入list中
            if (length > 0) {
                sensitiveWordList.add(txt.substring(i, i + length));

                // 减1的原因，是因为for会自增
                i = i + length - 1;
            }
        }
        return sensitiveWordList;
    }

    /**
     * 获取替换字符串
     */
    private String getReplaceChars(String replaceChar, int length) {
        StringBuilder resultReplace = new StringBuilder(replaceChar);
        for (int i = 1; i < length; i++) {
            resultReplace.append(replaceChar);
        }
        return resultReplace.toString();
    }

    /**
     * 替换敏感字字符
     */
    public String replaceSensitiveWord(String txt, String replaceChar) {
        String resultTxt = txt;

        // 获取所有的敏感词
        Set<String> set = getSensitiveWord(txt);
        Iterator<String> iterator = set.iterator();
        String word;
        String replaceString;
        while (iterator.hasNext()) {
            word = iterator.next();
            replaceString = getReplaceChars(replaceChar, word.length());
            resultTxt = resultTxt.replaceAll(word, replaceString);
        }
        return resultTxt;
    }

    /**
     * 判断文字是否包含敏感字符
     */
    public boolean isContaintSensitiveWord(String txt) {
        boolean flag = false;
        for (int i = 0; i < txt.length(); i++) {

            // 判断是否包含敏感字符c
            int matchFlag = this.checkSensitiveWord(txt, i);

            // 大于0存在，返回true
            if (matchFlag > 0) {
                flag = true;
            }
        }
        return flag;
    }
}