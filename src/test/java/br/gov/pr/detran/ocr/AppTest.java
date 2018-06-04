package br.gov.pr.detran.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import com.recognition.software.jdeskew.ImageDeskew;

import br.gov.pr.detran.ocr.filter.MedianFilter;
import br.gov.pr.detran.ocr.utils.ImageUtils;
import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITessAPI.TessPageSegMode;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.PdfUtilities;

public class AppTest {

	public static void main(String[] args) throws Exception {
		
		int countOk = 0;
		int countFail = 0;
		boolean teste = false;
		
		for (Path path: ImageUtils.listFiles()) {
			
//			File[] pngFiles = PdfUtilities.convertPdf2Png(new File("/home/fantini/termos/CX 22 1000/231/0100-assinado.pdf"));
			
			File[] pngFiles = PdfUtilities.convertPdf2Png(path.toFile());
			
			Tesseract instance = new Tesseract();
			
			instance.setDatapath(LoadLibs.extractNativeResources("tessdata").getAbsolutePath());
			instance.setPageSegMode(TessPageSegMode.PSM_SINGLE_LINE);
			instance.setTessVariable("tessedit_char_whitelist", "0123456789.-");
					
			try {
				
				//1. recorta imagem
				BufferedImage _img = ImageUtils.crop(ImageIO.read(pngFiles[0]));
				
				if (teste)
					ImageUtils.saveImage(_img, "teste1.png");
							
				//2. corrige a rotacao da imagem
				_img = ImageUtils.rotate(_img, -new ImageDeskew(_img).getSkewAngle());	
				
				if (teste)
					ImageUtils.saveImage(_img, "teste2.png");				
				
				//3. remove barcode
				_img = ImageUtils.removeBarCode(_img);
				
				if (teste)
					ImageUtils.saveImage(_img, "teste4.png");
				
				Boolean execFilter = false;
				Boolean isCpf = false;
				String result = "";
				
				while (isCpf == false) {
										
					BufferedImage __img = ImageUtils.copy(_img);
					
					//4. reducao de ruidos - filtragem por mediana
					if (execFilter) {
						__img = new MedianFilter(__img, 5).filter();
						
						if (teste)
							ImageUtils.saveImage(__img, "teste3.png");
					}
					
					//5. executa o algoritmo de OCR
				    result = instance.doOCR(__img).replaceAll("\\D", "");
				    
				    if (teste)
						ImageUtils.saveImage(__img, "teste5.png");
				    
				    if (org.apache.commons.lang3.StringUtils.isNotBlank(result))
				    	result = ("00000000000"+result).substring(result.length());
				    
				    //6. valida cpf
				    isCpf = ImageUtils.validaCpfCnpj(result);
				    
				    if (!isCpf && !execFilter) {
				    	execFilter = true;
				    } else {
				    	break;
				    }
			   
				}
			    
			    if (isCpf)
			    	countOk++;
			    else
			    	countFail++;
			    
			    System.out.println(new SimpleDateFormat("HH:mm:ss:SSS").format(new Date())+" -- "+path.toString()+" : "+result+" : "+isCpf+" - OK: "+countOk+" Fail: "+countFail);
			    
			} catch (Exception e) {
				countFail++;
				System.out.println(new SimpleDateFormat("HH:mm:ss:SSS").format(new Date())+" -- "+path.toString()+e.getMessage()+" - OK: "+countOk+" Fail: "+countFail);
			}
		}
		
	}
	
}
