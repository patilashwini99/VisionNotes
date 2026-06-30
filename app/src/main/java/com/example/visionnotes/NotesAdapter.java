package com.example.visionnotes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<NoteModel> noteList;
    private ArrayList<NoteModel> fullList;

    private String userId;
    private DatabaseReference notesRef;

    public NotesAdapter(Context context, ArrayList<NoteModel> noteList) {
        this.context = context;
        this.noteList = new ArrayList<>(noteList);
        this.fullList = new ArrayList<>(noteList);

        // Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notesRef = FirebaseDatabase.getInstance().getReference("notes").child(userId);
    }

    public void updateList(List<NoteModel> newList){
        noteList = new ArrayList<>(newList);
        fullList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NoteModel model = noteList.get(position);

        holder.title.setText(model.getTitle());
        holder.description.setVisibility(View.GONE);

        long modifiedMillis = model.getModifiedAt() != null ? model.getModifiedAt() : 0;
        if(modifiedMillis > 0){
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            holder.modifiedAt.setText("Saved: " + sdf.format(new Date(modifiedMillis)));
        } else {
            holder.modifiedAt.setText("");
        }

        // Click → Edit Note
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddNoteActivity.class);
            intent.putExtra("id", model.getId());
            intent.putExtra("title", model.getTitle());
            intent.putExtra("description", model.getDescription());
            intent.putExtra("imagePath", model.getImagePath());
            intent.putExtra("reminderMillis", model.getReminderTimeMillis());
            context.startActivity(intent);
        });

        // Long click → popup menu
        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.itemView);
            popupMenu.inflate(R.menu.note_menu);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_edit) {
                    Intent intent = new Intent(context, AddNoteActivity.class);
                    intent.putExtra("id", model.getId());
                    intent.putExtra("title", model.getTitle());
                    intent.putExtra("description", model.getDescription());
                    intent.putExtra("imagePath", model.getImagePath());
                    context.startActivity(intent);
                    return true;

                }else if (item.getItemId() == R.id.menu_delete) {
                    if(model.getId() != null && !model.getId().isEmpty()) {

                        // Show a toast immediately (optional)
                        Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show();

                        // Remove from Firebase
                        notesRef.child(model.getId()).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    // ✅ Remove from local list safely
                                    fullList.removeIf(note -> note.getId().equals(model.getId()));
                                    noteList.removeIf(note -> note.getId().equals(model.getId()));

                                    notifyDataSetChanged(); // safe because we rebuilt list
                                    Toast.makeText(context, "Deleted ✅", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Delete failed ❌", Toast.LENGTH_SHORT).show());
                    }
                    return true;
                }
                return false;
            });

            popupMenu.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return noteList == null ? 0 : noteList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, modifiedAt;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.noteTitle);
            description = itemView.findViewById(R.id.noteDescription);
            modifiedAt = itemView.findViewById(R.id.noteDate);
        }
    }

    public void filter(String text){
        if (noteList == null || fullList == null) return;

        noteList.clear();

        if(text == null || text.isEmpty()){
            noteList.addAll(fullList);
        } else {
            text = text.toLowerCase();
            for(NoteModel item : fullList){
                String title = item.getTitle() == null ? "" : item.getTitle();
                String desc  = item.getDescription() == null ? "" : item.getDescription();

                if(title.toLowerCase().contains(text) || desc.toLowerCase().contains(text)){
                    noteList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
}