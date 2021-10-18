module com.example.hangman {
	requires javafx.controls;
	requires javafx.fxml;

	requires org.controlsfx.controls;
	requires org.kordamp.bootstrapfx.core;
	requires javafx.graphics;
	requires javafx.media;

	opens com.example.hangman to javafx.fxml;
	exports com.example.hangman;
}