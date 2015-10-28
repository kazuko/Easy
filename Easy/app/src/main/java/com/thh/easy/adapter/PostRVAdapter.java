package com.thh.easy.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thh.easy.R;
import com.thh.easy.entity.Post;
import com.thh.easy.util.RoundedTransformation;
import com.thh.easy.util.Utils;
import com.thh.easy.view.SquaredFrameLayout;
import com.thh.easy.view.SquaredImageView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * 帖子
 * Created by cloud on 2015/10/25.
 */
public class PostRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{

    private static final int ANIMATED_ITEMS_COUNT = 2;

    private Context context;
    private int lastAnimatedPosition = -1;

    private OnFeedItemClickListener onPostItemClickListener;

    private List<Post> posts;

    private int avatarSize;


    public PostRVAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;

        avatarSize = context.getResources().getDimensionPixelSize(R.dimen.comment_avatar_size);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_post, parent, false);
            return new CellPostViewHolder(view);
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        runEnterAnimation(viewHolder.itemView, position);

        CellPostViewHolder holder = (CellPostViewHolder) viewHolder;

        // 从List中获取数据 填充View
        ((CellPostViewHolder) viewHolder).tvUserName.setText(posts.get(position).getUsername());
        ((CellPostViewHolder) viewHolder).tvPostTime.setText(posts.get(position).getDate());
        ((CellPostViewHolder) viewHolder).tvPostContent.setText(posts.get(position).getContent());
        ((CellPostViewHolder) viewHolder).tsLikesCounter.setText(""+posts.get(position).getLike());

        // 加载头像
        if (posts.get(position).getAvatar() != null && posts.get(position).getAvatar().contains("http://"))
            Picasso.with(context)
                    .load(posts.get(position).getAvatar())
                    .centerCrop()
                    .resize(avatarSize, avatarSize)
                    .transform(new RoundedTransformation())
                    .placeholder(R.mipmap.bili_default_avatar)
                    .into(((CellPostViewHolder) viewHolder).ibUserProtrait);

        // 加载图片
        if (posts.get(position).getImageUrl() != null && posts.get(position).getImageUrl().contains("http://"))
        {
            Picasso.with(context)
                    .load(posts.get(position).getImageUrl())
                    .placeholder(R.mipmap.search_loading_1)
                    .error(R.mipmap.search_failed)
                    .into(((CellPostViewHolder) viewHolder).ivPostCenter);
        }
        else
        {
            ((CellPostViewHolder) viewHolder).frameLayout.setVisibility(View.GONE);
//            System.out.println(position);
        }

        // 设置item里各点击事件
        setItemClickListener(holder, position);
    }

    /**
     * 设置点击事件
     * @param holder
     * @param position
     */
    private void setItemClickListener(CellPostViewHolder holder, int position) {

        // 绑定点击头像事件：跳转到个人展示页面
        holder.ibUserProtrait.setOnClickListener(this);
        holder.ibUserProtrait.setTag(position);

        // 绑定点击评论事件
        holder.ibComment.setOnClickListener(this);
        holder.ibComment.setTag(position);

        // 绑定点击赞事件
        holder.ibLike.setOnClickListener(this);
        holder.ibLike.setTag(position);

        // 绑定点击赞事件
        holder.ibMore.setOnClickListener(this);
        holder.ibMore.setTag(position);
    }

    /**
     * 进入动画
     * @param view
     * @param position
     */
    private void runEnterAnimation(View view, int position) {
        if (position >= ANIMATED_ITEMS_COUNT - 1) {
            return;
        }

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(Utils.getScreenHeight(context));
            view.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(700)
                    .start();
        }
    }

    public static class CellPostViewHolder extends RecyclerView.ViewHolder {

        //  头部
        ImageView ibUserProtrait;    // 发帖人头像
        TextView tvUserName;           // 发帖人名
        TextView tvPostTime;           // 发帖时间

        SquaredFrameLayout frameLayout;

        // 中间的图片
        ImageView ivPostCenter; // 中间的图片内容
        TextView tvPostContent;        // 发帖的内容

        // 底部
        ImageButton ibLike;     // 点赞
        ImageButton ibComment;  // 评论
        ImageButton ibMore;     // 更多
        TextView tsLikesCounter; // 点赞数

        public CellPostViewHolder(View view) {
            super(view);
            ibUserProtrait = (ImageView) view.findViewById(R.id.iv_post_user_image);
            tvUserName = (TextView) view.findViewById(R.id.iv_post_user_name);
            tvPostTime = (TextView) view.findViewById(R.id.item_post_time);
            tvPostContent = (TextView) view.findViewById(R.id.tv_item_post_content);

            frameLayout = (SquaredFrameLayout) view.findViewById(R.id.vImageRoot);
            ibLike = (ImageButton) view.findViewById(R.id.btnLike);
            ibComment = (ImageButton) view.findViewById(R.id.btnComments);
            ibMore = (ImageButton) view.findViewById(R.id.btnMore);

            ivPostCenter = (ImageView) view.findViewById(R.id.iv_post_center);
            tsLikesCounter = (TextView) view.findViewById(R.id.tv_post_like);
        }
    }

    @Override
    public void onClick(View view) {

        if (onPostItemClickListener != null) {
            int id = view.getId();
            switch (id){
                case R.id.iv_post_user_image:
                    // 用户头像
                    onPostItemClickListener.onProfileClick(view,(Integer) view.getTag());
                    break;
                case R.id.btnComments:
                    // 评论
                    onPostItemClickListener.onCommentsClick(view, (Integer) view.getTag());
                    break;
                case R.id.btnMore:
                    // 更多
                    onPostItemClickListener.onMoreClick(view,(Integer) view.getTag());
                    break;
                case R.id.btnLike:
                    // 点赞
                    onPostItemClickListener.onLikeClick(view,(Integer) view.getTag());
                    break;
                default:
                    break;

            }
        }
    }

    public interface OnFeedItemClickListener {
        void onCommentsClick(View v, int position); // 点击评论按钮
        void onProfileClick(View v, int position);  // 点击头像
        void onLikeClick(View v, int position);     // 点赞
        void onMoreClick(View v, int position);     // 更多
    }

    public void setOnPostItemClickListener(OnFeedItemClickListener onPostItemClickListener) {
        this.onPostItemClickListener = onPostItemClickListener;
    }
}
