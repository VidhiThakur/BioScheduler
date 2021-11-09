package BioScheduler;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class SchedularService {
    private List<TeachingAssistant> taList = new ArrayList<>();
    private List<Room> roomList = new ArrayList<>();
    private Map<String, List<Room>> dayRoomSlotMap = new HashMap<>();
    private Map<String, Boolean> taSlotMap = new HashMap<>();
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

        //comment - sorted taList on pref + avail count low -> high (so that ta with less options get slots first)
        Collections.sort(taList);
        this.createScheduleWithPrefForSingleDay();
        this.createScheduleWithPrefFor2InSingleDay();
        this.createScheduleForMultipleDays(null);
        int total = 0;
        for (Map.Entry<String, List<Schedule>> entry : taSchedules.entrySet()) {
            total += entry.getValue().size();
            System.out.println( entry.getValue().get(0).getTeachingAssistant().getName()  + "<==>" + entry.getValue().size());
        }
        System.out.println("Total schedules created: " + total);

        if(!checkIfScheduleIsComplete()){

            for(int i=0;i<80;i++) {
                completeAndPrint();
            }
            //do something
        }
        completeAndPrint();



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

        total = 0;
        for (Map.Entry<String, List<Schedule>> entry : taSchedules.entrySet()) {
            total += entry.getValue().size();
            System.out.println( entry.getValue().get(0).getTeachingAssistant().getName()  + "<==>" + entry.getValue().size());
        }
        System.out.println("Total schedules created: " + total);

    }

    private void completeAndPrint() {


        completeSchedule();
     completeScheduleForTANotCoveredTillNow();
        int total = 0;
        for (Map.Entry<String, List<Schedule>> entry : taSchedules.entrySet()) {
            total += entry.getValue().size();
            System.out.println( entry.getValue().get(0).getTeachingAssistant().getName()  + "<==>" + entry.getValue().size());
        }
        System.out.println("Total schedules created: " + total);

    }

    private void completeSchedule() {
        for (Map.Entry<String, List<Schedule>> entry : taSchedules.entrySet()) {
            int allotedSlots = entry.getValue().size();
            if (entry.getValue().get(0).getTeachingAssistant().getTAHours() > allotedSlots) {
                while(entry.getValue().get(0).getTeachingAssistant().getTAHours() > allotedSlots){
                    for(int count=0;count<entry.getValue().get(0).getTeachingAssistant().getPreferredSlot().size();count++){
                        Slot preferredSlot=entry.getValue().get(0).getTeachingAssistant().getPreferredSlot().get(count);
                        String slotStr=preferredSlot.getDay()+"_"+preferredSlot.getStartTime();

                        //comment - check if picked slot can be alloted or not
                        if(!checkIfSlotInSequenceAndSingleDayPossible(entry.getValue(),preferredSlot)) {
                            continue;
                        }
                        createScheduleForMultipleDays(entry.getValue().get(0).getTeachingAssistant().getName());
                        allotedSlots = taSchedules.get(entry.getValue().get(0).getTeachingAssistant().getName()).size();

                        for(Map.Entry<String,List<Schedule>> e:taSchedules.entrySet()){
                            if(entry.getValue().get(0).getTeachingAssistant().getTAHours() > allotedSlots)
                                for(int i=0;i<e.getValue().size();i++)
                                {
                                    if(e.getValue().get(i).getSlotPatternStr().endsWith(slotStr))
                                    {
                                        if(e.getValue().get(0).getTeachingAssistant().getName().equals(entry.getValue().get(0).getTeachingAssistant().getName()))
                                            break;
                                        else {
                                            if (e.getValue().get(0).getTeachingAssistant().getDontChangeTheSchedule()
                                                    || e.getValue().size() == 1
                                            //|| e.getValue().get(0).getTeachingAssistant().getAreClassesAllotedInSingleDay()
                                            ) //e.getValue().get(i).getTeachingAssistant().getAreClassesAllotedInSingleDay())
                                                continue;
                                            Schedule s = new Schedule();
                                            s = e.getValue().get(i);

                                            //check if the ta whom we want this new slot alloted to, already has a slot booked for the same time.
                                            if(taSlotMap.get(entry.getValue().get(0).getTeachingAssistant().getName()+"_"+s.getRoom().getSlot().getDay()+"_"+s.getRoom().getSlot().getStartTime()) != null){
                                                continue;
                                            }

//                                            if(checkIfSameDayConsecutiveSlotAsssignedInDifferentRoom(entry.getValue(),s))
//                                                continue;

//                                            boolean sameDayDifferentRoomSlotAlreadyAlloted=false;
//                                            for(Schedule allotedSchedule:entry.getValue())
//                                            {
//                                                if(allotedSchedule.getRoom().getSlot().getDay()==s.getRoom().getSlot().getDay() && !allotedSchedule.getRoom().equals(s.getRoom())){
//                                                    sameDayDifferentRoomSlotAlreadyAlloted=true;
//                                                    break;
//                                                }
//
//                                            }
//                                            if(sameDayDifferentRoomSlotAlreadyAlloted)
//                                                continue;



                                            int slotReplacedCount = s.getRoom().getSlot().getAlreadyReplaced() != null ? s.getRoom().getSlot().getAlreadyReplaced() : 0;
                                            if (slotReplacedCount > 6) {
                                                continue;
                                            }
                                            s.getRoom().getSlot().setAlreadyReplaced(slotReplacedCount + 1);
                                       //    e.getValue().get(0).getTeachingAssistant().setDontChangeTheSchedule(true);
                                            s.setTeachingAssistant(entry.getValue().get(0).getTeachingAssistant());
                                            entry.getValue().add(s);
                                            e.getValue().remove(i);

                                          //  e.getValue().get(0).getTeachingAssistant().setDontChangeTheSchedule(true);
                                            int taCurrentScheduleCount = taScheduleCount.getOrDefault(e.getValue().get(0).getTeachingAssistant().getName(),0);
                                            if(taCurrentScheduleCount>0) {
                                                taScheduleCount.put(e.getValue().get(0).getTeachingAssistant().getName(), taCurrentScheduleCount - 1);
                                            }

                                            taSlotMap.remove(e.getValue().get(0).getTeachingAssistant().getName() +"_"+s.getRoom().getSlot().getDay()+"_"+s.getRoom().getSlot().getStartTime());

                                            if(taScheduleCount.containsKey(entry.getValue().get(0).getTeachingAssistant().getName())){
                                                taScheduleCount.put(entry.getValue().get(0).getTeachingAssistant().getName(),taScheduleCount.get(entry.getValue().get(0).getTeachingAssistant().getName())+1);
                                            }
                                            else{
                                                taScheduleCount.put(entry.getValue().get(0).getTeachingAssistant().getName(),1);

                                            }

                                            taSlotMap.put(entry.getValue().get(0).getTeachingAssistant().getName() +"_"+s.getRoom().getSlot().getDay()+"_"+s.getRoom().getSlot().getStartTime(),true);

                                            createScheduleForMultipleDays(e.getValue().get(0).getTeachingAssistant().getName());
                                            allotedSlots++;
                                        }
                                    }
                                }

                        }
                    }
                    if(allotedSlots<entry.getValue().get(0).getTeachingAssistant().getTAHours()) {

                        for (int count = 0; count < entry.getValue().get(0).getTeachingAssistant().getAvailableSlot().size(); count++) {
                            Slot availableSlot = entry.getValue().get(0).getTeachingAssistant().getAvailableSlot().get(count);
                            String slotStr = availableSlot.getDay() + "_" + availableSlot.getStartTime();
                            if(!checkIfSlotInSequenceAndSingleDayPossible(entry.getValue(),availableSlot)) {
                                continue;
                            }
                            createScheduleForMultipleDays(entry.getValue().get(0).getTeachingAssistant().getName());
                            allotedSlots = taSchedules.get(entry.getValue().get(0).getTeachingAssistant().getName()).size();

                            for (Map.Entry<String, List<Schedule>> e : taSchedules.entrySet()) {
                                if (entry.getValue().get(0).getTeachingAssistant().getTAHours() > allotedSlots)
                                    for (int i = 0; i < e.getValue().size(); i++) {
                                        if (e.getValue().get(i).getSlotPatternStr().endsWith(slotStr)) {
                                            if (e.getValue().get(i).getTeachingAssistant().getName().equals(entry.getValue().get(0).getTeachingAssistant().getName()))
                                                break;
                                            else {
                                                if (e.getValue().get(0).getTeachingAssistant().getDontChangeTheSchedule()
                                                        ||  e.getValue().size()==1
                                             //         || e.getValue().get(0).getTeachingAssistant().getAreClassesAllotedInSingleDay()
                                                ) //e.getValue().get(i).getTeachingAssistant().getAreClassesAllotedInSingleDay())
                                                    continue;
                                                Schedule s = new Schedule();
                                                s = e.getValue().get(i);

                                                //comment - if same day time is already alloted to TA than skip that schedule
                                                if(taSlotMap.get(entry.getValue().get(0).getTeachingAssistant().getName()+"_"+s.getRoom().getSlot().getDay()+"_"+s.getRoom().getSlot().getStartTime()) != null){
                                                    continue;
                                                }

//                                                if(checkIfSameDayConsecutiveSlotAsssignedInDifferentRoom(entry.getValue(),s))
//                                                    continue;
//

                                                //comment - added alreadyReplaced counter in slot
                                                int slotReplacedCount = s.getRoom().getSlot().getAlreadyReplaced() != null?s.getRoom().getSlot().getAlreadyReplaced():0;
                                                if(slotReplacedCount > 6) {
                                                    continue;
                                                }

                                                s.getRoom().getSlot().setAlreadyReplaced(slotReplacedCount+1);
                                                 //           e.getValue().get(0).getTeachingAssistant().setDontChangeTheSchedule(true);
                                                s.setTeachingAssistant(entry.getValue().get(0).getTeachingAssistant());
                                                entry.getValue().add(s);
                                                e.getValue().remove(i);

                                                //comment - update taScheduleCount map used in createScheduleForMultipleDays
                                                int taCurrentScheduleCount = taScheduleCount.getOrDefault(e.getValue().get(0).getTeachingAssistant().getName(),0);
                                                if(taCurrentScheduleCount>0) {
                                                    taScheduleCount.put(e.getValue().get(0).getTeachingAssistant().getName(), taCurrentScheduleCount - 1);
                                                }

                             //              e.getValue().get(0).getTeachingAssistant().setDontChangeTheSchedule(true);

                                                //comment - update taSlotMap map used in createScheduleForMultipleDays
                                                taSlotMap.remove(e.getValue().get(0).getTeachingAssistant().getName() +"_"+s.getRoom().getSlot().getDay()+"_"+s.getRoom().getSlot().getStartTime());

                                                if (taScheduleCount.containsKey(entry.getValue().get(0).getTeachingAssistant().getName())) {
                                                    taScheduleCount.put(entry.getValue().get(0).getTeachingAssistant().getName(), taScheduleCount.get(entry.getValue().get(0).getTeachingAssistant().getName()) + 1);
                                                } else {
                                                    taScheduleCount.put(entry.getValue().get(0).getTeachingAssistant().getName(), 1);

                                                }

                                                //comment - update taSlotMap map used in createScheduleForMultipleDays
                                                taSlotMap.put(entry.getValue().get(0).getTeachingAssistant().getName() +"_"+s.getRoom().getSlot().getDay()+"_"+s.getRoom().getSlot().getStartTime(),true);

                                                createScheduleForMultipleDays(e.getValue().get(0).getTeachingAssistant().getName());
                                                allotedSlots++;
                                            }
                                        }
                                    }
                                createScheduleForMultipleDays(entry.getValue().get(0).getTeachingAssistant().getName());
                            }
                        }
                    }
                    if(allotedSlots<entry.getValue().get(0).getTeachingAssistant().getTAHours()){
                        createScheduleForMultipleDays(entry.getValue().get(0).getTeachingAssistant().getName());
                        System.out.println("Schedule is not feasible as classes cant be given to "+entry.getValue().get(0).getTeachingAssistant().getName());
                        break;
                    }
                }
            }
        }


    }

    private boolean checkIfSameDayConsecutiveSlotAsssignedInDifferentRoom(List<Schedule> value, Schedule s) {

        for(Schedule allotedSchedule:value)
        {
            if(allotedSchedule.getRoom().getSlot().getDay()==s.getRoom().getSlot().getDay() && !allotedSchedule.getRoom().equals(s.getRoom()) && lessThanOrEqual(allotedSchedule.getRoom(),s.getRoom(),true)){
                return true;
            }

        }
        return false;




    }

    private void completeScheduleForTANotCoveredTillNow() {
        Map<String, List<Schedule>> tempMap=new HashMap<>();

        TeachingAssistant taToCover=new TeachingAssistant();
        for (TeachingAssistant ta:taList
        ) {
            if(!taSchedules.containsKey(ta.getName()))
            {
                taToCover=ta;
            }
        }

        if(taToCover.getTAHours()==null)
            return;
        if(taToCover.getTAHours()<=taScheduleCount.get(taToCover.getName()))
            return;
        //  for (Map.Entry<String, List<Schedule>> entry : taSchedules.entrySet()) {
        int allotedSlots = 0;

       // while(taToCover.getTAHours() > allotedSlots){
            for(int count=0;count<taToCover.getPreferredSlot().size();count++){
                Slot preferredSlot=taToCover.getPreferredSlot().get(count);
                String slotStr=preferredSlot.getDay()+"_"+preferredSlot.getStartTime();

                //comment - check if picked slot can be alloted or not
                if(taSchedules.containsKey(taToCover.getName()))
                    if(!checkIfSlotInSequenceAndSingleDayPossible(taSchedules.get(taToCover.getName()),preferredSlot)) {
                        continue;
                    }
                try {
                    for (Map.Entry<String, List<Schedule>> e : taSchedules.entrySet()) {

                        if (taToCover.getTAHours() > allotedSlots)
                            for (int i = 0; i < e.getValue().size(); i++) {
                                if (e.getValue().get(i).getSlotPatternStr().endsWith(slotStr)) {
                                    if (e.getValue().get(i).getTeachingAssistant().getName().equals(taToCover.getName()))
                                        break;
                                    else {
                                        if (e.getValue().get(i).getTeachingAssistant().getDontChangeTheSchedule()
                                                || e.getValue().size() == 1
                                        //       || e.getValue().get(i).getTeachingAssistant().getAreClassesAllotedInSingleDay()
                                        ) //e.getValue().get(i).getTeachingAssistant().getAreClassesAllotedInSingleDay())
                                            continue;
                                        Schedule s = new Schedule();
                                        s = e.getValue().get(i);

                                        //check if the ta whom we want this new slot alloted to, already has a slot booked for the same time.
                                        if (taSlotMap.get(taToCover.getName() + "_" + s.getRoom().getSlot().getDay() + "_" + s.getRoom().getSlot().getStartTime()) != null) {
                                            continue;
                                        }

//                                        if(taSchedules.containsKey(taToCover.getName())) {
//                                            if (checkIfSameDayConsecutiveSlotAsssignedInDifferentRoom(taSchedules.get(taToCover.getName()), s))
//                                                continue;
//                                        }

                                        int slotReplacedCount = s.getRoom().getSlot().getAlreadyReplaced() != null ? s.getRoom().getSlot().getAlreadyReplaced() : 0;
                                        if (slotReplacedCount > 6) {
                                            continue;
                                        }
                                        s.getRoom().getSlot().setAlreadyReplaced(slotReplacedCount + 1);
                         //                e.getValue().get(0).getTeachingAssistant().setDontChangeTheSchedule(true);
                                        s.setTeachingAssistant(taToCover);
                                        if (tempMap.containsKey(taToCover.getName())) {

                                            List<Schedule> existingSchedule = tempMap.get(taToCover.getName());
                                            existingSchedule.add(s);
                                            tempMap.put(taToCover.getName(), existingSchedule);
                                        } else {
                                            List<Schedule> newSchedule = new ArrayList<>();
                                            newSchedule.add(s);
                                            tempMap.put(taToCover.getName(), newSchedule);
                                            //taSchedules.put(taToCover.getName(), newSchedule);

                                        }
                                        List<Schedule> tempScheduleListForDeletion=e.getValue();
                                        tempScheduleListForDeletion.remove(i);
                                        e.setValue(tempScheduleListForDeletion);

                                //      e.getValue().get(0).getTeachingAssistant().setDontChangeTheSchedule(true);
                                        int taCurrentScheduleCount = taScheduleCount.getOrDefault(e.getValue().get(0).getTeachingAssistant().getName(), 0);
                                        if (taCurrentScheduleCount > 0) {
                                            taScheduleCount.put(e.getValue().get(0).getTeachingAssistant().getName(), taCurrentScheduleCount - 1);
                                        }

                                        taSlotMap.remove(e.getValue().get(0).getTeachingAssistant().getName() + "_" + s.getRoom().getSlot().getDay() + "_" + s.getRoom().getSlot().getStartTime());

                                        if (taScheduleCount.containsKey(taToCover.getName())) {
                                            taScheduleCount.put(taToCover.getName(), taScheduleCount.get(taToCover.getName()) + 1);
                                        } else {
                                            taScheduleCount.put(taToCover.getName(), 1);

                                        }

                                        taSlotMap.put(taToCover.getName() + "_" + s.getRoom().getSlot().getDay() + "_" + s.getRoom().getSlot().getStartTime(), true);

                                        createScheduleForMultipleDays(e.getValue().get(0).getTeachingAssistant().getName());
                                        allotedSlots++;
                                    }
                                }
                            }

                    }


                    taSchedules.putAll(tempMap);
                }catch (Exception e){
                   throw e;
                }
            }
            if(allotedSlots<taToCover.getTAHours()) {

                for (int count = 0; count < taToCover.getAvailableSlot().size(); count++) {
                    Slot availableSlot = taToCover.getAvailableSlot().get(count);
                    String slotStr = availableSlot.getDay() + "_" + availableSlot.getStartTime();
                    if(taSchedules.containsKey(taToCover.getName()))
                        if(!checkIfSlotInSequenceAndSingleDayPossible(taSchedules.get(taToCover.getName()),availableSlot)) {
                            continue;
                        }
                    try {
                        for (Map.Entry<String, List<Schedule>> e : taSchedules.entrySet()) {
                            if (taToCover.getTAHours() > allotedSlots)
                                for (int i = 0; i < e.getValue().size(); i++) {
                                    if (e.getValue().get(i).getSlotPatternStr().endsWith(slotStr)) {
                                        if (e.getValue().get(i).getTeachingAssistant().getName().equals(taToCover.getName()))
                                            break;
                                        else {
                                            if (e.getValue().get(0).getTeachingAssistant().getDontChangeTheSchedule()
                                                    || e.getValue().size() == 1
                                              //     || e.getValue().get(0).getTeachingAssistant().getAreClassesAllotedInSingleDay()
                                            ) //e.getValue().get(i).getTeachingAssistant().getAreClassesAllotedInSingleDay())
                                                continue;
                                            Schedule s = new Schedule();
                                            s = e.getValue().get(i);

                                            //comment - if same day time is already alloted to TA than skip that schedule
                                            if (taSlotMap.get(taToCover.getName() + "_" + s.getRoom().getSlot().getDay() + "_" + s.getRoom().getSlot().getStartTime()) != null) {
                                                continue;
                                            }

//                                            if(taSchedules.containsKey(taToCover.getName())) {
//                                                if (checkIfSameDayConsecutiveSlotAsssignedInDifferentRoom(taSchedules.get(taToCover.getName()), s))
//                                                    continue;
//                                            }

                                            //comment - added alreadyReplaced counter in slot
                                            int slotReplacedCount = s.getRoom().getSlot().getAlreadyReplaced() != null ? s.getRoom().getSlot().getAlreadyReplaced() : 0;
                                            if (slotReplacedCount > 6) {
                                                continue;
                                            }

                                            s.getRoom().getSlot().setAlreadyReplaced(slotReplacedCount + 1);
                            //                    e.getValue().get(0).getTeachingAssistant().setDontChangeTheSchedule(true);


                                            s.setTeachingAssistant(taToCover);

                                            if (taSchedules.containsKey(taToCover.getName())) {

                                                List<Schedule> existingSchedule = taSchedules.get(taToCover.getName());
                                                existingSchedule.add(s);
                                                taSchedules.put(taToCover.getName(), existingSchedule);
                                            } else {
                                                List<Schedule> newSchedule = new ArrayList<>();
                                                newSchedule.add(s);
                                                taSchedules.put(taToCover.getName(), newSchedule);

                                            }
                                            e.getValue().remove(i);

                                            //comment - update taScheduleCount map used in createScheduleForMultipleDays
                                            int taCurrentScheduleCount = taScheduleCount.getOrDefault(e.getValue().get(0).getTeachingAssistant().getName(), 0);
                                            if (taCurrentScheduleCount > 0) {
                                                taScheduleCount.put(e.getValue().get(0).getTeachingAssistant().getName(), taCurrentScheduleCount - 1);
                                            }

                               //             e.getValue().get(0).getTeachingAssistant().setDontChangeTheSchedule(true);

                                            //comment - update taSlotMap map used in createScheduleForMultipleDays
                                            taSlotMap.remove(e.getValue().get(0).getTeachingAssistant().getName() + "_" + s.getRoom().getSlot().getDay() + "_" + s.getRoom().getSlot().getStartTime());

                                            if (taScheduleCount.containsKey(taToCover.getName())) {
                                                taScheduleCount.put(taToCover.getName(), taScheduleCount.get(taToCover.getName()) + 1);
                                            } else {
                                                taScheduleCount.put(taToCover.getName(), 1);

                                            }

                                            //comment - update taSlotMap map used in createScheduleForMultipleDays
                                            taSlotMap.put(taToCover.getName() + "_" + s.getRoom().getSlot().getDay() + "_" + s.getRoom().getSlot().getStartTime(), true);

                                            createScheduleForMultipleDays(e.getValue().get(0).getTeachingAssistant().getName());
                                            allotedSlots++;
                                        }
                                    }
                                }
                            createScheduleForMultipleDays(taToCover.getName());
                        }
                    }catch (Exception e){

                    }
                }
            }
//            if(allotedSlots<taToCover.getTAHours()){
//                System.out.println("Schedule is not feasible as classes cant be given to "+taToCover.getName());
//                break;
//            }


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
        font.setColor(new XSSFColor(Color.BLUE));


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

//                        List<Schedule> alreadyAllotedSchedules = new ArrayList<>();
//                        if(taSchedules.get(ta.getName()) != null) {
//                            alreadyAllotedSchedules = taSchedules.get(ta.getName());
//                        }
//                        if(possibleSlots.size()>0) {
//                            for (Schedule alreadyAllotedSingleSchedule : alreadyAllotedSchedules) {
//                                int timeDurationOfOneSlot=Integer.valueOf(alreadyAllotedSingleSchedule.getRoom().getSlot().getEndTime())-Integer.valueOf(alreadyAllotedSingleSchedule.getRoom().getSlot().getStartTime());
//                                for (Iterator<Room> iterator = possibleSlots.iterator(); iterator.hasNext(); ) {
//                                    Room singlePossibleSlot = iterator.next();
//
//                                    if (alreadyAllotedSingleSchedule.getRoom().getSlot().getDay() == singlePossibleSlot.getSlot().getDay() && (Math.abs(Integer.valueOf(alreadyAllotedSingleSchedule.getRoom().getSlot().getEndTime()) - Integer.valueOf(singlePossibleSlot.getSlot().getStartTime()))<timeDurationOfOneSlot || Math.abs((Integer.valueOf(alreadyAllotedSingleSchedule.getRoom().getSlot().getStartTime()) - Integer.valueOf(singlePossibleSlot.getSlot().getEndTime()))) <timeDurationOfOneSlot )  && singlePossibleSlot.getRoomNo() != alreadyAllotedSingleSchedule.getRoom().getRoomNo()) {
//                                        iterator.remove();
//                                    }
//
//
//
//
//                                }
//                            }
//
//                            for (Iterator<Room> iterator = possibleSlots.iterator(); iterator.hasNext(); ) {
//                                Room singlePossibleSlot = iterator.next();
//                                int timeDurationOfOneSlot=Integer.valueOf(singlePossibleSlot.getSlot().getEndTime())-Integer.valueOf(singlePossibleSlot.getSlot().getStartTime());
//
//                                for (Room possibleSlot:possibleSlots
//                                ) {
//                                    if (singlePossibleSlot.getSlot().getDay() == possibleSlot.getSlot().getDay() && (Math.abs(Integer.valueOf(singlePossibleSlot.getSlot().getEndTime()) - Integer.valueOf(possibleSlot.getSlot().getStartTime()))<timeDurationOfOneSlot || Math.abs((Integer.valueOf(singlePossibleSlot.getSlot().getStartTime()) - Integer.valueOf(possibleSlot.getSlot().getEndTime()))) <timeDurationOfOneSlot )  && singlePossibleSlot.getRoomNo() != possibleSlot.getRoomNo()) {
//
//                                        iterator.remove();
//                                    }
//                                }
//
//
//
//                            }
//                        }


                        if (possibleSlots.size() == ta.getTAHours()) {
                            singleDaySlotFound = true;
                            taScheduleCount.put(ta.getName(), ta.getTAHours());
                            break;
                        }
                    }
                }

//                List<Schedule> alreadyAllotedSchedules = new ArrayList<>();
//                if(taSchedules.get(ta.getName()) != null) {
//                    alreadyAllotedSchedules = taSchedules.get(ta.getName());
//                }
//                if(possibleSlots.size()>0) {
//                    for (Schedule alreadyAllotedSingleSchedule : alreadyAllotedSchedules) {
//                        int timeDurationOfOneSlot=Integer.valueOf(alreadyAllotedSingleSchedule.getRoom().getSlot().getEndTime())-Integer.valueOf(alreadyAllotedSingleSchedule.getRoom().getSlot().getStartTime());
//                        for (Iterator<Room> iterator = possibleSlots.iterator(); iterator.hasNext(); ) {
//                            Room singlePossibleSlot = iterator.next();
//
//                            if (alreadyAllotedSingleSchedule.getRoom().getSlot().getDay() == singlePossibleSlot.getSlot().getDay() && (Math.abs(Integer.valueOf(alreadyAllotedSingleSchedule.getRoom().getSlot().getEndTime()) - Integer.valueOf(singlePossibleSlot.getSlot().getStartTime()))<timeDurationOfOneSlot || Math.abs((Integer.valueOf(alreadyAllotedSingleSchedule.getRoom().getSlot().getStartTime()) - Integer.valueOf(singlePossibleSlot.getSlot().getEndTime()))) <timeDurationOfOneSlot )  && singlePossibleSlot.getRoomNo() != alreadyAllotedSingleSchedule.getRoom().getRoomNo()) {
//                                iterator.remove();
//                            }
//
//
//
//
//                        }
//                    }
//
//                    for (Iterator<Room> iterator = possibleSlots.iterator(); iterator.hasNext(); ) {
//                        Room singlePossibleSlot = iterator.next();
//                        int timeDurationOfOneSlot=Integer.valueOf(singlePossibleSlot.getSlot().getEndTime())-Integer.valueOf(singlePossibleSlot.getSlot().getStartTime());
//
//                        for (Room possibleSlot:possibleSlots
//                        ) {
//                            if (singlePossibleSlot.getSlot().getDay() == possibleSlot.getSlot().getDay() && (Math.abs(Integer.valueOf(singlePossibleSlot.getSlot().getEndTime()) - Integer.valueOf(possibleSlot.getSlot().getStartTime()))<timeDurationOfOneSlot || Math.abs((Integer.valueOf(singlePossibleSlot.getSlot().getStartTime()) - Integer.valueOf(possibleSlot.getSlot().getEndTime()))) <timeDurationOfOneSlot )  && singlePossibleSlot.getRoomNo() != possibleSlot.getRoomNo()) {
//
//                                iterator.remove();
//                            }
//                        }
//
//
//
//                    }
//                }


                if(singleDaySlotFound) {
                    for(Room m: possibleSlots) {
                        taSlotMap.put(ta.getName()+"_"+m.getSlot().getDay()+"_"+m.getSlot().getStartTime(),true);
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
                        taSlotMap.put(ta.getName() +  "_" + m.getSlot().getDay() + "_" + m.getSlot().getStartTime(), true);
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
                    }
                }
            }
        }
    }

    private void createScheduleForMultipleDays(String name) {
        for(TeachingAssistant ta: taList) {
            if(name != null && name !=ta.getName()) {
                continue;
            }
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

                    List<Schedule> alreadyAllotedSchedules = new ArrayList<>();
                    if(taSchedules.get(ta.getName()) != null) {
                        alreadyAllotedSchedules = taSchedules.get(ta.getName());
                    }

                    for (Room m : possibleSlots) {


                        taSlotMap.put(ta.getName() + "_" + m.getSlot().getDay() + "_" + m.getSlot().getStartTime(), true);
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
                    }
                    if(taScheduleCount.containsKey(ta.getName()))
                    {
                        taScheduleCount.put(ta.getName(), taScheduleCount.get(ta.getName())+possibleSlots.size());
                    }
                    else
                        taScheduleCount.put(ta.getName(), possibleSlots.size());
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
        Room firstSlot = new Room();
        Room secondSlot=new Room();
        //sort availableSlots on startTime and day
        Collections.sort(availableSlots);
        for(Room r: availableSlots) {
//            boolean areSlotsAllotedForSameDayInDifferentRoom=false;
//            for(Room possibleSingleSlot:possibleSlots){
//                if(r.getRoomNo()!=possibleSingleSlot.getRoomNo() && r.getSlot().getDay()==possibleSingleSlot.getSlot().getDay())
//                {
//                    areSlotsAllotedForSameDayInDifferentRoom=true;
//                    break;
//                }
//            }
//            if(areSlotsAllotedForSameDayInDifferentRoom)
//                continue;
            if(count<=hours) {
                if (possibleSlots.size() == 0) {
                    possibleSlots.add(r);
                    this.setPreviousSlot(firstSlot, r);
                    count++;
                }

                else {
                    boolean sameSlotGettingBookedAgain=false;
                    for(Room singlePossible:possibleSlots)
                    {
                        System.out.println((singlePossible.getSlot().getDay()==r.getSlot().getDay()));
                        System.out.println((Integer.valueOf(singlePossible.getSlot().getStartTime())-Integer.valueOf(r.getSlot().getStartTime())==0));
                        if((singlePossible.getSlot().getDay()==r.getSlot().getDay()) && (Integer.valueOf(singlePossible.getSlot().getStartTime())-Integer.valueOf(r.getSlot().getStartTime())==0)){
                            sameSlotGettingBookedAgain=true;
                            break;
                        }
                    }
                    if(sameSlotGettingBookedAgain)
                    {
                        continue;
                    }

                    if (count == 3) {
                        if(this.lessThanOrEqual(r,secondSlot,false) && this.lessThanOrEqual(secondSlot,firstSlot,false))
                        {continue;}
                        else   if(this.lessThanOrEqual(r,firstSlot,false) && this.lessThanOrEqual(firstSlot,secondSlot,false))
                        {continue;}


                        else {
                            possibleSlots.add(r);
                            this.setPreviousSlot(firstSlot, r);
                            count++;
                        }
                    } else {
                        //if (possibleSlots.size() == 1) {
                            this.setPreviousSlot(firstSlot,possibleSlots.get(0));

                            possibleSlots.add(r);

                            this.setPreviousSlot(secondSlot, r);
                            count++;
                       // }
//                        if (this.lessThanOrEqual(r,firstSlot,true)) {
//                            continue;
//                        } else {
//                            possibleSlots.add(r);
//                            this.setPreviousSlot(firstSlot, r);
//                            count++;
//                        }
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
            boolean sameSlotGettingBookedAgain=false;
            for(Room singlePossible:possibleSlots)
            {
                System.out.println((singlePossible.getSlot().getDay()==r.getSlot().getDay()));
                System.out.println((Integer.valueOf(singlePossible.getSlot().getStartTime())-Integer.valueOf(r.getSlot().getStartTime())==0));
                if((singlePossible.getSlot().getDay()==r.getSlot().getDay()) && (Integer.valueOf(singlePossible.getSlot().getStartTime())-Integer.valueOf(r.getSlot().getStartTime())==0)){
                    sameSlotGettingBookedAgain=true;
                    break;
                }
            }
            if(sameSlotGettingBookedAgain)
            {
                continue;
            }

//            boolean areSlotsAllotedForSameDayInDifferentRoom=false;
//            for(Room possibleSingleSlot:possibleSlots){
//                if(r.getRoomNo()!=possibleSingleSlot.getRoomNo() && r.getSlot().getDay()==possibleSingleSlot.getSlot().getDay())
//                {
//                    areSlotsAllotedForSameDayInDifferentRoom=true;
//                    break;
//                }
//            }
//            if(areSlotsAllotedForSameDayInDifferentRoom)
//                continue;
            if(count<=hours && count<3) {
                if (possibleSlots.size() == 0) {
                    possibleSlots.add(r);
                    count++;
                    this.setPreviousSlot(previousSlot, r);
                }
                else {
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
            boolean sameSlotGettingBookedAgain=false;
            for(Room singlePossible:possibleSlots)
            {
                System.out.println((singlePossible.getSlot().getDay()==r.getSlot().getDay()));
                System.out.println((Integer.valueOf(singlePossible.getSlot().getStartTime())-Integer.valueOf(r.getSlot().getStartTime())==0));
                if((singlePossible.getSlot().getDay()==r.getSlot().getDay()) && (Integer.valueOf(singlePossible.getSlot().getStartTime())-Integer.valueOf(r.getSlot().getStartTime())==0)){
                    sameSlotGettingBookedAgain=true;
                    break;
                }
            }
            if(sameSlotGettingBookedAgain)
            {
                continue;
            }

//            boolean areSlotsAllotedForSameDayInDifferentRoom=false;
//            for(Room possibleSingleSlot:possibleSlots){
//                int timeSlotDuration=Math.abs(Integer.valueOf(r.getSlot().getEndTime())- Integer.valueOf((r.getSlot().getStartTime())));
//
//                if(r.getRoomNo()!=possibleSingleSlot.getRoomNo() && r.getSlot().getDay()==possibleSingleSlot.getSlot().getDay() && (Math.abs(Integer.valueOf(r.getSlot().getEndTime())- Integer.valueOf((possibleSingleSlot.getSlot().getStartTime()))) <timeSlotDuration||Math.abs(Integer.valueOf(possibleSingleSlot.getSlot().getEndTime())- Integer.valueOf((r.getSlot().getStartTime())))<timeSlotDuration))
//                {
//                    areSlotsAllotedForSameDayInDifferentRoom=true;
//                    break;
//                }
//            }
//            if(areSlotsAllotedForSameDayInDifferentRoom)
//                continue;
            if(count<=hours && taSlotMap.get(taName+"_"+r.getSlot().getDay()+"_"+r.getSlot().getStartTime()) == null && daysBooked.getOrDefault(r.getSlot().getDay(),0) < 2) {
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
        int timeSlotDuration=Integer.valueOf(r2.getSlot().getEndTime())- Integer.valueOf((r2.getSlot().getStartTime()));
        if(r1.getSlot().getDay() != r2.getSlot().getDay()) {
            return false;
        }
        if (Integer.valueOf(r1.getSlot().getStartTime()) - Integer.valueOf(r2.getSlot().getStartTime())==0) {
            System.out.println(Integer.valueOf(r1.getSlot().getStartTime()));
            System.out.println(Integer.valueOf(r2.getSlot().getStartTime()));
            return true;
        }

        if ((Integer.valueOf(r1.getSlot().getStartTime()) - Integer.valueOf((r2.getSlot().getStartTime())) < timeSlotDuration)) {
            return true;
        }
        if ((Integer.valueOf(r1.getSlot().getStartTime()) - Integer.valueOf((r2.getSlot().getEndTime())) < timeSlotDuration)) {
            return true;
        }
        if (Math.abs(Integer.valueOf(r2.getSlot().getStartTime()) - Integer.valueOf((r1.getSlot().getEndTime()))) < timeSlotDuration) {
            return true;
        }
        if (Math.abs(Integer.valueOf(r2.getSlot().getStartTime()) - Integer.valueOf((r1.getSlot().getStartTime()))) < timeSlotDuration) {
            return true;
        }
//        if (startTime && (Integer.valueOf(r1.getSlot().getStartTime()) - Integer.valueOf((r2.getSlot().getStartTime())) < timeSlotDuration)) {
//            return true;
//        }
//        if (!startTime && (Integer.valueOf(r1.getSlot().getStartTime()) - Integer.valueOf((r2.getSlot().getEndTime())) < timeSlotDuration)) {
//            return true;
//        }
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

    private Boolean checkIfSlotInSequenceAndSingleDayPossible(List<Schedule> scheduleList, Slot s) {
        Boolean isSingleDay = scheduleList.get(0).getTeachingAssistant().getSingleDayPossible();
        Set<Integer> days = new HashSet<>();
        List<Slot> slots = new ArrayList<>();
        for(Schedule schedule: scheduleList) {
            slots.add(schedule.getRoom().getSlot());
        }
        slots.add(s);
        Collections.sort(slots);
        for(Slot sl: slots) {
            days.add(sl.getDay());
        }

        if(days.size() == 1 && slots.size() == 3 && !isSingleDay) {
            return false;
        }

        if(days.size() == 1 && slots.size() == 3)
        {
            /*
            r1 s1,e1
            r2 s2,e2
            r3 s3,e3

            r1 s1,e1
            r3 s3,e3
            r2 s2,e2

            r2 s2,e2
            r1 s1,e1
            r3 s3,e3

            r2 s2,e2
            r3 s3,e3
            r1 s1,e1

            r3 s3,e3
            r1 s1,e1
            r2 s2,e2

            r3 s3,e3
            r2 s2,e2
            r1 s1,e1

             */
            int timeSlotDuration=Integer.valueOf(slots.get(0).getEndTime())-Integer.valueOf(slots.get(0).getStartTime());
            if( (Math.abs(Integer.valueOf(slots.get(1).getStartTime())-Integer.valueOf(slots.get(0).getEndTime()))<timeSlotDuration)&&
                    (Math.abs(Integer.valueOf(slots.get(2).getStartTime())-Integer.valueOf(slots.get(1).getEndTime()))<timeSlotDuration))
            {
                return false;
            }
            if( (Math.abs(Integer.valueOf(slots.get(2).getStartTime())-Integer.valueOf(slots.get(0).getEndTime()))<timeSlotDuration)&&
                    (Math.abs(Integer.valueOf(slots.get(1).getStartTime())-Integer.valueOf(slots.get(2).getEndTime()))<timeSlotDuration))
            {
                return false;
            }
            if( (Math.abs(Integer.valueOf(slots.get(0).getStartTime())-Integer.valueOf(slots.get(1).getEndTime()))<timeSlotDuration)&&
                    (Math.abs(Integer.valueOf(slots.get(2).getStartTime())-Integer.valueOf(slots.get(0).getEndTime()))<timeSlotDuration))
            {
                return false;
            }
            if( (Math.abs(Integer.valueOf(slots.get(2).getStartTime())-Integer.valueOf(slots.get(1).getEndTime()))<timeSlotDuration)&&
                    (Math.abs(Integer.valueOf(slots.get(0).getStartTime())-Integer.valueOf(slots.get(2).getEndTime()))<timeSlotDuration))
            {
                return false;
            }
            if( (Math.abs(Integer.valueOf(slots.get(0).getStartTime())-Integer.valueOf(slots.get(2).getEndTime()))<timeSlotDuration)&&
                    (Math.abs(Integer.valueOf(slots.get(1).getStartTime())-Integer.valueOf(slots.get(0).getEndTime()))<timeSlotDuration))
            {
                return false;
            }
            if( (Math.abs(Integer.valueOf(slots.get(1).getStartTime())-Integer.valueOf(slots.get(2).getEndTime()))<timeSlotDuration)&&
                    (Math.abs(Integer.valueOf(slots.get(0).getStartTime())-Integer.valueOf(slots.get(1).getEndTime()))<timeSlotDuration))
            {
                return false;
            }

        }

        return true;
    }

    private void clear() {
        dayRoomSlotMap.clear();
        taSlotMap.clear();
        roomSlotBookedMap.clear();
        taList.clear();
        roomList.clear();
        taScheduleCount.clear();
        taSchedules.clear();
    }

}