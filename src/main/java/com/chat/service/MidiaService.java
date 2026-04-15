package com.chat.service;

import com.chat.model.Payload;
import com.chat.model.TipoConteudo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


public class MidiaService {

    private static final long MAX_BYTES = 50L * 1024 * 1024; 

    private final Path pastaDownloads;

    public MidiaService() {
        pastaDownloads = Path.of(System.getProperty("user.home"), "ChatDownloads");
        try { Files.createDirectories(pastaDownloads); } catch (IOException ignored) {}
    }


    public Payload empacotar(File arquivo, String remetente,
                             String destinatario, TipoConteudo tipo) throws IOException {
        if (arquivo.length() > MAX_BYTES) {
            throw new IOException("Arquivo muito grande (máx. 50 MB): " + arquivo.getName());
        }
        byte[] dados    = Files.readAllBytes(arquivo.toPath());
        String mimeType = detectarMime(arquivo.getName());
        return Payload.midia(remetente, destinatario, tipo,
                             arquivo.getName(), dados, mimeType);
    }


    public Path salvar(Payload p) throws IOException {
        String nome = p.getRemetente() + "_" + System.currentTimeMillis()
                      + "_" + p.getConteudo();
        Path destino = pastaDownloads.resolve(nome);
        Files.write(destino, p.getDados(), StandardOpenOption.CREATE);
        return destino;
    }

    public Path getPastaDownloads() { return pastaDownloads; }


    private String detectarMime(String nome) {
        String n = nome.toLowerCase();
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".png"))  return "image/png";
        if (n.endsWith(".gif"))  return "image/gif";
        if (n.endsWith(".mp3"))  return "audio/mpeg";
        if (n.endsWith(".ogg"))  return "audio/ogg";
        if (n.endsWith(".wav"))  return "audio/wav";
        if (n.endsWith(".mp4"))  return "video/mp4";
        if (n.endsWith(".webm")) return "video/webm";
        return "application/octet-stream";
    }
}
