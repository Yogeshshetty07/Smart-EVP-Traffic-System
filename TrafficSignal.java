public class TrafficSignal {
    private SignalColor currentColor = SignalColor.RED;
    private volatile boolean isEmergency = false;
    public enum SignalColor {
        RED, YELLOW, GREEN
    }
    public void startNormalOperation() {
        while (true) {
            if (!isEmergency) {
                switch (currentColor) {
                    case RED:
                        System.out.println("Signal: RED");
                        sleep(3000);
                        currentColor = SignalColor.YELLOW;
                        break;
                    case YELLOW:
                        System.out.println("Signal: YELLOW");
                        sleep(2000);
                        currentColor = SignalColor.GREEN;
                        break;
                    case GREEN:
                        System.out.println("Signal: GREEN");
                        sleep(3000);
                        currentColor = SignalColor.RED;
                        break;
                }
            } else {
                sleep(100); // Brief pause while emergency is handled
            }
        }
    }

    public void handleEmergency(EmergencyVehicle vehicle) {
        if (vehicle != EmergencyVehicle.NONE) {
            isEmergency = true;
            System.out.println("🚨 EMERGENCY VEHICLE DETECTED: " + vehicle);
            System.out.println("⚠️ Switching signal to GREEN for 10 seconds...");
            currentColor = SignalColor.GREEN;
            System.out.println("Signal: GREEN (EMERGENCY)");
            sleep(10000);
            isEmergency = false;
            System.out.println("✅ Emergency over. Returning to normal operation...");
        } else {
            System.out.println("Continuing normal signal operation...");
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
