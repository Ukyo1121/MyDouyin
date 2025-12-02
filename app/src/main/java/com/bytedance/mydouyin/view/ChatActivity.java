package com.bytedance.mydouyin.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bytedance.mydouyin.databinding.ActivityChatBinding;
import  com.bytedance.mydouyin.model.Message;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatAdapter chatAdapter;
    private List<Message> chatHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 接收传过来的消息对象
        Message targetMsg = (Message) getIntent().getSerializableExtra("message_data");
        if (targetMsg == null) return;

        // 标题栏昵称
        binding.tvTitle.setText(targetMsg.getLocalRemark() != null ? targetMsg.getLocalRemark() : targetMsg.getNickname());

        binding.btnBack.setOnClickListener(v -> finish());

        chatAdapter = new ChatAdapter(chatHistory);
        binding.rvChatList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChatList.setAdapter(chatAdapter);

        // 生成一些历史记录 + 把刚才点的由于最新消息放进去
        loadFakeHistory(targetMsg);
    }

    private void loadFakeHistory(Message currentMsg) {
        // 造几条文本历史
        Message h1 = new Message();
        h1.setType(Message.TYPE_TEXT);
        h1.setContent("你好呀！");
        h1.setAvatarResId(currentMsg.getAvatarResId());
        chatHistory.add(h1);

        // 把列表页显示的那条最新消息加进去
        chatHistory.add(currentMsg);

        chatAdapter.notifyDataSetChanged();
    }
}
