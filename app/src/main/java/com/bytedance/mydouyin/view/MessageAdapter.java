package com.bytedance.mydouyin.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.mydouyin.R;
import com.bytedance.mydouyin.databinding.ItemMessageBinding;
import com.bytedance.mydouyin.model.Message;

import java.util.ArrayList;
import java.util.List;

// 支持两种 Holder
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messageList = new ArrayList<>();
    private OnItemClickListener listener;
    private String searchKeyword = ""; // 搜索关键词

    // 定义类型常量
    private static final int TYPE_ITEM = 0;   // 普通消息
    private static final int TYPE_FOOTER = 1; // 底部状态栏

    // 脚部状态常量
    public static final int FOOTER_STATE_LOADING = 1;
    public static final int FOOTER_STATE_NO_MORE = 2;
    private int currentFooterState = FOOTER_STATE_NO_MORE;

    // 设置数据
    public void setDate(List<Message> list) {
        this.messageList = list;
        notifyDataSetChanged();
    }

    // 设置关键词 (用于高亮)
    public void setSearchKeyword(String keyword) {
        this.searchKeyword = keyword;
    }

    // 设置脚部状态 (加载中 / 没有更多)
    public void setFooterState(int state) {
        this.currentFooterState = state;
        // 刷新最后一行 (脚部)
        if (getItemCount() > 0) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Message message, View view);
        void onAvatarClick(Message message);
        void onItemLongClick(Message message, View view);
    }

    // 列表长度 = 消息数量 + 1个脚部
    @Override
    public int getItemCount() {
        return messageList == null ? 0 : messageList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        // 如果是最后一行，就是脚部
        if (position == messageList.size()) {
            return TYPE_FOOTER;
        }
        // 否则是普通消息
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            // 加载 item_footer.xml
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer, parent, false);
            return new FooterViewHolder(view);
        } else {
            // 加载 item_message.xml
            ItemMessageBinding binding = ItemMessageBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new MessageViewHolder(binding);
        }
    }

    // 绑定数据
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // 普通消息
        if (holder instanceof MessageViewHolder) {
            // 防止下标越界
            if (position < messageList.size()) {
                Message message = messageList.get(position);
                MessageViewHolder msgHolder = (MessageViewHolder) holder;

                // 调用 bind 显示数据
                msgHolder.bind(message, searchKeyword);

                // 设置整行点击
                msgHolder.itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(message, v);
                });

                // 设置头像点击
                msgHolder.ivAvatar.setOnClickListener(v -> {
                    if (listener != null) listener.onAvatarClick(message);
                });
                // 设置长按点击
                msgHolder.itemView.setOnLongClickListener(v -> {
                    if (listener != null) {
                        listener.onItemLongClick(message, v);
                    }
                    return true;
                });
            }
        }
        // 底部脚部
        else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footer = (FooterViewHolder) holder;
            // 根据状态控制显示
            if (currentFooterState == FOOTER_STATE_LOADING) {
                footer.pbLoading.setVisibility(View.VISIBLE);
                footer.tvText.setVisibility(View.GONE);
            } else if (currentFooterState == FOOTER_STATE_NO_MORE) {
                footer.pbLoading.setVisibility(View.GONE);
                footer.tvText.setVisibility(View.VISIBLE);
                footer.tvText.setText("暂时没有更多了");
            } else {
                footer.pbLoading.setVisibility(View.GONE);
                footer.tvText.setVisibility(View.GONE);
            }
        }
    }

    // 普通消息 ViewHolder
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageBinding binding;
        public android.widget.ImageView ivAvatar;

        public MessageViewHolder(@NonNull ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.ivAvatar = binding.ivAvatar;
        }

        public void bind(Message message, String keyword) {
            // 1. 设置昵称
            String showName = (message.getLocalRemark() != null && !message.getLocalRemark().isEmpty())
                    ? message.getLocalRemark() : message.getNickname();
            binding.tvNickname.setText(highlight(showName, keyword));

            // 2. 设置内容
            binding.tvContent.setText(highlight(message.getContent(), keyword));

            // 3. 设置时间
            binding.tvTime.setText(message.getTime());

            // 4. 设置头像
            if (message.getAvatarUrl() != null && !message.getAvatarUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(itemView.getContext())
                        .load(message.getAvatarUrl())
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(message.getAvatarResId());
            }

            // 5. 未读红点
            if (message.getUnreadCount() > 0) {
                binding.tvBadge.setVisibility(View.VISIBLE);
                binding.tvBadge.setText(String.valueOf(message.getUnreadCount()));
            } else {
                binding.tvBadge.setVisibility(View.GONE);
            }
            // 根据置顶状态改变背景颜色
            if (message.isPinned()) {
                // 置顶显示稍微深一点的灰色
                itemView.setBackgroundColor(android.graphics.Color.parseColor("#252735"));
            } else {
                // 普通状态 (恢复透明或默认背景)
                itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }

        }

        // 高亮工具方法
        private CharSequence highlight(String text, String keyword) {
            if (text == null) return "";
            if (keyword == null || keyword.isEmpty()) return text;
            android.text.SpannableString s = new android.text.SpannableString(text);
            String lText = text.toLowerCase();
            String lKey = keyword.toLowerCase();
            int index = lText.indexOf(lKey);
            while (index >= 0) {
                s.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#FE2C55")),
                        index, index + keyword.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = lText.indexOf(lKey, index + keyword.length());
            }
            return s;
        }
    }

    // 底部脚部 ViewHolder
    static class FooterViewHolder extends RecyclerView.ViewHolder {
        ProgressBar pbLoading;
        TextView tvText;

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            pbLoading = itemView.findViewById(R.id.pb_footer_loading);
            tvText = itemView.findViewById(R.id.tv_footer_text);
        }
    }
}