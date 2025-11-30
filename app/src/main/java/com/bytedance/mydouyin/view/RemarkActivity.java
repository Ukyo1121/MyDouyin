package com.bytedance.mydouyin.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bytedance.mydouyin.databinding.ActivityRemarkBinding;
import com.bytedance.mydouyin.model.RemarkDatabaseHelper;

public class RemarkActivity extends AppCompatActivity {

    private ActivityRemarkBinding binding;
    private RemarkDatabaseHelper dbHelper;
    private String currentNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRemarkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new RemarkDatabaseHelper(this);

        currentNickname = getIntent().getStringExtra("nickname");

        if (currentNickname != null) {
            binding.tvTitleNickname.setText("正在为 " + currentNickname + " 设置备注");

            String savedRemark = dbHelper.getRemark(currentNickname);
            if (savedRemark != null) {
                // 如果以前有备注有，填入输入框，方便用户修改
                binding.etRemark.setText(savedRemark);
                binding.etRemark.setSelection(savedRemark.length());
            }
        }

        // 保存按钮逻辑
        binding.btnSave.setOnClickListener(v -> {
            String inputRemark = binding.etRemark.getText().toString().trim();

            if (currentNickname != null) {
                // 写入数据库
                dbHelper.saveRemark(currentNickname, inputRemark);

                // 提示用户保存成功
                Toast.makeText(this, "备注已保存", Toast.LENGTH_SHORT).show();

                finish();
            }
        });

        // 退出按钮逻辑
        binding.btnExit.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("你所做的修改尚未保存，确认退出吗？")
                    .setPositiveButton("确认退出", (dialog, which) -> finish())
                    .setNegativeButton("取消", null)
                    .show();
        });
    }
}