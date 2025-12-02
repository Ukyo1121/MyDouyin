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
    // 声明接口变量
    private OnItemClickListener listener;

    // 更新数据的方法：当 ViewModel 有新数据时，调用这个方法刷新列表
    public void setDate(List<Message> list) {
        this.messageList = list;
        notifyDataSetChanged();
    }
    // 定义一个接口用于把点击事件传递出去
    public interface OnItemClickListener {
        // 点击整行（跳转聊天页面）
        void onItemClick(Message message);

        // 点击头像（跳转备注页面）
        void onAvatarClick(Message message);
    }

    // 暴露一个方法让外面设置监听器
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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
        Message message = messageList.get(position);

        holder.bind(message);

        // 设置整行点击事件 (跳转聊天页面)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(message);
                }
            }
        });

        // 设置头像点击事件 (跳转备注页面)
        holder.ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAvatarClick(message);
                }
            }
        });
    }
    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getType();
    }
    // 告诉列表一共有多少条数据
    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    // Inner Class: ViewHolder
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageBinding binding;
        public android.widget.ImageView ivAvatar;

        public MessageViewHolder(@NonNull ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.ivAvatar = binding.ivAvatar;
        }

        public void bind(Message message) {
            // 设置昵称
            // 如果有本地备注就显示备注，否则显示原昵称
            if (message.getLocalRemark() != null && !message.getLocalRemark().isEmpty()) {
                binding.tvNickname.setText(message.getLocalRemark());
            } else {
                binding.tvNickname.setText(message.getNickname());
            }
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
