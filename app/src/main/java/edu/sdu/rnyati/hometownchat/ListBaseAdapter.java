package edu.sdu.rnyati.hometownchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raghavnyati on 4/2/17.
 */

public class ListBaseAdapter extends BaseAdapter {
    private static ArrayList<UserDetails> searchArrayList;

    private LayoutInflater mInflater;
    private Context context;

    public ListBaseAdapter(Context context, ArrayList<UserDetails> results) {
        searchArrayList = results;
        this.context= context;
        mInflater = LayoutInflater.from(context);
    }

    public void addListItemToAdapter (List<UserDetails> list){
        searchArrayList.addAll(list);
    }

    public int getCount() {
        return searchArrayList.size();
    }

    public Object getItem(int position) {
        return searchArrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.custom_row_view, null);
            holder = new ViewHolder();
            holder.txtNickname = (TextView) convertView.findViewById(R.id.nickName);
            holder.txtCountry = (TextView) convertView
                    .findViewById(R.id.country);

            holder.chatButton = (Button) convertView.findViewById(R.id.chat);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtNickname.setText(searchArrayList.get(position).getId() + ":  " + searchArrayList.get(position).getNickName() + " - " + searchArrayList.get(position).getYear());
        holder.txtCountry.setText(searchArrayList.get(position).getCountry() + ", " +searchArrayList.get(position).getState()
                + " " + searchArrayList.get(position).getCity());

        final Bundle data = new Bundle();
        data.putString("nickname", holder.txtNickname.getText().toString());
        holder.chatButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(context,ChatMessageActivity.class);
                intent.putExtras(data);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView txtNickname;
        TextView txtCountry;
        Button chatButton;
    }
}
