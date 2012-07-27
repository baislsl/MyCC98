package tk.djcrazy.MyCC98.fragment;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;

import tk.djcrazy.MyCC98.R;
import tk.djcrazy.MyCC98.adapter.PersonalboardListViewAdapter;
import tk.djcrazy.MyCC98.view.ParentView;
import tk.djcrazy.MyCC98.view.PullToRefreshListView;
import tk.djcrazy.MyCC98.view.PullToRefreshListView.OnRefreshListener;
import tk.djcrazy.libCC98.CC98Parser;
import tk.djcrazy.libCC98.data.BoardEntity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class PersonalBoardFragment extends Fragment implements OnRefreshListener{

	private static final String TAG = "PersonalBoardFragment";
	private List<BoardEntity> boardList;
	private PersonalboardListViewAdapter boardListViewAdapter;
	private PullToRefreshListView listView;
  	private static final int GET_LIST_SUCCESS = 1;
	private static final int GET_LIST_FAILED = 0;
 	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_LIST_SUCCESS:
				listView.setAdapter(boardListViewAdapter);
				boardListViewAdapter.notifyDataSetChanged();
 				listView.onRefreshComplete();
				listView.invalidate();
 				break;
			case GET_LIST_FAILED:
 				Toast.makeText(getActivity(), "网络或解析出错！", Toast.LENGTH_SHORT).show();
				break;
 			default:
				break;
			}
		}
	};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRefresh();
     }
 	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.personal_board, null);
		listView = (PullToRefreshListView) view.findViewById(R.id.personal_board_list);
		listView.setOnRefreshListener(this);
		if (boardListViewAdapter!=null) {
			listView.setAdapter(boardListViewAdapter);
		}
 		return view;
	}

 	public void fetchContent(){
		new Thread() {
			@Override
			public void run() {
				try {
					boardList = CC98Parser.getPersonalBoardList();
					boardListViewAdapter = new PersonalboardListViewAdapter(getActivity(), boardList);
 					handler.sendEmptyMessage(GET_LIST_SUCCESS);
				} catch (ClientProtocolException e) {
					handler.sendEmptyMessage(GET_LIST_FAILED);
					e.printStackTrace();
				} catch (ParseException e) {
					handler.sendEmptyMessage(GET_LIST_FAILED);
					e.printStackTrace();
				} catch (IOException e) {
					handler.sendEmptyMessage(GET_LIST_FAILED);
					e.printStackTrace();
				}
			}
		}.start();
	}

 	public void scrollListTo(int x, int y) { 
 		listView.scrollTo(x, y);
	}

	@Override
	public void onRefresh() {
		fetchContent();
	}
}