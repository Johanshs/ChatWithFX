package com.ifrr.model;
 
public class Usuario {
    private String apelido;
    private String host;
 
    public Usuario(String apelido, String host) {
        this.apelido = apelido;
        this.host    = host;
    }
 
    public String getApelido() { return apelido; }
    public String getHost()    { return host; }
 
    @Override public String toString() { return getApelido(); }
 
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario u)) return false;
        return getApelido().equals(u.getApelido());
    }
 
    @Override public int hashCode() { return getApelido().hashCode(); }

    
    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public void setHost(String host) {
        this.host = host;
    }
}