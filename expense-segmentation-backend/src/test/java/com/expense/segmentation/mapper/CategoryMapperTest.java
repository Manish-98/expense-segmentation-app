package com.expense.segmentation.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.expense.segmentation.dto.CategoryResponse;
import com.expense.segmentation.model.Category;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CategoryMapperTest {

    private CategoryMapper categoryMapper;
    private Category testCategory;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryMapper = Mockito.mock(CategoryMapper.class);
        categoryId = UUID.randomUUID();

        testCategory = new Category();
        testCategory.setId(categoryId);
        testCategory.setName("Travel");
        testCategory.setDescription("Travel expenses");
        testCategory.setActive(true);
    }

    @Test
    void toResponse_WithCategory_ShouldMapAllFields() {
        // Given
        CategoryResponse expectedResponse = new CategoryResponse();
        expectedResponse.setId(categoryId);
        expectedResponse.setName("Travel");
        expectedResponse.setDescription("Travel expenses");
        expectedResponse.setActive(true);
        expectedResponse.setCreatedAt(LocalDateTime.now());

        when(categoryMapper.toResponse(testCategory)).thenReturn(expectedResponse);

        // When
        CategoryResponse response = categoryMapper.toResponse(testCategory);

        // Then
        assertThat(response.getId()).isEqualTo(categoryId);
        assertThat(response.getName()).isEqualTo("Travel");
        assertThat(response.getDescription()).isEqualTo("Travel expenses");
        assertThat(response.getActive()).isTrue();
    }

    @Test
    void toResponse_WithInactiveCategory_ShouldMapCorrectly() {
        // Given
        testCategory.setActive(false);
        CategoryResponse expectedResponse = new CategoryResponse();
        expectedResponse.setId(categoryId);
        expectedResponse.setName("Travel");
        expectedResponse.setDescription("Travel expenses");
        expectedResponse.setActive(false);

        when(categoryMapper.toResponse(testCategory)).thenReturn(expectedResponse);

        // When
        CategoryResponse response = categoryMapper.toResponse(testCategory);

        // Then
        assertThat(response.getActive()).isFalse();
    }

    @Test
    void toResponse_WithNullCategory_ShouldReturnNull() {
        // Given
        when(categoryMapper.toResponse(null)).thenReturn(null);

        // When
        CategoryResponse response = categoryMapper.toResponse(null);

        // Then
        assertThat(response).isNull();
    }

    @Test
    void toResponseList_WithCategories_ShouldMapAll() {
        // Given
        Category category2 = new Category();
        category2.setId(UUID.randomUUID());
        category2.setName("Meals");
        category2.setDescription("Meal expenses");
        category2.setActive(true);

        List<Category> categories = List.of(testCategory, category2);

        CategoryResponse response1 = new CategoryResponse();
        response1.setId(categoryId);
        response1.setName("Travel");

        CategoryResponse response2 = new CategoryResponse();
        response2.setId(category2.getId());
        response2.setName("Meals");

        List<CategoryResponse> expectedResponses = List.of(response1, response2);
        when(categoryMapper.toResponseList(categories)).thenReturn(expectedResponses);

        // When
        List<CategoryResponse> responses = categoryMapper.toResponseList(categories);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(categoryId);
        assertThat(responses.get(0).getName()).isEqualTo("Travel");
        assertThat(responses.get(1).getName()).isEqualTo("Meals");
    }

    @Test
    void toResponseList_WithEmptyList_ShouldReturnEmptyList() {
        // Given
        List<Category> categories = List.of();
        when(categoryMapper.toResponseList(categories)).thenReturn(List.of());

        // When
        List<CategoryResponse> responses = categoryMapper.toResponseList(categories);

        // Then
        assertThat(responses).isEmpty();
    }

    @Test
    void toResponseList_WithNullList_ShouldReturnNull() {
        // Given
        when(categoryMapper.toResponseList(null)).thenReturn(null);

        // When
        List<CategoryResponse> responses = categoryMapper.toResponseList(null);

        // Then
        assertThat(responses).isNull();
    }

    @Test
    void toResponseList_WithMixedActiveStatus_ShouldMapCorrectly() {
        // Given
        Category inactiveCategory = new Category();
        inactiveCategory.setId(UUID.randomUUID());
        inactiveCategory.setName("Inactive");
        inactiveCategory.setDescription("Inactive category");
        inactiveCategory.setActive(false);

        List<Category> categories = List.of(testCategory, inactiveCategory);

        CategoryResponse response1 = new CategoryResponse();
        response1.setId(categoryId);
        response1.setName("Travel");
        response1.setActive(true);

        CategoryResponse response2 = new CategoryResponse();
        response2.setId(inactiveCategory.getId());
        response2.setName("Inactive");
        response2.setActive(false);

        List<CategoryResponse> expectedResponses = List.of(response1, response2);
        when(categoryMapper.toResponseList(categories)).thenReturn(expectedResponses);

        // When
        List<CategoryResponse> responses = categoryMapper.toResponseList(categories);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getActive()).isTrue();
        assertThat(responses.get(1).getActive()).isFalse();
    }
}
