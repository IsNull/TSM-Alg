package mse.alg.ex2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.math.Vector3D;

/**
 * Created by IsNull on 04.10.14.
 */
public final class MathUtil {

    /**
     * Compute normal vectors of plane span by two vectors
     * which is the cross product of two vectors.
     * @param a vector in the plane
     * @param b vector in the plane
     * @return normal vector with positive z-coordinate
     */
    public static Vector3D norm(Vector3D a, Vector3D b) {
        /*
        x = Ay * Bz - By * Az
        y = Az * Bx - Bz * Ax
        z = Ax * By - Bx * Ay
         */
        return new Vector3D(
                a.getY()*b.getZ() - b.getY()*a.getZ(),
                a.getZ()*b.getX() - b.getZ()*a.getX(),
                a.getX()*b.getY() - b.getX()*a.getY());
    }

    /**
     * Computes the squared 3-dimensional Euclidean distance between two locations.
     *
     * @param a a point
     * @param b a point
     * @return the squared 3-dimensional Euclidean distance between two locations
     */
    public static double squareDist(Coordinate a, Coordinate b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double dz = a.z - b.z;

        return dx*dx + dy*dy + dz*dz;
    }

    /**
     * Compute cylindrical projection of point p
     *
     * @param vp view point =  center of cylinder
     * @param p point to be projected
     * @return projected point
     */
    public static Coordinate cylindricalProjection(Coordinate vp, Coordinate p) {
        final double xScale = 1000;		// arbitrary scaling factor
        final double yScale = 10000;	// arbitrary scaling factor: in reality: xScale = yScale
        final double max = Math.PI*xScale - 1.0e-10;

        double dist = vp.distance(p);
        if (dist == 0) return null;

        double x = Math.atan2(vp.y - p.y, vp.x - p.x)*xScale;

        if (x < -max || x > max) {
            return null;
        } else {
            double y = yScale*(p.z - vp.z)/dist;
            return new Coordinate(x, y);
        }
    }
}
