package com.example.visionnotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private MaterialToolbar toolbar;
    private NotesAdapter adapter;
    private ArrayList<NoteModel> noteList;

    private boolean currentThemeDark;
    private String userId;
    private DatabaseReference notesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentThemeDark = PrefsHelper.isDarkTheme(this);
        setTheme(currentThemeDark ? R.style.Theme_Notes : R.style.Theme_Notes);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Notes");
        toolbar.setNavigationIcon(R.drawable.ic_notes);
        toolbar.setNavigationOnClickListener(v -> loadNotes());

        fab.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, AddNoteActivity.class)));

        // Firebase references
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notesRef = FirebaseDatabase.getInstance().getReference("notes").child(userId);

        noteList = new ArrayList<>();
        adapter = new NotesAdapter(this, noteList);
        recyclerView.setAdapter(adapter);

        loadNotes();
    }

    private void loadNotes() {
        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                noteList.clear();
                for (DataSnapshot noteSnap : snapshot.getChildren()) {
                    NoteModel note = noteSnap.getValue(NoteModel.class);
                    if (note != null) noteList.add(note);
                }
                // Sort by modifiedAt descending
                noteList.sort((n1, n2) -> Long.compare(n2.getModifiedAt(), n1.getModifiedAt()));
                adapter.updateList(noteList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load notes ❌", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Menu setup
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        getMenuInflater().inflate(R.menu.top_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setQueryHint("Search notes...");
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if(adapter != null) adapter.filter(query);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if(adapter != null) adapter.filter(newText);
                        return false;
                    }
                });
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menu_settings){
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            return true;
        }

        if(id == R.id.menu_logout){
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        intent.putExtra("fromLogout", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}