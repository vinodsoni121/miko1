import java.io.*;
import java.util.*;

class Database {
    public static void createTable(String tableName, List<String> columns) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("table_name", tableName);
        metadata.put("columns", columns);

        try (FileWriter writer = new FileWriter(tableName + "_metadata.txt")) {
            writer.write(metadata.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void insertIntoTable(String tableName, List<String> values) {
        File metadataFile = new File(tableName + "_metadata.txt");
        if (!metadataFile.exists()) {
            System.out.println("Table '" + tableName + "' does not exist.");
            return;
        }

        try (Scanner scanner = new Scanner(metadataFile)) {
            String metadataStr = scanner.nextLine();
            Map<String, Object> metadata = parseMetadata(metadataStr);

            if (values.size() != ((List<String>) metadata.get("columns")).size()) {
                System.out.println("Number of values does not match the number of columns.");
                return;
            }

            Map<String, String> record = new HashMap<>();
            List<String> columns = (List<String>) metadata.get("columns");
            for (int i = 0; i < columns.size(); i++) {
                record.put(columns.get(i), values.get(i));
            }

            try (FileWriter writer = new FileWriter(tableName + ".txt", true)) {
                writer.write(record.toString() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Object> parseMetadata(String metadataStr) {
        metadataStr = metadataStr.replace("{", "").replace("}", "");
        Map<String, Object> metadata = new HashMap<>();
        String[] pairs = metadataStr.split(", ");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            if (key.equals("columns")) {
                List<String> columns = new ArrayList<>(Arrays.asList(value.split(",")));
                for (int i = 0; i < columns.size(); i++) {
                    columns.set(i, columns.get(i).trim());
                }
                metadata.put(key, columns);
            } else {
                metadata.put(key, value);
            }
        }
        return metadata;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter SQL statement: ");
            String query = scanner.nextLine().trim();

            if (query.equalsIgnoreCase("exit")) {
                break;
            }

            if (query.toLowerCase().startsWith("create table")) {
                String tableName = query.split(" ")[2];
                List<String> columns = Arrays.asList(query.split("\\(")[1].split("\\)")[0].split(","));
                for (int i = 0; i < columns.size(); i++) {
                    columns.set(i, columns.get(i).trim());
                }
                createTable(tableName, columns);
                System.out.println("Table '" + tableName + "' created.");
            } else if (query.toLowerCase().startsWith("insert into")) {
                String tableName = query.split(" ")[2];
                List<String> values = Arrays
                        .asList(query.substring(query.indexOf("(") + 1, query.indexOf(")")).split(","));
                for (int i = 0; i < values.size(); i++) {
                    values.set(i, values.get(i).trim());
                }
                insertIntoTable(tableName, values);
                System.out.println("Record inserted into table '" + tableName + "'.");
            } else {
                System.out.println("Invalid SQL statement.");
            }
        }
    }
}
