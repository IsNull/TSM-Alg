package mse.alg.ex3.JTS;



/**
 * 
 * @author Christoph Stamm
 *
 */
public class MultiCoordinate3D extends Coordinate {
	private static final long serialVersionUID = 6007711423926179264L;
	
	private int count;
	
	public MultiCoordinate3D(double _x, double _y, double _z) {
		super(_x, _y, _z);
		count = 1;
	}
	
	public MultiCoordinate3D(MultiCoordinate3D p) {
		super(p);
		count = p.count;
	}
	
	public MultiCoordinate3D(Coordinate p) {
		super(p);
		count = 1;
	}
	
	public int getCount() {
		return count;
	}
	
	public void inc() {
		count++;
	}
	
	public void add(int _count) {
		count += _count;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Coordinate)) {
			return false;
		}
		return equals3D((Coordinate)other);
	}
	
	@Override
	public int compareTo(Object o) {
		Coordinate other = (Coordinate)o;

		if (this == other) 
			return 0;
		if (x < other.x)
			return -1;
		if (x > other.x)
			return 1;
		if (y < other.y)
			return -1;
		if (y > other.y)
			return 1;
		if (z < other.z)
			return -1;
		if (z > other.z)
			return 1;
		return 0;
	}

}
