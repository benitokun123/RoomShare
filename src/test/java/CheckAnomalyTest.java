import CustomExceptions.RoomShareException;
import Enums.TimeUnit;
import Model_Classes.Assignment;
import Model_Classes.Meeting;
import Operations.CheckAnomaly;
import Operations.Parser;
import Operations.Storage;
import Operations.TaskList;
import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckAnomalyTest {
    private static final Parser parser = new Parser();
    private static final Storage storage = new Storage();
    private static Meeting meeting1, meeting2, meeting3, meeting4, meeting5;
    private static Assignment assignment1, assignment2;
    private static Date at1, at2, at3, at4, at5, at6, at7;
    private static TaskList taskList;

    static {
        try {
            at1 = parser.formatDateDDMMYY("12/12/2019 17:00");
            at2 = parser.formatDateDDMMYY("12/12/2019 19:00");
            at3 = parser.formatDateDDMMYY("12/12/2019 10:00");
            at4 = parser.formatDateDDMMYY("12/12/2019 09:00");
            at5 = parser.formatDateDDMMYY("21/12/2019 13:00");
            at6 = parser.formatDateDDMMYY("22/12/2019 13:00");
            at7 = parser.formatDateDDMMYY("25/12/2019 13:00");
            taskList = new TaskList(storage.loadFile("test.txt"));
            meeting1 = new Meeting("test1", at1, 2, TimeUnit.hours);
            meeting2 = new Meeting("test2", at2);
            meeting3 = new Meeting("test3", at3);
            meeting4 = new Meeting("test4", at4, 2, TimeUnit.hours);
            meeting5 = new Meeting("test5", at5);
            assignment1 = new Assignment("test6", at6);
            assignment1.setAssignee("harry");
            assignment2 = new Assignment("test6", at7);
            assignment2.setAssignee("harry");
        } catch (RoomShareException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void durationClashOverlap() { assertEquals(0, new CheckAnomaly().isTimeClash(meeting1)); }

    @Test
    public void durationClashIntersect() { assertEquals(0, new CheckAnomaly().isTimeClash(meeting2)); }

    @Test
    public void fixedClashIntersect() { assertEquals(1, new CheckAnomaly().isTimeClash(meeting3)); }

    @Test
    public void fixedClashOverlap() { assertEquals(1, new CheckAnomaly().isTimeClash(meeting4)); }

    @Test
    public void noClash() { assertEquals(-1, new CheckAnomaly().isTimeClash(meeting5)); }

    @Test
    public void duplicateClash() { assertEquals(3, new CheckAnomaly().isDuplicate(assignment1)); }

    @Test
    public void noDuplicate() { assertEquals(-1, new CheckAnomaly().isTimeClash(assignment2)); }
}