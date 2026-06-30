package com.example.visionnotes;

public class SettingItem {

    private String title;
    private boolean switchable;
    private boolean checked;

    public SettingItem(String title, boolean switchable, boolean checked) {
        this.title = title;
        this.switchable = switchable;
        this.checked = checked;
    }

    public String getTitle() {
        return title;
    }

    public boolean isSwitchable() {
        return switchable;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}