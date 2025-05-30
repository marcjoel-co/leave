module com.leave.engine {
    
    
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.media;

    
    requires java.desktop;

    
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation; 

    opens com.leave.engine to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.leave.engine;
    exports com.leave.engine.utils;
}    