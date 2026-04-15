module com.ifrr.chatjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    
    
    opens com.ifrr.controller to javafx.fxml;
    exports com.ifrr.controller;
}
