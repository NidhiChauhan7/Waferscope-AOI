/**
 * Represents a semiconductor wafer entity in the inspection system.
 *
 * OOP CONCEPTS DEMONSTRATED:
 * 1. Class: Blueprint defining state and behavior for semiconductor wafers.
 * 2. Object: Concrete instances created via new Wafer(...).
 * 3. Encapsulation: Private member variables (waferId, imageName) protected
 *    from direct external mutation, exposed via public getters.
 */
public class Wafer {

    private String waferId;
    private String imageName;

    public Wafer(String waferId, String imageName) {
        this.waferId = waferId;
        this.imageName = imageName;
    }

    public String getWaferId() {
        return waferId;
    }

    public String getImageName() {
        return imageName;
    }

    public void displayWaferInfo() {
        System.out.println("Wafer ID   : " + waferId);
        System.out.println("Image Name : " + imageName);
    }
}
