package com.amap.map3d.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.map3d.demo.inputtip.InputtipsActivity;
import com.amap.map3d.demo.overlay.MarkerClickActivity;
import com.amap.map3d.demo.poisearch.PoiKeywordSearchActivity;
import com.amap.map3d.demo.util.AMapUtil;
import com.amap.map3d.demo.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import overlay.PoiOverlay;


/**
 * AMapV2地图中介绍如何显示一个基本地图
 */
public class StandardAct extends Activity implements OnClickListener, AMap.OnMarkerClickListener {
	private MapView mapView;
	private AMap aMap;
	private MarkerOptions markerOption;

//	private Button locButton;
	private Button saveButton;
	private Button naviButton;
	private Button searchButton;

	private TextView showText;
//	private EditText sText;

	private RadioGroup rg1;
	private RadioButton rb1,rb2;

	private LatLng desPoint;
	private LatLng startPoint;

	private CustomMapStyleOptions mapStyleOptions = new CustomMapStyleOptions();

	private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
	private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);

	private AutoCompleteTextView searchText;// 输入搜索关键字
	private String keyWord = "";// 要输入的poi搜索关键字
	private ProgressDialog progDialog = null;// 搜索时进度条
	private EditText editCity;// 要输入的城市名字或者城市区号
	private PoiResult poiResult; // poi返回的结果
	private int currentPage = 0;// 当前页面，从0开始计数
	private PoiSearch.Query query;// Poi查询条件类
	private PoiSearch poiSearch;// POI搜索
//	private ListView minputlist;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_standard);
	    /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
		//Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
		//  MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);

		mapView = (MapView) findViewById(R.id.mapView);
		mapView.onCreate(savedInstanceState);// 此方法必须重写

		init();

		int from1 = getIntent().getIntExtra("from", 0);
		if (from1 == 1) {
			try {
				JSONObject jot = new JSONObject(getIntent().getStringExtra("jot"));
				showText.setText(getIntent().getStringExtra("jot") );
				String li = jot.getString("line");

				double la = jot.getDouble("lati");
				double lo = jot.getDouble("longi");
				String re = jot.getString("remark");
				desPoint = new LatLng(la, lo);
				addMarkersToMap(desPoint, li, re);
				aMap.moveCamera(CameraUpdateFactory
						.newCameraPosition(new CameraPosition(desPoint, 16f, 0, 0)));
			} catch (JSONException e) {

			}
		} else if (from1 == 2) {
			try {
				JSONArray jay = new JSONArray(getIntent().getStringExtra("jsonarry"));
				showText.setText(getIntent().getStringExtra("jsonarry") );
				for (int i = 0; i < jay.length(); i = i + 1) {
					try {
						String li = jay.getJSONObject(i).getString("line");

                        double la = jay.getJSONObject(i).getDouble("lati");
                        double lo = jay.getJSONObject(i).getDouble("longi");
						String re = jay.getJSONObject(i).getString("remark");
						desPoint = new LatLng(la, lo);

						addMarkersToMap(desPoint, li, re);
					} catch (JSONException e) {

					}
				}
				aMap.moveCamera(CameraUpdateFactory
						.newCameraPosition(new CameraPosition(desPoint, 10f, 0, 0)));
			} catch (JSONException e) {

			}
		} else {
			showText.setText("from: null");
		}

	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
		setMapCustomStyleFile(this);

//		locButton = (Button) findViewById(R.id.loc_button);
		saveButton = (Button) findViewById(R.id.save_button);
		naviButton = (Button) findViewById(R.id.navi_button);

		showText = (TextView)findViewById(R.id.showtext1);

		searchButton = (Button) findViewById(R.id.search_button);
//		sText = (EditText) findViewById(R.id.s_text);
//		minputlist = (ListView)findViewById(R.id.inputlist);
		searchText = (AutoCompleteTextView) findViewById(R.id.act);
//		searchText.addTextChangedListener(this);
		searchText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String newText = s.toString().trim();
				if (!AMapUtil.IsEmptyOrNullString(newText)) {
					InputtipsQuery inputquery = new InputtipsQuery(newText, editCity.getText().toString());
					Inputtips inputTips = new Inputtips(StandardAct.this, inputquery);
					inputTips.setInputtipsListener(new Inputtips.InputtipsListener() {
						@Override
						public void onGetInputtips(List<Tip> tipList, int rCode) {
							if (rCode == AMapException.CODE_AMAP_SUCCESS) {// 正确返回
								List<String> listString = new ArrayList<String>();
								for (int i = 0; i < tipList.size(); i++) {
									listString.add(tipList.get(i).getName());
								}
								ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
										getApplicationContext(),
										R.layout.route_inputs, listString);
								searchText.setAdapter(aAdapter);
								aAdapter.notifyDataSetChanged();
							} else {
								ToastUtil.showerror(StandardAct.this, rCode);
							}

						}
					});
					inputTips.requestInputtipsAsyn();
				}
			}

		});
		editCity = (EditText) findViewById(R.id.city);

		rg1 = (RadioGroup) findViewById(R.id.rg1) ;
		rb1 = (RadioButton) findViewById(R.id.rb1) ;
		rb2 = (RadioButton) findViewById(R.id.rb2) ;

		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				keyWord = AMapUtil.checkEditText(searchText);
//				keyWord = searchText.getText().toString();
//				keyWord = sText.getText().toString();
				if ("".equals(keyWord)) {
					ToastUtil.show(StandardAct.this, "请输入搜索关键字");
					return;
				} else {
					doSearchQuery();
					showText.setText(keyWord);
				}
			}
		});

		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(desPoint == null) {
					Toast.makeText(StandardAct.this, "请先选定位置", Toast.LENGTH_SHORT).show();
					return;
				} else {
					Intent intent = new Intent(StandardAct.this, SaveAct.class);
					intent.putExtra("from", "StandardAct");

                    intent.putExtra("latitude", desPoint.latitude);
                    intent.putExtra("longitude", desPoint.longitude);
					StandardAct.this.startActivity(intent);
				}
			}
		});

		naviButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(desPoint == null) {
					Toast.makeText(StandardAct.this, "请先选定目的地", Toast.LENGTH_SHORT).show();
					return;
				} else {
					Intent i1 = new Intent();
					i1.setData(Uri.parse("amapuri://route/plan/?did=&dlat=" + desPoint.latitude +
									"&dlon=" + desPoint.longitude + "&dname=B&dev=0&t=0"));
//					i1.setData(Uri.parse("baidumap://map/direction?" +
//							"&origin=" + startPoint.latitude + "," + startPoint.longitude +
//							"&destination=" + desPoint.latitude + "," + desPoint.longitude +
//							"&coord_type=bd09ll&mode=driving&src=andr.baidu.openAPIdemo"));
					startActivity(i1);
				}
			}
		});

		rg1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup radioGroup, int i) {
				if(i == rb1.getId()){
					aMap.setMapType(AMap.MAP_TYPE_NORMAL);
				} else if (i == rb2.getId()) {
					aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
				}
			}
		});


		aMap.setOnMapLongClickListener(new AMap.OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng point) {
				onResume();
				aMap.clear();
				addMarkersToMap(point, "点击的位置", "");
				desPoint = point;
			}
		});

	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		setupLocationStyle();

//		desPoint = new LatLng(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude());
//		showText.setText(aMap.getMyLocation().toString());
		LatLng tianjin = new LatLng(39.139797, 117.215881);
		aMap.moveCamera(CameraUpdateFactory
				.newCameraPosition(new CameraPosition(tianjin, 10f, 0, 0)));
	}

	/**
	 * 设置自定义定位蓝点
	 */
	private void setupLocationStyle(){
		// 自定义系统定位蓝点
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		// 自定义定位蓝点图标
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
				fromResource(R.drawable.gps_point));
		// 自定义精度范围的圆形边框颜色
		myLocationStyle.strokeColor(STROKE_COLOR);
		//自定义精度范围的圆形边框宽度
		myLocationStyle.strokeWidth(5);
		// 设置圆形的填充颜色
		myLocationStyle.radiusFillColor(FILL_COLOR);

		myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);

		// 将自定义的 myLocationStyle 对象添加到地图上
		aMap.setMyLocationStyle(myLocationStyle);
	}

	private void setMapCustomStyleFile(Context context) {
		String styleName = "style_new.data";
		InputStream inputStream = null;
		try {
			inputStream = context.getAssets().open(styleName);
			byte[] b = new byte[inputStream.available()];
			inputStream.read(b);

			if(mapStyleOptions != null) {
				// 设置自定义样式
				mapStyleOptions.setStyleData(b);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 在地图上添加marker
	 */
	private void addMarkersToMap(LatLng point, String title, String remark) {

		markerOption = new MarkerOptions().icon(BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
				.position(point)
				.title(title)
				.snippet(remark);
		Marker marker = aMap.addMarker(markerOption);
		marker.showInfoWindow();
	}

	/**
	 * 对marker标注点点击响应事件
	 */
	@Override
	public boolean onMarkerClick(final Marker marker) {
		if (aMap != null) {
			jumpPoint(marker);
		}
		Toast.makeText(StandardAct.this, "您点击了Marker", Toast.LENGTH_SHORT).show();
		return true;
	}

	/**
	 * marker点击时跳动一下
	 */
	public void jumpPoint(final Marker marker) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = aMap.getProjection();
		final LatLng markerLatlng = marker.getPosition();
		Point markerPoint = proj.toScreenLocation(markerLatlng);
		markerPoint.offset(0, -100);
		final LatLng startLatLng = proj.fromScreenLocation(markerPoint);
		final long duration = 1500;

		final Interpolator interpolator = new BounceInterpolator();
		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
				double lng = t * markerLatlng.longitude + (1 - t)
						* startLatLng.longitude;
				double lat = t * markerLatlng.latitude + (1 - t)
						* startLatLng.latitude;
				marker.setPosition(new LatLng(lat, lng));
				if (t < 1.0) {
					handler.postDelayed(this, 16);
				}
			}
		});
	}

	/**
	 * 开始进行poi搜索
	 */
	protected void doSearchQuery() {
		showProgressDialog();// 显示进度框
		currentPage = 0;
		query = new PoiSearch.Query(keyWord, "", editCity.getText().toString());// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
		query.setPageSize(10);// 设置每页最多返回多少条poiitem
		query.setPageNum(currentPage);// 设置查第一页

		poiSearch = new PoiSearch(this, query);
		poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
			@Override
			public void onPoiSearched(PoiResult result, int rCode) {
				dissmissProgressDialog();// 隐藏对话框
				if (rCode == AMapException.CODE_AMAP_SUCCESS) {
					if (result != null && result.getQuery() != null) {// 搜索poi的结果
						if (result.getQuery().equals(query)) {// 是否是同一条
							poiResult = result;
							// 取得搜索到的poiitems有多少页
							List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
							List<SuggestionCity> suggestionCities = poiResult
									.getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

							if (poiItems != null && poiItems.size() > 0) {
								aMap.clear();// 清理之前的图标
								PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
								poiOverlay.removeFromMap();
								poiOverlay.addToMap();
								poiOverlay.zoomToSpan();
								poiOverlay.notifyAll();
							} else if (suggestionCities != null
									&& suggestionCities.size() > 0) {
								showSuggestCity(suggestionCities);
							} else {
								ToastUtil.show(StandardAct.this,
										R.string.no_result);
							}
						}
					} else {
						ToastUtil.show(StandardAct.this,
								R.string.no_result);
					}
				} else {
					ToastUtil.showerror(StandardAct.this, rCode);
				}
			}

			@Override
			public void onPoiItemSearched(PoiItem poiItem, int i) {

			}
		});
		poiSearch.searchPOIAsyn();
	}

	/**
	 * poi没有搜索到数据，返回一些推荐城市的信息
	 */
	private void showSuggestCity(List<SuggestionCity> cities) {
		String infomation = "推荐城市\n";
		for (int i = 0; i < cities.size(); i++) {
			infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
					+ cities.get(i).getCityCode() + "城市编码:"
					+ cities.get(i).getAdCode() + "\n";
		}
		ToastUtil.show(StandardAct.this, infomation);

	}

	/**
	 * 显示进度框
	 */
	private void showProgressDialog() {
		if (progDialog == null)
			progDialog = new ProgressDialog(this);
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setIndeterminate(false);
		progDialog.setCancelable(false);
		progDialog.setMessage("正在搜索:\n" + keyWord);
		progDialog.show();
	}

	/**
	 * 隐藏进度框
	 */
	private void dissmissProgressDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		}

	}

	@Override
	public void onBackPressed() {
		if(aMap.getMapScreenMarkers().size() > 0){
			aMap.clear();
			desPoint = null;
			onResume();
		} else {
			super.onBackPressed();
		}
	}

}
