package com.example.myapplication.barcode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProductDetailsActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private int notificationYear;
    private int notificationMonth;
    private int notificationDayOfMonth;
    private int notificationHour;
    private int notificationMinute;

    private ImageView productImageView;
    private TextView productNameTextView;
    private Button chooseDateButton;
    private Button chooseTimeButton;
    private String productName;
    private String barcode;
    private DatePickerDialog datePickerDialog;
    private boolean isExpiryDateSet = false;
    private static final String CHANNEL_ID = "product_expiry_notification";
    private boolean addedManually = false;
    private String currentPhotoPath;
    private static final int IMAGE_WIDTH_DP = 380;
    private static final int IMAGE_HEIGHT_DP = 380;
    public static final int CAMERA_ACTION_CODE = 1;
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        imageView = findViewById(R.id.productImageView);
        productNameTextView = findViewById(R.id.productNameTextView);
        chooseDateButton = findViewById(R.id.ExpiryButton);
        chooseTimeButton = findViewById(R.id.NotifPickerButton);

        imageView.setBackgroundResource(R.drawable.rounded_border);

        productName = getIntent().getStringExtra("productName");
        barcode = getIntent().getStringExtra("barcode");

        productNameTextView.setText(productName);

        loadImageFromSharedPreferences(barcode);

        retrieveFromSharedPreferences();

        new RetrieveProductDetailsTask().execute(barcode);

        chooseTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isExpiryDateSet) {
                    Toast.makeText(ProductDetailsActivity.this, "Set Expiry Date First", Toast.LENGTH_SHORT).show();
                } else {
                    openDatePickerForNotification();
                }
            }
        });

        initDatePicker();

        chooseDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseExpiryDate();
            }
        });

        createNotificationChannel();
    }

    public void captureImage(View view){
        // Check if the permission is not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            String imagePath = saveImageToGallery(imageBitmap);
            saveImagePathToSharedPreferences(imagePath, barcode);
        }
    }

    // Load image from SharedPreferences
    private void loadImageFromSharedPreferences(String barcode) {
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String imagePath = sharedPref.getString("imagePath_" + barcode, "");
        if (!imagePath.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                // Handle case when bitmap is null
            }
        } else {
            // Handle case when imagePath is empty
        }
    }

    private void saveImagePathToSharedPreferences(String imagePath, String barcode) {
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("imagePath_" + barcode, imagePath); // Use barcode in the key
        editor.apply();
    }

    private String saveImageToGallery(Bitmap imageBitmap) {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + barcode + ".jpg";
        File imageFile = new File(storageDir, fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(imageFile));
            sendBroadcast(mediaScanIntent);
            Toast.makeText(this, "Image Saved Successfully", Toast.LENGTH_LONG).show();
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void chooseExpiryDate() {
        datePickerDialog.show();
    }

    private class RetrieveProductDetailsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://api.barcodelookup.com/v3/products?barcode=" + params[0] + "&formatted=y&key=unvcpksu94xiae621joo7w3hn1yyrd");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = connection.getInputStream();
                StringBuilder response = new StringBuilder();
                int data;
                while ((data = inputStream.read()) != -1) {
                    response.append((char) data);
                }
                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray productsArray = jsonObject.getJSONArray("products");
                    if (productsArray.length() > 0) {
                        JSONObject product = productsArray.getJSONObject(0);
                        JSONArray imagesArray = product.getJSONArray("images");
                        if (imagesArray.length() > 0) {
                            String imageUrl = imagesArray.getString(0);
                            new DownloadImageTask().execute(imageUrl);
                        } else {
                            Toast.makeText(ProductDetailsActivity.this, "Image not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProductDetailsActivity.this, "Product details not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ProductDetailsActivity.this, "Failed to parse JSON", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProductDetailsActivity.this, "Failed to retrieve product details", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                String imagePath = saveImageToGallery(bitmap);
                saveImagePathToSharedPreferences(imagePath, barcode);
            } else {
                Log.e("ImageDownload", "Failed to load image");
                Toast.makeText(ProductDetailsActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
    }

    private void setNotificationDate(int year, int month, int dayOfMonth) {
        notificationYear = year;
        notificationMonth = month;
        notificationDayOfMonth = dayOfMonth;
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void chooseDate(View view) {
        datePickerDialog.show();
    }

    private final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
            month += 1;
            String date = dayOfMonth + "/" + month + "/" + year;
            chooseDateButton.setText(date);
            setNotificationDate(year, month, dayOfMonth);
            isExpiryDateSet = true;
            saveExpiryDateToSharedPreferences(barcode, date);
            scheduleExpiryNotification();
            Calendar currentCalendar = Calendar.getInstance();
            Calendar expiryCalendar = Calendar.getInstance();
            expiryCalendar.set(year, month - 1, dayOfMonth);

            long currentTimeMillis = currentCalendar.getTimeInMillis();
            long expiryTimeMillis = expiryCalendar.getTimeInMillis();
            long timeDifferenceMillis = expiryTimeMillis - currentTimeMillis;

            if (timeDifferenceMillis > 0) {
                long millisecondsPerDay = 24 * 60 * 60 * 1000;
                long daysDifference = timeDifferenceMillis / millisecondsPerDay;
                long yearsDifference = daysDifference / 365;
                long remainingDays = daysDifference % 365;
                long monthsDifference = remainingDays / 30;
                remainingDays %= 30;

                StringBuilder differenceTextBuilder = new StringBuilder();

                if (yearsDifference > 0) {
                    differenceTextBuilder.append(yearsDifference).append(" Year");
                    if (yearsDifference > 1) differenceTextBuilder.append("s");
                    if (monthsDifference > 0 || remainingDays > 0) differenceTextBuilder.append(", ");
                    differenceTextBuilder.append(" left");
                }
                if (monthsDifference > 0) {
                    differenceTextBuilder.append(monthsDifference).append(" Month");
                    if (monthsDifference > 1) differenceTextBuilder.append("s");
                    if (remainingDays > 0) differenceTextBuilder.append(", ");
                    differenceTextBuilder.append(" left");
                }
                if (remainingDays > 0) {
                    differenceTextBuilder.append(remainingDays).append(" Day");
                    if (remainingDays > 1) differenceTextBuilder.append("s");
                    differenceTextBuilder.append(" left");
                }



                String differenceText = differenceTextBuilder.toString();

                TextView differenceTextView = findViewById(R.id.differenceTextView);
                differenceTextView.setText(differenceText);
            } else {
                TextView differenceTextView = findViewById(R.id.differenceTextView);
                differenceTextView.setText("");
            }
        }
    };


    private void openDatePickerForNotification() {
        Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, notificationDateSetListener, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    private final DatePickerDialog.OnDateSetListener notificationDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
            month += 1;
            String date = dayOfMonth + "/" + month + "/" + year;

            if (year > notificationYear || (year == notificationYear && month > notificationMonth) || (year == notificationYear && month == notificationMonth && dayOfMonth >= notificationDayOfMonth)) {
                Toast.makeText(ProductDetailsActivity.this, "Notification date needs to be before the expiry date", Toast.LENGTH_SHORT).show();
                openDatePickerForNotification();
            } else {
                setNotificationDate(year, month, dayOfMonth);
                openTimePickerForNotification();
                chooseTimeButton.setText(date);
            }
        }
    };

    private void openTimePickerForNotification() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                setNotificationTime(hourOfDay, minute);
                chooseTimeButton.setText(notificationDayOfMonth + "/" + notificationMonth + "/" + notificationYear + "\n" + String.format("%02d:%02d", notificationHour, notificationMinute));
                saveNotificationDateTimeToSharedPreferences(barcode, notificationDayOfMonth + "/" + notificationMonth + "/" + notificationYear + "\n" + String.format("%02d:%02d", notificationHour, notificationMinute));
                scheduleNotification();
            }
        }, hour, minute, false);

        timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                notificationYear = 0;
                notificationMonth = 0;
                notificationDayOfMonth = 0;
            }
        });

        timePickerDialog.show();
    }


    private void setNotificationTime(int hourOfDay, int minute) {
        notificationHour = hourOfDay;
        notificationMinute = minute;
    }

    private void saveExpiryDateToSharedPreferences(String barcode, String expiryDate) {
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("expiryDate_" + barcode, expiryDate);
        editor.apply();
    }

    private void saveNotificationDateTimeToSharedPreferences(String barcode, String notificationDateTime) {
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("notificationDateTime_" + barcode, notificationDateTime);
        editor.apply();
    }

    private void retrieveFromSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String expiryDate = sharedPref.getString("expiryDate_" + barcode, "");
        String notificationDateTime = sharedPref.getString("notificationDateTime_" + barcode, "");

        if (!expiryDate.isEmpty() && !notificationDateTime.isEmpty()) {
            chooseDateButton.setText(expiryDate);
            chooseTimeButton.setText(notificationDateTime);

            Calendar currentCalendar = Calendar.getInstance();
            Calendar expiryCalendar = Calendar.getInstance();

            String[] expiryDateParts = expiryDate.split("/");
            int expiryYear = Integer.parseInt(expiryDateParts[2]);
            int expiryMonth = Integer.parseInt(expiryDateParts[1]);
            int expiryDayOfMonth = Integer.parseInt(expiryDateParts[0]);

            expiryCalendar.set(expiryYear, expiryMonth - 1, expiryDayOfMonth);

            long currentTimeMillis = currentCalendar.getTimeInMillis();
            long expiryTimeMillis = expiryCalendar.getTimeInMillis();
            long timeDifferenceMillis = expiryTimeMillis - currentTimeMillis;

            long millisecondsPerDay = 24 * 60 * 60 * 1000;
            long daysDifference = timeDifferenceMillis / millisecondsPerDay;
            long yearsDifference = daysDifference / 365;
            long remainingDays = daysDifference % 365;
            long monthsDifference = remainingDays / 30;
            remainingDays %= 30;

            StringBuilder differenceTextBuilder = new StringBuilder();

            if (yearsDifference > 0) {
                differenceTextBuilder.append(yearsDifference).append(" Year");
                if (yearsDifference > 1) differenceTextBuilder.append("s");
                if (monthsDifference > 0 || remainingDays > 0) differenceTextBuilder.append(", ");
            }
            if (monthsDifference > 0) {
                differenceTextBuilder.append(monthsDifference).append(" Month");
                if (monthsDifference > 1) differenceTextBuilder.append("s");
                if (remainingDays > 0) differenceTextBuilder.append(", ");
            }
            if (remainingDays > 0) {
                differenceTextBuilder.append(remainingDays).append(" Day");
                if (remainingDays > 1) differenceTextBuilder.append("s");
            }

            differenceTextBuilder.append(" left");

            String differenceText = differenceTextBuilder.toString();

            if (yearsDifference == 0 && monthsDifference == 0 && remainingDays == 0) {
                differenceText = "EXPIRED";
            }

            TextView differenceTextView = findViewById(R.id.differenceTextView);
            differenceTextView.setText(differenceText);
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleNotification() {
        Log.d("Notification", "Scheduling notification...");

        long currentTimeMillis = System.currentTimeMillis();

        Calendar notificationTime = Calendar.getInstance();
        notificationTime.set(notificationYear, notificationMonth - 1, notificationDayOfMonth, notificationHour, notificationMinute, 0);
        long notificationTimeMillis = notificationTime.getTimeInMillis();

        long timeDifference = notificationTimeMillis - currentTimeMillis;

        if (timeDifference >= 0) {
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("productName", productName);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTimeMillis, pendingIntent);

            Log.d("Notification", "Notification scheduled successfully");
            Toast.makeText(this, "Notification Scheduled", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("Notification", "Failed to schedule notification");
            Toast.makeText(this, "Failed to schedule notification", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleExpiryNotification() {
        Log.d("ExpiryNotification", "Scheduling expiry notification...");

        long currentTimeMillis = System.currentTimeMillis();

        Calendar expiryDateCalendar = Calendar.getInstance();
        expiryDateCalendar.set(notificationYear, notificationMonth - 1, notificationDayOfMonth, notificationHour, notificationMinute, 0);
        long expiryTimeMillis = expiryDateCalendar.getTimeInMillis();

        long timeDifference = expiryTimeMillis - currentTimeMillis;

        if (timeDifference >= 0) {
            Intent intent = new Intent(this,NotificationReceiver.class);
            intent.putExtra("productName", productName);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, expiryTimeMillis, pendingIntent);

            Log.d("ExpiryNotification", "Expiry notification scheduled successfully");
            Toast.makeText(this, "Expiry Notification Scheduled", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("ExpiryNotification", "Failed to schedule expiry notification");
            Toast.makeText(this, "Failed to schedule expiry notification", Toast.LENGTH_SHORT).show();
        }
    }
}