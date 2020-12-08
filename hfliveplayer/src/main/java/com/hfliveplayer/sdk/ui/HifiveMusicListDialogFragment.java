package com.hfliveplayer.sdk.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.hfliveplayer.sdk.R;
import com.hfliveplayer.sdk.adapter.HifiveViewPagerAdapter;
import com.hfliveplayer.sdk.listener.HifiveAddMusicListener;
import com.hfliveplayer.sdk.model.HifiveMusicUserSheetModel;
import com.hfliveplayer.sdk.util.GsonUtils;
import com.hfliveplayer.sdk.util.HifiveDialogManageUtil;
import com.hfliveplayer.sdk.util.HifiveDisplayUtils;
import com.hfliveplayer.sdk.view.magicindicator.CommonNavigator;
import com.hfliveplayer.sdk.view.magicindicator.CommonNavigatorAdapter;
import com.hfliveplayer.sdk.view.magicindicator.CommonPagerTitleView;
import com.hfliveplayer.sdk.view.magicindicator.MagicIndicator;
import com.hfliveplayer.sdk.view.magicindicator.ViewPagerHelper;
import com.hfliveplayer.sdk.view.magicindicator.abs.IPagerIndicator;
import com.hfliveplayer.sdk.view.magicindicator.abs.IPagerTitleView;
import com.hifive.sdk.hInterface.DataResponse;
import com.hifive.sdk.manager.HFLiveApi;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


/**
 * 音乐列表的弹窗
 * @author huchao
 */
public class HifiveMusicListDialogFragment extends DialogFragment implements HifiveAddMusicListener {
    private MagicIndicator magicIndicator;
    private ViewPager viewPager;
    private Context mContext;
    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!= null) {
            Window window = getDialog().getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.gravity = Gravity.BOTTOM;
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = HifiveDisplayUtils.getPlayerHeight(mContext);
                window.setAttributes(params);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.HifiveSdkDialogStyly);
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mContext = this.getContext();
        if(getDialog()!= null) {
            Window window = getDialog().getWindow();
            if (window != null) {
                window.setWindowAnimations(R.style.AnimationBottomFade);
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
            }
        }
        View view = inflater.inflate(R.layout.hifive_dialog_music_list, container);
        if(HifiveDialogManageUtil.getInstance().updateObservable == null){
            HifiveDialogManageUtil.getInstance().updateObservable = new HifiveUpdateObservable();
        }
        initView(view);
        if(HifiveDialogManageUtil.getInstance().getUserSheetModels() != null
                && HifiveDialogManageUtil.getInstance().getUserSheetModels().size() >0){
            initMagicIndicator();
            initPage();
        }else{
            getData();
        }
        HifiveDialogManageUtil.getInstance().addDialog(this);
        return view;
    }
    //初始化view
    private void initView(View view) {
        view.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                HifiveDialogManageUtil.getInstance().removeDialog(0);
            }
        });
        view.findViewById(R.id.iv_sheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HifiveMusicSheetDialogFragment dialogFragment = new HifiveMusicSheetDialogFragment();
                if(getFragmentManager() != null)
                    dialogFragment.show(getFragmentManager(), HifiveMusicListDialogFragment.class.getSimpleName());
            }
        });
        view.findViewById(R.id.iv_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HifiveMusicSearchDialoglFragment dialogFragment = new HifiveMusicSearchDialoglFragment();
                if(getFragmentManager() != null)
                    dialogFragment.show(getFragmentManager(), HifiveMusicSearchDialoglFragment.class.getSimpleName());
            }
        });
        magicIndicator = view.findViewById(R.id.magic_indicator);
        viewPager = view.findViewById(R.id.viewpager);
    }
    //获取用户歌单列表
    private void getData() {
        if (HFLiveApi.Companion.getInstance() == null || mContext == null)
            return;
        HFLiveApi.Companion.getInstance().getMemberSheetList(mContext, "1", "10", new DataResponse() {
            @Override
            public void errorMsg(@NotNull String string, @org.jetbrains.annotations.Nullable Integer code) {
                HifiveDialogManageUtil.getInstance().showToast(getActivity(),string);
            }

            @Override
            public void data(@NotNull Object any) {
                Log.e("TAG", "我的歌单==" + any);

                List<HifiveMusicUserSheetModel> sheetModels  = GsonUtils.getRecords(String.valueOf(any), HifiveMusicUserSheetModel.class);
                HifiveDialogManageUtil.getInstance().setUserSheetModels(sheetModels);
                initMagicIndicator();
                initPage();
            }
        });
    }
    //初始化指示器
    private void initMagicIndicator() {
        final String[] titleContent = new String[]{
                mContext.getString(R.string.hifivesdk_music_current_play),
                mContext.getString(R.string.hifivesdk_music_like),
                mContext.getString(R.string.hifivesdk_music_karaoke)};
        CommonNavigator commonNavigator = new CommonNavigator(mContext);
        commonNavigator.setAdjustMode(false);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return titleContent.length;
            }

            @Override
            public IPagerTitleView getTitleView(final Context context, final int index) {
                final CommonPagerTitleView cptv = new CommonPagerTitleView(context);
                cptv.setContentView(R.layout.hifive_layout_indecator);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                cptv.setLayoutParams(lp);
                // 初始化
                final TextView titleText = cptv.findViewById(R.id.tv_indicator);
                final View vvDown = cptv.findViewById(R.id.vv_line);
                if(!TextUtils.isEmpty(titleContent[index]))
                    titleText.setText(titleContent[index]);
                cptv.setOnPagerTitleChangeListener(new CommonPagerTitleView.OnPagerTitleChangeListener() {
                    @Override
                    public void onSelected(int index, int totalCount) {
                        titleText.setTextColor(Color.parseColor("#FF000000"));
                        titleText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        vvDown.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onDeselected(int index, int totalCount) {
                        titleText.setTextColor(Color.parseColor("#FFC7C7CC"));
                        titleText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        vvDown.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {

                        cptv.setScaleX(1.0f - (0.1f) * leavePercent);
                        cptv.setScaleY(1.0f - (0.1f) * leavePercent);
                    }

                    @Override
                    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
                        cptv.setScaleX(0.9f + (0.1f) * enterPercent);
                        cptv.setScaleY(0.9f + (0.1f) * enterPercent);
                    }
                });

                cptv.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                viewPager.setCurrentItem(index);
                                            }
                                        }
                );
                return cptv;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                return null;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(magicIndicator, viewPager);
    }
    //初始化viewpager页卡
    private void initPage() {
        List<Fragment> fragments = new ArrayList<>();
        //1.当前播放
        HifiveMusicPalyListFragment currentPalyListFragment = new HifiveMusicPalyListFragment();
        currentPalyListFragment.setAddMusicListener(this);
        //2.我喜欢
        HifiveMusicLikeListFragment likeListFragment = new HifiveMusicLikeListFragment();
        likeListFragment.setAddMusicListener(this);
        //3.K歌
        HifiveMusicKaraokeListFragment karaokeListFragment = new HifiveMusicKaraokeListFragment();
        karaokeListFragment.setAddMusicListener(this);
        //为被观察者添加观察者
        HifiveDialogManageUtil.getInstance().updateObservable.addObserver(currentPalyListFragment);
        HifiveDialogManageUtil.getInstance().updateObservable.addObserver(likeListFragment);
        HifiveDialogManageUtil.getInstance().updateObservable.addObserver(karaokeListFragment);
        fragments.add(currentPalyListFragment);
        fragments.add(likeListFragment);
        fragments.add(karaokeListFragment);
        if (!isAdded()) return;
        HifiveViewPagerAdapter adapter = new HifiveViewPagerAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
        int firstPageIndex = 0;
        viewPager.setCurrentItem(firstPageIndex);
    }

    @Override
    public void onAddMusic() {
        HifiveMusicSheetDialogFragment dialogFragment = new HifiveMusicSheetDialogFragment();
        if(getFragmentManager() != null)
            dialogFragment.show(getFragmentManager(), HifiveMusicListDialogFragment.class.getSimpleName());
    }
}
