package com.jimome.mm.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.google.gson.Gson;
import com.jimome.mm.adapter.QuestionAdapter;
import com.jimome.mm.bean.BaseJson;
import com.jimome.mm.request.CacheRequest;
import com.jimome.mm.request.CacheRequestCallBack;
import com.jimome.mm.utils.ExitManager;
import com.jimome.mm.utils.NetworkUtils;
import com.jimome.mm.utils.PreferenceHelper;
import com.jimome.mm.utils.StringUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.unjiaoyou.mm.R;

/**
 * 常见问题页面
 * 
 * @author admin
 * 
 */
public class QuestionFragment extends BaseFragment {

	@ViewInject(R.id.tv_title)
	private TextView tv_title;
	@ViewInject(R.id.layout_back)
	private LinearLayout layout_back;
	@ViewInject(R.id.elv_question)
	// private ExpandableListView elv_question;
	private ListView lv_question;
	private LinearLayout contentLayout;
	// private ListView lv_question;
	// @ViewInject( R.id.img_loading_error)
	// private ImageView img_loading_error;
	// private Dialog mDialog;
	private QuestionAdapter questionAdapter;
	private BaseJson baseJson;
	private Activity context;

	@Override
	protected View initView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.fragment_question, container, false);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		if (getActivity() == null)
			context = activity;
		else
			context = getActivity();

	}

	private void initView() {
		// contentLayout = (LinearLayout) getActivity().getLayoutInflater()
		// .inflate(R.layout.layout_recommend, null);
		// lv_question = (ListView)
		// contentLayout.findViewById(R.id.lv_recommend);
		// pullScrollView.addBodyView(contentLayout);
		// pullScrollView.setheaderViewGone();
		// pullScrollView.setfooterViewGone();
		// pullScrollView.setOnPullListener(this);
		// lv_question.setOnItemClickListener(this);
	}

	private void waitDialog() {
		// mDialog = BasicUtils.showDialog(context, R.style.BasicDialog);
		// mDialog.setContentView(R.layout.dialog_wait);
		// mDialog.setCanceledOnTouchOutside(false);
		//
		// Animation anim = AnimationUtils.loadAnimation(context,
		// R.anim.dialog_prog);
		// LinearInterpolator lir = new LinearInterpolator();
		// anim.setInterpolator(lir);
		// mDialog.findViewById(R.id.img_dialog_progress).startAnimation(anim);
		// mDialog.setOnKeyListener(new OnKeyListener() {
		// @Override
		// public boolean onKey(DialogInterface dialog, int keyCode,
		// KeyEvent event) {
		// if (keyCode == KeyEvent.KEYCODE_BACK
		// && event.getAction() == KeyEvent.ACTION_DOWN) {
		// if (!context.isFinishing())
		// mDialog.dismiss();
		//
		// return false;
		// }
		// return false;
		// }
		// });
		mDialog.show();
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

	}

	private void getHttpData() {
		RequestParams params = new RequestParams();
		String key = "me/faq";
		params.addHeader("Authorization",
				PreferenceHelper.readString(context, "auth", "token"));
		CacheRequest.requestGET(context, key, params, key, 0,
				new CacheRequestCallBack() {

					@Override
					public void onData(String json) {
						// TODO Auto-generated method stub
						try {
							baseJson = new Gson()
									.fromJson(json, BaseJson.class);
							if (baseJson.getStatus().equals("200")) {
								questionAdapter = new QuestionAdapter(context,
										baseJson.getFaqs());
								lv_question.setAdapter(questionAdapter);
							} else {
								// img_loading_error.setVisibility(View.VISIBLE);
								// pullScrollView.setVisibility(View.GONE);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							if (mDialog != null) {
								mDialog.dismiss();
							}
						}
					}

					@Override
					public void onFail(HttpException e, String result,
							String json) {
						// TODO Auto-generated method stub
						if (mDialog != null) {
							mDialog.dismiss();
						}
						ExitManager.getScreenManager().intentLogin(context,
								e.getExceptionCode() + "");
					}
				});
	}

	@OnClick({ R.id.layout_back })
	public void onClickView(View v) {
		switch (v.getId()) {
		case R.id.layout_back:
			context.finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mDialog != null)
			mDialog.dismiss();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StatService.onResume(context);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		StatService.onPause(context);
	}

	@Override
	protected void initWidget() {
		// TODO Auto-generated method stub
		tv_title.setText(StringUtils.getResourse(R.string.str_FAQ));
		layout_back.setVisibility(View.VISIBLE);
		initView();
		if (!NetworkUtils.checkNet(context)) {
			// img_loading_error.setVisibility(View.VISIBLE);
		} else {
			// img_loading_error.setVisibility(View.GONE);
			waitDialog();
			getHttpData();
			// pullScrollView.setheaderViewGone();
			// pullScrollView.setfooterViewGone();
			// pullScrollView.setVisibility(View.VISIBLE);
		}
	}

	// @Override
	// public void refresh() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void loadMore() {
	// // TODO Auto-generated method stub
	// }
}
