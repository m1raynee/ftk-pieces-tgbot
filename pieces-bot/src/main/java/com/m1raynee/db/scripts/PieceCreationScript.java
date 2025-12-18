package com.m1raynee.db.scripts;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.m1raynee.db.HibernateConfiguration;
import com.m1raynee.db.entity.Box;
import com.m1raynee.db.entity.Piece;

public class PieceCreationScript {
    private static final String DELIMITER = ";";
    private static final String FILENAME = "C:\\Users\\peche\\OneDrive\\Документы\\Github\\ftk-pieces-tgbot\\pieces-bot\\src\\main\\resources\\pieces.csv";

    public static void main(String[] args) {
        HibernateConfiguration.getSessionFactory().inTransaction(session -> {
            try (BufferedReader r = new BufferedReader(new FileReader(FILENAME))) {
                String line;
                boolean isHeader = true;
                while ((line = r.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }

                    String[] values = line.split(DELIMITER, -1);
                    if (values.length < 5) {
                        System.out.println("Неверная строка: " + line.strip());
                        continue;
                    }

                    Box box = null;
                    if (!values[3].isEmpty())
                        box = session.find(Box.class, Integer.valueOf(values[3]));

                    String article = values[0].trim();
                    String name = values[1].trim();
                    Integer amount = (!values[2].trim().isEmpty()) ? Integer.valueOf(values[2].trim()) : 0;
                    String altName = values[4].trim();

                    if (amount == 0)
                        System.err.println("Вставка нуля на " + name);

                    Piece piece = new Piece(
                            ((article != null) ? article : null),
                            name,
                            amount,
                            ((altName != null) ? altName : null),
                            box);
                    session.persist(piece);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
