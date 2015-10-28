package com.thh.easy.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.ext.HttpCallback;
import com.android.volley.ext.RequestInfo;
import com.android.volley.ext.tools.HttpTools;
import com.squareup.picasso.Picasso;
import com.thh.easy.Constant.StringConstant;
import com.thh.easy.R;
import com.thh.easy.adapter.PostRVAdapter;
import com.thh.easy.entity.Post;
import com.thh.easy.entity.User;
import com.thh.easy.util.FileUtil;
import com.thh.easy.util.RoundedTransformation;
import com.thh.easy.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *   主界面:
 *         显示帖子
 *  @author cloud
 *  @time 2015 10 24
 */
public class MainActivity extends BaseDrawerActivity implements PostRVAdapter.OnFeedItemClickListener {

    private static final String TAG = "MainActivity";

    private boolean pendingIntroAnimation;// 是否开始进入动画
    private MenuItem inboxMenuItem;
    private PostRVAdapter postRVAdapter;

    @Bind(R.id.rv_post)
    public RecyclerView rvPost;     // 主界面帖子列表

    @Bind(R.id.iv_logo)
    public ImageView ivLogo;        // toolbar的logo

    @Bind(R.id.ib_new_post)
    public FloatingActionButton btnCreate;   // floating action button

    @Bind(R.id.cl_main_container)
    CoordinatorLayout clContainer;

    List<Post> postList = new ArrayList<Post>();

    LinearLayoutManager linearLayoutManager;

    boolean isLoading = false;

    int currentPage = 1;   // 当前页

    HttpTools httpTools;

    SharedPreferences sp;

    User u = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        HttpTools.init(this);
        httpTools = new HttpTools(this);

        // 设置进入动画
        if (savedInstanceState == null) {
            pendingIntroAnimation = true;
        }

        loadPosts();
       // 设置RecycleView
        setupPost();

        Intent intent = getIntent();
        if (intent.getBooleanExtra("login_success", false)) {
            Bundle bundle = intent.getBundleExtra("user");
            u = (User) bundle.get("user");
            setUserInfo(u);

            return;
        }

        // 验证用户之前是否登录
        sp = getSharedPreferences("user_sp", Context.MODE_PRIVATE);
        if (sp.getBoolean("user_login", false)) {
            u = (User)FileUtil.readObject(this, "user");
            setUserInfo(u);
        }
    }

    private void setUserInfo (User u) {
        username.setText(u.getUsername());

        if ("0".equals(u.getGender())) {
            gender.setBackgroundResource(R.mipmap.ic_user_female);
        }
        else if ("1".equals(u.getGender())) {
            gender.setBackgroundResource(R.mipmap.ic_user_male);
        }
        else {
            gender.setBackgroundResource(R.mipmap.ic_user_sox);
        }

        File avatar = new File (u.getAvatarFilePath());
        Picasso.with(getApplicationContext())
                .load(avatar)
                .centerCrop()
                .resize(avatarSize, avatarSize)
                .transform(new RoundedTransformation())
                .into(ivMenuUserProfilePhoto);
    }

    /**
     * 设置帖子recyclerview的相应初始数据
     */
    private void setupPost() {
        linearLayoutManager = new LinearLayoutManager(this);
        rvPost.setLayoutManager(linearLayoutManager);

        postRVAdapter = new PostRVAdapter(this, postList);
        postRVAdapter.setOnPostItemClickListener(this);
//        rvPost.setAdapter(postRVAdapter);


        //postRVAdapter.notifyItemChanged(postList.size());
        postRVAdapter.setOnPostItemClickListener(this);

        rvPost.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItems = linearLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = linearLayoutManager.getItemCount();

                //Log.i (TAG, "last - " + lastVisibleItems + " totle - " + totalItemCount);

                if (lastVisibleItems == totalItemCount-1 && dy > 0) {
                    if (isLoading) {
                        Log.d(TAG, "ignore manually update!");
                    } else {
                        // loadPosts中控制isLoading
                         loadPosts();
                        Log.i(TAG, "new data");
                        isLoading = false;
                    }
                }
            }
        });


    }

    /**
     * 请求帖子数据
     */
    private void loadPosts() {
        Map<String, String> params = new HashMap<String, String>(2);
        params.put(StringConstant.CURRENT_PAGE_KEY, currentPage+"");
        params.put(StringConstant.PER_PAGE_KEY, StringConstant.PER_PAGE_COUNT + "");
        RequestInfo info = new RequestInfo(StringConstant.SERVER_NEWPOST_URL, params);
        httpTools.post(info, new HttpCallback() {
            @Override
            public void onStart() {
                isLoading = true;
                Log.i(TAG, "当前页" + currentPage);
            }

            @Override
            public void onFinish() {
                // 一共加载多少条
                isLoading = false;
            }

            @Override
            public void onResult(String s) {
                Log.i(TAG, s);

                if (StringConstant.NULL_VALUE.equals(s)) {
                    Snackbar.make(clContainer, "网络貌似粗错了", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if ("[]".equals(s)) {
                   // Snackbar.make(clContainer, "什么帖子都没有呦", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                onReadJson(s);

                currentPage++;
            }

            @Override
            public void onError(Exception e) {
            }

            @Override
            public void onCancelled() {
            }

            @Override
            public void onLoading(long l, long l1) {

            }
        });


    }

    private void onReadJson(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);

            Log.i(TAG, jsonArray.length()+"");

            for (int i = 0 ; i < jsonArray.length() ; i ++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Post post = null;

                String imageUrl = null;
                if (!jsonObject.isNull("image")) {
                    imageUrl = jsonObject.getString("image");
                    Log.i(TAG, "装逼如风 常伴吾身"+i);
                }

                String avatar = null;
                if (!jsonObject.isNull("avatar"))
                    avatar = jsonObject.getString("avatar");

                post = new Post(jsonObject.getInt("id"),
                        jsonObject.getInt("user.id"),
                        jsonObject.getString("user.name"),
                        jsonObject.getString("contents"),
                        jsonObject.getString("dates"),
                        imageUrl,
                        avatar,
                        jsonObject.getInt("likes"));

                postList.add(post);
            }


            postRVAdapter.notifyItemChanged(postList.size());

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "解析Json出错");
        }
    }

    /**
     * 设置menu上的进入动画
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        inboxMenuItem = menu.findItem(R.id.action_search);
        inboxMenuItem.setActionView(R.layout.menu_item_view);
        if (pendingIntroAnimation) {
            // 开始动画~\(≧▽≦)/~啦啦啦
            pendingIntroAnimation = false;
            startIntroAnimation();
        }
        return true;
    }


    /**
     * toolbar 动画
     */
    private static final int ANIM_DURATION_TOOLBAR = 300;
    private void startIntroAnimation() {
        btnCreate.setTranslationY(2 * getResources().getDimensionPixelOffset(R.dimen.btn_fab_size));
        int actionbarSize = Utils.dpToPx(56);
        getToolbar().setTranslationY(-actionbarSize);
        ivLogo.setTranslationY(-actionbarSize);
        inboxMenuItem.getActionView().setTranslationY(-actionbarSize);

        getToolbar().animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(300);
        ivLogo.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(400);
        inboxMenuItem.getActionView().animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startContentAnimation();
                    }
                })
                .start();
    }


    /**
     *  floatingActionButton 进入动画
     */
    private static final int ANIM_DURATION_FAB = 400;
    private void startContentAnimation() {
        btnCreate.animate()
                .translationY(0)
                .setInterpolator(new OvershootInterpolator(1.f))
                .setStartDelay(300)
                .setDuration(ANIM_DURATION_FAB)
                .start();

        // 动画结束再设置帖子内容

        // 网络检查
        if (!Utils.checkNetConnection(getApplicationContext())) {
            Snackbar.make(clContainer, "少年呦 你联网了嘛", Snackbar.LENGTH_SHORT).show();
        }

        //loadPosts();
        rvPost.setAdapter(postRVAdapter);
        isLoading = false;
    }


    /**
     * 点击帖子中的头像后，进入个人信息界面
     * @param v
     * @param position
     */
    @Override
    public void onProfileClick(View v, int position) {
        // 获得点击头像时的位置
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2;

        // 进入用户信息界面时，设置进入动画
        UserProfileActivity.startUserProfileFromLocation(startingLocation, this);
        overridePendingTransition(0, 0);
    }


    /**
     * 回复点击事件
     * @param v
     * @param position
     */
    @Override
    public void onCommentsClick(View v, int position) {

        Toast.makeText(MainActivity.this, "点击了评论", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onLikeClick(View v, int position) {
        Toast.makeText(MainActivity.this, "赞一下~(≧▽≦)~啦啦啦", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMoreClick(View v, int position) {
        Toast.makeText(MainActivity.this, "更多", Toast.LENGTH_LONG).show();
    }

    // 点击更多跳转到更多界面
    @OnClick(R.id.more_new_post)
    void morePost() {
        startActivity(new Intent(MainActivity.this, NewPostActivity.class));
    }
}
