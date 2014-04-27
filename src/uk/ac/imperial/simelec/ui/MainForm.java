package uk.ac.imperial.simelec.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import uk.ac.imperial.simelec.SimElec;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Main form for SimElec model
 * 
 * @author James Keirstead
 * 
 */
public class MainForm implements javafx.fxml.Initializable {
	@FXML
	private ChoiceBox<Integer> cbxResidents;
	@FXML
	private ChoiceBox<String> cbxMonth;
	@FXML
	private ChoiceBox<String> cbxDayOfWeek;
	@FXML
	private Button btnOutdir;
	@FXML
	private TextField txfOutdir;
	@FXML
	private CheckBox chbLighting;
	@FXML
	private CheckBox chbAppliances;
	@FXML
	private CheckBox chbRPlots;
	@FXML
	private Button btnRunSimElec;
	@FXML
	private Label lblStatus;
	@FXML
	private Label lblAbout;
	@FXML
	private MenuBar menuBar;
	@FXML
	private CheckBox chbApplianceTotals;
	@FXML
	private CheckBox chbLightTotals;

	private Stage stage;
	private StringProperty statusText;

	public void initialize(URL location, ResourceBundle resources) {

		cbxResidents.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
		cbxMonth.setItems(FXCollections.observableArrayList("January",
				"February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December"));
		cbxDayOfWeek.setItems(FXCollections.observableArrayList("Weekday",
				"Weekend"));

		cbxResidents.setValue(1);
		cbxMonth.setValue("January");
		cbxDayOfWeek.setValue("Weekday");
		
		statusText = new SimpleStringProperty("");
		lblStatus.textProperty().bind(statusText);

		/*
		 * Make sure that "total" options grey out when the models are turned off
		 */
		chbLighting.setOnAction(new EventHandler<ActionEvent> () {
			public void handle(ActionEvent arg0) {
				chbLightTotals.setDisable(!chbLighting.isSelected());
			}			
		});
		
		chbAppliances.setOnAction(new EventHandler<ActionEvent> () {
			public void handle(ActionEvent arg0) {
				chbApplianceTotals.setDisable(!chbAppliances.isSelected());
			}			
		});
		
		Label menuLabel = new Label("About");
		menuLabel.setStyle("-fx-padding: 0px;");
		menuLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				Stage myDialog = new Stage();
				myDialog.initModality(Modality.WINDOW_MODAL);
				try {
					AboutUI about = new AboutUI();
					about.start(myDialog);
					myDialog.show();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		Menu fileMenuButton = new Menu();
		fileMenuButton.setGraphic(menuLabel);
		menuBar.getMenus().add(fileMenuButton);

		btnOutdir.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {

				DirectoryChooser dirChooser = new DirectoryChooser();
				File home = new File(System.getProperty("user.home"));
				dirChooser.setInitialDirectory(home);
				File selectedDir = dirChooser.showDialog(stage);
				if (selectedDir != null)
					txfOutdir.setText(selectedDir.getAbsolutePath());

			}
		});

		btnRunSimElec.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				final int residents = cbxResidents.getValue();
				String strMonth = cbxMonth.getValue();
				Calendar cal = Calendar.getInstance();
				try {
					cal.setTime(new SimpleDateFormat("MMM").parse(strMonth));
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				final int month = cal.get(Calendar.MONTH) + 1;
				final boolean weekend = cbxDayOfWeek.getValue().equals("Weekend");
				final String out_dir = txfOutdir.getText();				
				final boolean runLighting = chbLighting.isSelected();
				final boolean runAppliances = chbAppliances.isSelected();
				final boolean runRPlots = chbRPlots.isSelected();

				if (out_dir == null || out_dir.equals("")) {
					lblStatus.setTextFill(Color.RED);
					lblStatus.setText("Please select an output directory");

				} else {

					lblStatus.setTextFill(Color.BLACK);
					
					Task<Void> task = new Task<Void>() {

						@Override
						protected Void call() throws Exception {
							updateMessage("Starting model run...");
							
							SimElec model = new SimElec(month, residents, weekend,
									out_dir);
							model.setRunAppliances(runAppliances);
							model.setRunLighting(runLighting);
							model.setMakeRPlots(runRPlots);
							model.setLightingTotalsOnly(chbLightTotals.isSelected());
							model.setAppliancesTotalsOnly(chbApplianceTotals.isSelected());

							try {
								model.run();
							} catch (IOException io) {
								io.printStackTrace();
							}
							
							updateMessage("Model run complete");
							
							return null;
						}
					};
					
					lblStatus.textProperty().bind(task.messageProperty());

					Thread thread = new Thread(task);
					thread.setDaemon(true);
					thread.start();
				}
			}
		});

		
	}

	public void setStage(Stage primaryStage) {
		this.stage = primaryStage;

	}
}
