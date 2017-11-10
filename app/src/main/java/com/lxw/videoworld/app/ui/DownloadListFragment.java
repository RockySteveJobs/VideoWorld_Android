package com.lxw.videoworld.app.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lxw.videoworld.R;
import com.lxw.videoworld.app.config.Constant;
import com.lxw.videoworld.app.service.DownloadManager;
import com.lxw.videoworld.app.widget.DownloadManagerDialog;
import com.lxw.videoworld.framework.util.ValueUtil;
import com.lxw.videoworld.framework.widget.NumberProgressBar;
import com.xunlei.downloadlib.parameter.XLTaskInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DownloadListFragment extends Fragment {

    Unbinder unbinder;
    @BindView(R.id.recyclerview_download_list)
    RecyclerView recyclerviewDownloadList;
    private View rootView;
    private String downloadType;
    private BaseQuickAdapter<XLTaskInfo, BaseViewHolder> downloadManagerAdapter;

    public DownloadListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            downloadType = getArguments().getString(Constant.DOWNLOAD_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null){
            rootView = inflater.inflate(R.layout.fragment_download_list, container, false);
            unbinder = ButterKnife.bind(this, rootView);
            initView();
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    private void initView() {
        downloadManagerAdapter = new BaseQuickAdapter<XLTaskInfo, BaseViewHolder>(R.layout.item_download_manager, null) {
            @Override
            protected void convert(BaseViewHolder helper, final XLTaskInfo item) {
                helper.setText(R.id.txt_download_type, item.mFileName.split("\\.")[item.mFileName.split("\\.").length - 1]);
                helper.setText(R.id.txt_download_title, item.mFileName);
                if (item.mFileSize > 0) {
                    ((NumberProgressBar) helper.getView(R.id.txt_download_progress)).setProgress((int) Math.floor(item.mDownloadSize * 100 / item.mFileSize));
                } else {
                    ((NumberProgressBar) helper.getView(R.id.txt_download_progress)).setProgress(0);
                }
                setDownloadViewWithStatus(helper, item);
            }
        };
        recyclerviewDownloadList.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerviewDownloadList.setAdapter(downloadManagerAdapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            List<XLTaskInfo> downloadingList = new ArrayList<>();
            List<XLTaskInfo> completeList = new ArrayList<>();
            if (DownloadManager.xLTaskInfos != null) {
                for (int i = 0; i < DownloadManager.xLTaskInfos.size(); i++){
                    XLTaskInfo xlTaskInfo = DownloadManager.xLTaskInfos.get(i);
                    if (xlTaskInfo.mTaskStatus == 2 || (xlTaskInfo.mFileSize != 0 && xlTaskInfo.mFileSize == xlTaskInfo.mDownloadSize)){
                        completeList.add(xlTaskInfo);
                    } else downloadingList.add(xlTaskInfo);
                }
            }
            if (downloadType != null){
                switch (downloadType){
                    case Constant.DOWNLOAD_TYPE_ALL:
                        refreshData(DownloadManager.xLTaskInfos);
                        break;
                    case Constant.DOWNLOAD_TYPE_DOWNLOADING:
                        refreshData(downloadingList);
                        break;
                    case Constant.DOWNLOAD_TYPE_COMPLETE:
                        refreshData(completeList);
                        break;
                }
            }
        }
    }

    public void refreshData(List<XLTaskInfo> datas){
        if (downloadManagerAdapter != null) downloadManagerAdapter.setNewData(datas);
    }

    private void setDownloadViewWithStatus(BaseViewHolder helper, final XLTaskInfo xlTaskInfo) {
        final CardView layout = (CardView) helper.getView(R.id.cardview_download_item);
        final ImageView statusIcon = (ImageView) helper.getView(R.id.img_start_pause);
        switch (xlTaskInfo.mTaskStatus) {
            case 0:
                if (xlTaskInfo.mDownloadSize == xlTaskInfo.mFileSize && xlTaskInfo.mFileSize > 0) {// 已完成
                    helper.setText(R.id.txt_download_info, ValueUtil.formatFileSize(xlTaskInfo.mFileSize));
                    statusIcon.setImageResource(R.drawable.ic_complete);
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DownloadManagerDialog dialog = new DownloadManagerDialog(getActivity(), xlTaskInfo);
                            dialog.show();
                        }
                    };
                    statusIcon.setOnClickListener(listener);
                    layout.setOnClickListener(listener);
                } else {// 连接中
                    helper.setText(R.id.txt_download_info, ValueUtil.formatFileSize(xlTaskInfo.mDownloadSpeed) + "/s" + "\n" +
                            ValueUtil.formatFileSize(xlTaskInfo.mDownloadSize) + "\n" + ValueUtil.formatFileSize(xlTaskInfo.mFileSize));
                    statusIcon.setImageResource(R.drawable.ic_connect);
                    statusIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DownloadManager.stopTask(xlTaskInfo.mTaskId);
                            DownloadManager.startTask(getActivity(), xlTaskInfo);
                        }
                    });
                    layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DownloadManagerDialog dialog = new DownloadManagerDialog(getActivity(), xlTaskInfo);
                            dialog.show();
                        }
                    });
                }
                break;
            case 1:
                helper.setText(R.id.txt_download_info, ValueUtil.formatFileSize(xlTaskInfo.mDownloadSpeed) + "/s" + "\n" +
                        ValueUtil.formatFileSize(xlTaskInfo.mDownloadSize) + "\n" + ValueUtil.formatFileSize(xlTaskInfo.mFileSize));
                statusIcon.setImageResource(R.drawable.ic_pause);
                statusIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DownloadManager.stopTask(xlTaskInfo.mTaskId);
                        statusIcon.setImageResource(R.drawable.ic_start);
                    }
                });
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DownloadManagerDialog dialog = new DownloadManagerDialog(getActivity(), xlTaskInfo);
                        dialog.show();
                    }
                });
                break;
            case 2:
                helper.setText(R.id.txt_download_info, ValueUtil.formatFileSize(xlTaskInfo.mFileSize));
                statusIcon.setImageResource(R.drawable.ic_complete);
                View.OnClickListener listener1 = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DownloadManagerDialog dialog = new DownloadManagerDialog(getActivity(), xlTaskInfo);
                        dialog.show();
                    }
                };
                statusIcon.setOnClickListener(listener1);
                layout.setOnClickListener(listener1);
                break;
            case 3:
                helper.setText(R.id.txt_download_info, ValueUtil.formatFileSize(xlTaskInfo.mDownloadSize) + "\n" + ValueUtil.formatFileSize(xlTaskInfo.mFileSize));
                statusIcon.setImageResource(R.drawable.ic_error);
                statusIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DownloadManager.stopTask(xlTaskInfo.mTaskId);
                        DownloadManager.startTask(getActivity(), xlTaskInfo);
                        statusIcon.setImageResource(R.drawable.ic_connect);
                    }
                });
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DownloadManagerDialog dialog = new DownloadManagerDialog(getActivity(), xlTaskInfo);
                        dialog.show();
                    }
                });
                break;
            case 4:
                helper.setText(R.id.txt_download_info, ValueUtil.formatFileSize(xlTaskInfo.mDownloadSize) + "\n" + ValueUtil.formatFileSize(xlTaskInfo.mFileSize));
                statusIcon.setImageResource(R.drawable.ic_start);
                statusIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DownloadManager.startTask(getActivity(), xlTaskInfo);
                        statusIcon.setImageResource(R.drawable.ic_pause);
                    }
                });
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DownloadManagerDialog dialog = new DownloadManagerDialog(getActivity(), xlTaskInfo);
                        dialog.show();
                    }
                });
                break;
            default:
                break;
        }
    }

    public void setDownloadType(String downloadType) {
        this.downloadType = downloadType;
    }
}
