package com.ifrr.controller;

import com.ifrr.model.Payload;
import com.ifrr.model.TipoConteudo;
import com.ifrr.service.ConexaoService;
import com.ifrr.service.MidiaService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatController {

    @FXML private ListView<String>             lvUsuarios;
    @FXML private VBox                         vboxMensagens;
    @FXML private ScrollPane        scrollMensagens;
    @FXML private TextField         tfMensagem;
    @FXML private Label             lblDestinatario;
    @FXML private Label             lblStatus;
    @FXML private Button            btnEnviar;
    @FXML private Button            btnAnexar;
    @FXML private Button            btnTodos;

    private String           apelido;
    private String           destinatario = null;   // null = global
    private ConexaoService   conexao;
    private MidiaService     midia;
    private final ObservableList<String> usuarios = FXCollections.observableArrayList();
    private final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm");


    public void inicializar(String apelido, ConexaoService conexao) {
        this.apelido = apelido;
        this.conexao = conexao;
        this.midia   = new MidiaService();

        lvUsuarios.setItems(usuarios);
        atualizarDestinatario(null);

        conexao.setOnMensagem(p -> Platform.runLater(() -> processarPayload(p)));

        lvUsuarios.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String sel = lvUsuarios.getSelectionModel().getSelectedItem();
                if (sel != null && !sel.equals(apelido)) {
                    atualizarDestinatario(sel);
                }
            }
        });

        tfMensagem.setOnAction(e -> onEnviarTexto());

        vboxMensagens.heightProperty().addListener((obs, o, n) ->
            scrollMensagens.setVvalue(1.0));
    }


    private void processarPayload(Payload p) {
        switch (p.getTipo()) {
            case TEXTO  -> exibirBolhaTexto(p);
            case FOTO   -> exibirFoto(p);
            case AUDIO  -> exibirAudio(p);
            case VIDEO  -> exibirVideo(p);
            case SISTEMA -> processarSistema(p);
        }
    }

    private void processarSistema(Payload p) {
        String msg = p.getConteudo();

        if (msg.startsWith("USUARIOS:")) {
            usuarios.clear();
            String lista = msg.substring(9);
            if (!lista.isBlank()) {
                for (String u : lista.split(",")) {
                    if (!u.isBlank()) usuarios.add(u.trim());
                }
            }
        } else if (msg.startsWith("ENTROU:")) {
            exibirSistema("→ " + msg.substring(7) + " entrou no chat.");
        } else if (msg.startsWith("SAIU:")) {
            String quem = msg.substring(5);
            exibirSistema("← " + quem + " saiu do chat.");
            if (quem.equals(destinatario)) atualizarDestinatario(null);
        } else if ("DESCONECTADO".equals(msg)) {
            exibirSistema("⚠ Desconectado do servidor.");
            lblStatus.setText("Desconectado");
            lblStatus.setStyle("-fx-text-fill: #c0392b;");
        } else if ("APELIDO_OCUPADO".equals(msg)) {
            exibirSistema("⚠ Apelido já em uso.");
        }
    }


    @FXML private void onEnviarTexto() {
        String texto = tfMensagem.getText().trim();
        if (texto.isEmpty() || !conexao.isConectado()) return;

        Payload p = Payload.texto(apelido, destinatario, texto);
        conexao.enviar(p);
        tfMensagem.clear();
    }

    @FXML private void onAnexar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Selecionar arquivo de mídia");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imagens",  "*.jpg", "*.jpeg", "*.png", "*.gif"),
            new FileChooser.ExtensionFilter("Áudio",    "*.mp3", "*.wav", "*.ogg"),
            new FileChooser.ExtensionFilter("Vídeo",    "*.mp4", "*.webm"),
            new FileChooser.ExtensionFilter("Todos",    "*.*")
        );
        File arquivo = fc.showOpenDialog(btnAnexar.getScene().getWindow());
        if (arquivo == null) return;

        TipoConteudo tipo = detectarTipo(arquivo.getName());
        try {
            Payload p = midia.empacotar(arquivo, apelido, destinatario, tipo);
            conexao.enviar(p);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML private void onTodos() {
        atualizarDestinatario(null);
    }


    private void exibirBolhaTexto(Payload p) {
        boolean minha = apelido.equals(p.getRemetente());
        String hora   = SDF.format(new Date(p.getTimestamp()));

        Text textoNode = new Text(p.getConteudo());
        textoNode.setWrappingWidth(360);

        Label lblRem  = new Label(p.getRemetente() + "  " + hora);
        lblRem.setStyle("-fx-font-size:10; -fx-text-fill:#888;");

        VBox balao = new VBox(2, lblRem, textoNode);
        balao.setPadding(new Insets(6, 10, 6, 10));
        balao.setMaxWidth(400);
        balao.setStyle(minha
            ? "-fx-background-color:#dcf8c6; -fx-background-radius:12; -fx-border-radius:12;"
            : "-fx-background-color:#fff; -fx-background-radius:12; -fx-border-radius:12; -fx-border-color:#e0e0e0; -fx-border-width:0.5;");

        if (!p.isBroadcast()) {
            Label privLabel = new Label("🔒 privado");
            privLabel.setStyle("-fx-font-size:9; -fx-text-fill:#888;");
            balao.getChildren().add(privLabel);
        }

        HBox linha = new HBox(balao);
        linha.setPadding(new Insets(3, 12, 3, 12));
        linha.setAlignment(minha ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        vboxMensagens.getChildren().add(linha);
    }

    private void exibirFoto(Payload p) {
        boolean minha = apelido.equals(p.getRemetente());
        String hora   = SDF.format(new Date(p.getTimestamp()));

        try {
            Path salvo = midia.salvar(p);
            Image img  = new Image(new ByteArrayInputStream(p.getDados()),
                                   300, 0, true, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(300);
            iv.setPreserveRatio(true);
            iv.setStyle("-fx-cursor:hand;");
            iv.setOnMouseClicked(e -> abrirArquivoExterno(salvo));

            Label lblRem = new Label(p.getRemetente() + "  " + hora + "  📷 " + p.getConteudo());
            lblRem.setStyle("-fx-font-size:10; -fx-text-fill:#888;");

            VBox balao = new VBox(4, lblRem, iv);
            balao.setPadding(new Insets(6, 10, 6, 10));
            balao.setStyle("-fx-background-color:" + (minha ? "#dcf8c6" : "#fff")
                + "; -fx-background-radius:12; -fx-border-radius:12;"
                + (minha ? "" : " -fx-border-color:#e0e0e0; -fx-border-width:0.5;"));

            HBox linha = new HBox(balao);
            linha.setPadding(new Insets(3, 12, 3, 12));
            linha.setAlignment(minha ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            vboxMensagens.getChildren().add(linha);

        } catch (IOException e) {
            exibirSistema("Erro ao salvar foto: " + e.getMessage());
        }
    }

    private void exibirAudio(Payload p) {
        boolean minha = apelido.equals(p.getRemetente());
        try {
            Path salvo = midia.salvar(p);

            Button btnPlay = new Button("▶  " + p.getConteudo());
            btnPlay.setStyle("-fx-cursor:hand;");
            btnPlay.setOnAction(e -> reproduzirAudio(salvo));

            Label lblRem = new Label(p.getRemetente() + "  🎵");
            lblRem.setStyle("-fx-font-size:10; -fx-text-fill:#888;");

            VBox balao = new VBox(4, lblRem, btnPlay);
            balao.setPadding(new Insets(6, 10, 6, 10));
            balao.setStyle("-fx-background-color:" + (minha ? "#dcf8c6" : "#fff")
                + "; -fx-background-radius:12;");

            HBox linha = new HBox(balao);
            linha.setPadding(new Insets(3, 12, 3, 12));
            linha.setAlignment(minha ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            vboxMensagens.getChildren().add(linha);

        } catch (IOException e) {
            exibirSistema("Erro ao salvar áudio: " + e.getMessage());
        }
    }

    private void exibirVideo(Payload p) {
        boolean minha = apelido.equals(p.getRemetente());
        try {
            Path salvo = midia.salvar(p);

            Button btnAbrir = new Button("🎬  Abrir: " + p.getConteudo());
            btnAbrir.setOnAction(e -> abrirArquivoExterno(salvo));

            Label lblRem = new Label(p.getRemetente() + "  🎬");
            lblRem.setStyle("-fx-font-size:10; -fx-text-fill:#888;");

            VBox balao = new VBox(4, lblRem, btnAbrir);
            balao.setPadding(new Insets(6, 10, 6, 10));
            balao.setStyle("-fx-background-color:" + (minha ? "#dcf8c6" : "#fff")
                + "; -fx-background-radius:12;");

            HBox linha = new HBox(balao);
            linha.setPadding(new Insets(3, 12, 3, 12));
            linha.setAlignment(minha ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            vboxMensagens.getChildren().add(linha);

        } catch (IOException e) {
            exibirSistema("Erro ao salvar vídeo: " + e.getMessage());
        }
    }

    private void exibirSistema(String msg) {
        Label lbl = new Label(msg);
        lbl.setStyle("-fx-text-fill:#888; -fx-font-size:11; -fx-font-style:italic;");
        HBox linha = new HBox(lbl);
        linha.setAlignment(Pos.CENTER);
        linha.setPadding(new Insets(2, 0, 2, 0));
        vboxMensagens.getChildren().add(linha);
    }


    private void atualizarDestinatario(String novo) {
        destinatario = novo;
        if (novo == null) {
            lblDestinatario.setText("Para: Todos");
            lblDestinatario.setStyle("-fx-text-fill:#2980b9;");
        } else {
            lblDestinatario.setText("Para: " + novo + " (privado)");
            lblDestinatario.setStyle("-fx-text-fill:#8e44ad;");
        }
    }

    private TipoConteudo detectarTipo(String nome) {
        String n = nome.toLowerCase();
        if (n.matches(".*\\.(jpg|jpeg|png|gif)")) return TipoConteudo.FOTO;
        if (n.matches(".*\\.(mp3|wav|ogg)"))      return TipoConteudo.AUDIO;
        if (n.matches(".*\\.(mp4|webm)"))          return TipoConteudo.VIDEO;
        return TipoConteudo.FOTO;
    }

    private void reproduzirAudio(Path arquivo) {
        try {
            Media media = new Media(arquivo.toUri().toString());
            MediaPlayer player = new MediaPlayer(media);
            player.play();
        } catch (Exception e) {
            abrirArquivoExterno(arquivo);
        }
    }

    private void abrirArquivoExterno(Path arquivo) {
        try { Desktop.getDesktop().open(arquivo.toFile()); }
        catch (Exception e) { exibirSistema("Não foi possível abrir: " + arquivo.getFileName()); }
    }


}
