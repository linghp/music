package com.hfopenmusic.sdk.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.hfopen.sdk.entity.MusicList
import com.hfopen.sdk.entity.MusicRecord
import com.hfopen.sdk.entity.Record
import com.hfopen.sdk.entity.Tag
import com.hfopen.sdk.hInterface.DataResponse
import com.hfopen.sdk.manager.HFOpenApi
import com.hfopen.sdk.rx.BaseException
import com.hfopenmusic.sdk.HFOpenMusic
import com.hfopenmusic.sdk.R
import com.hfopenmusic.sdk.adapter.HifiveMusicSheetListAdapter
import com.hfopenmusic.sdk.util.GlideBlurTransformation
import com.hfopenmusic.sdk.util.HifiveDisplayUtils
import com.hfopenmusic.sdk.util.RoundedCornersTransform
import com.hfopenmusic.sdk.view.HifiveLoadMoreFooter
import com.hfopenmusic.sdk.view.HifiveRefreshHeader
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import java.util.*


/**
 * 音乐歌单详情的弹窗
 *
 * @author huchao
 */
class HifiveMusicChannelSheetDetailFragment : DialogFragment() {
    var isRefresh = false
    var isLoadMore = false
    private var sheetModel: Record? = null
    private var sheetId: Long = 0
    private var ivImage: ImageView? = null
    private var rootImage: ImageView? = null
    private var tvName: TextView? = null
    private var tvIntroduce: TextView? = null
    private var tvTips: TextView? = null
    private var mRecyclerView: RecyclerView? = null
    private lateinit var refreshLayout: SmartRefreshLayout
    private var adapter: HifiveMusicSheetListAdapter? = null
    private var musicModels: List<MusicRecord>? = null
    private var mContext: Context? = null
    private var page = 1
    private var totalCount = 0
    private var mHandler: Handler? = Handler { msg ->
        try {
            when (msg.what) {
                Refresh -> {
                    isRefresh = false
                    refreshLayout.finishRefresh()
                    if (musicModels != null && musicModels!!.isNotEmpty()) {
                        adapter!!.addHeaderView(R.layout.hifive_header_playall)
                        adapter!!.updateDatas(musicModels)
                    }
                    refreshLayout.setEnableLoadMore(adapter!!.datas.size < totalCount)
                }
                RequstFail -> refreshLayout.finishRefresh()
                LoadMore -> {
                    isLoadMore = false
                    if (musicModels != null) adapter!!.addDatas(musicModels)
                    if (adapter!!.datas.size < totalCount) {
                        refreshLayout.finishLoadMore()
                    } else {
                        refreshLayout.finishLoadMoreWithNoMoreData()
                    }
                }
                UPDATE_CURRENT_SONG -> {
                    val hifiveMusicModel = msg.obj as MusicRecord
                    HFOpenMusic.getInstance().addCurrentSingle(hifiveMusicModel)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        false
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val window = dialog!!.window
            if (window != null && mContext != null) {
                val params = window.attributes
                params.gravity = Gravity.BOTTOM
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.height = HifiveDisplayUtils.getPlayerHeight(mContext)
                window.attributes = params
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.HifiveSdkDialogStyly)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContext = context
        if (dialog != null) {
            val window = dialog!!.window
            if (window != null) {
                window.setWindowAnimations(R.style.AnimationRightFade)
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
            }
        }
        val view = inflater.inflate(R.layout.hifive_dialog_music_sheet_detail, container)
        if (arguments != null) sheetModel = arguments!!.getSerializable(MODEL) as Record?
        initView(view)
        updateSheetView()
        ininReclyView()
        refreshLayout.autoRefresh()
        HFOpenMusic.getInstance().addDialog(this)
        return view
    }

    //初始化view
    private fun initView(view: View) {
        view.findViewById<View>(R.id.iv_back).setOnClickListener {
            dismiss()
            HFOpenMusic.getInstance().removeDialog(2)
        }
        ivImage = view.findViewById(R.id.iv_image)
        rootImage = view.findViewById(R.id.root_image)
        tvName = view.findViewById(R.id.tv_name)
        tvIntroduce = view.findViewById(R.id.tv_introduce)
        tvTips = view.findViewById(R.id.tv_tips)
        refreshLayout = view.findViewById(R.id.refreshLayout)
        refreshLayout.setEnableLoadMore(true)
        refreshLayout.setRefreshHeader(HifiveRefreshHeader(context))
        refreshLayout.setRefreshFooter(HifiveLoadMoreFooter(context))
        mRecyclerView = view.findViewById(R.id.rv_music)
    }

    //初始化ReclyView
    private fun ininReclyView() {
        adapter = HifiveMusicSheetListAdapter(context, ArrayList())
        adapter!!.setOnRecyclerViewContentClick { position: Int ->
            mHandler!!.removeMessages(HifiveMusicPalyListFragment.UPDATE_CURRENT_SONG)
            val hifiveMusicModel = adapter!!.datas[position] as MusicRecord
            val message = mHandler!!.obtainMessage()
            message.obj = hifiveMusicModel
            message.what = HifiveMusicPalyListFragment.UPDATE_CURRENT_SONG
            mHandler!!.sendMessageDelayed(message, 200)
        }
        adapter!!.setOnRecyclerViewHeaderClick {
            HFOpenMusic.getInstance().updateCurrentList(adapter!!.datas as List<MusicRecord>)
            //加入播放列表成功
            HFOpenMusic.getInstance().showToast(activity, "加入播放列表成功")
        }
        mRecyclerView!!.adapter = adapter
        mRecyclerView!!.layoutManager = LinearLayoutManager(context)
        refreshLayout.setOnRefreshListener {
            if (!isRefresh) {
                page = 1
                isRefresh = true
                getData(Refresh)
            }
        }
        refreshLayout.setOnLoadMoreListener {
            if (!isLoadMore) {
                page++
                isLoadMore = true
                getData(LoadMore)
            }
        }
    }

    //更新ui
    private fun updateSheetView() {
        if (sheetModel != null && mContext != null) {
            sheetId = sheetModel!!.sheetId.toLong()
            if (sheetModel!!.cover != null && !TextUtils.isEmpty(sheetModel!!.cover!![0].url)) {

                val transformation = RoundedCornersTransform(context, HifiveDisplayUtils.dip2px(mContext, 20f).toFloat())
                //只是绘制左上角和右上角圆角
                transformation.setNeedCorner(true, true, false, false)

                val options = RequestOptions()
                        .placeholder(R.drawable.hifive_dialog_bg)
                        .error(R.drawable.hifive_dialog_bg)
                        .transform(MultiTransformation(GlideBlurTransformation(mContext), CenterCrop(), transformation))

                Glide.with(mContext!!).asBitmap().load(sheetModel!!.cover!![0].url)
                        .skipMemoryCache(true)
                        .apply(options)
                        .into(rootImage!!)

                Glide.with(mContext!!).load(sheetModel!!.cover!![0].url)
                        .error(R.mipmap.hifvesdk_sheet_default)
                        .placeholder(R.mipmap.hifvesdk_sheet_default)
                        .into(ivImage!!) //四周都是圆角的圆角矩形图片。
            } else {
                Glide.with(mContext!!).load(R.mipmap.hifvesdk_sheet_default)
                        .into(ivImage!!) //四周都是圆角的圆角矩形图片。
            }
            tvName!!.text = sheetModel!!.sheetName
            tvIntroduce!!.text = sheetModel!!.describe
            val tip = StringBuilder()
            getTipsText(tip, sheetModel!!.tag)
            if (tip.isNotEmpty()) {
                tvTips!!.visibility = View.VISIBLE
                tvTips!!.text = tip.toString()
            } else {
                tvTips!!.visibility = View.GONE
            }
        }
    }

    //获取标签名称
    private fun getTipsText(tip: StringBuilder, tag: List<Tag>?) {
        if (tag != null && tag.isNotEmpty()) {
            for ((child, _, tagName) in tag) {
                if (tip.isNotEmpty()) {
                    tip.append(",")
                }
                tip.append(tagName)
                getTipsText(tip, child)
            }
        }
    }

    private var toastStyle: Toast? = null

    //显示自定义toast信息
    private fun showToast(msgId: Int) {
        if (mContext != null) {
            if (toastStyle != null) {
                toastStyle!!.cancel()
                toastStyle = null
            }
            toastStyle = Toast(mContext)
            val layout = View.inflate(mContext, R.layout.hifive_layout_toast, null)
            val toastTextview = layout.findViewById<TextView>(R.id.tv_content)
            toastTextview.text = mContext!!.getString(msgId)
            toastStyle!!.view = layout
            toastStyle!!.setGravity(Gravity.CENTER, 0, 0)
            toastStyle!!.duration = Toast.LENGTH_SHORT
            toastStyle!!.show()
        }
    }

    //根据歌单id获取歌曲信息
    private fun getData(ty: Int) {
        try {
            if (mContext == null) return
            HFOpenApi.getInstance().sheetMusic(sheetId, 0, page, 20, object : DataResponse<MusicList> {
                override fun onError(exception: BaseException) {
                    if (ty != Refresh) { //上拉加载请求失败后，还原页卡
                        page--
                    }
                    HFOpenMusic.getInstance().showToast(activity, exception.msg)
                    mHandler!!.sendEmptyMessage(RequstFail)
                }

                override fun onSuccess(data: MusicList, taskId: String) {
                    musicModels = data.record
                    totalCount = data.meta.totalCount
                    if (mHandler == null) return
                    mHandler!!.sendEmptyMessage(ty)
                }
            })
        } catch (e: Exception) {
        }
    }

    override fun dismiss() {
        mHandler!!.removeCallbacksAndMessages(null)
        mHandler = null
        super.dismissAllowingStateLoss()
    }

    companion object {
        const val MODEL = "sheet_model"
        const val Refresh = 11 //刷新
        const val LoadMore = 12 //加载
        const val RequstFail = 15 //请求失败
        const val AddLike = 16 //添加喜欢的回调
        const val Addkaraoke = 17 //添加K歌的回调
        val UPDATE_CURRENT_SONG = 99 //切歌
    }
}