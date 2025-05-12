//jquery
$('document').ready(function () {
    $('.table .btn').on('click', function (event) {
        event.preventDefault();
        const href = $(this).attr('href');
        if ($(this).attr('id') === 'buttonEdit') {

            $.get(href, function (item) {
                $('#formIdEdit').val(item.id);
                $('#formNameEdit').val(item.name);
                $('#formTypeEdit').val(item.type);
                $('#formCountEdit').val(item.count);
                $('#formCostEdit').val(item.costPerUnit);
            });
            $('#editItemModal').modal('show');
        } else if ($(this).attr('id') === 'buttonDelete') {
            $.get(href, function (item) {
                $('#formIdDelete').val(item.id);
                $('#messageDelete').html("Do you want to delete " + item.name + "?");
            });
            $('#deleteItemModal').modal('show');
        }
    });
});