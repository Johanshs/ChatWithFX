module com.ifrr.chatjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires javafx.media;
    requires java.desktop;

    
    opens com.ifrr.controller to javafx.fxml;
    exports com.ifrr.controller;
}
