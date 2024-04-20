package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Authentication.User;
import com.example.myapplication.adapters.WalletHistoryAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class WalletFragment extends Fragment implements User.UserListener, CreateFragment.RecipeAddedListener {
    private User user;
    private TextView pointsTextView;
    private ProgressBar pointsProgressBar, loadingProgressBar;
    private TextView recipes;
    private WalletManager walletManager;
    private RecyclerView walletHistoryRecyclerView;
    private WalletHistoryAdapter walletHistoryAdapter;
    private ImageView redeemImage;

    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = User.getInstance();
        user.setListener(this);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallet, container, false);
        pointsTextView = rootView.findViewById(R.id.progress_text);
        pointsProgressBar = rootView.findViewById(R.id.progress_bar);
        recipes = rootView.findViewById(R.id.title);
        walletHistoryRecyclerView = rootView.findViewById(R.id.walletHistory);
        loadingProgressBar = rootView.findViewById(R.id.progressBar);
        walletHistoryAdapter = new WalletHistoryAdapter(new ArrayList<>());
        walletHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        walletHistoryRecyclerView.setAdapter(walletHistoryAdapter);
        redeemImage = rootView.findViewById(R.id.redeem);
        walletManager = new WalletManager(user.getUid());

        redeemImage.setOnClickListener(v -> {
            walletManager.checkPointsExceedLimit(
                    exceedsLimit -> {
                        if (exceedsLimit) {
                            showRedeemableRewardDialog();
                        } else {
                            showNoRedeemableVouchersDialog();
                        }
                    },
                    e -> {
                        Log.e("WalletFragment", "Error checking points limit", e);
                    }
            );
        });


        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pointsTextView.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        user.populateUserData();
        fetchWalletHistory();
    }

    private void fetchWalletHistory() {
        walletManager.fetchAllActionHistory(db,
                walletActions -> {
                    walletHistoryAdapter.setData(walletActions);
                    loadingProgressBar.setVisibility(View.GONE);
                    pointsTextView.setVisibility(View.VISIBLE);
                },
                e -> {
                    Toast.makeText(getActivity(), "Failed to fetch wallet history", Toast.LENGTH_SHORT).show();
                    Log.e("WalletFragment", "Error fetching wallet history", e);
                }
        );

        walletManager.checkPointsExceedLimit(
                exceedsLimit -> {
                    if (exceedsLimit) {
                        redeemImage.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary));
                    }
                },
                e -> {
                    Log.e("WalletFragment", "Error checking points limit", e);
                }
        );
    }

    @Override
    public void onUserDataLoaded() {
        pointsTextView.setText(String.valueOf(user.getPoints()));
        int points = user.getPoints();
        pointsProgressBar.setMax(1000);
        pointsProgressBar.setProgress(points);
    }

    @Override
    public void onUserDataUpdated() {
    }

    @Override
    public void onRecipeAdded() {
        Toast.makeText(getActivity(), "Recipe added!", Toast.LENGTH_SHORT).show();
        int currentPoints = user.getPoints();
        pointsTextView.setText(String.valueOf(currentPoints));
        pointsProgressBar.setProgress(currentPoints);
    }

    private void showRedeemableRewardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Redeemable Voucher");
        builder.setMessage("Congratulations! You have enough points to redeem a $5 FairPrice voucher.");
        builder.setIcon(R.drawable.redeem_primary);
        builder.setPositiveButton("Redeem", (dialog, which) -> {
            if (isFairPriceAppInstalled()) {
                openFairPriceApp();
            } else {
                Toast.makeText(requireContext(), "FairPrice app is not installed", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showNoRedeemableVouchersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("No Redeemable Vouchers");
        builder.setIcon(R.drawable.sentiment_dissatisfied);
        builder.setMessage("Sorry, there are currently no redeemable vouchers available.");
        builder.setNeutralButton("BACK", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isFairPriceAppInstalled() {
        PackageManager pm = requireContext().getPackageManager();
        try {
            pm.getPackageInfo("com.fairprice.mcomapp", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void openFairPriceApp() {
        Intent intent = requireContext().getPackageManager().getLaunchIntentForPackage("com.fairprice.mcomapp");
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            requireContext().startActivity(intent);
        }
    }
}

