package com.example.myapplication.Authentication;

public interface AuthObserver {
    void onAuthStatusChanged(boolean isSuccess, String message);
}

