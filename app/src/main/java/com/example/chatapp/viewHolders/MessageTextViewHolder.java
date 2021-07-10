package com.example.chatapp.viewHolders;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.example.chatapp.R;
import com.example.chatapp.interfaces.OnMessageItemClick;
import com.example.chatapp.models.Message;
import com.example.chatapp.utils.GeneralUtils;
import com.example.chatapp.utils.LinkTransformationMethod;
import com.vanniktech.emoji.EmojiTextView;

import static com.example.chatapp.adapters.MessageAdapter.MY;

/**
 * Created by mayank on 11/5/17.
 */

public class MessageTextViewHolder extends BaseMessageViewHolder {
    EmojiTextView text;
    LinearLayout ll;
    MaterialCardView cardView;

    private Message message;

    private static int _4dpInPx = -1;

    public MessageTextViewHolder(View itemView, View newMessageView, OnMessageItemClick itemClickListener) {
        super(itemView, newMessageView, itemClickListener);
        text = itemView.findViewById(R.id.text);
        ll = itemView.findViewById(R.id.container);
        cardView = itemView.findViewById(R.id.card_view);

        text.setTransformationMethod(new LinkTransformationMethod());
        text.setMovementMethod(LinkMovementMethod.getInstance());
        if (_4dpInPx == -1) _4dpInPx = GeneralUtils.dpToPx(itemView.getContext(), 4);
        itemView.setOnClickListener(v -> onItemClick(true));

        itemView.setOnLongClickListener(v -> {
            onItemClick(false);
            return true;
        });
    }

    @Override
    public void setData(Message message, int position) {
        super.setData(message, position);

        if (isMine()) {
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyMyMessage));
            ll.setBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyMyMessage));


        } else {
            text.setTextColor(ContextCompat.getColor(context, R.color.quantum_black_100));
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyNotMyMessage));
            ll.setBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyNotMyMessage));

        }
//        ll.setBackgroundColor(message.isSelected() ? ContextCompat.getColor(context, R.color.colorOnSurface) : isMine() ? Color.WHITE : ContextCompat.getColor(context, R.color.colorBgLight));
        text.setText(message.getBody());
        if (getItemViewType() == MY) {
            animateView(position);
        }
    }

    private void animateView(int position) {
        if (animate && position > lastPosition) {

            itemView.post(new Runnable() {
                @Override
                public void run() {

                    float originalX = cardView.getX();
                    final float originalY = itemView.getY();
                    int[] loc = new int[2];
                    newMessageView.getLocationOnScreen(loc);
                    cardView.setX(loc[0] / 2);
                    itemView.setY(loc[1]);
                    ValueAnimator radiusAnimator = new ValueAnimator();
                    radiusAnimator.setFloatValues(80, _4dpInPx);
                    radiusAnimator.setDuration(850);
                    radiusAnimator.addUpdateListener(animation -> cardView.setRadius((Float) animation.getAnimatedValue()));
                    radiusAnimator.start();
                    cardView.animate().x(originalX).setDuration(900).setInterpolator(new DecelerateInterpolator()).start();
                    itemView.animate().y(originalY - _4dpInPx).setDuration(750).setInterpolator(new DecelerateInterpolator()).start();
                    new Handler().postDelayed(() -> itemView.animate().y(originalY + _4dpInPx).setDuration(250).setInterpolator(new DecelerateInterpolator()).start(), 750);
                }
            });
            lastPosition = position;
//            setAnimation(getAdapterPosition());
            animate = false;
        }
    }

}
