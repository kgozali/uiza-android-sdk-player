package vn.uiza.data;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by www.muathu@gmail.com on 10/21/2017.
 */

public class EventBusData {


    // Bill Pugh Singleton Implementation
    private static class EventBusDataHelper {
        private static final EventBusData INSTANCE = new EventBusData();
    }

    public static EventBusData getInstance() {
        return EventBusDataHelper.INSTANCE;
    }

    private EventBusData() {
    }

    //

    public static class ConnectEvent {
        private boolean isConnected;
        private boolean isConnectedFast;
        private boolean isConnectedWifi;
        private boolean isConnectedMobile;

        public boolean isConnected() {
            return isConnected;
        }

        public void setConnected(boolean connected) {
            isConnected = connected;
        }

        public boolean isConnectedFast() {
            return isConnectedFast;
        }

        public void setConnectedFast(boolean connectedFast) {
            isConnectedFast = connectedFast;
        }

        public boolean isConnectedWifi() {
            return isConnectedWifi;
        }

        public void setConnectedWifi(boolean connectedWifi) {
            isConnectedWifi = connectedWifi;
        }

        public boolean isConnectedMobile() {
            return isConnectedMobile;
        }

        public void setConnectedMobile(boolean connectedMobile) {
            isConnectedMobile = connectedMobile;
        }
    }

    //postSticky can noti when activity onpause
    public void sendConnectChange(boolean isConnected, boolean isConnectedFast, boolean isConnectedWifi, boolean isConnectedMobile) {
        ConnectEvent connectEvent = new ConnectEvent();
        connectEvent.setConnected(isConnected);
        connectEvent.setConnectedFast(isConnectedFast);
        connectEvent.setConnectedWifi(isConnectedWifi);
        connectEvent.setConnectedMobile(isConnectedMobile);
        EventBus.getDefault().postSticky(connectEvent);
    }
}
