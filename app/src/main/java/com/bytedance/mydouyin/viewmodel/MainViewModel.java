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
    // 用于通知界面显示/隐藏底部加载条
    public MutableLiveData<Boolean> isLoadingMoreState = new MutableLiveData<>();
    // 存放从 JSON 读出来的所有数据（模拟服务器数据库）
    private List<Message> allMessages = new ArrayList<>();
    private RemarkDatabaseHelper dbHelper;
    public MainViewModel(@NonNull Application application) {
        super(application);
        dbHelper = new RemarkDatabaseHelper(application);
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
}
