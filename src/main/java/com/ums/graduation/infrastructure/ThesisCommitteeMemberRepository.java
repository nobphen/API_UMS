package com.ums.graduation.infrastructure;

import com.ums.graduation.domain.ThesisCommitteeMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThesisCommitteeMemberRepository extends JpaRepository<ThesisCommitteeMember, Long> {
}
