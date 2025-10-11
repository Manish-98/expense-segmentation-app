package com.expense.segmentation.mapper;

import com.expense.segmentation.dto.CategoryResponse;
import com.expense.segmentation.model.Category;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setActive(category.getActive());
        response.setCreatedAt(category.getCreatedAt());
        return response;
    }

    public List<CategoryResponse> toResponseList(List<Category> categories) {
        return categories.stream().map(this::toResponse).toList();
    }
}
