package com.example.myapplication.Authentication;

public interface AuthSubject {
    void registerObserver(AuthObserver observer);
    void removeObserver(AuthObserver observer);
    void notifyObservers(boolean isSuccess, String message);
}


