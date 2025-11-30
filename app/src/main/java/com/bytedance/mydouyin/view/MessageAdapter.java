package com.bytedance.mydouyin.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.mydouyin.databinding.ItemMessageBinding;
import com.bytedance.mydouyin.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    // 数据源：要展示的消息列表
    private List<Message> messageList = new ArrayList<>();

    // 更新数据的方法：当 ViewModel 有新数据时，调用这个方法刷新列表
    public void setDate(List<Message> list) {
        this.messageList = list;
        notifyDataSetChanged();
    }

    // 创建 ViewHolder：负责加载 xml 布局文件
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用 ViewBinding 加载 item_message.xml
        ItemMessageBinding binding = ItemMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MessageViewHolder(binding);
    }

    // 绑定 ViewHolder：把数据填入视图
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        // 获取当前这一行的消息数据
        Message message = messageList.get(position);

        // 调用 ViewHolder 里的方法进行显示
        holder.bind(message);
    }

    // 告诉列表一共有多少条数据
    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    // Inner Class: ViewHolder
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageBinding binding;

        public MessageViewHolder(@NonNull ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Message message) {
            // 设置昵称
            binding.tvNickname.setText(message.getNickname());
            // 设置内容
            binding.tvContent.setText(message.getContent());
            // 设置时间
            binding.tvTime.setText(message.getTime());

            // 设置头像
            binding.ivAvatar.setImageResource(message.getAvatarResId());

            // 处理未读角标的显示逻辑
            if (message.getUnreadCount() > 0) {
                binding.tvBadge.setVisibility(View.VISIBLE);
                binding.tvBadge.setText(String.valueOf(message.getUnreadCount()));
            } else {
                binding.tvBadge.setVisibility(View.GONE);
            }
        }
    }
}
