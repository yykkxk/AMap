package com.amap.map3d.demo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import java.io.ByteArrayOutputStream;
import cz.msebera.android.httpclient.Header;
import android.app.Activity;

@SuppressLint("NewApi")
public class UploadActivity extends Activity implements View.OnClickListener {

    private EditText editTextName;
    private ProgressDialog prgDialog;

    private int RESULT_LOAD_IMG = 1;
    private RequestParams params = new RequestParams();
    private String encodedString;
    private Bitmap bitmap;
    private String imgPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_upload);

        prgDialog= new ProgressDialog(this);
        prgDialog.setCancelable(false);

        editTextName = (EditText) findViewById(R.id.editText);
        findViewById(R.id.choose_image).setOnClickListener(this);
        findViewById(R.id.upload_image).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.choose_image:
                loadImage();
                break;
            case R.id.upload_image:
                uploadImage();
                break;
        }
    }

    public void loadImage() {
        //这里就写了从相册中选择图片，相机拍照的就略过了
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    //当图片被选中的返回结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                editTextName.setText(selectedImage.getPath().substring(selectedImage.getPath().lastIndexOf("/")+1));

                // 获取游标
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();
                ImageView imgView = (ImageView) findViewById(R.id.imageView);
                imgView.setImageBitmap(BitmapFactory.decodeFile(imgPath));
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }

    //开始上传图片
    private void uploadImage() {
        if (imgPath != null && !imgPath.isEmpty()) {
            prgDialog.setMessage("Converting Image to Binary Data");
            prgDialog.show();
            encodeImagetoString();
        } else {
            Toast.makeText(getApplicationContext(), "You must select image from gallery before you try to upload",
                    Toast.LENGTH_LONG).show();
        }
    }


    public void encodeImagetoString() {
        new AsyncTask<Void, Void, String>() {

            protected void onPreExecute() {

            };

            @Override
            protected String doInBackground(Void... params) {
                BitmapFactory.Options options = null;
                options = new BitmapFactory.Options();
                options.inSampleSize = 3;
                bitmap = BitmapFactory.decodeFile(imgPath,
                        options);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // 压缩图片
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                byte[] byte_arr = stream.toByteArray();
                // Base64图片转码为String
                encodedString = Base64.encodeToString(byte_arr, 0);
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                prgDialog.setMessage("Calling Upload");
                // 将转换后的图片添加到上传的参数中
                params.put("image", encodedString);
                params.put("filename", editTextName.getText().toString());
                // 上传图片
                imageUpload();
            }
        }.execute(null, null, null);
    }

    public void imageUpload() {
        prgDialog.setMessage("Invoking JSP");
        String url = "http://192.168.43.137:8088/web2/uploadimg.jsp";
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                prgDialog.hide();
                Toast.makeText(getApplicationContext(), "upload success", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                prgDialog.hide();
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(),
                            "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // 当 Http 响应码'500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // 当 Http 响应码 404, 500
                else {
                    Toast.makeText(
                            getApplicationContext(), "Error Occured n Most Common Error: n1. Device " +
                                    "not connected to Internetn2. Web App is not deployed in App servern3." +
                                    " App server is not runningn HTTP Status code : "
                                    + statusCode, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (prgDialog != null) {
            prgDialog .dismiss();
        }
    }
}