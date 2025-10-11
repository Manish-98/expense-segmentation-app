package com.expense.segmentation.service;

import com.expense.segmentation.dto.CategoryResponse;
import com.expense.segmentation.dto.CreateCategoryRequest;
import com.expense.segmentation.exception.DuplicateResourceException;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.CategoryMapper;
import com.expense.segmentation.model.Category;
import com.expense.segmentation.repository.CategoryRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> getAllActiveCategories() {
        log.debug("Fetching all active categories");
        List<Category> categories = categoryRepository.findByActiveTrueOrderByName();
        return categoryMapper.toResponseList(categories);
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.debug("Creating new category: {}", request.getName());

        // Check if category already exists
        if (categoryRepository.existsByNameAndActive(request.getName().trim(), true)) {
            throw new DuplicateResourceException(
                    "Category with name '" + request.getName().trim() + "' already exists");
        }

        Category category = new Category();
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setActive(true);

        category = categoryRepository.save(category);
        log.info("Created category: {} with ID: {}", category.getName(), category.getId());

        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void deactivateCategory(UUID categoryId) {
        log.debug("Deactivating category: {}", categoryId);

        Category category =
                categoryRepository
                        .findById(categoryId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Category not found with ID: " + categoryId));

        category.setActive(false);
        categoryRepository.save(category);
        log.info("Deactivated category: {}", categoryId);
    }
}
