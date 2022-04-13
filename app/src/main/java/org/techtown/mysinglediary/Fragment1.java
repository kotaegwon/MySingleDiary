package org.techtown.mysinglediary;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lib.kingja.switchbutton.SwitchMultiButton;

public class Fragment1 extends Fragment {

    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private SwitchMultiButton switchMultiButton;

    private Context context;
    private OnTabItemSelectedListener listener;

    private SimpleDateFormat todayDateFormat;

    private Button btn_todayWrite;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context=context;

        if(context instanceof OnTabItemSelectedListener){
            listener=(OnTabItemSelectedListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(context != null){
            context = null;
            listener= null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView=(ViewGroup) inflater.inflate(R.layout.fragment1, container, false);
        initUI(rootView);
        //db데이터 로딩
        loadNoteListData();
        return rootView;
    }

    private void initUI(ViewGroup rootView){
        btn_todayWrite=rootView.findViewById(R.id.todayWriteButton);
        btn_todayWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null){
                    listener.onTabSelected(1);
                }
            }
        });

        switchMultiButton=rootView.findViewById(R.id.switchButton);
        switchMultiButton.setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                Toast.makeText(getContext(), tabText, Toast.LENGTH_SHORT).show();

                adapter.switchLayout(position);
                adapter.notifyDataSetChanged();
            }
        });

        recyclerView=rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager manager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);

        adapter=new NoteAdapter();

        //adapter.addItem(new Note(10, "2", "충남 예산군", "","","안드로이드 꿀잼", "5", null, "4월 13일"));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnNoteItemClickListener() {
            @Override
            public void onItemClick(NoteAdapter.ViewHolder viewHolder, View view, int position) {
                Note item=adapter.getItem(position);
                Toast.makeText(getContext(),"아이템 선택됨 : "+item.getContents(), Toast.LENGTH_SHORT).show();

                if(listener != null){
                    listener.showFragment2(item);
                }
            }
        });
    }

    //리스트 데이터 로딩
    public int loadNoteListData() {
        String sql = "select _id, WEATHER, ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, MOOD, PICTURE, CREATE_DATE, MODIFY_DATE from " + NoteDatabase.TABLE_NOTE + " order by CREATE_DATE desc";

        int recordCount = -1;
        NoteDatabase database = NoteDatabase.getInstance(context);
        if (database != null) {
            Cursor outCursor = database.rawQuery(sql);

            recordCount = outCursor.getCount();

            ArrayList<Note> items = new ArrayList<Note>();

            for (int i = 0; i < recordCount; i++) {
                outCursor.moveToNext();

                int _id = outCursor.getInt(0);
                String weather = outCursor.getString(1);
                String address = outCursor.getString(2);
                String locationX = outCursor.getString(3);
                String locationY = outCursor.getString(4);
                String contents = outCursor.getString(5);
                String mood = outCursor.getString(6);
                String picture = outCursor.getString(7);
                String dateStr = outCursor.getString(8);
                String createDateStr = null;
                if (dateStr != null && dateStr.length() > 10) {
                    try {
                        Date inDate = Constants.dateFormat4.parse(dateStr);

                        if (todayDateFormat == null) {
                            todayDateFormat = new SimpleDateFormat(getResources().getString(R.string.today_date_format));
                        }
                        createDateStr = todayDateFormat.format(inDate);
                        //createDateStr = AppConstants.dateFormat3.format(inDate);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    createDateStr = "";
                }

                items.add(new Note(_id, weather, address, locationX, locationY, contents, mood, picture, createDateStr));
            }

            outCursor.close();

            adapter.setItems(items);
            adapter.notifyDataSetChanged();

        }

        return recordCount;
    }

}
