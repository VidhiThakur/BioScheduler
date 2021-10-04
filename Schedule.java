package BioScheduler;

public class Schedule {
    private TeachingAssistant teachingAssistant;
    private Room room;
    private Integer preferred; //1,2

    private String slotPatternStr;

    public String getSlotPatternStr() {
        return slotPatternStr;
    }

    public void setSlotPatternStr(String slotPatternStr) {
        this.slotPatternStr = slotPatternStr;
    }

    public TeachingAssistant getTeachingAssistant() {
        return teachingAssistant;
    }

    public void setTeachingAssistant(TeachingAssistant teachingAssistant) {
        this.teachingAssistant = teachingAssistant;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Integer getPreferred() {
        return preferred;
    }

    public void setPreferred(Integer preferred) {
        this.preferred = preferred;
    }

    @Override
    public String toString() {
        return "room=" + room + '}';
    }
}
