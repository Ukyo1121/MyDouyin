package com.bytedance.mydouyin.view;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.mydouyin.R;
import com.bytedance.mydouyin.databinding.ItemMessageBinding;
import com.bytedance.mydouyin.model.Message;

import java.util.ArrayList;
import java.util.List;
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> list;

    public ChatAdapter(List<Message> list) { this.list = list; }
    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }
    // 根据 消息类型 + 是否是自己，返回对应的 XML 布局 ID
    @Override
    public int getItemViewType(int position) {
        Message msg = list.get(position);
        boolean isSelf = msg.isSelf();
        if (msg.getType() == Message.TYPE_SYSTEM_TEXT_CARD) {
            return R.layout.item_chat_left_system_card;
        }
        else if (msg.getType() == Message.TYPE_CARD) {
            return isSelf ? R.layout.item_chat_right_card : R.layout.item_chat_left_card;
        } else if (msg.getType() == Message.TYPE_IMAGE) {
            return isSelf ? R.layout.item_chat_right_image : R.layout.item_chat_left_image;
        } else {
            // 默认文本
            return isSelf ? R.layout.item_chat_right_text : R.layout.item_chat_left_text;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

        // 根据布局 ID 判断该用哪个 Holder
        if (viewType == R.layout.item_chat_left_system_card) {
            return new SystemCardHolder(view);
        }
        else if (viewType == R.layout.item_chat_left_card || viewType == R.layout.item_chat_right_card) {
            return new CardHolder(view);
        } else if (viewType == R.layout.item_chat_left_image || viewType == R.layout.item_chat_right_image) {
            return new ImageHolder(view);
        } else {
            return new TextHolder(view);
        }
    }
    private void loadAvatar(android.view.View itemView, android.widget.ImageView ivAvatar, Message msg) {
        if (ivAvatar == null) return;

        // 加载avatarUrl
        if (msg.getAvatarUrl() != null && !msg.getAvatarUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(itemView.getContext())
                    .load(msg.getAvatarUrl())
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .circleCrop() // 切圆角
                    .into(ivAvatar);
        }
        else if (msg.getAvatarName() != null && !msg.getAvatarName().isEmpty()) {
            int resId = itemView.getContext().getResources().getIdentifier(
                    msg.getAvatarName(),
                    "drawable",
                    itemView.getContext().getPackageName()
            );

            if (resId > 0) {
                com.bumptech.glide.Glide.with(itemView.getContext())
                        .load(resId) // 加载本地资源 ID
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
            }
        }
       // 显示默认图
        else {
            ivAvatar.setImageResource(msg.getAvatarResId() != 0 ? msg.getAvatarResId() : android.R.drawable.sym_def_app_icon);
        }
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message msg = list.get(position);

        if (holder instanceof SystemCardHolder) {
            ((SystemCardHolder) holder).bind(msg);
        }
        else if (holder instanceof CardHolder) {
            ((CardHolder) holder).bind(msg);
        } else if (holder instanceof ImageHolder) {
            ((ImageHolder) holder).bind(msg);
        } else if (holder instanceof TextHolder) {
            ((TextHolder) holder).bind(msg);
        }
    }


    // 卡片 Holder
    class CardHolder extends RecyclerView.ViewHolder {
        ImageView ivCover, ivAvatar;
        TextView tvTitle, tvSubtitle;

        public CardHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_card_cover);
            tvTitle = itemView.findViewById(R.id.tv_card_title);
            tvSubtitle = itemView.findViewById(R.id.tv_card_subtitle);
            ivAvatar = itemView.findViewById(R.id.iv_avatar); // 绑定控件
        }

        public void bind(Message msg) {
            tvTitle.setText(msg.getCardTitle());
            tvSubtitle.setText(msg.getCardSubtitle());
            ivCover.setImageResource(msg.getMsgImageResId());

            // 调用辅助方法加载头像
            loadAvatar(itemView, ivAvatar, msg);

            itemView.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "点击了卡片：" + msg.getCardTitle(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    // 图片 Holder
    class ImageHolder extends RecyclerView.ViewHolder {
        ImageView ivImg, ivAvatar; // ivAvatar

        public ImageHolder(View itemView) {
            super(itemView);
            ivImg = itemView.findViewById(R.id.iv_chat_image);
            ivAvatar = itemView.findViewById(R.id.iv_avatar); // 绑定控件
        }

        public void bind(Message msg) {
            ivImg.setImageResource(msg.getMsgImageResId());

            // 调用辅助方法加载头像
            loadAvatar(itemView, ivAvatar, msg);
        }
    }

    // 文本 Holder
    class TextHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        ImageView ivAvatar;

        public TextHolder(View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_chat_text);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }

        public void bind(Message msg) {
            tvText.setText(msg.getContent());

            // 调用辅助方法加载头像
            loadAvatar(itemView, ivAvatar, msg);
        }
    }
    // 系统文本卡片 Holder
    class SystemCardHolder extends RecyclerView.ViewHolder {
        TextView tvContent, btnConfirm, btnDetail;
        ImageView ivAvatar;

        public SystemCardHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_sys_content);
            btnConfirm = itemView.findViewById(R.id.btn_confirm);
            btnDetail = itemView.findViewById(R.id.btn_detail);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }

        public void bind(Message msg) {
            tvContent.setText(msg.getContent());
            loadAvatar(itemView, ivAvatar, msg); // 加载系统头像

            // 确认按钮点击
            btnConfirm.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "已收到通知：" + msg.getNickname(), Toast.LENGTH_SHORT).show();
            });

            // 详情按钮点击
            btnDetail.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "正在跳转详情页...", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
