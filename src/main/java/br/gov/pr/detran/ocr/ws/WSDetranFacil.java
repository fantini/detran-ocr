package br.gov.pr.detran.ocr.ws;

import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.NameValuePair;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;

import br.gov.pr.celepar.ws.rest.security.consumer.connection.HttpConnection;
import br.gov.pr.detran.ocr.ws.dto.TermoDTO;
import br.gov.pr.detran.ocr.ws.dto.UsuarioDTO;

public class WSDetranFacil {
	
	private final Logger LOGGER = Logger.getLogger(WSDetranFacil.class.getName());
	
	private HttpConnection connection;
	private String address;	
	private ObjectMapper mapper;
		
	private static WSDetranFacil instance = null;
	
	private WSDetranFacil(){
		Properties properties = new Properties();    	
    	try {
    		LOGGER.info("Detran OCR [Starting setup... ]");
    		properties.load(WSDetranFacil.class.getClassLoader().getResourceAsStream("config.properties"));
    		this.connection = HttpConnection.getInstance(properties.getProperty("ws.id"), properties.getProperty("ws.key"));
    		this.address = properties.getProperty("ws.url");
    		this.mapper = new ObjectMapper().configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    		LOGGER.info("Detran OCR [Finishing setup... ]");
    	} catch (Exception e) {
    		LOGGER.log(Level.SEVERE, e.getMessage(), e.getCause());
    	}
	}
	
	public static synchronized WSDetranFacil getInstance() {
		if(instance == null){
            instance = new WSDetranFacil();
        }
        return instance;
	}
	
	public UsuarioDTO obterUsuario(Long cpf) {
		try {
			UsuarioDTO usuario = mapper.readValue(connection.get(address+"/consulta/usuario/"+cpf),UsuarioDTO.class);
			return usuario.getCpf() != null ? usuario : null;
		} catch (Exception e) {
			return null;
		}
	}
	
	public void move(Long cpf, String filename) throws Exception {
		connection.post(address+"/termo/"+cpf+"/"+filename+"/-100/detran-ocr", new ArrayList<NameValuePair>());
	}
	
	public void moveFromTempToNaoEncontrados(String filename) throws Exception {
		connection.post(address+"/termo/temp/nao/cadastrados/"+filename+"/detran-ocr", new ArrayList<NameValuePair>());
	}
	
	public void moveFromTempToNaoIdentificadoViaOCR(String filename) throws Exception {
		connection.post(address+"/termo/temp/nao/identificado/ocr/"+filename+"/detran-ocr", new ArrayList<NameValuePair>());
	}
	
	public TermoDTO obterFromTemp() throws Exception {
		try {
			TermoDTO termo = mapper.readValue(connection.get(address+"/termo/temp/detran-ocr"), TermoDTO.class);			
			return termo.getNome() != null ? termo : null;
		} catch (Exception e) {
			return null;
		}
	}
}
