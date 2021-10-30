package BioScheduler;

import java.util.List;

public class TeachingAssistant implements Comparable<TeachingAssistant>{
    private String name;
    private Integer TAHours;
    private List<Slot> preferredSlot;
    private List<Slot> availableSlot;
    private Boolean isMondayAvailable;
    private Boolean isSingleDayPossible;
    private Boolean areClassesAllotedInSingleDay;
    private Boolean dontChangeTheSchedule;
    private Integer totalSlotsCount;

    public Boolean getDontChangeTheSchedule() {
        return dontChangeTheSchedule;
    }

    public void setDontChangeTheSchedule(Boolean dontChangeTheSchedule) {
        this.dontChangeTheSchedule = dontChangeTheSchedule;
    }

    public TeachingAssistant() {
        this.isMondayAvailable = false;
        this.isSingleDayPossible = false;
        this.areClassesAllotedInSingleDay=false;
        this.dontChangeTheSchedule=false;

    }

    public Boolean getAreClassesAllotedInSingleDay() {
        return areClassesAllotedInSingleDay;
    }

    public void setAreClassesAllotedInSingleDay(Boolean areClassesAllotedInSingleDay) {
        this.areClassesAllotedInSingleDay = areClassesAllotedInSingleDay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTAHours() {
        return TAHours;
    }

    public void setTAHours(Integer TAHours) {
        this.TAHours = TAHours;
    }

    public List<Slot> getPreferredSlot() {
        return preferredSlot;
    }

    public void setPreferredSlot(List<Slot> preferredSlot) {
        this.preferredSlot = preferredSlot;
    }

    public List<Slot> getAvailableSlot() {
        return availableSlot;
    }

    public void setAvailableSlot(List<Slot> availableSlot) {
        this.availableSlot = availableSlot;
    }

    public Boolean getMondayAvailable() {
        return isMondayAvailable;
    }

    public void setMondayAvailable(Boolean mondayAvailable) {
        isMondayAvailable = mondayAvailable;
    }

    public Boolean getSingleDayPossible() {
        return isSingleDayPossible;
    }

    public void setSingleDayPossible(Boolean singleDayPossible) {
        isSingleDayPossible = singleDayPossible;
    }

    public Integer getTotalSlotsCount() {
        return totalSlotsCount;
    }

    public void setTotalSlotsCount(Integer totalSlotsCount) {
        this.totalSlotsCount = totalSlotsCount;
    }

    @Override
    public String toString() {
        return "TeachingAssistant{" +
                "name='" + name + '\'' +
                ", TAHours=" + TAHours +
                ", preferredSlot=" + preferredSlot +
                ", availableSlot=" + availableSlot +
                ", isMondayAvailable=" + isMondayAvailable +
                ", isSingleDayPossible=" + isSingleDayPossible +
                ", areClassesAllotedInSingleDay=" + areClassesAllotedInSingleDay +
                ", dontChangeTheSchedule=" + dontChangeTheSchedule +
                ", totalSlotsCount=" + totalSlotsCount +
                '}';
    }

    @Override
    public int compareTo(TeachingAssistant teachingAssistant) {
        return getTotalSlotsCount().compareTo(teachingAssistant.getTotalSlotsCount());
    }
}
