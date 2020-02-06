package Operations;

import CustomExceptions.RoomShareException;
import Enums.*;
import Model_Classes.Assignment;
import Model_Classes.Leave;
import Model_Classes.Meeting;
import Model_Classes.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * A class to perform operations on the task list in Duke.
 */
public class TaskList {
    private static final String COMPLETED_TASKS = "Completed Tasks:";
    private static final String YOUR_SEARCH_RETURNED_NO_RESULTS_TRY_SEARCHING_WITH_ANOTHER_KEYWORD = "    Your search returned no results.... Try searching with another keyword!";
    private static ArrayList<Task> tasks;
    private static SortType sortType = SortType.priority;

    /**
     * Constructor for the TaskList class.
     * takes in an ArrayList as the list of tasks to be operated on.
     * @param tasks ArrayList of Task objects to be operated on.
     */
    public TaskList(ArrayList<Task> tasks) {
        TaskList.tasks = tasks;
    }

    /**
     * Adds a new task into the task list.
     * @param newTask Task object to be added into the list of tasks
     */
    public void add(Task newTask) {
        tasks.add(newTask);
        sortTasks();
    }

    /**
     * Deletes a task from the list. Task to be deleted is specified by the index that is input into this method
     * Will not perform any operations if the index does not exist in the list.
     * @param index Index of task in the list to be deleted
     * @param deletedList temporary storage list for the deleted items so they can be restored
     * @throws RoomShareException If the index cannot be found in the list of tasks.
     */
    public void delete(int[] index, TempDeleteList deletedList) throws RoomShareException {
        int[] idx = index.clone();
        if (idx.length == 1) {
            boolean isNegativeIndex = idx[0] < 0;
            boolean isExceededIndex = idx[0] >= tasks.size();
            if (isNegativeIndex || isExceededIndex) {
                throw new RoomShareException(ExceptionType.outOfBounds);
            }
            deletedList.add(tasks.get(idx[0]));
            tasks.remove(idx[0]);
        } else {
            boolean isNegativeFirstIndex = idx[0] < 0;
            boolean isExceededFirstIndex = idx[0] >= tasks.size();
            boolean isNegativeSecondIndex = idx[1] < 0;
            boolean isExceededSecondIndex = idx[1] >= tasks.size();
            if (isNegativeFirstIndex || isExceededFirstIndex
                    || isNegativeSecondIndex || isExceededSecondIndex) {
                throw new RoomShareException(ExceptionType.outOfBounds);
            }
            for (int i = idx[0]; idx[1] >= idx[0]; idx[1]--) {
                deletedList.add(tasks.get(i));
                tasks.remove(i);
            }
        }
    }

    /**
     * Lists out all tasks in the current list in the order they were added into the list.
     * shows all information related to the tasks
     * hides completed tasks
     * @throws RoomShareException when the list is empty
     */
    public void list(OverdueList overdueList) throws RoomShareException {
        sortTasks();
        if (tasks.size() != 0) {
            int listCount = 1;
            checkForOverdueTasks(overdueList);
            checkForFinishedLeave();
            for (Task output : tasks) {
                if (!output.getDone() && !output.getOverdue()) {
                    String priorityLvl = indicatePriorityLevel(output);
                    if (priorityLvl.trim().equals("*"))
                        System.out.print(Color.BRIGHTYELLOW);
                    else if(priorityLvl.trim().equals("**"))
                        System.out.print(Color.ORANGE);
                    else
                        System.out.print(Color.RED);
                    System.out.println("\t" + listCount + ". " + output.toString() + priorityLvl);
                    showSubtasks(output);
                    System.out.print(Color.RESET);
                }
                listCount += 1;
            }
        } else {
            throw new RoomShareException(ExceptionType.emptyList);
        }
    }

    /**
     * Lists out completed tasks in the list.
     * @throws RoomShareException when there are no completed tasks
     */
    public void showCompleted() throws RoomShareException {
        sortTasks();
        System.out.println(Color.GREEN + COMPLETED_TASKS);
        if (tasks.size() != 0) {
            int listCount = 1;
            for (Task output : tasks) {
                if (output.getDone()) {
                    System.out.println("\t" + listCount + ". " + output.toString());
                    showSubtasks(output);
                }
                listCount += 1;
            }
            System.out.print(Color.RESET);
        } else {
            throw new RoomShareException(ExceptionType.emptyList);
        }
    }

    /**
     * Sets a task in the list as 'done' to mark that the user has completed the task.
     * Will not perform any operations if the index does not exist in the list.
     * @param index Index of the task to be marked as done.
     * @throws RoomShareException If the index cannot be found in the list of tasks.
     */
    public void done(int[] index) throws RoomShareException {
        if (index.length == 1) {
            boolean isNegativeIndex = index[0] < 0;
            boolean isExceededIndex = index[0] >= tasks.size();
            if (isNegativeIndex || isExceededIndex) {
                throw new RoomShareException(ExceptionType.outOfBounds);
            }
            tasks.get(index[0]).setDone(true);
        } else {
            boolean isNegativeFirstIndex = index[0] < 0;
            boolean isExceededFirstIndex = index[0] >= tasks.size();
            boolean isNegativeSecondIndex = index[1] < 0;
            boolean isExceededSecondIndex = index[1] >= tasks.size();
            if (isNegativeFirstIndex || isExceededFirstIndex
                    || isNegativeSecondIndex || isExceededSecondIndex) {
                throw new RoomShareException(ExceptionType.outOfBounds);
            }
            for (int i = index[0]; i <= index[1]; i++) {
                tasks.get(i).setDone(true);
            }
        }
    }

    /**
     * Set a subtask from an assignment as done.
     * @param input the String containing the index of the Assignment and subtask
     * @throws RoomShareException if there are formatting error or the task entered is not an Assignment
     */
    public void doneSubTask(String input) throws RoomShareException {
        int index;
        int subTaskIndex;
        try {
            String[] arr = input.split(" ");
            index = Integer.parseInt(arr[1]) - 1;
            subTaskIndex = Integer.parseInt(arr[2]) - 1;
            if (TaskList.get(index) instanceof Assignment) {
                ((Assignment) TaskList.get(index)).doneSubtask(subTaskIndex);
            } else {
                throw new RoomShareException(ExceptionType.subTaskError);
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException e1) {
            throw new RoomShareException(ExceptionType.wrongIndexFormat);
        }
    }

    /**
     * Searches for tasks that has the specified keyword and prints them to the console.
     * Will prompt that the search has no results if keyword does not exist in the list.
     * @param key Keyword of the search.
     */
    public void find(String key) {
        int queryCount = 1;
        for (Task query : tasks) {
            if (query.toString().toLowerCase().contains(key.trim())) {
                String priorityLevel = indicatePriorityLevel(query);
                System.out.println("\t" + queryCount + ". " + query.toString() + priorityLevel);
                showSubtasks(query);
                queryCount += 1;
            }
        }
        if (queryCount == 1) {
            System.out.println(YOUR_SEARCH_RETURNED_NO_RESULTS_TRY_SEARCHING_WITH_ANOTHER_KEYWORD);
        }
    }

    /**
     * Returns the entire ArrayList of tasks.
     * @return tasks The ArrayList of Task objects that is being operated on.
     */
    public static ArrayList<Task> getCurrentList() {
        return tasks;
    }

    /**
     * replaces the task at the specified index with a new task.
     * @param index index of the task to be replaced
     * @param replacement the replacement task
     */
    public void replace(int index, Task replacement) {
        tasks.set(index, replacement);
    }

    /**
     * Sets priority of task at an index to a new priority.
     * @param info the information of the task index and the priority it should be set to
     * @throws RoomShareException when the priority specified is wrong or index is out of bounds
     */
    public void setPriority(String[] info) throws RoomShareException {
        try {
            int index = Integer.parseInt(info[0]) - 1;
            Priority priority = Priority.valueOf(info[1]);
            tasks.get(index).setPriority(priority);
        } catch (IllegalArgumentException a) {
            throw new RoomShareException(ExceptionType.wrongPriority);
        } catch (IndexOutOfBoundsException i) {
            throw new RoomShareException(ExceptionType.outOfBounds);
        }

    }

    /**
     * Returns priority of the task in the form of an integer.
     * high = 0, medium = 1, low = 2
     * @param t task in which we are checking the value of
     * @return integer value of the task's priority
     */
    private static int getValue(Task t) {
        if (t.getPriority().equals(Priority.high)) {
            return 0;
        } else if (t.getPriority().equals(Priority.medium)) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Changes taskList sort mode.
     * @param sortType new sort mode
     */
    public static void changeSort(SortType sortType) {
        TaskList.sortType = sortType;
        sortTasks();
    }

    /**
     * Sorts the list based on current sort mode.
     * @throws IllegalArgumentException when the sort type is not of priority, alphabetical or by deadline
     */
    public static void sortTasks() {
        switch (sortType) {
        case priority:
            comparePriority();
            break;
        case alphabetical:
            compareAlphabetical();
            break;
        case deadline:
            compareDeadline();
            break;
        case type:
            compareType();
            break;
        default:
            throw new IllegalStateException("Unexpected value: " + sortType);
        }
    }

    /**
     * Compare tasks based on priority.
     */
    private static void comparePriority() {
        tasks.sort((task1, task2) -> {
            if (task1.getDone() && !task2.getDone()) {
                return 1;
            } else if (task2.getDone() && !task1.getDone()) {
                return -1;
            } else {
                return getValue(task1) - getValue(task2);
            }
        });
    }

    /**
     * Compare tasks based on Alphabetical order.
     */
    private static void compareAlphabetical() {
        tasks.sort((task1, task2) -> {
            if (task1.getDone() && !task2.getDone()) {
                return 1;
            } else if (task2.getDone() && !task1.getDone()) {
                return -1;
            } else {
                String name1 = task1.getDescription();
                String name2 = task2.getDescription();
                return name1.compareTo(name2);
            }
        });
    }

    /**
     * Compare tasks based on Deadline.
     */
    private static void compareDeadline() {
        tasks.sort((task1, task2) -> {
            if (task1.getDone() && !task2.getDone()) {
                return 1;
            } else if (task2.getDone() && !task1.getDone()) {
                return -1;
            } else {
                Date date1 = task1.getDate();
                Date date2 = task2.getDate();
                return (int) (date1.getTime() - date2.getTime());
            }
        });
    }

    /**
     * Compare tasks based on Type.
     */
    private static void compareType() {
        tasks.sort((task1, task2) -> {
            if (task1 instanceof Meeting && !(task2 instanceof Meeting)) {
                return -1;
            } else if (task1 instanceof Assignment) {
                if (task2 instanceof Meeting) {
                    return 1;
                } else if (task2 instanceof Leave) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                if (task2 instanceof Meeting || task2 instanceof Assignment) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    /**
     * Reorder the positions of two tasks inside the task list.
     * @param first the first task
     * @param second the second task
     */
    public void reorder(int first, int second) throws RoomShareException {
        try {
            Collections.swap(tasks, first, second);
        } catch (IndexOutOfBoundsException e) {
            throw new RoomShareException(ExceptionType.outOfBounds);
        }
    }

    /**
     * Snooze a specific task indicated by user.
     * @param index the index of the task to be snoozed
     * @param amount the amount of time to snooze
     * @param timeUnit unit for snooze time: month, day, hour, minute
     * @throws IndexOutOfBoundsException when the specified index is not within the task list indices
     */
    public void snooze(int index, int amount, TimeUnit timeUnit) throws RoomShareException {
        try {
            switch (timeUnit) {
            case month:
                tasks.get(index).snoozeMonth(amount);
                break;
            case day:
                tasks.get(index).snoozeDay(amount);
                break;
            case hours:
                tasks.get(index).snoozeHour(amount);
                break;
            case minutes:
                tasks.get(index).snoozeMinute(amount);
                break;
            default:
                tasks.get(index).snoozeMinute(0);
                break;
            }
        } catch (IndexOutOfBoundsException e) {
            throw new RoomShareException(ExceptionType.outOfBounds);
        }
    }

    /**
     * Get the number of tasks inside the task list.
     * @return the number of tasks inside the task list
     */
    int getSize() {
        int count = 0;
        for (Task t : tasks) {
            if (!t.getOverdue() && !(t instanceof Leave)) {
                count += 1;
            }
        }
        return count;
    }

    /**
     * Get the number of completed tasks inside the task list.
     * @return the number of completed tasks inside the task list
     */
    int getDoneSize() {
        int count = 0;
        for (Task t: tasks) {
            if (t.getDone() && !t.getOverdue() && !(t instanceof Leave)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Retrieve a task from the list.
     * @param index the index of the task
     * @return the task at the specified index of the task list
     * @throws RoomShareException when the index specified is out of bounds
     */
    public static Task get(int index) throws RoomShareException {
        try {
            return tasks.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new RoomShareException(ExceptionType.outOfBounds);
        }
    }

    /**
     * Returns current sort type of list.
     * @return current sort type of list
     */
    static SortType getSortType() {
        return sortType;
    }

    /**
     * lists out all the tasks associated with a certain assignee.
     * will include tasks that are tagged "everyone", since everyone includes the assignee
     * @param user assignee to the tasks
     * @throws RoomShareException when the list is empty
     */
    public int[] listTagged(String user) throws RoomShareException {
        int listCount = 1;
        int belongCount = 0;
        int doneCount  = 0;
        for (Task output : tasks) {
            boolean isSameUser = output.getAssignee().equals(user);
            boolean isEveryone = output.getAssignee().equals("everyone");
            if (isSameUser || isEveryone) {
                belongCount += 1;
                if (output.getDone()) {
                    doneCount += 1;
                }
                if (!output.getDone() && !output.getOverdue()) {
                    String priorityLvl = indicatePriorityLevel(output);
                    System.out.println("\t" + listCount + ". " + output.toString() + priorityLvl);
                    showSubtasks(output);
                }
                listCount += 1;
            }
        }
        if (belongCount == 0) {
            throw new RoomShareException(ExceptionType.emptyList);
        }
        return new int[]{belongCount, doneCount};
    }

    /**
     * sets the tasks which are done to an undone state.
     * @param index index of task
     * @param date date the new deadline of the task
     * @throws RoomShareException when the task selected is a Leave
     */
    public void reopen(int index, Date date) throws RoomShareException {
        TaskList.get(index).setDate(date);
        CheckAnomaly.isDuplicate(TaskList.get(index));
        if (tasks.get(index) instanceof Meeting) {
            CheckAnomaly.isTimeClash(TaskList.get(index));
        }
        TaskList.get(index).setDone(false);
    }

    /**
     * checks for overdue tasks in the task list.
     * removes from the current list if overdue, and adds it into the overdue list
     * @param overdueList overdue list to be added into
     */
    private void checkForOverdueTasks(OverdueList overdueList) {
        for (int i = 0; i < tasks.size(); i++) {
            boolean isPassedCurrentDate = new Date().after(tasks.get(i).getDate());
            boolean isNotALeave = !(tasks.get(i) instanceof Leave);
            if (isPassedCurrentDate && isNotALeave) {
                tasks.get(i).setOverdue(true);
                boolean hasNoDuplicateOverdue = !CheckAnomaly.isDuplicateOverdue(tasks.get(i));
                if (hasNoDuplicateOverdue) {
                    // no duplicates in overdue list
                    overdueList.add(tasks.get(i));
                }
                tasks.remove(tasks.get(i));
            }
        }
    }

    /**
     * checks for finished leave in the task list.
     * removes the finished leave if detected
     */
    private void checkForFinishedLeave() {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i) instanceof Leave && ((Leave) tasks.get(i)).getEndDate().before(new Date())) {
                tasks.remove(tasks.get(i));
            }
        }
    }

    /**
     * Shows the priority level of the task as String.
     * number of stars indicates the priority level
     * @param task task to be checked for priority level
     * @return String containing the number of stars as the priority level
     */
    private String indicatePriorityLevel(Task task) {
        Priority priority = task.getPriority();
        String priorityLvl;
        if (priority.equals(Priority.low)) {
            priorityLvl = " *";
        } else if (priority.equals(Priority.medium)) {
            priorityLvl = " **";
        } else {
            priorityLvl = " ***";
        }
        return priorityLvl;
    }

    /**
     * lists out the subtasks if the task is an Assignment.
     * @param task task to be checked for subtasks.
     */
    private void showSubtasks(Task task) {
        if (task instanceof Assignment && (((Assignment) task).getSubTasks() != null)) {
            ArrayList<String> subTasks = ((Assignment) task).getSubTasks();
            for (String subtask : subTasks) {
                System.out.println("\t" + "\t" + "- " + subtask);
            }
        }
    }
}
