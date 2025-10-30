import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

class Main {

  private static final int FACIAL_DATA_SIZE = 6;
  private static final int FACIAL_DATA_PERMUTATIONS = 15;

  // Reads file and parses all the faces from it
  // Returns a map with the key being the filename and the value being the
  // facial data.
  private static Map<String, double[]> parseFacialInformation(String filename) {
    Map<String, double[]> dataEntries = new HashMap<>();

    // Scan with a BufferedReader for performance
    try (InputStream inputStream = new FileInputStream(filename)) {
      Reader reader = new BufferedReader(new InputStreamReader(inputStream));

      try (Scanner scanner = new Scanner(reader)) {
        String line;
        String currentFace = null;
        while (scanner.hasNextLine()) {
          line = scanner.nextLine();

          // Skip comments and blank lines
          if (line.startsWith("#") || line.isBlank()) {
            continue;
          } else if (line.startsWith("FACE")) {
            // Face filename
            currentFace = line.split(" ")[1];
          } else {
            if (currentFace == null) {
              System.err.println(
                "Error: you must specify the face filename before the data."
              );
              System.exit(1);
            }

            // Extract all data from line
            String[] strData = line.split("\\s+");
            double[] data = new double[strData.length];

            if (data.length != FACIAL_DATA_SIZE) {
              System.err.println(
                "Error: incorrect number of data points for a person."
              );
              System.exit(1);
            }

            try {
              for (int i = 0; i < data.length; i++) {
                data[i] = Double.parseDouble(strData[i]);
              }
            } catch (NumberFormatException e) {
              System.err.println("Error: number format incorrect.");
              System.exit(1);
            }

            dataEntries.put(currentFace, data);
          }
        }
      }
    } catch (IOException e) {
      System.err.format("I/O Error: %s\n", e.getMessage());
      System.exit(1);
    }

    return dataEntries;
  }

  /// Converts the raw facial data into the ratios that can be compared.
  public static Map<String, double[]> convertFacialData(
    Map<String, double[]> dataEntries
  ) {
    Map<String, double[]> result = new HashMap<>(dataEntries.size());
    for (Map.Entry<String, double[]> entry : dataEntries.entrySet()) {
      result.put(entry.getKey(), calculateFacialRatios(entry.getValue()));
    }

    return result;
  }

  /// Calculates all ratio combinations between the data points.
  public static double[] calculateFacialRatios(double[] data) {
    double[] ratios = new double[FACIAL_DATA_PERMUTATIONS];

    // Calculate all combinations of the dataset C(FACIAL_DATA_SIZE, 2)
    int dataPoint = 0;

    // Calculates these ratios:
    // A/B, A/C, A/D, A/E, A/F, B/C, B/D, B/E, B/F, C/D, C/E, C/F, D/E, D/F, E/F
    for (int i = 0; i < FACIAL_DATA_SIZE - 1; i++) {
      for (int i2 = i + 1; i2 < FACIAL_DATA_SIZE; i2++) {
        ratios[dataPoint] = data[i] / data[i2];
        dataPoint++;
      }
    }

    return ratios;
  }

  /// Parses file and returns computed facial data
  public static Map<String, double[]> parseFacialRatios(String filename) {
    Map<String, double[]> facialData = parseFacialInformation(filename);

    return convertFacialData(facialData);
  }

  /// Prompts user for a number
  public static double getDouble(Scanner scanner) {
    while (true) {
      try {
        return scanner.nextDouble();
      } catch (InputMismatchException e) {
        System.err.print("Error: you must enter a valid integer: ");
      } finally {
        scanner.nextLine();
      }
    }
  }

  /// Inputs a face from the user
  public static double[] inputFacialData() {
    double[] result = new double[FACIAL_DATA_SIZE];

    try (Scanner scanner = new Scanner(System.in)) {
      int counter = 0;

      System.out.print(
        "Enter the distance from top of head to bottom of chin: "
      );
      result[counter] = getDouble(scanner);
      counter++;
      System.out.print("Enter the distance from left ear to right ear: ");
      result[counter] = getDouble(scanner);
      counter++;
      System.out.print(
        "Enter the distance from center point between the eyes and top of head: "
      );
      result[counter] = getDouble(scanner);
      counter++;
      System.out.print(
        "Enter the distance from center of left eye to center of right eye: "
      );
      result[counter] = getDouble(scanner);
      counter++;
      System.out.print("Enter the length of nose from top to bottom: ");
      result[counter] = getDouble(scanner);
      counter++;
      System.out.print(
        "Enter the distance from bottom of chin to middle of mouth: "
      );
      result[counter] = getDouble(scanner);
    }
    return result;
  }

  /// Compares two faces - the lower the number, the better.
  public static double compareFacialRatios(
    double[] face1Ratios,
    double[] face2Ratios
  ) {
    double result = 0;

    for (int i = 0; i < face1Ratios.length; i++) {
      result += Math.pow(
        ((face2Ratios[i] - face1Ratios[i]) / face1Ratios[i]),
        2.0
      );
    }

    return result;
  }

  /// Finds the face of best match for searchRatios
  public static String findBestMatch(
    Map<String, double[]> datasetRatios,
    double[] searchRatios
  ) {
    double minDifference = Double.POSITIVE_INFINITY;
    String key = null;
    for (Map.Entry<String, double[]> entry : datasetRatios.entrySet()) {
      double difference = compareFacialRatios(entry.getValue(), searchRatios);
      if (difference < minDifference) {
        minDifference = difference;
        key = entry.getKey();
      }
    }

    return key;
  }

  public static void main(String[] args) {
    final String path = "data/faces.txt";

    Map<String, double[]> facialRatios = parseFacialRatios(path);

    double[] userFacialData = inputFacialData();
    double[] userFacialRatios = calculateFacialRatios(userFacialData);

    System.out.format(
      "Best match: %s\n",
      findBestMatch(facialRatios, userFacialRatios)
    );
  }
}
