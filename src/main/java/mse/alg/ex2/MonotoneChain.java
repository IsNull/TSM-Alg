package  mse.alg.ex2;

import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import java.util.List;

/**
 * Immutable, strictly x-monotone chain of points
 * @author Christoph Stamm
 *
 */
public class MonotoneChain implements Comparable<MonotoneChain> {
	public static RobustLineIntersector s_rli = new RobustLineIntersector();

	private Coordinate[] m_coords;						// coordinates in increasing x-order
	
	private MonotoneChain(int size) {
		assert size >= 2 : "wrong size: " + size;
		m_coords = new Coordinate[size];
	}

	/**
	 * Create new x-monotone chain of two points
	 * @param p1
	 * @param p2
	 */
	public MonotoneChain(Coordinate p1, Coordinate p2) {
		m_coords = new Coordinate[2];
		add(0, p1);
		add(1, p2);
		assert isValid() : "is not x-monotone: " + toLineString();
	}
	
	/**
	 * Create new x-monotone chain of all three points
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public MonotoneChain(Coordinate p1, Coordinate p2, Coordinate p3) {
		m_coords = new Coordinate[3];
		add(0, p1);
		add(1, p2);
		add(2, p3);
		assert isValid() : "is not x-monotone: " + toLineString();
	}
	
	/**
	 * Create new x-monotone chain of all points
	 * @param coords points
	 * @return x-monotone chain or null if the number of points is too small
	 */
	public static MonotoneChain create(List<Coordinate> coords) {
		if (coords.size() < 2) return null;
		
		MonotoneChain mc = new MonotoneChain(coords.size());
		coords.toArray(mc.m_coords);

		assert mc.isValid() : 
			"mc is not valid: " + mc.toLineString();
		return mc;
	}
	
	/**
	 * Validity checker: checks strict x-monotonicity in O(n) time
	 * @return true if the chain is strict x-monotone
	 */
	public boolean isValid() {
		// check x-monotonicity
		for (int i=0; i < m_coords.length - 1; i++) {
			if (m_coords[i + 1].x <= m_coords[i].x) {
				System.out.println("is not x-monotone");
				return false;
			}
		}
		return true;
	}

	/**
	 * Interface to JTS geometry
	 * @return JTS line string 
	 */
	public LineString toLineString() {
		return new GeometryFactory().createLineString(m_coords);			
	}
	
	/**
	 * Return points
	 * @return
	 */
	public Coordinate[] getCoords() {
		return m_coords;
	}
	
	/**
	 * Add point (capacity must be large enough)
	 * @param c point
	 */
	private void add(int i, Coordinate c) {
		m_coords[i] = c;
	}
	
	/**
	 * Defines natural sorting order
	 * @param mc
	 * @return
	 */
	public int compareTo(MonotoneChain mc) {
		if (getMinX() < mc.getMinX()) return -1;
		if (getMinX() > mc.getMinX()) return 1;
		return (this == mc) ? 0 : -1;	// only identical chains are allowed to be equal
	}
	
	/**
	 * Returns true if two chains are siblings in a bigger x-monotone chain
	 * @param mc
	 * @return true if this is left sibling of mc
	 */
	public boolean isLeftSiblingOf(MonotoneChain mc) {
		return getLast().equals(mc.getFirst());
	}
	
	public int size() {
		return m_coords.length;
	}
	
	public Coordinate get(int index) {
		return m_coords[index];
	}
	
	public double getMinX() {
		return getFirst().x;
	}
	
	public double getMaxX() {
		return getLast().x;
	}
	
	public Coordinate getFirst() {
		return m_coords[0];
	}
	
	public Coordinate getLast() {
		return m_coords[m_coords.length - 1];
	}

	/**
	 * Splits a line segment vertically at x and computes an intersection point
	 * 
	 * @param startIndex start index of the segment being splitted
	 * @param x
	 */
	public Coordinate splitSegment(int startIndex, double x) {
		final double offset = 10; // arbitrary value > 0
		
		Coordinate p1 = m_coords[startIndex], p2 = m_coords[startIndex + 1];
		double yMin = Math.min(p1.y, p2.y) - offset;
		double yMax = Math.max(p1.y, p2.y) + offset;
		s_rli.computeIntersection(p1, p2, new Coordinate(x, yMin), new Coordinate(x, yMax));
		
		return (s_rli.hasIntersection()) ? s_rli.getIntersection(0) : null;
	}
	
}
