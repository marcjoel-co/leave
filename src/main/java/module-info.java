module com.leave.engine {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.leave.engine to javafx.fxml;
    exports com.leave.engine;
}
