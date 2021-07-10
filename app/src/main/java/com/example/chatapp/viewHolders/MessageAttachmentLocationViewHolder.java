package com.example.chatapp.viewHolders;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.example.chatapp.R;
import com.example.chatapp.interfaces.OnMessageItemClick;
import com.example.chatapp.models.Message;
import com.example.chatapp.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageAttachmentLocationViewHolder extends BaseMessageViewHolder {
    private final TextView text;
    private final ImageView locationImage;
    private final LinearLayout ll;
    MaterialCardView cardView;
    private static final String TAG = "MessageAttachmentLocati";

    private final String staticMap = "https://maps.googleapis.com/maps/api/staticmap?center=%s,%s&zoom=18&size=512x350&format=jpg";
    private String latitude, longitude, place, placeName;

    public MessageAttachmentLocationViewHolder(View itemView, OnMessageItemClick itemClickListener) {
        super(itemView, itemClickListener);
        text = itemView.findViewById(R.id.text);
        locationImage = itemView.findViewById(R.id.locationImage);
        ll = itemView.findViewById(R.id.container);
        cardView = itemView.findViewById(R.id.card_view);
        itemView.setOnClickListener(v -> onItemClick(true));

        itemView.setOnLongClickListener(v -> {
            onItemClick(false);
            return true;
        });

        locationImage.setOnClickListener(v -> {

            if (!Helper.CHAT_CAB && !TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + latitude + "," + longitude + "(" + placeName + " , " + place + ")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                }
            }
        });

        // under onlick upper
//                Builder builder = new Uri.Builder();
//                builder.scheme("https")
//                        .authority("www.google.com")
//                        .appendPath("maps")
//                        .appendPath("dir")
//                        .appendPath("")
//                        .appendQueryParameter("api", "1")
//                        .appendQueryParameter("destination", 80.00023 + "," + 13.0783);
//                String url = builder.build().toString();
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                context.startActivity(i);
    }

    @Override
    public void setData(Message message, int position) {
        super.setData(message, position);
        if (isMine()) {
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyMyMessage));
            ll.setBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyMyMessage));


        } else {
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyNotMyMessage));
            ll.setBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyNotMyMessage));

        }
        try {
            JSONObject placeData = new JSONObject(message.getAttachment().getData());
            place = placeData.getString("address");
            placeName = placeData.getString("addressName");
            latitude = placeData.getString("latitude");
            longitude = placeData.getString("longitude");

            text.setText(place);

//            String link = String.format(staticMap + "&key=" + context.getResources().getString(R.string.geo_api_key + "&markers=color:red|label:Y|%s,%s&markers=color:red", latitude, longitude, latitude, longitude);
            String link = String.format(staticMap + "&key=" + "AIzaSyA7dDjOqEW3XO1X_7AmMgiXixF78l3eNgs" + "&markers=color:red|label:Y|%s,%s&markers=color:red", latitude, longitude, latitude, longitude);
//            Log.d(TAG, "setData location: " + link);
//            Glide.with(context).load(String.format(staticMap + "&key=" + context.getResources().getString(R.string.geo_api_key) + "&markers=color:red|label:Y|%s,%s&markers=color:red", latitude, longitude, latitude, longitude)).into(locationImage);
            if(isMine())
            {
                text.setTextColor(context.getResources().getColor(R.color.quantum_white_100));
                Glide.with(context)
                        .load(String.format(staticMap + "&key=" + "AIzaSyA7dDjOqEW3XO1X_7AmMgiXixF78l3eNgs" + "&markers=color:red|label:Y|%s,%s&markers=color:red", latitude, longitude, latitude, longitude))
                        .placeholder(R.drawable.ic_location)
                        .into(locationImage);
            }
            else {
                int nightModeFlags =
                        context.getResources().getConfiguration().uiMode &
                                Configuration.UI_MODE_NIGHT_MASK;
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                    {
                        Glide.with(context)
                                .load(String.format(staticMap + "&key=" + "AIzaSyA7dDjOqEW3XO1X_7AmMgiXixF78l3eNgs" + "&markers=color:red|label:Y|%s,%s&markers=color:red", latitude, longitude, latitude, longitude))
                                .placeholder(R.drawable.ic_location_not_mine_dark)
                                .into(locationImage);
                    }
                        break;

                    case Configuration.UI_MODE_NIGHT_NO:
                    {
                        Glide.with(context)
                                .load(String.format(staticMap + "&key=" + "AIzaSyA7dDjOqEW3XO1X_7AmMgiXixF78l3eNgs" + "&markers=color:red|label:Y|%s,%s&markers=color:red", latitude, longitude, latitude, longitude))
                                .placeholder(R.drawable.ic_location_not_mine_light)
                                .into(locationImage);
                    }
                        break;

                    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    {
                        Glide.with(context)
                                .load(String.format(staticMap + "&key=" + "AIzaSyA7dDjOqEW3XO1X_7AmMgiXixF78l3eNgs" + "&markers=color:red|label:Y|%s,%s&markers=color:red", latitude, longitude, latitude, longitude))
                                .placeholder(R.drawable.ic_location_not_mine_dark)
                                .into(locationImage);
                    }
                        break;
                }
                /*{
                    Glide.with(context)
                            .load(String.format(staticMap + "&key=" + "AIzaSyA7dDjOqEW3XO1X_7AmMgiXixF78l3eNgs" + "&markers=color:red|label:Y|%s,%s&markers=color:red", latitude, longitude, latitude, longitude))
                            .placeholder(R.drawable.ic_location_not_mine_light)
                            .into(locationImage);
                }*/
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorPrimary : R.color.colorBgLight));
//        ll.setBackgroundColor(message.isSelected() ? ContextCompat.getColor(context, R.color.colorPrimary) : isMine() ? Color.WHITE : ContextCompat.getColor(context, R.color.colorBgLight));
    }
}
