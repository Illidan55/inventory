//jquery
$(document).ready(function () {
    $('.table tbody').on('click', '.btn', function (event) {
        event.preventDefault();
        const $button = $(this);
        const href = $button.attr('href');

        if ($button.hasClass('btn-edit')) {
            $.get(href, function (sale) {
                const dateObj = new Date(sale.saleDate); // saleDate is the Instant field from backend
                const month = ('0' + (dateObj.getUTCMonth() + 1)).slice(-2); // Months are 0-indexed
                const day = ('0' + dateObj.getUTCDate()).slice(-2);
                const year = dateObj.getUTCFullYear();
                let valueToSet = `${month}-${day}-${year}`;

                $('#formIdEditSale').val(sale.id);
                $('#formSaleDateEditSale').val(valueToSet);
                $('#formNameEditSale').val(sale.name);
                $('#formTypeEditSale').val(sale.type);
                $('#formCountEditSale').val(sale.count);
                $('#formLocationEditSale').val(sale.location);
                $('#formCostEditSale').val(sale.cost);
                $('#formSalePriceEditSale').val(sale.salePrice);
            }).fail(function(jqXHR, textStatus, errorThrown) {
                console.error("Error fetching sale data for edit:", textStatus, errorThrown);
                alert("Failed to load sale data for editing.");
            });

            const editModalElement = document.getElementById('editSaleModal');
            if (editModalElement) {
                const modal = bootstrap.Modal.getOrCreateInstance(editModalElement);
                modal.show();
            } else {
                console.error("Edit modal #editSaleModal not found in DOM");
            }

        } else if ($button.hasClass('btn-delete')) {
            const saleId = $button.data('sale-id');
            const saleName = $button.data('sale-name');
            const modalTargetSelector = $button.data('bs-target');

            if (saleId !== undefined && saleName !== undefined && modalTargetSelector) {
                const modalElement = document.querySelector(modalTargetSelector); // Use vanilla JS selector
                if (modalElement) {
                    const inputIdElement = modalElement.querySelector('#formIdDeleteSale');
                    const messageElement = modalElement.querySelector('#messageDeleteSale');
                    if (inputIdElement) {
                        inputIdElement.value = saleId;
                    } else {
                        console.error("Input #formIdDeleteSale not found inside modal:", modalTargetSelector);
                    }
                    if (messageElement) {
                        messageElement.textContent = `Do you want to delete '${saleName}'?`;
                    } else {
                        console.error("Message element #messageDeleteSale not found inside modal:", modalTargetSelector);
                    }
                    const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
                    modal.show();
                } else {
                    console.error("Delete modal element not found using selector:", modalTargetSelector);
                }
            } else {
                console.error("Missing data-sale-id, data-sale-name, or data-bs-target on delete button:", $button[0]);
            }
        }
    });
});