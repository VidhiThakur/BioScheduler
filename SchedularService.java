package BioScheduler;


import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class SchedularService {
    private List<TeachingAssistant> taList = new ArrayList<>();
    private List<Room> roomList = new ArrayList<>();
    private Map<String, List<Room>> dayRoomSlotMap = new HashMap<>();
    private Map<String, Boolean> taRoomSlotMap = new HashMap<>();
    private Map<String, Boolean> roomSlotBookedMap = new HashMap<>();
    private List<String> roomsNameList = new ArrayList<>();
    private Map<String, Integer> taScheduleCount = new HashMap<>();
    private Map<String,List<Schedule>> taSchedules = new HashMap<>();
    private ArrayList<String> timeSlotSortedArr=new ArrayList<>();


    public void createSchedule() throws IOException {
        this.clear();
        Input utils=new Input();
        utils.initialiseData(taList, roomList, dayRoomSlotMap, roomsNameList,timeSlotSortedArr);
        if(!checkIfScheduleIsFeasible(taList,dayRoomSlotMap))
        {
            System.out.println("A schedule is not feasible because ta's are less than the slots");
            return;
        }
        this.createScheduleWithPrefForSingleDay();
        this.createScheduleWithPrefFor2InSingleDay();


        this.createScheduleForMultipleDays();

        if(!checkIfScheduleIsComplete()){
      //     completeSchedule();
            //do something
        }
        //Complete

        // Optimise

        /*
        for a person who did not get any preferred slot, check if that slot does not belong to a ta in his 3/2 classes bracket
        if yes, check for other preferred slots
        if no, then allot this to our ta, aur dusre ke liye run createScheduleForMultipleDays() recursively
         */
        printScheduleToExcel(taSchedules);
        System.out.println("vidhi");
        System.out.println(taSchedules.toString());
    }

    private void completeSchedule() {
        for (Map.Entry<String, List<Schedule>> entry : taSchedules.entrySet()) {
            int allotedSlots = entry.getValue().size();
            if (entry.getValue().get(0).getTeachingAssistant().getTAHours() > allotedSlots) {
                while(entry.getValue().get(0).getTeachingAssistant().getTAHours() > allotedSlots){

                    for(int count=0;count<entry.getValue().get(0).getTeachingAssistant().getPreferredSlot().size();count++){
                        Slot preferredSlot=entry.getValue().get(0).getTeachingAssistant().getPreferredSlot().get(count);
                        String slotStr=preferredSlot.getDay()+"_"+preferredSlot.getStartTime();
                        for(Map.Entry<String,List<Schedule>> e:taSchedules.entrySet()){
                            for(int i=0;i<e.getValue().size();i++)
                            {
                                if(e.getValue().get(i).getSlotPatternStr().endsWith(slotStr))

                                {
                                    if(e.getValue().get(i).getTeachingAssistant().getName().equals(entry.getValue().get(0).getTeachingAssistant().getName()))
                                    break;
                                    else
                                    {
                                        if(e.getValue().get(i).getTeachingAssistant().getDontChangeTheSchedule()) //e.getValue().get(i).getTeachingAssistant().getAreClassesAllotedInSingleDay())
                                            continue;
                                        Schedule s=new Schedule();
                                        s=e.getValue().get(i);
                                        entry.getValue().get(i).getTeachingAssistant().setDontChangeTheSchedule(true);
                                        s.setTeachingAssistant(entry.getValue().get(i).getTeachingAssistant());
                                        entry.getValue().add(s);
                                        e.getValue().remove(i);
                                        createScheduleForMultipleDays();
                                        allotedSlots++;
                                    }
                                }
                            }

                        }
                    }
                    if(allotedSlots<entry.getValue().get(0).getTeachingAssistant().getTAHours()){
                        System.out.println("Schedule is not feasible as classes cant be given to "+entry.getValue().get(0).getTeachingAssistant().getName());
                        break;
                    }
                 //       entry.getValue().get(0).getTeachingAssistant().getPreferredSlot()
                }
            }
        }

    }

    private boolean checkIfScheduleIsComplete() {
        for (Map.Entry<String, List<Schedule>> entry : taSchedules.entrySet()) {
            int allotedSlots = entry.getValue().size();
            if (entry.getValue().get(0).getTeachingAssistant().getTAHours() > allotedSlots) {
                return false;
            }
        }
        return true;

    }

    private void printScheduleToExcel(Map<String, List<Schedule>> taSchedules) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("Instructor Timetable");
        int i=0;
        XSSFRow row = spreadsheet.createRow(i);
        HashMap<Integer,String> dayMap=new HashMap<>();
        dayMap.put(1,"Monday");
        dayMap.put(2,"Tuesday");
        dayMap.put(3,"Wednesday");
        dayMap.put(4,"Thursday");
       XSSFCellStyle style =workbook.createCellStyle();
        XSSFFont font=workbook.createFont();
        /* set the weight of the font */
        font.setBold(true);
        /* attach the font to the style created earlier */
        style.setFont(font);

        for(int rowId=0;rowId<roomsNameList.size();rowId++)
        {

            Cell cell=row.createCell(0);
            cell.setCellValue(roomsNameList.get(rowId));
            cell.setCellStyle(style);
            for(int day=1;day<=5;day++)
            {
                cell=row.createCell(day);
                cell.setCellValue(dayMap.get(day));
                cell.setCellStyle(style);

            }
            i=i+timeSlotSortedArr.size()+1;
            row=spreadsheet.createRow(i);


        }
        i=1;
        for(int rowIndex=1;rowIndex<=roomsNameList.size()*timeSlotSortedArr.size()+1;rowIndex++)
        {
            for (String timeSlot:timeSlotSortedArr) {
                row=spreadsheet.createRow(rowIndex++);
                Cell cell=row.createCell(0);
                cell.setCellValue(timeSlot);
                cell.setCellStyle(style);



            }
        }
        row=spreadsheet.getRow(0);
        DataFormatter dataFormatter = new DataFormatter();
        HashMap<String,Integer> dayMapReverse=new HashMap<>();
        dayMapReverse.put("Monday",1);


        int rowNum=0;
        for(i=0;i<roomsNameList.size();i++)
        {
            row=spreadsheet.getRow(rowNum);
            if(row==null)
                break;
            String roomNo=dataFormatter.formatCellValue(row.getCell(0));
            for (Map.Entry<String, List<Schedule>> entry : taSchedules.entrySet()){
                for(Schedule singleSchedule:entry.getValue())
                {
                    String timeSlotTA=singleSchedule.getRoom().getSlot().getStartTime()+"-"+singleSchedule.getRoom().getSlot().getEndTime();
                    if(singleSchedule.getRoom().getRoomNo().equals(roomNo))
                    {
                        if(row==null)continue;
                        int tempRow=rowNum;
                        for(int time=1;time<=timeSlotSortedArr.size();time++){

                            row=spreadsheet.getRow(tempRow+time);
                            if(row==null)continue;
                            if(row.getCell(0)==null)
                                continue;
                            if(dataFormatter.formatCellValue(row.getCell(0)).equals(timeSlotTA)){
                                Cell cell=row.createCell(singleSchedule.getRoom().getSlot().getDay());
                                cell.setCellValue(singleSchedule.getTeachingAssistant().getName());
                            }
                        }
                    }
                }
            }
            rowNum=rowNum+timeSlotSortedArr.size()+1;
        }


            FileOutputStream out = new FileOutputStream(
                new File("/home/vidhi/IdeaProjects/BioScheduler/src/output.xlsx"));

        workbook.write(out);
        out.close();



    }

    private boolean checkIfScheduleIsFeasible(List<TeachingAssistant> taList, Map<String, List<Room>> dayRoomSlotMap) {

        //Count TA hours< preferred+available slots
    Integer countOfTaHours=0;
        for(TeachingAssistant ta:taList)
    {
        try {
            countOfTaHours += ta.getTAHours();
        }catch (Throwable e){
            System.out.println("vidhi");
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }
        Integer countOfRoomSlots=0;
        for(List<Room> rooms: dayRoomSlotMap.values()){
            countOfRoomSlots+=rooms.size();
        }
        if(countOfRoomSlots>countOfTaHours)
            return false;
        return true;
    }

    private void createScheduleWithPrefForSingleDay() {
        for(TeachingAssistant ta: taList) {
            if(ta.getSingleDayPossible()) {
                List<Room> possibleSlots = new ArrayList<>();
                Boolean singleDaySlotFound = false;
                for (int i = 1; i <= 5; i++) {
                    if(i == 1 && !ta.getMondayAvailable()) {
                        continue;
                    }
                    possibleSlots.clear();
                    List<Room> dayslots = this.getAvailableSlotsForDay(i);
                    possibleSlots.addAll(this.checkPossibleForSingleDay(dayslots, ta.getTAHours(), ta.getPreferredSlot()));
                    if (possibleSlots.size() == ta.getTAHours()) {
                        singleDaySlotFound = true;
                        ta.setAreClassesAllotedInSingleDay(true);
                        taScheduleCount.put(ta.getName(), ta.getTAHours());
                        break;
                    }
                }

                if(!singleDaySlotFound) {
                    for (int i = 1; i <= 5; i++) {
                        if (i == 1 && !ta.getMondayAvailable()) {
                            continue;
                        }
                        possibleSlots.clear();
                        List<Slot> taSlots = new ArrayList<>();
                        List<Room> dayslots = this.getAvailableSlotsForDay(i);
                        taSlots.addAll(ta.getPreferredSlot());
                        taSlots.addAll(ta.getAvailableSlot());
                        possibleSlots.addAll(this.checkPossibleForSingleDay(dayslots, ta.getTAHours(), taSlots));
                        if (possibleSlots.size() == ta.getTAHours()) {
                            singleDaySlotFound = true;
                            taScheduleCount.put(ta.getName(), ta.getTAHours());
                            break;
                        }
                    }
                }

                if(singleDaySlotFound) {
                    for(Room m: possibleSlots) {
                        taRoomSlotMap.put(ta.getName()+"_"+m.getRoomNo()+"_"+m.getSlot().getDay()+"_"+m.getSlot().getStartTime(),true);
                        roomSlotBookedMap.put(m.getRoomNo()+"_"+m.getSlot().getDay()+"_"+m.getSlot().getStartTime(), true);
                        List<Schedule> ls = new ArrayList<>();
                        if(taSchedules.get(ta.getName()) != null) {
                            ls = taSchedules.get(ta.getName());
                        }
                        Schedule s = new Schedule();
                        ta.setAreClassesAllotedInSingleDay(true);
                        s.setTeachingAssistant(ta);
                        s.setSlotPatternStr(m.getRoomNo()+"_"+m.getSlot().getDay()+"_"+m.getSlot().getStartTime());
                        s.setRoom(m);
                        ls.add(s);
                        taSchedules.put(ta.getName(), ls);
                    }
                }
            }
        }
    }

    private void createScheduleWithPrefFor2InSingleDay() {
        for(TeachingAssistant ta: taList) {
            if (taScheduleCount.get(ta.getName()) == null || taScheduleCount.get(ta.getName()) < ta.getTAHours()) {
                List<Room> possibleSlots = new ArrayList<>();
                Boolean singleDaySlotFound = false;
                for (int i = 1; i <= 5; i++) {
                    if(i == 1 && !ta.getMondayAvailable()) {
                        continue;
                    }
                    possibleSlots.clear();
                    List<Room> dayslots = this.getAvailableSlotsForDay(i);
                    possibleSlots.addAll(this.checkPossibleFor2InSingleDay(dayslots, ta.getTAHours(), ta.getPreferredSlot()));
                    if (possibleSlots.size() == 2) {
                        singleDaySlotFound = true;
                        taScheduleCount.put(ta.getName(), 2);
                        break;
                    }
                }

                if(!singleDaySlotFound) {
                    for (int i = 1; i <= 5; i++) {
                        if(i == 1 && !ta.getMondayAvailable()) {
                            continue;
                        }
                        possibleSlots.clear();
                        List<Slot> taSlots = new ArrayList<>();
                        List<Room> dayslots = this.getAvailableSlotsForDay(i);
                        taSlots.addAll(ta.getPreferredSlot());
                        taSlots.addAll(ta.getAvailableSlot());
                        possibleSlots.addAll(this.checkPossibleFor2InSingleDay(dayslots, ta.getTAHours(), taSlots));
                        if (possibleSlots.size() == 2) {
                            singleDaySlotFound = true;
                            taScheduleCount.put(ta.getName(), 2);
                            break;
                        }
                    }
                }

                if (singleDaySlotFound) {
                    for (Room m : possibleSlots) {
                        taRoomSlotMap.put(ta.getName() + "_" + m.getRoomNo() + "_" + m.getSlot().getDay() + "_" + m.getSlot().getStartTime(), true);
                        roomSlotBookedMap.put(m.getRoomNo() + "_" + m.getSlot().getDay() + "_" + m.getSlot().getStartTime(), true);
                        List<Schedule> ls = new ArrayList<>();
                        if (taSchedules.get(ta.getName()) != null) {
                            ls = taSchedules.get(ta.getName());
                        }
                        Schedule s = new Schedule();
                        ta.setAreClassesAllotedInSingleDay(true);
                        s.setTeachingAssistant(ta);
                        s.setSlotPatternStr(m.getRoomNo()+"_"+m.getSlot().getDay()+"_"+m.getSlot().getStartTime());
                        s.setRoom(m);
                        ls.add(s);
                        taSchedules.put(ta.getName(), ls);
                    }
                }
            }
        }
    }

    private void createScheduleForMultipleDays() {
        for(TeachingAssistant ta: taList) {
            if (taScheduleCount.get(ta.getName()) == null || taScheduleCount.get(ta.getName()) < ta.getTAHours()) {
                int hoursScheduled = taScheduleCount.getOrDefault(ta.getName(),0);
                if (ta.getTAHours() - hoursScheduled > 0) {
                    List<Room> possibleSlots = new ArrayList<>();
                    List<Room> dayslots = new ArrayList<>();
                    for (int i = 1; i <= 5; i++) {
                        if (i == 1 && !ta.getMondayAvailable()) {
                            continue;
                        }
                        dayslots.addAll(this.getAvailableSlotsForDay(i));
                    }

                    possibleSlots.clear();
                    possibleSlots.addAll(this.checkPossibleForMultipleDays(dayslots, ta.getTAHours() - hoursScheduled, ta.getPreferredSlot(),ta.getName()));

                    if(ta.getTAHours() - hoursScheduled - possibleSlots.size() > 0) {
                        dayslots.clear();
                        for (int i = 1; i <= 5; i++) {
                            if (i == 1 && !ta.getMondayAvailable()) {
                                continue;
                            }
                            dayslots.addAll(this.getAvailableSlotsForDay(i));
                        }

                        possibleSlots.addAll(this.checkPossibleForMultipleDays(dayslots, ta.getTAHours() - hoursScheduled- possibleSlots.size(), ta.getAvailableSlot(),ta.getName()));
                    }

                    for (Room m : possibleSlots) {
                        taRoomSlotMap.put(ta.getName() + "_" + m.getRoomNo() + "_" + m.getSlot().getDay() + "_" + m.getSlot().getStartTime(), true);
                        roomSlotBookedMap.put(m.getRoomNo() + "_" + m.getSlot().getDay() + "_" + m.getSlot().getStartTime(), true);
                        List<Schedule> ls = new ArrayList<>();
                        if (taSchedules.get(ta.getName()) != null) {
                            ls = taSchedules.get(ta.getName());
                        }
                        Schedule s = new Schedule();
                        s.setTeachingAssistant(ta);
                        s.setSlotPatternStr(m.getRoomNo()+"_"+m.getSlot().getDay()+"_"+m.getSlot().getStartTime());
                        s.setRoom(m);
                        ls.add(s);
                        taSchedules.put(ta.getName(), ls);
                        taScheduleCount.put(ta.getName(), possibleSlots.size());
                    }
                }
            }
        }
    }

    private List<Room> checkPossibleForSingleDay(List<Room> slots, int hours, List<Slot> taPref) {
        List<Room> availableSlots = new ArrayList<>();
        for(Room m : slots) {
            if(isSlotInTAPref(m.getSlot(), taPref)) {
                availableSlots.add(m);
            }
        }
        int count = 1;
        List<Room> possibleSlots = new ArrayList<>();
        Room previousSlot = new Room();
        //sort availableSlots on startTime and day
        Collections.sort(availableSlots);
        for(Room r: availableSlots) {
            if(count<=hours) {
                if (possibleSlots.size() == 0) {
                    possibleSlots.add(r);
                    this.setPreviousSlot(previousSlot, r);
                    count++;
                } else {
                    if (count == 3) {
                        if (this.lessThanOrEqual(r,previousSlot,false)) {
                            continue;
                        }if (this.lessThanOrEqual(r,previousSlot,true)) {
                            continue;
                        }
                        else {
                            possibleSlots.add(r);
                            this.setPreviousSlot(previousSlot, r);
                            count++;
                        }
                    } else {
                        if (this.lessThanOrEqual(r,previousSlot,true)) {
                            continue;
                        } else {
                            possibleSlots.add(r);
                            this.setPreviousSlot(previousSlot, r);
                            count++;
                        }
                    }
                }
            }
        }

        return possibleSlots;
    }

    private List<Room> checkPossibleFor2InSingleDay(List<Room> slots, int hours, List<Slot> taPref) {
        List<Room> availableSlots = new ArrayList<>();
        for(Room m : slots) {
            if(isSlotInTAPref(m.getSlot(), taPref)) {
                availableSlots.add(m);
            }
        }
        int count = 1;
        List<Room> possibleSlots = new ArrayList<>();
        Room previousSlot = new Room();
        //sort availableSlots on startTime and day
        Collections.sort(availableSlots);
        for(Room r: availableSlots) {
            if(count<=hours && count<3) {
                if (possibleSlots.size() == 0) {
                    possibleSlots.add(r);
                    count++;
                    this.setPreviousSlot(previousSlot, r);
                } else {
                    if (this.lessThanOrEqual(r,previousSlot,true)) {
                        continue;
                    } else {
                        possibleSlots.add(r);
                        this.setPreviousSlot(previousSlot, r);
                        count++;
                    }
                }
            }
        }

        return possibleSlots;
    }

    private List<Room> checkPossibleForMultipleDays(List<Room> slots, int hours, List<Slot> taPref, String taName) {
        List<Room> availableSlots = new ArrayList<>();
        for(Room m : slots) {
            if(isSlotInTAPref(m.getSlot(), taPref)) {
                availableSlots.add(m);
            }
        }
        int count = 1;
        List<Room> possibleSlots = new ArrayList<>();
        Room previousSlot = new Room();
        //sort availableSlots on startTime and day
        Collections.sort(availableSlots);
        Map<Integer,Integer> daysBooked = new HashMap<>();
        this.getDaysBookedForTA(daysBooked, taName);
        for(Room r: availableSlots) {
            if(count<=hours && daysBooked.getOrDefault(r.getSlot().getDay(),0) < 2) {
                if (possibleSlots.size() == 0) {
                    possibleSlots.add(r);
                    daysBooked.put(r.getSlot().getDay(),daysBooked.getOrDefault(r.getSlot().getDay(),0)+1);
                    this.setPreviousSlot(previousSlot, r);
                    count++;
                } else {
                        if (count == 3) {
                        if (this.lessThanOrEqual(r,previousSlot, false)) {
                            continue;
                        }
                        if (this.lessThanOrEqual(r,previousSlot,true)) {
                            continue;
                        }
                        else {
                            possibleSlots.add(r);
                            this.setPreviousSlot(previousSlot, r);
                            count++;
                        }
                    } else {
                        if (this.lessThanOrEqual(r,previousSlot,true)) {
                            continue;
                        } else {
                            possibleSlots.add(r);
                            daysBooked.put(r.getSlot().getDay(),daysBooked.getOrDefault(r.getSlot().getDay(),0)+1);
                            this.setPreviousSlot(previousSlot, r);
                            count++;
                        }
                    }
                }
            }
        }
        daysBooked.clear();
        return possibleSlots;
    }

    private Boolean lessThanOrEqual(Room r1, Room r2, Boolean startTime) {
        if(r1.getSlot().getDay() != r2.getSlot().getDay()) {
            return false;
        }
        if (startTime && Double.parseDouble(r1.getSlot().getStartTime()) <= Double.parseDouble(r2.getSlot().getStartTime())) {
            return true;
        }
        if (!startTime && Double.parseDouble(r1.getSlot().getStartTime()) <= Double.parseDouble(r2.getSlot().getEndTime())) {
            return true;
        }
        return false;
    }

    private void setPreviousSlot(Room r1, Room r2) {
        r1.setSlot(r2.getSlot());
        r1.setRoomNo(r2.getRoomNo());
    }

    private Boolean isSlotInTAPref(Slot s1, List<Slot> s2) {
        for(Slot s: s2) {
            if (s1.getDay() == s.getDay() && s1.getStartTime().equals(s.getStartTime())) {
                s1.setPreference(s.getPreference());
                return true;
            }
        }
        return false;
    }

    private List<Room> getAvailableSlotsForDay(int day) {
        List<Room> ls = new ArrayList<>();
        for(String rn: roomsNameList) {
            List<Room> drl = dayRoomSlotMap.get(day + "_" + rn);
            if (drl != null)
                for (Room r : drl) {
                    if (roomSlotBookedMap.get(rn + "_" + day + "_" + r.getSlot().getStartTime()) == null) {
                        ls.add(r);
                    }
                }
        }
        return ls;
    }

    private void getDaysBookedForTA(Map<Integer,Integer> daysBooked, String taName) {
        List<Schedule> taSchedule = taSchedules.get(taName);
        if(taSchedule != null) {
            for(Schedule s : taSchedule) {
                daysBooked.put(s.getRoom().getSlot().getDay(),daysBooked.getOrDefault(s.getRoom().getSlot().getDay(),0)+1);
            }
        }
    }

    private void clear() {
        dayRoomSlotMap.clear();
        taRoomSlotMap.clear();
        roomSlotBookedMap.clear();
        taList.clear();
        roomList.clear();
        taScheduleCount.clear();
        taSchedules.clear();
    }

}