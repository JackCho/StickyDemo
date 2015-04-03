package me.ele.homedemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import me.ele.components.recyclerview.EMRecyclerView;
import me.ele.components.recyclerview.HeaderViewRecyclerAdapter;
import me.ele.components.recyclerview.OnMoreListener;
import me.ele.components.refresh.PullRefreshLayout;
import me.ele.omniknight.OKFragment;
import roboguice.inject.ContentView;

/**
 * Created by caoyubin on 15/3/31.
 */
@ContentView(R.layout.fragment_first)
public class FirstFragment extends OKFragment {

    private int scrollTotalDeltaY = 0;
    private int pullRefreshTotalDetalY = 0;

    @InjectView(R.id.sticky)
    protected TextView stickyView;
    @InjectView(R.id.recyclerview)
    protected EMRecyclerView recyclerView;

    private HeaderViewRecyclerAdapter adapter;
    private MyAdapter realAdapter;
    private LinearLayout footer;
    private View header;

    private Handler handler;

    private int count;

    public static FirstFragment newInstance(int count) {
        FirstFragment fragment = new FirstFragment();
        Bundle args = new Bundle();
        args.putInt("count", count);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        count = getArguments().getInt("count");

        handler = new Handler(Looper.getMainLooper());

        setHasOptionsMenu(true);

        stickyView.setText("Sticky");
        stickyView.setTranslationY(getOriginOffset());
        stickyView.setOnClickListener(clickListener);

        header = getActivity().getLayoutInflater().inflate(R.layout.head, null);
        realAdapter = new MyAdapter(getData());
        adapter = new HeaderViewRecyclerAdapter(realAdapter);
        adapter.addHeaderView(header);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollListener(scrollListener);

        handler.post(new Runnable() {
            @Override
            public void run() {
                addFooter();
            }
        });

        recyclerView.setRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        recyclerView.hideProgress();
                    }
                }, 3000);
            }
        });

        recyclerView.setOnMoreListener(new OnMoreListener() {
            @Override
            public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        recyclerView.hideMoreProgress();
                    }
                }, 3000);
            }
        });

        recyclerView.getSwipeToRefresh().setRefreshScrollListener(new PullRefreshLayout.OnRefreshScrollListener() {
            @Override
            public void onScroll(int totalY, int dy) {
                pullRefreshTotalDetalY = totalY;
                if (totalY == 0) {
                    stickyView.setTranslationY(getOriginOffset());
                    stickyView.setClickable(true);
                } else {
                    float translationY = stickyView.getTranslationY();
                    stickyView.setTranslationY(translationY + dy);
                    stickyView.setClickable(false);
                }
            }
        });

    }

    private void addFooter() {
        if (isFullScreen()) {
            return;
        }

        footer = new LinearLayout(getActivity());
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, getFooterHeight());
        footer.setLayoutParams(params);
        adapter.addFooterView(footer);
        adapter.notifyDataSetChanged();
    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            scrollTotalDeltaY += dy;

            //set sticky view's position
            int translationY = (getOriginOffset() - scrollTotalDeltaY) < 0 ? 0 : (getOriginOffset() - scrollTotalDeltaY);
            stickyView.setTranslationY(translationY + pullRefreshTotalDetalY);
        }


        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int scrollDistance = getHeaderHeight() - stickyView.getHeight() - scrollTotalDeltaY;
            if (scrollDistance <= 0) {
                return;
            }

            int duration = computeScrollDuration(0, scrollDistance, 0, 0);
            recyclerView.getRecyclerView().smoothScrollBy(0, scrollDistance);
            stickyView.animate().translationY(0).setInterpolator(sQuinticInterpolator).setDuration(duration).start();
        }
    };

    private List<String> getData() {
        List<String> data = new ArrayList<>();

        for (int i = 1; i < count + 1; i++) {
            data.add("测试数据" + i);
        }

        return data;
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private List<String> list;

        public MyAdapter(List<String> list) {
            this.list = list;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_expandable_list_item_1, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
            myViewHolder.textView.setText(list.get(i));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

    }

    private class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    private int getOriginOffset() {
        return dpToPx(100);
    }

    private int getActionbarSize() {
        return dpToPx(56);
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (dp * displayMetrics.density + 0.5);
    }

    private boolean isFullScreen() {
        return getEstimateHeight() > getOneScreenScrollHeight();
    }

    private int getEstimateHeight() {
        int totalHeight = getHeaderHeight();
        int itemCount = adapter.getItemCount();
        for (int i = 1; i < itemCount; i++) {
            View child = recyclerView.getRecyclerView().getChildAt(i);
            if (child != null) {
                totalHeight += child.getHeight();
            } else {
                totalHeight += recyclerView.getRecyclerView().getChildAt(1).getHeight();
            }
        }
        return totalHeight;
    }

    private int getOneScreenScrollHeight() {
        return getScreenHeight() + getHeaderHeight() - stickyView.getHeight() - getActionbarSize();
    }

    private int getHeaderHeight() {
        return header.getHeight();
    }

    private int getFooterHeight() {
        return getOneScreenScrollHeight() - getEstimateHeight();
    }

    private int getScreenHeight() {
        return getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();
    }

    private int computeScrollDuration(int dx, int dy, int vx, int vy) {
        final int absDx = Math.abs(dx);
        final int absDy = Math.abs(dy);
        final boolean horizontal = absDx > absDy;
        final int velocity = (int) Math.sqrt(vx * vx + vy * vy);
        final int delta = (int) Math.sqrt(dx * dx + dy * dy);
        final int containerSize = horizontal ? recyclerView.getWidth() : recyclerView.getHeight();
        final int halfContainerSize = containerSize / 2;
        final float distanceRatio = Math.min(1.f, 1.f * delta / containerSize);
        final float distance = halfContainerSize + halfContainerSize *
                distanceInfluenceForSnapDuration(distanceRatio);

        final int duration;
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            float absDelta = (float) (horizontal ? absDx : absDy);
            duration = (int) (((absDelta / containerSize) + 1) * 300);
        }
        return Math.min(duration, 2000);
    }

    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    private static final Interpolator sQuinticInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

}
