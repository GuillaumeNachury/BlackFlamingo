package com.gnachury;

import java.io.File;


public interface MyListener {
    // you can define any parameter as per your requirement
    public void callback(boolean result, File pathImage);
}