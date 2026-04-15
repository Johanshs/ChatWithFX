package com.ifrr.service;

import com.ifrr.model.Payload;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.logging.Logger;


public class ConexaoService {

    private static final Logger LOG = Logger.getLogger(ConexaoService.class.getName());

    private Socket             socket;
    private ObjectOutputStream out;
    private ObjectInputStream  in;
    private boolean            conectado = false;
    private Consumer<Payload>  onMensagem;

    public void setOnMensagem(Consumer<Payload> callback) {
        this.onMensagem = callback;
    }

   
    public boolean conectar(String host, int porta, String apelido) {
        try {
            socket = new Socket(host, porta);
            out    = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in     = new ObjectInputStream(socket.getInputStream());

            out.writeObject(Payload.sistema(apelido));
            out.flush();
            out.reset();

            conectado = true;


            Payload primeira = (Payload) in.readObject();
            if (primeira.getTipo() == com.ifrr.model.TipoConteudo.SISTEMA
                    && "APELIDO_OCUPADO".equals(primeira.getConteudo())) {
                fechar();
                return false;
            }

            if (onMensagem != null) onMensagem.accept(primeira);

            Thread leitor = new Thread(this::loopLeitura, "client-reader");
            leitor.setDaemon(true);
            leitor.start();

            return true;

        } catch (Exception e) {
            LOG.severe("Falha ao conectar: " + e.getMessage());
            return false;
        }
    }


    private void loopLeitura() {
        try {
            Payload p;
            while (conectado && (p = (Payload) in.readObject()) != null) {
                if (onMensagem != null) {
                    final Payload payload = p;
                    onMensagem.accept(payload);
                }
            }
        } catch (EOFException | java.net.SocketException ignored) {
        } catch (Exception e) {
            LOG.warning("Leitura interrompida: " + e.getMessage());
        } finally {
            conectado = false;
            if (onMensagem != null) {
                onMensagem.accept(Payload.sistema("DESCONECTADO"));
            }
        }
    }


    public synchronized void enviar(Payload p) {
        if (out == null) return;
        try {
            out.writeObject(p);
            out.flush();
            out.reset();
        } catch (IOException e) {
            LOG.warning("Falha ao enviar: " + e.getMessage());
        }
    }


    public void fechar() {
        conectado = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    public boolean isConectado() { return conectado; }
}
