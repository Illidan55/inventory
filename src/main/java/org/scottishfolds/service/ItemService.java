package org.scottishfolds.service;

import org.scottishfolds.entity.Item;
import org.scottishfolds.repository.ItemRepository;
import org.scottishfolds.requestDTO.CreateItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for item controller
 */
@Service
public class ItemService {
    private final ItemRepository itemRepository;

    /**
     * Constructor dependency injection for ItemRepository
     *
     * @param itemRepository
     */
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /**
     * Find item by id
     *
     * @param id
     * @return
     */
    public Optional<Item> findById(String id) {
        return itemRepository.findById(id);
    }

    /**
     * Create item using the CreateItem dto
     *
     * @param createItem
     */
    public void createItem(CreateItem createItem) {
        Item item = new Item();
        item.setName(createItem.getName());
        item.setType(createItem.getType());
        item.setCount(0);
        item.setCostPerUnit(createItem.getCostPerUnit());

        itemRepository.save(item);

    }

    /**
     * Save item
     *
     * @param item
     */
    public void save(Item item) {
        itemRepository.save(item);
    }

    /**
     * Delete item by id
     *
     * @param id
     */
    public void deleteById(String id) {
        itemRepository.deleteById(id);
    }

    /**
     * Find a page based on page, size, sortField, sortDirection
     *
     * @param page
     * @param size
     * @param sortField
     * @param sortDirection
     * @return
     */
    public Page<Item> findAll(int page, int size, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return itemRepository.findAll(pageable);
    }

    /**
     * Find a page based on page, size, sortField, sortDirection and keyword
     *
     * Note: Will search both type and name columns
     *
     * @param page
     * @param size
     * @param sortField
     * @param sortDirection
     * @param keyword
     * @return
     */
    public Page<Item> findByKeyWord(int page, int size, String sortField, String sortDirection, String keyword) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return itemRepository.findByKeyword(keyword, pageable);
    }
}
