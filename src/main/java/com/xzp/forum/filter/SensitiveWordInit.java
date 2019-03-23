package com.xzp.forum.filter;

import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/* *
 * 初始化敏感词库<br>
 * 将敏感词加入到HashMap中<br>
 * 构建DFA算法模型
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class SensitiveWordInit {
    /**
     * 初始化敏感字库
     */
    Map initKeyWord() {

        // 读取敏感词库
        Set<String> wordSet = readSensitiveWordFile();

        // 将敏感词库加入到HashMap中
        return addSensitiveWordToHashMap(wordSet);
    }

    /**
     * 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型：
     * 中 = { isEnd = 0 国 = {
     * isEnd = 1 人 = {isEnd = 0 民 = {isEnd = 1} } 男 = { isEnd = 0 人 = { isEnd = 1 } } } }
     * 五 = { isEnd = 0 星 = { isEnd = 0 红 = { isEnd = 0 旗 = { isEnd = 1} } } }
     */
    private Map addSensitiveWordToHashMap(Set<String> wordSet) {
        // 初始化敏感词容器，减少扩容操作
        Map wordMap = new HashMap(wordSet.size());

        for (String word : wordSet) {
            Map nowMap = wordMap;
            for (int i = 0; i < word.length(); i++) {
                // 转换成char型
                char keyChar = word.charAt(i);

                // 获取
                Object tempMap = nowMap.get(keyChar);

                // 如果存在该key，直接赋值，不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                if (tempMap != null) {
                    nowMap = (Map) tempMap;
                } else {
                    // 设置标志位
                    Map<String, String> newMap = new HashMap<>();
                    newMap.put("isEnd", "0");

                    // 添加到集合
                    nowMap.put(keyChar, newMap);
                    nowMap = newMap;
                }

                // 最后一个
                if (i == word.length() - 1) {
                    nowMap.put("isEnd", "1");
                }
            }
        }
        return wordMap;
    }

    /**
     * 读取敏感词库中的内容，将内容添加到set集合中
     */
    private Set<String> readSensitiveWordFile() {
        Set<String> wordSet = null;

        // 读取文件
        //File file = new File("E:\\sc\\forum\\src\\main\\resources\\static\\text\\wordFilter.txt");
        try {
            File file = ResourceUtils.getFile("classpath:static/text/wordFilter.txt");
            InputStreamReader read = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);

            // 文件流是否存在
            if (file.isFile() && file.exists()) {
                wordSet = new HashSet<>();
                StringBuilder sb = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(read);
                String txt;
                // 读取文件，将文件内容放入到set中
                while ((txt = bufferedReader.readLine()) != null) {
                    sb.append(txt);
                }
                bufferedReader.close();

                String str = sb.toString();
                String[] ss = str.split("，");
                Collections.addAll(wordSet, ss);
            }

            // 关闭文件流
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wordSet;
    }
}