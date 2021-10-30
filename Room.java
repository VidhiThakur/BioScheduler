package BioScheduler;


public class Room implements Comparable<Room>{
    private Slot slot;
    private String roomNo;

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    @Override
    public String toString() {
        return "slot=" + slot +
                ", roomNo='" + roomNo + '\'' +
                '}';
    }

    @Override
    public int compareTo(Room room) {
        int roomCompare = getRoomNo().compareTo(room.getRoomNo());
        if(roomCompare == 0) {
            return Integer.valueOf(getSlot().getStartTime()).compareTo(Integer.valueOf(room.getSlot().getStartTime()));
        }
        return roomCompare;
    }
}
