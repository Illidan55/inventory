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

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Optional<Item> findById(String id) {
        return itemRepository.findById(id);
    }

    public void createItem(CreateItem createItem) {
        Item item = new Item();
        item.setName(createItem.getName());
        item.setType(createItem.getType());
        item.setCount(0);
        item.setCostPerUnit(createItem.getCostPerUnit());

        itemRepository.save(item);

    }

    public void save(Item item) {
        itemRepository.save(item);
    }

    public void deleteById(String id) {
        itemRepository.deleteById(id);
    }

    public Page<Item> findAll(int page, int size, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return itemRepository.findAll(pageable);
    }

    public Page<Item> findByKeyWord(int page, int size, String sortField, String sortDirection, String keyword) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return itemRepository.findByKeyword(keyword, pageable);
    }
}
