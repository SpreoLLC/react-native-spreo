package com.reactlibrary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;




public class SpreoViewPagerAdapter extends PagerAdapter {

    private ArrayList<SpreoCoverFlowData> mData = new ArrayList<>(0);
    private Context mContext;
    SpreoVideoView videoView;
    private int item = 0;

    public void setItem(int item) {
        this.item = item;
    }


    public SpreoViewPagerAdapter(Context context, ArrayList<SpreoCoverFlowData> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = null;
        if (mData.get(position).galleryObject != null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.spreo_item_coverflow, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.image_cover);
            Bitmap bitmap = mData.get(position).galleryObject.getBitmap();
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else if (mData.get(position).url != null && !mData.get(position).url.isEmpty()) {
            view = LayoutInflater.from(mContext).inflate(R.layout.spreo_item_coverflow_video, null);
            videoView = (SpreoVideoView) view.findViewById(R.id.video_cover);
            videoView.setActivity((Activity) mContext);
            videoView.setBackgroundColor(Color.WHITE);
            final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            Uri uri = Uri.parse(mData.get(position).url);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    progressBar.setVisibility(View.GONE);
                    videoView.showControls();
                    if (item == getCount() - 1) {
                        videoView.start();
                    }
                }
            });
            try {
                videoView.setVideoURI(uri);

            } catch (IOException e) {
                e.printStackTrace();
            }
            //videoView.setVideoPath(mData.get(position).url);


        }
        if (view != null) {
            container.addView(view);
        }

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    public void startVideo() {
        if (videoView != null) {
            videoView.start();
        }
    }

    public void stopVideo() {
        if (videoView != null) {
            videoView.stop();
        }
    }

    public void pauseVideo() {
        if (videoView != null) {
            videoView.pause();
        }
    }

//    public static Bitmap getScaledBitmap(Bitmap bmp) {
//        Bitmap result = bmp;
//
//        try {
//            final int maxSize = 500;
//            int outWidth;
//            int outHeight;
//            int inWidth = bmp.getWidth();
//            int inHeight = bmp.getHeight();
//            if(inWidth > inHeight){
//                outWidth = maxSize;
//                outHeight = (inHeight * maxSize) / inWidth;
//            } else {
//                outHeight = maxSize;
//                outWidth = (inWidth * maxSize) / inHeight;
//            }
//
//            result = Bitmap.createScaledBitmap(bmp, outWidth, outHeight, false);
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
//
//        return result;
//    }



}

