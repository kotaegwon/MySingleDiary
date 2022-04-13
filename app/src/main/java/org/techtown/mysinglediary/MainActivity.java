package org.techtown.mysinglediary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.techtown.mysinglediary.data.WeatherItem;
import org.techtown.mysinglediary.data.WeatherResult;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnTabItemSelectedListener, OnRequestListener, MyApplication.OnResponseListener {

    private static final String TAG = "MainActivity";
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    private long backpressedTime=0;

    private Fragment1 fragment1;
    private Fragment2 fragment2;
    private Fragment3 fragment3;

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    private Location currentLocation;
    private GPSListener gpsListener;

    int locationCount=0;
    private String currentWeather;
    private String currentAddress;
    private String currentDateString;
    private Date currentDate;
    private SimpleDateFormat todayDateFormat;

    public static NoteDatabase mDatabase=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment1=new Fragment1();
        fragment2=new Fragment2();
        fragment3=new Fragment3();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, fragment1).commit();

//        바텀내비를 사용하여 탭을 이동했지만 메뉴를 두개로 줄이면서 툴바메뉴로 바꿈
//        bottomNavigationView=findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                switch (item.getItemId()){
//                    case R.id.tab1:
//                        Toast.makeText(MainActivity.this, "목록", Toast.LENGTH_SHORT).show();
//                        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, fragment1).commit();
//                    return true;
//
//                    case R.id.tab2:
//                        Toast.makeText(MainActivity.this, "작성",Toast.LENGTH_SHORT).show();
//                        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, fragment2).commit();
//                    return true;
//
////                    case R.id.tab3:
////                        Toast.makeText(MainActivity.this, "통계", Toast.LENGTH_SHORT).show();
////                        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, fragment3).commit();
////                    return true;
//                }
//                return false;
//            }
//        });

        setPicturePath();

        AndPermission.with(this)
                .runtime()
                .permission(
                        Permission.ACCESS_FINE_LOCATION,
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        showToast("허용된 권한 갯수 : " + permissions.size());
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        showToast("거부된 권한 갯수 : " + permissions.size());
                    }
                })
                .start();

        // 데이터베이스 열기
        openDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.tab1:
                Toast.makeText(MainActivity.this, "목록", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, fragment1).addToBackStack(null).commit();
                break;
            case R.id.tab2:
                Toast.makeText(MainActivity.this, "작성",Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, fragment2).addToBackStack(null).commit();
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        if(System.currentTimeMillis()>backpressedTime+2000){
            backpressedTime=System.currentTimeMillis();

        }else if(System.currentTimeMillis()<=backpressedTime+2000){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setMessage("앱을 종료하시겠습니까?");
            builder.setPositiveButton("아니오",((dialog, which) -> {dialog.cancel();}));
            builder.setNegativeButton("예",((dialog, which) -> {finish();}));
            builder.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mDatabase != null){
            mDatabase.close();
            mDatabase=null;
        }
    }

    //데이터 베이스 열기(없을때 만듬)
    public void openDatabase(){
        if(mDatabase != null){
            mDatabase.close();
            mDatabase=null;
        }

        mDatabase=NoteDatabase.getInstance(this);
        boolean isOpen = mDatabase.open();
        if (isOpen) {
            Log.d(TAG, "Note database is open.");
        } else {
            Log.d(TAG, "Note database is not open.");
        }
    }

    public void setPicturePath(){
        String folderPath=getFilesDir().getAbsolutePath();
        Constants.FOLDER_PHOTO=folderPath+ File.separator+"photo";

        File photoFolder=new File(Constants.FOLDER_PHOTO);
        if(!photoFolder.exists()){
            photoFolder.mkdir();
        }
    }

    @Override
    public void onTabSelected(int position) {
        if(position == 0){
            getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, fragment1).addToBackStack(null).commit();
        }else if(position == 1){
            fragment2 = new Fragment2();
            getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, fragment2).addToBackStack(null).commit();
        }

//        else if(position==2){
//            bottomNavigationView.setSelectedItemId(R.id.tab3);
//        }
    }

    @Override
    public void showFragment2(Note item) {
        fragment2=new Fragment2();
        fragment2.setItem(item);

        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, fragment2).addToBackStack(null).commit();
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void onRequest(String command) {
        if (command != null) {
            if (command.equals("getCurrentLocation")) {
                getCurrentLocation();
            }
        }
    }

    public void getCurrentLocation(){
        currentDate = new Date();
        currentDateString = Constants.dateFormat3.format(currentDate);

        if(todayDateFormat==null){
            todayDateFormat=new SimpleDateFormat(getResources().getString(R.string.today_date_format));
        }
        currentDateString=todayDateFormat.format(currentDate);
        if(fragment2 != null){
            fragment2.setDateString(currentDateString);
        }

        LocationManager manager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            if(currentLocation!=null) {
                currentLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double latitude = currentLocation.getLatitude();
                double longitude = currentLocation.getLongitude();

                String message = "마지막 위치 -> 위도: " + latitude + "\n경도: " + longitude;
                println(message);


                getCurrentWeather();
                getCurrentAddress();
            }
            
            gpsListener=new GPSListener();
            long minTime=1000;
            float minDistance=0;
            
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);
            println("현재 위치 요청됨");
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }
    
    public void stopLocationService(){
        LocationManager manager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            manager.removeUpdates(gpsListener);
            println("현재 위치 요청됨");
        }catch (SecurityException e){
            e.printStackTrace();
        }

    }

    class GPSListener implements LocationListener{

        @Override
        public void onLocationChanged(@NonNull Location location) {
            currentLocation=location;
            locationCount++;

            Double latitude=location.getLatitude(); //위도
            Double longitude=location.getLongitude(); //경도

            String message="현재 위치 -> 위도 : "+latitude+"\n경도 : "+longitude;
            println(message);

            getCurrentWeather();
            getCurrentAddress();

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }

    }

    //현재 위치를 이용해 주소 확인 위한 메서드
    //Geocoder클래스를 이용하여 현재 위치를 주소로 변환하는 것을 확인할 수 있음
    public void getCurrentAddress() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (addresses != null && addresses.size() > 0) {
            currentAddress = null;

            Address address = addresses.get(0);
            if (address.getLocality() != null) {
                currentAddress = address.getLocality();
            }

            if (address.getSubLocality() != null) {
                if (currentAddress != null) {
                    currentAddress +=  " " + address.getSubLocality();
                } else {
                    currentAddress = address.getSubLocality();
                }
            }

            String adminArea = address.getAdminArea();
            String country = address.getCountryName();
            println("Address : " + country + " " + adminArea + " " + currentAddress);

            if (fragment2 != null) {
                fragment2.setAddress(currentAddress);
            }
        }
    }

    public void getCurrentWeather() {

        Map<String, Double> gridMap = GridUtil.getGrid(currentLocation.getLatitude(), currentLocation.getLongitude());
        double gridX = gridMap.get("x");
        double gridY = gridMap.get("y");
        println("x -> " + gridX + ", y -> " + gridY);

        sendLocalWeatherReq(gridX, gridY);

    }

    public void sendLocalWeatherReq(double gridX, double gridY) {
        String url = "http://www.kma.go.kr/wid/queryDFS.jsp";
        url += "?gridx=" + Math.round(gridX);
        url += "&gridy=" + Math.round(gridY);

        Map<String,String> params = new HashMap<String,String>();

        MyApplication.send(Constants.REQ_WEATHER_BY_GRID, Request.Method.GET, url, params, this);
    }

    @Override
    public void processResponse(int requestCode, int responseCode, String response) {
        if(responseCode==200){
            if(requestCode==Constants.REQ_WEATHER_BY_GRID){
                XmlParserCreator parserCreator=new XmlParserCreator() {
                    @Override
                    public XmlPullParser createParser() {
                        try {
                            return XmlPullParserFactory.newInstance().newPullParser();
                        } catch (XmlPullParserException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

                GsonXml gsonXml=new GsonXmlBuilder().setXmlParserCreator(parserCreator).setSameNameLists(true).create();
                WeatherResult weatherResult=gsonXml.fromXml(response, WeatherResult.class);

                //현재 기준 시간
                try {
                    Date tmDate=Constants.dateFormat.parse(weatherResult.header.tm);
                    String tmDateText=Constants.dateFormat2.format(tmDate);

                    for(int i=0;i<weatherResult.body.datas.size();i++){
                        WeatherItem item=weatherResult.body.datas.get(i);
                        float ws=Float.valueOf(String.valueOf((int)Math.round(item.ws*10)))/10.0f;
                    }

                    //현재 날씨
                    WeatherItem item =weatherResult.body.datas.get(0);
                    currentWeather=item.wfKor;
                    if(fragment2!=null){
                        fragment2.setWeather(item.wfKor);
                    }

                    //2회 후 위치요청서비스 중지
                    if(locationCount>1){
                        stopLocationService();;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else{
                println("Unknown request code : " + requestCode);
            }
        }

    }

    private void println(String data) {
        Log.d(TAG, data);
    }

}