package com.ifrr.controller;

import com.ifrr.core.ChatServer;
import com.ifrr.service.ConexaoService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField     tfApelido;
    @FXML private TextField     tfHost;
    @FXML private TextField     tfPorta;
    @FXML private CheckBox      cbServidorLocal;
    @FXML private Button        btnConectar;
    @FXML private Label         lblStatus;
    @FXML private ProgressIndicator spinner;

    private ConexaoService conexaoService;

    @FXML
    public void initialize() {
        spinner.setVisible(false);
        tfPorta.setText(String.valueOf(ChatServer.PORT));

        cbServidorLocal.selectedProperty().addListener((obs, antigo, novo) -> {
            tfHost.setDisable(novo);
            if (novo) tfHost.setText("localhost");
        });
    }

    @FXML
    private void onConectar() {
        String apelido = tfApelido.getText().trim();
        String host    = tfHost.getText().trim();
        int    porta;

        if (apelido.isEmpty() || apelido.contains(",") || apelido.contains(":")) {
            mostrarErro("Apelido inválido (sem vírgulas ou dois-pontos).");
            return;
        }
        try {
            porta = Integer.parseInt(tfPorta.getText().trim());
        } catch (NumberFormatException e) {
            mostrarErro("Porta inválida.");
            return;
        }

        if (cbServidorLocal.isSelected()) {
            try {
                ChatServer server = new ChatServer();
                server.iniciar();
            } catch (IOException e) {
            }
        }

        btnConectar.setDisable(true);
        spinner.setVisible(true);
        lblStatus.setText("Conectando…");

        final String finalHost  = host;
        final int    finalPorta = porta;

        Thread t = new Thread(() -> {
            conexaoService = new ConexaoService();
            boolean ok = conexaoService.conectar(finalHost, finalPorta, apelido);

            Platform.runLater(() -> {
                spinner.setVisible(false);
                if (ok) {
                    abrirChat(apelido, conexaoService);
                } else {
                    btnConectar.setDisable(false);
                    mostrarErro("Não foi possível conectar. Verifique host/porta ou se o apelido já está em uso.");
                }
            });
        }, "login-thread");
        t.setDaemon(true);
        t.start();
    }

    private void abrirChat(String apelido, ConexaoService conexaoService) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ifrr/chatjavafx/ChatView.fxml"));
            Parent root = loader.load();

            ChatController chatCtrl = loader.getController();
            chatCtrl.inicializar(apelido, conexaoService);

            Stage stage = (Stage) btnConectar.getScene().getWindow();
            stage.setTitle("Chat — " + apelido);
            stage.setScene(new Scene(root, 860, 600));
            stage.setOnCloseRequest(e -> conexaoService.fechar());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir janela de chat: " + e.getMessage());
            btnConectar.setDisable(false);
    
        }
    }

    private void mostrarErro(String msg) {
        lblStatus.setStyle("-fx-text-fill: #c0392b;");
        lblStatus.setText(msg);
    }
}
