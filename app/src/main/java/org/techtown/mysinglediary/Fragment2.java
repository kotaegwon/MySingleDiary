package org.techtown.mysinglediary;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUriExposedException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.github.channguyen.rsv.RangeSliderView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Fragment2 extends Fragment {

    int mMode = Constants.MODE_INSERT;
    int _id = -1;
    int weatherIndex = 0;
    int moodIndex = 2;

    private Note item;

    private Context context;
    private OnTabItemSelectedListener listener;
    private OnRequestListener requestListener;
    private Button btn_save, btn_delete, btn_close;
    private RangeSliderView rangeSliderView;
    private TextView dateTextView, locationTextView;

    private ImageView weatherIcon, pictureImageView;

    private EditText contentsInput;

    boolean isPhotoCaptured;
    boolean isPhotoFileSaved;
    boolean isPhotoCanceled;

    int selectedPhotoMenu;

    Uri uri;
    File file;
    Bitmap resultPhotoBitmap;

    SimpleDateFormat dateFormat;
    String currentDateString;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context=context;

        if(context instanceof OnTabItemSelectedListener){
            listener=(OnTabItemSelectedListener) context;
        }

        if(context instanceof OnRequestListener){
            requestListener=(OnRequestListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(context != null){
            context=null;
            listener=null;
            requestListener=null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView=(ViewGroup) inflater.inflate(R.layout.fragment2, container, false);
        initUI(rootView);

        //현재 위치 확인
        if (requestListener != null) {
            requestListener.onRequest("getCurrentLocation");
        }
        applyItem();

        return rootView;
    }
    private void initUI(ViewGroup rootView){
        weatherIcon = rootView.findViewById(R.id.weatherIcon);
        dateTextView=rootView.findViewById(R.id.dateTextView);
        locationTextView=rootView.findViewById(R.id.locationTextView);
        btn_save=rootView.findViewById(R.id.saveButton);
        btn_delete=rootView.findViewById(R.id.deleteButton);
        btn_close=rootView.findViewById(R.id.closeButton);

        contentsInput=rootView.findViewById(R.id.contentsInput);
        pictureImageView=rootView.findViewById(R.id.pictureImageView);
        pictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPhotoCaptured || isPhotoFileSaved){
                    showDialog(Constants.CONTENT_PHOTO_EX);
                }else{
                    showDialog(Constants.CONTENT_PHOTO);
                }
            }
        });

        View.OnClickListener onclickListener=new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.saveButton:
                        if(mMode == Constants.MODE_INSERT){
                            saveNote(); //db저장 메서드
                        }else if(mMode == Constants.MODE_MODIFY){
                            modifyNote();//db변경 메서드
                        }
                        if(listener!=null){
                            listener.onTabSelected(0);
                        }
                        break;
                    case R.id.deleteButton:
                        deleteNote(); //db저장 메서드
                        if(listener != null){
                            listener.onTabSelected(0);
                        }
                        break;
                    case R.id.closeButton:
                        if(listener != null){
                            listener.onTabSelected(0);
                        }
                        break;
                }
            }
        };
        btn_save.setOnClickListener(onclickListener);
        btn_delete.setOnClickListener(onclickListener);
        btn_close.setOnClickListener(onclickListener);


        rangeSliderView = rootView.findViewById(R.id.sliderView);
        final RangeSliderView.OnSlideListener listener = new RangeSliderView.OnSlideListener() {
            @Override
            public void onSlide(int index) {
                Toast.makeText(context, "기분 : "+(index+1), Toast.LENGTH_SHORT).show();
                moodIndex = index;
            }
        };

        rangeSliderView.setOnSlideListener(listener);
        rangeSliderView.setInitialIndex(2);
    }

    public void setAddress(String data) {
        locationTextView.setText(data);
    }

    public void setDateString(String dateString) {
        dateTextView.setText(dateString);
    }

    public void setContents(String data){
        contentsInput.setText(data);
    }

    public void setPicture(String picturePath, int sampleSize){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inSampleSize=sampleSize;
        resultPhotoBitmap=BitmapFactory.decodeFile(picturePath, options);

        pictureImageView.setImageBitmap(resultPhotoBitmap);
    }
    public void setMood(String mood){
        try {
            moodIndex=Integer.parseInt(mood);
            rangeSliderView.setInitialIndex(moodIndex);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void setItem(Note item){
        this.item=item;
    }

    public void applyItem(){

        if(item != null){
            mMode=Constants.MODE_MODIFY;

            setWeatherIndex(Integer.parseInt(item.getWeather()));
            setAddress(item.getAddress());
            setDateString(item.getCreateDateStr());
            setContents(item.getContents());

            String picturePath=item.getPicture();

            if(picturePath == null || picturePath.equals("")){
                pictureImageView.setImageResource(R.drawable.noimagefound);
            }else{
                setPicture(item.getPicture(), 1);
            }
            setMood(item.getMood());
        }else{
            mMode=Constants.MODE_INSERT;

            setWeatherIndex(0);
            setAddress("");

            Date currentDate=new Date();
            if(dateFormat==null){
                dateFormat=new SimpleDateFormat(getResources().getString(R.string.today_date_format));
            }
            currentDateString=dateFormat.format(currentDate);
            setDateString(currentDateString);

            contentsInput.setText("");
            pictureImageView.setImageResource(R.drawable.noimagefound);
            setMood("2");
        }
    }

    public void setWeather(String data) {
        Constants.println("setWeather called : " + data);

        if (data != null) {
            if (data.equals("맑음")) {
                weatherIcon.setImageResource(R.drawable.weather_1);
                weatherIndex = 0;
            } else if (data.equals("구름 조금")) {
                weatherIcon.setImageResource(R.drawable.weather_2);
                weatherIndex = 1;
            } else if (data.equals("구름 많음")) {
                weatherIcon.setImageResource(R.drawable.weather_3);
                weatherIndex = 2;
            } else if (data.equals("흐림")) {
                weatherIcon.setImageResource(R.drawable.weather_4);
                weatherIndex = 3;
            } else if (data.equals("비")) {
                weatherIcon.setImageResource(R.drawable.weather_5);
                weatherIndex = 4;
            } else if (data.equals("눈/비")) {
                weatherIcon.setImageResource(R.drawable.weather_6);
                weatherIndex = 5;
            } else if (data.equals("눈")) {
                weatherIcon.setImageResource(R.drawable.weather_7);
                weatherIndex = 6;
            } else {
                Log.d("Fragment2", "Unknown weather string : " + data);
            }
        }
    }

    public void setWeatherIndex(int index) {
        if (index == 0) {
            weatherIcon.setImageResource(R.drawable.weather_1);
            weatherIndex = 0;
        } else if (index == 1) {
            weatherIcon.setImageResource(R.drawable.weather_2);
            weatherIndex = 1;
        } else if (index == 2) {
            weatherIcon.setImageResource(R.drawable.weather_3);
            weatherIndex = 2;
        } else if (index == 3) {
            weatherIcon.setImageResource(R.drawable.weather_4);
            weatherIndex = 3;
        } else if (index == 4) {
            weatherIcon.setImageResource(R.drawable.weather_5);
            weatherIndex = 4;
        } else if (index == 5) {
            weatherIcon.setImageResource(R.drawable.weather_6);
            weatherIndex = 5;
        } else if (index == 6) {
            weatherIcon.setImageResource(R.drawable.weather_7);
            weatherIndex = 6;
        } else {
            Log.d("Fragment2", "Unknown weather index : " + index);
        }

    }


    public void showDialog(int id) {
        AlertDialog.Builder builder = null;

        switch(id) {

            case Constants.CONTENT_PHOTO:
                builder = new AlertDialog.Builder(context);

                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        selectedPhotoMenu = whichButton;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(selectedPhotoMenu == 0 ) {
                            showPhotoCaptureActivity();
                        } else if(selectedPhotoMenu == 1) {
                            showPhotoSelectionActivity();
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                break;

            case Constants.CONTENT_PHOTO_EX:
                builder = new AlertDialog.Builder(context);

                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo_ex, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        selectedPhotoMenu = whichButton;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(selectedPhotoMenu == 0) {
                            showPhotoCaptureActivity();
                        } else if(selectedPhotoMenu == 1) {
                            showPhotoSelectionActivity();
                        } else if(selectedPhotoMenu == 2) {
                            isPhotoCanceled = true;
                            isPhotoCaptured = false;

                            pictureImageView.setImageResource(R.drawable.picture1);
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                break;
            default:
                break;
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showPhotoCaptureActivity() {
        try {
            file = createFile();
            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);
        } else {
            uri = Uri.fromFile(file);
        }


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        startActivityForResult(intent, Constants.REQ_PHOTO_CAPTURE);

    }

    private File createFile() {
        String filename = createFilename();
        File outFile = new File(context.getFilesDir(), filename);

        return outFile;
    }

    public void showPhotoSelectionActivity() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, Constants.REQ_PHOTO_SELECTION);
    }


    //다른 액티비티로부터의 응답 처리

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (intent != null) {
            switch (requestCode) {
                case Constants.REQ_PHOTO_CAPTURE:  // 사진 찍는 경우

                    resultPhotoBitmap = decodeSampledBitmapFromResource(file, pictureImageView.getWidth(), pictureImageView.getHeight());
                    pictureImageView.setImageBitmap(resultPhotoBitmap);

                    break;

                case Constants.REQ_PHOTO_SELECTION:  // 사진을 앨범에서 선택하는 경우

                    Uri fileUri = intent.getData();

                    ContentResolver resolver = context.getContentResolver();

                    try {
                        InputStream inputStream = resolver.openInputStream(fileUri);
                        resultPhotoBitmap = BitmapFactory.decodeStream(inputStream);
                        pictureImageView.setImageBitmap(resultPhotoBitmap);

                        inputStream.close();

                        isPhotoCaptured = true;
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }
    }
    //디코드
    public static Bitmap decodeSampledBitmapFromResource(File res, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res.getAbsolutePath(),options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(res.getAbsolutePath(),options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height;
            final int halfWidth = width;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    private String createFilename() {
        Date curDate = new Date();
        String curDateStr = String.valueOf(curDate.getTime());

        return curDateStr;
    }

    //db 추가
    private void saveNote() {
        String address = locationTextView.getText().toString();
        String contents = contentsInput.getText().toString();

        String picturePath = savePicture();

        String sql = "insert into " + NoteDatabase.TABLE_NOTE +
                "(WEATHER, ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, MOOD, PICTURE) values(" +
                "'"+ weatherIndex + "', " +
                "'"+ address + "', " +
                "'"+ "" + "', " +
                "'"+ "" + "', " +
                "'"+ contents + "', " +
                "'"+ moodIndex + "', " +
                "'"+ picturePath + "')";

        NoteDatabase database = NoteDatabase.getInstance(context);
        database.execSQL(sql);

    }
    //db 레코드 수정
    private void modifyNote() {
        if (item != null) {
            String address = locationTextView.getText().toString();
            String contents = contentsInput.getText().toString();

            String picturePath = savePicture();

            //노트테이블 업데이트
            String sql = "update " + NoteDatabase.TABLE_NOTE +
                    " set " +
                    "   WEATHER = '" + weatherIndex + "'" +
                    "   ,ADDRESS = '" + address + "'" +
                    "   ,LOCATION_X = '" + "" + "'" +
                    "   ,LOCATION_Y = '" + "" + "'" +
                    "   ,CONTENTS = '" + contents + "'" +
                    "   ,MOOD = '" + moodIndex + "'" +
                    "   ,PICTURE = '" + picturePath + "'" +
                    " where " +
                    "   _id = " + item._id;

            NoteDatabase database = NoteDatabase.getInstance(context);
            database.execSQL(sql);
        }
    }

    //db 레코드 삭제
    private void deleteNote() {

        if (item != null) {
            // delete note
            String sql = "delete from " + NoteDatabase.TABLE_NOTE +
                    " where " +
                    "   _id = " + item._id;

            NoteDatabase database = NoteDatabase.getInstance(context);
            database.execSQL(sql);
        }
    }

    private String savePicture() {
        if (resultPhotoBitmap == null) {
            return "";
        }

        File photoFolder = new File(Constants.FOLDER_PHOTO);

        if(!photoFolder.isDirectory()) {
            photoFolder.mkdirs();
        }

        String photoFilename = createFilename();
        String picturePath = photoFolder + File.separator + photoFilename;

        try {
            FileOutputStream outputStream = new FileOutputStream(picturePath);
            resultPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return picturePath;
    }

}
