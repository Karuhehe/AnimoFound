module ph.edu.dlsu.lbycpei.animofound {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens controller to javafx.fxml;
    opens model to com.google.gson;
    opens main to javafx.graphics;
    opens util to javafx.fxml;

    exports main;
}
