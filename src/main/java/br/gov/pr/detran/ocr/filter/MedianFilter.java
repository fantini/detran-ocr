package br.gov.pr.detran.ocr.filter;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class MedianFilter implements Filter {
	
	private BufferedImage image;
	private Integer size;
	
	public MedianFilter(BufferedImage image, Integer size) {
		this.image = image;
		this.size = size;
	}
	
	public BufferedImage filter() {
		
		BufferedImage _image = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		
		int height = image.getHeight();
        int width = image.getWidth();
        
        int[] a;
        
        for (int k = 0; k < height; k++){
            for (int j = 0; j < width; j++) {
                
            	a = getArray(image, j, k);
                
            	int[] red, green, blue;
                
            	red = new int[a.length];
                green = new int[a.length];
                blue = new int[a.length];
                
                for (int i = 0; i < a.length; i++) {                	
                    red[i] = new Color(a[i]).getRed();
                    green[i] = new Color(a[i]).getGreen();
                    blue[i] = new Color(a[i]).getBlue();
                }

                _image.setRGB(j, k, new Color(median(red),median(green),median(blue)).getRGB());
            }
        }
		
		return _image;
	}
	 
    
    public int median(int[] a) {
        int temp;
        int asize = a.length;
        //sort the array in increasing order
        for (int i = 0; i < asize ; i++) {
            for (int j = i+1; j < asize; j++) {
                if (a[i] > a[j]) {
                    temp = a[i];
                    a[i] = a[j];
                    a[j] = temp;
                }
            }
        }
        
        if (asize%2 == 1)
            return a[asize/2];
        else
            return ((a[asize/2]+a[asize/2 - 1])/2);
    }
    
    public int[] getArray(BufferedImage image, int x, int y){
        int[] n; //store the pixel values of position(x, y) and its neighbors
        int h = image.getHeight();
        int w = image.getWidth();
        int xmin, xmax, ymin, ymax; //the limits of the part of the image on which the filter operate on
        xmin = x - size/2;
        xmax = x + size/2;
        ymin = y - size/2;
        ymax = y + size/2;
        
        //special edge cases
        if (xmin < 0)
            xmin = 0;
        if (xmax > (w - 1))
            xmax = w - 1;
        if (ymin < 0)
            ymin = 0;
        if (ymax > (h - 1))
            ymax = h - 1;
        //the actual number of pixels to be considered
        int nsize = (xmax-xmin+1)*(ymax-ymin+1);
        n = new int[nsize];
        int k = 0;
        for (int i = xmin; i <= xmax; i++)
            for (int j = ymin; j <= ymax; j++){
                n[k] = image.getRGB(i, j); //get pixel value
                k++;
            }
        return n;
    }
}
