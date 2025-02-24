package com.example.gastroexpert.model;

import java.io.Serializable;

public class ModelListDisease implements Serializable {
    private String strKode;
    private String strDaftarPenyakit;

    public String getStrKode() {
        return strKode;
    }

    public void setStrKode(String strKode) {
        this.strKode = strKode;
    }

    public String getStrDaftarPenyakit() {
        return strDaftarPenyakit;
    }

    public void setStrDaftarPenyakit(String strDaftarPenyakit) {
        this.strDaftarPenyakit = strDaftarPenyakit;
    }
}
