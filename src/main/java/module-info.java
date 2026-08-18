module org.example.magua {
    requires javafx.controls;
    requires javafx.graphics;

    exports org.example.magua;
    exports org.example.magua.ui;
    exports org.example.magua.state;
    exports org.example.magua.utils;

    opens org.example.magua to javafx.graphics;
}
