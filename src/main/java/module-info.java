module org.example.magua {
    requires javafx.controls;
    requires javafx.graphics;
    requires tools.jackson.databind;
    requires static lombok;

    exports org.example.magua;
    exports org.example.magua.ui;

    opens org.example.magua to javafx.graphics;
    exports org.example.magua.config;
    opens org.example.magua.config to javafx.graphics;
}
