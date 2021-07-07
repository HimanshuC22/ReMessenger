package com.example.chatapp.viewHolders;

import android.view.View;

import com.example.chatapp.models.AttachmentTypes;

/**
 * Created by a_man on 5/11/2017.
 */

public class MessageTypingViewHolder extends BaseMessageViewHolder {
    public MessageTypingViewHolder(View itemView) {
        super(itemView, AttachmentTypes.NONE_TYPING, null);
    }
}
