package mse.alg;

import com.vividsolutions.jts.geom.Coordinate;


public final class DebugUtil {

    public static void printAll(Coordinate[] inputPts) {
        System.out.print("{ ");
        for(Coordinate c : inputPts){
            System.out.print(c.toString() + ", ");
        }
        System.out.println(" }");
    }

    public static void printAll(Iterable<Coordinate> inputPts) {
        System.out.print("{ ");
        for(Coordinate c : inputPts){
            System.out.print(c.toString() + ", ");
        }
        System.out.println(" }");
    }

}