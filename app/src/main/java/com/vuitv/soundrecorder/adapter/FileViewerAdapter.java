package com.vuitv.soundrecorder.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vuitv.soundrecorder.DBHelper;
import com.vuitv.soundrecorder.R;
import com.vuitv.soundrecorder.RecordingItem;
import com.vuitv.soundrecorder.fragment.PlayFragment;
import com.vuitv.soundrecorder.listener.OnDatabaseChangedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by vuitv on 10/10/2018.
 */

public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder>
        implements OnDatabaseChangedListener {

    private static final String LOG_TAG = "FileViewerAdapter";

    private DBHelper database;
    private RecordingItem item;
    private Context mContext;
    private LinearLayoutManager mLayoutManager;

    public FileViewerAdapter(Context context, LinearLayoutManager layoutManager) {
        mContext = context;
        database = new DBHelper(mContext);
        database.setOnDatabaseChangedListener(this);
        mLayoutManager = layoutManager;
    }

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view, parent, false);
        mContext = parent.getContext();
        return new RecordingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, int position) {
        item = getItem(position);
        long duration = item.getLength();
        long min = TimeUnit.MILLISECONDS.toMinutes(duration);
        long sec = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(min);

        holder.name.setText(item.getName());
        holder.length.setText(String.format("%02d:%02d", min, sec));
        holder.dateAdd.setText(DateUtils.formatDateTime(mContext, item.getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayFragment playFragment = new PlayFragment().newInstance(getItem(holder.getPosition()));

                FragmentTransaction transaction = ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction();

                playFragment.show(transaction, "dialog_play");
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ArrayList<String> entrys = new ArrayList<String>();
                entrys.add(mContext.getString(R.string.dialog_file_share));
                entrys.add(mContext.getString(R.string.dialog_file_rename));
                entrys.add(mContext.getString(R.string.dialog_file_delete));

                final CharSequence[] item = entrys.toArray(new CharSequence[entrys.size()]);

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.dialog_title_options));
                builder.setItems(item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            shareFileDialog(holder.getPosition());
                        }
                        if (which == 1) {
                            renameFileDialog(holder.getPosition());
                        } else if (which == 2) {
                            deleteFileDialog(holder.getPosition());
                        }
                    }
                });
                builder.setCancelable(true);
                builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return false;
            }
        });

    }

    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected TextView length;
        protected TextView dateAdd;
        protected View cardView;

        public RecordingsViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvFileName);
            length = itemView.findViewById(R.id.tvFileLength);
            dateAdd = itemView.findViewById(R.id.tvDateAdd);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }

    @Override
    public int getItemCount() {
        return database.getCount();
    }

    public RecordingItem getItem(int position) {
        return database.getItemAt(position);
    }

    @Override
    public void onNewDatabaseEntryAdded() {
        notifyItemInserted(getItemCount() - 1);
        mLayoutManager.scrollToPosition(getItemCount() - 1);
    }

    //TODO onDatabaseEntryRenamed
    @Override
    public void onDatabaseEntryRenamed() {

    }

    public void remove(int position) {
        File file = new File(getItem(position).getFilePath());
        file.delete();

        Toast.makeText(mContext, String.format(mContext.getString(R.string.toast_file_delete), getItem(position).getName()), Toast.LENGTH_SHORT).show();

        database.removeItemWithId(getItem(position).getId());
        notifyItemRemoved(position);

    }

    public void removeOutOfApp(String filepath) {
        //TODO removeOutOfApp
    }

    public void rename(int position, String name) {
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/" + name;

        File file = new File(filepath);

        if (file.exists() && !file.isDirectory()) {
            Toast.makeText(mContext, String.format(mContext.getString(R.string.toast_file_exists), name), Toast.LENGTH_SHORT).show();
        } else {
            File oldFile1 = new File(getItem(position).getFilePath());
            oldFile1.renameTo(file);
            database.renameItem(getItem(position), name, filepath);
            notifyItemChanged(position);
        }

    }

    public void shareFileDialog(int position) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(mContext,"com.vuitv.fileprovider",new File(getItem(position).getFilePath())));
        intent.setType("audio/mp4");
        mContext.startActivity(Intent.createChooser(intent, mContext.getText(R.string.send_to)));
    }

    public void renameFileDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_rename_file, null);

        final EditText input = view.findViewById(R.id.new_name);

        builder.setTitle(mContext.getString(R.string.dialog_title_rename));
        builder.setCancelable(true);
        builder.setPositiveButton(mContext.getString(R.string.dialog_action_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim() + ".mp4";
                rename(position, newName);
                dialog.cancel();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void deleteFileDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(String.format(mContext.getString(R.string.dialog_text_delete), getItem(position).getName()));
        builder.setCancelable(true);
        builder.setPositiveButton(mContext.getString(R.string.dialog_action_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                remove(position);
                dialog.cancel();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.dialog_action_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
