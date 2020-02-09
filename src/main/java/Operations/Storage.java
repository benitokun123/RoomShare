package Operations;

import CustomExceptions.RoomShareException;
import Enums.*;
import Model_Classes.Assignment;
import Model_Classes.Leave;
import Model_Classes.Meeting;
import Model_Classes.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.simple.*;

/**
 * Performs storage operations such as writing and reading from a .txt file.
 */
public class Storage {

    /**
     * Constructor for the Storage class.
     */
    public Storage() {
    }

    /**
     * Convert a Task to a JsonObject which cab be serialized by Jsoner.
     * @param task the task to be converted
     * @return the JsonObject version of a task
     */
    private JsonObject convertTaskToJsonObject(Task task) {
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy hh:mma");
        JsonObject jsonObject = new JsonObject();

        jsonObject.put("description", task.getDescription());
        jsonObject.put("isDone", task.getDone());
        jsonObject.put("date", f.format(task.getDate()));
        jsonObject.put("priority", task.getPriority().toString());
        jsonObject.put("assignee", task.getAssignee());
        jsonObject.put("recurrence", task.getRecurrenceSchedule().toString());
        jsonObject.put("hasRecurring", task.hasRecurring());
        jsonObject.put("overdue", task.getOverdue());

        if (task instanceof Assignment) {
            jsonObject.put("type","as");
            jsonObject.put("subTask", ((Assignment) task).getSubTasks());
        } else if (task instanceof Meeting) {
            jsonObject.put("type","mt");
            jsonObject.put("duration", ((Meeting) task).getDuration());
            jsonObject.put("timeUnit", ((Meeting) task).getTimeUnit().toString());
        } else if (task instanceof Leave) {
            jsonObject.put("type","lv");
            jsonObject.put("user", ((Leave) task).getUser());
            jsonObject.put("start", ((Leave) task).getStartDate().toString());
            jsonObject.put("end", ((Leave) task).getEndDate().toString());
        }
        return jsonObject;
    }

    /**
     * Convert a JsonObject to a Task which can be later addded to a task list.
     * @param jsonObject the JsonObject to be converted
     * @return a Task equivalent of the JsonObject
     * @throws RoomShareException if there's any error processing the parameters of the JsonObject
     */
    private Task convertJsonObjectToTask(JsonObject jsonObject) throws RoomShareException{
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy hh:mma");
        String type = jsonObject.getString("type");
        String description = jsonObject.getString("description");
        boolean isDone = jsonObject.getBoolean("isDone");

        Date date;
        try {
            date = f.parse(jsonObject.getString("date"));
        } catch (ParseException e) {
            throw new RoomShareException(ExceptionType.loadError);
        }

        Priority priority = Priority.valueOf(jsonObject.getString("priority"));
        String assignee = jsonObject.getString("assignee");
        boolean hasRecurring = jsonObject.getBoolean("hasRecurring");
        boolean overdue = jsonObject.getBoolean("overdue");
        RecurrenceScheduleType recurrence = RecurrenceScheduleType.valueOf(jsonObject.getString("recurrence"));

        if (type.equals("as")) {
            ArrayList<String> subTask = (ArrayList<String>) jsonObject.get("subTask");
            Assignment as = new Assignment(description,isDone,date,priority,assignee,hasRecurring,overdue,recurrence);
            as.addSubTasks(subTask);
            return as;
        } else if (type.equals("mt")) {
            int duration = jsonObject.getInteger("duration");
            TimeUnit timeUnit = TimeUnit.valueOf(jsonObject.getString("timeUnit"));
            Meeting mt = new Meeting(description,isDone,date,priority,assignee,hasRecurring,overdue,recurrence);
            mt.setDuration(duration,timeUnit);
            return mt;
        } else {
            Date start,end;
            try {
                start = f.parse(jsonObject.getString("start"));
                end = f.parse(jsonObject.getString("end"));
            } catch (ParseException e) {
                throw new RoomShareException(ExceptionType.loadError);
            }
            Leave lv = new Leave(description,isDone,date,priority,assignee,hasRecurring,overdue,recurrence);
            lv.setStartDate(start);
            lv.setEndDate(end);
            return lv;
        }
    }

    /**
     * Convert a List of Task to a JsonArray which can be serialized by Jsoner.
     * @param list the List to be converted
     * @return the JsonArray equivalent of the List
     */
    private JsonArray convertListToJsonArray(ArrayList<Task> list) {
        JsonArray jsonArray = new JsonArray();
        for (Task t: list) {
            jsonArray.add(convertTaskToJsonObject(t));
        }
        return jsonArray;
    }

    /**
     * Convert a Task List to a JsonArray and write it into a JSON file.
     * @param list the List to be written
     * @param fileName the file destination
     * @throws RoomShareException if there's any error processing the List
     */
    public void writeJSONFile(ArrayList<Task> list, String fileName) throws RoomShareException{
        JsonArray JSONList = convertListToJsonArray(list);
        try (FileWriter file = new FileWriter(fileName)) {
            Jsoner.serializeStrictly(JSONList,file);
        } catch (IOException e) {
            throw new RoomShareException(ExceptionType.writeError);
        }
    }

    /**
     * Load a JSON file and convert its info into a Task List
     * @param fileName the File to be processed
     * @return the Task List derived from the file
     * @throws RoomShareException if there's any error processing the file
     */
    public ArrayList<Task> loadJSONFile(String fileName) throws RoomShareException{
        ArrayList<JsonObject> jsonList  = new ArrayList<>();
        ArrayList<Task> list = new ArrayList<>();
        try (FileReader file  = new FileReader(fileName)) {
            JsonArray jsonArray = (JsonArray) Jsoner.deserialize(file);
            for (Object object : jsonArray) {
                jsonList.add((JsonObject) object);
            }
            for (JsonObject jsonObject : jsonList) {
                list.add(convertJsonObjectToTask(jsonObject));
            }
        } catch (DeserializationException | IOException e) {
            throw new RoomShareException(ExceptionType.loadError);
        }
        return list;
    }

    /**
     * Create a new text file and write all information of the current task list to it.
     * @param list the current task list
     * @throws RoomShareException when there is an error in writing the log file
     */
    public String writeLogFile(ArrayList<Task> list) throws RoomShareException {
        String fileName = "log " + new Date().toString() + ".txt";
        fileName = fileName.replaceAll(" ", "_").replaceAll(":","_");
        String filePath = "logs\\" + fileName;
        String folderName = "logs";
        try {
            File file = new File(filePath);
            File folder = new File(folderName);
            if (!folder.exists()) folder.mkdir();
            file.createNewFile();
            PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8);
            for (Task t : list) {
                writer.println(t.toString());
            }
            writer.close();
        } catch (IOException e) {
            throw new RoomShareException(ExceptionType.logError);
        }
        return filePath;
    }

    /**
     * Extracts and converts all the information in the task object for storage
     * will format the time information for meeting and assignment tasks
     * Additional formatting will be done for recurring tasks to include recurrence schedule
     * returns a string with all the relevant information.
     *
     * @param task task object to be converted
     * @return time A String containing all the relevant information
     * @throws RoomShareException If there is any error in parsing the Date information.
     */
    public String convertForStorage(Task task) throws RoomShareException {
        try {
            String time = "";
            String[] prelimSplit = task.toString().split("\\(");
            String[] tempString = prelimSplit[2].split("\\s+");
            String year = tempString[6].substring(0, tempString[6].length() - 1);
            Date date = new SimpleDateFormat("MMM").parse(tempString[2]);
            DateFormat dateFormat = new SimpleDateFormat("MM");
            String month = dateFormat.format(date);
            String[] timeArray = tempString[4].split(":", 3);
            String day = tempString[3];
            time = day + "/" + month + "/" + year + " " + timeArray[0] + ":" + timeArray[1];
            return time;
        } catch (ParseException e) {
            throw new RoomShareException(ExceptionType.wrongFormat);
        }
    }

    /**
     * Extracts the time information from the leave class object for it to be able
     * to store in the data file to be saved.
     *
     * @param task Task object to be converted.
     * @return time A string with the correct formatting to be placed in the data file.
     * @throws RoomShareException If there is any error in parsing the Date information.
     */
    public String convertForStorageLeave(Task task) throws RoomShareException {
        try {
            String time;
            String[] prelimSplit = task.toString().split("\\(");
            String[] tempString = prelimSplit[2].split("\\s+");

            String fromYear = tempString[6].trim();
            String toYear = tempString[13].trim().substring(0, tempString[13].length() -1);

            Date fromMonth = new SimpleDateFormat("MMM").parse(tempString[2]);
            DateFormat dateFormatFromMonth = new SimpleDateFormat("MM");
            String fromMth = dateFormatFromMonth.format(fromMonth);
            Date toMonth = new SimpleDateFormat("MMM").parse(tempString[9]);
            DateFormat dateFormatToMonth = new SimpleDateFormat("MM");
            String toMth = dateFormatToMonth.format(toMonth);

            String[] fromTimeArray = tempString[4].split(":", 3);
            String[] toTimeArray = tempString[11].split(":", 3);

            String fromDay = tempString[3];
            String toDay = tempString[10];

            time = fromDay + "/" + fromMth + "/" + fromYear
                    + " " + fromTimeArray[0] + ":" + fromTimeArray[1] + "-"
                    + toDay + "/" + toMth + "/" + toYear
                    + " " + toTimeArray[0] + ":" + toTimeArray[1];
            return time;
        } catch (ParseException e) {
            throw new RoomShareException(ExceptionType.wrongFormat);
        }
    }
}


