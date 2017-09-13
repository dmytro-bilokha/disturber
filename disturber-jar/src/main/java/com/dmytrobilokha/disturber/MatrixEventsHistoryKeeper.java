package com.dmytrobilokha.disturber;

import com.dmytrobilokha.disturber.appeventbus.AppEvent;
import com.dmytrobilokha.disturber.appeventbus.AppEventBus;
import com.dmytrobilokha.disturber.appeventbus.AppEventListener;
import com.dmytrobilokha.disturber.appeventbus.AppEventType;
import com.dmytrobilokha.disturber.commonmodel.MatrixEvent;
import com.dmytrobilokha.disturber.commonmodel.RoomKey;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The service to store Matrix events history
 */
@ApplicationScoped
public class MatrixEventsHistoryKeeper {

    private final Map<RoomKey, List<MatrixEvent>> roomEventMap = new HashMap<>();
    private final AppEventListener<RoomKey, MatrixEvent> newMatrixEventListener = this::storeMatrixEvent;
    private final AppEventListener<String, Void> loginListener = this::clearHistoryOnLogin;

    protected MatrixEventsHistoryKeeper() {
        //No args constructor to keep CDI framework happy
    }

    @Inject
    public MatrixEventsHistoryKeeper(AppEventBus eventBus) {
        eventBus.subscribe(newMatrixEventListener, AppEventType.MATRIX_NEW_EVENT_GOT);
        eventBus.subscribe(loginListener, AppEventType.MATRIX_LOGGEDIN);
    }

    public List<MatrixEvent> getRoomEventsHistory(RoomKey roomKey) {
        List<MatrixEvent> roomEventsHistory = roomEventMap.get(roomKey);
        if (roomEventsHistory == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(roomEventsHistory);
    }

    private void clearHistoryOnLogin(AppEvent<String, Void> loginEvent) {
        String userId = loginEvent.getClassifier();
        for (Iterator<RoomKey> keyIterator = roomEventMap.keySet().iterator(); keyIterator.hasNext();) {
            RoomKey roomKey = keyIterator.next();
            if (userId.equals(roomKey.getUserId()))
                keyIterator.remove();
        }
    }

    private void storeMatrixEvent(AppEvent<RoomKey, MatrixEvent> appEvent) {
        roomEventMap.computeIfAbsent(appEvent.getClassifier(), roomKey -> new ArrayList<>()).add(appEvent.getPayload());
    }

    public void eagerInit(@Observes @Initialized(ApplicationScoped.class) Object initEvent) {
        //The methods does nothing. We need it just to ensure a CDI framework initializes the bean eagerly, so we won't
        //miss Matrix events in our history.
    }

}
