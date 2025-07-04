package ru.practicum.category.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Transactional
@Service
@AllArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

  private final EventRepository eventRepository;
  private final CategoryRepository categoryRepository;

  /**
   * Adding new category to the DB by Admin
   */
  @Override
  public CategoryDto addCategory(NewCategoryDto dto) {
    log.info("Validating category dto: {}", dto);
    if (categoryRepository.existsByName(dto.getName())) {
      throw new AlreadyExistsException("Category with name " + dto.getName() + " already exists");
    }

    Category category = CategoryMapper.toCategory(dto);
    categoryRepository.save(category);
    log.info("Category saved: {}", category);

    return CategoryMapper.toCategoryDto(category);
  }

  /**
   * Retrieves all available categories.
   */
  @Override
  @Transactional(readOnly = true)
  public List<CategoryDto> findAllBy(Pageable pageRequest) {
    return categoryRepository.findAll(pageRequest).map(CategoryMapper::toCategoryDto).getContent();
  }

  /**
   * Retrieves Category information with specified ID.
   */
  @Override
  @Transactional(readOnly = true)
  public CategoryDto getCategoryById(Long id) {
    log.info("Get category by id: {}", id);

    return categoryRepository.findById(id)
        .map(CategoryMapper::toCategoryDto)
        .orElseThrow(() -> new NotFoundException("Category with id " + id + " not found"));
  }

  /**
   * Updates category name for a category with specified ID.
   */
  @Override
  public CategoryDto updateCategory(Long id, NewCategoryDto dto) {
    log.info("Update category: {}", dto);
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Category with id " + id + " not found"));
    if (categoryRepository.existsByName(dto.getName()) && !category.getName().equals(dto.getName())) {
      log.warn("Failed to update category. Name '{}' already exists.", dto.getName());
      throw new AlreadyExistsException("Category name already exists.");
    }
    category.setName(dto.getName());
    categoryRepository.save(category);
    return CategoryMapper.toCategoryDto(category);
  }

  /**
   * Deletes category record from the DB, ensuring it is not related to any event.
   */
  @Override
  public void deleteCategory(Long id) {
    log.info("Delete category by id: {}", id);
    if (!categoryRepository.existsById(id)) {
      throw new NotFoundException("Category with id " + id + " not found");
    }
    if (eventRepository.existsByCategoryId(id)) {
      log.warn("Category with id {} is in use by an event and cannot be deleted.", id);
      throw new ConflictException("Cannot be deleted; it's in use by an event.");
    }
    categoryRepository.deleteById(id);
  }
}
