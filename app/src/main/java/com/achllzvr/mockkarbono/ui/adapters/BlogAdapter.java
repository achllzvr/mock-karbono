package com.achllzvr.mockkarbono.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.api.data.models.BlogPost;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.ViewHolder> {
    private List<BlogPost> posts = new ArrayList<>();

    public void setPosts(List<BlogPost> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blog_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlogPost post = posts.get(position);
        holder.tvTitle.setText(post.title);
        holder.tvExcerpt.setText(post.excerpt);
        if (post.imageUrl != null) {
            Glide.with(holder.itemView.getContext()).load(post.imageUrl).into(holder.imgCover);
        }
    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvExcerpt;
        ImageView imgCover;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvExcerpt = itemView.findViewById(R.id.tvExcerpt);
            imgCover = itemView.findViewById(R.id.imgCover);
        }
    }
}