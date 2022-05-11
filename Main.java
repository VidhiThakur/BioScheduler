package BioScheduler;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        // Call this file in order to create the schedule
        SchedularService schedularService=new SchedularService();
        schedularService.createSchedule();


    }
}
