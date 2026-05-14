package ru.kpfu.itis.cleancontacts.service.aidl;

interface IStatusCallback {
    void onResult(int statusCode, String message);
}