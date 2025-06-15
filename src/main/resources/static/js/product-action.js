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
            const $editForm = $('#editProductModal').find('form');

            $editForm.find('input[name="pageNumber"]').val(pageNumber);
            $editForm.find('input[name="pageSize"]').val(pageSize);
            $editForm.find('input[name="sortField"]').val(sortField);
            $editForm.find('input[name="sortDirection"]').val(sortDirection);
            $editForm.find('input[name="keyword"]').val(keyword);
            $.get(href, function (product) {
                console.log("Populating edit modal for product:", product);
                $('#formIdEditProduct').val(product.id);
                $('#formNameEditProduct').val(product.name);
                $('#formTypeEditProduct').val(product.type);
                $('#formInStoreStockEditProduct').val(product.inStoreStock);
                $('#formOnlineStoreStockEditProduct').val(product.onlineStock);
                $('#formBackStockEditProduct').val(product.backStock);
                $('#formInStorePriceEditProduct').val(product.inStorePrice);
                $('#formOnlinePriceEditProduct').val(product.onlinePrice);
            }).fail(function(jqXHR, textStatus, errorThrown) {
                console.error("Error fetching product data for edit:", textStatus, errorThrown);
                alert("Failed to load product data for editing.");
            });

            const editModalElement = document.getElementById('editProductModal');
            if (editModalElement) {
                const modal = bootstrap.Modal.getOrCreateInstance(editModalElement);
                modal.show();
            } else {
                console.error("Edit modal #editProductModal not found in DOM");
            }

        } else if ($button.hasClass('btn-delete')) {
            const productId = $button.data('product-id') || $button.data('item-id');
            const productName = $button.data('product-name') || $button.data('item-name');
            const modalTargetSelector = $button.data('bs-target');
            if (productId !== undefined && productName !== undefined && modalTargetSelector) {
                const modalElement = document.querySelector(modalTargetSelector);
                if (modalElement) {
                    const inputIdElement = modalElement.querySelector('#formIdDeleteProduct');
                    const messageElement = modalElement.querySelector('#messageDeleteProduct');
                    if (inputIdElement) {
                        inputIdElement.value = productId;
                    } else {
                        console.error("Input #formIdDeleteProduct not found inside modal:", modalTargetSelector);
                    }
                    if (messageElement) {
                        messageElement.textContent = `Do you want to delete '${productName}'?`;
                    } else {
                        console.error("Message element #messageDeleteProduct not found inside modal:", modalTargetSelector);
                    }
                    const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
                    modal.show();
                } else {
                    console.error("Delete modal element not found using selector:", modalTargetSelector);
                }
            } else {
                console.error("Missing data attribute(s) or data-bs-target on delete button:", {
                    'data-product-id': $button.data('product-id'),
                    'data-item-id': $button.data('item-id'),
                    'data-product-name': $button.data('product-name'),
                    'data-item-name': $button.data('item-name'),
                    'data-bs-target': $button.data('bs-target'),
                    'buttonElement': $button[0]
                });
            }
        }
    });
});