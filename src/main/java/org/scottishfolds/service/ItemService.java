package org.scottishfolds.service;

import org.scottishfolds.entity.Item;
import org.scottishfolds.repository.ItemRepository;
import org.scottishfolds.requestDTO.CreateItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<Item> findByCountGreaterThan(int count) {
        return itemRepository.findByCountGreaterThan(count);
    }

    public boolean createItem(CreateItem createItem) {
        Item item = new Item();
        item.setName(createItem.getName());
        item.setType(createItem.getType());
        item.setCount(0);
        item.setCostPerUnit(createItem.getCostPerUnit());

        Item created = itemRepository.save(item);
        if (created != null && created.getId() != null && !created.getId().isEmpty()) {
            return true;
        }
        return false;
    }
    public boolean save(Item item) {
        itemRepository.save(item);
        if (item != null && item.getId() != null && !item.getId().isEmpty()) {
            return true;
        }
        return false;
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }
    public void deleteById(String id) {
        itemRepository.deleteById(id);
    }
    public Page<Item> findByPage(int page, int size, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return itemRepository.findAll(pageable);
    }
}
