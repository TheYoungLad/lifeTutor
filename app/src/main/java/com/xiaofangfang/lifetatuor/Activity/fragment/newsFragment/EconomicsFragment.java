package com.xiaofangfang.lifetatuor.Activity.fragment.newsFragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaofangfang.lifetatuor.Activity.fragment.newsFragment.parent.ParentFragment;
import com.xiaofangfang.lifetatuor.R;
import com.xiaofangfang.lifetatuor.dao.DbOpener;
import com.xiaofangfang.lifetatuor.model.news.Economics;
import com.xiaofangfang.lifetatuor.model.news.parent.CommonNews;
import com.xiaofangfang.lifetatuor.set.SettingStandard;
import com.xiaofangfang.lifetatuor.tools.GsonParseData;
import com.xiaofangfang.lifetatuor.tools.Looger;
import com.xiaofangfang.lifetatuor.tools.UiThread;

/**
 * 经济相关的新闻
 */
public class EconomicsFragment extends ParentFragment {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkUpdate(Economics.class.getSimpleName(),
                SettingStandard.News.NewsType.CAIJING);
    }


    @Override
    protected void progressResult(String responseData) {
        response = responseData;
        Economics economics = new Economics();
        CommonNews commonNews = GsonParseData.parseNews(responseData,
                economics);
        economics = new Economics(commonNews);
        onstartRunUi();
    }

    private void onstartRunUi() {

        UiThread.getUiThread(getContext()).post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup)
                inflater.inflate(R.layout.news_economics, container, false);

        initView(viewGroup);

        return viewGroup;
    }

    /**
     * 初始化里面的子view
     *
     * @param viewGroup
     */
    private void initView(ViewGroup viewGroup) {

    }

    /**
     * 在视图被销毁的时候保存网页的会话数据
     */
    @Override
    public void onDestroy() {
        DbOpener.saveInfo(Economics.class, getContext(), response);
        super.onDestroy();
        Looger.d("fragment被销毁");
    }

}
