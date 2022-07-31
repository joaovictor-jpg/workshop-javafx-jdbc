package gui;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Main;
import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Seller;
import model.services.SellerService;

public class SellerListController implements Initializable, DataChangeListener {

	private SellerService service;
	private ObservableList<Seller> obsList;
	@FXML
	private TableView<Seller> tableViewSeller;
	@FXML
	private TableColumn<Seller, Integer> tableViewId;
	@FXML
	private TableColumn<Seller, String> tableViewName;
	@FXML
	private TableColumn<Seller, String> tableViewEmail;
	@FXML
	private TableColumn<Seller, Date> tableViewBirthDate;
	@FXML
	private TableColumn<Seller, Double> tableViewBaseSalary;
	@FXML
	private TableColumn<Seller, Seller> tableColumnEDIT;
	@FXML
	private TableColumn<Seller, Seller> tableColumnREMOVE;
	@FXML
	private Button btNew;

	public void setSellerService(SellerService service) {
		this.service = service;
	}

	@FXML
	public void onBtNewAction(ActionEvent event) {
		System.out.println("onBtNewAction");
		Stage parentStage = Utils.currentStage(event);
		Seller obj = new Seller();
		createDialogForm(obj, "/gui/SellerForm.fxml", parentStage);
	}

	@Override
	public void initialize(URL uri, ResourceBundle rb) {
		initalizeNode();
	}

	private void initalizeNode() {
		tableViewId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableViewName.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableViewEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		tableViewBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
		Utils.formatTableColumnDate(tableViewBirthDate, "dd/MM/yyyy");
		tableViewBaseSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
		Utils.formatTableColumnDouble(tableViewBaseSalary, 2);

		Stage stage = (Stage) Main.getMainScene().getWindow();
		tableViewSeller.prefHeightProperty().bind(stage.heightProperty());
	}

	public void updatetableView() {
		if (service == null) {
			throw new IllegalStateException("Service was null");
		}
		List<Seller> list = service.findAll();
		obsList = FXCollections.observableArrayList(list);
		tableViewSeller.setItems(obsList);
		initEditButtons();
		initRemoveButtons();
	}

	private void createDialogForm(Seller obj, String absoluteName, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();

			SellerFormController controller = loader.getController();
			controller.setSeller(obj);
			controller.setSellerService(new SellerService());
			controller.subscribeDataChangeListeners(this);
			controller.updateFormData();

			Stage dialogForm = new Stage();
			dialogForm.setTitle("Enter Seller data");
			dialogForm.setScene(new Scene(pane));
			dialogForm.setResizable(false);
			dialogForm.initOwner(parentStage);
			dialogForm.initModality(Modality.WINDOW_MODAL);
			dialogForm.showAndWait();

		} catch (IOException e) {
			Alerts.showAlert("IO Exception", "Error loading view", e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChenged() {
		updatetableView();
	}

	private void initEditButtons() {
		tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEDIT.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("edit");

			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> createDialogForm(obj, "/gui/SellerForm.fxml", Utils.currentStage(event)));
			}
		});
	}

	private void initRemoveButtons() {
		tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnREMOVE.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("remove");

			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> removeEntity(obj));
			}
		});
	}

	protected void removeEntity(Seller obj) {
		Optional<ButtonType> result = Alerts.showConfirmation("Confirmation", "Are you sure to delete?");
		if (result.get() == ButtonType.OK) {
			if (service == null) {
				throw new IllegalStateException("Service was null");
			}

			try {
				service.remove(obj);
				updatetableView();
			} catch (DbException e) {
				Alerts.showAlert("Error removing object", null, e.getMessage(), AlertType.ERROR);
			}
		}
	}

}
