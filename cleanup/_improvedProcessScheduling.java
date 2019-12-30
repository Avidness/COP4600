// NOTES:

// Turn around = runtime + waittime





// Alan Ness
// COP 4600

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class ProcessScheduling{

	private static class Process{
		String name;
		int arrival;
		int burst;
		int runtime;
		
		int remaining_time;
		int wait;
		int turnaround;

		boolean arrived;
		boolean finished; 

		String state;
		int finish_time;
		int start_time;
	}

	private static class Scheduler{
		int processcount;
		int runfor;
		String type;
		int quantum;
		Process[] processes;
		LinkedList<Process> readyProcesses;
	}

	public static void main(String[] args){

		// Create the scheduler using settings from a file
		Scheduler s = ParseSchedulerFromFile("processes.in");

		// Set up the output file
		PrintWriter output = null;
		try{
			output = new PrintWriter("processes.out");

			// Start the appropriate scheduler
			if(s.type.equals("fcfs")){

				// First Come First Served
				FirstComeFirstServe(s, output);
			}else if(s.type.equals("sjf")){

				// Shortest Job First
				ShortestJobFirst(s, output);
			}else if(s.type.equals("rr")){

				// Round Robin
				RoundRobin(s, output);
			}
    	output.close();
    }catch(FileNotFoundException e){
      e.printStackTrace();
    }finally{
      if(output!=null){
        output.close();
  	  }
	  }
	}

	public static Scheduler ParseSchedulerFromFile(String fileName){
		// Read in file
		File file = new File(fileName);
		Scheduler s = new Scheduler();
		try{
			// Loop until the file is empty
			int processIndex = 0;
			Scanner sc = new Scanner(file);
			while (sc.hasNextLine()){

				// Loop through each word in the line
				String thisLine = sc.nextLine();
				String[] words = thisLine.split("\\s");
				for(int i=0; i<words.length; i++){

					// Look for keywords
					String word = words[i];
					if(word.equals("processcount")){
						s.processcount = Integer.parseInt(words[i+1]);
						s.processes = new Process[s.processcount];
						i++;
					}else if(word.equals("runfor")){
						s.runfor = Integer.parseInt(words[i+1]);
						i++;
					}else if(word.equals("use")){
						s.type = words[i+1];
						i++;
					}else if(word.equals("quantum")){
						s.quantum = Integer.parseInt(words[i+1]);
						i++;
					}else if(word.equals("process")){

						// Parse the process
						Process p = new Process();
						for(int j=0; j<words.length; j++){
							String pword = words[j];
							if(pword.equals("name")){
								p.name = words[j+1];
							}else if(pword.equals("arrival")){
								p.arrival = Integer.parseInt(words[j+1]);
							}else if(pword.equals("burst")){
								p.burst = Integer.parseInt(words[j+1]);
							}
						}
						p.runtime=0;
						s.processes[processIndex] = p;
						processIndex++;
						break;
					}else if(word.equals("end")){
						break;
					}else if(word.contains("#")){
						break;
					}
				}
			}

			// Close the scanner
			sc.close();
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}

		// Return the parsed object
		return s;
	}

	private static void RunScheduler(Scheduler s, Printwriter output){
		
		// Print basic info about this scheduler
		output.println(s.processcount + " processes");
		output.println("Using TODO");
		output.println();
		s.readyProcesses = new LinkedList<Process>();

		// Start the timer
		int cur_time = 0;
		int time_limit = s.runfor;
		while (cur_time <= time_limit){
			String timestamp = "Time " + cur_time + ": ";

			// Find new arrivals and put them in the queue
			for(Process p : s.processes){
				if(p.arrival == cur_time){
					output.println(timestamp + p.name + " arrived");
					p.arrived = true;
					p.finished = false;
					s.readyProcesses.add(p);
				}
			}

			// Find process to run based on scheduling algorithm
			Process p_selected = null;
			if(s.type == "fcfs"){
				// First come first served
				 p_selected = s.readyProcesses.getFirst();

			}else if(s.type == "sjf"){
				// Shortest Job first
				// Preemptive
				if(s.readyProcesses.size() > 0){
					p_selected = s.readyProcesses.getFirst();
					for(Process p : s.readyProcesses){
						if(p.remaining_time < p_selected.remaining_time){
							p_selected = p;
						}
					}
				}

			}else if(s.type == "rr"){
				// Round robin

			}

			// Run the process
			

			// Update the clock	
			cur_time++;
		}
	}

	private static void FirstComeFirstServe(Scheduler s, PrintWriter output){
		
		// Print basic info about this scheduler
		output.println(s.processcount + " processes");
		output.println("Using First Come First Served");
		output.println();
		s.readyProcesses = new LinkedList<Process>();

		// Start the timer
		int cur_time = 0;
		int time_limit = s.runfor;
		while (cur_time <= time_limit){
			String timestamp = "Time " + cur_time + ": ";
			
			// Find any new arrivals and put them in the queue
			for(Process p : s.processes){
				if(p.arrival == cur_time){
					output.println(timestamp + p.name + " arrived");
					s.readyProcesses.add(p);
				}
			}

			// Run the first process in the queue
      if(s.readyProcesses.size() > 0){
        Process this_p = s.readyProcesses.getFirst();
        
        // Did it finish?
        if(this_p.burst == 0){
          output.println(timestamp + this_p.name + " finished");

          // Remove it from the queue
          s.readyProcesses.remove();
					this_p.finish_time = cur_time;
					
					// Get the next process
					if(s.readyProcesses.size() > 0){
						this_p = s.readyProcesses.getFirst();
					}
        }

				// Print a message if this process is just starting up
				if(this_p.runtime == 0){
					output.println(timestamp + this_p.name + " selected (burst " + this_p.burst + ")");
				}

				// Run the process
				this_p.runtime++;
				this_p.burst--;
      }else{
				// Ready queue is empty
        output.println(timestamp + " Idle");
      }
      cur_time++; 	
		}

		output.println();
		for(Process p : s.processes){
			output.println(p.name + " wait " + " turnaround " + (p.finish_time - p.arrival));
		}
	}

private static void ShortestJobFirst(Scheduler s, PrintWriter output){
		
		// Print basic info about this scheduler
		output.println(s.processcount + " process");
		output.println("Using Shortest Job First");
		output.println();
		s.readyProcesses = new LinkedList<Process>();

		// Start the timer	
		int cur_time = 0;
		int time_limit = s.runfor;
		Process prevProcess = null;
		while(cur_time < time_limit){
			String timestamp = "Time " + cur_time + ": ";

			//Process this_p = null;
			//if(s.readyProcesses.size() > 0){
			//	this_p = getShortestProcess(s.readyProcesses);
			//}

			// Loop through each process
			for(Process p : s.processes){
				if(p.arrival == cur_time){
					output.println(timestamp + p.name + " arrived");
					s.readyProcesses.add(p);
					p.wait = 0;
				}

				if(s.readyProcesses.contains(p)){// && p.name != this_p.name){
					p.wait++;
				}
			}

			boolean active = false;
			// Run the current process
      //if(this_p != null){
			if(s.readyProcesses.size() > 0){
        Process this_p = getShortestProcess(s.readyProcesses);
       	
        // Did it finish?
        if(this_p.burst == 0){
          output.println(timestamp + this_p.name + " finished");

          // Remove it from the queue
          s.readyProcesses.remove(this_p);
          this_p.finish_time = cur_time;

          // Get the next process
          if(s.readyProcesses.size() > 0){
            this_p = getShortestProcess(s.readyProcesses);
          }
        }

				// If we switched to a new process, announce it
 				if(prevProcess == null || prevProcess.name != this_p.name){
          output.println(timestamp + this_p.name + " selected (burst " + this_p.burst + ")");
        }

				prevProcess = this_p;
				if(this_p.burst != 0){
					// Run the process
 		      this_p.runtime++;
 	  	    this_p.burst--;
					active = true;
				}
      }

			if(!active){
        output.println(timestamp + " Idle");
      }

			cur_time++;
		}

		output.println("Finished at time " + cur_time);
		output.println();
		for(Process p : s.processes){
      output.println(p.name + " wait " + p.wait + " turnaround " + (p.finish_time - p.arrival));
    }		
	}

private static Process getShortestProcess(LinkedList<Process> readyProcesses){
		Process min_p = null;
		for(Process p : readyProcesses){
			if(min_p == null || min_p.burst > p.burst){
				min_p = p;
			}
		}
		return min_p;
}

	private static void RoundRobin(Scheduler s, PrintWriter output){
		
		// Print basic info about this scheduler
		output.println(s.processcount + " processes");
		output.println("Using Round-Robin");
		output.println("Quantum " + s.quantum);
		output.println();
		s.readyProcesses = new LinkedList<Process>();

		// Start the timer 
		int cur_time = 0;
		int time_limit = s.runfor;
		while (cur_time < time_limit){
			String timestamp = "Time " + cur_time + ": ";

			// Loop through each process
			for(Process p : s.processes){

				// Find any new arrivals
				if(p.arrival == cur_time){
					output.println(timestamp + p.name + " arrived");
				}

				// Decide whether it should be put in the queue
				boolean hasArrived = p.arrival <= cur_time;
				boolean inQueue = s.readyProcesses.contains(p);
				boolean hasFinished = p.burst == 0;
				if(hasArrived && !inQueue && !hasFinished){
					s.readyProcesses.add(p);
				}
			}

			// Run the first process in the queue
			if(s.readyProcesses.size() > 0){
				Process this_p = s.readyProcesses.getFirst();

				// Print a message if we're just now switching to this process 
				if(this_p.runtime == 0){
					output.println(timestamp + this_p.name + " selected (burst " + this_p.burst + ")");
				}

				// Run the process
				this_p.runtime++;
				this_p.burst--;
				//System.out.println(" " + timestamp + this_p.name + " running ");
				
				// Did it finish?
				if(this_p.burst == 0){
					output.println(timestamp + this_p.name + " finished");
					// Remove it from the queue
					s.readyProcesses.remove();
					this_p.finish_time = cur_time;
				}

				// Stop if it exceded the quantum
				if(this_p.runtime >= s.quantum){
					this_p.runtime = 0;
					s.readyProcesses.remove();
					//System.out.println(" " + timestamp + " " + this_p.name + " stopped");
				}
			}else{
				output.println(timestamp + " Idle");
			}

			cur_time++;
		}

		output.println("Finished at time " + cur_time);
		output.println();
		for(Process p : s.processes){
      output.println(p.name + " wait " + " turnaround " + (p.finish_time - p.arrival));
    }
	}
}