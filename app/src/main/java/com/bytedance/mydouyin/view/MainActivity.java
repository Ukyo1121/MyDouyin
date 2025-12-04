package com.bytedance.mydouyin.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bytedance.mydouyin.databinding.ActivityMainBinding;
import com.bytedance.mydouyin.model.Message;
import com.bytedance.mydouyin.viewmodel.MainViewModel;

import com.bytedance.mydouyin.R;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private void showDeleteConfirmDialog(com.bytedance.mydouyin.model.Message message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定要删除与 " + message.getNickname() + " 的对话吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    // 调用 ViewModel 删除
                    viewModel.deleteMessage(message);
                })
                .setNegativeButton("取消", null)
                .show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化 Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // 设置当前界面内容为 Binding 的根视图
        setContentView(binding.getRoot());

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // 设置 RecyclerView 的布局：线性布局
        binding.rvMessageList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        // 创建并设置 Adapter
        com.bytedance.mydouyin.view.MessageAdapter adapter = new com.bytedance.mydouyin.view.MessageAdapter();
        // 设置点击监听
        adapter.setOnItemClickListener(new com.bytedance.mydouyin.view.MessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.bytedance.mydouyin.model.Message message, android.view.View view) {
                // 清空未读数
                viewModel.clearUnread(message.getNickname());

                // 准备跳转 Intent
                android.content.Intent intent = new android.content.Intent(MainActivity.this, com.bytedance.mydouyin.view.ChatActivity.class);
                intent.putExtra("message_data", message);

                startActivity(intent);
            }
            @Override
            public void onItemLongClick(com.bytedance.mydouyin.model.Message message, android.view.View view) {
                String pinOption = message.isPinned() ? "取消置顶" : "置顶";
                String[] options = {pinOption, "删除"};

                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setItems(options, (dialog, which) -> {
                            if (which == 0) {
                                // 点击了 置顶/取消置顶
                                viewModel.toggleMessagePin(message);
                            } else if (which == 1) {
                                // 点击了 删除
                                showDeleteConfirmDialog(message);
                            }
                        })
                        .show();
            }
            @Override
            public void onAvatarClick(com.bytedance.mydouyin.model.Message message) {
                // 点击头像，跳转备注页面
                android.content.Intent intent = new android.content.Intent(MainActivity.this, com.bytedance.mydouyin.view.RemarkActivity.class);
                intent.putExtra("message_data", message);
                startActivity(intent);
            }
        });

        binding.rvMessageList.setAdapter(adapter);
        // 监听新消息通知，显示顶部 Widget
        viewModel.newNotification.observe(this, new androidx.lifecycle.Observer<com.bytedance.mydouyin.model.Message>() {
            @Override
            public void onChanged(com.bytedance.mydouyin.model.Message message) {
                showNotificationWidget(message);
            }
        });
        // 监听加载状态
        viewModel.isLoadingMoreState.observe(this, isLoading -> {
            if (isLoading) {
                adapter.setFooterState(MessageAdapter.FOOTER_STATE_LOADING);
            }
        });
        // 监听“没有更多数据”的状态
        viewModel.isNoMoreData.observe(this, isNoMore -> {
            if (isNoMore) {
                adapter.setFooterState(MessageAdapter.FOOTER_STATE_NO_MORE);
            }
        });
        // 观察 ViewModel 的数据变化
        viewModel.messageList.observe(this, new androidx.lifecycle.Observer<java.util.List<com.bytedance.mydouyin.model.Message>>() {
            @Override
            public void onChanged(java.util.List<com.bytedance.mydouyin.model.Message> messages) {
                // 一旦数据变了，自动塞给 Adapter
                adapter.setDate(messages);
                // setRefreshing(false) 表示刷新结束，停止刷新动画
                binding.srlRefresh.setRefreshing(false);
            }
        });

        // 监听滚动信号
        viewModel.scrollToTopSignal.observe(this, new androidx.lifecycle.Observer<Boolean>() {
            @Override
            public void onChanged(Boolean shouldScroll) {
                if (shouldScroll) {
                    // 收到信号，列表自动平滑滚动顶部
                    binding.rvMessageList.smoothScrollToPosition(0);
                }
            }
        });

        // 监听下拉动作
        binding.srlRefresh.setOnRefreshListener(new androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 用户下拉，告诉 ViewModel 刷新数据
                viewModel.refreshData();
            }
        });
        // 监听列表滑动状态
        binding.rvMessageList.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // dy > 0 表示正在向下滑动
                if (dy > 0) {
                    // 检查是否滑到底了
                    if (!binding.rvMessageList.canScrollVertically(1)) {
                        // 滑动到底到底触发加载更多
                        viewModel.loadMoreData();
                    }
                }
            }
        });
        // 发起第一次数据加载
        viewModel.loadData();
        // 监听搜索框输入
        android.widget.EditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString();

                // 告诉 Adapter 关键词变了，高亮显示张
                adapter.setSearchKeyword(keyword);

                // 告诉 ViewModel 筛选数据
                viewModel.searchMessages(keyword);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        // 每次回到页面，都让 ViewModel 重新检查一下备注
        if (viewModel != null) {
            viewModel.reloadRemarks();
        }
    }
    // 显示顶部通知 Widget
    private void showNotificationWidget(com.bytedance.mydouyin.model.Message message) {
        // 绑定视图
        android.widget.ImageView ivAvatar = findViewById(R.id.iv_notify_avatar);
        android.widget.TextView tvTitle = findViewById(R.id.tv_notify_title);
        android.widget.TextView tvContent = findViewById(R.id.tv_notify_content);
        androidx.cardview.widget.CardView card = findViewById(R.id.card_notification);

        // 设置数据
        tvTitle.setText("新消息: " + message.getNickname());
        tvContent.setText(message.getContent());

        // 加载头像
        if (message.getAvatarUrl() != null && !message.getAvatarUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this).load(message.getAvatarUrl()).circleCrop().into(ivAvatar);
        } else if (message.getAvatarName() != null) {
            int resId = getResources().getIdentifier(message.getAvatarName(), "drawable", getPackageName());
            if (resId > 0) com.bumptech.glide.Glide.with(this).load(resId).circleCrop().into(ivAvatar);
        } else {
            ivAvatar.setImageResource(message.getAvatarResId());
        }

        // 设置点击跳转 (点击 Widget 进入聊天)
        card.setOnClickListener(v -> {
            viewModel.clearUnread(message.getNickname());
            android.content.Intent intent = new android.content.Intent(MainActivity.this, com.bytedance.mydouyin.view.ChatActivity.class);
            intent.putExtra("message_data", message);
            startActivity(intent);
            // 点击后立马隐藏卡片
            card.setVisibility(android.view.View.GONE);
        });

        // 执行动画：下滑出现
        card.setVisibility(android.view.View.VISIBLE);
        card.setTranslationY(-300);
        card.animate()
                .translationY(0)
                .setDuration(500)
                .setInterpolator(new androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                .start();

        // 3秒后自动收起
        card.removeCallbacks(hideRunnable);
        card.postDelayed(hideRunnable, 3000);
    }

    // 隐藏卡片的任务
    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            android.view.View card = findViewById(R.id.card_notification);
            if (card != null) {
                card.animate()
                        .translationY(-300)
                        .setDuration(500)
                        .withEndAction(() -> card.setVisibility(android.view.View.GONE))
                        .start();
            }
        }
    };
}