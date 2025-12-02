package com.bytedance.mydouyin.model;
import java.io.Serializable;
public class Message implements Serializable{
    // 定义消息类型
    public static final int TYPE_TEXT = 0;   // 纯文本
    public static final int TYPE_IMAGE = 1;  // 图片
    public static final int TYPE_CARD = 2;   // 运营卡片 (如商品、地点)

    // 基础字段
    private int type = TYPE_TEXT;
    private String nickname;
    private String content; // 对于文本，它是内容；对于图片/卡片，它是列表上显示的“摘要”
    private String time;
    private int avatarResId;
    private int unreadCount;
    private String localRemark;

    // 聊天/卡片专用字段
    private int msgImageResId; // 图片消息的大图 / 卡片的封面图
    private String cardTitle;  // 卡片标题
    private String cardSubtitle; // 卡片副标题

    // 区分消息发送者 (true=我发的, false=对方发的)
    private boolean isSelf = false;

    // 构造函数
    public Message(int avatarResId, String nickname, String content, String time, int unreadCount, boolean isSystem) {
        this.avatarResId = avatarResId;
        this.nickname = nickname;
        this.content = content;
        this.time = time;
        this.unreadCount = unreadCount;
    }
    public Message() {
    }

    public int getAvatarResId() { return avatarResId; }
    public String getNickname() { return nickname; }
    public String getContent() { return content; }
    public String getTime() { return time; }
    public int getUnreadCount() { return unreadCount; }
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

    public void setLocalRemark(String localRemark) {
        this.localRemark = localRemark;
    }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public int getMsgImageResId() { return msgImageResId; }
    public void setMsgImageResId(int msgImageResId) { this.msgImageResId = msgImageResId; }

    public String getCardTitle() { return cardTitle; }
    public void setCardTitle(String cardTitle) { this.cardTitle = cardTitle; }

    public String getCardSubtitle() { return cardSubtitle; }
    public void setCardSubtitle(String cardSubtitle) { this.cardSubtitle = cardSubtitle; }

    public boolean isSelf() { return isSelf; }
    public void setSelf(boolean self) { isSelf = self; }
}
