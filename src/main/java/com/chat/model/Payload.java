package com.chat.model;

import java.io.Serializable;


public class Payload implements Serializable {

    private static final long serialVersionUID = 1L;

    private String      remetente;
    private String      destinatario;   // null = broadcast
    private TipoConteudo tipo;
    private String      conteudo;       
    private byte[]      dados;          
    private String      mimeType;       
    private long        timestamp;

    // ─── Construtores ──────────────────────────────────────────────────────────

    public Payload() {}

    public static Payload texto(String remetente, String destinatario, String texto) {
        Payload p = new Payload();
        p.remetente    = remetente;
        p.destinatario = destinatario;
        p.tipo         = TipoConteudo.TEXTO;
        p.conteudo     = texto;
        p.timestamp    = System.currentTimeMillis();
        return p;
    }

    public static Payload midia(String remetente, String destinatario,
                                TipoConteudo tipo, String nomeArquivo,
                                byte[] dados, String mimeType) {
        Payload p = new Payload();
        p.remetente    = remetente;
        p.destinatario = destinatario;
        p.tipo         = tipo;
        p.conteudo     = nomeArquivo;
        p.dados        = dados;
        p.mimeType     = mimeType;
        p.timestamp    = System.currentTimeMillis();
        return p;
    }

    public static Payload sistema(String conteudo) {
        Payload p = new Payload();
        p.remetente = "SERVIDOR";
        p.tipo      = TipoConteudo.SISTEMA;
        p.conteudo  = conteudo;
        p.timestamp = System.currentTimeMillis();
        return p;
    }


    public String      getRemetente()    { return remetente; }
    public String      getDestinatario() { return destinatario; }
    public TipoConteudo getTipo()        { return tipo; }
    public String      getConteudo()     { return conteudo; }
    public byte[]      getDados()        { return dados; }
    public String      getMimeType()     { return mimeType; }
    public long        getTimestamp()    { return timestamp; }

    public void setRemetente(String v)    { remetente = v; }
    public void setDestinatario(String v) { destinatario = v; }
    public void setTipo(TipoConteudo v)   { tipo = v; }
    public void setConteudo(String v)     { conteudo = v; }
    public void setDados(byte[] v)        { dados = v; }
    public void setMimeType(String v)     { mimeType = v; }

    public boolean isBroadcast() { return destinatario == null || destinatario.isBlank(); }
}
