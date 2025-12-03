package com.bytedance.mydouyin.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChatDataHelper {

    // 静态 Map：Key是昵称，Value是这个人的消息列表
    private static final Map<String, List<Message>> chatStore = new HashMap<>();

    // 随机文案库，用于生成不一样的历史消息
    private static final String[] HISTORY_SAMPLES = {
            "在吗？", "吃了吗？", "最近怎么样？", "哈哈哈哈", "OK",
            "收到", "好的，没问题", "下周见", "晚安", "早安"
    };

   // 获取指定联系人的聊天记录
    public static List<Message> getChatHistory(String nickname, boolean isSystem, Message initMsg) {
        if (!chatStore.containsKey(nickname)) {
            List<Message> history = new ArrayList<>();

            // 生成随机旧历史消息 (仅限非系统消息)
            if (!isSystem) {
                int count = new Random().nextInt(6) + 5;
                for (int i = 0; i < count; i++) {
                    Message msg = new Message();
                    msg.setNickname(nickname);
                    msg.setContent(HISTORY_SAMPLES[new Random().nextInt(HISTORY_SAMPLES.length)]);
                    msg.setTime("10-2" + i);
                    msg.setType(Message.TYPE_TEXT);
                    msg.setSelf(new Random().nextBoolean()); // 随机左右
                    history.add(msg);
                }
            }

            // 把传入的“初始预览消息”强制追加到消息最后
            if (initMsg != null) {
                initMsg.setSelf(false);
                history.add(initMsg);
            }

            chatStore.put(nickname, history);
        }
        return chatStore.get(nickname);
    }

   // 添加新消息
    public static void addMessage(String nickname, Message message) {
        // 在添加新消息前，先初始化历史消息
        getChatHistory(nickname, message.isSystem(), null);

        List<Message> list = chatStore.get(nickname);
        if (list != null) {
            list.add(message);
        }
    }
}