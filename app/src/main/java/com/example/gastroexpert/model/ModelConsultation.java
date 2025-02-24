package com.example.gastroexpert.model;

import java.io.Serializable;

public class ModelConsultation implements Serializable {

    String strGejala = null;
    boolean selected = false;

    public String getStrGejala() {
        return strGejala;
    }

    public void setStrGejala(String strGejala) {
        this.strGejala = strGejala;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
