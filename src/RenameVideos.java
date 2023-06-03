import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RenameVideos {
    private static HashMap<String, Integer> existingFiles = new HashMap<>();

    private static String getNewName(String path, String name) {
        int suffix = 1;
        String baseName = name;
        String newName = Paths.get(path, name + ".mp4").toString();
        while (existingFiles.containsKey(newName)) {
            newName = Paths.get(path, baseName + "_" + suffix++ + ".mp4").toString();
        }
        existingFiles.put(newName, 1);
        return newName;
    }

    private static void renameFiles(String videoPath, String csvPath, int startRow) {
        try {
            List<Path> videoFiles = new ArrayList<>();
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(videoPath))) {
                for (Path path : directoryStream) {
                    if (!Files.isDirectory(path) && path.toString().endsWith(".mp4")) {
                        videoFiles.add(path);
                    }
                }
            }
            videoFiles.sort(Comparator.comparing(Path::toString));

            int numVideos = videoFiles.size();
            int numLines = (int) Files.lines(Paths.get(csvPath)).count();

            System.out.println("Number of video files: " + numVideos);
            System.out.println("Number of csv rows: " + numLines);

            System.out.println("Do you want to proceed with renaming? (yes/no): ");
            Scanner scanner = new Scanner(System.in);
            String confirmation = scanner.nextLine();
            if (!confirmation.equalsIgnoreCase("yes")) {
                return;
            }

            BufferedReader csvReader = new BufferedReader(new FileReader(csvPath));
            String row;
            int currentRow = 0;
            for (Path video : videoFiles) {
                if ((row = csvReader.readLine()) == null) {
                    break;
                }
                currentRow++;
                if (currentRow < startRow) continue;

                String newName = getNewName(videoPath, row);
                Files.move(video, Paths.get(newName));
                System.out.println("Renamed: " + video.getFileName() + " to " + Paths.get(newName).getFileName());
            }
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter path to video directory: ");
        String videoPath = scanner.nextLine();

        System.out.println("Enter path to csv file: ");
        String csvPath = scanner.nextLine();

        System.out.println("Enter start row (default 1): ");
        String startRowStr = scanner.nextLine();
        int startRow = 1;
        if (!startRowStr.isEmpty()) {
            startRow = Integer.parseInt(startRowStr);
        }

        try {
            Files.walk(Paths.get(videoPath)).forEach(path -> existingFiles.put(path.toString(), 1));
        } catch (IOException e) {
            e.printStackTrace();
        }

        renameFiles(videoPath, csvPath, startRow);
    }
}
