package com.example.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.domain.LakeProfile;

public interface LakeProfileRepository extends CrudRepository<LakeProfile, Long> {
}
