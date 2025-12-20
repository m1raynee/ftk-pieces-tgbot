package com.m1raynee.db;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.m1raynee.db.dto.PieceAmountDTO;
import com.m1raynee.db.entity.Box;
import com.m1raynee.db.entity.Piece;
import com.m1raynee.db.entity.Student;

public final class Database {
    private Database() {
    }

    public static Student createStudent(String name) {
        Student student = new Student(name);
        HibernateConfiguration.getSessionFactory().inTransaction(session -> session.persist(student));
        return student;
    }

    public static Box fetchBoxPreview(Integer box_index) {
        return HibernateConfiguration.getSessionFactory().fromSession(session -> {
            List<PieceAmountDTO> amounts = session.createSelectionQuery("""
                    select new com.m1raynee.db.dto.PieceAmountDTO(p.id, SUM(pa.amount))
                    from Piece p
                    left join p.actions pa
                    join p.box b
                    where b.index=:index
                    group by p.id
                    """,
                    PieceAmountDTO.class)
                    .setParameter("index", box_index)
                    .getResultList();

            Map<Long, Long> stockMap = amounts.stream()
                    .collect(Collectors.toMap(PieceAmountDTO::getPieceId,
                            dto -> dto.getCurrentAmount() != null ? dto.getCurrentAmount() : 0L));

            Box resultBox = session
                    .createSelectionQuery("from Box b left join fetch b.pieces where b.index=:index", Box.class)
                    .setParameter("index", box_index).getSingleResultOrNull();

            for (Piece piece : resultBox.getPieces()) {
                piece.setCalculatedAmount(stockMap.get(piece.getId()));
            }
            return resultBox;
        });
    }
}
