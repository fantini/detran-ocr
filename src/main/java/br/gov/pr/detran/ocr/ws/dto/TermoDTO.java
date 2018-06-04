package br.gov.pr.detran.ocr.ws.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TermoDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private String nome;
	private byte[] arquivo;
	private List<String> mensagens = new ArrayList<String>(0);
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public byte[] getArquivo() {
		return arquivo;
	}
	public void setArquivo(byte[] arquivo) {
		this.arquivo = arquivo;
	}
	public List<String> getMensagens() {
		return mensagens;
	}
	public void setMensagens(List<String> mensagens) {
		this.mensagens = mensagens;
	}
	
}