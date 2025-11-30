package com.bytedance.mydouyin.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bytedance.mydouyin.databinding.ActivityMainBinding;
import com.bytedance.mydouyin.viewmodel.MainViewModel;

import com.bytedance.mydouyin.R;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
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
            public void onItemClick(com.bytedance.mydouyin.model.Message message) {
                // 创建 Intent跳转到 RemarkActivity
                android.content.Intent intent = new android.content.Intent(MainActivity.this, com.bytedance.mydouyin.view.RemarkActivity.class);

                intent.putExtra("nickname", message.getNickname());

                // 启动跳转
                startActivity(intent);
            }
        });

        binding.rvMessageList.setAdapter(adapter);

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

        // 监听底部加载条的状态
        viewModel.isLoadingMoreState.observe(this, new androidx.lifecycle.Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isVisible) {
                // 状态变了，控制进度条显示/隐藏
                if (isVisible) {
                    binding.pbLoadMore.setVisibility(android.view.View.VISIBLE);
                } else {
                    binding.pbLoadMore.setVisibility(android.view.View.GONE);
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
    }
    @Override
    protected void onResume() {
        super.onResume();

        // 每次回到页面，都让 ViewModel 重新检查一下备注
        if (viewModel != null) {
            viewModel.reloadRemarks();
        }
    }
}