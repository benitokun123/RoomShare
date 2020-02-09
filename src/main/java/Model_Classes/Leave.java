package Model_Classes;

import Enums.Priority;
import Enums.RecurrenceScheduleType;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An object class representing tasks the are leaves.
 * Stores the description as well as the start and end time of the leave.
 */

public class Leave extends Task {
    private Date from;
    private Date to;

    /**
     * constructor for the leave class.
     * @param description description of the leave
     * @param user the person who is taking leave
     * @param from the start date and time of the leave
     * @param to the end date and time for the leave
     */
    public Leave(String description, String user, Date from, Date to) {
        super(description, from);
        super.setAssignee(user);
        this.from = from;
        this.to = to;
    }

    public Leave(String description, boolean isDone, Date date,
                Priority priority, String assignee, boolean hasRecurring,
                boolean isOverdue, RecurrenceScheduleType recurrence) {
        super(description,isDone,date,priority,assignee,hasRecurring,isOverdue,recurrence);
    }

    /**
     * gets the start date of the leave.
     * @return the start date and time of the leave
     */
    public Date getStartDate() {
        return this.from;
    }

    /**
     * sets the start date of the leave.
     * @param date the start date and time of the leave
     */
    public void setStartDate(Date date) {
        this.from = date;
    }

    /**
     * gets the end date of the leave.
     * @return end date and time of the leave
     */
    public Date getEndDate() {
        return this.to;
    }

    /**
     * sets the end date of the leave.
     * @param date the end date and time of the leave
     */
    public void setEndDate(Date date) {
        this.to = date;
    }

    /**
     * gets the user who is being assigned to the leave.
     * @return user who is assigned the leave
     */
    @Override
    public String getAssignee() {
        return super.getAssignee();
    }

    /**
     * returns the information of the leave being taken.
     * @return String with the information of the leave.
     */
    @Override
    public String toString() {
        SimpleDateFormat f = new SimpleDateFormat("dd MMMM yyyy hh:mma");
        return "[L] " + super.getDescription() + " (" + super.getAssignee() + ")" + " (From: " + f.format(from) + " To: " + f.format(to) + ")";
    }

    /**
     * setter for user.
     * @param user name of user for the leave
     */
    public void setUser(String user) {
        super.setAssignee(user);
    }

    /**
     * getter for user.
     * @return the name of the user for the leave
     */
    public String getUser() {
        return super.getAssignee();
    }

}