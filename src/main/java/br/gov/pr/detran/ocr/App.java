package br.gov.pr.detran.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.recognition.software.jdeskew.ImageDeskew;

import br.gov.pr.detran.ocr.filter.MedianFilter;
import br.gov.pr.detran.ocr.utils.ImageUtils;
import br.gov.pr.detran.ocr.utils.LogUtils;
import br.gov.pr.detran.ocr.ws.WSDetranFacil;
import br.gov.pr.detran.ocr.ws.dto.TermoDTO;
import br.gov.pr.detran.ocr.ws.dto.UsuarioDTO;
import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITessAPI.TessPageSegMode;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.PdfUtilities;

public class App { 

	private final static Logger LOGGER = Logger.getLogger(App.class.getName());
		
	public static void main(String[] args) throws Exception {
		
		LOGGER.addHandler(LogUtils.fileHandler("fail", "FAIL"));
		LOGGER.addHandler(LogUtils.fileHandler("found", "FOUND"));
		LOGGER.addHandler(LogUtils.fileHandler("notfound", "NOTFOUND"));
		LOGGER.addHandler(LogUtils.fileHandler("error", "ERROR"));
		
		
		LOGGER.info("Detran OCR version 1.1.3");
		
		Tesseract instance = new Tesseract();
		
		instance.setDatapath(LoadLibs.extractNativeResources("tessdata").getAbsolutePath());
		instance.setPageSegMode(TessPageSegMode.PSM_SINGLE_LINE);
		instance.setTessVariable("tessedit_char_whitelist", "0123456789.-");	
				
		TermoDTO termo = null;
		
		int countFail = 0;
		int countNotFound = 0;
		int countFound = 0;
		int countError = 0;
				
		WSDetranFacil ws = WSDetranFacil.getInstance();
		
		while((termo = ws.obterFromTemp()) != null) {
			
			try {
				
				LOGGER.info(termo.getNome()+": processando...");
				
				//0. converte pdf para png
				File[] pngFiles = PdfUtilities.convertPdf2Png(Files.write(Paths.get("file.pdf"), termo.getArquivo()).toFile());			
				
				//1. recorta imagem
				BufferedImage _img = ImageUtils.crop(ImageIO.read(pngFiles[0]));				
							
				//2. corrige a rotacao da imagem
				_img = ImageUtils.rotate(_img, -new ImageDeskew(_img).getSkewAngle());									
								
				//3. remove barcode
				_img = ImageUtils.removeBarCode(_img);
			    
			    Boolean execFilter = false;
				Boolean isCpf = false;
				String cpf = "";
				
				while (isCpf == false) {
										
					BufferedImage __img = ImageUtils.copy(_img);
					
					//4. reducao de ruidos - filtragem por mediana
					if (execFilter) {
						__img = new MedianFilter(__img, 5).filter();			
					}
					
					//5. executa o algoritmo de OCR
				    cpf = instance.doOCR(__img).replaceAll("\\D", "");
				    cpf = cpf != null && cpf.trim().length() > 0 ? 
					    	("00000000000"+cpf).substring(cpf.length()) : cpf;
				    
				    //6. valida cpf
				    isCpf = ImageUtils.validaCpfCnpj(cpf);
				    
				    if (!isCpf && !execFilter) {
				    	execFilter = true;
				    } else {
				    	break;
				    }
			   
				}			    
			    
			    //7. consulta usuario
			    if (isCpf) {
			    	
			    	LOGGER.info("["+cpf+"] "+termo.getNome()+": cpf identificado");
			    	
			    	UsuarioDTO usuario = ws.obterUsuario(Long.valueOf(cpf));			    	
			    	
			    	if (usuario != null) {
			    		
			    		LOGGER.log(Level.INFO, "["+cpf+":"+usuario.getId()+"] "+termo.getNome()+": usuario encontrado", "FOUND");
			    		
			    		ws.move(Long.valueOf(cpf), termo.getNome());
			    		
			    		countFound++;		    		
			    		
			    	} else {
			    		
			    		LOGGER.log(Level.WARNING, "["+cpf+"] "+termo.getNome()+": usuario nao encontrado", "NOTFOUND");
			    		
			    		ws.moveFromTempToNaoEncontrados(termo.getNome());
			    		
			    		countNotFound++;
			    	}
			   
			    } else {
			    	
			    	LOGGER.log(Level.WARNING, "["+cpf+"] "+termo.getNome()+": cpf nao identificado", "FAIL");
			    	
			    	ws.moveFromTempToNaoIdentificadoViaOCR(termo.getNome());
			    	
			    	countFail++;
			    }
			    
			} catch (Exception e) {
								
				LOGGER.log(Level.SEVERE, termo.getNome()+": "+e.getMessage(), "ERROR");
				
				ws.moveFromTempToNaoIdentificadoViaOCR(termo.getNome());
				
				countError++;
			}
		}
		
		LOGGER.info("processamento concluido");
		LOGGER.info("termos processados: "+(countFound+countNotFound+countFail+countError));
		LOGGER.info("termos registrados: "+countFound);
		LOGGER.info("termos nao registrados : "+countNotFound);
		LOGGER.info("termos nao identificados: "+countFail);
		LOGGER.info("termos com falha no processamento: "+countError);
		
	}
	
}
