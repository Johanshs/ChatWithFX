package com.ifrr.core;

import com.ifrr.model.Payload;
import com.ifrr.model.TipoConteudo;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;


public class ClientHandler implements Runnable {

    private static final Logger LOG = Logger.getLogger(ClientHandler.class.getName());

    private final Socket            socket;
    private final ChatServer        server;
    private       String            apelido;
    private       ObjectOutputStream out;
    private       ObjectInputStream  in;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in  = new ObjectInputStream(socket.getInputStream());

            Payload handshake = (Payload) in.readObject();
            this.apelido = handshake.getConteudo();

            if (server.apelidoEmUso(apelido)) {
                enviar(Payload.sistema("APELIDO_OCUPADO"));
                fechar();
                return;
            }

            server.registrar(apelido, this);
            LOG.info("[+] " + apelido + " conectado de " + socket.getInetAddress());

            server.broadcast(Payload.sistema("ENTROU:" + apelido), apelido);

            server.broadcastListaUsuarios();

            Payload p;
            while ((p = (Payload) in.readObject()) != null) {
                rotear(p);
            }

        } catch (EOFException | java.net.SocketException ignored) {
        } catch (Exception e) {
            LOG.warning("Erro no handler de " + apelido + ": " + e.getMessage());
        } finally {
            desconectar();
        }
    }

    private void rotear(Payload p) {
        if (p.isBroadcast()) {
            server.broadcast(p, null);          
        } else {
            server.privado(p);                  
        }
    }

    public synchronized void enviar(Payload p) {
        try {
            out.writeObject(p);
            out.flush();
            out.reset(); 
        } catch (IOException e) {
            LOG.warning("Falha ao enviar para " + apelido + ": " + e.getMessage());
        }
    }

    private void desconectar() {
        if (apelido != null) {
            server.remover(apelido);
            server.broadcast(Payload.sistema("SAIU:" + apelido), null);
            server.broadcastListaUsuarios();
            LOG.info("[-] " + apelido + " desconectado.");
        }
        fechar();
    }

    private void fechar() {
        try { socket.close(); } catch (IOException ignored) {}
    }

    public String getApelido() { return apelido; }
}
