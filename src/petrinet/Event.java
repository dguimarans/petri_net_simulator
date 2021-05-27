package petrinet;

public class Event implements Comparable<Event> {
	
	private int transition;
	private double time;
	
	public Event(int transition, double time){
		this.transition = transition;
		this.time = time;
	}
	
	public int getTransition(){
		return this.transition;
	}
	
	public double getTime(){
		return this.time;
	}

	
	public int compareTo(Event otherEvent) {
		if (this.time < otherEvent.getTime())
			return -1;
		else if (this.time == otherEvent.getTime())
			return 0;
		else
			return 1;
	}

}
