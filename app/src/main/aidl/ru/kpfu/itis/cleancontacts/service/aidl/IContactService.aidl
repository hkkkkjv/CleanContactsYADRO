package ru.kpfu.itis.cleancontacts.service.aidl;

import ru.kpfu.itis.cleancontacts.service.aidl.IStatusCallback;

interface IContactService {
    void removeDuplicates(in IStatusCallback callback);
}