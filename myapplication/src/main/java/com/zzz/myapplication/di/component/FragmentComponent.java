package com.zzz.myapplication.di.component;

import android.app.Activity;

import com.zzz.myapplication.di.FragmentScope;
import com.zzz.myapplication.di.module.FragmentModule;
import com.zzz.myapplication.ui.zhihu.fragment.CommentFragment;
import com.zzz.myapplication.ui.zhihu.fragment.DailyFragment;
import com.zzz.myapplication.ui.zhihu.fragment.HotFragment;
import com.zzz.myapplication.ui.zhihu.fragment.SectionFragment;
import com.zzz.myapplication.ui.zhihu.fragment.ThemeFragment;
import com.zzz.myapplication.ui.zhihu.fragment.ZhihuMainFragment;

import dagger.Component;

/**
 * @创建者 zlf
 * @创建时间 2016/9/19 14:11
 */

@FragmentScope
@Component(dependencies = AppComponent.class,modules = FragmentModule.class)
public interface FragmentComponent {

//    RetrofitHelper getRetrofitHelper();

    Activity getActivity();

    void inject(ZhihuMainFragment zhihuMainFragment);

    void inject(DailyFragment dailyFragment);

    void inject(ThemeFragment themeFragment);

    void inject(SectionFragment sectionFragment);

    void inject(HotFragment hotFragment);

    void inject(CommentFragment commentFragment);
}
