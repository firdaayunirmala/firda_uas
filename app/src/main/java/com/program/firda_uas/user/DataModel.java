package com.program.firda_uas.user;

public class DataModel {
    private String id, nim, nama, telepon, email;
    public DataModel() {
    }
    public DataModel(String id,String nim, String nama, String telepon, String email) {
        this.id = id;
        this.nim = nim;
        this.nama = nama;
        this.telepon = telepon;
        this.email = email;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getNim(){return nim;}
    public void setNim(String nim) {this.nim = nim;}
    public String getNama() {
        return nama;
    }
    public void setNama(String nama) {
        this.nama = nama;
    }
    public String getTelepon() {
        return telepon;
    }
    public void setTelepon(String telepon) {
        this.telepon = telepon;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
