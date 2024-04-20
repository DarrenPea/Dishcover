package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.example.myapplication.Authentication.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_IMAGE_GALLERY = 102;
    private static final int REQUEST_CAMERA_PERMISSION = 103;
    private TextInputLayout recipeNameEditTextLayout, ingredientsEditTextLayout, recipeEditTextLayout;
    private TextInputEditText recipeNameEditText, ingredientsEditText, recipeEditText;
    private Uri selectedImageUri;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseFirestore firestore;

    private CardView uploadedImageView;

    private ImageView finaluploadedImageView;

    private String userHandle;

    private Uri userProfilePic;

    private TextView uploadImageText;
    private ImageView uploadImageButton;
    private WalletManager userWalletManager;

    private RecipeAddedListener recipeAddedListener;

    public interface RecipeAddedListener {
        void onRecipeAdded();
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CreateFragment() {
    }

    public static CreateFragment newInstance(String param1, String param2) {
        CreateFragment fragment = new CreateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create, container, false);

        CircleImageView profileImageView = rootView.findViewById(R.id.profileImageView);

        uploadImageButton = rootView.findViewById(R.id.uploadImageButton);
        uploadedImageView = rootView.findViewById(R.id.uploadedImageView);
        finaluploadedImageView = rootView.findViewById(R.id.imageView8);
        TextView fullNameTextView = rootView.findViewById(R.id.fullName);
        TextView userNameTextView = rootView.findViewById(R.id.userName);

        recipeNameEditTextLayout = rootView.findViewById(R.id.recipeNameTextLayout);
        ingredientsEditTextLayout = rootView.findViewById(R.id.ingredientsTextLayout);
        recipeEditTextLayout = rootView.findViewById(R.id.recipeTextLayout);

        recipeNameEditText = (TextInputEditText) recipeNameEditTextLayout.getEditText();
        ingredientsEditText = (TextInputEditText) ingredientsEditTextLayout.getEditText();
        recipeEditText = (TextInputEditText) recipeEditTextLayout.getEditText();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        firestore = FirebaseFirestore.getInstance();
        uploadImageText = rootView.findViewById(R.id.uploadImageText);
        LinearLayout uploadRecipeLayout = rootView.findViewById(R.id.uploadRecipeLayout);


        User currentUser = User.getInstance();
        String uid = currentUser.getUid();
        String fullName = currentUser.getFullName();
        userWalletManager = new WalletManager(uid);
        fullNameTextView.setText(fullName != null ? fullName: "");
        userHandle = currentUser.getDisplayName();
        userNameTextView.setText(userHandle != null ? userHandle: "");
        userProfilePic = currentUser.getProfileImageUri();
        profileImageView.setImageURI(userProfilePic);

        uploadImageButton.setOnClickListener(v -> {
            showImageSourceOptions();
        });

        uploadRecipeLayout.setOnClickListener(v -> {
            boolean hasError = false; // Flag to track if any error occurred

            // Check if recipe name is empty
            if (TextUtils.isEmpty(recipeNameEditText.getText())) {
                recipeNameEditText.setError("Recipe name is required");
                hasError = true; // Set flag to true if error occurred
            }

            // Check if ingredients are empty
            if (TextUtils.isEmpty(ingredientsEditText.getText())) {
                ingredientsEditText.setError("Ingredients are required");
                hasError = true; // Set flag to true if error occurred
            }

            // Check if recipe details are empty
            if (TextUtils.isEmpty(recipeEditText.getText())) {
                recipeEditText.setError("Recipe details are required");
                hasError = true; // Set flag to true if error occurred
            }

            // If any error occurred, return without uploading
            if (hasError) {
                return;
            }

            // Check if an image is selected
            if (selectedImageUri != null) {
                String userId = currentUser.getUid();
                uploadImageToFirebaseStorage(selectedImageUri, userId);
            } else {
                Toast.makeText(getActivity(), "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });



        return rootView;
    }

    private void showImageSourceOptions() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Intent chooserIntent = Intent.createChooser(pickPhotoIntent, "Select Picture");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePictureIntent});
            startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageSourceOptions();
            } else {
                Toast.makeText(getActivity(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (data != null && data.getData() != null) {
                    // Image selected from the gallery
                    selectedImageUri = data.getData(); // Store the URI
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);
                        finaluploadedImageView.setImageBitmap(bitmap);
                        finaluploadedImageView.setVisibility(View.VISIBLE);
                        uploadImageButton.setVisibility(View.GONE);
                        uploadImageText.setVisibility(View.GONE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Image captured from the camera
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    finaluploadedImageView.setImageBitmap(imageBitmap);
                    finaluploadedImageView.setVisibility(View.VISIBLE);
                    uploadImageButton.setVisibility(View.GONE);
                    uploadImageText.setVisibility(View.GONE);
                    // Create a temporary URI for the captured image
                    selectedImageUri = getImageUri(imageBitmap);
                }
            }
        }
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                finaluploadedImageView.setImageBitmap(imageBitmap);
                finaluploadedImageView.setVisibility(View.VISIBLE);
                uploadImageButton.setVisibility(View.GONE);
                uploadedImageView.setVisibility(View.GONE);
                uploadImageText.setVisibility(View.GONE);
                selectedImageUri = getImageUri(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getActivity().getContentResolver(), selectedImageUri);
                    finaluploadedImageView.setImageBitmap(bitmap);
                    finaluploadedImageView.setVisibility(View.VISIBLE);
                    uploadImageButton.setVisibility(View.GONE);
                    uploadedImageView.setVisibility(View.GONE);
                    uploadImageText.setVisibility(View.GONE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

    private void uploadRecipeDataToFirestore(String imageUrl, String userId) {
        String dishName = recipeNameEditText.getText().toString().trim();
        String ingredients = ingredientsEditText.getText().toString().trim();
        String recipe = recipeEditText.getText().toString().trim();

        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("imageUrl", imageUrl);
        recipeData.put("userId", userId);
        recipeData.put("dishName", dishName);
        recipeData.put("ingredients", ingredients);
        recipeData.put("recipe", recipe);
        recipeData.put("likes", 0);
        recipeData.put("reviews", 0);
        recipeData.put("userHandle", userHandle);
        recipeData.put("userProfilePicUrl", userProfilePic);
        recipeData.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("recipes")
                .add(recipeData)
                .addOnSuccessListener(documentReference -> {
                    String recipeId = documentReference.getId();

                    // Add the generated recipeId to the recipe document
                    firestore.collection("recipes").document(documentReference.getId())
                            .update("recipeId", recipeId)
                            .addOnSuccessListener(aVoid -> {
                                // Recipe ID added to recipe document successfully
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure to update recipe document
                                String errorMessage = e.getMessage();
                                Toast.makeText(getActivity(), "Failed to update recipe document: " + errorMessage, Toast.LENGTH_SHORT).show();
                            });

                    // Update the user's document with the recipe_id field
                    firestore.collection("users").document(userId)
                            .update(
                                    "recipeIds", FieldValue.arrayUnion(recipeId),
                                    "points", FieldValue.increment(75)
                            )
                            .addOnSuccessListener(aVoid -> {
                                // Recipe ID added to user's document successfully
                                userWalletManager.appendToWalletHistory("Posted a recipe:", 75);
                                Toast.makeText(getActivity(), "Recipe added successfully. You have been awarded points. Check your wallet?  ", Toast.LENGTH_SHORT).show();
                                showRecipeOptionsDialog();
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure to update user document
                                String errorMessage = e.getMessage();
                                Toast.makeText(getActivity(), "Failed to update user document: " + errorMessage, Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    String errorMessage = e.getMessage();
                    Toast.makeText(getActivity(), "Failed to add recipe: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
    }



    private void uploadImageToFirebaseStorage(Uri imageUri, String userId) {
        if (imageUri != null) {
            String imageName = UUID.randomUUID().toString();
            StorageReference imageRef = storageRef.child("recipe_images/" + userId + "/" + imageName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            uploadRecipeDataToFirestore(imageUrl, userId);
                            finaluploadedImageView.setImageURI(imageUri);
                            finaluploadedImageView.setVisibility(View.VISIBLE);
                            uploadImageButton.setVisibility(View.GONE);
                            uploadImageText.setVisibility(View.GONE);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Show dialog after recipe upload
    private void showRecipeOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.recipe_created_options, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView btnCreateAnother = dialogView.findViewById(R.id.createButton);
        TextView btnViewRecipe = dialogView.findViewById(R.id.viewButton);

        btnCreateAnother.setOnClickListener(v -> {
            clearInputFields();
            dialog.dismiss();
        });


        btnViewRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), YourRecipes.class);
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
    }


    private void clearInputFields() {
        recipeNameEditText.setText("");
        ingredientsEditText.setText("");
        recipeEditText.setText("");
        selectedImageUri = null;
        finaluploadedImageView.setImageResource(R.drawable.recipe_image_border);
        finaluploadedImageView.setVisibility(View.VISIBLE);
        uploadImageButton.setVisibility(View.VISIBLE);
        uploadImageText.setVisibility(View.VISIBLE);
    }


    private Uri getImageUri(Bitmap inImage) {
        String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), inImage, "temp_image", null);
        return Uri.parse(path);
    }

    public void setRecipeAddedListener(RecipeAddedListener listener) {
        this.recipeAddedListener = listener;
    }
}