package com.example.chatapp.viewHolders;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.example.chatapp.R;
import com.example.chatapp.interfaces.OnMessageItemClick;
import com.example.chatapp.models.Attachment;
import com.example.chatapp.models.AttachmentTypes;
import com.example.chatapp.models.Message;
import com.example.chatapp.utils.FileUtils;
import com.example.chatapp.utils.Helper;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by a_man on 02-02-2018.
 */

public class MessageAttachmentRecordingViewHolder extends BaseMessageViewHolder {
    TextView text;
    TextView durationOrSize;
    LinearLayout ll;
    ProgressBar progressBar;
    ImageView playPauseToggle;
    private Message message;
    private File file;
    MaterialCardView cardView;

    private final RecordingViewInteractor recordingViewInteractor;

    public MessageAttachmentRecordingViewHolder(View itemView, OnMessageItemClick itemClickListener, RecordingViewInteractor recordingViewInteractor) {
        super(itemView, itemClickListener);
        text = itemView.findViewById(R.id.text);
        durationOrSize = itemView.findViewById(R.id.duration);
        ll = itemView.findViewById(R.id.container);
        progressBar = itemView.findViewById(R.id.progressBar);
        cardView = itemView.findViewById(R.id.card_view);
        playPauseToggle = itemView.findViewById(R.id.playPauseToggle);

        this.recordingViewInteractor = recordingViewInteractor;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Helper.CHAT_CAB)
                    downloadFile();
                onItemClick(true);
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemClick(false);
                return true;
            }
        });
    }

    @Override
    public void setData(Message message, int position) {
        super.setData(message, position);

        if (isMine()) {
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyMyMessage));
            ll.setBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyMyMessage));


        } else {
            linearLayoutTest.setBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyNotMyMessage));
//            cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyNotMyMessage));
            ll.setBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorBgLight : R.color.messageBodyNotMyMessage));

        }
        this.message = message;
        Attachment attachment = message.getAttachment();

        boolean loading = message.getAttachment().getUrl().equals("loading");
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        playPauseToggle.setVisibility(loading ? View.GONE : View.VISIBLE);

        file = new File(Environment.getExternalStorageDirectory() + "/"
                +
                Environment.DIRECTORY_DOWNLOADS + "/" + AttachmentTypes.getTypeName(message.getAttachmentType())
                , message.getAttachment().getName());
        if (file.exists()) {
            Uri uri = Uri.fromFile(file);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(context, uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int millis = Integer.parseInt(durationStr);
            durationOrSize.setText(TimeUnit.MILLISECONDS.toMinutes(millis) + ":" + TimeUnit.MILLISECONDS.toSeconds(millis));
            mmr.release();
        } else
            durationOrSize.setText(FileUtils.getReadableFileSize(attachment.getBytesCount()));

        playPauseToggle.setImageDrawable(ContextCompat.getDrawable(context, file.exists() ? recordingViewInteractor.isRecordingPlaying(message.getAttachment().getName()) ? R.drawable.ic_stop : R.drawable.ic_play_circle_outline : R.drawable.ic_file_download_accent_36dp));
//        cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorPrimary : R.color.colorBgLight));
//        ll.setBackgroundColor(message.isSelected() ? ContextCompat.getColor(context, R.color.colorPrimary) : isMine() ? Color.WHITE : ContextCompat.getColor(context, R.color.colorBgLight));
    }

    //@OnClick(R.id.playPauseToggle)
    public void downloadFile() {
        if (file.exists()) {
            recordingViewInteractor.playRecording(file, message.getAttachment().getName(), getAdapterPosition());
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            Uri uri = MyFileProvider.getUriForFile(context,
//                    context.getString(R.string.authority),
//                    file);
//            intent.setDataAndType(uri, Helper.getMimeType(context, uri)); //storage path is path of your vcf file and vFile is name of that file.
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            context.startActivity(intent);
        } else if (!isMine() && !message.getAttachment().getUrl().equals("loading")) {
            broadcastDownloadEvent();
        } else {
            Toast.makeText(context, "File unavailable Honey", Toast.LENGTH_SHORT).show();
        }
    }

    public interface RecordingViewInteractor {
        boolean isRecordingPlaying(String fileName);

        void playRecording(File file, String fileName, int position);
    }

}
