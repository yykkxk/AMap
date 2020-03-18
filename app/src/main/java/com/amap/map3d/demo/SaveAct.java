package com.amap.map3d.demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


/***
 * 展示定位sdk配置的示例，配置选项后调用的实际上是locationActivity的定位功能，但是覆盖了新的配置项
 * 注意：有些选项存在缓存的原因，所有在选中后再取消依然会在定位结果中显示出来
 * @author baidu
 *
 */
public class SaveAct extends Activity{
	private EditText lineText;
	private EditText remarkText;
	private Button addButton;
	private TextView text13, text14;
	private TextView showText;
    private Spinner spArea, spLevel, spType;
    private String[] itemArea = { "地区","河东东丽", "河北北辰", "和平河西津南", "南开虹桥西青","滨海", "静海", "宝坻", "蓟州", "宁河" };
    private String[] itemLevel = { "电压","110kV", "220kV"};
    private String[] itemType = { "设备","接地箱", "终端塔", "其它"};

    public static final int SHOW_RESPONSE = 0;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_RESPONSE:
                    String response = (String) msg.obj;
//                    textView_response.setText(response);
                    showText.setText(response);
                    break;
                default:
                    break;
            }
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.a_save);
		lineText = (EditText)findViewById(R.id.line_text);
		remarkText = (EditText)findViewById(R.id.remark_text);
		showText = (TextView) findViewById(R.id.show_text);
		addButton = (Button)findViewById(R.id.add_button);
        text13 = (TextView) findViewById(R.id.textview13);
        text14 = (TextView) findViewById(R.id.textview14);

        spArea = (Spinner) findViewById(R.id.spArea);
        spType = (Spinner) findViewById(R.id.spType);
        spLevel = (Spinner) findViewById(R.id.spLevel);
        ArrayAdapter<String> adAera = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, itemArea);
        adAera.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spArea.setAdapter(adAera);
        spArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> adLevel = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, itemLevel);
        adLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLevel.setAdapter(adLevel);

        ArrayAdapter<String> adType = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, itemType);
        adType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(adType);

        double lati = getIntent().getDoubleExtra("latitude", 0);
        double longi = getIntent().getDoubleExtra("longitude", 0);
        text13.setText("" + lati);
        text14.setText("" + longi);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		addButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        double lati = getIntent().getDoubleExtra("latitude", 0);
                        double longi = getIntent().getDoubleExtra("longitude", 0);

                        String line = lineText.getText().toString();
                        String remark = remarkText.getText().toString();
                        int area = spArea.getId();
                        int type = spType.getId();
                        int level = spLevel.getId();

                        String url = "http://192.168.43.137:8088/web2/SaveServlet.action";
                        String urlget = url
                                + "?line=" + line
                                + "&remark=" + remark
                                + "&lati=" + lati
                                + "&longi=" + longi
                                + "&type=" + type
                                + "&area=" + area
                                + "&level=" + level;
                        HttpClient httpCient = new DefaultHttpClient();
                        //第二步：创建代表请求的对象,参数是访问的服务器地址
                        HttpGet httpGet = new HttpGet(urlget);

                        try {
                            //第三步：执行请求，获取服务器发还的相应对象
                            HttpResponse httpResponse = httpCient.execute(httpGet);
                            //第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
                            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                //第五步：从相应对象当中取出数据，放到entity当中
                                HttpEntity entity = httpResponse.getEntity();
                                String response = EntityUtils.toString(entity,"utf-8");//将entity当中的数据转换为字符串

                                //在子线程中将Message对象发出去
                                Message message = new Message();
                                message.what = SHOW_RESPONSE;
                                message.obj = response.toString();
                                handler.sendMessage(message);
                            }

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }).start();

			}
		});

		lineText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(lineText.getText().toString().trim().length() == 0) {
                    addButton.setEnabled(false);
                } else {
                    addButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
        } );
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}
