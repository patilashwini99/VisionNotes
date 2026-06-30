package com.example.visionnotes;

import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

import android.speech.RecognizerIntent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;

public class AddNoteActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private ImageView imagePreview;
    private MaterialToolbar toolbar;

    private FirebaseAuth auth;
    private DatabaseReference database;

    private String noteId = null;
    private Long reminderMillis = null;
    private String imagePath = null;

    private ActivityResultLauncher<Intent> cameraLauncher, galleryLauncher, pdfLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        imagePreview = findViewById(R.id.imagePreview);
        toolbar = findViewById(R.id.toolbar);

        // Firebase
        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference("notes").child(userId);

        // Edit mode
        if (getIntent().hasExtra("id")) {
            noteId = getIntent().getStringExtra("id");
            etTitle.setText(getIntent().getStringExtra("title"));
            etDescription.setText(getIntent().getStringExtra("description"));
            imagePath = getIntent().getStringExtra("imagePath");
            reminderMillis = getIntent().getLongExtra("reminderMillis", 0);
        }

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        initActivityLaunchers();
    }

    private void initActivityLaunchers() {
        // Camera
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        imagePreview.setImageBitmap(photo);
                        findViewById(R.id.imageCard).setVisibility(View.VISIBLE);
                        runOCR(photo);
                        imagePath = null; // captured image not saved path yet
                    }
                });

        // Gallery
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        try {
                            Uri uri = result.getData().getData();
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imagePreview.setImageBitmap(bitmap);
                            findViewById(R.id.imageCard).setVisibility(View.VISIBLE);
                            runOCR(bitmap);
                            imagePath = uri.toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image ❌", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // PDF
        pdfLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        extractPDFText(uri);
                        imagePath = uri.toString();
                    }
                });
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note_menu, menu);
        try {
            Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
            m.setAccessible(true);
            m.invoke(menu, true);
        } catch (Exception ignored) {}
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_save) saveNote();
        else if (id == R.id.menu_camera) openCamera();
        else if (id == R.id.menu_gallery) openGallery();
        else if (id == R.id.menu_pdf) openPDF();
        else if (id == R.id.menu_delete) deleteNote();
        else if (id == R.id.menu_reminder) setReminder();
        else if (id == R.id.menu_voice) startVoiceInput();
        return true;
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(result != null && !result.isEmpty())
                etDescription.append(" " + result.get(0));
        }
    }

    // Save or Update Note
    private void saveNote() {

        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        // 🔥 AUTO SMART TITLE (इथे add करायचं)
        if (title.isEmpty() && !desc.isEmpty()) {

            String[] stopWords = {"is","the","and","a","an","of","to","in","on","for","with","that","this","it"};

            String[] words = desc.toLowerCase().split(" ");
            StringBuilder sb = new StringBuilder();

            int count = 0;

            for (String word : words) {

                boolean isStopWord = false;

                for (String sw : stopWords) {
                    if (word.equals(sw)) {
                        isStopWord = true;
                        break;
                    }
                }

                if (!isStopWord && word.length() > 3) {
                    sb.append(word).append(" ");
                    count++;
                }

                if (count == 4) break;
            }

            title = sb.toString().trim();

            // fallback
            if (title.isEmpty()) {
                title = desc.substring(0, Math.min(25, desc.length()));
            }

            etTitle.setText(title);
        }


        if (title.isEmpty() && desc.isEmpty()) {
            Toast.makeText(this, "Add title or description", Toast.LENGTH_SHORT).show();
            return;
        }


        if (reminderMillis == null) reminderMillis = 0L;

        long currentMillis = System.currentTimeMillis();

        NoteModel note = new NoteModel(title, desc, reminderMillis, imagePath);
        note.setModifiedAt(currentMillis);

        DatabaseReference userNotesRef = FirebaseDatabase.getInstance()
                .getReference("notes")
                .child(auth.getCurrentUser().getUid());

        if (noteId == null) {
            DatabaseReference newNoteRef = userNotesRef.push();
            noteId = newNoteRef.getKey();
            note.setId(noteId);

            newNoteRef.setValue(note)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Note saved ✅", Toast.LENGTH_SHORT).show();
                        if (reminderMillis > 0) scheduleReminder(reminderMillis);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Save failed ❌", Toast.LENGTH_SHORT).show());

        } else {
            note.setId(noteId);
            userNotesRef.child(noteId).setValue(note)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Note updated ✅", Toast.LENGTH_SHORT).show();
                        if (reminderMillis > 0) scheduleReminder(reminderMillis);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed ❌", Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteNote() {
        if(noteId != null){
            FirebaseDatabase.getInstance()
                    .getReference("notes")
                    .child(auth.getCurrentUser().getUid())
                    .child(noteId)
                    .removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Deleted ❌", Toast.LENGTH_SHORT).show());
        }
    }

    private void openCamera() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
            return;
        }
        cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
    }

    private void openGallery() {
        galleryLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
    }

    private void openPDF() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        pdfLauncher.launch(intent);
    }

    private void runOCR(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(text -> etDescription.setText(text.getText()))
                .addOnFailureListener(e -> Toast.makeText(this, "OCR Failed ❌", Toast.LENGTH_SHORT).show());
    }

    private void extractPDFText(Uri uri) {
        try {
            ParcelFileDescriptor fd = getContentResolver().openFileDescriptor(uri, "r");
            PdfRenderer renderer = new PdfRenderer(fd);
            for(int i = 0; i < renderer.getPageCount(); i++){
                PdfRenderer.Page page = renderer.openPage(i);
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                runOCR(bitmap);
                page.close();
            }
            renderer.close();
        } catch (Exception e){
            Toast.makeText(this, "PDF Error ❌", Toast.LENGTH_SHORT).show();
        }
    }

    private void setReminder() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                (view, year, month, dayOfMonth) -> new TimePickerDialog(this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        (timeView, hourOfDay, minute) -> {
                            Calendar cal = Calendar.getInstance();
                            cal.set(year, month, dayOfMonth, hourOfDay, minute);
                            reminderMillis = cal.getTimeInMillis();

                            Toast.makeText(this, "Reminder Set ⏰", Toast.LENGTH_SHORT).show();
                            scheduleReminder(reminderMillis);

                        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
                , c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void scheduleReminder(long time) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", etTitle.getText().toString());
        intent.putExtra("desc", etDescription.getText().toString());
        intent.putExtra("noteId", noteId);

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if(am != null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()){
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                return;
            }
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
        }
    }
}