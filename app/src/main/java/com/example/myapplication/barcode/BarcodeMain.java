package com.example.myapplication.barcode;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import com.google.gson.annotations.SerializedName;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.EventListener;


public class BarcodeMain extends AppCompatActivity {

    private ImageView navigateBack;

    private LinearLayout productContainer;
    private FirebaseFirestore firestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_main);

        firestore = FirebaseFirestore.getInstance();
        navigateBack = findViewById(R.id.imageView6);
        ImageButton scanButton = findViewById(R.id.ScanButton);
        productContainer = findViewById(R.id.productContainer);
        Button addButton = findViewById(R.id.addButton); // Assuming you have an add button with id addButton

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to show the add product dialog
                showAddProductDialog();
            }
        });

        navigateBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanCode();
            }
        });

        // Retrieve and display product names from Firestore
        retrieveProductNames();
    }


    private void scanCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scannedBarcode = result.getContents();
                getProductDetails(scannedBarcode);
            } else {
                Toast.makeText(this, "No Results", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getProductDetails(String barcode) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.barcodelookup.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BarcodeLookupApi barcodeLookupApi = retrofit.create(BarcodeLookupApi.class);
        Call<ProductResponse> call = barcodeLookupApi.getProductDetails(barcode, "y", "unvcpksu94xiae621joo7w3hn1yyrd");

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getProducts() != null && !response.body().getProducts().isEmpty()) {
                    String productName = response.body().getProducts().get(0).getTitle();
                    addProductButton(productName, barcode); // Update UI with scanned product name and barcode

                    // Store the product name in Firestore
                    storeProductNameInFirestore(productName, barcode);
                } else {
                    Toast.makeText(BarcodeMain.this, "Product not found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(BarcodeMain.this, "Failed to retrieve product details", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void addProductButton(final String productName, final String barcode) {
        LinearLayout productLayout = new LinearLayout(this);
        productLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        productLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView productTextView = new TextView(this);
        productTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        productTextView.setText(productName);
        productTextView.setTextSize(18);
        productTextView.setPadding(16, 8, 16, 8);

        ImageButton editButton = new ImageButton(this);
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        editParams.setMargins(0, 0, 16, 0); // Add margin to the right
        editButton.setLayoutParams(editParams);
        editButton.setImageResource(R.drawable.pen); // Set your edit button icon here
        editButton.setBackgroundColor(Color.TRANSPARENT); // Set background color to transparent
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a dialog to edit the product name
                openEditProductNameDialog(productName, barcode);
            }
        });


        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        deleteButton.setImageResource(R.drawable.dustbin); // Set your icon here
        deleteButton.setBackgroundColor(Color.TRANSPARENT); // Set background color to transparent
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Delete the product from Firestore and update the UI
                deleteProductFromFirestore(barcode);
            }
        });

        productLayout.addView(productTextView);
        productLayout.addView(editButton);
        productLayout.addView(deleteButton);
        productContainer.addView(productLayout);

        // Set click listener for the product name to navigate to ProductDetailsActivity
        productTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass the product name and barcode to the ProductDetailsActivity
                Intent intent = new Intent(BarcodeMain.this, ProductDetailsActivity.class);
                intent.putExtra("productName", productName); // Pass the product name
                intent.putExtra("barcode", barcode); // Pass the barcode
                startActivity(intent);
            }
        });
    }


    public interface BarcodeLookupApi {
        @GET("v3/products")
        Call<ProductResponse> getProductDetails(
                @Query("barcode") String barcode,
                @Query("formatted") String formatted,
                @Query("key") String apiKey
        );
    }

    public class ProductResponse {
        @SerializedName("products")
        private List<Product> products;

        public List<Product> getProducts() {
            return products;
        }
    }

    public class Product {
        @SerializedName("title")
        private String title;

        public String getTitle() {
            return title;
        }
    }

    private void storeProductNameInFirestore(String productName, String barcode) {
        if (barcode != null && !barcode.isEmpty()) {
            // Store the product name in Firestore
            Map<String, Object> productData = new HashMap<>();
            productData.put("productName", productName);

            // Use barcode as document ID
            firestore.collection("products").document(barcode)
                    .set(productData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Product name stored successfully
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BarcodeMain.this, "Failed to store product name", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(BarcodeMain.this, "Barcode is empty or null", Toast.LENGTH_SHORT).show();
        }
    }


    private void retrieveProductNames() {
        // Retrieve product names from Firestore continuously
        // Add Firestore listener to update UI when data changes
        firestore.collection("products")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(BarcodeMain.this, "Error retrieving product names", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Clear existing product buttons
                        productContainer.removeAllViews();

                        if (value != null) {
                            for (DocumentSnapshot document : value.getDocuments()) {
                                // Get product name and barcode from Firestore document
                                String productName = document.getString("productName");
                                String barcode = document.getId(); // Assuming barcode is document ID
                                addProductButton(productName, barcode); // Add product button to UI
                            }
                        }
                    }
                });
    }

    private void deleteProductFromFirestore(final String barcode) {
        // Delete the product entry from Firestore
        firestore.collection("products").document(barcode)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Product deleted successfully
                        Toast.makeText(BarcodeMain.this, "Product deleted", Toast.LENGTH_SHORT).show();
                        // Remove the corresponding product button from the UI
                        removeProductButton(barcode);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BarcodeMain.this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeProductButton(String barcode) {
        // Find and remove the product button from the container
        for (int i = 0; i < productContainer.getChildCount(); i++) {
            View view = productContainer.getChildAt(i);
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                String tag = (String) textView.getTag(); // Assuming you set the barcode as tag for each product button
                if (tag != null && tag.equals(barcode)) {
                    productContainer.removeViewAt(i);
                    break;
                }
            }
        }
    }

    private void openEditProductNameDialog(final String currentProductName, final String barcode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Product Name");

        // Set up the input
        final EditText input = new EditText(this);
        input.setText(currentProductName); // Set the current product name as default text
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newProductName = input.getText().toString().trim();
                if (!newProductName.isEmpty()) {
                    // Update the product name in Firestore and UI
                    updateProductNameInFirestore(newProductName, barcode);
                } else {
                    Toast.makeText(BarcodeMain.this, "Product name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void updateProductNameInFirestore(final String newProductName, final String barcode) {
        // Update the product name in Firestore
        firestore.collection("products").document(barcode)
                .update("productName", newProductName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Product name updated successfully
                        Toast.makeText(BarcodeMain.this, "Product name updated", Toast.LENGTH_SHORT).show();
                        // Update the UI with the new product name
                        updateProductButtonName(newProductName, barcode);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BarcodeMain.this, "Failed to update product name", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProductButtonName(String newProductName, String barcode) {
        // Update the product button in the UI with the new product name
        for (int i = 0; i < productContainer.getChildCount(); i++) {
            View view = productContainer.getChildAt(i);
            if (view instanceof LinearLayout) {
                LinearLayout productLayout = (LinearLayout) view;
                TextView productTextView = (TextView) productLayout.getChildAt(0);
                String tag = (String) productTextView.getTag(); // Assuming you set the barcode as tag for each product button
                if (tag != null && tag.equals(barcode)) {
                    productTextView.setText(newProductName);
                    break;
                }
            }
        }
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Product");

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint("Enter product name");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String productName = input.getText().toString().trim();
                if (!productName.isEmpty()) {
                    // Generate a random barcode for the new product
                    String barcode = generateRandomBarcode();
                    // Add the new product to the UI with edit and delete buttons
                    addProductButton(productName, barcode);
                    // Store the product name in Firestore
                    storeProductNameInFirestore(productName, barcode);
                } else {
                    Toast.makeText(BarcodeMain.this, "Product name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private String generateRandomBarcode() {
        // Generate a random 6-digit barcode
        Random random = new Random();
        StringBuilder barcodeBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            barcodeBuilder.append(random.nextInt(10)); // Append a random digit (0-9)
        }
        return barcodeBuilder.toString();
    }
}
