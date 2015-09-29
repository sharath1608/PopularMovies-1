package app.sunshine.android.example.com.popmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Asus1 on 9/20/2015.
 */

/**
 * Created by Asus1 on 8/31/2015.
 */
public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.ViewHolder> {

    private List<GridViewObject> gridViewList;
    private Context mContext;
    private OnItemClickListener mItemClickListener;

    public void setMovieList(List<GridViewObject> gridViewList) {
        this.gridViewList = gridViewList;
    }

    public MovieListAdapter(List<GridViewObject> gridViewList ) {
        this.gridViewList = new ArrayList<>(gridViewList);
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageView;
        public TextView textView;
        public ProgressBar progBar;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.griditem_image);
            textView = (TextView) itemView.findViewById(R.id.griditem_tag);
            progBar = (ProgressBar) itemView.findViewById(R.id.prog_bar);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
            }
        }
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View gridView = layoutInflater.inflate(R.layout.grid_item_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(gridView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MovieListAdapter.ViewHolder holder, int position) {
        final String LOG_TAG = getClass().getSimpleName();
        GridViewObject viewObject = gridViewList.get(position);
        final ProgressBar progressBar = holder.progBar;
        holder.textView.setText(viewObject.getMovieTag());
        PicassoImageCache
        .getPicassoInstance(mContext)
                .load(gridViewList.get(position).getMovieUrl())
                .error(R.drawable.user_placeholder_image)
                .fit()
                .into(holder.imageView, new Callback() {

                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(ProgressBar.GONE);
                    }

                    @Override
                    public void onError() {
                        Log.e(LOG_TAG, "Error in loading images");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return gridViewList.size();
    }
}