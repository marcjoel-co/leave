module com.leave.engine {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.leave.engine to javafx.fxml;
    exports com.leave.engine;
}
