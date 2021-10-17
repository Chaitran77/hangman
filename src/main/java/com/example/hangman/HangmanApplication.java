package com.example.hangman;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

public class HangmanApplication extends Application {

	Random random = new Random();

	private GameButton startButton;
	private GameButton chooseFileButton;
	private HBox wordLetterLabelsContainer;
	private Label filePathLabel;
	private Label instructionLabel;
	private HBox incorrectGuessSection;

	private String wordsFilePath = String.valueOf(getClass().getResource("words.txt"));
	private HBox incorrectGuessLabels;
	private ImageView currentHangmanImage;

	private char[] wordToGuess = null;
	private char[] guessedLetters = null;
	private char[] incorrectlyGuessedLetters = null;

	private LetterLabel[] wordLetterLabels;

	private Node createHSpacer() {
		final Region spacer = new Region();
		// Make it always grow or shrink according to the available space
		HBox.setHgrow(spacer, Priority.ALWAYS);
		return spacer;
	}

	private Node createVSpacer() {
		final Region spacer = new Region();
		// Make it always grow or shrink according to the available space
		VBox.setVgrow(spacer, Priority.ALWAYS);
		return spacer;
	}

	public class GameButton extends Button {
		public GameButton(String text, String colour) {
			super(text);
			setStyle("-fx-padding: 35px; -fx-font-size: 30px; -fx-text-fill: " + colour);
		}
	}

	public class GameLabel extends Label {
		public GameLabel(String text) {
			super(text);
			setStyle("-fx-font-size: 30px");
		}
	}

	public class LetterLabel extends Label {
		private final int width;

		public LetterLabel(int width) {
			// one of these objects per letter in the chosen word.
			// therefore starts off as a blank line
			this.width = width;

			// min-width must be set, else label won't appear.
			setStyle("-fx-min-width: " + this.width + "; -fx-border-width: 0 0 3px 0; -fx-border-color: black; -fx-font-size: " + this.width + "px;");

		}

		public void setLetter(char letter) {
			setText(String.valueOf(letter));
		}
	}


	public void startGame(Scene scene) throws IOException {
		// disable buttons
		startButton.setDisable(true);
		chooseFileButton.setDisable(true);

		// pick a word from the file
		String[] allWords = Files.readString(Path.of(wordsFilePath)).split("\n");
		wordToGuess = allWords[random.nextInt(allWords.length)].toCharArray();
		System.out.println(new String(wordToGuess));

		guessedLetters = new char[wordToGuess.length];
		incorrectlyGuessedLetters = new char[26- wordToGuess.length];

		// generate the letter labels
		wordLetterLabels = new LetterLabel[wordToGuess.length];
		wordLetterLabelsContainer.getChildren().addAll(createHSpacer(), createHSpacer());

		for (int i=0; i< wordLetterLabels.length; i++) {
			wordLetterLabels[i] = new LetterLabel(40);
			wordLetterLabelsContainer.getChildren().addAll(wordLetterLabels[i], createHSpacer());
		}

		wordLetterLabelsContainer.getChildren().addAll(createHSpacer());

		// setup event listeners
		scene.setOnKeyPressed(new EventHandler<>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().isLetterKey()) {
					String letterReceived = event.getText();
					System.out.println(letterReceived);
					if (!(new String(guessedLetters).contains(letterReceived)) && !(new String(incorrectlyGuessedLetters).contains(letterReceived))) {
						// letter hasn't been guessed
						boolean letterFound = false;
						for (int i=0; i<wordToGuess.length; i++) {
							if (wordToGuess[i] == letterReceived.charAt(0)) {
								guessedLetters[i] = wordToGuess[i];
								wordLetterLabels[i].setLetter(wordToGuess[i]);
								letterFound = true;
							}
						}
						if (!letterFound) {
							incorrectlyGuessedLetters[0] = letterReceived.charAt(0);
							// advance the hangman bit of the game
						}
					}
				}
			}
		});

		// reveal game elements
		wordLetterLabelsContainer.setVisible(true);
		instructionLabel.setVisible(true);
		incorrectGuessSection.setVisible(true);
	}

	@Override
	public void start(Stage stage) {

		VBox rootLayout = new VBox();
		rootLayout.setAlignment(Pos.CENTER);

		HBox controlButtons = new HBox();

		this.startButton = new GameButton("Start game   ▶️ ", "green");

		this.chooseFileButton = new GameButton("Choose word file", "black");
		chooseFileButton.setOnMouseClicked((EventHandler<Event>) event -> {
			wordsFilePath = String.valueOf(new FileChooser().showOpenDialog(null));
			filePathLabel.setText(wordsFilePath);
		});

		controlButtons.getChildren().addAll(createHSpacer(), startButton, createHSpacer(), createHSpacer(), chooseFileButton, createHSpacer());

		this.filePathLabel = new Label(wordsFilePath); // empty to begin with, set when the file chooser dialog returns

		this.wordLetterLabelsContainer = new HBox();
		wordLetterLabelsContainer.setAlignment(Pos.CENTER);
		wordLetterLabelsContainer.setVisible(false);

		this.instructionLabel = new GameLabel("Type your letter. Correct guesses will appear above. Incorrect guesses will appear down below.");
		instructionLabel.setVisible(false);


		this.incorrectGuessLabels = new HBox();
		this.currentHangmanImage = new ImageView(String.valueOf(getClass().getResource("Hangman-0.png")));


		this.incorrectGuessSection = new HBox(
				new VBox(
						new GameLabel("Incorrectly guessed letters: "),
						this.incorrectGuessLabels
				),
				this.currentHangmanImage
		);
		incorrectGuessSection.setAlignment(Pos.CENTER);
		incorrectGuessSection.setVisible(false);

		rootLayout.getChildren().addAll(createVSpacer(),
				controlButtons,
				filePathLabel,
				createVSpacer(),
				createVSpacer(),
				wordLetterLabelsContainer,
				createVSpacer(),
				instructionLabel,
				createVSpacer(),
				incorrectGuessSection,
				createVSpacer(),
				createVSpacer()
		);

		Scene scene = new Scene(new StackPane(rootLayout), 2560, 1680);
		stage.setTitle("Hangman!");
		stage.setScene(scene);

		startButton.setOnMouseClicked((EventHandler<Event>) event -> {
			try {
				startGame(scene);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});


		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}