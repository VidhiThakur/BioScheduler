package BioScheduler;

public class Slot implements Comparable<Slot>{
    int day;
    String startTime;
    String endTime;
    int preference;
    Integer alreadyReplaced;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getPreference() {
        return preference;
    }

    public void setPreference(int preference) {
        this.preference = preference;
    }

    public Integer getAlreadyReplaced() {
        return alreadyReplaced;
    }

    public void setAlreadyReplaced(Integer alreadyReplaced) {
        this.alreadyReplaced = alreadyReplaced;
    }

    @Override
    public String toString() {
        return "Slot{" +
                "day=" + day +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", preference=" + preference +
                ", alreadyReplaced=" + alreadyReplaced +
                '}';
    }

    @Override
    public int compareTo(Slot slot) {
        return getStartTime().compareTo(slot.getStartTime());
    }
}
