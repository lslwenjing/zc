package com.jimome.mm.fragment;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.daimajia.swipe.implments.SwipeItemMangerImpl;
import com.google.gson.Gson;
import com.jimome.mm.activity.FragmentToActivity;
import com.jimome.mm.activity.TalkActivity;
import com.jimome.mm.adapter.InboxMainAdapter;
import com.jimome.mm.bean.BaseJson;
import com.jimome.mm.common.Conf;
import com.jimome.mm.request.CacheRequest;
import com.jimome.mm.request.CacheRequestCallBack;
import com.jimome.mm.utils.BasicUtils;
import com.jimome.mm.utils.ExitManager;
import com.jimome.mm.utils.PreferenceHelper;
import com.jimome.mm.view.MyListView;
import com.jimome.mm.view.PullScrollView;
import com.jimome.mm.view.PullScrollView.OnPullListener;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.unjiaoyou.mm.R;

/**
 * 私信页面
 * 
 * @author admin
 * 
 */
public class DynamicFragment extends BaseFragment implements OnPullListener,
		OnItemClickListener, OnClickListener {

	@ViewInject(R.id.tv_title)
	private TextView tv_title;
	@ViewInject(R.id.layout_back)
	private LinearLayout layout_back;
	@ViewInject(R.id.pushscroll_gv)
	private PullScrollView pullScrollView;
	private LinearLayout contentLayout;
	private InboxMainAdapter inboxMainAdapter;
	// private Dialog mDialog;
	private LinearLayout reply, request;
	private TextView reply_nums, request_nums;
	private BaseJson inbox;
	private MyListView lv_inbox;
	private int page = 1;
	private String dialog_id;
	private NotificationManager mNotificationManager;

	private Activity context;

	@Override
	protected View initView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		return arg0.inflate(R.layout.fragment_inbox, arg1, false);
	}

	@Override
	protected void initWidget() {
		// TODO Auto-generated method stub
		tv_title.setText(R.string.str_dynamic);
		layout_back.setVisibility(View.VISIBLE);

		contentLayout = (LinearLayout) context.getLayoutInflater().inflate(
				R.layout.layout_dynamic, null);

		reply = (LinearLayout) contentLayout.findViewById(R.id.layout_reply);
		request = (LinearLayout) contentLayout
				.findViewById(R.id.layout_request);

		reply_nums = (TextView) contentLayout.findViewById(R.id.tv_reply_nums);
		request_nums = (TextView) contentLayout
				.findViewById(R.id.tv_request_nums);

		lv_inbox = (MyListView) contentLayout.findViewById(R.id.lv_inbox);
		lv_inbox.setOnItemClickListener(this);

		reply.setOnClickListener(this);
		request.setOnClickListener(this);

		pullScrollView.addBodyView(contentLayout);
		pullScrollView.setOnPullListener(this);
		pullScrollView.setheaderViewReset();
		pullScrollView.setfooterViewReset();
		if (SelectFragment.sendMsg) {
			// if (SystemTool.checkNet(context)) {
			page = 1;
			waitDialog();
			getHttpData(1);
			// } else {
			// ViewInject.toast(getString(R.string.str_net_register));
			// pullScrollView.setheaderViewReset();
			// }
			SelectFragment.sendMsg = false;
		} else {
			if (inbox != null) {
				if (inbox.getComment_nums() != null
						&& !inbox.getComment_nums().trim().equals("0")) {
					reply_nums.setText(inbox.getComment_nums().trim());
					reply_nums.setVisibility(View.VISIBLE);
				} else {
					reply_nums.setVisibility(View.GONE);
				}
				if (inbox.getGreet_nums() != null
						&& !inbox.getGreet_nums().trim().equals("0")) {
					request_nums.setText(inbox.getGreet_nums().trim());
					request_nums.setVisibility(View.VISIBLE);
				} else {
					request_nums.setVisibility(View.GONE);
				}

				if (inboxMainAdapter != null) {
					lv_inbox.setAdapter(inboxMainAdapter);
					pullScrollView.post(new Runnable() {

						@Override
						public void run() {

							pullScrollView.scrollTo(0, 0);
						}
					});
				}
			} else {

				waitDialog();
				getHttpData(1);
			}
		}
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

	private void waitDialog() {
		mDialog.show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("jimo.action.delete");
		intentFilter.addAction("jimo.action.inboxrefresh");
		context.registerReceiver(mRefreshBroadcastReceiver, intentFilter);

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		// TODO Auto-generated method stub
		try {
			if (inboxMainAdapter != null
					&& !inboxMainAdapter.getAllData().get(pos).getMsg_nums()
							.equals("0")) {
//				Intent intent = new Intent();
//				intent.putExtra("inboxBadge", inboxMainAdapter.getAllData()
//						.get(pos).getMsg_nums());
//				intent.putExtra("type", 1);
//				intent.setAction("com.action.inboxBadge");
//				context.sendBroadcast(intent);
			}

			inboxMainAdapter.refresh(pos);
			inboxMainAdapter.notifyDataSetChanged();
			Intent chatIntent = new Intent(context, TalkActivity.class);
			chatIntent.putExtra("person", inboxMainAdapter.getAllData()
					.get(pos));
			context.startActivity(chatIntent);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 得到用户地址后启动的广播
	private BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			try {

				if (action.equals("jimo.action.delete")) {
					String nums = inboxMainAdapter.getAllData()
							.get(intent.getExtras().getInt("pos"))
							.getMsg_nums();
					dialog_id = inboxMainAdapter.getAllData()
							.get(intent.getExtras().getInt("pos"))
							.getDialog_id();

//					if (nums.equals("1")) {
//						Intent delIntent = new Intent();
//						delIntent.putExtra("inboxBadge", "1");
//						delIntent.putExtra("type", 1);
//						delIntent.setAction("com.action.inboxBadge");
//						context.sendBroadcast(delIntent);
//					}

					inboxMainAdapter.remove(intent.getIntExtra("pos", 0));
					inboxMainAdapter.notifyDataSetChanged();
					getHttpData(3);

				} else if (action.equals("jimo.action.inboxrefresh")) {
					page = 1;
					getHttpData(1);
					SelectFragment.sendMsg = false;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mDialog != null)
			mDialog.dismiss();
		if (mRefreshBroadcastReceiver != null)
			context.unregisterReceiver(mRefreshBroadcastReceiver);
	}

	private void setView() {
		// TODO Auto-generated method stub
		try {
			mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(1);

			if (!inbox.getComment_nums().trim().equals("0")) {
				reply_nums.setText(inbox.getComment_nums().trim());
				reply_nums.setVisibility(View.VISIBLE);
			} else {
				reply_nums.setVisibility(View.GONE);
			}
			if (!inbox.getGreet_nums().trim().equals("0")) {
				request_nums.setText(inbox.getGreet_nums().trim());
				request_nums.setVisibility(View.VISIBLE);
			} else {
				request_nums.setVisibility(View.GONE);
			}
			if (inbox.getDialogs() != null && inbox.getDialogs().size() > 0) {
				if (page == 1) {
					inboxMainAdapter = new InboxMainAdapter(context,
							inbox.getDialogs());
					lv_inbox.setAdapter(inboxMainAdapter);
					inboxMainAdapter.setMode(SwipeItemMangerImpl.Mode.Single);
				} else {
					inboxMainAdapter.update(inbox.getDialogs());
					inboxMainAdapter.notifyDataSetChanged();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * type 1代表正常信息  3代表删除消息
	 * 
	 * @param type
	 */
	private void getHttpData(final int type) {
		int cache_time = 0;
		RequestParams params = new RequestParams();
		String key = "";
		if (type == 3) {
			key = "msg/dialog/delete";
			params.addQueryStringParameter("cur_user", Conf.userID);
			params.addQueryStringParameter("dialog_id", dialog_id);
			// } else if (type == 2) {
			// key = "msg/ignore";
			// params.addQueryStringParameter("cur_user", Conf.userID);
		} else {
			// if (page == 1) {
			// waitDialog();
			// }
			params.addQueryStringParameter("cur_user", Conf.userID);
			params.addQueryStringParameter("page", String.valueOf(page));
			key = "msg/news";
		}
		params.addHeader("Authorization",
				PreferenceHelper.readString(context, "auth", "token"));
		CacheRequest.requestGET(context, key, params, key, cache_time,
				new CacheRequestCallBack() {

					@Override
					public void onFail(HttpException e, String result,
							String json) {
						// TODO Auto-generated method stub
						if (mDialog != null) {
							mDialog.dismiss();
						}
						ExitManager.getScreenManager().intentLogin(context,
								e.getExceptionCode() + "");
						if (json.equals("")) {
							BasicUtils
									.toast(context.getString(R.string.str_net_register));
							return;
						}
					}

					@Override
					public void onData(String json) {
						// TODO Auto-generated method stub
						if (mDialog != null) {
							mDialog.dismiss();
						}
						if (json.equals("")) {
							BasicUtils
									.toast(context.getString(R.string.str_net_register));
							return;
						}
						try {

							inbox = new Gson().fromJson(json, BaseJson.class);
							if (inbox != null
									&& inbox.getStatus().equals("200")) {
								if (type == 1) {
									setView();// 设置
//									Intent intent = new Intent();
//									intent.putExtra("inboxBadge",
//											inbox.getTotal_nums());
//									intent.putExtra("type", 4);
//									intent.setAction("com.action.inboxBadge");
//									context.sendBroadcast(intent);
								}
							} else {
								if (type == 1) {
									if (page > 1)
										--page;
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							pullScrollView.setheaderViewReset();
							pullScrollView.setfooterViewReset();
						}
					}
				});
		// kjh.get(url, params, new HttpCallBack() {
		//
		// @Override
		// public void onSuccess(Object obj) {
		// // TODO Auto-generated method stub
		// try {
		// LogUtils.printLogE("inbox", obj.toString());
		// inbox = new Gson().fromJson(obj.toString(), BaseJson.class);
		// if (inbox != null && inbox.getStatus().equals("200")) {
		// if (type == 1) {
		// setView();// 设置
		// Intent intent = new Intent();
		// intent.putExtra("inboxBadge", inbox.getTotal_nums());
		// intent.putExtra("type", 4);
		// intent.setAction("com.action.inboxBadge");
		// context.sendBroadcast(intent);
		// }
		// } else {
		// ViewInject.toast(obj.toString());
		// if (type == 1) {
		// if (page > 1)
		// --page;
		// }
		// }
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } finally {
		// pullScrollView.setheaderViewReset();
		// pullScrollView.setfooterViewReset();
		// if (mDialog != null) {
		// mDialog.dismiss();
		// }
		// }
		// }
		//
		// @Override
		// public void onLoading(long count, long current) {
		// // TODO Auto-generated method stub
		// }
		//
		// @Override
		// public void onFailure(Throwable t, int errorNo, String strMsg) {
		// // TODO Auto-generated method stub
		// pullScrollView.setheaderViewReset();
		// pullScrollView.setfooterViewReset();
		// if (page > 1)
		// --page;
		// if (mDialog != null) {
		// mDialog.dismiss();
		// }
		// ExitManager.getScreenManager().intentLogin(context,
		// StringUtils.httpRsponse(t.toString()));
		// }
		//
		// });

	}

	@OnClick({ R.id.layout_back })
	protected void onClickView(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.layout_back:
			context.finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.layout_reply:
			try {
				reply_nums.setVisibility(View.GONE);
				if (inbox != null && inbox.getComment_nums() != null
						&& !inbox.getComment_nums().trim().equals("0")) {
					Intent delIntent = new Intent();
					delIntent.putExtra("inboxBadge", inbox.getComment_nums()
							.trim());
					delIntent.putExtra("type", 1);
					delIntent.setAction("com.action.inboxBadge");
					context.sendBroadcast(delIntent);
					inbox.setComment_nums("0");
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			Intent intent = new Intent(new Intent(context,
					FragmentToActivity.class));
			intent.putExtra("who", "comment");
			context.startActivity(intent);
			break;
		case R.id.layout_request:
			try {
				request_nums.setVisibility(View.GONE);
				if (!inbox.getGreet_nums().trim().equals("0")) {
					Intent delIntent = new Intent();
					delIntent.putExtra("inboxBadge", inbox.getGreet_nums()
							.trim());
					delIntent.putExtra("type", 1);
					delIntent.setAction("com.action.inboxBadge");
					context.sendBroadcast(delIntent);
					inbox.setGreet_nums("0");
				}

				Intent praiseIntent = new Intent(new Intent(context,
						FragmentToActivity.class));
				praiseIntent.putExtra("who", "praise");
				context.startActivity(praiseIntent);
			} catch (Exception e) {
				// TODO: handle exception
			}
			break;
		default:
			break;
		}
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
	public void refresh() {
		// TODO Auto-generated method stub
		// if (SystemTool.checkNet(context)) {
		page = 1;
		getHttpData(1);
		// } else {
		// ViewInject.toast(getString(R.string.str_net_register));
		// pullScrollView.setheaderViewReset();
		// }
	}

	@Override
	public void loadMore() {
		// TODO Auto-generated method stub

		// if (SystemTool.checkNet(context)) {
		if (inbox != null && inbox.getDialogs() != null
				&& inbox.getDialogs().size() > 0) {
			++page;
		} else {
			if (inbox == null) {
				page = 1;
			} else if (page != 1)
				++page;
		}
		getHttpData(1);
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

}
