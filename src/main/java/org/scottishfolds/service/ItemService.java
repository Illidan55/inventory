package org.scottishfolds.service;

import org.scottishfolds.entity.Item;
import org.scottishfolds.repository.ItemRepository;
import org.scottishfolds.requestDTO.CreateItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
     * <p>
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
    /**
     * Imports items from a CSV file.
     * Assumes CSV format: name,type, count, costPerUnit
     *
     * @param file The CSV file to import.
     * @throws IOException If an error occurs during file reading.
     * @throws IllegalArgumentException If the file data is not as expected.
     */
    public void importItemsFromCSV(MultipartFile file) throws IOException {
        List<Item> itemsToSave = new ArrayList<>();
        if (file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty.");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] values = line.split(",");
                if (values.length >= 3) {
                    Item item = new Item();
                    item.setName(values[0].trim());
                    item.setType(values[1].trim());
                    item.setCount(Integer.parseInt(values[2].trim()));
                    try {
                        item.setCostPerUnit(Float.parseFloat(values[3].trim()));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping row due to invalid costPerUnit format: " + line + ". Error: " + e.getMessage());
                        continue;
                    }
                    itemsToSave.add(item);
                } else {
                    System.err.println("Skipping malformed CSV row: " + line);
                }
            }
        }

        if (!itemsToSave.isEmpty()) {
            itemRepository.saveAll(itemsToSave);
        }
    }
}
