package com.bytedance.mydouyin.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
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
        // 开启转场功能
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 设置窗口背景色（防止闪白）
        getWindow().getDecorView().setBackgroundColor(android.graphics.Color.parseColor("#161823"));

        // 配置共享元素动画
        initTransition();

        // 推迟动画执行
        supportPostponeEnterTransition();

        binding.getRoot().setTransitionName("shared_card");

        Message targetMsg = (Message) getIntent().getSerializableExtra("message_data");
        if (targetMsg == null) return;

        String displayName = (targetMsg.getLocalRemark() != null && !targetMsg.getLocalRemark().isEmpty())
                ? targetMsg.getLocalRemark()
                : targetMsg.getNickname();
        binding.tvTitle.setText(displayName);

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
        // 启动动画
        binding.rvChatList.post(new Runnable() {
            @Override
            public void run() {
                supportStartPostponedEnterTransition();
            }
        });

        binding.btnBack.setOnClickListener(v -> {
            supportFinishAfterTransition();
        });

        // 初始化“跟手下滑”功能
        setupSwipeToDismiss();

        // 发送消息逻辑

        android.widget.EditText etInput = binding.etInput;
        android.widget.ImageView ivAdd = binding.ivAdd;
        android.widget.ImageView btnSend = binding.btnSend;

        // 监听输入框变化，切换右侧按钮状态
        etInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    // 有文字显示红色发送键
                    ivAdd.setVisibility(android.view.View.GONE);
                    btnSend.setVisibility(android.view.View.VISIBLE);
                } else {
                    // 没文字显示加号
                    ivAdd.setVisibility(android.view.View.VISIBLE);
                    btnSend.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // 点击发送按钮
        btnSend.setOnClickListener(v -> {
                    String content = etInput.getText().toString().trim();
                    if (content.isEmpty()) return;

                    // 构造新消息
                    com.bytedance.mydouyin.model.Message myMsg = new com.bytedance.mydouyin.model.Message();
                    myMsg.setNickname("我");
                    myMsg.setContent(content);
                    myMsg.setTime(com.bytedance.mydouyin.utils.TimeUtils.getFriendlyTimeSpanByNow(System.currentTimeMillis()));
                    myMsg.setType(com.bytedance.mydouyin.model.Message.TYPE_TEXT);
                    myMsg.setSelf(true);
                    myMsg.setAvatarName("my_avatar");

                    // 存入数据仓库
                    com.bytedance.mydouyin.model.Message currentTarget =
                            (com.bytedance.mydouyin.model.Message) getIntent().getSerializableExtra("message_data");

                    if (currentTarget != null) {
                        com.bytedance.mydouyin.model.ChatDataHelper.addMessage(currentTarget.getNickname(), myMsg);
                    }

                    // 刷新界面
                    chatAdapter.notifyItemInserted(chatHistory.size() - 1);
                    binding.rvChatList.scrollToPosition(chatHistory.size() - 1);

                    // 清空输入框
                    etInput.setText("");
        });
        // 监听键盘弹出/布局变化，自动滚到底部
        binding.rvChatList.addOnLayoutChangeListener(new android.view.View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(android.view.View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // 如果底部位置变了，说明键盘弹出或收起导致高度变化
                if (bottom < oldBottom) {
                    // 并且列表里有数据
                    if (chatAdapter.getItemCount() > 0) {
                        // 自动滚动到最后一条，方便用户看到刚才的内容
                        binding.rvChatList.post(() ->
                                binding.rvChatList.scrollToPosition(chatAdapter.getItemCount() - 1)
                        );
                    }
                }
            }
        });
    }
    // 定义一个丝滑的动画集合
    private void initTransition() {
        // 定义：变大、变位置、变形状
        android.transition.TransitionSet set = new android.transition.TransitionSet();
        set.addTransition(new android.transition.ChangeBounds());
        set.addTransition(new android.transition.ChangeTransform());
        set.addTransition(new android.transition.ChangeImageTransform()); // 如果有共享图片用这个

        // 设置时长：300ms ~ 400ms 是最自然的
        set.setDuration(350);

        // 设置插值器
        set.setInterpolator(new androidx.interpolator.view.animation.FastOutSlowInInterpolator());

        // 应用到进入和返回
        getWindow().setSharedElementEnterTransition(set);
        getWindow().setSharedElementReturnTransition(set);

        // 渐变浮现
        android.transition.Fade fade = new android.transition.Fade();
        fade.setDuration(300);
        getWindow().setEnterTransition(fade);
    }
    // 实现跟手下滑退出的核心代码
    @SuppressLint("ClickableViewAccessibility")
    private void setupSwipeToDismiss() {
        // 获取内容层
        final View contentView = binding.layoutContent;

        // 获取提示语
        final android.widget.TextView tvHint = binding.tvDismissHint;

        // 监听标题栏触摸
        binding.layoutHeader.setOnTouchListener(new View.OnTouchListener() {
            private float startY;
            private boolean isTracking = false;
            private final int touchSlop = 20;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getRawY();
                        isTracking = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dy = event.getRawY() - startY;
                        if (dy > touchSlop && !isTracking) isTracking = true;

                        if (isTracking && dy > 0) {
                            // 只移动内容板，不移动底板
                            contentView.setTranslationY(dy / 1.5f);

                            // 根据距离改变提示文字
                            if (contentView.getTranslationY() > contentView.getHeight() / 5f) {
                                tvHint.setText("松手关闭");
                            } else {
                                tvHint.setText("下拉关闭页面");
                            }
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isTracking) {
                            float currentY = contentView.getTranslationY();
                            if (currentY > contentView.getHeight() / 5f) {
                                // 关闭动画：移出屏幕
                                contentView.animate()
                                        .translationY(contentView.getHeight())
                                        .setDuration(300)
                                        .withEndAction(() -> {
                                            finish();
                                            overridePendingTransition(0, 0);
                                        })
                                        .start();
                            } else {
                                // 回弹动画
                                contentView.animate().translationY(0).setDuration(200).start();
                            }
                        }
                        isTracking = false;
                        break;
                }
                return true;
            }
        });
    }
}
