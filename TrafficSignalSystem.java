import java.util.Scanner;

public class TrafficSignalSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TrafficSignal trafficSignal = new TrafficSignal();
        
        // Start normal operation in a separate thread
        Thread normalOperationThread = new Thread(() -> {
            trafficSignal.startNormalOperation();
        });
        normalOperationThread.start();

        while (true) {
            System.out.println("\nSelect emergency vehicle type (or 'exit' to quit):");
            System.out.println("1. Ambulance");
            System.out.println("2. Fire Truck");
            System.out.println("3. Police Vehicle");
            System.out.println("4. Government Vehicle");
            System.out.println("5. No Emergency (Continue normal operation)");
            System.out.println("6. Exit");

            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit") || input.equals("6")) {
                System.out.println("Exiting program...");
                System.exit(0);
            }

            EmergencyVehicle selectedVehicle = EmergencyVehicle.NONE;
            switch (input) {
                case "1":
                    selectedVehicle = EmergencyVehicle.AMBULANCE;
                    break;
                case "2":
                    selectedVehicle = EmergencyVehicle.FIRE_TRUCK;
                    break;
                case "3":
                    selectedVehicle = EmergencyVehicle.POLICE_VEHICLE;
                    break;
                case "4":
                    selectedVehicle = EmergencyVehicle.GOVERNMENT_VEHICLE;
                    break;
                case "5":
                    selectedVehicle = EmergencyVehicle.NONE;
                    break;
                default:
                    System.out.println("Invalid input. Please try again.");
                    continue;
            }

            trafficSignal.handleEmergency(selectedVehicle);
        }
    }
} 