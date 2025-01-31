package org.example.models;

public class SleepBinning {
    private int hrri;
    private int hrss;

    public SleepBinning() {
        this.hrri = 0;
        this.hrss = 0;
    }

    public SleepBinning(int hrri, int hrss) {
        this.hrri = hrri;
        this.hrss = hrss;
    }

    public int getHrri() {
        return hrri;
    }

    public void setHrri(int hrri) {
        this.hrri = hrri;
    }

    public int getHrss() {
        return hrss;
    }

    public void setHrss(int hrss) {
        this.hrss = hrss;
    }
}
