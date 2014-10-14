package mse.alg.ex3.PicsiSWT.imageprocessing;


import mse.alg.ex3.PicsiSWT.main.PicsiSWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

public class Inverter implements IImageProcessor {

	@Override
	public boolean isEnabled(int imageType) {
		return imageType != PicsiSWT.IMAGE_TYPE_INDEXED;
	}

	@Override
	public Image run(Image input, int imageType) {
		ImageData inData = input.getImageData();
		/*
		for(int v=0; v < inData.height; v++) {
			for (int u=0; u < inData.width; u++) {
				int pixel = inData.getPixel(u,v);
				RGB rgb = inData.palette.getRGB(pixel);
				rgb.red = 255 - rgb.red;
				rgb.green = 255 - rgb.green;
				rgb.blue = 255 - rgb.blue;
				inData.setPixel(u, v, inData.palette.getPixel(rgb));
			}
		}
		*/
		
		byte[] data = inData.data;
		
		for(int i=0; i < data.length; i++) {
			data[i] = (byte)~data[i];
		}

		return new Image(input.getDevice(), inData);
	}

}
