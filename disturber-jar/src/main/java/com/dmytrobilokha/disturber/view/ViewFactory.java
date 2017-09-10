package com.dmytrobilokha.disturber.view;

import com.dmytrobilokha.disturber.boot.FXMLLoaderProducer;
import com.dmytrobilokha.disturber.controller.ChatTabController;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by dimon on 10.09.17.
 */
@ApplicationScoped
public class ViewFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ViewFactory.class);
    private static final String FXMLS_LOCATION = "/fxml/";
    private static final String CHAT_TAB_FXML = FXMLS_LOCATION + "ChatTab.fxml";

    private FXMLLoaderProducer fxmlLoaderProducer;

    protected ViewFactory() {
        //No args constructor to keep CDI framework happy
    }

    @Inject
    public ViewFactory(FXMLLoaderProducer fxmlLoaderProducer) {
        this.fxmlLoaderProducer = fxmlLoaderProducer;
    }

    public Tab produceChatTab(RoomKey roomKey) {
        FXMLLoader fxmlLoader = fxmlLoaderProducer.produce();
        fxmlLoader.setLocation(getClass().getResource(CHAT_TAB_FXML));
        Tab chatTab;
        try {
            chatTab = fxmlLoader.load();
            ChatTabController chatTabController =  fxmlLoader.getController();
            chatTabController.setRoomToFollow(roomKey);
        } catch (IOException ex) {
            LOG.error("Failed to load chat tab FXML for {} because of input/output error", roomKey, ex);
            throw new RuntimeException("Failed to load chat tab FXML for " + roomKey + " because of input/output error");
        }
        return chatTab;
    }

}
