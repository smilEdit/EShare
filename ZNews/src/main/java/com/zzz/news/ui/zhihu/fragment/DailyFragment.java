package com.zzz.news.ui.zhihu.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.melnykov.fab.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.victor.loading.rotate.RotateLoading;
import com.zzz.news.R;
import com.zzz.news.app.App;
import com.zzz.news.base.BaseFragment;
import com.zzz.news.component.RxBus;
import com.zzz.news.model.bean.DailyBeforeListBean;
import com.zzz.news.model.bean.DailyListBean;
import com.zzz.news.model.db.RealmHelper;
import com.zzz.news.presenter.DailyPresenter;
import com.zzz.news.presenter.contract.DailyContract;
import com.zzz.news.ui.zhihu.activity.CalendarActivity;
import com.zzz.news.ui.zhihu.activity.ZhihuDetailActivity;
import com.zzz.news.ui.zhihu.adapter.DailyAdapter;
import com.zzz.news.util.ZCircularAnim;
import com.zzz.news.util.ZDate;
import com.zzz.news.util.ZToast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;



/**
 * @创建者 zlf
 * @创建时间 2016/9/20 14:56
 */
public class DailyFragment extends BaseFragment<DailyPresenter> implements DailyContract.View {
    @BindView(R.id.rl_daily_loading)
    RotateLoading        mRlDailyLoading;
    @BindView(R.id.rv_daily_list)
    RecyclerView         mRvDailyList;
    @BindView(R.id.fab_daily_calender)
    FloatingActionButton mFabDailyCalender;
    @BindView(R.id.srl_daily_refresh)
    SwipeRefreshLayout   mSrlDailyRefresh;

    RealmHelper  mRealmHelper;
    DailyAdapter mAdapter;
    String       mCurrentDate;

    List<DailyListBean.StoriesBean> mList = new ArrayList<>();

    @Override
    protected void initInject() {
        getFragmentComponent().inject(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_daily;
    }

    @Override
    protected void initEventAndData() {
        mRealmHelper = App.getAppComponent().realmHelper();
        mCurrentDate = ZDate.getTomorrowDate();
        mAdapter = new DailyAdapter(mContext, mList);
        mAdapter.setOnItemClickListener(new DailyAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(int position, View view) {
                mRealmHelper.inserNewsId(mList.get(position).getId());
                mAdapter.setReadState(position, true);
                if (mAdapter.getIsBefore()) {
                    mAdapter.notifyItemChanged(position + 1);
                } else {
                    mAdapter.notifyItemChanged(position + 2);
                }
                Intent intent = new Intent();
                intent.setClass(mContext, ZhihuDetailActivity.class);
                intent.putExtra("id", mList.get(position).getId());
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(mActivity, view, "shareView");
                mContext.startActivity(intent, options.toBundle());
            }
        });
        mSrlDailyRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRlDailyLoading.start();
                if (mCurrentDate.equals(ZDate.getTomorrowDate())) {
                    mPresenter.getDailyData();
                } else {
                    int year = Integer.valueOf(mCurrentDate.substring(0, 4));
                    int month = Integer.valueOf(mCurrentDate.substring(4, 6));
                    int day = Integer.valueOf(mCurrentDate.substring(6, 8));
                    CalendarDay date = CalendarDay.from(year, month, day);
                    RxBus.getDefault().post(date);
                    //                    mPresenter.getBeforeData(mCurrentDate);
                }
            }
        });
        mSrlDailyRefresh.setColorSchemeColors(Color.BLUE);
        mRvDailyList.setLayoutManager(new LinearLayoutManager(mContext));
        mRvDailyList.setAdapter(mAdapter);
        mRvDailyList.setVisibility(View.INVISIBLE);
        mFabDailyCalender.attachToRecyclerView(mRvDailyList);
        mRlDailyLoading.start();
        mPresenter.getDailyData();
    }

    @Override
    public void showContent(DailyListBean info) {
        mRlDailyLoading.stop();
        mRvDailyList.setVisibility(View.VISIBLE);
        mList = info.getStories();
        mAdapter.addDailyDate(info);
        mPresenter.startInterval();
    }

    @Override
    public void showMoreContent(String date, DailyBeforeListBean info) {
        mSrlDailyRefresh.setRefreshing(false);
        mPresenter.stopInterval();
        mList = info.getStories();
        mCurrentDate = String.valueOf(Integer.valueOf(info.getDate()));
        mRlDailyLoading.stop();
        mAdapter.addDailyBeforeDate(info);
    }

    @Override
    public void showError(String msg) {
        mRlDailyLoading.stop();
        ZToast.showShortToast(mContext, "数据加载失败");
    }

    @Override
    public void showProgress() {
        mRlDailyLoading.start();
    }

    @Override
    public void doInterval(int currentCount) {
        mAdapter.changeTopPager(currentCount);
    }

    @OnClick(R.id.fab_daily_calender)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_daily_calender:
                Intent intent = new Intent();
                intent.setClass(mContext, CalendarActivity.class);
                ZCircularAnim.startActivity(mActivity, intent, mFabDailyCalender, R.color.colorPrimary);
                break;
        }
    }
}
