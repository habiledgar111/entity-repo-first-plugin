package io.github.repository;

import io.github.entity.Staffs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffsRepository extends JpaRepository<Staffs, Integer> {

}
