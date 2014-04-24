package uk.ac.imperial.simelec.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import uk.ac.imperial.simelec.SimElec;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
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

	private Stage stage;

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
				int residents = cbxResidents.getValue();
				String strMonth = cbxMonth.getValue();
				Calendar cal = Calendar.getInstance();
				try {
					cal.setTime(new SimpleDateFormat("MMM").parse(strMonth));
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				int month = cal.get(Calendar.MONTH) + 1;
				boolean weekend = cbxDayOfWeek.getValue().equals("Weekend");
				String out_dir = txfOutdir.getText();

				boolean runLighting = chbLighting.isSelected();
				boolean runAppliances = chbAppliances.isSelected();
		//		boolean runRPlots = chbRPlots.isSelected();

				if (out_dir == null || out_dir.equals("")) {
					lblStatus.setTextFill(Color.RED);
					lblStatus.setText("Please select an output directory");

				} else {
					
					SimElec model = new SimElec(month, residents, weekend,
							out_dir);
					model.setRunAppliances(runAppliances);
					model.setRunLighting(runLighting);
					// model.makeRplots(runRPlots);

					try {
						model.run();
					} catch (IOException io) {
						io.printStackTrace();
					}

					lblStatus.setTextFill(Color.BLACK);
					lblStatus.setText("Model run complete");
				}
			}
		});
		
		chbRPlots.setDisable(true);
	}

	public void setStage(Stage primaryStage) {
		this.stage = primaryStage;

	}
}
