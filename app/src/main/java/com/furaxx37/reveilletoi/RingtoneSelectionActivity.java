package com.furaxx37.reveilletoi;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RingtoneSelectionActivity extends AppCompatActivity implements RingtoneAdapter.OnRingtoneClickListener {
    
    private static final String TAG = "RingtoneSelection";
    private static final int PERMISSION_REQUEST_READ_MEDIA_AUDIO = 1001;
    public static final String EXTRA_SELECTED_RINGTONE_URI = "selected_ringtone_uri";
    public static final String EXTRA_CURRENT_RINGTONE_URI = "current_ringtone_uri";
    
    private RecyclerView recyclerView;
    private Button btnSelectRingtone;
    private RingtoneAdapter adapter;
    private List<RingtoneItem> ringtones;
    private String selectedRingtoneUri;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtone_selection);
        
        initializeViews();
        setupRecyclerView();
        setupListeners();
        
        // Check and request permissions before loading ringtones
        if (checkAudioPermission()) {
            loadRingtones();
        } else {
            requestAudioPermission();
        }
        
        // Get current ringtone URI from intent
        String currentRingtoneUri = getIntent().getStringExtra(EXTRA_CURRENT_RINGTONE_URI);
        if (currentRingtoneUri != null) {
            selectedRingtoneUri = currentRingtoneUri;
        }
    }

    private boolean checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) 
                   == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                   == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 
                PERMISSION_REQUEST_READ_MEDIA_AUDIO);
        } else {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                PERMISSION_REQUEST_READ_MEDIA_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_READ_MEDIA_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, load ringtones
                loadRingtones();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission required to access ringtones", Toast.LENGTH_LONG).show();
                // Load default ringtones or show empty list
                ringtones = new ArrayList<>();
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_ringtones);
        btnSelectRingtone = findViewById(R.id.btn_select_ringtone);
        
        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Select Ringtone");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        ringtones = new ArrayList<>();
        adapter = new RingtoneAdapter(ringtones, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadRingtones() {
        try {
            // Get system ringtones
            RingtoneManager manager = new RingtoneManager(this);
            manager.setType(RingtoneManager.TYPE_ALARM | RingtoneManager.TYPE_RINGTONE);
            
            Cursor cursor = manager.getCursor();
            
            // Add default ringtone option
            ringtones.add(new RingtoneItem("Default Alarm", 
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString(), 
                true));
            
            // Add system ringtones
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                    String uriString = manager.getRingtoneUri(cursor.getPosition()).toString();
                    
                    ringtones.add(new RingtoneItem(title, uriString, false));
                    
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
            // Add silent option
            ringtones.add(new RingtoneItem("Silent", "", false));
            
            adapter.notifyDataSetChanged();
            
            // Select current ringtone if available
            if (selectedRingtoneUri != null) {
                selectRingtoneByUri(selectedRingtoneUri);
            } else {
                // Select default if no current selection
                adapter.setSelectedPosition(0);
                selectedRingtoneUri = ringtones.get(0).getUri();
            }
            
            Log.d(TAG, "Loaded " + ringtones.size() + " ringtones");
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading ringtones", e);
            Toast.makeText(this, "Error loading ringtones", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectRingtoneByUri(String uri) {
        for (int i = 0; i < ringtones.size(); i++) {
            if (ringtones.get(i).getUri().equals(uri)) {
                adapter.setSelectedPosition(i);
                recyclerView.scrollToPosition(i);
                break;
            }
        }
    }

    private void setupListeners() {
        btnSelectRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRingtone();
            }
        });
    }

    private void selectRingtone() {
        if (selectedRingtoneUri != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_SELECTED_RINGTONE_URI, selectedRingtoneUri);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Please select a ringtone", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRingtoneClick(RingtoneItem ringtone, int position) {
        selectedRingtoneUri = ringtone.getUri();
        adapter.setSelectedPosition(position);
        
        // Stop any currently playing ringtone
        stopCurrentRingtone();
        
        // Play the selected ringtone (if not silent)
        if (!ringtone.getUri().isEmpty()) {
            playRingtone(ringtone.getUri());
        }
    }

    private void playRingtone(String uriString) {
        try {
            stopCurrentRingtone();
            
            Uri uri = Uri.parse(uriString);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.setLooping(false);
            mediaPlayer.prepare();
            mediaPlayer.start();
            
            // Stop after 3 seconds
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopCurrentRingtone();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error playing ringtone", e);
            Toast.makeText(this, "Cannot play this ringtone", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopCurrentRingtone() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping ringtone", e);
            } finally {
                mediaPlayer = null;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCurrentRingtone();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCurrentRingtone();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        stopCurrentRingtone();
        super.onBackPressed();
    }

}
