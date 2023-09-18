module com.example.ca1_v1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.ca1_v1 to javafx.fxml;
    exports com.example.ca1_v1;

    opens utils to javafx.fxml;
    exports utils;
}