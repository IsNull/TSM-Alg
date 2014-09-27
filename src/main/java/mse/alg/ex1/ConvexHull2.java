package mse.alg.ex1;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.UniqueCoordinateArrayFilter;
import mse.alg.DebugUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Computes the convex hull of a {@link Geometry}. The convex hull is the
 * smallest convex Geometry that contains all the points in the input Geometry.
 * <p>
 * Uses an incremental approach
 *
 * @version 1.0
 */
public class ConvexHull2 {
    private GeometryFactory geomFactory;
    private Coordinate[] inputPts;

    /**
     * Create a new convex hull construction for the input {@link Geometry}.
     */
    public ConvexHull2(Geometry geometry) {
        this(extractCoordinates(geometry), geometry.getFactory());
    }

    /**
     * Create a new convex hull construction for the input {@link Coordinate}
     * array.
     */
    public ConvexHull2(Coordinate[] pts, GeometryFactory geomFactory) {
        inputPts = CoordinateArrays.removeRepeatedPoints(pts);
        this.geomFactory = geomFactory;
    }

    private static Coordinate[] extractCoordinates(Geometry geom) {
        UniqueCoordinateArrayFilter filter = new UniqueCoordinateArrayFilter();
        geom.apply(filter);
        return filter.getCoordinates();
    }

    /**
     * Returns a {@link Geometry} that represents the convex hull of the input
     * geometry. The returned geometry contains the minimal number of points
     * needed to represent the convex hull. In particular, no more than two
     * consecutive points will be collinear.
     *
     * @return if the convex hull contains 3 or more points, a {@link Polygon};
     *         2 points, a {@link LineString}; 1 point, a {@link Point}; 0
     *         points, an empty {@link GeometryCollection}.
     */
    public Geometry getConvexHull()  {
        if (inputPts.length == 0) {
            return geomFactory.createGeometryCollection(null);
        }
        if (inputPts.length == 1) {
            return geomFactory.createPoint(inputPts[0]);
        }
        if (inputPts.length == 2) {
            return geomFactory.createLineString(inputPts);
        }

        // Sort coordinates by X
        sortCoordinatesByX(inputPts);

        System.out.println("Sorted Coordinates by X:");
        DebugUtil.printAll(inputPts);

        try {
            List<Coordinate> upperHull = createUpperHull(inputPts);
            List<Coordinate> lowerHull = createLowerHull(inputPts);
            return createGeometry(mergeHulls(upperHull, lowerHull));
        }catch (RuntimeException e){
            // Show exception stacktrace to make debugging easier
            e.printStackTrace();
            throw e;
        }


    }


    private List<Coordinate> createUpperHull(Coordinate[] sortedPnts){
        List<Coordinate> upperHull = new ArrayList<Coordinate>();

        upperHull.add( sortedPnts[0] );
        upperHull.add( sortedPnts[1] );

        for(int i = 2; i < sortedPnts.length; i++){
            upperHull.add( sortedPnts[i] );
            //System.out.println("Upper Hull Added " +  sortedPnts[i] + " @ " + i);
            reduceConcave(upperHull);
        }

        return upperHull;
    }

    private List<Coordinate> createLowerHull(Coordinate[] sortedPnts){
        List<Coordinate> lowerHull = new ArrayList<Coordinate>();
        int n = sortedPnts.length - 1;

        lowerHull.add( sortedPnts[n] );
        lowerHull.add( sortedPnts[n-1] );

        for(int i = n-2; i >= 0; i--){

            lowerHull.add( sortedPnts[i] );
            //System.out.println("Lower Hull Added " +  sortedPnts[i] + " @ " + i);
            reduceConcave(lowerHull);
        }

        return lowerHull;
    }

    /**
     *
     * @param hull
     */
    private void reduceConcave(List<Coordinate> hull){
        int j = hull.size() - 1;

        while (hull.size() > 2 &&
                isLeftTurn( // Check if the last 3 points are concave
                        hull.get(j-2),
                        hull.get(j-1),
                        hull.get(j))){
            // The hull is currently concave which is bad
            // We remove the second last point.

            //System.out.println("Reducing hull since it is concave { " + hull.get(j-2) + ", " + hull.get(j-1) + ", " + hull.get(j) + " }");
            hull.remove(j-1);
            j = hull.size() - 1;
        }
    }


    /**
     * Checks if the point p3 is on the left side of line p1-p2. (counter clock)
     * @param p1
     * @param p2
     * @param p3
     * @return
     */
    private boolean isLeftTurn(Coordinate p1, Coordinate p2, Coordinate p3){
        int or = CGAlgorithms.computeOrientation(p1, p2, p3);
        //System.out.println("Orientation " + p1 + " " + p2 + " " + p3 + " = " + or);
        return or == -1;
    }


    private Coordinate[] mergeHulls(List<Coordinate> upperHull, List<Coordinate> lowerHull){

        // Remove first point of lower hull, since upper should contain it as-well.
        lowerHull.remove(0);
        upperHull.addAll(lowerHull);

        return upperHull.toArray(new Coordinate[upperHull.size()]);
    }

    private Geometry createGeometry(Coordinate[] hull){
        if (hull.length == 3) {
            return geomFactory.createLineString(new Coordinate[] { hull[0], hull[1] });
        }
        LinearRing linearRing = geomFactory.createLinearRing(hull);
        return geomFactory.createPolygon(linearRing, null);
    }

    /**
     * Sort the coordinates by x
     * @param inputPts
     */
    private void sortCoordinatesByX(Coordinate[] inputPts){
        Arrays.sort(inputPts, new Comparator<Coordinate>() {
            @Override
            public int compare(Coordinate c1, Coordinate c2) {
                if(c1.x == c2.x){
                    return Double.compare(c1.y, c2.y);
                }else {
                    return Double.compare(c1.x, c2.x);
                }
            }
        });
    }





}