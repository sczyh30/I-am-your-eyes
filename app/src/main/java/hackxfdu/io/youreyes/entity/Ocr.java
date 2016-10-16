package hackxfdu.io.youreyes.entity;

public class Ocr {
    private String ch;
    private String en;

    public Ocr(String ch, String en) {
        this.ch = ch;
        this.en = en;
    }

    public String getCh() {
        return ch;
    }

    public void setCh(String ch) {
        this.ch = ch;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

}
