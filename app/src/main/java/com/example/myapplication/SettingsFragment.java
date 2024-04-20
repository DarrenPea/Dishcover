package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import androidx.fragment.app.Fragment;

import com.example.myapplication.Authentication.User;
import com.example.myapplication.editprofile.EditPassword;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private CircleImageView profileImageView;
    private TextView fullNameTextView, userNameTextView;
    private FirebaseFirestore firestore;
    private LinearLayout logoutLayout, resetPasswordLayout, savedRecipesLayout;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        profileImageView = view.findViewById(R.id.profile_pic);

        fullNameTextView = view.findViewById(R.id.full_name);
        userNameTextView = view.findViewById(R.id.display_name);

        User currentUser = User.getInstance();
        String fullName = currentUser.getFullName();
        fullNameTextView.setText(fullName != null ? fullName: "");
        String userName = currentUser.getDisplayName();
        userNameTextView.setText(userName != null ? userName: "");
        Uri profileImageUri = currentUser.getProfileImageUri();
        profileImageView.setImageURI(profileImageUri);

        ImageView editIcon = view.findViewById(R.id.edit_icon);
        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditAccountActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout recipesOpen = view.findViewById(R.id.yourRecipesSection);
        recipesOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), YourRecipes.class);
                startActivity(intent);
            }
        });

        savedRecipesLayout = view.findViewById(R.id.savedSection);
        savedRecipesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SavedRecipes.class);
                startActivity(intent);
            }
        });

        logoutLayout = view.findViewById(R.id.logoutLayout);
        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        resetPasswordLayout = view.findViewById(R.id.resetPasswordLayout);
        resetPasswordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditPassword.class);
                startActivity(intent);
            }
        });

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh or update any data/UI elements here
        User currentUser = User.getInstance();
        String fullName = currentUser.getFullName();
        fullNameTextView.setText(fullName != null ? fullName: "");
        String userName = currentUser.getDisplayName();
        userNameTextView.setText(userName != null ? userName: "");
        Uri profileImageUri = currentUser.getProfileImageUri();
        profileImageView.setImageURI(profileImageUri);
    }


    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_logout, null);
        builder.setView(dialogView);

        // Find buttons in the dialog layout
        TextView btnLogout = dialogView.findViewById(R.id.button_logout);
        TextView btnExit = dialogView.findViewById(R.id.button_exit);
        ImageView closeButton = dialogView.findViewById(R.id.image_close);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set click listeners for the buttons
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), NewLogin.class);
                startActivity(intent);
                getActivity().finish();
                dialog.dismiss();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finishAffinity();
                dialog.dismiss();
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
