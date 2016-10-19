package hackxfdu.io.youreyes.entity;

/**
 * Image distinguishing result entity.
 *
 * @author sczyh30
 */

public class ImageResult {

    private String description;
    private String ocr;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOcr() {
        return ocr;
    }

    public void setOcr(String ocr) {
        this.ocr = ocr;
    }
}
