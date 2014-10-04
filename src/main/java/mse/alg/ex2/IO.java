package  mse.alg.ex2;

import com.vividsolutions.jts.geom.Coordinate;



import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class IO {
	static String s_path = "";

	/**
	 * Set path (excluding file name, but including trailing slash)
	 * @param path
	 */
	public static void setPath(String path) {
		s_path = path;
	}
	
	/**
	 * Read list of terrain points in csv format: x-coord;y-coord;z-coord.
	 * 
	 * @param fileName
	 * @return new created ArrayList of Coordinate instances
	 * @throws IOException
	 */
	static List<Coordinate> readTerrain(String fileName) throws IOException {
		ArrayList<Coordinate> terrain = new ArrayList<Coordinate>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(s_path + fileName));
			reader.lines().forEach((s) -> {
				String[] parts = s.split(";");
				terrain.add(new Coordinate(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2])));
			});
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();;
		}
	
		return terrain;
	}
	
	/**
	 * Writes one or two geometries (geometry collections) into a xml file. 
	 * The file can be read and the content can be visualized by the JTS TestBuilder.
	 * 
	 * @param fileName
	 * @param geomA geometry A
	 * @param geomB geometry B
	 * @throws FileNotFoundException
	 */
	static void writeXML(String fileName, Geometry geomA, Geometry geomB) throws FileNotFoundException {
		final String prolog = "<run><case><desc>" + fileName + "</desc>";
		final String epilog = "</case></run>";
		
		WKTWriter writer = new WKTWriter();
		PrintStream ps = new PrintStream(s_path + fileName);
		
		ps.println(prolog);
		ps.println("<a>" + writer.writeFormatted(geomA) + "</a>");
		if (geomB != null) ps.println("<b>" + writer.writeFormatted(geomB) + "</b>");
		ps.println(epilog);
		ps.close();
	}

}
