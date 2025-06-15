//jquery
$(document).ready(function () {
    $('.table tbody').on('click', '.btn', function (event) {
        event.preventDefault();
        const $button = $(this);
        const href = $button.attr('href');

        if ($button.hasClass('btn-edit')) {
            const $sortData = $('#sortData');
            const pageNumber = $sortData.data('page-number');
            const pageSize = $sortData.data('page-size');
            const sortField = $sortData.data('sort-field');
            const sortDirection = $sortData.data('sort-direction');
            const keyword = $sortData.data('keyword');
            const $editForm = $('#editItemModal').find('form');

            $editForm.find('input[name="pageNumber"]').val(pageNumber);
            $editForm.find('input[name="pageSize"]').val(pageSize);
            $editForm.find('input[name="sortField"]').val(sortField);
            $editForm.find('input[name="sortDirection"]').val(sortDirection);
            $editForm.find('input[name="keyword"]').val(keyword);
            $.get(href, function (item) {
                $('#formIdEditItem').val(item.id);
                $('#formNameEditItem').val(item.name);
                $('#formTypeEditItem').val(item.type);
                $('#formCountEditItem').val(item.count);
                $('#formCostEditItem').val(item.costPerUnit);
            }).fail(function(jqXHR, textStatus, errorThrown) {
                console.error("Error fetching item data for edit:", textStatus, errorThrown);
                alert("Failed to load item data for editing.");
            });

            const editModalElement = document.getElementById('editItemModal');
            if (editModalElement) {
                const modal = bootstrap.Modal.getOrCreateInstance(editModalElement);
                modal.show();
            } else {
                console.error("Edit modal #editItemModal not found in DOM");
            }

        } else if ($button.hasClass('btn-delete')) {
            const itemId = $button.data('item-id');
            const itemName = $button.data('item-name');
            const modalTargetSelector = $button.data('bs-target');

            if (itemId !== undefined && itemName !== undefined && modalTargetSelector) {
                const modalElement = document.querySelector(modalTargetSelector); // Use vanilla JS selector
                if (modalElement) {
                    const inputIdElement = modalElement.querySelector('#formIdDeleteItem');
                    const messageElement = modalElement.querySelector('#messageDeleteItem');
                    if (inputIdElement) {
                        inputIdElement.value = itemId;
                    } else {
                        console.error("Input #formIdDeleteItem not found inside modal:", modalTargetSelector);
                    }
                    if (messageElement) {
                        messageElement.textContent = `Do you want to delete '${itemName}'?`;
                    } else {
                        console.error("Message element #messageDeleteItem not found inside modal:", modalTargetSelector);
                    }
                    const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
                    modal.show();
                } else {
                    console.error("Delete modal element not found using selector:", modalTargetSelector);
                }
            } else {
                console.error("Missing data-item-id, data-item-name, or data-bs-target on delete button:", $button[0]);
            }
        }
    });
});