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



/**
 * A node of a {@link KdTree3D}, which represents one or more points in the same
 * location.
 * 
 * @author Christoph Stamm
 */
public class KdNode3D<E> {
	private MultiCoordinate3D p = null;
	private E data;
	private KdNode3D<E> left;
	private KdNode3D<E> right;

	/**
	 * Creates a new KdNode3D.
	 * 
	 * @param _x
	 *            coordinate of point
	 * @param _y
	 *            coordinate of point
	 * @param _z
	 *            coordinate of point
	 * @param _data
	 *            a data objects to associate with this node
	 */
	public KdNode3D(double _x, double _y, double _z, E _data) {
		p = new MultiCoordinate3D(_x, _y, _z);
		left = null;
		right = null;
		data = _data;
	}

	/**
	 * Creates a new KdNode3D.
	 * 
	 * @param _p
	 *            point location of new node
	 * @param _data
	 *            a data objects to associate with this node
	 */
	public KdNode3D(Coordinate _p, E _data) {
		this(_p.x, _p.y, _p.z, _data);
	}

	/**
	 * Creates a new KdNode3D.
	 * 
	 * @param _p
	 *            point location of new node
	 * @param _data
	 *            a data objects to associate with this node
	 */
	public KdNode3D(MultiCoordinate3D _p, E _data) {
		this(_p.x, _p.y, _p.z, _data);
		p.add(_p.getCount() - 1);
	}

	/**
	 * Returns the X coordinate of the node
	 * 
	 * @retrun X coordinate of the node
	 */
	public double getX() {
		return p.x;
	}

	/**
	 * Returns the Y coordinate of the node
	 * 
	 * @return Y coordinate of the node
	 */
	public double getY() {
		return p.y;
	}

	/**
	 * Returns the Z coordinate of the node
	 * 
	 * @return Z coordinate of the node
	 */
	public double getZ() {
		return p.z;
	}

	/**
	 * Returns the location of this node
	 * 
	 * @return p location of this node
	 */
	public Coordinate getCoordinate() {
		return p;
	}

	/**
	 * Gets the user data object associated with this node.
	 * 
	 * @return user data
	 */
	public E getData() {
		return data;
	}

	/**
	 * Sets the user data object associated with this node
	 * @param _data user data
	 */
	public void setData(E _data) {
		data = _data;
	}
	
	/**
	 * 
	 * @return true if it is a leaf node
	 */
	public boolean isLeaf() {
		return left == null && right == null;
	}
	
	/**
	 * Returns the left node of the tree
	 * 
	 * @return left node
	 */
	public KdNode3D<E> getLeft() {
		return left;
	}

	/**
	 * Returns the right node of the tree
	 * 
	 * @return right node
	 */
	public KdNode3D<E> getRight() {
		return right;
	}

	// Increments counts of points at this location
	void increment() {
		p.inc();
	}

	/**
	 * Returns the number of inserted points that are coincident at this
	 * location.
	 * 
	 * @return number of inserted points that this node represents
	 */
	public int getCount() {
		return p.getCount();
	}

	/**
	 * Tests whether more than one point with this value have been inserted (up
	 * to the tolerance)
	 * 
	 * @return true if more than one point have been inserted with this value
	 */
	public boolean isRepeated() {
		return p.getCount() > 1;
	}

	// Sets left node value
	void setLeft(KdNode3D<E> _left) {
		left = _left;
	}

	// Sets right node value
	void setRight(KdNode3D<E> _right) {
		right = _right;
	}
}
