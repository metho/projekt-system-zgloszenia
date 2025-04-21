package com.puw.systemzgloszen.repository;

import com.puw.systemzgloszen.entity.Ticket;
import com.puw.systemzgloszen.entity.UserComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCommentRepository extends JpaRepository<UserComment, Long> {

    List<UserComment> findByTicket(Ticket ticket);
}
