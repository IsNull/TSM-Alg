package  mse.alg.ex2;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.math.Vector3D;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void main(String[] args) throws IOException {
		final Coordinate viewPoint = new Coordinate(683200, 246700, 406 + 1.8); // Bürkliplatz
		
		GeometryFactory factory = new GeometryFactory();
		
		// read terrain x:[677000..687000], y:[242000..257000]
		IO.setPath("\\"); // TODO: set correct directory path
		List<Coordinate> terrainPts = IO.readTerrain("Zurich25m.csv");
		
		// build triangulation
		DelaunayTriangulationBuilder terrainBuilder = new DelaunayTriangulationBuilder();
		
		terrainBuilder.setSites(terrainPts);
		Geometry triangles = terrainBuilder.getTriangles(factory);
		
		// filter front facing triangles
		ArrayList<MonotoneChain> chains = new ArrayList<>(triangles.getNumGeometries()/2);
		// type cast is necessary because of ambiguities in overloaded method apply
		triangles.apply((GeometryFilter)((g) -> {
			if (g instanceof Polygon) {
				Polygon poly = (Polygon)g;
				LineString boundary = poly.getExteriorRing();
				Coordinate p0 = boundary.getCoordinateN(0);
				Coordinate p1 = boundary.getCoordinateN(1);
				Coordinate p2 = boundary.getCoordinateN(2);

				// compute normal of triangle
				Vector3D v12 = new Vector3D(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);
				Vector3D v13 = new Vector3D(p2.x - p0.x, p2.y - p0.y, p2.z - p0.z);
				Vector3D v = new Vector3D(p0.x - viewPoint.x, p0.y - viewPoint.y, p0.z - viewPoint.z).normalize();
				Vector3D norm = MathUtil.norm(v12, v13);

				// test for front- or back-facing triangle
				if (v.dot(norm) < 0) { // cos of angle between vectors is negative
					// front-facing triangle
					MonotoneChain mc = computeUpperBoundary(viewPoint, p0, p1, p2);
					if (mc != null) chains.add(mc);
				}
			}
		}));
		
		// simple test
		/*
		chains.clear();
		Coordinate c1 = new Coordinate(0,5);
		Coordinate c2 = new Coordinate(7,12);
		Coordinate c3 = new Coordinate(14,5);
		Coordinate c4 = new Coordinate(7,0);
		Coordinate c5 = new Coordinate(14,10);
		Coordinate c6 = new Coordinate(21,0);
		Coordinate c7 = new Coordinate(7,5);
		Coordinate c8 = new Coordinate(10,10);
		Coordinate c9 = new Coordinate(14,5);
		chains.add(new MonotoneChain(c1,c2,c3));
		chains.add(new MonotoneChain(c4,c5,c6));
		chains.add(new MonotoneChain(c7,c8,c9));
		*/
		
		// compute horizon 
		Horizon horizon = computeHorizon(chains);
		Geometry geom = horizon.toLineString();
		//System.out.println(geom);

		// simplify the resulting horizon
		//geom = DouglasPeuckerSimplifier.simplify(geom, 5.0);

		// produce output
		IO.writeXML("horizon.xml", geom, null);
	}
	
	/**
	 * Computes horizon of a set of x-monotone chains in O(k + n log n) 
	 * using a divide-and-conquer approach
	 * 
	 * @param chains upper boundaries of projected triangles
	 * @return computed horizon
	 */
	public static Horizon computeHorizon(List<MonotoneChain> chains) {
       return  new HorizonAlgorithm().computeHorizon(chains);
	}
	
	/**
	 * Computes cylindrical projection of the vertices of a triangle and
	 * computes the upper boundary of the projected triangle.
	 * 
	 * @param viewPoint viewpoint = center of cylinder
	 * @param p0 vertex of the triangle
	 * @param p1 vertex of the triangle
	 * @param p2 vertex of the triangle
	 * @return monotone chain or null if no monotone chain is possible
	 */
	public static MonotoneChain computeUpperBoundary(Coordinate viewPoint, Coordinate p0, Coordinate p1, Coordinate p2) {
		Coordinate minX, midX, maxX;
		
		p0 = MathUtil.cylindricalProjection(viewPoint, p0);
		if (p0 != null) {
			minX = maxX = p0;
			p1 = MathUtil.cylindricalProjection(viewPoint, p1);
			if (p1 != null) {
				if (p1.compareTo(minX) < 0) minX = p1;
				if (p1.compareTo(maxX) > 0) maxX = p1;
				p2 = MathUtil.cylindricalProjection(viewPoint, p2);
				if (p2 != null) {
					if (p2.compareTo(minX) < 0) minX = p2;
					if (p2.compareTo(maxX) > 0) maxX = p2;
					if (p0 != minX && p0 != maxX) midX = p0;
					else if (p1 != minX && p1 != maxX) midX = p1;
					else midX = p2;
					
					// compute upper boundary
					int orient = CGAlgorithms.orientationIndex(minX, maxX, midX);
					if (orient == 1) {
						// midX is to the left of (minX, maxX)
						if (minX.x == midX.x) {
							// handles 90� angles correctly
							return new MonotoneChain(midX, maxX);
						} else {
							return new MonotoneChain(minX, midX, maxX);
						}
					} else if (orient == -1 && minX.x < maxX.x) {
						// midX is to the right of (minX, maxX)
						// handles also 90� angles correctly
						return new MonotoneChain(minX, maxX);
					}
				}
			}
		}
		return null;
	}
	

}
