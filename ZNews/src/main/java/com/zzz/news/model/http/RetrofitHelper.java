package com.zzz.news.model.http;

import com.zzz.news.BuildConfig;
import com.zzz.news.app.Constants;
import com.zzz.news.model.bean.CommentBean;
import com.zzz.news.model.bean.DailyBeforeListBean;
import com.zzz.news.model.bean.DailyListBean;
import com.zzz.news.model.bean.DetailExtraBean;
import com.zzz.news.model.bean.GankItemBean;
import com.zzz.news.model.bean.HotListBean;
import com.zzz.news.model.bean.JokeBean;
import com.zzz.news.model.bean.LishiBean;
import com.zzz.news.model.bean.RobotBean;
import com.zzz.news.model.bean.SectionChildListBean;
import com.zzz.news.model.bean.SectionListBean;
import com.zzz.news.model.bean.ThemeChildListBean;
import com.zzz.news.model.bean.ThemeListBean;
import com.zzz.news.model.bean.TopNewsBean;
import com.zzz.news.model.bean.WeixinBean;
import com.zzz.news.model.bean.ZhihuDetailBean;
import com.zzz.news.util.ZSystem;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * @创建者 zlf
 * @创建时间 2016/9/18 15:50
 */
public class RetrofitHelper {

    private static OkHttpClient sOkHttpClient   = null;
    private static ZhihuApis    zhihuApiService = null;
    private static GankApis     sGankApis       = null;
    private static JuHeApis     sJuHeApis       = null;
    private static JuHeApis     sJuheApis       = null;
    private static JuHeApis     sJuheRApis      = null;
    private static JuHeApis     sJuHeLApis      = null;

    public RetrofitHelper() {
        init();
    }

    private void init() {
        initOkHttp();
        zhihuApiService = getZhihuApiService();
        sGankApis = getGankApisService();
        sJuHeApis = getJuHeApisService();
        sJuheApis = getJuHeJokeApisService();
        sJuheRApis = getJuheRobotApisService();
        sJuHeLApis = getJuheLishiApisService();
    }

    private void initOkHttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            builder.addInterceptor(loggingInterceptor);
        }
        File cacheFile = new File(Constants.PATH_CACHE);
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 50);
        Interceptor cacheInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                if (!ZSystem.isNetworkConnected()) {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }
                Response response = chain.proceed(request);
                if (ZSystem.isNetworkConnected()) {
                    int maxAge = 0;
                    // 有网络时, 不缓存, 最大保存时长为0
                    response.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .removeHeader("Pragma")
                            .build();
                } else {
                    // 无网络时，设置超时为4周
                    int maxStale = 60 * 60 * 24 * 28;
                    response.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .removeHeader("Pragma")
                            .build();
                }
                return response;
            }
        };
        builder.cache(cache).addInterceptor(cacheInterceptor);
        //设置超时
        builder.connectTimeout(15, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(true);
        sOkHttpClient = builder.build();
    }

    //知乎数据获取
    private static ZhihuApis getZhihuApiService() {
        Retrofit zhihuRetrofit = new Retrofit.Builder()
                .baseUrl(ZhihuApis.HOST)
                .client(sOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return zhihuRetrofit.create(ZhihuApis.class);
    }

    public Observable<DailyListBean> fetchDailyListInfo() {
        return zhihuApiService.getDailyList();
    }

    public Observable<DailyBeforeListBean> fetchDailyBeforeListInfo(String date) {
        return zhihuApiService.getDailyBeforeList(date);
    }

    public Observable<ThemeListBean> fetchThemeListBean() {
        return zhihuApiService.getThemeList();
    }

    public Observable<DetailExtraBean> fetchDetailExtraBean(int id) {
        return zhihuApiService.getDetailExtraInfo(id);
    }

    public Observable<ZhihuDetailBean> fetchDetailInfo(int id) {
        return zhihuApiService.getDetailInfo(id);
    }

//    public Observable<WelcomeBean> fetchWelcomeBean() {
//        return sGankApis.getFuliList(1);
//    }

    public Observable<CommentBean> fetchLongCommentBean(int id) {
        return zhihuApiService.getLongCommentInfo(id);
    }

    public Observable<CommentBean> fetchShortCommentBean(int id) {
        return zhihuApiService.getShortCommentInfo(id);
    }

    public Observable<HotListBean> fetchHotList() {
        return zhihuApiService.getHotList();
    }

    public Observable<SectionListBean> fetchSectionList() {
        return zhihuApiService.getSectionList();
    }

    public Observable<SectionChildListBean> fetchSectionChildList(int id) {
        return zhihuApiService.getSectionChildList(id);
    }

    public Observable<ThemeChildListBean> fetchThemeChildList(int id) {
        return zhihuApiService.getThemeChildList(id);
    }

    //gank数据获取
    public static GankApis getGankApisService() {
        Retrofit gankRetrofit = new Retrofit.Builder()
                .baseUrl(GankApis.HOST)
                .client(sOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return gankRetrofit.create(GankApis.class);
    }

    public Observable<HttpResponse<List<GankItemBean>>> fetchFuliImage(int num) {
        return sGankApis.getFuliList(num);
    }

    public Observable<HttpResponse<List<GankItemBean>>> fetchGirlList(int num, int page) {
        return sGankApis.getGirlList(num, page);
    }

    public Observable<HttpResponse<List<GankItemBean>>> fetchTechList(String tech, int num, int page) {
        return sGankApis.getTechList(tech, num, page);
    }

    //集合数据获取
    public static JuHeApis getJuHeApisService() {
        Retrofit juheRetrofit = new Retrofit.Builder()
                .baseUrl(JuHeApis.HOST)
                .client(sOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return juheRetrofit.create(JuHeApis.class);
    }

    public Observable<JuHeHttpResponse<TopNewsBean.ResultBean>> fetchTopNewsList() {
        return sJuHeApis.getTopNewsList();
    }

    public Observable<JuHeHttpResponse<WeixinBean.ResultBean>> fetchWeixinList(int ps, int pno) {
        return sJuHeApis.getWeixinList(Constants.JUHE_WEIXIN_KEY, ps, pno);
    }

    //坑爹聚合 数据
    public static JuHeApis getJuHeJokeApisService() {
        Retrofit juheRetrofit = new Retrofit.Builder()
                .baseUrl(JuHeApis.HOST_JOKE)
                .client(sOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return juheRetrofit.create(JuHeApis.class);
    }

    //getJokeList(@Query("key")String key,@Query("page")int page,
    //@Query("pagesize") int pagesize, @Query("sort") String sort, @Query("time") String time);
    public Observable<JuHeHttpResponse<JokeBean.ResultBean>> fetchJokeBean(int page, int pagesize, String sort, String time) {
        return sJuheApis.getJokeList(Constants.JUHE_JOKE_KEY, page, pagesize, sort, time);
    }

    //机器人聚合 数据
    public static JuHeApis getJuheRobotApisService() {
        Retrofit juheRetrofit = new Retrofit.Builder()
                .baseUrl(JuHeApis.HOST_ROBOT)
                .client(sOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return juheRetrofit.create(JuHeApis.class);
    }

    public Observable<JuHeHttpResponse<RobotBean.ResultBean>> fetchRobotAnswer(String info) {
        return sJuheRApis.getRobotInfo(info, Constants.JUHE_ROBOT_KEY);
    }

    //历史上的今天 数据
    public static JuHeApis getJuheLishiApisService() {
        Retrofit juheRetrofit = new Retrofit.Builder()
                .baseUrl(JuHeApis.HOST_LISHI)
                .client(sOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return juheRetrofit.create(JuHeApis.class);
    }

    public Observable<JuHeHttpResponse<List<LishiBean.ResultBean>>> fetchLishiInfo(int month, int day) {
        return sJuHeLApis.getLishiInfo(Constants.JUHE_LISHI_KEY, "1.0", month, day);
    }
}
