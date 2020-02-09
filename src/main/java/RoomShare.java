import CustomExceptions.DuplicateException;
import CustomExceptions.RoomShareException;
import CustomExceptions.TimeClashException;
import Enums.ExceptionType;
import Enums.SortType;
import Enums.TaskType;
import Enums.TimeUnit;
import Model_Classes.ProgressBar;
import Model_Classes.Task;
import Operations.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Main class of the RoomShare program.
 */
public class RoomShare {
    private Ui ui;
    private Storage storage;
    private TaskList taskList;
    private OverdueList overdueList;
    private Parser parser;
    private TempDeleteList tempDeleteList;
    private TaskCreator taskCreator;
    private Help help;
    private ListRoutine listRoutine;

    /**
     * Constructor of a RoomShare class. Creates all necessary objects and collections for RoomShare to run
     * Also loads the ArrayList of tasks from the data.txt file
     */
    private RoomShare() throws RoomShareException {
        ui = new Ui();
        help = new Help();
        ui.startUp();
        storage = new Storage();
        parser = new Parser();
        taskCreator = new TaskCreator();
        ArrayList<Task> tempStorage = new ArrayList<>();
        tempDeleteList = new TempDeleteList(tempStorage);

        try {
            taskList = new TaskList(storage.loadJSONFile("data.json"));
        } catch (RoomShareException e) {
            ui.showError(e);
            ArrayList<Task> emptyList = new ArrayList<>();
            taskList = new TaskList(emptyList);
        }
        try {
            overdueList = new OverdueList(storage.loadJSONFile("overdue.json"));
        } catch (RoomShareException e) {
            ui.showError(e);
            ArrayList<Task> emptyList = new ArrayList<>();
            overdueList = new OverdueList(emptyList);
        }
        listRoutine = new ListRoutine(taskList, overdueList);
        RecurHandler recurHandler = new RecurHandler(taskList);
        if (recurHandler.checkRecurrence()) {
            ui.showChangeInTaskList();
            taskList.list(overdueList);
        }
        listRoutine.list();
    }

    /**
     * Deals with the operation flow of RoomShare.
     */
    private void run() throws RoomShareException, IOException, InterruptedException {
        boolean isExit = false;
        while (!isExit) {
            TaskType type;
            try {
                String command = parser.getCommand();
                type = TaskType.valueOf(command);
            } catch (IllegalArgumentException e) {
                type = TaskType.others;
            }
            switch (type) {
            case help:
                Ui.clearScreen();
                ui.startUp();
                help.showHelp(parser.getCommandLine());
                break;

            case bye:
                isExit = true;
                try {
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                } catch (RoomShareException e) {
                    ui.showError(e);
                }
                try {
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                } catch (RoomShareException e) {
                    ui.showError(e);
                }
                parser.close();
                ui.showBye();
                break;

            case list:
                Ui.clearScreen();
                ui.startUp();
                listRoutine.list();
                break;

            case done:
                Ui.clearScreen();
                ui.startUp();
                try {
                    String input = parser.getCommandLine().trim();
                    if(input.split(" ")[0].equals("subtask")) {
                        taskList.doneSubTask(input);
                    } else {
                        int[] index = parser.getIndexRange(input);
                        taskList.done(index);
                        ui.showDone();
                    }
                } catch (RoomShareException e) {
                    ui.showError(e);
                } finally {
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                listRoutine.list();
                break;

            case delete:
                Ui.clearScreen();
                ui.startUp();
                try {
                    String input = parser.getCommandLine().trim();
                    int[] index = parser.getIndexRange(input);
                    taskList.delete(index, tempDeleteList);
                    ui.showDeleted(index);
                } catch (RoomShareException e) {
                    ui.showError(e);
                } finally {
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                listRoutine.list();
                break;

            case removeoverdue:
                Ui.clearScreen();
                ui.startUp();
                try {
                    String input = parser.getCommandLine().trim();
                    int[] index = parser.getIndexRange(input);
                    overdueList.remove(index, tempDeleteList);
                    ui.showDeleted(index);
                } catch (RoomShareException e) {
                    ui.showError(e);
                } finally {
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                listRoutine.list();
                break;

            case restore:
                Ui.clearScreen();
                ui.startUp();
                ui.showRestoreList();
                try {
                    String input = parser.getCommandLine().trim();
                    tempDeleteList.list();
                    int restoreIndex = parser.getIndex(input);
                    tempDeleteList.restore(restoreIndex, taskList);
                } catch (RoomShareException e) {
                    ui.showError(e);
                } finally {
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                listRoutine.list();
                break;

            case find:
                Ui.clearScreen();
                ui.startUp();
                listRoutine.list();
                ui.showFind();
                taskList.find(parser.getKey().toLowerCase());
                break;

            case priority:
                Ui.clearScreen();
                ui.startUp();
                boolean success = true;
                try {
                    taskList.setPriority(parser.getPriority());
                } catch (RoomShareException e) {
                    success = false;
                    ui.showError(e);
                    ui.priorityInstruction();
                } finally {
                    if (success) {
                        TaskList.sortTasks();
                        ui.prioritySet();
                    }
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                listRoutine.list();
                break;

            case add:
                Ui.clearScreen();
                ui.startUp();
                try {
                    String input = parser.getCommandLine().trim();
                    taskList.add(taskCreator.create(input));
                    ui.showAdd();
                } catch (RoomShareException | DuplicateException | TimeClashException e) {
                    ui.showError(e);
                } finally {
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                listRoutine.list();
                break;

            case snooze:
                Ui.clearScreen();
                ui.startUp();
                try {
                    String input = parser.getCommandLine().trim();
                    int index = parser.getIndex(input);
                    int amount = parser.getAmount(input);
                    TimeUnit timeUnit = parser.getTimeUnit(input);
                    if (amount < 0) {
                        throw new RoomShareException(ExceptionType.negativeTimeAmount);
                    }
                    taskList.snooze(index, amount, timeUnit);
                    ui.showSnoozeComplete(index + 1, amount, timeUnit);
                } catch (RoomShareException e) {
                    ui.showError(e);
                } finally {
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                listRoutine.list();
                break;

            case reorder:
                Ui.clearScreen();
                ui.startUp();
                try {
                    String input = parser.getCommandLine().trim();
                    int firstIndex = parser.getIndex(input, 0);
                    int secondIndex = parser.getIndex(input, 1);
                    taskList.reorder(firstIndex, secondIndex);
                    ui.showReordering();
                } catch (RoomShareException e) {
                    ui.showError(e);;
                } finally {
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                listRoutine.list();
                break;

            case subtask:
                Ui.clearScreen();
                ui.startUp();
                try {
                    String input = parser.getCommandLine().trim();
                    int index = parser.getIndexSubtask(input);
                    String subTasks = parser.getSubTasks(input);
                    new subTaskCreator(index, subTasks);
                } catch (RoomShareException e) {
                    ui.showError(e);
                } finally {
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                listRoutine.list();
                break;

            case update:
                Ui.clearScreen();
                ui.startUp();
                try {
                    String input = parser.getCommandLine().trim();
                    int index = parser.getIndex(input);
                    Task oldTask = TaskList.get(index);
                    taskCreator.updateTask(input,oldTask);
                    ui.showUpdated(index+1);
                } catch (RoomShareException e) {
                    ui.showError(e);
                } finally {
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                listRoutine.list();
                break;
                
            case sort:
                Ui.clearScreen();
                ui.startUp();
                SortType sortType;
                try {
                    String input = parser.getCommandLine().trim();
                    sortType = parser.getSort(input);
                } catch (RoomShareException e) {
                    ui.showError(e);
                    sortType = SortType.priority;
                } finally {
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                TaskList.changeSort(sortType);
                ui.showChangeInPriority(sortType);
                listRoutine.list();
                break;

            case log:
                Ui.clearScreen();
                ui.startUp();
                listRoutine.list();
                try {
                    String filePath = storage.writeLogFile(TaskList.getCurrentList());
                    ui.showLogSuccess(filePath);
                } catch (RoomShareException e) {
                    ui.showError(e);
                } finally {
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                break;

            case overdue:
                Ui.clearScreen();
                ui.startUp();
                listRoutine.list();
                ui.showOverdueList();
                try {
                    overdueList.list();
                } catch (RoomShareException e) {
                    ui.showError(e);
                }
                try {
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                } catch (RoomShareException e) {
                    ui.showError(e);
                }
                break;

            case reschedule:
                Ui.clearScreen();
                ui.startUp();
                try {
                    overdueList.list();
                    String input = parser.getCommandLine();
                    String[] range = input.split(" ");
                   int[] indexes = parser.getIndexRange(range[0]);
                      if (indexes.length != 1) {
                          for (int i = indexes[0]; i <= indexes[1]; i++) {
                              Task oldTask = overdueList.get(i);
                              taskCreator.rescheduleTask(input, oldTask);
                              ui.showUpdated(i + 1);
                          }
                    } else {
                          Task oldTask = overdueList.get(indexes[0]);
                          taskCreator.rescheduleTask(input, oldTask);
                          ui.showUpdated(indexes[0] + 1);
                      }
                    overdueList.reschedule(indexes, taskList);
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                } catch (RoomShareException e) {
                    ui.showError(e);
                } finally {
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                listRoutine.list();
                break;

            case show:
                Ui.clearScreen();
                ui.startUp();
                try {
                    String input = parser.getCommandLine().trim();
                    if (input.equals("deleted")) {
                        ui.showDeletedList();
                        try {
                            tempDeleteList.list();
                        } catch (RoomShareException e) {
                            ui.showError(e);
                        }
                    } else {
                        ui.showTagged(input);
                        int[] doneArray = taskList.listTagged(input);
                        ui.showTaggedPercentage(input);
                        ProgressBar progressBar = new ProgressBar(doneArray[0], doneArray[1]);
                        ui.showBar(progressBar.showBar());
                    }
                } catch (RoomShareException e) {
                    ui.showError(e);
                } finally {
                    storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                }
                break;

            case reopen:
                Ui.clearScreen();
                ui.startUp();
                try {
                    String input = parser.getCommandLine();
                    int index = parser.getIndex(input);
                    ArrayList<Date> date = taskCreator.extractDate(input);
                    taskList.reopen(index,date.get(0));
                } catch (RoomShareException e) {
                    ui.showError(e);
                }
                storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                listRoutine.list();
                ui.showDoneList();
                taskList.showCompleted();
                break;

            default:
                Ui.clearScreen();
                ui.startUp();
                ui.showError(new RoomShareException(ExceptionType.invalidCommand));
                listRoutine.list();
                storage.writeJSONFile(TaskList.getCurrentList(), "data.json");
                try {
                    storage.writeJSONFile(OverdueList.getOverdueList(), "overdue.json");
                } catch (RoomShareException e) {
                    ui.showError(e);
                }
                break;
            }
        }
    }

    /**
     * Main function of RoomShare.
     * Creates a new instance of RoomShare class
     * @param args command line arguments
     * @throws RoomShareException Custom exception class within RoomShare program
     */
    public static void main(String[] args) throws RoomShareException, IOException, InterruptedException {
        new RoomShare().run();
        System.exit(0);
    }
}
