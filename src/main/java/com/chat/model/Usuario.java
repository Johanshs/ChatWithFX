package com.chat.model;
 
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

    /**
     * @param apelido the apelido to set
     */
    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }
}