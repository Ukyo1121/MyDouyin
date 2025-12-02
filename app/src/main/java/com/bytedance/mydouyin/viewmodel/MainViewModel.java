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
    // 随机数工具
    private java.util.Random random = new java.util.Random();

    // 模拟的好友名单
    private String[] mockSenders = {
            "张三", "李四", "王五", "妈妈", "产品经理-强哥", "房东阿姨", "前任", "外卖小哥"
    };

    // 模拟的聊天内容库
    private String[] mockContents = {
            "在吗？借我点钱急用",
            "哈哈哈哈哈哈笑死我了",
            "[动画表情]",
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
                }
            }
            return list != null ? list : new ArrayList<>();

        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    // 把数据库里的备注同步到内存列表里
    private void fillRemarks(List<Message> list) {
        if (list == null) return;
        for (Message msg : list) {
            // 用昵称去数据库查备注
            String remark = dbHelper.getRemark(msg.getNickname());
            // 填入对象
            msg.setLocalRemark(remark);
        }
    }
    // 加载数据的方法
    public void loadData() {
        // 拉取最新全量数据
        allMessages = readJsonFromAssets();
        fillRemarks(allMessages);
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
        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}

            // 重新读取数据
            allMessages = readJsonFromAssets();
            fillRemarks(allMessages);
            List<Message> firstPage = new ArrayList<>();
            int count = Math.min(20, allMessages.size());
            for (int i = 0; i < count; i++) {
                firstPage.add(allMessages.get(i));
            }

            messageList.postValue(firstPage);
        }).start();
    }
    // 模拟上滑加载更多数据
    public void loadMoreData() {
        if (isLoading) return;

        // 如果当前显示的条数已经等于总条数，说明没数据了，直接返回
        List<Message> currentDisplayList = messageList.getValue();
        if (currentDisplayList == null || currentDisplayList.size() >= allMessages.size()) {
            return;
        }

        isLoading = true;
        isLoadingMoreState.setValue(true);

        new Thread(() -> {
            try {
                Thread.sleep(1000); // 模拟网络延迟
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int currentCount = currentDisplayList.size();

            int nextCount = Math.min(currentCount + 10, allMessages.size());

            // 准备一个新的列表，先装入旧数据
            List<Message> newList = new ArrayList<>(currentDisplayList);

            // 从仓库里追加新数据
            List<Message> nextChunk = allMessages.subList(currentCount, nextCount);
            newList.addAll(nextChunk);

            // 更新界面
            messageList.postValue(newList);

            isLoading = false;
            isLoadingMoreState.postValue(false);

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
    // 启动更逼真的消息模拟中心
    public void startMessageSimulation() {
        new Thread(() -> {
            while (isSimulating) {
                try {
                    // 为了演示效果，我们设快一点，3秒来一条
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 随机选人
                String senderName = mockSenders[random.nextInt(mockSenders.length)];
                // 构建信息
                Message newMsg = new Message();
                newMsg.setNickname(senderName);
                newMsg.setTime("刚刚");
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
                    // 实际图片资源 (暂时用系统图标代替)
                    newMsg.setMsgImageResId(android.R.drawable.ic_menu_gallery);
                }
                else {
                    // Type 3: 运营卡片消息
                    newMsg.setType(Message.TYPE_CARD);

                    // 随机模拟两个卡片例子
                    if (random.nextBoolean()) {
                        newMsg.setContent("分享地点：瑞幸咖啡（武汉大学信息学部店）"); // 列表摘要
                        newMsg.setCardTitle("瑞幸咖啡（武汉大学信息学部店）");
                        newMsg.setCardSubtitle("咖啡厅 · 附近");
                        newMsg.setMsgImageResId(android.R.drawable.star_big_on); // 卡片封面
                    } else {
                        // 例子B: 豆乳黑麒麟
                        newMsg.setContent("分享商品：豆乳黑麒麟（中杯）"); // 列表摘要
                        newMsg.setCardTitle("豆乳黑麒麟 (中杯)");
                        newMsg.setCardSubtitle("¥10.5");
                        newMsg.setMsgImageResId(android.R.drawable.star_big_off);// 卡片封面
                    }
                }

                // 消息置顶
                // 操作消息总仓库
                if (allMessages != null) {
                    int targetIndex = -1;
                    for (int i = 0; i < allMessages.size(); i++) {
                        if (allMessages.get(i).getNickname().equals(senderName)) {
                            targetIndex = i;
                            break;
                        }
                    }
                    // 如果有历史对话框，先删掉旧的
                    if (targetIndex != -1) {
                        String savedRemark = allMessages.get(targetIndex).getLocalRemark();
                        newMsg.setLocalRemark(savedRemark);

                        allMessages.remove(targetIndex);
                    } else {
                        if (dbHelper != null) {
                            newMsg.setLocalRemark(dbHelper.getRemark(senderName));
                        }
                    }
                    // 把新的消息插到第一条
                    allMessages.add(0, newMsg);
                }

                // 操作显示列表
                List<Message> currentList = messageList.getValue();
                if (currentList == null) currentList = new ArrayList<>();
                List<Message> newList = new ArrayList<>(currentList);

                int uiTargetIndex = -1;
                for (int i = 0; i < newList.size(); i++) {
                    if (newList.get(i).getNickname().equals(senderName)) {
                        uiTargetIndex = i;
                        break;
                    }
                }
                if (uiTargetIndex != -1) {
                    // 旧消息在屏幕可见范围内需要删除
                    newList.remove(uiTargetIndex);
                }
                // 新消息置顶
                newList.add(0, newMsg);

                messageList.postValue(newList);
                scrollToTopSignal.postValue(true);
            }
        }).start();
    }
}
