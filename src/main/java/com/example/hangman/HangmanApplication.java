package com.example.hangman;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public class HangmanApplication extends Application {

	Random random = new Random();

	private GameButton startButton;
	private GameButton chooseFileButton;
	private HBox wordLetterLabelsContainer;
	private Label filePathLabel;
	private Label instructionLabel;
	private HBox incorrectGuessSection;

	private String wordsFileResName = "words.txt";
	private String correctSoundFileResName = "correct.wav";
	private String wrongSoundFileResName = "wrong.wav";

	private InputStream wordsFileStream = getClass().getResourceAsStream(wordsFileResName);
	private boolean externalWordsFile = false;

	private HBox incorrectGuessLabels;
	private ImageView currentHangmanImage;

	private char[] wordToGuess = null;
	private char[] guessedLetters = null;
	private ArrayList<Character> incorrectlyGuessedLetters;

	private int incorrectGuesses = 0;

	private LetterLabel[] wordLetterLabels;
	private Image hangmanImageData;

//	private final AudioClip correctSound = new AudioClip(getClass().getResource(correctSoundFileResName).toExternalForm());
//	private final AudioClip wrongSound = new AudioClip(getClass().getResource(wrongSoundFileResName).toExternalForm());

	EventHandler<KeyEvent> keypressListener;
	private final int maxIncorrectGuesses = 6;


	private String readFromInputStream(InputStream inputStream)
			throws IOException {
		StringBuilder resultStringBuilder = new StringBuilder();
		try (BufferedReader br
					 = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = br.readLine()) != null) {
				resultStringBuilder.append(line).append("\n");
			}
		}
		// vvery important - allows IS to be read again
//		inputStream.mark(0);
//		inputStream.reset();
		return resultStringBuilder.toString();
	}


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
			// therefore, starts off as a blank line
			this.width = width;

			// min-width must be set, else label won't appear.
			setStyle("-fx-min-width: " + this.width + "; -fx-border-width: 0 0 7px 0; -fx-border-color: black; -fx-font-size: " + this.width + "px;");
			setAlignment(Pos.CENTER);
		}

		public void setLetter(char letter) {
			setText(String.valueOf(letter).toUpperCase());
		}
	}

	public void startGame(Scene scene) throws IOException {
		// disable buttons
		startButton.setDisable(true);
		chooseFileButton.setDisable(true);

		// pick a word from the file
		String[] allWords = readFromInputStream(wordsFileStream).split("\n");
		wordToGuess = allWords[random.nextInt(allWords.length)].trim().toCharArray();
		System.out.println(new String(wordToGuess));

		// VERY IMPORTANT TO RECREATE I.S.!
		if (!externalWordsFile) {
			wordsFileStream = getClass().getResourceAsStream(wordsFileResName);
		} else {
			wordsFileStream = new FileInputStream(wordsFileResName);
		}


		guessedLetters = new char[wordToGuess.length];
		incorrectlyGuessedLetters = new ArrayList<>();

		// generate the letter labels
		wordLetterLabels = new LetterLabel[wordToGuess.length];
		wordLetterLabelsContainer.getChildren().addAll(createHSpacer(), createHSpacer());

		for (int i=0; i< wordLetterLabels.length; i++) {
			wordLetterLabels[i] = new LetterLabel(75);
			wordLetterLabelsContainer.getChildren().addAll(wordLetterLabels[i], createHSpacer());
		}

		wordLetterLabelsContainer.getChildren().addAll(createHSpacer());

		// setup event listeners
		keypressListener = new EventHandler<>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().isLetterKey()) {
					String letterReceived = event.getText();
					System.out.println(letterReceived);
					if (!(new String(guessedLetters).contains(letterReceived)) && !(incorrectlyGuessedLetters.contains(letterReceived.charAt(0)))) {
						// letter hasn't been guessed
						boolean letterFound = false;
						for (int i=0; i<wordToGuess.length; i++) {
							if (wordToGuess[i] == letterReceived.charAt(0)) {
								guessedLetters[i] = wordToGuess[i];
								wordLetterLabels[i].setLetter(wordToGuess[i]);
								letterFound = true;
								System.out.println(Arrays.asList(guessedLetters).contains(null));
								System.out.println(Arrays.toString(guessedLetters));
//								correctSound.play();

								if (Arrays.equals(wordToGuess, guessedLetters)) {
									System.out.println("WON");
									try {
										winGame(scene);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
						}
						if (!letterFound) {
							incorrectlyGuessedLetters.add(letterReceived.charAt(0));
							// advance the hangman bit of the game
							incorrectGuesses++;

							if (incorrectGuesses <= maxIncorrectGuesses) {
								LetterLabel incorrectLetterLabel = new LetterLabel(30);
								incorrectLetterLabel.setLetter(letterReceived.charAt(0));
								incorrectGuessLabels.getChildren().add(incorrectLetterLabel);

//								wrongSound.play();

								incorrectGuessSection.getChildren().remove(3);
								hangmanImageData = new Image(String.valueOf(getClass().getResource("Hangman-" + incorrectGuesses + ".png")));
								currentHangmanImage = new ImageView(hangmanImageData);
								incorrectGuessSection.getChildren().add(3, currentHangmanImage);
							} else {
								try {
									loseGame(scene);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		};

		scene.setOnKeyPressed(keypressListener);

		// reveal game elements
		wordLetterLabelsContainer.setVisible(true);
		instructionLabel.setVisible(true);
		incorrectGuessSection.setVisible(true);
	}

	public void winGame(Scene scene) throws IOException {

		String wordToGuessString = new String(wordToGuess);

		Alert alert = new Alert(Alert.AlertType.NONE,
				"Congratulations - you won!\nThe word was " + wordToGuessString.toUpperCase() + ".\nWould you like to play again?",
				ButtonType.NO,
				ButtonType.YES);
		alert.setTitle("You won!");
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.YES) {
			cleanup();
			startGame(scene);
		} else {
			startButton.setDisable(false);
			chooseFileButton.setDisable(false);

			// disable key listener
			scene.removeEventHandler(KeyEvent.KEY_PRESSED, keypressListener);
		}
	}

	public void loseGame(Scene scene) throws IOException {

		String wordToGuessString = new String(wordToGuess);

		Alert alert = new Alert(Alert.AlertType.NONE,
				"You unfortunately lost :(\nThe word was " + wordToGuessString.toUpperCase() + ".\nBetter luck next time!\nWould you like to play again?",
				ButtonType.NO,
				ButtonType.YES);
		alert.setTitle("You Lost :(");
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.YES) {
			cleanup();
			startGame(scene);
		} else {
			startButton.setDisable(false);
			chooseFileButton.setDisable(false);

			// disable key listener
			scene.removeEventHandler(KeyEvent.KEY_PRESSED, keypressListener);
		}
	}

	public void cleanup() {

		incorrectGuessLabels.getChildren().clear();

		incorrectGuessSection.getChildren().remove(3);
		hangmanImageData = new Image(String.valueOf(getClass().getResource("Hangman-0.png")));
		currentHangmanImage = new ImageView(hangmanImageData);
		incorrectGuessSection.getChildren().add(3, currentHangmanImage);

		incorrectGuesses = 0;

		wordToGuess = null;
		guessedLetters = null;
		incorrectlyGuessedLetters = null;

		wordLetterLabels = null;
		wordLetterLabelsContainer.getChildren().clear();
	}

	@Override
	public void start(Stage stage) {

		VBox rootLayout = new VBox();
		rootLayout.setAlignment(Pos.CENTER);

		HBox controlButtons = new HBox();

		this.startButton = new GameButton("Start game   ▶", "green");

		this.chooseFileButton = new GameButton("Choose word file", "black");
		chooseFileButton.setOnMouseClicked((EventHandler<Event>) event -> {
			File result = new FileChooser().showOpenDialog(null);
			if (result != null) {
				wordsFileResName = String.valueOf(result);
				filePathLabel.setText(wordsFileResName);
				// VERY IMPORTANT TO RECREATE I.S.!
				try {
					wordsFileStream = new FileInputStream(wordsFileResName);
					externalWordsFile = true;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		});

		controlButtons.getChildren().addAll(createHSpacer(), startButton, createHSpacer(), createHSpacer(), chooseFileButton, createHSpacer());

		this.filePathLabel = new Label("words.txt"); // empty to begin with, set when the file chooser dialog returns


		this.wordLetterLabelsContainer = new HBox();
		wordLetterLabelsContainer.setAlignment(Pos.CENTER);
		wordLetterLabelsContainer.setVisible(false);

		this.instructionLabel = new GameLabel("Type your letter. Correct guesses will appear above. Incorrect guesses will appear down below.");
		instructionLabel.setVisible(false);


		this.incorrectGuessLabels = new HBox();
		hangmanImageData = new Image(String.valueOf(getClass().getResource("Hangman-0.png")));
		this.currentHangmanImage = new ImageView(hangmanImageData);

		VBox incorrectGuessesBox = new VBox(
				new GameLabel("Incorrectly guessed letters: "),
				this.incorrectGuessLabels,
				createVSpacer()
		);
		incorrectGuessesBox.setAlignment(Pos.BOTTOM_LEFT);

		this.incorrectGuessSection = new HBox(
				createHSpacer(),
				incorrectGuessesBox,
				createHSpacer(),
				this.currentHangmanImage,
				createHSpacer()
		);
		incorrectGuessSection.setAlignment(Pos.CENTER);
		incorrectGuessSection.setVisible(false);

		rootLayout.getChildren().addAll(createVSpacer(),
				controlButtons,
				createVSpacer(),
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
		StackPane pane = new StackPane(rootLayout);
		pane.setStyle("-fx-background-color: linear-gradient(to top left, rgba(150,150,255) 10%, rgba(255,224,0) 50%, rgba(150,255,150) 100%);");

		Scene scene = new Scene(pane, 1500, 945);
		stage.setTitle("Hangman!");
		stage.setScene(scene);

		startButton.setOnMouseClicked((EventHandler<Event>) event -> {
			try {
				cleanup();
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