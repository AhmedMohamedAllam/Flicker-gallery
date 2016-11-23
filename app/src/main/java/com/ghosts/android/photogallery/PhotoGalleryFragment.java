package com.ghosts.android.photogallery;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.ghosts.android.photogallery.GalleryItem.PhotosBean.PhotoBean;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Allam on 12/03/2016.
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "Allam";
    private List<PhotoBean> mitems = new ArrayList<>();
    private RecyclerView mPhotoRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private int mCurrentPage = 1;
    private ProgressDialog mProgressDialog;
    private PhotoAdapter mAdapter;
    private boolean mLoadFlag = false;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems(1);

        PollService.setServiceAlarm(getContext(), true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.photo_gallery_recycler_view);

        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        mGridLayoutManager = (GridLayoutManager) mPhotoRecyclerView.getLayoutManager();

        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mGridLayoutManager.findLastCompletelyVisibleItemPosition() == mitems.size() - 1) {
                    if (mCurrentPage != new GalleryItem.PhotosBean().getPages()) {
                        updateItems(++mCurrentPage);
                    }
                }
            }
        });
        setUpAdapter();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_fragment_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.search_view);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                QueryPreferences.setStoredQuery(getActivity(), query);
                updateItems(mCurrentPage);
                searchView.onActionViewCollapsed();
                mLoadFlag = true;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_search:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems(mCurrentPage);
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getContext(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("page", mCurrentPage);
    }

    private void setUpAdapter() {
        mAdapter = new PhotoAdapter(mitems);
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(mAdapter);
        }
    }

    private void updateItems(final int page) {
        mProgressDialog = ProgressDialog.show(getActivity(), "Loading images", "Loading...", true, true);
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(page, query).execute();
    }


    public class PhotoHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.recycler_image_view);
        }

        public void bindPhotos(PhotoBean photo) {
            Picasso.with(getActivity()).load(photo.getUrl_s()).placeholder(R.drawable.loading).into(mImageView);
        }

    }

    public class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        List<PhotoBean> items;

        public PhotoAdapter(List<PhotoBean> items) {
            this.items = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.recycler_view_item, parent, false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            PhotoBean photo = items.get(position);
            holder.bindPhotos(photo);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }


    public class FetchItemsTask extends AsyncTask<Void, Void, List<PhotoBean>> {
        private int mPage;
        private String mQuery;

        public FetchItemsTask(int page, String query) {
            mPage = page;
            mQuery = query;
        }

        @Override
        protected List<PhotoBean> doInBackground(Void... params) {

            try {
                if (mQuery != null)
                    return new FlickerFetcher().fetchItems(mPage, FlickerFetcher.SEARCH_METHOD, mQuery);
                else
                    return new FlickerFetcher().fetchItems(mPage, FlickerFetcher.FETCH_RECENTS_METHOD, null);

            } catch (Exception e) {
                Log.e(TAG, "No Internet", e);
                return null;
            }

        }


        @Override
        protected void onPostExecute(List<PhotoBean> items) {

            if (items.isEmpty()) {
                Toast.makeText(getActivity(), "No images found !", Toast.LENGTH_SHORT).show();
            } else {
                if (mLoadFlag) {
                    mitems = new ArrayList<>();
                }

                for (int i = 0; i < items.size(); i++) {
                    mitems.add(items.get(i));
                }

                if (mLoadFlag) {
                    setUpAdapter();
                    mLoadFlag = false;
                } else {
                    mAdapter.notifyDataSetChanged();
                }
            }
                mProgressDialog.dismiss();
        }
    }


}
