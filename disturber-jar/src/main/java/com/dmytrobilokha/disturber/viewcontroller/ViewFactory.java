package com.dmytrobilokha.disturber.viewcontroller;

import com.dmytrobilokha.disturber.SystemMessage;
import com.dmytrobilokha.disturber.boot.FXMLLoaderProducer;
import com.dmytrobilokha.disturber.viewcontroller.main.MainLayoutController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * The factory to create views on-demand
 */
@ApplicationScoped
public class ViewFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ViewFactory.class);
    private static final String MAIN_LAYOUT_FXML = "main/MainLayout.fxml";

    private FXMLLoaderProducer fxmlLoaderProducer;
    private ResourceBundle messageBundle;

    protected ViewFactory() {
        //No args constructor to keep CDI framework happy
    }

    @Inject
    public ViewFactory(FXMLLoaderProducer fxmlLoaderProducer) {
        this.fxmlLoaderProducer = fxmlLoaderProducer;
        messageBundle = fxmlLoaderProducer.getMessageBundle();
    }

    public void showErrorAlert(SystemMessage message) {
        createAlert(message).showAndWait();
    }

    public DialogButton showErrorDialog(SystemMessage message, DialogButton... buttons) {
        Alert alert = createAlert(message);
        ButtonType[] buttonTypes = mapToFxButtons(buttons);
        List<ButtonType> errorDialogButtonTypes = alert.getDialogPane().getButtonTypes();
        errorDialogButtonTypes.clear();
        errorDialogButtonTypes.addAll(Arrays.asList(buttonTypes));
        Optional<ButtonType> pressed = alert.showAndWait();
        return whichPressed(pressed, buttonTypes, buttons);
    }

    private Alert createAlert(SystemMessage message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(messageBundle.getString("error"));
        alert.setHeaderText(null);
        alert.setContentText(message.getMessage());
        Label label = new Label(messageBundle.getString("error.details"));
        TextArea textArea = new TextArea(message.getDetails());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);
        return alert;
    }

    private ButtonType[] mapToFxButtons(DialogButton[] buttons) {
        ButtonType[] buttonTypes = new ButtonType[buttons.length];
        for (int i = 0; i < buttons.length; i++) {
            ButtonType buttonType = new ButtonType(messageBundle.getString(buttons[i].getLabelKey()));
            buttonTypes[i] = buttonType;
        }
        return buttonTypes;
    }

    private DialogButton whichPressed(Optional<ButtonType> pressed, ButtonType[] buttonTypes, DialogButton[] buttons) {
        if (pressed.isPresent()) {
            ButtonType pressedType = pressed.get();
            for (int i = 0; i < buttons.length; i++) {
                if (pressedType == buttonTypes[i])
                    return buttons[i];
            }
        }
        return DialogButton.NONE;
    }

    public <T> ObservableList<T> createList() {
        return FXCollections.observableArrayList();
    }

    public <T> TreeItem<T> createTreeRoot() {
        return new TreeItem<>();
    }

    public <T> TreeItem<T> createTreeItem(T value, TreeItem<T> parent) {
        TreeItem<T> treeItem = new TreeItem<>(value);
        treeItem.setExpanded(true);
        parent.getChildren().add(treeItem);
        return treeItem;
    }

    public <T> void updateView(TreeItem<T> treeItem) {
        TreeItem.TreeModificationEvent<T> event = new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), treeItem);
        Event.fireEvent(treeItem, event);
    }

    public Image createIcon(AppIcon icon) {
        return new Image(icon.getLocation());
    }

    public TextField createSelectableField(String content) {
        TextField field = new TextField(content);
        field.setEditable(false);
        return field;
    }

    public ViewControllerHolder<Parent, MainLayoutController> produceMainLayout() {
        return load(MAIN_LAYOUT_FXML);
    }

    private <V, C> ViewControllerHolder<V, C> load(String fxmlLocation) {
        FXMLLoader fxmlLoader = fxmlLoaderProducer.produce();
        fxmlLoader.setLocation(getClass().getResource(fxmlLocation));
        try {
            V view = fxmlLoader.load();
            C controller =  fxmlLoader.getController();
            return new ViewControllerHolder<>(view, controller);
        } catch (IOException ex) {
            LOG.error("Failed to load view {} because of internal error", fxmlLocation, ex);
            throw new IllegalStateException("Failed to load view" + fxmlLocation + " because of internal error");
        }
    }

    public static class ViewControllerHolder<V, C> {
        private final V view;
        private final C controller;

        private ViewControllerHolder(V view, C controller) {
            this.view = view;
            this.controller = controller;
        }

        public V getView() {
            return view;
        }

        public C getController() {
            return controller;
        }
    }

}
