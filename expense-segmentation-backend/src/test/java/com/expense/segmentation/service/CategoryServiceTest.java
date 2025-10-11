package com.expense.segmentation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.expense.segmentation.dto.CategoryResponse;
import com.expense.segmentation.dto.CreateCategoryRequest;
import com.expense.segmentation.exception.DuplicateResourceException;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.CategoryMapper;
import com.expense.segmentation.model.Category;
import com.expense.segmentation.repository.CategoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks private CategoryService categoryService;

    private Category testCategory;
    private CategoryResponse testCategoryResponse;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        testCategory = new Category();
        testCategory.setId(categoryId);
        testCategory.setName("Travel");
        testCategory.setDescription("Travel expenses");
        testCategory.setActive(true);

        testCategoryResponse = new CategoryResponse();
        testCategoryResponse.setId(categoryId);
        testCategoryResponse.setName("Travel");
        testCategoryResponse.setDescription("Travel expenses");
        testCategoryResponse.setActive(true);
        testCategoryResponse.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllActiveCategories_WithActiveCategories_ShouldReturnActiveCategories() {
        // Given
        Category inactiveCategory = new Category();
        inactiveCategory.setId(UUID.randomUUID());
        inactiveCategory.setName("Inactive");
        inactiveCategory.setActive(false);

        List<Category> allCategories = List.of(testCategory, inactiveCategory);
        List<Category> activeCategories = List.of(testCategory);
        List<CategoryResponse> expectedResponses = List.of(testCategoryResponse);

        when(categoryRepository.findByActiveTrueOrderByName()).thenReturn(activeCategories);
        when(categoryMapper.toResponseList(activeCategories)).thenReturn(expectedResponses);

        // When
        List<CategoryResponse> result = categoryService.getAllActiveCategories();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Travel");
        verify(categoryRepository).findByActiveTrueOrderByName();
        verify(categoryMapper).toResponseList(activeCategories);
    }

    @Test
    void getAllActiveCategories_WithNoActiveCategories_ShouldReturnEmptyList() {
        // Given
        Category inactiveCategory = new Category();
        inactiveCategory.setId(UUID.randomUUID());
        inactiveCategory.setName("Inactive");
        inactiveCategory.setActive(false);

        List<Category> allCategories = List.of(inactiveCategory);
        List<Category> activeCategories = List.of();
        List<CategoryResponse> expectedResponses = List.of();

        when(categoryRepository.findByActiveTrueOrderByName()).thenReturn(activeCategories);
        when(categoryMapper.toResponseList(activeCategories)).thenReturn(expectedResponses);

        // When
        List<CategoryResponse> result = categoryService.getAllActiveCategories();

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository).findByActiveTrueOrderByName();
        verify(categoryMapper).toResponseList(activeCategories);
    }

    @Test
    void createCategory_WithValidData_ShouldCreateAndReturnCategory() {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Travel");
        request.setDescription("Travel expenses");

        when(categoryRepository.existsByNameAndActive("Travel", true)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // When
        CategoryResponse result = categoryService.createCategory(request);

        // Then
        assertThat(result.getName()).isEqualTo("Travel");
        assertThat(result.getDescription()).isEqualTo("Travel expenses");
        assertThat(result.getActive()).isTrue();
        verify(categoryRepository).existsByNameAndActive("Travel", true);
        verify(categoryRepository).save(any(Category.class));
        verify(categoryMapper).toResponse(testCategory);
    }

    @Test
    void createCategory_WithDuplicateName_ShouldThrowException() {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Travel");
        request.setDescription("Travel expenses");

        when(categoryRepository.existsByNameAndActive("Travel", true)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Category with name 'Travel' already exists");

        verify(categoryRepository).existsByNameAndActive("Travel", true);
        verify(categoryRepository, org.mockito.Mockito.never()).save(any(Category.class));
    }

    @Test
    void createCategory_WithTrimmedName_ShouldCheckTrimmedName() {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("  Travel  ");
        request.setDescription("Travel expenses");

        when(categoryRepository.existsByNameAndActive("Travel", true)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // When
        CategoryResponse result = categoryService.createCategory(request);

        // Then
        assertThat(result.getName()).isEqualTo("Travel");
        assertThat(result.getDescription()).isEqualTo("Travel expenses");
        assertThat(result.getActive()).isTrue();
        verify(categoryRepository).existsByNameAndActive("Travel", true);
        verify(categoryRepository).save(any(Category.class));
        verify(categoryMapper).toResponse(testCategory);
    }

    @Test
    void deactivateCategory_WithExistingCategory_ShouldDeactivateAndReturnCategory() {
        // Given
        Category activeCategory = new Category();
        activeCategory.setId(categoryId);
        activeCategory.setName("Travel");
        activeCategory.setActive(true);

        Category deactivatedCategory = new Category();
        deactivatedCategory.setId(categoryId);
        deactivatedCategory.setName("Travel");
        deactivatedCategory.setActive(false);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(activeCategory));
        when(categoryRepository.save(activeCategory)).thenReturn(deactivatedCategory);

        // When
        categoryService.deactivateCategory(categoryId);

        // Then
        assertThat(activeCategory.getActive()).isFalse();
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(activeCategory);
    }

    @Test
    void deactivateCategory_WithNonExistentCategory_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.deactivateCategory(categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with ID: " + categoryId);

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, org.mockito.Mockito.never()).save(any(Category.class));
    }

    @Test
    void deactivateCategory_WithAlreadyInactiveCategory_ShouldStillSaveAndReturn() {
        // Given
        Category inactiveCategory = new Category();
        inactiveCategory.setId(categoryId);
        inactiveCategory.setName("Travel");
        inactiveCategory.setActive(false);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(inactiveCategory));
        when(categoryRepository.save(inactiveCategory)).thenReturn(inactiveCategory);

        // When
        categoryService.deactivateCategory(categoryId);

        // Then
        assertThat(inactiveCategory.getActive()).isFalse();
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(inactiveCategory);
    }
}
