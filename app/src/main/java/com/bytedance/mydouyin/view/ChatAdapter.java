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
    @Override
    public int getItemViewType(int position) {
        // 这里简化：假设全是左边 (对方发的)
        // 实际开发要判断 isSelf
        return list.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == Message.TYPE_CARD) {
            return new CardHolder(inflater.inflate(R.layout.item_chat_left_card, parent, false));
        } else if (viewType == Message.TYPE_IMAGE) {
            return new ImageHolder(inflater.inflate(R.layout.item_chat_left_image, parent, false));
        } else {
            return new TextHolder(inflater.inflate(R.layout.item_chat_left_text, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message msg = list.get(position);

        if (holder instanceof CardHolder) {
            ((CardHolder) holder).bind(msg);
        } else if (holder instanceof ImageHolder) {
            ((ImageHolder) holder).bind(msg);
        } else if (holder instanceof TextHolder) {
            ((TextHolder) holder).bind(msg);
        }
    }


    // 卡片 Holder
    class CardHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvSubtitle;
        public CardHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_card_cover);
            tvTitle = itemView.findViewById(R.id.tv_card_title);
            tvSubtitle = itemView.findViewById(R.id.tv_card_subtitle);
        }
        public void bind(Message msg) {
            tvTitle.setText(msg.getCardTitle());
            tvSubtitle.setText(msg.getCardSubtitle());
            ivCover.setImageResource(msg.getMsgImageResId());

            // 卡片点击事件
            itemView.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "点击了卡片：" + msg.getCardTitle(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    // 图片 Holder
    class ImageHolder extends RecyclerView.ViewHolder {
        ImageView ivImg;
        public ImageHolder(View itemView) {
            super(itemView);
            ivImg = itemView.findViewById(R.id.iv_chat_image);
        }
        public void bind(Message msg) {
            // 这里显示真正的大图，而不是 "[图片]" 文字
            ivImg.setImageResource(msg.getMsgImageResId());
        }
    }

    // 文本 Holder
    class TextHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        public TextHolder(View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_chat_text);
        }
        public void bind(Message msg) {
            tvText.setText(msg.getContent());
        }
    }
}
