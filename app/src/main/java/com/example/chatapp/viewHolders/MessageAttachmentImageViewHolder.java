package com.example.chatapp.viewHolders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.material.card.MaterialCardView;
import com.example.chatapp.R;
import com.example.chatapp.activities.ImageViewerActivity;
import com.example.chatapp.interfaces.OnMessageItemClick;
import com.example.chatapp.models.Message;
import com.example.chatapp.utils.Helper;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vanniktech.emoji.EmojiTextView;


/**
 * Created by mayank on 11/5/17.
 */

public class MessageAttachmentImageViewHolder extends BaseMessageViewHolder {
    ImageView image;
    LinearLayout ll;
    MaterialCardView cardView;
    EmojiTextView text;
    private static final String TAG = "MessageAttachmentImageV";

    public MessageAttachmentImageViewHolder(View itemView, OnMessageItemClick itemClickListener) {
        super(itemView, itemClickListener);
        image = itemView.findViewById(R.id.image);
        ll = itemView.findViewById(R.id.container);
        cardView = itemView.findViewById(R.id.card_view);
        text = itemView.findViewById(R.id.text);
        itemView.setOnClickListener(v -> onItemClick(true));

        itemView.setOnLongClickListener(v -> {
            onItemClick(false);
            return true;
        });
    }

    @Override
    public void setData(final Message message, int position) {
        super.setData(message, position);

        if (isMine()) {
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyMyMessage));
            ll.setBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyMyMessage));


        } else {

            cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyNotMyMessage));
            ll.setBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyNotMyMessage));

        }
//        cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorPrimary : R.color.colorBgLight));
//        ll.setBackgroundColor(message.isSelected() ? ContextCompat.getColor(context, R.color.colorPrimary) : isMine() ? Color.WHITE : ContextCompat.getColor(context, R.color.colorBgLight));

       /* StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(message.getAttachment().getUrl());
        reference.getBytes(10*1024*1024).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
            roundedBitmapDrawable.setCornerRadius(20);
            image.setImageBitmap(roundedBitmapDrawable.getBitmap());
        });*/
        Glide.with(context).load(message.getAttachment().getUrl())
                .transform()
                .apply(new RequestOptions().placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL).transform(new CenterCrop(), new RoundedCorners(25)))
                .into(image);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Helper.CHAT_CAB)
                    context.startActivity(ImageViewerActivity.newUrlInstance(context, message.getAttachment().getUrl()));
            }
        });

    }
}
