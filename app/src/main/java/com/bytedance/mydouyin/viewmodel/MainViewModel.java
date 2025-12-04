package com.bytedance.mydouyin.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;
import com.bytedance.mydouyin.model.Message;
import com.bytedance.mydouyin.R;
import com.bytedance.mydouyin.model.RemarkDatabaseHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

public class MainViewModel extends AndroidViewModel {

    // LiveData是一个可以被观察的数据容器，当这里面的 List 变了，Activity 会自动收到通知
    public MutableLiveData<List<Message>> messageList = new MutableLiveData<>();
    // 增加一个标记，防止短时间内触发多次加载
    private boolean isLoading = false;
    // 用来控制模拟线程是否继续运行
    private boolean isSimulating = true;
    // 用于通知界面显示/隐藏底部加载条
    public MutableLiveData<Boolean> isLoadingMoreState = new MutableLiveData<>();
    // 新通知到来滚动到顶部信号
    public MutableLiveData<Boolean> scrollToTopSignal = new MutableLiveData<>();
    // 存放从 JSON 读出来的所有数据（模拟服务器数据库）
    private List<Message> allMessages = new ArrayList<>();
    private RemarkDatabaseHelper dbHelper;
    // 记录当前的搜索关键词，默认为空
    private String currentKeyword = "";
    // 信息列表是否到底
    public MutableLiveData<Boolean> isNoMoreData = new MutableLiveData<>(false);
    // 用于弹出顶部通知
    public MutableLiveData<Message> newNotification = new MutableLiveData<>();
    // 随机数工具
    private java.util.Random random = new java.util.Random();

    // 模拟的好友名单
    private String[] mockSenders = {
            "张三", "李四", "王五", "妈妈", "产品经理-强哥", "房东阿姨", "姐姐", "外卖小哥"
    };

    // 模拟的聊天内容库
    private String[] mockContents = {
            "在吗？借我点钱急用",
            "哈哈哈哈哈哈笑死我了",
            "今晚出来喝酒吗？",
            "文件发你了，记得收一下",
            "恭喜恭喜！",
            "周末有空吗？一起去看电影吧",
            "帮我点一下第一条朋友圈，谢谢",
            "睡了吗？",
            "收到请回复"
    };
    public MainViewModel(@NonNull Application application) {
        super(application);
        dbHelper = new RemarkDatabaseHelper(application);
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        isSimulating = false; // 停止模拟线程
    }
    // 读取本地 JSON 文件的方法
    private List<Message> readJsonFromAssets() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = getApplication().getAssets().open("data.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();

            Gson gson = new Gson();
            Type listType = new TypeToken<List<Message>>(){}.getType();
            List<Message> list = gson.fromJson(stringBuilder.toString(), listType); // 数据解析结果
            if (list != null) {
                for (Message msg : list) {
                    // 手动给每条消息设置一个默认头像
                    msg.setAvatarResId(android.R.drawable.sym_def_app_icon);
                    if (msg.isSystem()) {
                        msg.setType(Message.TYPE_SYSTEM_TEXT_CARD);
                    }
                }
            }
            return list != null ? list : new ArrayList<>();

        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    // 删除信息
    public void deleteMessage(Message message) {
        if (allMessages != null) {
            allMessages.remove(message);

            List<Message> result = filterList(allMessages, currentKeyword);
            messageList.setValue(result);
        }
    }
    // 对列表进行排序：置顶的在前，其他按时间排序
    private void sortMessages(List<Message> list) {
        if (list == null) return;
        java.util.Collections.sort(list, new java.util.Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                if (o1.isPinned() && !o2.isPinned()) return -1;
                if (!o1.isPinned() && o2.isPinned()) return 1;
                return 0;
            }
        });
    }
    // 置顶功能
    public void toggleMessagePin(Message message) {
        boolean newStatus = !message.isPinned();
        message.setPinned(newStatus);

        // 写入数据库
        if (dbHelper != null) {
            dbHelper.updatePinStatus(message.getNickname(), newStatus);
        }

        // 重新排序并刷新
        if (allMessages != null) {
            sortMessages(allMessages);

            // 刷新 UI
            List<Message> result = filterList(allMessages, currentKeyword);
            messageList.setValue(result);
        }
    }
    // 把数据库里的备注和置顶状态同步到内存列表里
    private void fillRemarks(List<Message> list) {
        if (list == null) return;

        try {
            for (Message msg : list) {
                if (dbHelper != null) {
                    // 读取备注
                    String remark = dbHelper.getRemark(msg.getNickname());
                    msg.setLocalRemark(remark);
                    // 读取置顶状态
                    boolean pinned = dbHelper.isPinned(msg.getNickname());
                    msg.setPinned(pinned);
                }
            }
            sortMessages(list);
        } catch (Exception e) {
            // 如果发生了并发修改异常，说明刚才正好有新消息进来。
            // 直接忽略这次错误，界面马上会收到新消息的通知再次刷新。
            e.printStackTrace();
        }
    }
    // 加载数据的方法
    public void loadData() {
        // 拉取最新全量数据
        allMessages = readJsonFromAssets();
        fillRemarks(allMessages);
        // 把 JSON 里读到的所有头像，预存到 ChatDataHelper 的缓存里
        if (allMessages != null) {
            for (Message msg : allMessages) {
                com.bytedance.mydouyin.model.ChatDataHelper.saveAvatarInfo(msg);
            }
        }
        // 截取前 20 条展示
        List<Message> firstPage = new ArrayList<>();
        int count = Math.min(20, allMessages.size());

        for (int i = 0; i < count; i++) {
            firstPage.add(allMessages.get(i));
        }

        messageList.setValue(firstPage);
        // 开始模拟消息
        startMessageSimulation();
    }

    // 模拟下拉更新数据
    public void refreshData() {
        isNoMoreData.postValue(false);
        new Thread(() -> {
            try {
                Thread.sleep(500); // 模拟网络延迟
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 遍历所有消息，将未读数清零
            if (allMessages != null) {
                for (Message msg : allMessages) {
                    msg.setUnreadCount(0); // 强制设为 0
                }
            }

            // 截取第一页数据 (前20条)
            List<Message> firstPage = new ArrayList<>();
            int count = Math.min(20, allMessages != null ? allMessages.size() : 0);

            if (allMessages != null) {
                for (int i = 0; i < count; i++) {
                    firstPage.add(allMessages.get(i));
                }
            }

            // 更新 UI
            messageList.postValue(firstPage);

        }).start();
    }
    // 模拟上滑加载更多数据
    public void loadMoreData() {
        if (isLoading) return;

        List<Message> currentDisplayList = messageList.getValue();

        // 如果当前已经显示了所有数据，直接显示“没有更多”，不再请求
        if (currentDisplayList != null && allMessages != null && currentDisplayList.size() >= allMessages.size()) {
            isNoMoreData.setValue(true);
            return;
        }

        isLoading = true;
        isLoadingMoreState.setValue(true); // 显示加载圈
        isNoMoreData.setValue(false);

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int currentCount = currentDisplayList != null ? currentDisplayList.size() : 0;
            // 判空保护
            if (allMessages == null) allMessages = new ArrayList<>();

            int nextCount = Math.min(currentCount + 10, allMessages.size());

            List<Message> newList = new ArrayList<>();
            if (currentDisplayList != null) newList.addAll(currentDisplayList);

            if (currentCount < allMessages.size()) {
                List<Message> nextChunk = allMessages.subList(currentCount, nextCount);
                newList.addAll(nextChunk);
            }

            // 更新列表
            messageList.postValue(newList);

            isLoading = false;
            isLoadingMoreState.postValue(false);

            if (newList.size() >= allMessages.size()) {
                isNoMoreData.postValue(true);
            }

        }).start();
    }
    // 列表加载新备注
    public void reloadRemarks() {
        List<Message> currentList = messageList.getValue();
        if (currentList == null) return;

        // 重新查库填数据
        fillRemarks(currentList);

        messageList.setValue(currentList);

        // 更新总仓库
        fillRemarks(allMessages);
    }
    // 启动消息模拟中心
    public void startMessageSimulation() {
        new Thread(() -> {
            while (isSimulating) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 随机选人
                String senderName = mockSenders[random.nextInt(mockSenders.length)];
                // 构建信息
                Message newMsg = new Message();
                newMsg.setNickname(senderName);
                newMsg.setTime(com.bytedance.mydouyin.utils.TimeUtils.getFriendlyTimeSpanByNow(System.currentTimeMillis()));
                newMsg.setUnreadCount(1);
                newMsg.setAvatarResId(android.R.drawable.sym_def_app_icon);
                newMsg.setSelf(false); // 模拟的是别人发给我的信息

                int randomCase = random.nextInt(10);

                if (randomCase < 5) {
                    // Type 1: 文本消息
                    newMsg.setType(Message.TYPE_TEXT);

                    // 从十条消息中随机选择一条
                    String[] textMessages = {
                            "今晚去看电影吗",
                            "好的，没问题！",
                            "我刚完成那个项目报告，花了整整三天时间，现在终于可以放松一下了。",
                            "👋",
                            "明天会议改到下午3点了，记得提前准备一下演示材料，我们需要向客户展示最新的产品进展。",
                            "嗯",
                            "你看到昨晚的比赛了吗？真是太精彩了！最后时刻的那个绝杀球简直让人难以置信，全场观众都沸腾了。",
                            "OK",
                            "这家新开的餐厅评价很好，我们要不要周末去试试？听说他们的招牌菜特别好吃。",
                            "刚刚收到通知，下周出差计划有变，需要提前两天出发，你能帮忙调整一下酒店预订吗？"
                    };

                    int messageIndex = random.nextInt(textMessages.length);
                    newMsg.setContent(textMessages[messageIndex]);
                }
                else if (randomCase < 8) {
                    // Type 2: 图片消息
                    newMsg.setType(Message.TYPE_IMAGE);
                    newMsg.setContent("[图片]");

                    int[] localImages = {
                            com.bytedance.mydouyin.R.drawable.pic_1,
                            com.bytedance.mydouyin.R.drawable.pic_2,
                            com.bytedance.mydouyin.R.drawable.pic_3,
                            com.bytedance.mydouyin.R.drawable.pic_4,
                            com.bytedance.mydouyin.R.drawable.pic_5,
                            com.bytedance.mydouyin.R.drawable.pic_6,
                            com.bytedance.mydouyin.R.drawable.pic_7
                    };

                    // 随机抽取一个下标
                    int index = random.nextInt(localImages.length);

                    // 设置给消息对象
                    newMsg.setMsgImageResId(localImages[index]);
                }
                else {
                    // Type 3: 运营卡片消息
                    newMsg.setType(Message.TYPE_CARD);

                    // 随机模拟两个卡片例子
                    if (random.nextBoolean()) {
                        newMsg.setContent("分享地点：瑞幸咖啡（武汉大学信息学部店）"); // 列表摘要
                        newMsg.setCardTitle("瑞幸咖啡（武汉大学信息学部店）");
                        newMsg.setCardSubtitle("咖啡厅 · 附近");
                        newMsg.setMsgImageResId(R.drawable.coffee); // 卡片封面
                    } else {
                        newMsg.setContent("分享商品：伯牙绝弦"); // 列表摘要
                        newMsg.setCardTitle("伯牙绝弦 (大杯)");
                        newMsg.setCardSubtitle("¥18");
                        newMsg.setMsgImageResId(R.drawable.chaji);// 卡片封面
                    }
                }

                // 更新总仓库
                int targetIndex = -1;
                int oldUnreadCount = 0;

                if (allMessages != null) {
                    // 查找旧消息
                    for (int i = 0; i < allMessages.size(); i++) {
                        if (allMessages.get(i).getNickname().equals(senderName)) {
                            targetIndex = i;
                            oldUnreadCount = allMessages.get(i).getUnreadCount();
                            break;
                        }
                    }

                    if (targetIndex != -1) {
                        // 找到旧消息：继承 备注 和 置顶状态
                        Message oldMsg = allMessages.get(targetIndex);
                        newMsg.setLocalRemark(oldMsg.getLocalRemark());

                        newMsg.setPinned(oldMsg.isPinned());

                        allMessages.remove(targetIndex);
                    } else {
                        if (dbHelper != null) {
                            newMsg.setLocalRemark(dbHelper.getRemark(senderName));
                            newMsg.setPinned(dbHelper.isPinned(senderName));
                        }
                    }

                    // 累加未读数
                    newMsg.setUnreadCount(oldUnreadCount + 1);
                    com.bytedance.mydouyin.model.ChatDataHelper.addMessage(senderName, newMsg);

                    // 插入到第一位
                    allMessages.add(0, newMsg);

                    // 插入后立即排序
                    sortMessages(allMessages);
                }

                // 使用 filterList 方法，根据当前是否在搜索 (currentKeyword) 自动返回正确的数据
                // 如果 currentKeyword 是空，它会返回全部；如果有值，它会按规则过滤
                List<Message> resultList = filterList(allMessages, currentKeyword);

                // 更新界面
                messageList.postValue(resultList);

                // 触发顶部弹窗通知
                // 只有当不在搜索状态时才弹，避免打扰
                if (currentKeyword == null || currentKeyword.isEmpty()) {
                    newNotification.postValue(newMsg);
                }
            }
        }).start();
    }
    public void clearUnread(String nickname) {
        // 修改总仓库
        if (allMessages != null) {
            for (Message msg : allMessages) {
                if (msg.getNickname().equals(nickname)) {
                    msg.setUnreadCount(0); // 清零
                    break;
                }
            }
        }

        // 修改当前显示的列表并刷新 UI
        List<Message> currentList = messageList.getValue();
        if (currentList != null) {
            for (Message msg : currentList) {
                if (msg.getNickname().equals(nickname)) {
                    msg.setUnreadCount(0); // 清零
                    break;
                }
            }
            messageList.setValue(currentList);
        }
    }
    // 搜索消息
    public void searchMessages(String keyword) {
        this.currentKeyword = keyword;

        List<Message> snapshot = new ArrayList<>();
        if (allMessages != null) {
            snapshot.addAll(allMessages);
        }

        // 调用通用过滤方法
        List<Message> result = filterList(snapshot, keyword);

        // 更新界面
        messageList.setValue(result);
    }
    // 通用过滤方法
    // 通用的过滤方法 (支持高亮历史记录)
    private List<Message> filterList(List<Message> sourceList, String keyword) {
        List<Message> result = new ArrayList<>();
        if (sourceList == null) return result;

        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>(sourceList);
        }

        String key = keyword.toLowerCase();

        try {
            for (Message msg : sourceList) {
                // 检查首页显示的字段 (昵称、备注、摘要、卡片标题)
                boolean matchMain = false;

                if (msg.getNickname() != null && msg.getNickname().toLowerCase().contains(key)) matchMain = true;
                else if (msg.getLocalRemark() != null && msg.getLocalRemark().toLowerCase().contains(key)) matchMain = true;
                else if (msg.getContent() != null && msg.getContent().toLowerCase().contains(key)) matchMain = true;
                else if (msg.getCardTitle() != null && msg.getCardTitle().toLowerCase().contains(key)) matchMain = true;

                if (matchMain) {
                    // 如果主要信息匹配，直接显示原对象
                    result.add(msg);
                }
                else {
                    // 2. 如果主要信息不匹配，去查历史记录对象
                    Message historyMsg = com.bytedance.mydouyin.model.ChatDataHelper.getMatchedMessage(msg, key);

                    if (historyMsg != null) {
                        // 找到了历史匹配！创建替身用于展示
                        Message tempMsg = new Message();

                        // A. 身份信息：复制当前联系人的 (保持头像、昵称是这个人的)
                        tempMsg.setNickname(msg.getNickname());
                        tempMsg.setLocalRemark(msg.getLocalRemark());
                        tempMsg.setUnreadCount(msg.getUnreadCount());
                        tempMsg.setSystem(msg.isSystem());

                        // 复制头像
                        tempMsg.setAvatarResId(msg.getAvatarResId());
                        tempMsg.setAvatarUrl(msg.getAvatarUrl());
                        tempMsg.setAvatarName(msg.getAvatarName());

                        // B. 展示内容：复制历史记录的真实数据！
                        tempMsg.setContent(historyMsg.getContent()); // 显示搜到的那句话

                        // 【核心修改】使用历史记录的真实时间！
                        tempMsg.setTime(historyMsg.getTime());

                        tempMsg.setType(Message.TYPE_TEXT);

                        result.add(tempMsg);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
