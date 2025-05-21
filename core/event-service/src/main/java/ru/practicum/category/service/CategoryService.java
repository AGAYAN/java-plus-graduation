package ru.practicum.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto addCategory(NewCategoryDto dto);

    List<CategoryDto> findAllBy(Pageable pageRequest);

    CategoryDto getCategoryById(Long id);

    CategoryDto updateCategory(Long id, NewCategoryDto dto);

    void deleteCategory(Long id);
}
