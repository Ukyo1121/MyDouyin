package com.bytedance.mydouyin.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bytedance.mydouyin.databinding.ActivityRemarkBinding;
import com.bytedance.mydouyin.model.Message;
import com.bytedance.mydouyin.model.RemarkDatabaseHelper;

public class RemarkActivity extends AppCompatActivity {

    private ActivityRemarkBinding binding;
    private RemarkDatabaseHelper dbHelper;
    private Message targetMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRemarkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new RemarkDatabaseHelper(this);

        // 获取传递过来的 Message 对象
        targetMsg = (Message) getIntent().getSerializableExtra("message_data");

        // 兼容旧逻辑
        if (targetMsg == null && getIntent().getStringExtra("nickname") != null) {
            targetMsg = new Message();
            targetMsg.setNickname(getIntent().getStringExtra("nickname"));
        }

        if (targetMsg != null) {
            // 显示原昵称
            binding.tvNickname.setText("昵称: " + targetMsg.getNickname());

            // 显示头像
            loadAvatar();

            // 回显数据库里的数据 (备注 & 电话)
            RemarkDatabaseHelper.LocalInfo info = dbHelper.getLocalInfo(targetMsg.getNickname());

            if (info.remark != null) {
                binding.etRemark.setText(info.remark);
                binding.etRemark.setSelection(info.remark.length()); // 光标移到最后
            }
            if (info.phone != null) {
                binding.etPhone.setText(info.phone);
            }
        }

        // 保存按钮
        binding.btnSave.setOnClickListener(v -> {
            if (targetMsg != null) {
                String inputRemark = binding.etRemark.getText().toString().trim();
                String inputPhone = binding.etPhone.getText().toString().trim();

                // 保存到数据库
                dbHelper.saveRemarkInfo(targetMsg.getNickname(), inputRemark, inputPhone);

                Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // 取消按钮
        binding.btnExit.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定要放弃修改吗？")
                    .setPositiveButton("确定退出", (dialog, which) -> finish())
                    .setNegativeButton("继续编辑", null)
                    .show();
        });
    }

    // 辅助方法：加载头像
    private void loadAvatar() {
        if (targetMsg.getAvatarUrl() != null && !targetMsg.getAvatarUrl().isEmpty()) {
            Glide.with(this).load(targetMsg.getAvatarUrl()).circleCrop().into(binding.ivAvatar);
        }
        else if (targetMsg.getAvatarName() != null && !targetMsg.getAvatarName().isEmpty()) {
            int resId = getResources().getIdentifier(targetMsg.getAvatarName(), "drawable", getPackageName());
            if (resId > 0) Glide.with(this).load(resId).circleCrop().into(binding.ivAvatar);
        }
        else {
            binding.ivAvatar.setImageResource(targetMsg.getAvatarResId() != 0 ? targetMsg.getAvatarResId() : android.R.drawable.sym_def_app_icon);
        }
    }
}