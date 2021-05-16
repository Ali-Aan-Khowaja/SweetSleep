package com.softaan.sweetsleep;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StoredFileAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<StoredFile> storedFiles;

    public StoredFileAdapter(Context context, ArrayList<StoredFile> storedFiles) {
        this.context = context;
        this.storedFiles = storedFiles;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder itemHolder = (ViewHolder) holder;
        StoredFile storedFile = storedFiles.get(position);

        itemHolder.name.setText(storedFile.getName());

        itemHolder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowResults.class);
                intent.putExtra("File Name", storedFile.getName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (storedFiles != null)
            return storedFiles.size();
        else
            return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView name;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.view = view;
            this.name = view.findViewById(R.id.fileName);
        }
    }
}
