package mse.alg.ex2;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import mse.alg.ex2.Horizon;
import mse.alg.ex2.MonotoneChain;

import java.util.LinkedList;

/**
 * Sweep line status
 * @author Christoph Stamm
 *
 */
public class Status {
	private Horizon m_horizon;					// resulting horizon
	private LinkedList<Coordinate> m_result;	// resulting chain
	private MonotoneChain m_upper, m_lower; 	// two chains; if there is only one of them, then it is m_upper
	private int m_upperIndex, m_lowerIndex;		// index of last handled point in chain
	
	/**
	 * Create new status
	 * @param h resulting horizon
	 */
	public Status(Horizon h) {
		assert h != null;
		m_horizon = h;
		m_result = new LinkedList<Coordinate>();
	}
	
	public boolean isEmpty() {
		return m_upper == null;
	}
	
	public boolean hasTwoSegments() {
		return m_lower != null && m_upper != null;
	}
	
	/**
	 * Handles StartEvent
	 * for same x: start has lowest priority
	 * 
	 * @param mc monotone chain of the point to be handled
	 * @return visibility of included start point
	 */
	public void start(MonotoneChain mc) {
		//System.out.println("start");
		assert m_lower == null : "status already contains two segments";
		
		Coordinate p = mc.get(0);
		
		if (isEmpty()) {
			// first chain: start new upper 
			m_upper = mc; m_upperIndex = 0;
			add(p);
		} else {
			// second chain (mc) starts: compute new order
			Coordinate u0 = m_upper.get(m_upperIndex);
			Coordinate u1 = m_upper.get(m_upperIndex + 1);
			int orient = CGAlgorithms.orientationIndex(u0, u1, p);
			
			if (orient == 1) {
				// mc starts above m_upper
				if (p.x == u0.x) {
					assert p.y > u0.y;
				} else {
					assert p.x > u0.x && p.x < u1.x;
					// split m_upper chain at p.x
					Coordinate ip = m_upper.splitSegment(m_upperIndex, p.x);
					assert MonotoneChain.s_rli.isInteriorIntersection(0);
					add(ip);
				}
				swap();
				m_upper = mc; m_upperIndex = 0;
				
				add(p);
				
			} else if (orient == 0) {
				// mc starts on m_upper: check next vertex
				Coordinate p1 = mc.get(1);
				
				orient = CGAlgorithms.orientationIndex(u0, u1, p1);
				if (orient == 1) {
					// mc is above m_upper
					if (p.x == u0.x) {
						assert p.equals(u0);					
					} else {
						assert p.x > u0.x && p.x < u1.x;
						// continue current result
						add(p);
					}
					
					swap();
					m_upper = mc; m_upperIndex = 0;
					
				} else {
					// mc is below m_upper
					m_lower = mc; m_lowerIndex = 0;
				}
			} else {
				// mc starts below m_upper
				m_lower = mc; m_lowerIndex = 0;
			}
		}
		
		if (hasTwoSegments()) {
			intersect();
		}
	}
	
	private void intersect() {
		//System.out.println("intersect");
		assert m_upper != null : "invalid status";
		assert m_lower != null : "status contains one segement only";
		
		Coordinate u0 = m_upper.get(m_upperIndex);
		Coordinate l0 = m_lower.get(m_lowerIndex);
		
		if (m_upper.size() > m_upperIndex + 1 && m_lower.size() > m_lowerIndex + 1) {
			Coordinate u1 = m_upper.get(m_upperIndex + 1);
			Coordinate l1 = m_lower.get(m_lowerIndex + 1);
		
			// check for intersection
			MonotoneChain.s_rli.computeIntersection(u0, u1, l0, l1);
			
			if (MonotoneChain.s_rli.isProper()) {
				// handle intersection: inserting ip between l1 and l2
				Coordinate ip = MonotoneChain.s_rli.getIntersection(0);
				add(ip);
				swap();
			} else if (MonotoneChain.s_rli.isInteriorIntersection(0)) {
				// intersection in one of the end points of lower
				// handle this intersection point
				add(l0);
				
				// check orientation of u2
				if (CGAlgorithms.orientationIndex(l0, l1, m_upper.get(m_upperIndex + 1)) == -1) {
					// u2 is below lower
					swap();
				}
			} else if (MonotoneChain.s_rli.isInteriorIntersection(1)) { 
				// intersection in one of the end points of upper
				// handle this intersection point
				add(u0);
				
				// check orientation of l2
				if (CGAlgorithms.orientationIndex(u0, u1, m_lower.get(m_lowerIndex + 1)) == 1) {
					// l2 is above upper
					swap();
				}
			}
		}
	}
	
	/**
	 * Handles InnerEvent
	 * for same x: next has highest priority
	 * 
	 * @param mc monotone chain of the point to be handled
	 * @param index index of the point to be handled
	 */
	public void next(MonotoneChain mc, int index) {
		//System.out.println("nextVertex");
		if (mc == m_upper) {
			m_upperIndex = index;
			// continue result
			add(m_upper.get(m_upperIndex));
		} else if (mc == m_lower) {
			m_lowerIndex = index;
		}
		
		if (hasTwoSegments()) {
			intersect();
		}
	}
	
	/**
	 * Handles StopEvent
	 * for same x: stop is called after next but before start
	 * 
	 * @param mc monotone chain of the point to be handled
	 * @param index index of the point to be handled
	 */
	public void stop(MonotoneChain mc, int index) {
		assert mc == m_lower || mc == m_upper : "wrong mc";
		//System.out.println("stop");
		
		if (mc == m_upper) {
			m_upperIndex = index;
			// upper stops
			Coordinate p = mc.get(index);
			
			add(p);

			swap();
			
			if (m_upper != null) {
				add(m_upper.splitSegment(m_upperIndex, p.x));
			} else {
				finish();
			}
		} else {
			m_lowerIndex = index;
		}		
		m_lower = null;
	}
	
	private void swap() {
		MonotoneChain t = m_upper; m_upper = m_lower; m_lower = t;
		int i = m_upperIndex; m_upperIndex = m_lowerIndex; m_lowerIndex = i;			
	}
	
	private void add(Coordinate p) {
		Coordinate last = (m_result.isEmpty()) ? null : m_result.getLast();
		
		if (last == null || p.x > last.x) {
			m_result.add(p);
		} else if (p.y != last.y) {
			assert p.x == last.x;
			// vertical jump
			finish();
			
			// start new result
			m_result.add(p);
		}
	}
	
	private void finish() {
		m_horizon.add(MonotoneChain.create(m_result));
		m_result.clear();		
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
class StartEvent extends SweepLine<Status>.Event {
	private MonotoneChain m_mc;
	
	protected StartEvent(SweepLine<Status> sl, double time, MonotoneChain mc) {
		sl.super(time, 1);
		m_mc = mc;
	}

	@Override
	public void apply(Status s) {
		s.start(m_mc);
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
class InnerEvent extends SweepLine<Status>.Event {
	private MonotoneChain m_mc;
	private int m_index;
	
	protected InnerEvent(SweepLine<Status> sl, double time, MonotoneChain mc, int index) {
		sl.super(time, 3);
		m_mc = mc;
		m_index = index;
	}

	@Override
	public void apply(Status s) {
		s.next(m_mc, m_index);		
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
class StopEvent extends SweepLine<Status>.Event {
	private MonotoneChain m_mc;
	private int m_index;
	
	protected StopEvent(SweepLine<Status> sl, double time, MonotoneChain mc, int index) {
		sl.super(time, 2);
		m_mc = mc;
		m_index = index;
	}

	@Override
	public void apply(Status s) {
		s.stop(m_mc, m_index);
	}
}


