$('document').ready(function () {
    $('.table .btn').on('click', function (event) {
        event.preventDefault();
        const href = $(this).attr('href');
        if ($(this).attr('id') === 'buttonEdit') {

            $.get(href, function (product) {
                $('#formIdEdit').val(product.id);
                $('#formNameEdit').val(product.name);
                $('#formTypeEdit').val(product.type);
                $('#formInStoreStockEdit').val(product.inStoreStock);
                $('#formOnlineStoreStockEdit').val(product.onlineStock);
                $('#formBackStockEdit').val(product.backStock);
                $('#formInStorePriceEdit').val(product.inStorePrice);
                $('#formOnlinePriceEdit').val(product.onlinePrice);

            });

            $('#editProductModal').modal('show');
        } else if ($(this).attr('id') === 'buttonDelete') {
            $.get(href, function (product) {
                $('#formIdDelete').val(product.id);
                $('#messageDelete').html("Do you want to delete " + product.name + "?");
            });

            $('#deleteProductModal').modal('show');
        }
    });

});