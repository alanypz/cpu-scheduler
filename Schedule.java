import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*
 *  To run:
 *  [1] Open command line
 *  [2] Set cwd to directory containing this file
 *  [3] Enter "javac Schedule.java"
 *  [4] Verify three class files were generated
 *  [5] Place "processes.in" input file in the cwd
 *  [6] Enter "java Schedule"
 *  [7] Open the generated "processes.out" to view output
 *
 */

public class Schedule {

    //  Static I/O Filenames
    static String inputFilename = "processes.in";
    static String outputFilename = "processes.out";
    
    public static void main(String[] args)
    {
        Scheduler scheduler = null;
        
        //  Read and parse input
        try
        {
            List<String> input = readInput();
            scheduler = parseInput(input);
            
        }
        catch (IOException e)
        {
            System.out.println("Error: " + inputFilename + " could not be read.");
//            System.out.println(e);
        }
        catch (NumberFormatException | IndexOutOfBoundsException e)
        {
            System.out.println("Error: Formatting error in " + inputFilename );
//            System.out.println(e);
        }
        
        //  Switch cases for process types
        switch (scheduler.use)
        {
            case "fcfs":
                fcfs(scheduler);
                break;
            case "rr":
                rr(scheduler);
                break;
            case "sjf":
                sjf(scheduler);
                break;
            default:
                break;
        }                
    }
    
    //  Opens and reads input file, returns as list of Strings
    /**
     * 
     * @return
     *      List<String> object, where each String is a line from input file
     * @throws IOException 
     *      Exception if unable to read input file
     */
    public static List<String> readInput() throws IOException
    {
        List<String> input = new ArrayList<>();
        
        FileReader fileReader = new FileReader(new File(inputFilename));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        while ((line = bufferedReader.readLine()) != null)
        {
            line = line.split("#")[0].trim();
            if (!line.isEmpty())
                input.add(line);
        }
        fileReader.close();
        
        return input;
    }
    
    //  Parses lines from input file and creates Scheduler and Process instances
    /**
     * 
     * @param lines
     * @return
     *      Scheduler object with properties read from input file
     * @throws NumberFormatException
     *      Exception occurs when an unexpected value type is read (string instead of number)
     * @throws IndexOutOfBoundsException 
     *      Exception occurs if input line is incomplete (process name line is missing tokens)
     */
    public static Scheduler parseInput(List<String> lines) throws NumberFormatException, IndexOutOfBoundsException
    {
        Scheduler scheduler = new Scheduler();
        
        //  Iterate through each line of input
        //  Use keywords at beginning of each line to identify the type of input
        for (String line: lines)
        {
            String processcount_ = "processcount";
            if (line.startsWith(processcount_))
            {
                line = line.split(processcount_)[1].trim();
                scheduler.processcount = Integer.valueOf(line);
                continue;
            }
            
            String runfor_ = "runfor";
            if (line.startsWith(runfor_))
            {
                line = line.split(runfor_)[1].trim();
                scheduler.runfor = Integer.valueOf(line);
                continue;
            }
            
            String use_ = "use";
            if (line.startsWith(use_))
            {
                line = line.split(use_)[1].trim();
                scheduler.use = line;
                scheduler.usename = scheduler.useref.get(line);
                continue;
            }
            
            String quantum_ = "quantum";
            if (line.startsWith(quantum_))
            {
                line = line.split(quantum_)[1].trim();
                scheduler.quantum = Integer.valueOf(line);
                continue;
            }
            
            Process process = null;
            
            String processname_ = "process name";
            String arrival_ = "arrival";
            String burst_ = "burst";
            
            //  Note: Will only detect "process name" if separator is a space
            if (line.startsWith(processname_))
            {
                line = line.split(processname_)[1].trim();
                String[] part = line.split(" ", 2);
                String processName = part[0];
                line = part[1].trim();
                
                if (line.startsWith(arrival_))
                {
                    line = line.split(arrival_)[1].trim();
                    part = line.split(" ", 2);
                    Integer processArrival = Integer.valueOf(part[0]);
                    line = part[1].trim();
                    
                    if (line.startsWith(burst_))
                    {
                        line = line.split(burst_)[1].trim();
                        part = line.split(" ", 2);
                        Integer processBurst = Integer.valueOf(part[0]);
                        //  Create new Process only if all parameters were found in line
                        process = new Process(processName, processArrival, processBurst);
                    }
                }
                
                if (process != null)
                {
                    scheduler.addProcess(process);
                }
                else
                {
                    System.out.println("Error: Unable to parse process parameters from input file.");
                }
                
                continue;
            }
            
            String end_ = "end";
            if (line.startsWith(end_))
            {
                break;
            }
            
            //  Print error if input line count not be recognized
            System.out.println("Error: Could not parse line from input file.");
            System.out.println("\t" +line);
        }
        
        String rr_ = "rr";
        String fcfs_ = "fcfs";
        String sjf_ = "sjf";
        String errorMessage = "";
        
        //  Print errors depending on inconsistencies from input.
        if (!Arrays.asList(rr_, fcfs_, sjf_).contains(scheduler.use))
        {
            errorMessage = "Error: Invalid \"use\" parameter.";
        }
        else if (scheduler.use.equals(rr_) && scheduler.quantum == null)
        {
            errorMessage = "Error: Missing quantum parameter.";
        }
//        else if (!scheduler.use.equals(rr_) && scheduler.quantum != null)
//        {
//            errorMessage = "Error: Unecessary quantum parameter.";
//        }
        else if (scheduler.processcount != scheduler.processes.size())
        {
            errorMessage = "Error: Number of processes does not equal \"processcount\" parameter.";
        }
        
        if (!errorMessage.equals(""))
        {
            System.out.println(errorMessage);
            scheduler = new Scheduler();
        }
        
        return scheduler;
    }
    
    /**
     * Formats output string and writes to output file
     * @param scheduler
     *      Scheduler object with properties updated during execution
     * @param processString 
     *      String built during scheduler simulation
     */
    public static void writeOutput(Scheduler scheduler, String processString)
    {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream(outputFilename), "utf-8")))
        {
            String ops;
            
            StringBuilder headerString = new StringBuilder();
            ops = Integer.toString(scheduler.processcount) + " processes" + "\n";
            ops += "Using " + scheduler.usename + "\n";
            if (scheduler.quantum != null)
            {
                ops += "Quantum " + Integer.toString(scheduler.quantum) + "\n";
            }
            ops += "\n";
            headerString.append(ops);
            
            StringBuilder footerString = new StringBuilder();
            ops = "Finished at time " + Integer.toString(scheduler.end) + "\n\n";
            
            for (Process p : scheduler.processes)
            {
                ops += p.name + " wait " + Integer.toString(p.wait);
                if (p.turnaround > 0)
                {
                    ops += " turnaround " + Integer.toString(p.turnaround);
                }
                ops += "\n";
            }
            footerString.append(ops);
            
            writer.write(headerString + processString + footerString);
        }
        catch (IOException e)
        {
            System.out.println("Error: Unable to create " + outputFilename);
            System.out.println(e);
        }
    }
    
    //  
    /**
     * Executes First-Come First-Served
     * @param scheduler 
     *      Scheduler object with attributes parsed from input file
     */
    public static void fcfs(Scheduler scheduler)
    {
        //  Initialize variables for simulation
        Integer time = 0;
        List<Process> queue = new ArrayList<>();
        Boolean running = false;
        List<Process> processes = scheduler.processes;
        
        StringBuilder out = new StringBuilder();
        String ops;
        
        //  Perform simulation until runfor time is passed
        while (time < scheduler.runfor)
        {
            //  Check for new arrivals at current time
            for (Process process: processes)
            {
                if (process.arrival.equals(time))
                {
                    process.start = time;
                    queue.add(0, process);
                    ops = "Time " + Integer.toString(time) + ": " + process.name + " arrived" + "\n";
                    out.append(ops);
                }
            }
            if (!running)
            {
                //  Select process if nothing is running
                if (queue.size() > 0)
                {
                    Process process = queue.get(queue.size()-1);
                    running = true;
                    ops = "Time " + Integer.toString(time) + ": " 
                        + process.name + " selected (burst " 
                        +  Integer.toString(process.burst) + ")" + "\n";
                    out.append(ops);
                }
                //  Idle if no process is available
                else
                {
                    ops = "Time " + Integer.toString(time) + ": Idle" + "\n";
                    out.append(ops);
                }
            }
            time++;
            //  Update/check properties after time period is "complete"
            if (running)
            {
                Process process = queue.get(queue.size()-1);
                process.burst--;
                if (process.burst <= 0)
                {
                    process.turnaround = time - process.start;
                    queue.remove(queue.size()-1);
                    running = false;
                    ops = "Time " + Integer.toString(time) + ": " 
                        + process.name + " finished" + "\n";
                    out.append(ops);
                }
                //  Update wait time for other processes
                for (Process p : queue)
                {
                    if (!p.equals(process))
                    {
                        p.wait++;
                    }
                }
            }
            
        }       
        
        scheduler.end = time;
        writeOutput(scheduler, out.toString());
    }

    /**
     * Executes Round Robin
     * @param scheduler 
     *      Scheduler object with attributes parsed from input file
     */
    public static void rr(Scheduler scheduler)
    {
        //  Initialize variables for simulation
        Integer time = 0;
        List<Process> queue = new ArrayList<>();
        Boolean running = false;
        List<Process> processes = scheduler.processes;
        Integer quantum = scheduler.quantum;
        Integer q = scheduler.quantum;
        
        StringBuilder out = new StringBuilder();
        String ops;
        
        //  Perform simulation until runfor time is passed
        while (time < scheduler.runfor)
        {
            //  Check for new arrivals at current time
            for (Process process: processes)
            {
                if (process.arrival.equals(time))
                {
                    process.start = time;
                    queue.add(process);
                    ops = "Time " + Integer.toString(time) + ": " + process.name + " arrived" + "\n";
                    out.append(ops);
                }
            }
            if (!running)
            {
                //  Select process if nothing is running
                if (queue.size() > 0)
                {
                    Process process = queue.get(0);
                    running = true;
                    q = quantum;
                    ops = "Time " + Integer.toString(time) + ": " 
                        + process.name + " selected (burst " 
                        +  Integer.toString(process.burst) + ")" + "\n";
                    out.append(ops);
                }
                //  Idle if no process is available
                else
                {
                    ops = "Time " + Integer.toString(time) + ": Idle" + "\n";
                    out.append(ops);
                }
            }
            time++;
            q--;
            //  Update/check properties after time period is "complete"
            if (running)
            {
                Process process = queue.get(0);
                process.burst--;
                if (process.burst <= 0)
                {
                    process.turnaround = time - process.start;
                    queue.remove(0);
                    running = false;
                    ops = "Time " + Integer.toString(time) + ": " 
                        + process.name + " finished" + "\n";
                    out.append(ops);
                }
                else if (q <= 0)
                {
                    Process requeue = queue.remove(0);
                    queue.add(requeue);
                    running = false;
                }
                 //  Update wait time for other processes
                for (Process p : queue)
                {
                    if (!p.equals(process))
                    {
                        p.wait++;
                    }
                }
            }
            
        }       
        
        scheduler.end = time;
        writeOutput(scheduler, out.toString());
    }
    
    /**
     * Executes Preemptive Shortest Job First
     * @param scheduler 
     *      Scheduler object with attributes parsed from input file
     */
    public static void sjf(Scheduler scheduler)
    {
        //  Initialize variables for simulation
        Integer time = 0;
        List<Process> queue = new ArrayList<>();
        List<Process> processes = scheduler.processes;
        Process running = null;
        Process sj = null;
        
        StringBuilder out = new StringBuilder();
        String ops;
        
        //  Perform simulation until runfor time is passed
        while (time < scheduler.runfor)
        {
            //  Check for new arrivals at current time
            for (Process process: processes)
            {
                if (process.arrival.equals(time))
                {
                    process.start = time;
                    queue.add(process);
                    Collections.sort(queue, (p1, p2) -> p1.burst.compareTo(p2.burst));
                    sj = queue.get(0);
                    ops = "Time " + Integer.toString(time) + ": " + process.name + " arrived" + "\n";
                    out.append(ops);
                }
            }
            if (running == null)
            {
                //  Select process if nothing is running
                if (queue.size() > 0)
                {
                    running = queue.get(0);
                    ops = "Time " + Integer.toString(time) + ": " 
                        + running.name + " selected (burst " 
                        +  Integer.toString(running.burst) + ")" + "\n";
                    out.append(ops);
                }
                //  Idle if no process is available
                else
                {
                    ops = "Time " + Integer.toString(time) + ": Idle" + "\n";
                    out.append(ops);
                }
            }
            //  Check whether process should be preempted
            else if (running != null && !running.equals(sj))
            {
                running = sj;
                ops = "Time " + Integer.toString(time) + ": " 
                        + running.name + " selected (burst " 
                        +  Integer.toString(running.burst) + ")" + "\n";
                    out.append(ops);
            }
            time++;
            //  Update/check properties after time period is "complete"
            if (running != null)
            {
                running.burst--;
                if (running.burst <= 0)
                {
                    running.turnaround = time - running.start;
                    queue.remove(0);
                    if (queue.size() > 0)
                    {
                        Collections.sort(queue, (p1, p2) -> p1.burst.compareTo(p2.burst));
                        sj = queue.get(0);
                    }
                    else
                    {
                        sj = null;
                    }
                    ops = "Time " + Integer.toString(time) + ": " 
                        + running.name + " finished" + "\n";
                    out.append(ops);
                    running = null;
                }
                 //  Update wait time for other processes
                for (Process p : queue)
                {
                    if (!p.equals(running))
                    {
                        p.wait++;
                    }
                }
            }
            
        }       
        
        scheduler.end = time;
        writeOutput(scheduler, out.toString());
    }
    
}

//  Process object with properties relating to individual processes
class Process {
    
    String name;
    Integer arrival;
    Integer burst;
    Integer wait;
    Integer start;
    Integer turnaround;
    
    public Process (String name, Integer arrival, Integer burst)
    {
        this.name = name;
        this.arrival = arrival;
        this.burst = burst;
        
        this.wait = 0;
        this.start = 0;
        this.turnaround = 0;
    }
}

//  Scheduler object with properties relating to entire simulation
class Scheduler {
    
    List<Process> processes = new ArrayList<>();
    Map<String, String> useref = new HashMap<String, String>();
    
    Integer processcount;
    Integer runfor;
    Integer quantum;
    Integer end;
    
    String use;
    String usename;
    
    public Scheduler()
    {
        this.quantum = null;
        this.usename = "";
        this.use = "";
        this.useref.put("fcfs", "First-Come First-Served");
        this.useref.put("rr", "Round Robin");
        this.useref.put("sjf", "Preemptive Shortest Job First");
    }
  
    public void addProcess(Process process)
    {
        this.processes.add(process);
    }
    
    public List<Process> getProcesses()
    {
        return this.processes;
    }
}