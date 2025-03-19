module org.myproject.chatjfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens org.myproject.chatjfx to javafx.fxml;
    exports org.myproject.chatjfx;
}