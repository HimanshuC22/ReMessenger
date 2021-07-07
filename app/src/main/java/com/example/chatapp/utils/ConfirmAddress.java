package com.example.chatapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.chatapp.R;
import com.example.chatapp.models.Attachment;
import com.example.chatapp.models.AttachmentTypes;
import com.example.chatapp.models.Group;
import com.example.chatapp.models.User;

import org.json.JSONException;
import org.json.JSONObject;

public class ConfirmAddress extends DialogFragment implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button yes, no;

    private String chatChild, userOrGroupId;
    protected User userMe, user;
    protected Group group;

    public ConfirmAddress() {

    }

    Double Lat;
    Double Long;
    String Address;
    TextView myAddress;
    Button SelectBtn;
    Button ChangeBtn;
//    MapFragment mapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Lat = getArguments().getDouble("lat");
        Long = getArguments().getDouble("long");
        Address = getArguments().getString("address");
        chatChild = getArguments().getString("attachment_chat_child");
        userOrGroupId = getArguments().getString("attachment_recipient_id");
        user = getArguments().getParcelable("attachment_recipient_user");
        group = getArguments().getParcelable("attachment_recipient_group");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.location_fragment, container, false);
        myAddress = (TextView) v.findViewById(R.id.myAddress);
        SelectBtn = (Button) v.findViewById(R.id.Select);
        ChangeBtn = (Button) v.findViewById(R.id.Change);
        c = getActivity();
        myAddress.setText(Address);

//        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapp);
//        mapFragment.getMapAsync(this);
        // Toast.makeText(getActivity(),mNum,Toast.LENGTH_LONG).show();

        SelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                c.finish();
//                getFragmentManager().beginTransaction().remove(mapFragment).commit();
//                Intent intent = new Intent(c, ChatActivity.class);
//                intent.putExtra("address",Address);
//                intent.putExtra("addressName","Address");
//                intent.putExtra("latitude",Lat);
//                intent.putExtra("longitude",Long);
//                dismiss();
////                c.startActivityForResult(intent,7410);
//                c.finish();
                Toast.makeText(getActivity(), myAddress.getText().toString(), Toast.LENGTH_LONG).show();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("address", Address);
                    jsonObject.put("addressName", "Address");
                    jsonObject.put("latitude", Lat);
                    jsonObject.put("longitude", Long);
                    Attachment attachment = new Attachment();
                    attachment.setData(jsonObject.toString());
                    Intent intent2 = new Intent(Helper.SEND_LOCATION);
                    intent2.putExtra("attachment", attachment);
                    intent2.putExtra("attachment_type", AttachmentTypes.LOCATION);
                    intent2.putExtra("attachment_chat_child", chatChild);
                    intent2.putExtra("attachment_recipient_id", userOrGroupId);
                    intent2.putExtra("attachment_recipient_user", user);
                    intent2.putExtra("attachment_recipient_group", group);
                    LocalBroadcastManager.getInstance(c).sendBroadcast(intent2);
                    c.finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        ChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getFragmentManager().beginTransaction().remove(mapFragment).commit();
                dismiss();
            }
        });
        getDialog().setCanceledOnTouchOutside(true);
        return v;

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
//        getFragmentManager().beginTransaction().remove(mapFragment).commit();

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
//        dismiss();
    }

    @Override
    public void onClick(View v) {

    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
////        mMap = googleMap;
////        myAddress.setText(Address);
////        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
////        MarkerOptions markerOptions = new MarkerOptions();
////        markerOptions.position(new LatLng(Lat, Long));
////
////        markerOptions.title(Address);
////        mMap.clear();
////        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
////                new LatLng(Lat, Long), 16f);
////        mMap.animateCamera(location);
////        mMap.addMarker(markerOptions);
////        Log.d("status", "success");
//    }


}