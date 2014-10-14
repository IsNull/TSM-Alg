
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package mse.alg.ex3.JTS;


import java.io.Serializable;

/**
 * Defines a rectangular region of the 2D coordinate plane. It is often used to
 * represent the bounding box of a {@link com.vividsolutions.jts.geom.Geometry}, e.g. the minimum and
 * maximum x and y values of the {@link com.vividsolutions.jts.geom.Coordinate}s.
 * <p>
 * Note that Envelopes support infinite or half-infinite regions, by using the
 * values of <code>Double.POSITIVE_INFINITY</code> and
 * <code>Double.NEGATIVE_INFINITY</code>.
 * <p>
 * When Envelope objects are created or initialized, the supplies extent values
 * are automatically sorted into the correct order.
 *
 * @version 1.7
 *
 * @author Christoph Stamm
 */
public class Envelope3D implements Serializable {
	private static final long serialVersionUID = 5873921885273102420L;

	public int hashCode() {
		// Algorithm from Effective Java by Joshua Bloch [Jon Aquino]
		int result = 17;
		result = 37 * result + Coordinate.hashCode(minx);
		result = 37 * result + Coordinate.hashCode(maxx);
		result = 37 * result + Coordinate.hashCode(miny);
		result = 37 * result + Coordinate.hashCode(maxy);
		result = 37 * result + Coordinate.hashCode(minz);
		result = 37 * result + Coordinate.hashCode(maxz);
		return result;
	}

	/**
	 * Test the point q to see whether it intersects the Envelope defined by
	 * p1-p2
	 * 
	 * @param p1
	 *            one extremal point of the envelope
	 * @param p2
	 *            another extremal point of the envelope
	 * @param q
	 *            the point to test for intersection
	 * @return <code>true</code> if q intersects the envelope p1-p2
	 */
	public static boolean intersects(Coordinate p1, Coordinate p2, Coordinate q) {
		//OptimizeIt shows that Math#min and Math#max here are a bottleneck.
		//Replace with direct comparisons. [Jon Aquino]
		if (((q.x >= (p1.x < p2.x ? p1.x : p2.x)) && (q.x <= (p1.x > p2.x ? p1.x : p2.x))) &&
		    ((q.y >= (p1.y < p2.y ? p1.y : p2.y)) && (q.y <= (p1.y > p2.y ? p1.y : p2.y))) &&
		    ((q.z >= (p1.z < p2.z ? p1.z : p2.z)) && (q.z <= (p1.z > p2.z ? p1.z : p2.z)))) {
			return true;
		}
		return false;
	}

	/**
	 * Tests whether the envelope defined by p1-p2 and the envelope defined by
	 * q1-q2 intersect.
	 * 
	 * @param p1
	 *            one extremal point of the envelope P
	 * @param p2
	 *            another extremal point of the envelope P
	 * @param q1
	 *            one extremal point of the envelope Q
	 * @param q2
	 *            another extremal point of the envelope Q
	 * @return <code>true</code> if Q intersects P
	 */
	public static boolean intersects(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
	    double minq = Math.min(q1.x, q2.x);
	    double maxq = Math.max(q1.x, q2.x);
	    double minp = Math.min(p1.x, p2.x);
	    double maxp = Math.max(p1.x, p2.x);
	
	    if( minp > maxq )
	        return false;
	    if( maxp < minq )
	        return false;
	
	    minq = Math.min(q1.y, q2.y);
	    maxq = Math.max(q1.y, q2.y);
	    minp = Math.min(p1.y, p2.y);
	    maxp = Math.max(p1.y, p2.y);
	
	    if( minp > maxq )
	        return false;
	    if( maxp < minq )
	        return false;
	
	    minq = Math.min(q1.z, q2.z);
	    maxq = Math.max(q1.z, q2.z);
	    minp = Math.min(p1.z, p2.z);
	    maxp = Math.max(p1.z, p2.z);
	
	    if( minp > maxq )
	        return false;
	    if( maxp < minq )
	        return false;

	    return true;
	}

	/**
	 * the minimum x-coordinate
	 */
	private double minx;

	/**
	 * the maximum x-coordinate
	 */
	private double maxx;

	/**
	 * the minimum y-coordinate
	 */
	private double miny;

	/**
	 * the maximum y-coordinate
	 */
	private double maxy;

	/**
	 * the minimum z-coordinate
	 */
	private double minz;

	/**
	 * the maximum z-coordinate
	 */
	private double maxz;

	/**
	 * Creates a null <code>Envelope</code>.
	 */
	public Envelope3D() {
		init();
	}

	/**
	 * Creates an <code>Envelope</code> for a region defined by maximum and
	 * minimum values.
	 *
	 * @param x1
	 *            the first x-value
	 * @param x2
	 *            the second x-value
	 * @param y1
	 *            the first y-value
	 * @param y2
	 *            the second y-value
	 * @param z1
	 *            the first y-value
	 * @param z2
	 *            the second y-value
	 */
	public Envelope3D(double x1, double x2, double y1, double y2, double z1,
			double z2) {
		init(x1, x2, y1, y2, z1, z2);
	}

	/**
	 * Creates an <code>Envelope</code> for a region defined by two Coordinates.
	 *
	 * @param p1
	 *            the first Coordinate
	 * @param p2
	 *            the second Coordinate
	 */
	public Envelope3D(Coordinate p1, Coordinate p2) {
		init(p1.x, p2.x, p1.y, p2.y, p1.z, p2.z);
	}

	/**
	 * Creates an <code>Envelope</code> for a region defined by a single
	 * Coordinate.
	 *
	 * @param p
	 *            the Coordinate
	 */
	public Envelope3D(Coordinate p) {
		init(p.x, p.x, p.y, p.y, p.z, p.z);
	}

	/**
	 * Create an <code>Envelope</code> from an existing Envelope.
	 *
	 * @param env
	 *            the Envelope to initialize from
	 */
	public Envelope3D(Envelope3D env) {
		init(env);
	}

	/**
	 * Initialize to a null <code>Envelope</code>.
	 */
	public void init() {
		setToNull();
	}

	/**
	 * Initialize an <code>Envelope</code> for a region defined by maximum and
	 * minimum values.
	 *
	 * @param x1
	 *            the first x-value
	 * @param x2
	 *            the second x-value
	 * @param y1
	 *            the first y-value
	 * @param y2
	 *            the second y-value
	 * @param z1
	 *            the first y-value
	 * @param z2
	 *            the second y-value
	 */
	public void init(double x1, double x2, double y1, double y2, double z1,
			double z2) {
		if (x1 < x2) {
			minx = x1;
			maxx = x2;
		} else {
			minx = x2;
			maxx = x1;
		}
		if (y1 < y2) {
			miny = y1;
			maxy = y2;
		} else {
			miny = y2;
			maxy = y1;
		}
		if (z1 < z2) {
			minz = z1;
			maxz = z2;
		} else {
			minz = z2;
			maxz = z1;
		}
	}

	/**
	 * Initialize an <code>Envelope</code> to a region defined by two
	 * Coordinates.
	 *
	 * @param p1
	 *            the first Coordinate
	 * @param p2
	 *            the second Coordinate
	 */
	public void init(Coordinate p1, Coordinate p2) {
		init(p1.x, p2.x, p1.y, p2.y, p1.z, p2.z);
	}

	/**
	 * Initialize an <code>Envelope</code> to a region defined by a single
	 * Coordinate.
	 *
	 * @param p
	 *            the coordinate
	 */
	public void init(Coordinate p) {
		init(p.x, p.x, p.y, p.y, p.z, p.z);
	}

	/**
	 * Initialize an <code>Envelope</code> from an existing Envelope.
	 *
	 * @param env
	 *            the Envelope to initialize from
	 */
	public void init(Envelope3D env) {
		this.minx = env.minx;
		this.maxx = env.maxx;
		this.miny = env.miny;
		this.maxy = env.maxy;
		this.minz = env.minz;
		this.maxz = env.maxz;
	}

	/**
	 * Makes this <code>Envelope</code> a "null" envelope, that is, the envelope
	 * of the empty geometry.
	 */
	public void setToNull() {
		minx = 0;
		maxx = -1;
		miny = 0;
		maxy = -1;
		minz = 0;
		maxz = -1;
	}

	/**
	 * Returns <code>true</code> if this <code>Envelope</code> is a "null"
	 * envelope.
	 *
	 * @return <code>true</code> if this <code>Envelope</code> is uninitialized
	 *         or is the envelope of the empty geometry.
	 */
	public boolean isNull() {
		return maxx < minx;
	}

	/**
	 * Returns the difference between the maximum and minimum x values.
	 *
	 * @return max x - min x, or 0 if this is a null <code>Envelope</code>
	 */
	public double getWidthX() {
		if (isNull()) {
			return 0;
		}
		return maxx - minx;
	}

	/**
	 * Returns the difference between the maximum and minimum y values.
	 *
	 * @return max y - min y, or 0 if this is a null <code>Envelope</code>
	 */
	public double getWidthY() {
		if (isNull()) {
			return 0;
		}
		return maxy - miny;
	}

	/**
	 * Returns the difference between the maximum and minimum x values.
	 *
	 * @return max z - min z, or 0 if this is a null <code>Envelope</code>
	 */
	public double getWidthZ() {
		if (isNull()) {
			return 0;
		}
		return maxz - minz;
	}

	/**
	 * Returns the <code>Envelope</code>s minimum x-value. min x > max x
	 * indicates that this is a null <code>Envelope</code>.
	 *
	 * @return the minimum x-coordinate
	 */
	public double getMinX() {
		return minx;
	}

	/**
	 * Returns the <code>Envelope</code>s maximum x-value. min x > max x
	 * indicates that this is a null <code>Envelope</code>.
	 *
	 * @return the maximum x-coordinate
	 */
	public double getMaxX() {
		return maxx;
	}

	/**
	 * Returns the <code>Envelope</code>s minimum y-value. min y > max y
	 * indicates that this is a null <code>Envelope</code>.
	 *
	 * @return the minimum y-coordinate
	 */
	public double getMinY() {
		return miny;
	}

	/**
	 * Returns the <code>Envelope</code>s maximum y-value. min y > max y
	 * indicates that this is a null <code>Envelope</code>.
	 *
	 * @return the maximum y-coordinate
	 */
	public double getMaxY() {
		return maxy;
	}

	/**
	 * Returns the <code>Envelope</code>s minimum y-value. min z > max z
	 * indicates that this is a null <code>Envelope</code>.
	 *
	 * @return the minimum z-coordinate
	 */
	public double getMinZ() {
		return minz;
	}

	/**
	 * Returns the <code>Envelope</code>s maximum y-value. min z > max z
	 * indicates that this is a null <code>Envelope</code>.
	 *
	 * @return the maximum z-coordinate
	 */
	public double getMaxZ() {
		return maxz;
	}

	/**
	 * Gets the area of this envelope.
	 * 
	 * @return the area of the envelope
	 * @return 0.0 if the envelope is null
	 */
	public double getArea() {
		return getWidthX() * getWidthY();
	}

	/**
	 * Gets the volume of this envelope.
	 * 
	 * @return the area of the envelope
	 * @return 0.0 if the envelope is null
	 */
	public double getVolume() {
		return getWidthX() * getWidthY() * getWidthZ();
	}

	/**
	 * Gets the minimum extent of this envelope across all dimensions.
	 * 
	 * @return the minimum extent of this envelope
	 */
	public double minExtent() {
		if (isNull())
			return 0.0;
		double wx = getWidthX();
		double wy = getWidthY();
		double wz = getWidthZ();
		if (wx < wy) {
			return Math.min(wx, wz);
		} else {
			return Math.min(wy, wz);
		}
	}

	/**
	 * Gets the maximum extent of this envelope across all dimensions.
	 * 
	 * @return the maximum extent of this envelope
	 */
	public double maxExtent() {
		if (isNull())
			return 0.0;
		double wx = getWidthX();
		double wy = getWidthY();
		double wz = getWidthZ();
		if (wx < wy) {
			return Math.max(wx, wz);
		} else {
			return Math.max(wy, wz);
		}
	}

	/**
	 * Enlarges this <code>Envelope</code> so that it contains the given
	 * {@link com.vividsolutions.jts.geom.Coordinate}. Has no effect if the point is already on or within
	 * the envelope.
	 *
	 * @param p
	 *            the Coordinate to expand to include
	 */
	public void expandToInclude(Coordinate p) {
		expandToInclude(p.x, p.y, p.z);
	}

	/**
	 * Expands this envelope by a given distance in all directions. Both
	 * positive and negative distances are supported.
	 *
	 * @param distance
	 *            the distance to expand the envelope
	 */
	public void expandBy(double distance) {
		expandBy(distance, distance, distance);
	}

	/**
	 * Expands this envelope by a given distance in all directions. Both
	 * positive and negative distances are supported.
	 *
	 * @param deltaX
	 *            the distance to expand the envelope along the the X axis
	 * @param deltaY
	 *            the distance to expand the envelope along the the Y axis
	 * @param deltaZ
	 *            the distance to expand the envelope along the the Z axis
	 */
	public void expandBy(double deltaX, double deltaY, double deltaZ) {
		if (isNull())
			return;

		minx -= deltaX;
		maxx += deltaX;
		miny -= deltaY;
		maxy += deltaY;
		minz -= deltaZ;
		maxz += deltaZ;

		// check for envelope disappearing
		if (minx > maxx || miny > maxy || minz > maxz)
			setToNull();
	}

	/**
	 * Enlarges this <code>Envelope</code> so that it contains the given point.
	 * Has no effect if the point is already on or within the envelope.
	 *
	 * @param x
	 *            the value to lower the minimum x to or to raise the maximum x
	 *            to
	 * @param y
	 *            the value to lower the minimum y to or to raise the maximum y
	 *            to
	 * @param z
	 *            the value to lower the minimum z to or to raise the maximum z
	 *            to
	 */
	public void expandToInclude(double x, double y, double z) {
		if (isNull()) {
			minx = x;
			maxx = x;
			miny = y;
			maxy = y;
			minz = z;
			maxz = z;
		} else {
			if (x < minx) {
				minx = x;
			}
			if (x > maxx) {
				maxx = x;
			}
			if (y < miny) {
				miny = y;
			}
			if (y > maxy) {
				maxy = y;
			}
			if (z < minz) {
				minz = z;
			}
			if (z > maxz) {
				maxz = z;
			}
		}
	}

	/**
	 * Enlarges this <code>Envelope</code> so that it contains the
	 * <code>other</code> Envelope. Has no effect if <code>other</code> is
	 * wholly on or within the envelope.
	 *
	 * @param other
	 *            the <code>Envelope</code> to expand to include
	 */
	public void expandToInclude(Envelope3D other) {
		if (other.isNull()) {
			return;
		}
		if (isNull()) {
			minx = other.getMinX();
			maxx = other.getMaxX();
			miny = other.getMinY();
			maxy = other.getMaxY();
			minz = other.getMinZ();
			maxz = other.getMaxZ();
		} else {
			if (other.minx < minx) {
				minx = other.minx;
			}
			if (other.maxx > maxx) {
				maxx = other.maxx;
			}
			if (other.miny < miny) {
				miny = other.miny;
			}
			if (other.maxy > maxy) {
				maxy = other.maxy;
			}
			if (other.minz < minz) {
				minz = other.minz;
			}
			if (other.maxz > maxz) {
				maxz = other.maxz;
			}
		}
	}

	/**
	 * Translates this envelope by given amounts in the X and Y and Z direction.
	 *
	 * @param transX
	 *            the amount to translate along the X axis
	 * @param transY
	 *            the amount to translate along the Y axis
	 * @param transZ
	 *            the amount to translate along the Z axis
	 */
	public void translate(double transX, double transY, double transZ) {
		if (isNull()) {
			return;
		}
		init(getMinX() + transX, getMaxX() + transX, 
			 getMinY() + transY, getMaxY() + transY,
			 getMinZ() + transZ, getMaxZ() + transZ);
	}

	/**
	 * Computes the coordinate of the center of this envelope (as long as it is
	 * non-null
	 *
	 * @return the center coordinate of this envelope <code>null</code> if the
	 *         envelope is null
	 */
	public Coordinate centre() {
		if (isNull())
			return null;
		return new Coordinate(
				(getMinX() + getMaxX())/2.0,
				(getMinY() + getMaxY())/2.0,
				(getMinZ() + getMaxZ())/2.0);
	}

	/**
	 * Computes a corner of this envelope (as long as it is non-null)
	 * 
	 * @param minX true: smaller x-coordinate is; false: larger x-coordinate is used
	 * @param minY true: smaller y-coordinate is; false: larger y-coordinate is used
	 * @param minZ true: smaller z-coordinate is; false: larger z-coordinate is used
	 * @return corner
	 */
	public Coordinate corner(boolean minX, boolean minY, boolean minZ) {
		if (isNull()) return null;
		
		return new Coordinate(
				(minX) ? getMinX() : getMaxX(),
				(minY) ? getMinY() : getMaxY(),
				(minZ) ? getMinZ() : getMaxZ());
	}
	
	/**
	 * Computes the intersection of two {@link Envelope3D}s.
	 *
	 * @param env
	 *            the envelope to intersect with
	 * @return a new Envelope representing the intersection of the envelopes
	 *         (this will be the null envelope if either argument is null, or
	 *         they do not intersect
	 */
	public Envelope3D intersection(Envelope3D env) {
		if (isNull() || env.isNull() || !intersects(env))
			return new Envelope3D();

		double intMinX = minx > env.minx ? minx : env.minx;
		double intMinY = miny > env.miny ? miny : env.miny;
		double intMinZ = minz > env.minz ? minz : env.minz;
		double intMaxX = maxx < env.maxx ? maxx : env.maxx;
		double intMaxY = maxy < env.maxy ? maxy : env.maxy;
		double intMaxZ = maxz < env.maxz ? maxz : env.maxz;
		return new Envelope3D(intMinX, intMaxX, intMinY, intMaxY, intMinZ, intMaxZ);
	}

	/**
	 * Check if the region defined by <code>other</code> overlaps (intersects)
	 * the region of this <code>Envelope</code>.
	 *
	 * @param other
	 *            the <code>Envelope</code> which this <code>Envelope</code> is
	 *            being checked for overlapping
	 * @return <code>true</code> if the <code>Envelope</code>s overlap
	 */
	public boolean intersects(Envelope3D other) {
		if (isNull() || other.isNull()) {
			return false;
		}
		return !(other.minx > maxx || other.maxx < minx || 
				 other.miny > maxy || other.maxy < miny ||
				 other.minz > maxz || other.maxz < minz);
	}

	/**
	 * @deprecated Use #intersects instead. In the future, #overlaps may be
	 *             changed to be a true overlap check; that is, whether the
	 *             intersection is two-dimensional.
	 */
	public boolean overlaps(Envelope3D other) {
		return intersects(other);
	}

	/**
	 * Check if the point <code>p</code> overlaps (lies inside) the region of
	 * this <code>Envelope</code>.
	 *
	 * @param p
	 *            the <code>Coordinate</code> to be tested
	 * @return <code>true</code> if the point overlaps this
	 *         <code>Envelope</code>
	 */
	public boolean intersects(Coordinate p) {
		return intersects(p.x, p.y, p.z);
	}

	/**
	 * @deprecated Use #intersects instead.
	 */
	public boolean overlaps(Coordinate p) {
		return intersects(p);
	}

	/**
	 * Check if the point <code>(x, y, z)</code> overlaps (lies inside) the region
	 * of this <code>Envelope</code>.
	 *
	 * @param x
	 *            the x-ordinate of the point
	 * @param y
	 *            the y-ordinate of the point
	 * @param z
	 *            the z-ordinate of the point
	 * @return <code>true</code> if the point overlaps this
	 *         <code>Envelope</code>
	 */
	public boolean intersects(double x, double y, double z) {
		if (isNull())
			return false;
		return !(x > maxx || x < minx || 
				 y > maxy || y < miny ||
				 z > maxz || z < minz);
	}

	/**
	 * @deprecated Use #intersects instead.
	 */
	public boolean overlaps(double x, double y, double z) {
		return intersects(x, y, z);
	}

	/**
	 * Tests if the <code>Envelope other</code> lies wholely inside this
	 * <code>Envelope</code> (inclusive of the boundary).
	 * The same as <code>covers</code>
	 * <p>
	 * Note that this is <b>not</b> the same definition as the SFS
	 * <tt>contains</tt>, which would exclude the envelope boundary.
	 *
	 * @param other
	 *            the <code>Envelope</code> to check
	 * @return true if <code>other</code> is contained in this
	 *         <code>Envelope</code>
	 *
	 * @see #covers(Envelope3D)
	 */
	public boolean contains(Envelope3D other) {
		return covers(other);
	}

	/**
	 * Tests if the given point lies in or on the envelope.
	 * The same as <code>covers</code>
	 * <p>
	 * Note that this is <b>not</b> the same definition as the SFS
	 * <tt>contains</tt>, which would exclude the envelope boundary.
	 *
	 * @param p
	 *            the point which this <code>Envelope</code> is being checked
	 *            for containing
	 * @return <code>true</code> if the point lies in the interior or on the
	 *         boundary of this <code>Envelope</code>.
	 * 
	 * @see #covers(com.vividsolutions.jts.geom.Coordinate)
	 */
	public boolean contains(Coordinate p) {
		return covers(p);
	}

	/**
	 * Tests if the given point lies in or on the envelope.
	 * The same as <code>covers</code>
	 * <p>
	 * Note that this is <b>not</b> the same definition as the SFS
	 * <tt>contains</tt>, which would exclude the envelope boundary.
	 *
	 * @param x
	 *            the x-coordinate of the point which this <code>Envelope</code>
	 *            is being checked for containing
	 * @param y
	 *            the y-coordinate of the point which this <code>Envelope</code>
	 *            is being checked for containing
	 * @param z
	 *            the z-coordinate of the point which this <code>Envelope</code>
	 *            is being checked for containing
	 * @return <code>true</code> if <code>(x, y)</code> lies in the interior or
	 *         on the boundary of this <code>Envelope</code>.
	 * 
	 * @see #covers(double, double)
	 */
	public boolean contains(double x, double y, double z) {
		return covers(x, y, z);
	}

	/**
	 * Tests if the given point lies in or on the envelope.
	 *
	 * @param x
	 *            the x-coordinate of the point which this <code>Envelope</code>
	 *            is being checked for containing
	 * @param y
	 *            the y-coordinate of the point which this <code>Envelope</code>
	 *            is being checked for containing
	 * @param z
	 *            the z-coordinate of the point which this <code>Envelope</code>
	 *            is being checked for containing
	 * @return <code>true</code> if <code>(x, y)</code> lies in the interior or
	 *         on the boundary of this <code>Envelope</code>.
	 */
	public boolean covers(double x, double y, double z) {
		if (isNull())
			return false;
		return x >= minx && x <= maxx && 
			   y >= miny && y <= maxy &&
			   z >= minz && z <= maxz;
	}

	/**
	 * Tests if the given point lies in or on the envelope.
	 *
	 * @param p
	 *            the point which this <code>Envelope</code> is being checked
	 *            for containing
	 * @return <code>true</code> if the point lies in the interior or on the
	 *         boundary of this <code>Envelope</code>.
	 */
	public boolean covers(Coordinate p) {
		return covers(p.x, p.y, p.z);
	}

	/**
	 * Tests if the <code>Envelope other</code> lies wholely inside this
	 * <code>Envelope</code> (inclusive of the boundary).
	 *
	 * @param other
	 *            the <code>Envelope</code> to check
	 * @return true if this <code>Envelope</code> covers the <code>other</code>
	 */
	public boolean covers(Envelope3D other) {
		if (isNull() || other.isNull()) {
			return false;
		}
		return other.getMinX() >= minx && other.getMaxX() <= maxx && 
			   other.getMinY() >= miny && other.getMaxY() <= maxy &&
			   other.getMinZ() >= minz && other.getMaxZ() <= maxz;
	}

	/**
	 * Computes the distance between this and another <code>Envelope</code>. The
	 * distance between overlapping Envelopes is 0. Otherwise, the distance is
	 * the Euclidean distance between the closest points.
	 */
	public double distance(Envelope3D env) {
		if (intersects(env))
			return 0;

		double dx = 0.0;
		if (maxx < env.minx)
			dx = env.minx - maxx;
		else if (minx > env.maxx)
			dx = minx - env.maxx;

		double dy = 0.0;
		if (maxy < env.miny)
			dy = env.miny - maxy;
		else if (miny > env.maxy)
			dy = miny - env.maxy;

		double dz = 0.0;
		if (maxz < env.minz)
			dz = env.minz - maxz;
		else if (minz > env.maxz)
			dz = minz - env.maxz;

		// if either is zero, the envelopes overlap either vertically or
		// horizontally
		if (dx == 0.0)
			return dy;
		if (dy == 0.0)
			return dx;
		if (dz == 0.0)
			return dz;
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}

	public boolean equals(Object other) {
		if (!(other instanceof Envelope3D)) {
			return false;
		}
		Envelope3D env = (Envelope3D) other;
		if (isNull()) {
			return env.isNull();
		}
		return maxx == env.getMaxX() && maxy == env.getMaxY() && maxz == env.getMaxZ() && 
			   minx == env.getMinX() && miny == env.getMinY() && minz == env.getMinZ();
	}

	public String toString() {
		return "Env[" + minx + " : " + maxx + ", " + miny + " : " + maxy + ", " + minz + " : " + maxz + "]";
	}
}
