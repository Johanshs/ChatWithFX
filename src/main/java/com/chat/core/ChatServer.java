package com.chat.core;

import com.chat.model.Payload;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class ChatServer {

    private static final Logger LOG  = Logger.getLogger(ChatServer.class.getName());
    public  static final int    PORT = 12345;

    private final Map<String, ClientHandler> clientes = new ConcurrentHashMap<>();
    private final ExecutorService            pool     = Executors.newCachedThreadPool();
    private       ServerSocket               serverSocket;
    private       boolean                    rodando  = false;


    public void iniciar() throws IOException {
        serverSocket = new ServerSocket(PORT);
        rodando = true;
        LOG.info("Servidor iniciado na porta " + PORT);

        Thread acceptor = new Thread(() -> {
            while (rodando) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(socket, this);
                    pool.execute(handler);
                } catch (IOException e) {
                    if (rodando) LOG.warning("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }, "acceptor");
        acceptor.setDaemon(true);
        acceptor.start();
    }

    public void parar() {
        rodando = false;
        pool.shutdownNow();
        try { serverSocket.close(); } catch (IOException ignored) {}
    }


    public void broadcast(Payload p, String excludeApelido) {
        clientes.forEach((apelido, handler) -> {
            if (excludeApelido == null || !apelido.equals(excludeApelido)) {
                handler.enviar(p);
            }
        });
    }

    public void privado(Payload p) {
        ClientHandler destinatario = clientes.get(p.getDestinatario());
        ClientHandler remetente    = clientes.get(p.getRemetente());

        if (destinatario != null) destinatario.enviar(p);
        if (remetente    != null) remetente.enviar(p);
    }

    public void broadcastListaUsuarios() {
        String lista = clientes.keySet().stream()
                               .sorted()
                               .collect(Collectors.joining(","));
        broadcast(Payload.sistema("USUARIOS:" + lista), null);
    }


    public void registrar(String apelido, ClientHandler handler) {
        clientes.put(apelido, handler);
    }

    public void remover(String apelido) {
        clientes.remove(apelido);
    }

    public boolean apelidoEmUso(String apelido) {
        return clientes.containsKey(apelido);
    }

    public int totalConectados() { return clientes.size(); }


    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer();
        server.iniciar();
        System.out.println("=== Chat Server rodando na porta " + PORT + " ===");
        System.out.println("Pressione ENTER para encerrar.");
        System.in.read();
        server.parar();
    }
}
