package BioScheduler;

public class InputParametersForAlgorithm {


    private Integer noOfIterations=0;
    private Integer slotReplacementCount=0;

    public Integer getNoOfIterations() {
        return noOfIterations;
    }

    public void setNoOfIterations(Integer noOfIterations) {
        this.noOfIterations = noOfIterations;
    }

    public Integer getSlotReplacementCount() {
        return slotReplacementCount;
    }

    public void setSlotReplacementCount(Integer slotReplacementCount) {
        this.slotReplacementCount = slotReplacementCount;
    }

    @Override
    public String toString() {
        return "InputParametersForAlgorithm{" +
                "noOfIterations=" + noOfIterations +
                ", slotReplacementCount=" + slotReplacementCount +
                '}';
    }
}
