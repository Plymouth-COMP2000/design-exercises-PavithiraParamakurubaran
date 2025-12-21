package com.pavithira.dinevista;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationActivity extends BaseActivity {

    private LinearLayout notificationsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        setupBottomNavigation(R.id.nav_settings); // Or whichever menu item
        setupDrawer();

        notificationsContainer = findViewById(R.id.notificationsContainer);

        loadNotifications();
    }

    private void loadNotifications() {
        String customId = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("CUSTOM_ID", "guest");

        notificationsContainer.removeAllViews();

        SQLiteDatabase db = new DatabaseHelper(this).getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM notifications WHERE custom_id=? ORDER BY timestamp DESC",
                new String[]{customId}
        );

        if (cursor.getCount() == 0) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No notifications yet.");
            emptyText.setTextSize(16);
            emptyText.setPadding(16, 16, 16, 16);
            notificationsContainer.addView(emptyText);
        } else {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                int read = cursor.getInt(cursor.getColumnIndexOrThrow("read"));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));

                View item = getLayoutInflater().inflate(R.layout.item_notification, notificationsContainer, false);
                TextView tvTitle = item.findViewById(R.id.tvTitle);
                TextView tvMessage = item.findViewById(R.id.tvMessage);

                tvTitle.setText(title);
                tvMessage.setText(message);

                // Highlight unread notifications
                if (read == 0) {
                    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvMessage.setTypeface(null, android.graphics.Typeface.BOLD);
                }

                notificationsContainer.addView(item);

                // Mark as read in the database
                ContentValues cv = new ContentValues();
                cv.put("read", 1);
                db.update(DatabaseHelper.TABLE_NOTIFICATIONS, cv, "custom_id=? AND timestamp=?",
                        new String[]{customId, String.valueOf(timestamp)});
            }
        }

        cursor.close();
        db.close();
    }
}
