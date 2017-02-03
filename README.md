# cpu-scheduler

A console mode application that simulates single-processor scheduling algorithms, including:

* First-Come First-Served
* Preemptive Shortest Job First
* Round-Robin

## Execution

From the command line:

```
$ javac Schedule.java
$ java Schedule
```

## Input

Input file must be named **processes.in**.

```
processcount 2 # Read 5 processes
runfor 15	   # Run for 15 time units
use rr         # Can be fcfs, sjf, or rr
quantum 2
process name P1 arrival 3 burst 5
process name P2 arrival 0 burst 9
end
```

## Output

Generated output file is named **processes.out**.

```
2 processes
Using Round Robin
Quantum 2

Time 0: P2 arrived
Time 0: P2 selected (burst 9)
Time 2: P2 selected (burst 7)
Time 3: P1 arrived
Time 4: P1 selected (burst 5)
Time 6: P2 selected (burst 5)
Time 8: P1 selected (burst 3)
Time 10: P2 selected (burst 3)
Time 12: P1 selected (burst 1)
Time 13: P1 finished
Time 13: P2 selected (burst 1)
Time 14: P2 finished
Time 14: Idle

Finished at time 15
P1 wait 5 turnaround 10
P2 wait 5 turnaround 14
```
