package CustomExceptions;

import Enums.ExceptionType;

public class RoomShareException extends Exception {
    private static final String LINE = "___________________________________________________________________________________\n";

    private static final String OUT_OF_BOUNDS_TEXT = "\tIndex is out of Bounds!\n";
    private static final String ANOMALY_TEXT = "\tAnomaly Detected\n";
    private static final String EMPTY_LIST_TEXT = "\tList is empty\n";
    private static final String WRONG_FORMAT_TEXT = "\tWrong Format Detected\n";
    private static final String WRONG_PRIORITY_TEXT = "\tYou've entered wrong format of priority\n";
    private static final String SUB_TASK_TEXT = "\tOnly Assignments are supported with Subtasks\n";
    private static final String WRONG_TASK_TYPE_TEXT = "\tOnly meeting, assignment, or leave tag are accepted\n";
    private static final String EMPTY_DESCRIPTION_TEXT = "\tYou haven't included the description of you task\n";
    private static final String EMPTY_DATE_TEXT = "\tYou haven't included the date of your task\n";
    private static final String EMPTY_USER_TEXT = "\tYou haven't included the user of your task\n";
    private static final String EMPTY_TASK_TYPE_TEXT = "\tYou haven't specified the type of your task: assignment, meeting, or leave\n";
    private static final String WRITE_ERROR_TEXT = "\tError in writing file, cancelling write process...\n";
    private static final String WRONG_INDEX_FORMAT_TEXT = "\tThe index you've enter is in the wrong format\n";
    private static final String WRONG_TIME_FORMAT_TEXT = "\tYou've entered an invalid time format\n";
    private static final String WRONG_SORT_TYPE_TEXT = "\tPlease enter a valid sort type: "
            + "priority, alphabetical or deadline\n";
    private static final String LOG_ERROR_TEXT = "\tError writing to a new log file. Please try again.\n";
    private static final String NEGATIVE_AMOUNT_TEXT = "\tThe amount of time cannot be negative.\n";
    private static final String EMPTY_SUB_TASK = "\tYou haven't included your list of sub-tasks\n";
    private static final String DUPLICATE_SUB = "\tDuplicate subtask detected\n";
    private static final String LEAVE_DONE = "\tLeave cannot be set to done\n";
    private static final String INVALID_INPUT_TEXT = "\tYour input String seems to be wrong.\n"
            + "\tPlease check your formatting and ensure that the use of special characters are correct!\n";
    private static final String LOAD_ERROR_MESSAGE = "\terror in loading file: will be initialising empty list instead!\n";
    private static final String INVALID_DATE_MESSAGE = "\tThe date you've input is before the current date!\n";
    private static final String WRONG_DATE_FORMAT_TEXT = "\tYou've entered invalid date or time\n";
    private static final String EMPTY_INDEX = "\tPlease enter a valid index within the range of the list! Eg. reopen 1\n";
    private static final String INVALID_LEAVE_DATE_MESSAGE = "\tPlease check your dates for your leave!\n";
    private static final String NO_SUCH_SUBTASK = "\tSubtask does not exist!\n";
    private static final String ASSIGNEE_SET_TO_EVERYONE = "\tThere might have been an error when setting the assignee\n"
            + "\tIt could be an error in your entry of the assignee field\n"
            + "\tHowever, if you had intended to set the assignee to 'everyone', then ignore this message\n";
    public static final String INVALID_COMMAND_TEXT = "\tInvalid command! Type \"help\" to find out more about available commands\n";

    private String message;

    /**
     * Constructor for DukeException Exception.
     * Takes in the exception type thrown and prints out the specific error message
     * @param type type of exception detected
     */
    public RoomShareException(ExceptionType type) {
        switch (type) {

        case emptyUser:
            message = EMPTY_USER_TEXT;
            break;

        case emptyList:
            message = EMPTY_LIST_TEXT;
            break;

        case writeError:
            message = WRITE_ERROR_TEXT;
            break;

        case wrongIndexFormat:
            message = WRONG_INDEX_FORMAT_TEXT;
            break;

        case wrongTimeFormat:
            message = WRONG_TIME_FORMAT_TEXT;
            break;

        case wrongFormat:
            message = WRONG_FORMAT_TEXT;
            break;

        case outOfBounds:
            message = OUT_OF_BOUNDS_TEXT;
            break;

        case wrongPriority:
            message = WRONG_PRIORITY_TEXT;
            break;

        case subTaskError:
            message = SUB_TASK_TEXT;
            break;

        case wrongTaskType:
            message = WRONG_TASK_TYPE_TEXT;
            break;

        case emptyDescription:
            message = EMPTY_DESCRIPTION_TEXT;
            break;

        case emptyDate:
            message = EMPTY_DATE_TEXT;
            break;

        case emptyTaskType:
            message = EMPTY_TASK_TYPE_TEXT;
            break;

        case emptySubTask:
            message = EMPTY_SUB_TASK;
            break;

        case wrongSortFormat:
            message = WRONG_SORT_TYPE_TEXT;
            break;

        case logError:
            message = LOG_ERROR_TEXT;
            break;

        case negativeTimeAmount:
            message = NEGATIVE_AMOUNT_TEXT;
            break;

        case duplicateSubtask:
            message = DUPLICATE_SUB;
            break;
            
        case leaveDone:
            message = LEAVE_DONE;
            break;
            
        case invalidInputString:
            message = INVALID_INPUT_TEXT;
            break;

        case loadError:
            message = LOAD_ERROR_MESSAGE;
            break;

        case invalidDateError:
            message = INVALID_DATE_MESSAGE;
            break;

        case invalidDateRange:
            message = INVALID_LEAVE_DATE_MESSAGE;
            break;

        case wrongDateFormat:
            message = WRONG_DATE_FORMAT_TEXT;
            break;

        case emptyIndex:
            message = EMPTY_INDEX;
            break;

        case noSubtask:
            message = NO_SUCH_SUBTASK;
            break;

        case assigneeSetToEveyone:
            message = ASSIGNEE_SET_TO_EVERYONE;
            break;

        case invalidCommand:
            message = INVALID_COMMAND_TEXT;
            break;

        default:
            message = ANOMALY_TEXT;
            break;
        }
    }

    /**
     * toString() method returning the message of the Exception.
     * @return the message of the Exception
     */
    @Override
    public String toString() {
        return LINE + message + LINE;
    }
}
