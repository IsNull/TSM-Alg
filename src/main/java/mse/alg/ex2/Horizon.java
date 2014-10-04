package  mse.alg.ex2;

import com.vividsolutions.jts.geom.LineString;



import java.util.LinkedList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * x-monotone linked list of strictly x-monotone chains
 * @author Christoph Stamm
 *
 */
public class Horizon {
	private LinkedList<MonotoneChain> m_chains = new LinkedList<>();	// x-monotone linked list of chains
	
	private Horizon() {	}
	
	/**
	 * Creates new horizon of one monotone chain
	 * 
	 * @param mc monotone chain
	 */
	public Horizon(MonotoneChain mc) {
		m_chains.add(mc);
	}
	
	public boolean isEmpty() {
		return m_chains.size() == 0;
	}
	
	public int size() {
		return m_chains.size();
	}
	
	/**
	 * Merges two horizons (this and h) in O(n + k) time and returns the merged horizon.
	 * Uses a plane sweep approach.
	 * 
	 * @param h second horizon
	 * @return merged horizon
	 */
	public Horizon merge(Horizon h) {
		Horizon horizon = new Horizon();
		SweepLine<Status> sl = new SweepLine<Status>(new Status(horizon));

		// TODO: fill in sweep line with initial events
		// TODO: run sweep line process
		
		assert horizon.isValid() : "invalid horizon";
		
		return horizon;
	}
	
	/**
	 * Checks x-monotonicity of this horizon in O(n) time
	 * @return true if x-monotone
	 */
	public boolean isValid() {
		MonotoneChain prev = null;
		for(MonotoneChain mc: m_chains) {
			if (prev != null && prev.getMaxX() > mc.getMinX()) 
				return false;
			prev = mc;
		}
		return true;
	}
	
	/**
	 * Interface to JTS
	 * @return LineString without duplicated points
	 */
	public LineString toLineString() {
		// count coordinates
		int cnt = 0;
		MonotoneChain prev = null;
		
		for(MonotoneChain mc: m_chains) {
			if (prev != null && prev.isLeftSiblingOf(mc))
				cnt--;
			cnt += mc.size();
			prev = mc;
		}
		
		// collect coordinates
		Coordinate[] coords = new Coordinate[cnt];
		cnt = 0;
		prev = null;
		for(MonotoneChain mc: m_chains) {
			if (prev != null && prev.isLeftSiblingOf(mc)) {
				System.arraycopy(mc.getCoords(), 1, coords, cnt, mc.size() - 1);
				cnt += mc.size() - 1;
			} else {
				System.arraycopy(mc.getCoords(), 0, coords, cnt, mc.size());
				cnt += mc.size();
			}
			prev = mc;
		}
		
		return new GeometryFactory().createLineString(coords);
	}
	
	/**
	 * Interface to JTS
	 * @return MultiLineString of all chains in this horizon
	 */
	public MultiLineString toMultiLineString() {
		LineString[] chains = new LineString[m_chains.size()];
		int i = 0;
		
		for(MonotoneChain mc: m_chains) {
			chains[i++] = mc.toLineString();
		}
		return new GeometryFactory().createMultiLineString(chains);
	}
	
	/**
	 * Add new chain to horizon
	 * @param mc x-monotone chain
	 */
	public void add(MonotoneChain mc) {
		if (mc != null) {
			assert m_chains.isEmpty() || m_chains.getLast().getMaxX() <= mc.getMinX();
			m_chains.add(mc);
		}
	}
	
}
