package com.project.WebAloTra.service;

import java.util.List;
import java.util.Optional;

import com.project.WebAloTra.entity.Branch;

public interface BranchService {
    Branch createBranch(Branch branch);
    Optional<Branch> getBranchById(Long id);
    Optional<Branch> getBranchByCode(String branchCode);
    List<Branch> getAllBranches();
    Branch updateBranch(Long id, Branch branch);
    void deleteBranch(Long id);
    List<Branch> getActiveBranches();
}
