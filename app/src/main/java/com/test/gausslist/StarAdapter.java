package com.test.gausslist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.test.gausslist.parallaxstyle.VerticalHeadMovingStyle;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import cn.jzvd.JZVideoPlayerStandard;


/**
 * Created by Xiejq on 2018/6/14.
 */
public class StarAdapter extends RecyclerView.Adapter<CommonViewHolder> {

    private static final int ITEM_PIC = 0;
    private static final int ITEM_VIDEO = 1;
    private static final String VIDEO_URL = "http://jzvd.nathen.cn/6ea7357bc3fa4658b29b7933ba575008/fbbba953374248eb913cb1408dc61d85-5287d2089db37e62345123a1be272f8b.mp4";


    private final Activity mActivity;
    private RecyclerView mRecyclerView;
    private HashMap<Integer, RecyclerView.OnScrollListener> mScrollListenerHashMap;
    private HashMap<Integer, CustomGlobalLayoutListener> mOnGlobalLayoutListenerHashMap;
    private HashMap<Integer, Runnable> mItemBgRunnables;  //保留页面上的线程进度

    private String[] mColorList = {"#99FFB6C1", "#99800080", "#994B0082", "#99F8F8FF", "#9900FFFF", "#99FFFF00", "#99FFA500", "#99C0C0C0"};
    private int[] mImgs = {R.drawable.m001, R.drawable.m002, R.drawable.m003, R.drawable.m004, R.drawable.m005};


    @SuppressLint("UseSparseArrays")
    public StarAdapter(Activity activity) {
        mScrollListenerHashMap = new HashMap<>();
        mOnGlobalLayoutListenerHashMap = new HashMap<>();
        mItemBgRunnables = new HashMap<>();
        mActivity = activity;

    }

    @NonNull
    @Override
    public CommonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_PIC) {
            return CommonViewHolder.create(parent.getContext(), R.layout.item_one, parent);
        } else if (viewType == ITEM_VIDEO) {
            return CommonViewHolder.create(parent.getContext(), R.layout.item_two, parent);
        }
        return CommonViewHolder.create(parent.getContext(), R.layout.item_one, parent);
    }


    @Override
    public int getItemViewType(int position) {
        if (position % 6 == 0) {
            return ITEM_VIDEO;
        } else {
            return ITEM_PIC;
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        ThreadPoolExecutor itemBgSingleThreadPool = (ThreadPoolExecutor) ThreadManager.getInstance().getItemBgSingleThreadPool();
        itemBgSingleThreadPool.getQueue().clear();
    }

    @Override
    public void onBindViewHolder(@NonNull CommonViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        RelativeLayout itemHead = holder.getView(R.id.view_head);
        final RelativeLayout layoutContent = holder.getView(R.id.layout_content);
        TextView tvPos = holder.getView(R.id.pos);
        final ImageView ivBg = holder.getView(R.id.img_bg_layout);
        TextView tvTitle = holder.getView(R.id.tv_title);
        final ScrollParallaxImageView guessView = holder.getView(R.id.faker_guess_view);
        guessView.setEnableScrollParallax(true);
        guessView.setParallaxStyles(new VerticalHeadMovingStyle());
        if (getItemViewType(position) == ITEM_PIC) {
            ImageView picContent = holder.getView(R.id.pic_content);
            picContent.setImageResource(mImgs[(position - 1) % 5]);
        } else if (getItemViewType(position) == ITEM_VIDEO) {
            JZVideoPlayerStandard videoPlay = holder.getView(R.id.video_content);
            videoPlay.setUp(VIDEO_URL, 1);
            videoPlay.thumbImageView.setImageResource(mImgs[(position / 6) % 5]);
            if (videoPlay.textureViewContainer.getChildAt(0) instanceof TextureView) {
                TextureView textureView = (TextureView) videoPlay.textureViewContainer.getChildAt(0);
                textureView.setOpaque(true);
            }
        }
        //防止页面卡顿，异步处理图片
        Thread bgThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap gaussBlur = ImageUtils.gaussBlur(ivBg.getContext(), BitmapFactory.decodeResource(ivBg.getContext().getResources(), mImgs[(position > 1 ? (position - 1) : position) % 5]), 10);
                final Bitmap bitmapBg = ImageUtils.toConformBitmap(gaussBlur, mColorList[position % 8]);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivBg.setImageBitmap(bitmapBg);
                        listenerGlobal(position, layoutContent, guessView);
                    }
                });
            }
        });
        ThreadManager.getInstance().getItemBgSingleThreadPool().execute(bgThread);
        mItemBgRunnables.put(position, bgThread);

        tvTitle.setText(mColorList[position % 8]);
        tvTitle.setBackgroundColor(Color.parseColor(mColorList[position % 8]));


        tvPos.setText(String.valueOf(position) + mColorList[position % 8]);
        itemHead.setTag(position);
        itemHead.offsetTopAndBottom(0);
        listenerItemHead(itemHead, holder.getConvertView(), position, guessView);
        itemHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "bingo", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void listenerGlobal(int position, RelativeLayout layoutContent, ScrollParallaxImageView guessView) {
        CustomGlobalLayoutListener onGlobalLayoutListener = mOnGlobalLayoutListenerHashMap.get(position);
        if (onGlobalLayoutListener == null) {
            onGlobalLayoutListener = new CustomGlobalLayoutListener(new WeakReference<View>(layoutContent), new WeakReference<ImageView>(guessView), new WeakReference<>(mActivity));
            layoutContent.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
            mOnGlobalLayoutListenerHashMap.put(position, onGlobalLayoutListener);
            onGlobalLayoutListener.setSetCover(false);
        } else {
            layoutContent.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
            onGlobalLayoutListener.setSetCover(false);
        }
        // TODO: 2018/7/12  测试
        layoutContent.setTag(position);
        onGlobalLayoutListener.attach();
    }


    @Override
    public void onViewRecycled(@NonNull CommonViewHolder holder) {
        int adapterPosition = holder.getAdapterPosition();
        RecyclerView.OnScrollListener onScrollListener = mScrollListenerHashMap.get(adapterPosition);
        if (onScrollListener != null && mRecyclerView != null) {
            mRecyclerView.removeOnScrollListener(onScrollListener);
            mScrollListenerHashMap.remove(adapterPosition);
        }
        RelativeLayout layoutContent = holder.getView(R.id.layout_content);
        CustomGlobalLayoutListener onGlobalLayoutListener = mOnGlobalLayoutListenerHashMap.get(holder.getAdapterPosition());
        if (onGlobalLayoutListener != null) {
            onGlobalLayoutListener.detached();
            layoutContent.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
        // notice  条目重用时，移除此堵塞线程
        Runnable itemRunnable = mItemBgRunnables.get(adapterPosition);
        if (itemRunnable != null) {
            ThreadPoolExecutor itemBgSingleThreadPool = (ThreadPoolExecutor) ThreadManager.getInstance().getItemBgSingleThreadPool();
            if (itemBgSingleThreadPool.getQueue().contains(itemRunnable)) {
                itemBgSingleThreadPool.getQueue().remove(itemRunnable);
            }
            mItemBgRunnables.remove(adapterPosition);
        }
        super.onViewRecycled(holder);
    }

    private void listenerItemHead(final RelativeLayout itemHead, final View item, final int position, final ScrollParallaxImageView guessView) {
        if (mRecyclerView != null) {
            RecyclerView.OnScrollListener onScrollListener;
            if (mScrollListenerHashMap.get(position) == null) {
                onScrollListener = new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        if (layoutManager != null) {
                            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                            Integer pos = (Integer) itemHead.getTag();
                            if (position == firstVisibleItemPosition && firstVisibleItemPosition == pos) {
                                int[] outLocationItem = new int[2];
                                int[] outLocationItemHead = new int[2];
                                int[] outLocationRecycler = new int[2];
                                item.getLocationOnScreen(outLocationItem);
                                itemHead.getLocationOnScreen(outLocationItemHead);
                                recyclerView.getLocationOnScreen(outLocationRecycler);
                                int headHeight = itemHead.getMeasuredHeight();
                                int parentHeight = item.getMeasuredHeight();
                                if (position == 0) {
                                    Log.i("今晚打老虎", "onScrolled:    >>0<<  outLocationRecycler[1] - outLocationItemHead[1] 》》 " + (outLocationRecycler[1] - outLocationItemHead[1]));
                                    Log.i("今晚打老虎", "onScrolled:    >>0<<  outLocationRecycler[1]  》》 " + (outLocationRecycler[1]));
                                    Log.i("今晚打老虎", "onScrolled:    >>0<<  outLocationItemHead[1]  》》 " + (outLocationItemHead[1]));
                                }
                                //判断滑动方向和距离
                                // 向上滑动：outLocationRecycler[1] - outLocationItem[1] <= parentHeight - headHeight条件下执行偏移
                                // 向下滑动：outLocationRecycler[1] - outLocationItem[1] <= parentHeight - headHeight条件下执行偏移       outLocationRecycler[1] - outLocationItem[1] > parentHeight - headHeight时调整偏移
                                if (dy < 0) {
                                    if (outLocationRecycler[1] - outLocationItem[1] <= parentHeight - headHeight) {
                                        itemHead.offsetTopAndBottom(outLocationRecycler[1] - outLocationItemHead[1]);
                                        guessView.setEnableScrollParallax(true);
                                        guessView.setDrawPosType(ScrollParallaxImageView.DrawPosType.SLIDE);
                                    } else {
                                        //因为dy离散，故做一下校验
                                        if (parentHeight - (outLocationRecycler[1] - outLocationItem[1]) != (headHeight - (outLocationRecycler[1] - outLocationItemHead[1]))) {
                                            itemHead.offsetTopAndBottom(parentHeight - (outLocationRecycler[1] - outLocationItem[1]) - (headHeight - (outLocationRecycler[1] - outLocationItemHead[1])));
                                        }
                                        guessView.setDrawPosType(ScrollParallaxImageView.DrawPosType.END);
                                        guessView.setEnableScrollParallax(false);
                                    }
                                } else {
                                    if (outLocationRecycler[1] - outLocationItem[1] <= parentHeight - headHeight) {
                                        itemHead.offsetTopAndBottom(outLocationRecycler[1] - outLocationItemHead[1]);
                                        guessView.setEnableScrollParallax(true);
                                        guessView.setDrawPosType(ScrollParallaxImageView.DrawPosType.SLIDE);
                                    } else {
                                        //因为dy离散，故做一下校验
                                        if (parentHeight - (outLocationRecycler[1] - outLocationItem[1]) != (headHeight - (outLocationRecycler[1] - outLocationItemHead[1]))) {
                                            itemHead.offsetTopAndBottom(parentHeight - (outLocationRecycler[1] - outLocationItem[1]) - (headHeight - (outLocationRecycler[1] - outLocationItemHead[1])));
                                        }
                                        guessView.setDrawPosType(ScrollParallaxImageView.DrawPosType.END);
                                        guessView.setEnableScrollParallax(false);
                                    }
                                }
                            } else {
                                guessView.setDrawPosType(ScrollParallaxImageView.DrawPosType.START);
                                guessView.setEnableScrollParallax(false);
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) itemHead.getLayoutParams();
                                if (layoutParams != null) {
                                    layoutParams.topMargin = 0;
                                    itemHead.setLayoutParams(layoutParams);
                                }
                            }
                        }
                    }
                };
                mScrollListenerHashMap.put(position, onScrollListener);
            } else {
                onScrollListener = mScrollListenerHashMap.get(position);
            }
            mRecyclerView.addOnScrollListener(onScrollListener);
        }


    }

    @Override
    public int getItemCount() {
        return 100;
    }


    public static class CustomGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {

        private boolean isSetCover;
        private View layoutContent;
        private ImageView guessView;
        private WeakReference<View> mWeakContent;
        private WeakReference<ImageView> mWeakGuess;
        private WeakReference<Activity> mActivityWeakReference;
        public boolean isAttaching;
        private Thread mCacheThread;


        public CustomGlobalLayoutListener(WeakReference<View> layoutContent, WeakReference<ImageView> guessView, WeakReference<Activity> activityWeakReference) {
            this.mWeakContent = layoutContent;
            this.mWeakGuess = guessView;
            this.mActivityWeakReference = activityWeakReference;
        }

        public void setSetCover(boolean setCover) {
            isSetCover = setCover;
        }

        @Override
        public void onGlobalLayout() {
            if (mWeakContent.get() != null && mWeakGuess.get() != null && mActivityWeakReference.get() != null) {
                layoutContent = mWeakContent.get();
                guessView = mWeakGuess.get();
                if (layoutContent.getWidth() > 0 && !isSetCover) {
                    isSetCover = true;
                    //防止页面卡顿，异步处理图片
                    mCacheThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap coverGuess = Bitmap.createBitmap(layoutContent.getMeasuredWidth(), layoutContent.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(coverGuess);
                            layoutContent.draw(canvas);
                            final Bitmap gaussBlurCover = ImageUtils.gaussBlur(mActivityWeakReference.get(), coverGuess, 20);
                            mActivityWeakReference.get().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (isAttaching) {
                                        guessView.setImageBitmap(gaussBlurCover != null ? gaussBlurCover : coverGuess);
                                    } else {
                                        ImageUtils.recycleBitmap(coverGuess);
                                    }
                                    if (gaussBlurCover != null) {
                                        ImageUtils.recycleBitmap(coverGuess);
                                    }
                                }
                            });
                        }
                    });
                    layoutContent.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ThreadManager.getInstance().getFixThreadPool().execute(mCacheThread);
                        }
                    }, 10);
                    //在背景绘制出来之后，截取下层的bitmap
//                layoutContent.setDrawingCacheEnabled(true);
//                layoutContent.buildDrawingCache(true);
//                Bitmap drawingCache = layoutContent.getDrawingCache(true);
//                if (drawingCache != null) {
//                    Bitmap coverGuess = Bitmap.createBitmap(drawingCache);
//                    layoutContent.destroyDrawingCache();
//                    layoutContent.setDrawingCacheEnabled(false);
//                    Bitmap gaussBlurCover = ImageUtils.gaussBlur(mActivityWeakReference.get(), coverGuess, 10);
//                    guessView.setImageBitmap(gaussBlurCover != null ? gaussBlurCover : coverGuess);
//                    isSetCover = true;
//                }
                }
            }
        }

        public void attach() {
            isAttaching = true;
        }

        public void detached() {
            //移除线程
            ThreadPoolExecutor fixThreadPool = (ThreadPoolExecutor) ThreadManager.getInstance().getFixThreadPool();
            BlockingQueue<Runnable> queue = fixThreadPool.getQueue();
            queue.remove(mCacheThread);
            isAttaching = false;
        }
    }
}
