package com.bytedance.mydouyin.model;

public class Message {
    // 1. 头像
    private int avatarResId;

    // 2. 昵称
    private String nickname;

    // 3. 消息摘要
    private String content;

    // 4. 时间文案
    private String time;

    // 5. 未读消息数 (用于显示角标)
    private int unreadCount;

    // 6. 是否是系统消息
    private boolean isSystem;
    // 7.本地备注
    private String localRemark;

    // 构造函数
    public Message(int avatarResId, String nickname, String content, String time, int unreadCount, boolean isSystem) {
        this.avatarResId = avatarResId;
        this.nickname = nickname;
        this.content = content;
        this.time = time;
        this.unreadCount = unreadCount;
        this.isSystem = isSystem;
    }
    public Message() {
    }

    // Getter
    public int getAvatarResId() { return avatarResId; }
    public String getNickname() { return nickname; }
    public String getContent() { return content; }
    public String getTime() { return time; }
    public int getUnreadCount() { return unreadCount; }
    public boolean isSystem() { return isSystem; }
    public String getLocalRemark() {
        return localRemark;
    }

    public void setAvatarResId(int avatarResId) {
        this.avatarResId = avatarResId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public void setLocalRemark(String localRemark) {
        this.localRemark = localRemark;
    }
}
