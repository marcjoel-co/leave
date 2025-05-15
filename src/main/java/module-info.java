module com.leave.engine { 
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics; 
    requires javafx.base;

    requires com.fasterxml.jackson.core;    
    requires com.fasterxml.jackson.databind; 
    
    opens com.leave.engine to javafx.fxml;
    opens com.leave.engine.utils to javafx.fxml;

    exports com.leave.engine;
}