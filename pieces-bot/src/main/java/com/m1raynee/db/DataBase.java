package com.m1raynee.db;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.m1raynee.db.dto.PieceAmountDTO;
import com.m1raynee.db.entity.Box;
import com.m1raynee.db.entity.Piece;
import com.m1raynee.db.entity.PieceAction;
import com.m1raynee.db.entity.Student;
import com.m1raynee.telegram.utils.Pageable;

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
                                        select new com.m1raynee.db.dto.PieceAmountDTO(p.id,
                                                SUM(case pa.actionState
                                                        when 'I' then pa.amount
                                                else 0 end),
                                                SUM(case pa.actionState
                                                        when 'T' then pa.amount
                                                        when 'L' then pa.amount
                                                else 0 end))
                                        from Piece p
                                        left join p.actions pa
                                        join p.box b
                                        where b.index=:index
                                        group by p.id
                                        """,
                                        PieceAmountDTO.class)
                                        .setParameter("index", box_index)
                                        .getResultList();

                        Map<Long, PieceAmountDTO> stockMap = amounts.stream()
                                        .collect(Collectors.toMap(PieceAmountDTO::getPieceId, dto -> dto));

                        Box resultBox = session
                                        .createSelectionQuery(
                                                        "from Box b left join fetch b.pieces where b.index=:index",
                                                        Box.class)
                                        .setParameter("index", box_index).getSingleResultOrNull();

                        for (Piece piece : resultBox.getPieces()) {
                                piece.setPositiveAmount(stockMap.get(piece.getId()).getPositiveAmount());
                                piece.setNegativeAmount(stockMap.get(piece.getId()).getNegativeAmount());
                        }
                        return resultBox;
                });
        }

        public static Piece fetchPieceActionsPaginated(long piece_id, Pageable page) {
                return fetchPieceActionsPaginated(
                                HibernateConfiguration.getSessionFactory()
                                                .fromSession(session -> session.find(Piece.class, piece_id)),
                                page);
        }

        public static Piece fetchPieceActionsPaginated(Piece piece, Pageable page) {
                String hql = "select pa from PieceAction pa where pa.piece.id = :pieceId order by created_at desc";

                List<PieceAction> actions = HibernateConfiguration.getSessionFactory().fromSession(session -> {
                        return session.createSelectionQuery(hql, PieceAction.class)
                                        .setParameter("pieceId", piece.getId())
                                        .setFirstResult(page.startI())
                                        .setMaxResults(page.size)
                                        .getResultList();
                });
                piece.setTrActions(actions);
                return piece;
        }
}
