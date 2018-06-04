package br.gov.pr.detran.ocr.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageUtils {
	
	public static List<Path> listFiles() throws Exception {
		
		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.pdf");
		
		Path dir = Paths.get("/termos");
		
		final List<Path> termos = new ArrayList<Path>();
		
		FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {

				if (matcher.matches(file.getFileName())) {
					termos.add(file);
				}
				
				return FileVisitResult.CONTINUE;
			}
			
		};
		
		Files.walkFileTree(dir, matcherVisitor);
				
		return termos;
	}
	
	public static BufferedImage removeBarCode(BufferedImage image) {
		
		int left = 0;
		int right = 0;
		int count = 0;
		
		for (int y = 0; y < image.getHeight(); y++) {
		    for (int x = 0; x < image.getWidth(); x++) {
		    	if (image.getRGB(x, y) != -1) {
		    		if (left == 0)
		    			left = x;
		    		right = x;
		    	}	
		    }
		    
//		    if (right - left < 280 && right - left > 230) {
		    if (right - left < 280 && right - left > 180) {
		    	if (++count > 5)
		    		return image.getSubimage(left-15, y-6, 300, 30);
		    } else {
		    	count = 0;
		    }
		    
		    left = 0;
		    right = 0;
		}
		
		return image;
		
	}
	
	public static  BufferedImage resize(BufferedImage image, int width, int height) {
		
		int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
		
		BufferedImage resizedImage = new BufferedImage(width, height,type);
		
		Graphics2D g = resizedImage.createGraphics();
		
		g.drawImage(image, 0, 0, width, height, null);
		
		g.dispose();
		
		return resizedImage;
    }
	
	public static BufferedImage crop(BufferedImage src) {
		//return src.getSubimage(1450, 550, 950, 330); - 8.7%
		//return src.getSubimage(1450, 560, 950, 330);
		return src.getSubimage(1600, 560, 630, 330); 
	}
	
	public static BufferedImage rotate(BufferedImage img, Double ang) {

		BufferedImage rotate = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		Graphics2D graphic = rotate.createGraphics();
		graphic.setPaint(Color.WHITE);
		graphic.fillRect(0, 0, rotate.getWidth(), rotate.getHeight());
				
		AffineTransform tx = AffineTransform.getRotateInstance(
				Math.toRadians(ang), rotate.getWidth() / 2, rotate.getHeight() / 2);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

		rotate.createGraphics().drawImage(op.filter(img, null), 0, 0, null);
		
		return rotate;
	}
	
	public static boolean validaCpfCnpj (String cpfCnpj)  throws Exception {

		if (cpfCnpj.length() == 11 )
	    {
			int     d1, d2;
			int     digito1, digito2, resto; 
	        int     digitoCPF; 
	        String  digitoResultado; 
	        d1 = d2 = 0; 
	        digito1 = digito2 = resto = 0; 
	        
	        for (int cont = 1; cont < cpfCnpj.length() -1; cont++) 
	        {
	        	digitoCPF = Integer.valueOf (cpfCnpj.substring(cont -1, cont)).intValue(); 
	            d1 = d1 + ( 11 - cont ) * digitoCPF; 
	            d2 = d2 + ( 12 - cont ) * digitoCPF;
	        }
	        
	        resto = (d1 % 11);
	        
	        if (resto < 2)
	        	digito1 = 0;
	        else
	        	digito1 = 11 - resto;
	        
	        d2 += 2 * digito1;
	        resto = (d2 % 11);
	        
	        if (resto < 2)
	        	digito2 = 0;
	        else
	        	digito2 = 11 - resto;
	        
	        String digitoCpf = cpfCnpj.substring (cpfCnpj.length()-2, cpfCnpj.length());
	        
	        digitoResultado = String.valueOf(digito1) + String.valueOf(digito2);
	        
	        return digitoCpf.equals(digitoResultado); 
	    }
		
		else if (cpfCnpj.length() == 14)
		{
			int soma = 0, dig; 
	        String cnpjCalc = cpfCnpj.substring(0,12); 
	        char[] cnpj = cpfCnpj.toCharArray(); 
            
	        for( int i = 0; i < 4; i++ )
            	if ( cnpj[i]-48 >=0 && cnpj[i]-48 <=9 )
            		soma += (cnpj[i] - 48) * (6 - (i + 1));
            
            for( int i = 0; i < 8; i++ )
            	if ( cnpj[i+4]-48 >=0 && cnpj[i+4]-48 <=9 )
            		soma += (cnpj[i+4] - 48) * (10 - (i + 1)); 
	            
            dig = 11 - (soma % 11);
            cnpjCalc += ( dig == 10 || dig == 11 ) ? "0" : Integer.toString(dig); 

            soma = 0; 
	        
            for ( int i = 0; i < 5; i++ )
            	if ( cnpj[i]-48 >=0 && cnpj[i]-48 <=9 ) 
	                soma += (cnpj[i] - 48) * (7 - (i + 1)); 
	            
            for ( int i = 0; i < 8; i++ ) 
	            if ( cnpj[i+5]-48 >=0 && cnpj[i+5]-48 <=9 ) 
	                soma += (cnpj[i+5] - 48) * (10 - (i + 1)); 
	            
            dig = 11 - (soma % 11);
            cnpjCalc += ( dig == 10 || dig == 11 ) ? "0" : Integer.toString(dig);
            
            return cpfCnpj.equals(cnpjCalc); 
		}
		
		return false;
	}
	
	public static BufferedImage copy(BufferedImage source){
	    BufferedImage _source = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
	    Graphics2D g = _source.createGraphics();
	    g.drawImage(source, 0, 0, null);
	    g.dispose();
	    return _source;
	}
	
	public static void saveImage(BufferedImage image, String fileName) throws Exception {
		Path file = Paths.get(fileName);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( image, "png", baos );
		baos.flush();
		byte[] imageInByte = baos.toByteArray();
		baos.close();
		Files.write(file, imageInByte);
	}
	
}
