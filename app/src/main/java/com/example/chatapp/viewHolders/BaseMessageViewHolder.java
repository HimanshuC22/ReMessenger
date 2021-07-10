package com.example.chatapp.viewHolders;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.activities.ChatDetailActivity;
import com.example.chatapp.interfaces.OnMessageItemClick;
import com.example.chatapp.models.AttachmentTypes;
import com.example.chatapp.models.DownloadFileEvent;
import com.example.chatapp.models.Message;
import com.example.chatapp.models.User;
import com.example.chatapp.utils.GeneralUtils;
import com.example.chatapp.utils.Helper;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;
import static com.example.chatapp.adapters.MessageAdapter.OTHER;

/**
 * Created by mayank on 11/5/17.
 */

public class BaseMessageViewHolder extends RecyclerView.ViewHolder {
    protected static int lastPosition;
    public static boolean animate;
    protected static View newMessageView;
    private int attachmentType;
    protected Context context;
    ImageView left, right;
    LinearLayout linearC;
    LinearLayout linearLayoutTest;

    private static int _48dpInPx = -1;
    private Message message;
    private OnMessageItemClick itemClickListener;

    TextView time, senderName;
    CardView cardView;

    public BaseMessageViewHolder(View itemView, OnMessageItemClick itemClickListener) {
        super(itemView);
        if (itemClickListener != null)
            this.itemClickListener = itemClickListener;
        context = itemView.getContext();
        time = itemView.findViewById(R.id.time);
        senderName = itemView.findViewById(R.id.senderName);
        cardView = itemView.findViewById(R.id.card_view);
        linearLayoutTest = itemView.findViewById(R.id.linearTest);
        linearC = itemView.findViewById(R.id.linearColorLayout);
        left = itemView.findViewById(R.id.left_bubble);
        right = itemView.findViewById(R.id.right_bubble);
        if (_48dpInPx == -1) _48dpInPx = GeneralUtils.dpToPx(itemView.getContext(), 48);
    }

    public BaseMessageViewHolder(View itemView, int attachmentType, OnMessageItemClick itemClickListener) {
        super(itemView);
        this.itemClickListener = itemClickListener;
        this.attachmentType = attachmentType;
    }

    public BaseMessageViewHolder(View itemView, View newMessage, OnMessageItemClick itemClickListener) {
        this(itemView, itemClickListener);
        this.itemClickListener = itemClickListener;
        if (newMessageView == null) newMessageView = newMessage;
    }

    protected boolean isMine() {
        return (getItemViewType() & OTHER) != OTHER;
    }

    public void setData(Message message, int position) {
        this.message = message;
        StorageReference reference = FirebaseStorage.getInstance().getReference().child(context.getString(R.string.appName)).child("ProfileImage").child(message.getSenderId());
        reference.getBytes(9*1024*1024).addOnSuccessListener(bytes -> {
            Bitmap raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Bitmap raw1 = Bitmap.createScaledBitmap(raw, 80, 80, false);
            RoundedBitmapDrawable bitmap = RoundedBitmapDrawableFactory.create(context.getResources(), raw1);
            bitmap.setCircular(true);
            left.setImageDrawable(bitmap);
            right.setImageDrawable(bitmap);
        });
        if (attachmentType == AttachmentTypes.NONE_TYPING)
            return;

        String sName = getContactName(context, message.getSenderName());

        time.setText(Helper.getTime(message.getDate()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(isMine())
            time.setTextColor(context.getColor(R.color.colorSurface));
        }
        if (message.getRecipientId().startsWith(Helper.GROUP_PREFIX)) {
            senderName.setText(isMine() ? "You" : sName);
            senderName.setVisibility(View.VISIBLE);
        } else {
            senderName.setVisibility(View.GONE);
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) linearLayoutTest.getLayoutParams();
        if (isMine()) {
            layoutParams.gravity = Gravity.END;
            layoutParams.leftMargin = _48dpInPx;
//            linearC.setBackgroundColor(context.getResources().getColor(R.color.messageBodyMyMessage));
            right.setVisibility(View.VISIBLE);
            cardView.setCardBackgroundColor(context.getResources().getColor(R.color.messageBodyMyMessage));
            time.setCompoundDrawablesWithIntrinsicBounds(0, 0, message.isSent() ? (message.isDelivered() ? R.drawable.ic_done_all_black : R.drawable.ic_done_black) : R.drawable.ic_waiting, 0);
        } else {
            left.setVisibility(View.VISIBLE);
            layoutParams.gravity = Gravity.START;
            layoutParams.rightMargin = _48dpInPx;
            cardView.setCardBackgroundColor(context.getResources().getColor(R.color.messageBodyNotMyMessage));
            //itemView.startAnimation(AnimationUtils.makeInAnimation(itemView.getContext(), true));
        }
        linearLayoutTest.setLayoutParams(layoutParams);

        senderName.setOnClickListener(view -> {

            User user = new User(message.getSenderId(), message.getSenderName(), message.getSenderStatus(), message.getSenderImage());
            Intent intent = new Intent(context, ChatDetailActivity.class);
            intent.putExtra("extradatauser", user);
            context.startActivity(intent);

        });

    }

    void onItemClick(boolean b) {
        if (itemClickListener != null && message != null) {
            if (b)
                itemClickListener.OnMessageClick(message, getAdapterPosition());
            else
                itemClickListener.OnMessageLongClick(message, getAdapterPosition());
        }
    }

    private String getContactName(Context context, String number) {

        String name = null;

        // define the columns I want the query to return
        String[] projection = new String[]{
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup._ID};

        // encode the phone number and build the filter URI
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        // query time
        Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                Log.v(TAG, "Started uploadcontactphoto: Contact Found @ " + number);
                Log.v(TAG, "Started uploadcontactphoto: Contact name  = " + name);
            } else {
                Log.v(TAG, "Contact Not Found @ " + number);
            }
            cursor.close();
        }

        if (name == null || name == "") {
            name = number;
        }

        return name;
    }

    void broadcastDownloadEvent(DownloadFileEvent downloadFileEvent) {
        Intent intent = new Intent(Helper.BROADCAST_DOWNLOAD_EVENT);
        intent.putExtra("data", downloadFileEvent);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    void broadcastDownloadEvent() {
        Intent intent = new Intent(Helper.BROADCAST_DOWNLOAD_EVENT);
        intent.putExtra("data", new DownloadFileEvent(message.getAttachmentType(), message.getAttachment(), getAdapterPosition()));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
