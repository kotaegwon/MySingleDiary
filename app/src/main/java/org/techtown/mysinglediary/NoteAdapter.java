package org.techtown.mysinglediary;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> implements OnNoteItemClickListener {

    ArrayList<Note> items=new ArrayList<Note>();

    //리사이클러 뷰 아이템 리스너
    OnNoteItemClickListener listener;

    //Fragment1 내용, 사진 타입 변수
    int layoutType=0;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.note_item, viewGroup, false);

        return new ViewHolder(itemView, this, layoutType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note item=items.get(position);
        holder.setItem(item);
        holder.setLayoutType(layoutType);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Note item) {
        items.add(item);
    }

    public void setItems(ArrayList<Note> items) {
        this.items = items;
    }

    public Note getItem(int position) {
        return items.get(position);
    }

    public void setOnItemClickListener(OnNoteItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(ViewHolder viewHolder, View view, int position) {
        if(listener != null){
            listener.onItemClick(viewHolder, view, position);
        }
    }

    public void switchLayout(int position){
        layoutType=position;
    }


    static class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout layout1, layout2; //내용, 사진

        ImageView moodImageView, weatherImageView, pictureExistsImageView; //내용 탭 이미지뷰
        ImageView pictureImageView, moodImageView2, weatherImageView2; //그림 탭 이미지뷰

        TextView contentsTextView, locationTextView, dateTextView; //내용 탭 텍스트 뷰
        TextView contentsTextView2, locationTextView2, dateTextView2; //그림 탭 텍스트 뷰

        public ViewHolder(@NonNull View itemView, final OnNoteItemClickListener listener, int layoutType) {
            super(itemView);

            layout1 = itemView.findViewById(R.id.layout1);
            layout2 = itemView.findViewById(R.id.layout2);

            weatherImageView = itemView.findViewById(R.id.weatherImageView);
            weatherImageView2 = itemView.findViewById(R.id.weatherImageView2);

            pictureImageView = itemView.findViewById(R.id.pictureImageView);
            pictureExistsImageView = itemView.findViewById(R.id.pictureExistsImageView);

            moodImageView = itemView.findViewById(R.id.moodImageView);
            moodImageView2 = itemView.findViewById(R.id.moodImageView2);

            contentsTextView = itemView.findViewById(R.id.contentsTextView);
            contentsTextView2 = itemView.findViewById(R.id.contentsTextView2);

            locationTextView = itemView.findViewById(R.id.locationTextView);
            locationTextView2 = itemView.findViewById(R.id.locationTextView2);

            dateTextView = itemView.findViewById(R.id.dateTextView);
            dateTextView2 = itemView.findViewById(R.id.dateTextView2);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=getAdapterPosition();

                    if(listener != null){
                        listener.onItemClick(ViewHolder.this, view, position);
                    }
                }
            });
            setLayoutType(layoutType);
        }

        public void setItem(Note item) {
            String mood = item.getMood();
            int moodIndex = Integer.parseInt(mood);
            setMoodImage(moodIndex);

            String picturePath = item.getPicture();
            if (picturePath != null && !picturePath.equals("")) {
                pictureExistsImageView.setVisibility(View.VISIBLE);
                pictureImageView.setVisibility(View.VISIBLE);
                pictureImageView.setImageURI(Uri.parse("file://" + picturePath));

            } else {
                pictureExistsImageView.setVisibility(View.GONE);
                pictureImageView.setVisibility(View.GONE);
                pictureImageView.setImageResource(R.drawable.noimagefound);

            }

            String weather = item.getWeather();
            int weatherIndex = Integer.parseInt(weather);
            setWeatherImage(weatherIndex);

            contentsTextView.setText(item.getContents());
            contentsTextView2.setText(item.getContents());

            locationTextView.setText(item.getAddress());
            locationTextView2.setText(item.getAddress());

            dateTextView.setText(item.getCreateDateStr());
            dateTextView2.setText(item.getCreateDateStr());
        }

        public void setMoodImage(int moodIndex) {
            switch(moodIndex) {
                case 0:
                    moodImageView.setImageResource(R.drawable.smile1_48);
                    moodImageView2.setImageResource(R.drawable.smile1_48);
                    break;
                case 1:
                    moodImageView.setImageResource(R.drawable.smile2_48);
                    moodImageView2.setImageResource(R.drawable.smile2_48);
                    break;
                case 2:
                    moodImageView.setImageResource(R.drawable.smile3_48);
                    moodImageView2.setImageResource(R.drawable.smile3_48);
                    break;
                case 3:
                    moodImageView.setImageResource(R.drawable.smile4_48);
                    moodImageView2.setImageResource(R.drawable.smile4_48);
                    break;
                case 4:
                    moodImageView.setImageResource(R.drawable.smile5_48);
                    moodImageView2.setImageResource(R.drawable.smile5_48);
                    break;
                default:
                    moodImageView.setImageResource(R.drawable.smile3_48);
                    moodImageView2.setImageResource(R.drawable.smile3_48);
                    break;
            }
        }

        public void setWeatherImage(int weatherIndex) {
            switch(weatherIndex) {
                case 0:
                    weatherImageView.setImageResource(R.drawable.weather_icon_1);
                    weatherImageView2.setImageResource(R.drawable.weather_icon_1);
                    break;
                case 1:
                    weatherImageView.setImageResource(R.drawable.weather_icon_2);
                    weatherImageView2.setImageResource(R.drawable.weather_icon_2);
                    break;
                case 2:
                    weatherImageView.setImageResource(R.drawable.weather_icon_3);
                    weatherImageView2.setImageResource(R.drawable.weather_icon_3);
                    break;
                case 3:
                    weatherImageView.setImageResource(R.drawable.weather_icon_4);
                    weatherImageView2.setImageResource(R.drawable.weather_icon_4);
                    break;
                case 4:
                    weatherImageView.setImageResource(R.drawable.weather_icon_5);
                    weatherImageView2.setImageResource(R.drawable.weather_icon_5);
                    break;
                case 5:
                    weatherImageView.setImageResource(R.drawable.weather_icon_6);
                    weatherImageView2.setImageResource(R.drawable.weather_icon_6);
                    break;
                case 6:
                    weatherImageView.setImageResource(R.drawable.weather_icon_7);
                    weatherImageView2.setImageResource(R.drawable.weather_icon_7);
                    break;
                default:
                    weatherImageView.setImageResource(R.drawable.weather_icon_1);
                    weatherImageView2.setImageResource(R.drawable.weather_icon_1);
                    break;
            }
        }

        public void setLayoutType(int layoutType) {
            if (layoutType == 0) {
                layout1.setVisibility(View.VISIBLE);
                layout2.setVisibility(View.GONE);
            } else if (layoutType == 1) {
                layout1.setVisibility(View.GONE);
                layout2.setVisibility(View.VISIBLE);
            }
        }

    }

}