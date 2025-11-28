package com.jeondoksi.jeondoksi.domain.report.repository;

import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import com.jeondoksi.jeondoksi.domain.report.entity.Report;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAllByUser(User user);

    /**
     * 사용자의 최근 독후감 3건을 조회 (자가 복제 검사용)
     * 
     * @param user 사용자
     * @return 최근 독후감 3건 (최신순)
     */
    List<Report> findTop3ByUserOrderByCreatedAtDesc(User user);

    Optional<Report> findByBookAndUser(Book book, User user);
}
