package mse.alg.ex3.JTS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * An unfinished implementation of a 3-D KD-Tree. 
 * KD-trees provide fast range searching on point data.
 * <p>
 * This implementation supports detecting and snapping points which are closer
 * than a given tolerance value. If the same point (up to tolerance) is inserted
 * more than once a new node is not created but the count of the existing node
 * is incremented.
 * 
 * 
 * @author Christoph Stamm
 */
public class KdTree3D<E> {
	private static class ProjectionalComparator implements Comparator<Coordinate> {
		private int dimension;
		
		public ProjectionalComparator(int dim) {
			dimension = dim;
		}
		
		public int compare(Coordinate c1, Coordinate c2) {
			double d1 = c1.getOrdinate(dimension);
			double d2 = c2.getOrdinate(dimension);
			
			return Coordinate.DimensionalComparator.compare(d1, d2);
		}
	}
	
	private static ProjectionalComparator ProjComp[] = {
		new ProjectionalComparator(Coordinate.X),
		new ProjectionalComparator(Coordinate.Y),
		new ProjectionalComparator(Coordinate.Z)
	};
	
	private KdNode3D<E> root = null;
	private KdNode3D<E> last = null;
	private long numberOfNodes;
	private double tolerance;

	/**
	 * Creates a new instance of a KdTree3D with a snapping tolerance of 0.0.
	 * (I.e. distinct points will <i>not</i> be snapped)
	 */
	public KdTree3D() {
		this(0.0);
	}

	/**
	 * Creates a new instance of a KdTree3D, specifying a snapping distance
	 * tolerance. Points which lie closer than the tolerance to a point already
	 * in the tree will be treated as identical to the existing point.
	 * 
	 * @param _tolerance
	 *            the tolerance distance for considering two points equal
	 */
	public KdTree3D(double _tolerance) {
		tolerance = _tolerance;
	}

	/**
	 * Build a new homogeneous, balanced 3D kd-tree with the given points.
	 * No node data of type E is allocated.
	 * If the list elements implement the interface Multiplicity then the multiplicity of
	 * the elements is also stored in the tree.
	 * 
	 * @param points Tree content
	 * @return root node
	 */
	public KdNode3D<E> buildTree(List<? extends MultiCoordinate3D> points) {
		// TODO
		// there are already three comparators for x, y and z: ProjComp[i]
		
		assert size() == points.size() : "wrong tree size: " + size() + ", " + points.size();
		return root;
	}
	
	/**
	 * Tests whether the index contains any items.
	 * 
	 * @return true if the index does not contain any items
	 */
	public boolean isEmpty() {
		if (root == null)
			return true;
		return false;
	}

	/**
	 * Return number of nodes in tree
	 * 
	 * @return number of tree nodes
	 */
	public long size() {
		return numberOfNodes;
	}
	
	/**
	 * Inserts a new point in the kd-tree, with no data.
	 * 
	 * @param p
	 *            the point to insert
	 * @return the KdNode3D containing the point
	 */
	public KdNode3D<E> insert(Coordinate p) {
		return insert(p, null);
	}

	/**
	 * Inserts a new point into the kd-tree.
	 * 
	 * @param p
	 *            the point to insert
	 * @param data
	 *            a data item for the point
	 * @return returns a new KdNode3D if a new point is inserted, else an existing
	 *         node is returned with its counter incremented. This can be
	 *         checked by testing returnedNode.getCount() > 1.
	 */
	public KdNode3D<E> insert(Coordinate p, E data) {
		if (root == null) {
			root = new KdNode3D<E>(p, data);
			numberOfNodes++;
			return root;
		}

		KdNode3D<E> currentNode = root;
		KdNode3D<E> leafNode = root;
		int dim = 0;
		boolean isLessThan = true;

		/**
		 * Traverse the tree, first cutting the plane left-right (by X ordinate)
		 * then top-bottom (by Y ordinate), then by Z ordinate
		 */
		while (currentNode != last) {
			// test if point is already a node
			if (currentNode != null) {
				boolean isInTolerance = p.distance3D(currentNode.getCoordinate()) <= tolerance;
				// check if point is already in tree (up to tolerance) and if so
				// simply return existing node
				if (isInTolerance) {
					currentNode.increment();
					return currentNode;
				}
			}

			if (dim == 0) {
				isLessThan = p.x < currentNode.getX();
			} else if (dim == 1) {
				isLessThan = p.y < currentNode.getY();
			} else {
				isLessThan = p.z < currentNode.getZ();
			}
			leafNode = currentNode;
			if (isLessThan) {
				currentNode = currentNode.getLeft();
			} else {
				currentNode = currentNode.getRight();
			}

			dim++;
			if (dim == 3) dim = 0;
		}

		// no node found, add new leaf node to tree
		numberOfNodes++;
		KdNode3D<E> node = new KdNode3D<E>(p, data);
		node.setLeft(last);
		node.setRight(last);
		if (isLessThan) {
			leafNode.setLeft(node);
		} else {
			leafNode.setRight(node);
		}
		return node;
	}

	private void queryNode(KdNode3D<E> currentNode, KdNode3D<E> bottomNode, Envelope3D queryEnv, int dim, List<KdNode3D<E>> result) {
		if (currentNode == bottomNode)
			return;

		double min;
		double max;
		double discriminant;
		
		if (dim == 0) {
			min = queryEnv.getMinX();
			max = queryEnv.getMaxX();
			discriminant = currentNode.getX();
		} else if (dim == 1) {
			min = queryEnv.getMinY();
			max = queryEnv.getMaxY();
			discriminant = currentNode.getY();
		} else {
			min = queryEnv.getMinZ();
			max = queryEnv.getMaxZ();
			discriminant = currentNode.getZ();
		}
		boolean searchLeft = min < discriminant;
		boolean searchRight = discriminant <= max;

		if (searchLeft) {
			queryNode(currentNode.getLeft(), bottomNode, queryEnv, (dim + 1)%3, result);
		}
		if (queryEnv.contains(currentNode.getCoordinate())) {
			result.add(currentNode);
		}
		if (searchRight) {
			queryNode(currentNode.getRight(), bottomNode, queryEnv, (dim + 1)%3, result);
		}

	}

	/**
	 * Performs a range search of the points in the index.
	 * 
	 * @param queryEnv
	 *            the range rectangle to query
	 * @return a list of the KdNode3D found
	 */
	public List<KdNode3D<E>> query(Envelope3D queryEnv) {
		List<KdNode3D<E>> result = new ArrayList<KdNode3D<E>>();
		
		queryNode(root, last, queryEnv, 0, result);
		return result;
	}

	/**
	 * Performs a range search of the points in the index.
	 * 
	 * @param queryEnv
	 *            the range rectangle to query
	 * @param result
	 *            a list to accumulate the result nodes into
	 */
	public void query(Envelope3D queryEnv, List<KdNode3D<E>> result) {
		queryNode(root, last, queryEnv, 0, result);
	}
}