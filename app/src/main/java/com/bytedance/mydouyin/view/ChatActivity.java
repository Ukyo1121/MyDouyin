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

        Message targetMsg = (Message) getIntent().getSerializableExtra("message_data");
        if (targetMsg == null) return;

        String nickname = targetMsg.getLocalRemark() != null ? targetMsg.getLocalRemark() : targetMsg.getNickname();
        binding.tvTitle.setText(nickname);
        binding.btnBack.setOnClickListener(v -> finish());

        // 获取历史记录
        boolean isSystem = targetMsg.isSystem();
        chatHistory = com.bytedance.mydouyin.model.ChatDataHelper.getChatHistory(
                targetMsg.getNickname(),
                isSystem,
                targetMsg
        );

        // 初始化 Adapter
        chatAdapter = new ChatAdapter(chatHistory);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(false);
        binding.rvChatList.setLayoutManager(layoutManager);
        binding.rvChatList.setAdapter(chatAdapter);

        // 手动滚动到消息底部
        if (chatAdapter.getItemCount() > 0) {
            binding.rvChatList.scrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }
}
