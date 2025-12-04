package com.bytedance.mydouyin.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChatDataHelper {

    // 静态 Map：Key是昵称，Value是这个人的消息列表
    private static final Map<String, List<Message>> chatStore = new HashMap<>();
    // 全局头像缓存：Key=昵称, Value=Message(只存头像相关字段)
    private static final Map<String, Message> avatarCache = new HashMap<>();

    // 随机文案库，用于生成不一样的历史消息
    private static final String[] HISTORY_SAMPLES = {
            "在吗？", "吃了吗？", "最近怎么样？", "哈哈哈哈", "OK",
            "收到", "好的，没问题", "下周见", "晚安", "早安"
    };

    //保存头像信息到缓存
    public static void saveAvatarInfo(Message msg) {
        if (msg == null || msg.getNickname() == null) return;

        // 如果缓存里没有，或者缓存里的没图但新来的有图，就更新
        if (!avatarCache.containsKey(msg.getNickname()) ||
                (msg.getAvatarUrl() != null || msg.getAvatarName() != null)) {

            Message avatarInfo = new Message();
            avatarInfo.setAvatarUrl(msg.getAvatarUrl());
            avatarInfo.setAvatarName(msg.getAvatarName());
            avatarInfo.setAvatarResId(msg.getAvatarResId());

            avatarCache.put(msg.getNickname(), avatarInfo);
        }
    }

    // 给消息补全头像 (从缓存里取)
    private static void fillAvatar(Message targetMsg) {
        if (targetMsg.isSelf()) return;

        Message cached = avatarCache.get(targetMsg.getNickname());
        if (cached != null) {
            targetMsg.setAvatarUrl(cached.getAvatarUrl());
            targetMsg.setAvatarName(cached.getAvatarName());
            targetMsg.setAvatarResId(cached.getAvatarResId());
        }
    }
    private static void generateBaseHistory(String nickname) {
        List<Message> history = new ArrayList<>();
        int count = new Random().nextInt(6) + 5; // 生成 5-10 条

        for (int i = 0; i < count; i++) {
            Message msg = new Message();
            msg.setNickname(nickname);
            msg.setContent(HISTORY_SAMPLES[new Random().nextInt(HISTORY_SAMPLES.length)]);

            // 时间逻辑 (0-3天前)
            long now = System.currentTimeMillis();
            long randomPast = i * 30 * 60 * 1000L + new Random().nextInt(60 * 60 * 1000);
            msg.setTime(com.bytedance.mydouyin.utils.TimeUtils.getFriendlyTimeSpanByNow(now - randomPast));

            msg.setType(Message.TYPE_TEXT);
            msg.setSelf(new Random().nextBoolean());

            // 补全头像
            if (msg.isSelf()) {
                msg.setAvatarName("my_avatar");
            } else {
                fillAvatar(msg); // 从缓存借头像
            }
            history.add(msg);
        }

        // 存入 Map
        chatStore.put(nickname, history);
    }
   // 获取指定联系人的聊天记录
   public static List<Message> getChatHistory(String nickname, boolean isSystem, Message initMsg) {
       if (initMsg != null) saveAvatarInfo(initMsg);

       // 缓存不存在，说明这是第一次接触这个联系人
       if (!chatStore.containsKey(nickname)) {
           if (!isSystem) {
               // 生成随机历史
               generateBaseHistory(nickname);
           } else {
               chatStore.put(nickname, new ArrayList<>());
           }

           if (initMsg != null) {
               initMsg.setSelf(false);
               chatStore.get(nickname).add(initMsg);
           }
       }

       return chatStore.get(nickname);
   }

    // 添加新消息
    public static void addMessage(String nickname, Message message) {
        fillAvatar(message);

        // 如果内存里还没有这个人的记录，且不是系统消息，先生成历史记录
        if (!chatStore.containsKey(nickname)) {
            boolean isSystem = message.isSystem() || message.getType() == Message.TYPE_CARD;

            if (!isSystem) {
                generateBaseHistory(nickname);
            } else {
                chatStore.put(nickname, new ArrayList<>());
            }
        }

        List<Message> list = chatStore.get(nickname);
        if (list != null) {
            list.add(message);
        }
    }
    // 搜索指定人的历史记录中是否包含关键词
    public static boolean hasHistoryMatch(Message currentMsg, String keyword) {
        if (currentMsg == null || keyword == null) return false;

        // 获取历史记录
        // 传入 currentMsg 作为 initMsg
        boolean isSystem = currentMsg.isSystem() || currentMsg.getType() == Message.TYPE_CARD;

        List<Message> history = getChatHistory(currentMsg.getNickname(), isSystem, currentMsg);

        // 遍历检查
        if (history != null) {
            String lowerKey = keyword.toLowerCase();
            for (Message msg : history) {
                if (msg.getContent() != null && msg.getContent().toLowerCase().contains(lowerKey)) {
                    return true; // 只要有一条命中了，就返回 true
                }
            }
        }
        return false;
    }
    // 获取历史记录中匹配关键词的第一条完整消息对象
    public static Message getMatchedMessage(Message currentMsg, String keyword) {
        if (currentMsg == null || keyword == null) return null;

        boolean isSystem = currentMsg.isSystem() || currentMsg.getType() == Message.TYPE_CARD;
        List<Message> history = getChatHistory(currentMsg.getNickname(), isSystem, currentMsg);

        if (history != null) {
            String lowerKey = keyword.toLowerCase();
            // 倒序遍历，找最近的一条
            for (int i = history.size() - 1; i >= 0; i--) {
                Message hMsg = history.get(i);
                if (hMsg.getContent() != null && hMsg.getContent().toLowerCase().contains(lowerKey)) {
                    return hMsg; // 【关键】直接返回这个历史消息对象
                }
            }
        }
        return null;
    }
}
