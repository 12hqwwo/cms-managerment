package com.project.WebAloTra.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import com.project.WebAloTra.entity.Branch;
import com.project.WebAloTra.repository.BranchRepository;
import com.project.WebAloTra.service.BranchService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BranchServiceImpl implements BranchService {

    @Autowired
    private BranchRepository branchRepository;

    @Override
    @Transactional
    public Branch createBranch(Branch branch) {
        branch.setCreateDate(LocalDateTime.now());
        branch.setUpdateDate(LocalDateTime.now());
        branch.setActive(true);
        Branch savedBranch = branchRepository.save(branch);
        branchRepository.flush();
        return savedBranch;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Branch> getBranchById(Long id) {
        return branchRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Branch> getBranchByCode(String branchCode) {
        return branchRepository.findByBranchCode(branchCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    @Override
    @Transactional
    public Branch updateBranch(Long id, Branch branchDetails) {
        Optional<Branch> optionalBranch = branchRepository.findById(id);
        if (optionalBranch.isPresent()) {
            Branch branch = optionalBranch.get();
            if (branchDetails.getBranchName() != null) {
                branch.setBranchName(branchDetails.getBranchName());
            }
            if (branchDetails.getAddress() != null) {
                branch.setAddress(branchDetails.getAddress());
            }
            if (branchDetails.getPhone() != null) {
                branch.setPhone(branchDetails.getPhone());
            }
            if (branchDetails.getEmail() != null) {
                branch.setEmail(branchDetails.getEmail());
            }
            branch.setUpdateDate(LocalDateTime.now());
            Branch updatedBranch = branchRepository.save(branch);
            branchRepository.flush();
            return updatedBranch;
        }
        return null;
    }





    @Override
    @Transactional
    public void deleteBranch(Long id) {
        try {
            branchRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("KhÄ‚Â´ng thĂ¡Â»Æ’ xÄ‚Â³a chi nhÄ‚Â¡nh nÄ‚Â y vÄ‚Â¬ Ă„â€˜ang chĂ¡Â»Â©a dĂ¡Â»Â¯ liĂ¡Â»â€¡u rÄ‚Â ng buĂ¡Â»â„¢c (TÄ‚Â i khoĂ¡ÂºÂ£n, HÄ‚Â³a Ă„â€˜Ă†Â¡n, Kho...).");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Branch> getActiveBranches() {
        return getAllBranches().stream()
                .filter(Branch::isActive)
                .collect(Collectors.toList());
    }
}
