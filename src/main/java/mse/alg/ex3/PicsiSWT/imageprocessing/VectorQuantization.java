package mse.alg.ex3.PicsiSWT.imageprocessing;


import mse.alg.ex3.JTS.*;
import mse.alg.ex3.PicsiSWT.main.PicsiSWT;
import mse.alg.ex3.PicsiSWT.utils.Parallel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import javax.swing.*;
import java.util.*;


/**
 * MSE Algorithms: Exercise 4 Template
 * @author Christoph Stamm
 *
 */
public class VectorQuantization implements IImageProcessor {
	/**
	 * Statistical data for a palette color
	 */
	private static class CenterData {
		int m_count; // number of points
		double m_cumX, m_cumY, m_cumZ; // cumulated x,y,z values of count points

        /**
         * Cumulates the RGB values of the given node to the overall sum.
         * @param node
         */
		public void cumulate(KdNode3D<NodeData> node) {
			assert node != null : "node is null";
			
			// get multiplicity
			int count = node.getCount();
			Coordinate coord = node.getCoordinate();
			
			m_count += count;
			m_cumX += count*coord.x;
			m_cumY += count*coord.y;
			m_cumZ += count*coord.z;
		}

        /**
         * Cumulates the RGB values of the given CenterData to this CenterData.
         * @param cd
         */
		public void cumulateSubtree(CenterData cd) {
			assert cd != null : "cd is null";
			
			m_count += cd.m_count;
			m_cumX += cd.m_cumX;
			m_cumY += cd.m_cumY;
			m_cumZ += cd.m_cumZ;
		}
		
		public double meanX() {
			return m_cumX/m_count;
		}
		
		public double meanY() {
			return m_cumY/m_count;
		}
		
		public double meanZ() {
			return m_cumZ/m_count;
		}

        /**
         * Resets all the RGB values
         */
		public void reset() {
			m_count = 0;
			m_cumX = m_cumY = m_cumZ = 0;
		}
	}
	
	/**
	 * Statistical data in a tree node
	 */
	private static class NodeData extends CenterData {
		Envelope3D m_cell; // Kd-tree region of a node
		
		public NodeData(KdNode3D<NodeData> node) {
			cumulate(node);
			m_cell = new Envelope3D(node.getCoordinate());
		}
		
		public void cumulateSubtree(NodeData data) {
			super.cumulateSubtree(data);
			m_cell.expandToInclude(data.m_cell);
		}
	}

	
	
	
	@Override
	public boolean isEnabled(int imageType) {
		return imageType == PicsiSWT.IMAGE_TYPE_RGB;
	}

	@Override
	public Image run(Image input, int imageType) {
		// let the user choose the interpolation method
		Object[] methods = { "Most Frequent Colors", "Random Colors", "k-Means", "k-Means with kd-Tree" };
		int m = JOptionPane.showOptionDialog(null, "Choose the vector quantization method", "Convert to Indexed Color", 
				JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, methods, methods[3]);
		if (m == -1) return null;

		// create palette and output image data
		ImageData inData = input.getImageData();
		final int size = inData.width*inData.height;
		
		// compute correct palette size
		int nBits = 8;
		int n = 1 << nBits;
		
		if (size < n) {
			nBits /= 2;
			while(nBits >= 2 && (1 << nBits) > size) {
				nBits /= 2;
			}
			n = 1 << nBits;
			if (size > n) {
				nBits *= 2;
				n = 1 << nBits;
			}
		}

		// allocate palette
		RGB[] pal = new RGB[n];
		
		if (size <= n) {
			// fill in palette with all image colors
			for(int i=0; i < size; i++) {
				pal[i] = inData.palette.getRGB(inData.getPixel(i%inData.width, i/inData.width));
			}
			// fil in rest with dummy colors
			for(int i=size; i < n; i++) {
				pal[i] = pal[size - 1];
			}
		} else {
			if (m == 0) {
				// choose most frequently used colors
				List<MultiCoordinate3D> colors = distinctColors(inData);
				
				colors.sort((c1, c2) -> { 
					int cnt1 = c1.getCount(), cnt2 = c2.getCount();
					if (cnt1 > cnt2) return -1;
					if (cnt1 < cnt2) return 1;
					return 0;
				});
				for (int i = 0; i < n; i++) {
					pal[i] = coordToRGB(colors.get(i));
				}
			} else {
                // Initialize Coordinate Array which will represent all the Pixels of the Image.
                // Each Pixel will be represented as a coordinate by which the x,y,z values are the rgb values.
				Coordinate[] palColors = new Coordinate[n];
				
				// choose randomly n center colors
				Random rand = new Random();
						
				for(int i=0; i < n; i++) {
					int r = rand.nextInt(size);
					palColors[i] = rgbToCoord(inData.palette.getRGB(inData.getPixel(r%inData.width, r/inData.width)));
				}
				
				// improve palette with k-means algorithm
				if (m == 2) kMeans(inData, palColors);
				else if (m == 3) filteringAlgorithm(inData, palColors);

				// store new palette 
				for (int i=0; i < n; i++) {
					pal[i] = coordToRGB(palColors[i]);
				}
			}
		}
		
		// applying new pixel colors using the palette and compute distortion
		PaletteData pd = new PaletteData(pal);
		ImageData outData = new ImageData(inData.width, inData.height, nBits, pd);
		int dist = 0;

		for (int v=0; v < inData.height; v++) {
			for (int u=0; u < inData.width; u++) {
				RGB rgb = inData.palette.getRGB(inData.getPixel(u, v));
				int pixelValue = ImageProcessing.nearestPaletteColor(pd, rgb);
				dist += ImageProcessing.colorSquareDist(rgb, pd.colors[pixelValue]); // just for testing
				outData.setPixel(u, v, pixelValue);
			}
		}
		
		// show distortion
		System.out.println("distortion: " + dist/size);
		
		return new Image(input.getDevice(), outData);
	}

	/**
	 * Standard k-Means algorithm
	 * @param inData input image
	 * @param palColors palette colors, represent the chosen centroids
	 */
	private void kMeans(ImageData inData, Coordinate[] palColors) {
        @SuppressWarnings("unchecked")
		List<RGB>[] assoc = new List[palColors.length]; // all pixels of the same nearest palette color
		int maxIterations = 100; // in case of no convergence
		boolean changing = true;
		
		// create association list
		for(int i=0; i < assoc.length; i++) assoc[i] = new LinkedList<>();
		
		// fill in the association list
		for (int v=0; v < inData.height; v++) {
			for (int u=0; u < inData.width; u++) {
				RGB rgb = inData.palette.getRGB(inData.getPixel(u, v));
				int index = nearestPaletteColor(palColors, rgb);
				assoc[index].add(rgb);
			}
		}
		
		// stop iterative process if the associations don't change anymore
		while(changing) {

            //Stop if the centroids doesn't change anymore
            changing = false;

			// compute new palette colors
			// TODO done

            // Iterate through the whole List of LinkedList. Each LinkedList represents a cluster which need to be generalized
            // by one color. We therefore need to calculate the average color value.
            for(int i=0; i<assoc.length; i++){

                // Initialize r,g,b sum variables which will contain the total amount of
                // red, green and blue percentage of all points within a cluster.
                double tmpR = 0;
                double tmpG = 0;
                double tmpB = 0;

                // For each pixel/point we take the r,g,b values and add them to the corresponding variable.
                double counter = 0;
                for(RGB rgb : assoc[i]){
                    tmpR += rgb.red;
                    tmpB += rgb.blue;
                    tmpG += rgb.green;

                    counter++;
                }

                // Clear the points of the cluster, since we will add them again with new r,g,b values later below.
                assoc[i].clear();

                // Set the new centroid to the color palette by calculating the average color of the cluster.
                if(counter >0){
                    //Check if the centroid has changed
                    if(palColors[i].compareTo(new Coordinate(tmpR/counter, tmpG/counter, tmpB/counter)) != 0){
                        palColors[i].setCoordinate(new Coordinate(tmpR / counter, tmpG / counter, tmpB / counter));
                        changing = true;
                    }
                }else {
                    System.out.println("LinkedList in Assoc Array at position: " + i + " was empty!");
                }
            }
			
			// compute new associations
			// TODO done

            // fill in the association list
            for (int v=0; v < inData.height; v++) {
                for (int u=0; u < inData.width; u++) {
                    RGB rgb = inData.palette.getRGB(inData.getPixel(u, v));
                    int index = nearestPaletteColor(palColors, rgb);
                    assoc[index].add(rgb);
                }
            }
			
			// prevent endless looping in never divergent cases
			maxIterations--; 
			if (maxIterations == 0) changing = false;
			
			// compute distortion (just for testing and watching the improvements)
			System.out.println("distortion: " + distortion(inData, palColors));
		}
	}
	
	/**
	 * Improved k-means algorithm (Filtering algorithm)
	 * @param inData input image
	 * @param palColors palette colors
	 */
	private void filteringAlgorithm(ImageData inData, Coordinate[] palColors) {
		final int len = palColors.length;
		final int size = inData.width*inData.height;
		
		int maxIterations = 100; // in case of no convergence
		boolean changing = true;
		KdTree3D<NodeData> kdTree = new KdTree3D<>();
		CenterData[] counts = new CenterData[len];	// center data
			
		for(int i=0; i < len; i++) {
			counts[i] = new CenterData();
		}
		
		System.out.println("Remove duplicates");
		// remove duplicated colors and build point list used to build the kd-tree
		// build homogeneous, balanced kd-tree
		System.out.println("Build kd-Tree");
		KdNode3D<NodeData> root = kdTree.buildTree(distinctColors(inData),0);
		
		System.out.println("Image Size: " + size);
		//System.out.println("Multi-Point Size: " + points.size());
		System.out.println("Tree Size: " + kdTree.size());

		// create cells
		cumulateCells(root);
		
		// initialize candidates
		HashSet<Integer> cand = new HashSet<>(len);
		for(int i=0; i < len; i++) cand.add(i);
		
		while(changing) {
			changing = false;
						
			// filter candidates
			filter(root, cand, palColors, counts);
			
			// compute new palette colors
			for(int i=0; i < len; i++) {
				int cnt = counts[i].m_count;
				
				if (cnt > 0) {
					double val = counts[i].meanX();
					if (val != palColors[i].x) {
						changing = true;
						palColors[i].x = val;
					}
					val = counts[i].meanY();
					if (val != palColors[i].y) {
						changing = true;
						palColors[i].y = val;
					}
					val = counts[i].meanZ();
					if (val != palColors[i].z) {
						changing = true;
						palColors[i].z = val;
					}
				}	
				// reset counts
				counts[i].reset();
			}
			
			// compute distortion
			//System.out.println("distortion: " + distortion(inData, palColors));
			
			// prevent endless looping in never divergent cases
			maxIterations--; 
			if (maxIterations == 0) changing = false;
		}
	}
	
	/**
	 * Produce a list of all colors in the given image.
	 * @param inData input image
	 * @return ArrayList with all colors and their multiplicity 
	 */
	private List<MultiCoordinate3D> distinctColors(ImageData inData) {
		// we use a Map, because Java doesn't support get in Set		
		// parallel version with reduction
		TreeMap<MultiCoordinate3D, MultiCoordinate3D> points = new TreeMap<>();

		Parallel.For(0, inData.height, points,
                // Creator
                () -> new TreeMap<MultiCoordinate3D, MultiCoordinate3D>(),
                // Loop
                (v, pnts) -> {
                    for (int u = 0; u < inData.width; u++) {
                        RGB rgb = inData.palette.getRGB(inData.getPixel(u, v));
                        MultiCoordinate3D mc = new MultiCoordinate3D(rgb.red, rgb.green, rgb.blue);
                        MultiCoordinate3D mcInTree = pnts.get(mc);
                        if (mcInTree != null && mcInTree.equals3D(mc)) {
                            mcInTree.inc();
                        } else {
                            pnts.put(mc, mc);
                        }
                    }
                },
                // Reduction
                (result, pnts) -> {
                    for (MultiCoordinate3D mc : pnts.keySet()) {
                        MultiCoordinate3D mcR = result.get(mc);
                        if (mcR == null) {
                            result.put(mc, mc);
                        } else {
                            mcR.add(mc.getCount());
                        }
                    }
                }
        );
		assert points.size() > 0 && points.size() <= inData.width*inData.height : "wrong points size: " + points.size();		
		
		return new ArrayList<>(points.keySet());
	}
	
	/**
	 * MSE: mean square error
	 * @param inData original colors
	 * @param palColors computed palette colors
	 * @return MSE
	 */
	private double distortion(ImageData inData, Coordinate[] palColors) {
		double dist = 0;
		
		for (int v=0; v < inData.height; v++) {
			for (int u=0; u < inData.width; u++) {
				RGB rgb = inData.palette.getRGB(inData.getPixel(u, v));
				int pixelValue = nearestPaletteColor(palColors, rgb);
				dist += colorSquareDist(rgb, palColors[pixelValue]);
			}
		}
		return dist/(inData.width*inData.height);
	}
	
	/**
	 * Recursively creates kd-tree node data and computes statistical data per node
	 * 
	 * @param node kd-tree node
	 */
	private void cumulateCells(KdNode3D<NodeData> node) {
		NodeData data = new NodeData(node);
		node.setData(data);
		
		if (!node.isLeaf()) {
			KdNode3D<NodeData> left = node.getLeft();
			KdNode3D<NodeData> right = node.getRight();
			
			if (left != null) {
				cumulateCells(left);
				data.cumulateSubtree(left.getData());
			}
			if (right != null) {
				cumulateCells(right);
				data.cumulateSubtree(right.getData());
			}
		}
	}
	
	/**
	 * Recursive filter algorithm
	 * 
	 * @param node kd-tree node
	 * @param cand Set of possible palette colors = candidates
	 * @param palColors Current palette colors
	 * @param counts Statistical data per palette color
	 */
	@SuppressWarnings("unchecked")
	private void filter(KdNode3D<NodeData> node, HashSet<Integer> cand, Coordinate[] palColors, CenterData[] counts) {
		if (node == null) return;

		assert cand.size() > 0 : "no candidates: " + cand.size();
		
		NodeData data = node.getData();
		
		if (node.isLeaf()) {
			// find closest candidate to the stored color in this leaf
			int bestIndex = nearestPaletteColor(cand, palColors, node.getCoordinate());
			CenterData cd = counts[bestIndex];
			cd.cumulate(node);
		} else {			
			// find closest candidate to center (s_rgb)
			int bestIndex = nearestPaletteColor(cand, palColors, data.m_cell.centre());
			Coordinate c1 = palColors[bestIndex];
			
			// copy and filter candidates
			cand = (HashSet<Integer>)cand.clone();
			Iterator<Integer> it = cand.iterator();
			
			while(it.hasNext()) {
				int index = it.next();
				
				if (index != bestIndex) {
					if (farther(data.m_cell, c1, palColors[index])) {
						// prune index from candidates
						it.remove(); // most relevant instruction: without this instruction it also works but it lasts much longer
					}
				}
			}
			
			// update metrics
			if (cand.size() == 1) {
				CenterData cd = counts[bestIndex];
				cd.cumulateSubtree(data);
			} else {
				// find closest candidate to the stored color in this node
				bestIndex = nearestPaletteColor(cand, palColors, node.getCoordinate());
				CenterData cd = counts[bestIndex];
				cd.cumulate(node);
				
				int candSize = cand.size();
				//System.out.println("cand size: " + candSize);
				filter(node.getLeft(), cand, palColors, counts);
				assert candSize == cand.size() : "wrong cand size: " + candSize + ", " + cand.size();
				filter(node.getRight(), cand, palColors, counts);
				assert candSize == cand.size() : "wrong cand size: " + candSize + ", " + cand.size();
			}
		}
	}
	
	/**
	 * Checks if point c2 is farther from cell than point c1
	 * A cell is the bounding box of a point set. In this case it is a hyper-rectangle.
	 * @param cell
	 * @param c1
	 * @param c2
	 * @return true if point c2 is farther from cell than point c1
	 */
	private boolean farther(Envelope3D cell, Coordinate c1, Coordinate c2) {
		// TODO done

        // Calculate the connecting vector between c1 and c2.
        double tmpX = c2.x - c1.x;
        double tmpY = c2.y - c1.y;
        double tmpZ = c2.z - c1.z;

        // Initialize the coordinate values of the point vH which is the crucial point to check its distance.
        // If this point is closer to c1 then all of the points in the cell will be closer to c1. The same goes for the case
        // when the point is closer to c2.
        double vhX = 0;
        double vhY = 0;
        double vhZ = 0;

        // Below we check the x,y,z components of the connecting vector if they are negative. If so we need to take the minimum x,y,z value
        // of all points within the cell respectively. Check FilteringAlgorithm.pdf section 2 below fig 1 for more detail.
        if(tmpX < 0){
            vhX = cell.getMinX();
        }else{
            vhX = cell.getMaxX();
        }

        if(tmpY < 0){
            vhY = cell.getMinY();
        }else{
            vhY = cell.getMaxY();
        }

        if(tmpZ < 0){
            vhZ = cell.getMinZ();
        }else{
            vhZ = cell.getMaxZ();
        }

        // Create point vH which is the only point we need to check its distance to c1 and c2.
        Coordinate vh = new Coordinate(vhX, vhY, vhZ);

        // Check the distances.
        double distanceC1 = vh.distance(c1);
        double distanceC2 = vh.distance(c2);

        // If the distance from vH to c2 is bigger we return true, which means the cell is farther away from c2 as from c1.
        return distanceC2 > distanceC1;
	}
	
	static private RGB coordToRGB(Coordinate coord) {
		return new RGB((int)(coord.x + 0.5), (int)(coord.y + 0.5), (int)(coord.z + 0.5));
	}

	static private Coordinate rgbToCoord(RGB rgb) {
		return new Coordinate(rgb.red, rgb.green, rgb.blue);
	}
	
	/**
	 * Compute square distance between two colors
	 * @param c1 color 1
	 * @param c2 color 2
	 * @return square distance
	 */
	static private double colorSquareDist(Coordinate c1, Coordinate c2) {
		return colorSquareDist(c1.x, c1.y, c1.z, c2.x, c2.y, c2.z);
	}
	static private double colorSquareDist(RGB c1, Coordinate c2) {
		return colorSquareDist(c1.red, c1.green, c1.blue, c2.x, c2.y, c2.z);
	}
	static private double colorSquareDist(double c1x, double c1y, double c1z, double c2x, double c2y, double c2z) {
		double dr = c1x - c2x;
		double dg = c1y - c2y;
		double db = c1z - c2z;
		
		double dist = dr*dr + dg*dg + db*db;
		assert dist >= 0 : "wrong distance: " + dist;
		return dist;
	}
	
	/**
	 * Search among the candidates the most similar color in a given palette (using square distance metric)
	 * 
	 * @param candidates candidates
	 * @param palColors palette colors
	 * @param rgb given color
	 * @return most similar color to given color
	 */
	static private int nearestPaletteColor(Set<Integer> candidates, Coordinate[] palColors, Coordinate rgb) {
		double min = Double.MAX_VALUE;
		int bestIndex = -1;
		
		for(int index: candidates) {
			double d = colorSquareDist(rgb, palColors[index]);
			if (d < min) {
				min = d;
				bestIndex = index;
			}
		}
		assert bestIndex >= 0 && bestIndex < palColors.length : "wrong bestIndex: " + bestIndex;
		return bestIndex;
	}
	
	/**
	 * Search the most similar color in a given palette (using square distance metric)
	 * @param palColors palette
	 * @param rgb given color
	 * @return most similar color to given color
	 */
	static private int nearestPaletteColor(Coordinate[] palColors, RGB rgb) {
		int min = -1;
		double bestDiff = Double.MAX_VALUE;
		
		for (int i=0; i < palColors.length; i++) {
			Coordinate c = palColors[i];
			assert c != null : "invalid palette at pos " + i;
			double diff = colorSquareDist(rgb, c);
			if (diff < bestDiff) {
				min = i;
				bestDiff = diff;
			}
		}
		assert min >= 0 && min < palColors.length : "wrong minimum: " + min;
		return min;
	}
}
