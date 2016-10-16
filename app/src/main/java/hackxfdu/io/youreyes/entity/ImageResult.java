package hackxfdu.io.youreyes.entity;

/**
 * Image distinguishing result entity.
 */

public class ImageResult {
    String description;
    Ocr ocr;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Ocr getOcr() {
        return ocr;
    }

    public void setOcr(Ocr ocr) {
        this.ocr = ocr;
    }
}
