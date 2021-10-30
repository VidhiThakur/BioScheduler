package BioScheduler;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Input {

    public void initialiseData(List<TeachingAssistant> taList, List<Room> roomList, Map<String, List<Room>> dayRoomSlotMap, List<String> roomsNameList,ArrayList<String> timeSlotsSortedArr) throws IOException {

        File file = new File("/home/vidhi/IdeaProjects/BioScheduler/src/Input.xlsx");   //creating a new file instance
        FileInputStream fis = null;   //obtaining bytes from the file
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//creating Workbook instance that refers to .xlsx file
        System.out.println(fis);
        XSSFWorkbook workbook=null;
        try {
            workbook = new XSSFWorkbook(fis);
            HashSet<String> mapOfClasses = new HashSet<String>();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return;
        }
        //  System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

        //  System.out.println("Retrieving Sheets using Iterator");
        Sheet sheet=workbook.getSheetAt(0);
        DataFormatter dataFormatter = new DataFormatter();
        //     System.out.println("\n\nIterating over Rows and Columns using for-each loop\n");
        //ArrayList<String> timeSlotsSortedArr=new ArrayList<>();
        ArrayList<String> classRooms=new ArrayList<>();
        for (Row row: sheet) {
            Cell firstCell=row.getCell(0);
            //   System.out.println(dataFormatter.formatCellValue(firstCell));
            if("TimeSlots".equals(dataFormatter.formatCellValue(firstCell))) {
                for (int count = 1; count < row.getLastCellNum(); count++) {
                    timeSlotsSortedArr.add(dataFormatter.formatCellValue(row.getCell(count)));


                }

            }
            if("ClassRooms".equals(dataFormatter.formatCellValue(firstCell))) {
                for (int count = 1; count < row.getLastCellNum(); count++) {
                    classRooms.add(dataFormatter.formatCellValue(row.getCell(count)));

                }

            }
        }

        Sheet sheet1=workbook.getSheetAt(1);
        int index=0;


        Row row=sheet1.getRow(index);

        while(index<(sheet1.getLastRowNum()))
        {

            if(sheet1.getRow(index)==null)
                break;
            String roomNo=dataFormatter.formatCellValue(sheet1.getRow(index).getCell(0));
            index++;
            row=sheet1.getRow(index);
            String timeSlot="";
            //     System.out.println(timeSlot);
            Integer countOfTimeslots=0;
            Room r = new Room();
            Slot s = new Slot();
            while(countOfTimeslots<timeSlotsSortedArr.size()) {
                timeSlot=dataFormatter.formatCellValue(row.getCell(0)).trim();
                for (int cell = 1; cell <= row.getLastCellNum(); cell++) {
                    //   System.out.println(dataFormatter.formatCellValue(row.getCell(cell)).trim());
                    if (dataFormatter.formatCellValue(row.getCell(cell)).trim().equals("Yes")) {
                        r=new Room();
                        s=new Slot();
                        r.setRoomNo(roomNo);
                        s.setDay(cell);
                        String[] timeSlotRange = timeSlot.split("-");
                        s.setStartTime(timeSlotRange[0]);
                        s.setEndTime(timeSlotRange[1]);
                        r.setSlot(s);
                        roomList.add(r);
                    }

                }
                if(index<sheet1.getLastRowNum())
                    row=sheet1.getRow(++index);
                countOfTimeslots++;
            }

        }


        Sheet sheet4=workbook.getSheetAt(3);
        index=0;
        row=sheet4.getRow(index);
        ArrayList<Slot> availableSlots=new ArrayList<>();
        ArrayList<Slot> preferredSlots=new ArrayList<>();
        Slot slot=new Slot();
        while(index<(sheet4.getLastRowNum()))
        {
            TeachingAssistant ta=new TeachingAssistant();

            if(sheet4.getRow(index)==null)
                break;
            String instructor=dataFormatter.formatCellValue(sheet4.getRow(index).getCell(0));
            if(instructor.isEmpty()||instructor=="")
                 break;
            ta.setName(dataFormatter.formatCellValue(sheet4.getRow(index).getCell(0)));

            index++;
            row=sheet4.getRow(index);
            String timeSlot="";
            //       System.out.println(timeSlot);
            Integer countOfTimeslots=0;

            while(countOfTimeslots<timeSlotsSortedArr.size()) {
                timeSlot=dataFormatter.formatCellValue(row.getCell(0)).trim();
                for (int cell = 1; cell <= row.getLastCellNum(); cell++) {
                    if(row.getCell(cell)==null)
                        break;
                    slot=new Slot();
                    if (dataFormatter.formatCellValue(row.getCell(cell)).trim().equals("1")) {
                        String[] timeSlotRange = timeSlot.split("-");
                        slot.setStartTime(timeSlotRange[0]);
                        slot.setEndTime(timeSlotRange[1]);
                        slot.setPreference(1);
                        slot.setDay(cell);
                        availableSlots.add(slot);
                    }
                    else if (dataFormatter.formatCellValue(row.getCell(cell)).trim().equals("2")) {
                        String[] timeSlotRange = timeSlot.split("-");
                        slot.setStartTime(timeSlotRange[0]);
                        slot.setEndTime(timeSlotRange[1]);
                        slot.setPreference(2);
                        slot.setDay(cell);
                        preferredSlots.add(slot);
                    }


                }
                if(index<sheet4.getLastRowNum())
                    row=sheet4.getRow(++index);
                countOfTimeslots++;
            }

            ta.setAvailableSlot(availableSlots);
            ta.setPreferredSlot(preferredSlots);
            ta.setTotalSlotsCount(availableSlots.size()+preferredSlots.size());
            taList.add(ta);
            availableSlots=new ArrayList<>();
            preferredSlots=new ArrayList<>();

        }
        //         System.out.println(instructorPref);

        Sheet sheet3=workbook.getSheetAt(2);
        //row=sheet3.getRow();
        index=1;
        System.out.println(sheet3.getLastRowNum());
        ArrayList<TeachingAssistant> taList1=new ArrayList<>();
        while(index<=(sheet3.getLastRowNum())) {
            row=sheet3.getRow(index);
            if(row==null)
                break;
            for(TeachingAssistant ta:taList)
            {
                if(ta.getName().equals(dataFormatter.formatCellValue(row.getCell(0))))
                {
                    ta.setTAHours(Integer.valueOf(dataFormatter.formatCellValue(row.getCell(1))));
                    ta.setMondayAvailable(dataFormatter.formatCellValue(row.getCell(2)).equals("Yes")?true:false);
                    ta.setSingleDayPossible(dataFormatter.formatCellValue(row.getCell(3)).equals("Yes")?true:false);
                    taList1.add(ta);
                }
            }
            index++;
        }
        System.out.println(taList);
        System.out.println(roomList);
        taList=taList1;
        for(Room r: roomList) {
            if(dayRoomSlotMap.get(r.getSlot().getDay()+"_"+r.getRoomNo())==null) {
                List<Room> ls = new ArrayList<>();
                ls.add(r);
                dayRoomSlotMap.put(r.getSlot().getDay()+"_"+r.getRoomNo(), ls);
            } else {
                List<Room> ls = dayRoomSlotMap.get(r.getSlot().getDay()+"_"+r.getRoomNo());
                ls.add(r);
                dayRoomSlotMap.put(r.getSlot().getDay()+"_"+r.getRoomNo(), ls);
            }
            if(!roomsNameList.contains(r.getRoomNo())) {
                roomsNameList.add(r.getRoomNo());
            }
        }
    }
}
