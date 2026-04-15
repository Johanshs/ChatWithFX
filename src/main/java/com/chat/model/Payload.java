/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.chat.model;

import java.io.Serializable;

/**
 * Payload — contrato de comunicação entre cliente e servidor.
 * Trafega como JSON via ObjectOutputStream/ObjectInputStream.
 *
 * destinatario == null  →  mensagem global (broadcast)
 * destinatario != null  →  mensagem privada
 */
public class Payload implements Serializable {

    private static long serialVersionUID = 1L;

    private String      remetente;
    private String      destinatario;   // null = broadcast
    private TipoConteudo tipo;
    private String      conteudo;       // texto ou, para mídia, nome do arquivo
    private byte[]      dados;          // bytes da mídia (null para TEXTO)
    private String      mimeType;       // ex.: "image/jpeg", "audio/mp3"
    private long        timestamp;

    // Construtores 

    public Payload() {}

    // Mensagem de texto (global ou privada)
    public static Payload texto(String remetente, String destinatario, String texto) {
        Payload p = new Payload();
        p.setRemetente(remetente);
        p.setDestinatario(destinatario);
        p.setTipo(TipoConteudo.Texto);
        p.setConteudo(texto);
        p.setTimestamp(System.currentTimeMillis());
        return p;
    }

    // Mensagem de mídia (áudio, foto, vídeo) 
    public static Payload midia(String remetente, String destinatario,
                                TipoConteudo tipo, String nomeArquivo,
                                byte[] dados, String mimeType) {
        Payload p = new Payload();
        p.setRemetente(remetente);
        p.setDestinatario(destinatario);
        p.setTipo(tipo);
        p.setConteudo(nomeArquivo);
        p.setDados(dados);
        p.setMimeType(mimeType);
        p.setTimestamp(System.currentTimeMillis());
        return p;
    }

    /** Mensagem de sistema (lista de usuários, notificações) */
    public static Payload sistema(String conteudo) {
        Payload p = new Payload();
        p.setRemetente("SERVIDOR");
        p.setTipo(TipoConteudo.Sistema);
        p.setConteudo(conteudo);
        p.setTimestamp(System.currentTimeMillis());
        return p;
    }

    public boolean isBroadcast() { return getDestinatario() == null || getDestinatario().isBlank(); }

    /**
     * @return the serialVersionUID
     */
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    /**
     * @param aSerialVersionUID the serialVersionUID to set
     */
    public static void setSerialVersionUID(long aSerialVersionUID) {
        serialVersionUID = aSerialVersionUID;
    }

    /**
     * @return the remetente
     */
    public String getRemetente() {
        return remetente;
    }

    /**
     * @param remetente the remetente to set
     */
    public void setRemetente(String remetente) {
        this.remetente = remetente;
    }

    /**
     * @return the destinatario
     */
    public String getDestinatario() {
        return destinatario;
    }

    /**
     * @param destinatario the destinatario to set
     */
    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    /**
     * @return the tipo
     */
    public TipoConteudo getTipo() {
        return tipo;
    }

    /**
     * @param tipo the tipo to set
     */
    public void setTipo(TipoConteudo tipo) {
        this.tipo = tipo;
    }

    /**
     * @return the conteudo
     */
    public String getConteudo() {
        return conteudo;
    }

    /**
     * @param conteudo the conteudo to set
     */
    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    /**
     * @return the dados
     */
    public byte[] getDados() {
        return dados;
    }

    /**
     * @param dados the dados to set
     */
    public void setDados(byte[] dados) {
        this.dados = dados;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}