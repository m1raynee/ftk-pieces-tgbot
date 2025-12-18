package com.m1raynee.db.scripts;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.m1raynee.db.HibernateConfiguration;
import com.m1raynee.db.entity.Box;

public class BoxCreationScript {
    private static final String DELIMITER = ",";
    private static final String FILENAME = "C:\\Users\\peche\\OneDrive\\Документы\\Github\\ftk-pieces-tgbot\\pieces-bot\\src\\main\\resources\\boxes.csv";

    public static void main(String[] args) {
        HibernateConfiguration.getSessionFactory().inStatelessSession(session -> {
            try (BufferedReader r = new BufferedReader(new FileReader(FILENAME))) {
                String line;
                boolean isHeader = true;
                while ((line = r.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }

                    String[] values = line.split(DELIMITER);
                    if (values.length < 3) {
                        System.out.println("Неверная строка: " + line.strip());
                        continue;
                    }

                    Integer index = Integer.valueOf(values[0].trim());
                    String name = values[1].trim();
                    String placeCode = values[2].trim();

                    session.insert(new Box(index, name, placeCode));
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
