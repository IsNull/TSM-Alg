package

import java.util.PriorityQueue;

package  mse.alg.ex2;

import java.util.PriorityQueue;

/**
 * Sweep line
 * @author Christoph Stamm
 *
 * @param <S>
 */
public class SweepLine<S> {
	/**
	 * Base class of a sweep line event
	 * @author Christoph Stamm
	 *
	 */
	public abstract class Event implements Comparable<Event> {
		protected double m_time;	// event time
		protected int m_prio; 		// higher value means higher priority
		
		protected Event(double time, int prio) {
			m_time = time;
			m_prio = prio;
		}
		
		@Override
		public int compareTo(Event e) {
			if (m_time < e.m_time) return -1;
			if (m_time > e.m_time) return 1;
			if (m_prio > e.m_prio) return -1;
			if (m_prio < e.m_prio) return 1;
			return 0;
		}
		
		public abstract void apply(S s);
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	private PriorityQueue<Event> m_timeLine = new PriorityQueue<>();	// calendar
	private S m_status;												// status
	
	/**
	 * Creates new sweep line
	 * @param s status
	 */
	public SweepLine(S s) {
		m_status = s;
	}
	
	/**
	 * Add a new event to sweep line calendar
	 * @param e event
	 */
	public void addEvent(Event e) {
		m_timeLine.add(e);
	}
	
	/**
	 * Run sweep line process
	 */
	public void process() {
		while(!m_timeLine.isEmpty()) {
			Event e = m_timeLine.remove();
			e.apply(m_status);
		}
	}
	
}
