/**
 * Abstract base class defining the standard structure for wafer analyzers.
 *
 * OOP CONCEPTS DEMONSTRATED:
 * 1. Abstraction: Cannot be instantiated directly (cannot do 'new Analyzer()').
 * 2. Abstract Method: Forces all derived analyzer classes to implement analyze().
 * 3. Concrete Method: Provides common utility method showSystemMessage().
 */
public abstract class Analyzer {

    /**
     * Abstract method enforcing child classes to provide a concrete analysis algorithm.
     */
    public abstract void analyze(Wafer wafer, InspectionResult result);

    /**
     * Concrete method shared by all analyzer subclasses.
     */
    public void showSystemMessage() {
        System.out.println("Semiconductor Wafer Inspection System");
    }
}
