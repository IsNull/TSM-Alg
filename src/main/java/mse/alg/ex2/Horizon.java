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

        double timeCount = 0;

        /*
        //Debug
        System.out.println("--- Start ---");
        for(int j=0; j < this.m_chains.size(); j++) {
            for (int i = 0; i < this.m_chains.get(j).size(); i++) {
                System.out.println(j + "-this: " + this.m_chains.get(j).get(i).toString());
            }
        }
        for(int j=0; j < h.m_chains.size(); j++) {
            for (int i = 0; i < h.m_chains.get(j).size(); i++) {
                System.out.println(j + "-h: " + h.m_chains.get(j).get(i).toString());
            }
        }
        */

        SortHorizon sort1 = new SortHorizon(this.m_chains);
        SortHorizon sort2 = new SortHorizon(h.m_chains);

        while(!sort1.isFinished() || !sort2.isFinished()) {
            if (!sort1.isFinished()) {
                if (sort2.isFinished() || sort1.getNextX() <= sort2.getNextX()) {

                    if (sort1.isNextStart()) {
                        //System.out.println("Start-S1-Index:"+sort1.getNextX());
                        sl.addEvent(new StartEvent(sl, sort1.getNextX(), sort1.getChain()));
                        continue;
                    }
                    if (sort1.isNextStop()) {
                        //System.out.print("Stop-S1-Index:"+sort1.getNextX());
                        sl.addEvent(new StopEvent(sl, sort1.getNextX(), sort1.getChain(), sort1.getStopIndex()));
                        //System.out.println("-->"+sort1.getStopIndex());
                        continue;
                    }
                    if (sort1.isNextInner()) {
                        //System.out.print("Inner-S1-Index:"+sort1.getNextX());
                        sl.addEvent(new InnerEvent(sl, sort1.getNextX(), sort1.getChain(), sort1.getNextIndex() - 1));
                        //System.out.println("-->"+(sort1.getNextIndex() - 1));
                        continue;
                    }

                }
            }
            if (!sort2.isFinished()) {
                if (sort1.isFinished() || sort2.getNextX() <= sort1.getNextX()) {

                    if (sort2.isNextStart()) {
                        //System.out.println("Start-S2-Index:"+sort2.getNextX());
                        sl.addEvent(new StartEvent(sl, sort2.getNextX(), sort2.getChain()));
                        continue;
                    }
                    if (sort2.isNextStop()) {
                        //System.out.print("Stop-S2-Index:"+sort2.getNextX());
                        sl.addEvent(new StopEvent(sl, sort2.getNextX(), sort2.getChain(), sort2.getStopIndex()));
                        //System.out.println("-->"+sort2.getStopIndex());
                        continue;
                    }
                    if (sort2.isNextInner()) {
                        //System.out.print("Inner-S2-Index:"+sort2.getNextX());
                        sl.addEvent(new InnerEvent(sl, sort2.getNextX(), sort2.getChain(), sort2.getNextIndex() - 1));
                        //System.out.println("-->"+(sort2.getNextIndex() - 1));
                        continue;
                    }
                }

            }

        }

        sl.process();
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
